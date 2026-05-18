package com.gym.security;

import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.*;

import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.*;

@Slf4j
@Component
public class JwtUtil {

    private final java.security.PrivateKey privateKey;
    private final java.security.PublicKey publicKey;
    private final long expirationMs;
    private final long refreshExpirationMs;
    private final JWKSet jwkSet;

    public JwtUtil(
            @Value("${jwt.private-key:DEFAULT}") String privateKeyPem,
            @Value("${jwt.public-key:DEFAULT}") String publicKeyPem,
            @Value("${jwt.private-key-path:#{null}}") String privateKeyPath,
            @Value("${jwt.public-key-path:#{null}}") String publicKeyPath,
            @Value("${jwt.expiration-ms}") long expirationMs,
            @Value("${jwt.refresh-expiration-ms}") long refreshExpirationMs) {
        
        boolean hasKeyStrings = !"DEFAULT".equals(privateKeyPem) && !"DEFAULT".equals(publicKeyPem) 
                && privateKeyPem != null && !privateKeyPem.isBlank()
                && publicKeyPem != null && !publicKeyPem.isBlank();
        
        boolean hasKeyPaths = privateKeyPath != null && !privateKeyPath.isBlank()
                && publicKeyPath != null && !publicKeyPath.isBlank();
        
        if (hasKeyPaths) {
            log.info("Loading JWT keys from files: {} and {}", privateKeyPath, publicKeyPath);
            try {
                this.privateKey = loadPrivateKeyFromFile(privateKeyPath);
                this.publicKey = loadPublicKeyFromFile(publicKeyPath);
            } catch (Exception e) {
                throw new IllegalStateException("Failed to load JWT keys from files", e);
            }
        } else if (hasKeyStrings) {
            log.info("Loading JWT keys from environment variables");
            this.privateKey = parsePrivateKey(privateKeyPem);
            this.publicKey = parsePublicKey(publicKeyPem);
        } else {
            log.info("No JWT keys configured - generating development keys (configure JWT_PRIVATE_KEY/PUBLIC_KEY for production)");
            try {
                KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
                generator.initialize(2048);
                KeyPair keyPair = generator.generateKeyPair();
                this.privateKey = keyPair.getPrivate();
                this.publicKey = keyPair.getPublic();
            } catch (NoSuchAlgorithmException e) {
                throw new IllegalStateException("Failed to generate RSA key pair", e);
            }
        }
        
        this.expirationMs = expirationMs;
        this.refreshExpirationMs = refreshExpirationMs;
        
        RSAKey rsaKey = new RSAKey.Builder((RSAPublicKey) this.publicKey)
                .privateKey((RSAPrivateKey) this.privateKey)
                .keyID(UUID.randomUUID().toString())
                .build();
        
        this.jwkSet = new JWKSet(rsaKey);
    }

