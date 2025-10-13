# DocumentVerificationService

## Version 0.1.0


A Java Spring Boot service for document intake and LLM-based decision parsing.

## Features

- Subscribes to various events from the solace cloud event mesh and solace agend mesh.
- Processes document validation using solace agent mesh and gpt-4o llm 
- Parses LLM response
- Publishes document-fail and document-verified events to solace event mesh.

