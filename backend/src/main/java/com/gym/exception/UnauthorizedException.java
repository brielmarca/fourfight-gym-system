package com.gym.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import java.net.URI;

public class UnauthorizedException extends RuntimeException {
    public UnauthorizedException(String message) {
        super(message);
    }

    public static UnauthorizedException invalidCredentials() {
        return new UnauthorizedException("Invalid email or password");
    }

    public static UnauthorizedException tokenExpired() {
        return new UnauthorizedException("Token has expired");
    }

    public static UnauthorizedException tokenRevoked() {
        return new UnauthorizedException("Token has been revoked");
    }

    public ProblemDetail toProblemDetail() {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, getMessage());
        problem.setTitle("Unauthorized");
        problem.setType(URI.create("https://api.gym.com/errors/unauthorized"));
        return problem;
    }
}