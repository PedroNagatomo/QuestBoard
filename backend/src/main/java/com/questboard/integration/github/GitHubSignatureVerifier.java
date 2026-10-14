package com.questboard.integration.github;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;

@Component
@Slf4j
public class GitHubSignatureVerifier {

    private final byte[] secret;

    public GitHubSignatureVerifier(@Value("${app.integrations.github.webhook-secret}") String secret) {
        this.secret = secret.getBytes(StandardCharsets.UTF_8);
    }

    public boolean verify(String body, String signatureHeader) {
        if (signatureHeader == null || !signatureHeader.startsWith("sha256=")) return false;
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret, "HmacSHA256"));
            byte[] hash = mac.doFinal(body.getBytes(StandardCharsets.UTF_8));
            String computed = "sha256=" + HexFormat.of().formatHex(hash);

            // comparação em tempo constante
            return MessageDigest.isEqual(
                    computed.getBytes(StandardCharsets.UTF_8),
                    signatureHeader.getBytes(StandardCharsets.UTF_8)
            );
        } catch (Exception e) {
            log.warn("Erro ao verificar assinatura GitHub: {}", e.getMessage());
            return false;
        }
    }
}