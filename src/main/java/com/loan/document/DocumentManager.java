package com.loan.document;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.stream.binder.BinderHeaders;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.loan.ApplicationSubmitEvent;
import com.loan.model.LoanApplication;
import com.solacesystems.jcsmp.Topic;

/**
 * DocumentManager handles all document related events, llm communication and pdf processing
 */
@Service
public class DocumentManager {

	private static final Logger logger = LoggerFactory.getLogger(DocumentManager.class);
	private final StreamBridge streamBridge;
	private final ObjectMapper mapper;
	private static final Set<String> PDF_EXT = Set.of(".pdf");
    private static String requestTopic = "loan-project/solace-agent-mesh/v1/llm-service/request/general-good/app1";
	private static String replyTopic = "loan-project/solace-agent-mesh/v1/llm-service/response/app1";
	@Value("${app.uploads.base-dir}")
	private String directoryPath;
	@Value("${ai.prompts.validate}")
	private String prompt;
	
	public DocumentManager(StreamBridge streamBridge) {
		this.streamBridge = streamBridge;
		this.mapper = new ObjectMapper();
	}

	
/**
 * Responds to loanSubmit event
 * @return
 */
	@Bean
	public Consumer<ApplicationSubmitEvent> loanSubmit() {
		return data -> {
			logger.info("Loan Submit event recieved");
			LoanApplication loan = data.getLoanApplication();

			List<URI> filesUriList = null;
			try {
				filesUriList = getFiles();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
				try {
				sendValidatePackage(loan.getLoanId(), filesUriList, prompt, requestTopic, 
						replyTopic, 
						streamBridge);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
	
		};
	}

	private List<URI> getFiles() throws IOException {
		
	    
		Path dir = Paths.get(directoryPath);
		if (!Files.isDirectory(dir)) {
			throw new IllegalArgumentException("Not a valid directory: " + directoryPath);
		}
		DirectoryStream<Path> stream = null;
		List<URI> uris = new ArrayList<>();
		try {
			stream = Files.newDirectoryStream(dir);

			for (Path path : stream) {
				if (Files.isRegularFile(path)) {
					uris.add(path.toUri());
				}
			}

		} finally {
			stream.close();
		}

		return uris;

	}

	/**
	 * Responds to llm response. Validates the response and publishes next event
	 * @return
	 */
	@Bean
	public Consumer<Message<Map<String, Object>>> aiConnect() {
		return data -> {

			logger.info("AI response event recieved");
			Object payload = data.getPayload();
			String contentText = null;
			String loanId = null;
			
			String destination = getDestinationName(data);
			 if (destination != null && destination.startsWith(replyTopic)) {
	                // Extract loanId from topic: replyTopic/{loanId}
	               loanId = destination.substring(replyTopic.length()).substring(1);
			  }
			if (payload instanceof Map) {
				@SuppressWarnings("unchecked")
				Map<String, Object> map = (Map<String, Object>) payload;
				Object content = map.get("content");
				contentText = (content != null) ? content.toString() : null;
			} else {
				
				contentText = String.valueOf(payload);
			}
			Decision decision = LlmDecisionParser.parseDecision(contentText);
			boolean pass = decision == Decision.PASS;
			
			if (pass) {
				
				String topic = String.format("loans/originationservices/documents/verified/%s",
						loanId);
				streamBridge.send("documentVerified-out-0",
			    	    MessageBuilder.withPayload(data)
			    	        .setHeader(BinderHeaders.TARGET_DESTINATION, topic)
			    	        .build());
				
			} else {
				String topic = String.format("loans/originationservices/documents/failed/%s",
						loanId);
		    	streamBridge.send("documentFailed-out-0",
	    	    MessageBuilder.withPayload(data)
	    	        .setHeader(BinderHeaders.TARGET_DESTINATION, topic)
	    	        .build());
			//	  streamBridge.send(topic, data);
			}
		};
	}

	private boolean isPdf(URI uri) {
		String p = Optional.ofNullable(uri.getPath()).orElse("").toLowerCase(Locale.ROOT);
		return PDF_EXT.stream().anyMatch(p::endsWith);
	}

	/**
	 * Communicates with Solace AIConnector llm. sends request
	 * @param loanId
	 * @param fileUris
	 * @param prompt
	 * @param requestTopic
	 * @param replyTopic
	 * @param streamBridge
	 * @throws Exception
	 */
	public void sendValidatePackage(String loanId, List<URI> fileUris, String prompt, String requestTopic, 
			String replyTopic, 
			StreamBridge streamBridge) throws Exception {
		StringBuilder sb = new StringBuilder();
		
		List<Map<String, Object>> docs = new ArrayList<>();
		for (int i = 0; i < fileUris.size(); i++) {
			URI uri = fileUris.get(i);
			
			if (!isPdf(uri)) {
				
				continue;
			}
			byte[] bytes = readBytesFromUri(uri); 
			String b64 = Base64.getEncoder().encodeToString(bytes);

			  String text = truncate(extractPdfText(bytes), 8000); 
			  sb.append("\n\n--- Document: ").append(extractFileName(uri)).append(" ---\n")
			    .append(text);

			Map<String, Object> doc = new HashMap<>();
			doc.put("type", "base64");
			doc.put("id", "DOC-" + (i + 1));
			doc.put("content", b64);
			doc.put("mimeType", "application/pdf");
			doc.put("fileName", extractFileName(uri)); 
			docs.add(doc);
		}

		if (docs.isEmpty()) {
			throw new IllegalArgumentException("No valid PDF documents to send.");
		}

			
		List<Map<String, Object>> messages = List.of(
				Map.of("role", "system", "content", "You are a precise assistant for loan document validation."),
				Map.of("role", "user", "content", prompt + sb.toString()));

		
		Map<String, Object> payload = new HashMap<>();
		payload.put("task", "validate_package"); // optional 
		payload.put("loanId", loanId);
		payload.put("batchId", UUID.randomUUID().toString());
		payload.put("documents", docs);
		payload.put("messages", messages); // 

		if (replyTopic == null || replyTopic.isBlank()) {
			throw new IllegalArgumentException("replyTopic must be set (non-empty)");
		}
		if (requestTopic == null || requestTopic.isBlank()) {
			throw new IllegalArgumentException("requestTopic must be set (non-empty)");
		}

		Message<Map<String, Object>> msg = MessageBuilder.withPayload(payload)
				// tell LLM service where to reply
				.setHeader("__solace_ai_connector_broker_request_response_topic__", replyTopic+"/"+loanId).build();

		
		streamBridge.send("aiConnect-out-0",
				MessageBuilder.fromMessage(msg).setHeader("solace_destination", requestTopic+"/"+loanId).build());
	}

	

	private String extractFileName(URI uri) {
		String path = uri.getPath();
		if (path == null || path.isBlank())
			return "unknown";
		int idx = path.lastIndexOf('/');
		return (idx >= 0 && idx < path.length() - 1) ? path.substring(idx + 1) : path;
	}

	private byte[] readBytesFromUri(URI uri) throws Exception {
		if ("file".equalsIgnoreCase(uri.getScheme())) {
			// Local file
			Path path = Paths.get(uri);
			return Files.readAllBytes(path);
		} else if ("http".equalsIgnoreCase(uri.getScheme()) || "https".equalsIgnoreCase(uri.getScheme())) {
			// Remote file via HTTP/HTTPS
			try (InputStream in = uri.toURL().openStream()) {
				return in.readAllBytes();
			}
		} else {
			throw new IllegalArgumentException("Unsupported URI scheme: " + uri.getScheme());
		}
	}
	static String extractPdfText(byte[] pdfBytes) throws IOException {
		  try (PDDocument doc = PDDocument.load(pdfBytes)) {
		    PDFTextStripper stripper = new PDFTextStripper();
		    return stripper.getText(doc);
		  }
		}

		static String truncate(String s, int max) {
		  if (s == null) return "";
		  return s.length() <= max ? s : s.substring(0, max) + " ...[truncated]";
		}
		private static String getDestinationName(Message<?> msg) {
		    Object val = msg.getHeaders().get("solace_destination");
		    if (val instanceof String) return (String) val;
		    if (val instanceof Topic)  return ((Topic) val).getName();
		    return val != null ? val.toString() : null;
		}

}
