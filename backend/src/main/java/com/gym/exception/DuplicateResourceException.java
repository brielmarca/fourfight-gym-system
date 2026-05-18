package com.gym.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import java.net.URI;

public class DuplicateResourceException extends RuntimeException {
    private final String resourceType;
    private final String field;

    public DuplicateResourceException(String message) {
        super(message);
        this.resourceType = "Resource";
        this.field = "field";
    }

    public DuplicateResourceException(String resourceType, String field, Object value) {
        super(String.format("%s already exists with %s: %s", resourceType, field, value));
        this.resourceType = resourceType;
        this.field = field;
    }

    public ProblemDetail toProblemDetail() {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, getMessage());
        problem.setTitle("Duplicate Resource");
        problem.setType(URI.create("https://api.gym.com/errors/duplicate"));
        problem.setProperty("resourceType", resourceType);
        problem.setProperty("field", field);
        return problem;
    }
}