    public String generateAccessToken(UUID userId, String email, String role) {
        String keyId = jwkSet.getKeys().get(0).getKeyID();
        
        return Jwts.builder()
            .subject(userId.toString())
            .claim("email", email)
            .claim("role", role)
            .claim("typ", "at+jwt")
            .claim("kid", keyId)
            .id(UUID.randomUUID().toString())
            .issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis() + expirationMs))
            .signWith(privateKey, Jwts.SIG.RS256)
            .compact();
    }

    public String generateRefreshToken(UUID userId) {
        return Jwts.builder()
            .subject(userId.toString())
            .claim("typ", "rt+jwt")
            .id(UUID.randomUUID().toString())
            .issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis() + refreshExpirationMs))
            .signWith(privateKey, Jwts.SIG.RS256)
            .compact();
    }

    public String generatePreAuthToken(UUID userId, String email) {
        return Jwts.builder()
            .subject(userId.toString())
            .claim("email", email)
            .claim("typ", "pre")
            .id(UUID.randomUUID().toString())
            .issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis() + 300000))
            .signWith(privateKey, Jwts.SIG.RS256)
            .compact();
    }

    public UUID extractUserId(String token) {
        return UUID.fromString(extractClaim(token, Claims::getSubject));
    }

    public String extractRole(String token) {
        return extractClaim(token, claims -> claims.get("role", String.class));
    }

    public String extractEmail(String token) {
        return extractClaim(token, claims -> claims.get("email", String.class));
    }

    public String extractJti(String token) {
        return extractClaim(token, Claims::getId);
    }

    public String extractKid(String token) {
        return extractClaim(token, claims -> claims.get("kid", String.class));
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, ClaimsResolver<T> resolver) {
        Claims claims = extractAllClaims(token);
        return resolver.resolve(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
            .verifyWith(publicKey)
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }

    public boolean isTokenExpired(String token) {
        try {
            return extractExpiration(token).before(new Date());
        } catch (JwtException e) {
            return true;
        }
    }

    public boolean validateToken(String token) {
        try {
            extractAllClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.debug("Invalid JWT token: {}", e.getMessage());
            return false;
        }
    }

    public boolean validateTokenWithKeyId(String token, String expectedKeyId) {
        try {
            String kid = extractKid(token);
            if (!expectedKeyId.equals(kid)) {
                log.debug("Token key ID mismatch: expected {}, got {}", expectedKeyId, kid);
                return false;
            }
            return validateToken(token);
        } catch (JwtException e) {
            log.debug("Token validation failed: {}", e.getMessage());
            return false;
        }
    }

    public String getCurrentKeyId() {
        return jwkSet.getKeys().get(0).getKeyID();
    }

    public String getJwkSetJson() {
        return jwkSet.toPublicJWKSet().toJSONObject().toString();
    }

    public long getExpirationMs() {
        return expirationMs;
    }

    public long getRefreshExpirationMs() {
        return refreshExpirationMs;
    }

    private java.security.PrivateKey parsePrivateKey(String pem) {
        try {
            String cleanedPem = pem.replace("\\n", "\n");
            cleanedPem = cleanedPem
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s", "");
            byte[] decoded = Base64.getDecoder().decode(cleanedPem);
            KeyFactory factory = KeyFactory.getInstance("RSA");
            return factory.generatePrivate(new java.security.spec.PKCS8EncodedKeySpec(decoded));
        } catch (Exception e) {
            throw new IllegalStateException("Failed to parse RSA private key", e);
        }
    }

    private java.security.PublicKey parsePublicKey(String pem) {
        try {
            String cleanedPem = pem.replace("\\n", "\n");
            cleanedPem = cleanedPem
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s", "");
            byte[] decoded = Base64.getDecoder().decode(cleanedPem);
            KeyFactory factory = KeyFactory.getInstance("RSA");
            return factory.generatePublic(new java.security.spec.X509EncodedKeySpec(decoded));
        } catch (Exception e) {
            throw new IllegalStateException("Failed to parse RSA public key", e);
        }
    }
    
    private java.security.PrivateKey loadPrivateKeyFromFile(String path) {
        try {
            String pem = new String(java.nio.file.Files.readAllBytes(java.nio.file.Paths.get(path)));
            return parsePrivateKey(pem);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to load private key from: " + path, e);
        }
    }
    
    private java.security.PublicKey loadPublicKeyFromFile(String path) {
        try {
            String pem = new String(java.nio.file.Files.readAllBytes(java.nio.file.Paths.get(path)));
            return parsePublicKey(pem);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to load public key from: " + path, e);
        }
    }

    public static Map<String, String> generateKeyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        KeyPair keyPair = generator.generateKeyPair();
        
        RSAPrivateKey rsaPrivateKey = (RSAPrivateKey) keyPair.getPrivate();
        RSAPublicKey rsaPublicKey = (RSAPublicKey) keyPair.getPublic();
        
        String privateKeyPem = "-----BEGIN RSA PRIVATE KEY-----\n" +
            Base64.getEncoder().encodeToString(rsaPrivateKey.getEncoded()) +
            "\n-----END RSA PRIVATE KEY-----";
        
        String publicKeyPem = "-----BEGIN RSA PUBLIC KEY-----\n" +
            Base64.getEncoder().encodeToString(rsaPublicKey.getEncoded()) +
            "\n-----END RSA PUBLIC KEY-----";
        
        Map<String, String> keys = new HashMap<>();
        keys.put("privateKey", privateKeyPem);
        keys.put("publicKey", publicKeyPem);
        
        return keys;
    }

    @FunctionalInterface
    public interface ClaimsResolver<T> {
        T resolve(Claims claims);
    }
}