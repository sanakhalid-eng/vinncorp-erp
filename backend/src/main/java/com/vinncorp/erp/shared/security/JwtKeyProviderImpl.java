package com.vinncorp.erp.shared.security;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Component
public class JwtKeyProviderImpl implements JwtKeyProvider {

    private static final Logger log = LoggerFactory.getLogger(JwtKeyProviderImpl.class);

    private final String privateKeyBase64;
    private final String publicKeyBase64;

    private PrivateKey privateKey;
    private PublicKey publicKey;

    public JwtKeyProviderImpl(
            @Value("${jwt.private-key}") String privateKeyBase64,
            @Value("${jwt.public-key}") String publicKeyBase64
    ) {
        this.privateKeyBase64 = privateKeyBase64;
        this.publicKeyBase64 = publicKeyBase64;
    }

    @PostConstruct
    public void init() {
        try {
            if (privateKeyBase64 != null && !privateKeyBase64.isEmpty()
                    && publicKeyBase64 != null && !publicKeyBase64.isEmpty()) {
                KeyFactory keyFactory = KeyFactory.getInstance("RSA");
                byte[] privateKeyBytes = Base64.getDecoder().decode(privateKeyBase64);
                PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
                this.privateKey = keyFactory.generatePrivate(privateKeySpec);
                byte[] publicKeyBytes = Base64.getDecoder().decode(publicKeyBase64);
                X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(publicKeyBytes);
                this.publicKey = keyFactory.generatePublic(publicKeySpec);
                log.info("JWT key pair loaded from configuration");
            } else {
                log.warn("JWT_PRIVATE_KEY_BASE64/JWT_PUBLIC_KEY_BASE64 not set, generating ephemeral key pair");
                KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
                generator.initialize(2048);
                KeyPair keyPair = generator.generateKeyPair();
                this.privateKey = keyPair.getPrivate();
                this.publicKey = keyPair.getPublic();
            }
        } catch (Exception e) {
            log.warn("Failed to decode JWT_PRIVATE_KEY_BASE64/JWT_PUBLIC_KEY_BASE64, generating ephemeral key pair: {}", e.getMessage());
            try {
                KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
                generator.initialize(2048);
                KeyPair keyPair = generator.generateKeyPair();
                this.privateKey = keyPair.getPrivate();
                this.publicKey = keyPair.getPublic();
            } catch (Exception ex) {
                throw new RuntimeException("Failed to generate ephemeral JWT key pair", ex);
            }
        }
    }

    @Override
    public PrivateKey getPrivateKey() {
        return privateKey;
    }

    @Override
    public PublicKey getPublicKey() {
        return publicKey;
    }
}
