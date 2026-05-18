package com.gym.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import java.net.URI;
import java.util.Map;

public class ResourceNotFoundException extends RuntimeException {
    private final String resourceType;
    private final Object identifier;

    public ResourceNotFoundException(String resourceType, Object identifier) {
        super(String.format("%s not found with identifier: %s", resourceType, identifier));
        this.resourceType = resourceType;
        this.identifier = identifier;
    }

    public ProblemDetail toProblemDetail() {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, getMessage());
        problem.setTitle("Resource Not Found");
        problem.setType(URI.create("https://api.gym.com/errors/not-found"));
        problem.setProperty("resourceType", resourceType);
        problem.setProperty("identifier", identifier);
        return problem;
    }
}