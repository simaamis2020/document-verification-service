package com.loan.document;

import com.fasterxml.jackson.annotation.JsonInclude;



@JsonInclude(JsonInclude.Include.NON_NULL)
public class DocumentFail {

	public DocumentFail () {
	}

	public DocumentFail (
		String eventId, 
		String sourceSystem, 
		Document document, 
		String correlationId, 
		EventType eventType, 
		java.time.OffsetDateTime timestamp) {
		this.eventId = eventId;
		this.sourceSystem = sourceSystem;
		this.document = document;
		this.correlationId = correlationId;
		this.eventType = eventType;
		this.timestamp = timestamp;
	}

	private String eventId;
	private String sourceSystem;
	private Document document;
	private String correlationId;
	private EventType eventType;
	private java.time.OffsetDateTime timestamp;
	public String getEventId() {
		return eventId;
	}

	public DocumentFail setEventId(String eventId) {
		this.eventId = eventId;
		return this;
	}


	public String getSourceSystem() {
		return sourceSystem;
	}

	public DocumentFail setSourceSystem(String sourceSystem) {
		this.sourceSystem = sourceSystem;
		return this;
	}


	public Document getDocument() {
		return document;
	}

	public DocumentFail setDocument(Document document) {
		this.document = document;
		return this;
	}



	@JsonInclude(JsonInclude.Include.NON_NULL)
	public static class Document {

		public Document () {
		}

		public Document (
			Boolean retryable, 
			java.time.OffsetDateTime failedAt, 
			FailureCode failureCode, 
			String documentType, 
			String provider, 
			String failureReason, 
			String customerId, 
			String documentId, 
			Details details, 
			String loanId, 
			Status status, 
			Integer attempts) {
			this.retryable = retryable;
			this.failedAt = failedAt;
			this.failureCode = failureCode;
			this.documentType = documentType;
			this.provider = provider;
			this.failureReason = failureReason;
			this.customerId = customerId;
			this.documentId = documentId;
			this.details = details;
			this.loanId = loanId;
			this.status = status;
			this.attempts = attempts;
		}

		private Boolean retryable;
		private java.time.OffsetDateTime failedAt;
		private FailureCode failureCode;
		private String documentType;
		private String provider;
		private String failureReason;
		private String customerId;
		private String documentId;
		private Details details;
		private String loanId;
		private Status status;
		private Integer attempts;
		public Boolean getRetryable() {
			return retryable;
		}

		public Document setRetryable(Boolean retryable) {
			this.retryable = retryable;
			return this;
		}


		public java.time.OffsetDateTime getFailedAt() {
			return failedAt;
		}

		public Document setFailedAt(java.time.OffsetDateTime failedAt) {
			this.failedAt = failedAt;
			return this;
		}


		public FailureCode getFailureCode() {
			return failureCode;
		}

		public Document setFailureCode(FailureCode failureCode) {
			this.failureCode = failureCode;
			return this;
		}


		public static enum FailureCode { DOCUMENT_EXPIRED,DOCUMENT_TAMPERED,LOW_QUALITY_IMAGE,MISMATCH_INFORMATION,UNSUPPORTED_FORMAT,MISSING_PAGES,PROVIDER_ERROR,TIMEOUT,OTHER }
		public String getDocumentType() {
			return documentType;
		}

		public Document setDocumentType(String documentType) {
			this.documentType = documentType;
			return this;
		}


		public String getProvider() {
			return provider;
		}

		public Document setProvider(String provider) {
			this.provider = provider;
			return this;
		}


		public String getFailureReason() {
			return failureReason;
		}

		public Document setFailureReason(String failureReason) {
			this.failureReason = failureReason;
			return this;
		}


		public String getCustomerId() {
			return customerId;
		}

		public Document setCustomerId(String customerId) {
			this.customerId = customerId;
			return this;
		}


		public String getDocumentId() {
			return documentId;
		}

		public Document setDocumentId(String documentId) {
			this.documentId = documentId;
			return this;
		}


		public Details getDetails() {
			return details;
		}

		public Document setDetails(Details details) {
			this.details = details;
			return this;
		}



		@JsonInclude(JsonInclude.Include.NON_NULL)
		public static class Details {

			public Details () {
			}


			public String toString() {
				return "Details ["
				+ " ]";
			}
		}

		public String getLoanId() {
			return loanId;
		}

		public Document setLoanId(String loanId) {
			this.loanId = loanId;
			return this;
		}


		public Status getStatus() {
			return status;
		}

		public Document setStatus(Status status) {
			this.status = status;
			return this;
		}


		public static enum Status { failed }
		public Integer getAttempts() {
			return attempts;
		}

		public Document setAttempts(Integer attempts) {
			this.attempts = attempts;
			return this;
		}

		public String toString() {
			return "Document ["
			+ " retryable: " + retryable
			+ " failedAt: " + failedAt
			+ " failureCode: " + failureCode
			+ " documentType: " + documentType
			+ " provider: " + provider
			+ " failureReason: " + failureReason
			+ " customerId: " + customerId
			+ " documentId: " + documentId
			+ " details: " + details
			+ " loanId: " + loanId
			+ " status: " + status
			+ " attempts: " + attempts
			+ " ]";
		}
	}

	public String getCorrelationId() {
		return correlationId;
	}

	public DocumentFail setCorrelationId(String correlationId) {
		this.correlationId = correlationId;
		return this;
	}


	public EventType getEventType() {
		return eventType;
	}

	public DocumentFail setEventType(EventType eventType) {
		this.eventType = eventType;
		return this;
	}


	public static enum EventType {DOCUMENT_VERIFICATION_FAILED}
	public java.time.OffsetDateTime getTimestamp() {
		return timestamp;
	}

	public DocumentFail setTimestamp(java.time.OffsetDateTime timestamp) {
		this.timestamp = timestamp;
		return this;
	}

	public String toString() {
		return "DocumentFail ["
		+ " eventId: " + eventId
		+ " sourceSystem: " + sourceSystem
		+ " document: " + document
		+ " correlationId: " + correlationId
		+ " eventType: " + eventType
		+ " timestamp: " + timestamp
		+ " ]";
	}
}
