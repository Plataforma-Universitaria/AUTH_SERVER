package br.ueg.tc.auth.server.service;

import io.jsonwebtoken.Jwts;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Date;

@Service
public class JwtService {

    @Value("${jwt.private-key}")
    private String privateKeyRaw;

    @Value("jwt.issuer")
    private String issuer;

    @Value("${jwt.expiration}")
    private long expirationInSeconds;

    private PrivateKey privateKey;

    @PostConstruct
    public void init() throws Exception {
        this.privateKey = loadPrivateKey(privateKeyRaw);
    }

    public String generateToken(String clientId) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationInSeconds * 1000);

        return Jwts.builder()
                .subject(clientId)
                .issuedAt(now)
                .issuer(issuer)
                .expiration(expiry)
                .signWith(privateKey)
                .compact();
    }

    private PrivateKey loadPrivateKey(String pem) throws Exception {
        String content = pem
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\r?\\n", "")
                .replaceAll("\\s+", "");

        byte[] decoded = Base64.getDecoder().decode(content);
        return KeyFactory.getInstance("RSA")
                .generatePrivate(new PKCS8EncodedKeySpec(decoded));
    }

    private PublicKey loadPublicKey(String pem) throws Exception {
        String content = pem
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\r?\\n", "")
                .replaceAll("\\s+", "");
        byte[] decoded = Base64.getDecoder().decode(content);
        return KeyFactory.getInstance("RSA")
                .generatePublic(new X509EncodedKeySpec(decoded));
    }
}
