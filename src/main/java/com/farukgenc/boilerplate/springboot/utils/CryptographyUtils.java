package com.farukgenc.boilerplate.springboot.utils;

import org.springframework.stereotype.Component;
import javax.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.Base64;

@Component
public class CryptographyUtils {

    private KeyPair ed25519KeyPair;

    @PostConstruct
    public void init() {
        try {
            KeyPairGenerator kpg = KeyPairGenerator.getInstance("Ed25519");
            ed25519KeyPair = kpg.generateKeyPair();
            // In a real application, the private key must be stored securely
            // and the public key might be distributed.
            // For this example, we generate it on startup.
            System.out.println("ED25519 Public Key (Base64): " + Base64.getEncoder().encodeToString(ed25519KeyPair.getPublic().getEncoded()));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Failed to initialize ED25519 key pair", e);
        }
    }

    public String generateSHA256Hash(String data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Failed to generate SHA-256 hash", e);
            // Or handle more gracefully depending on application requirements
        }
    }

    public String signWithED25519(String dataToSign) {
        if (ed25519KeyPair == null) {
            throw new IllegalStateException("ED25519 KeyPair not initialized.");
        }
        try {
            Signature sig = Signature.getInstance("EdDSA"); // "EdDSA" is the standard name for Ed25519 in Java
            sig.initSign(ed25519KeyPair.getPrivate());
            sig.update(dataToSign.getBytes(StandardCharsets.UTF_8));
            byte[] signatureBytes = sig.sign();
            return Base64.getEncoder().encodeToString(signatureBytes);
        } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
            throw new RuntimeException("Failed to sign data with ED25519", e);
        }
    }

    // Optional: Method to verify signature (for consumers or testing)
    public boolean verifyED25519Signature(String data, String signatureB64, PublicKey publicKey) {
        try {
            Signature sig = Signature.getInstance("EdDSA");
            sig.initVerify(publicKey);
            sig.update(data.getBytes(StandardCharsets.UTF_8));
            return sig.verify(Base64.getDecoder().decode(signatureB64));
        } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
            // Log error or handle
            System.err.println("Error verifying signature: " + e.getMessage());
            return false;
        }
    }

    public PublicKey getPublicKey() {
        if (ed25519KeyPair == null) {
            throw new IllegalStateException("ED25519 KeyPair not initialized.");
        }
        return ed25519KeyPair.getPublic();
    }
}
