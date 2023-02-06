package org.yc7521.pay.config;

import io.jsonwebtoken.SignatureAlgorithm;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

public class JwtConstants {
  public static final long      ACCESS_TOKEN_VALIDITY_SECONDS = 30 * 24 * 60 * 60;
  public static final SecretKey SIGNING_KEY                   =
    new SecretKeySpec(
      fillKey("payment-api"),
      SignatureAlgorithm.HS256.getJcaName()
    );
  public static final String    TOKEN_PREFIX                  = "Bearer ";
  public static final String    HEADER_STRING                 = "Authorization";

  private static byte[] fillKey(String key) {
    byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);
    byte[] keyBytes256 = new byte[256];
    System.arraycopy(keyBytes, 0, keyBytes256, 0, Math.min(keyBytes.length, 256));
    return keyBytes256;
  }
}