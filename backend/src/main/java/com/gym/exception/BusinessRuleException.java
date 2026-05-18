package com.gym.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import java.net.URI;

public class BusinessRuleException extends RuntimeException {
    private final String code;

    public BusinessRuleException(String code, String message) {
        super(message);
        this.code = code;
    }

    public BusinessRuleException(String message) {
        super(message);
        this.code = "BUSINESS_RULE";
    }

    public ProblemDetail toProblemDetail() {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_ENTITY, getMessage());
        problem.setTitle("Business Rule Violation");
        problem.setType(URI.create("https://api.gym.com/errors/business-rule"));
        problem.setProperty("code", code);
        return problem;
    }
}