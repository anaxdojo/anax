package org.anax.framework.reporting.authentication;

import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import static org.apache.commons.codec.binary.Base64.encodeBase64URLSafeString;
import static org.apache.commons.codec.binary.Hex.encodeHexString;

@Slf4j
public class JwtBuilder {

    public static String generateJWTToken(String canonicalUrl, String key, String sharedSecret) {

        JwtClaims claims = new JwtClaims();
        claims.setIss(key);
        claims.setIat(System.currentTimeMillis() / 1000L);
        claims.setExp(claims.getIat() + 18000L);

        String jwtToken = "";

        try {
            claims.setQsh(getQueryStringHash(canonicalUrl));
            jwtToken = sign(claims, sharedSecret);
        } catch (NoSuchAlgorithmException | InvalidKeyException | UnsupportedEncodingException e) {
            log.error("Exception while generating JWT token");
            e.printStackTrace();
        }
        if (jwtToken.isEmpty()) {
            log.error("Will return empty JWT token");
        }
        return jwtToken;
    }

    private static String sign(JwtClaims claims, String sharedSecret)
            throws InvalidKeyException, NoSuchAlgorithmException {
        String signingInput = getSigningInput(claims);
        String signed256 = signHmac256(signingInput, sharedSecret);
        return signingInput + "." + signed256;
    }

    private static String getSigningInput(JwtClaims claims) {
        JwtHeader header = new JwtHeader();
        header.alg = "HS256";
        header.typ = "JWT";
        Gson gson = new Gson();
        String headerJsonString = gson.toJson(header);
        String claimsJsonString = gson.toJson(claims);
        return encodeBase64URLSafeString(headerJsonString.getBytes())
                + "."
                + encodeBase64URLSafeString(claimsJsonString.getBytes());
    }

    private static String signHmac256(String signingInput, String sharedSecret)
            throws NoSuchAlgorithmException, InvalidKeyException {
        SecretKey key = new SecretKeySpec(sharedSecret.getBytes(), "HmacSHA256");
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(key);
        return encodeBase64URLSafeString(mac.doFinal(signingInput.getBytes()));
    }

    private static String getQueryStringHash(String canonicalUrl)
            throws NoSuchAlgorithmException, UnsupportedEncodingException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(canonicalUrl.getBytes("UTF-8"));
        byte[] digest = md.digest();
        return encodeHexString(digest);
    }
}
