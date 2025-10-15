## ---------Architecture Diagram---------------

https://www.figma.com/board/lUSLTNd6bkNYCwGmHZawGJ/Loan-Processing-Flow-with-Solace?node-id=0-1&t=vbeEO5UROfnxwhhf-1

# DocumentVerificationService

## Version 0.1.0


A Java Spring Boot service for document intake and LLM-based decision parsing.

## Features

- Subscribes to various events from the solace cloud event mesh and solace agend mesh.
- Processes document validation using solace agent mesh and gpt-4o llm 
- Parses LLM response
- Publishes document-fail and document-verified events to solace event mesh.

## How to run
- Update Configuration

  Open src/main/resources/application.yml.

Change Solace connection variables (host, msgVpn, clientUsername, clientPassword) to match your environment.
Change file upload directory to match yours

 - Build the Project

   mvn clean install -DskipTests


 - Run the Application

   mvn spring-boot:run
