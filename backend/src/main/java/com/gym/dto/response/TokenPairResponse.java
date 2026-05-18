package com.gym.dto.response;

public record TokenPairResponse(
    String accessToken,
    String refreshToken,
    String tokenType,
    Long expiresIn
) {
    public static TokenPairResponse of(String accessToken, String refreshToken, long expiresIn) {
        return new TokenPairResponse(
            accessToken,
            refreshToken,
            "Bearer",
            expiresIn
        );
    }
}