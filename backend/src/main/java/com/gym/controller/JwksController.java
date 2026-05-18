package com.gym.controller;

import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.gym.security.JwtUtil;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/.well-known")
@RequiredArgsConstructor
public class JwksController {

    private final JwtUtil jwtUtil;

    @GetMapping("/jwks.json")
    public ResponseEntity<Map<String, Object>> getJwks() {
        String jwksJson = jwtUtil.getJwkSetJson();
        
        Map<String, Object> jwks = Map.of(
            "keys", new Object[]{jwksJson}
        );
        
        return ResponseEntity.ok(jwks);
    }
}