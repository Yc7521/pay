package org.yc7521.pay.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.yc7521.pay.model.UserToken;

import java.io.Serializable;
import java.util.Date;
import java.util.function.Function;

import static org.yc7521.pay.config.JwtConstants.ACCESS_TOKEN_VALIDITY_SECONDS;
import static org.yc7521.pay.config.JwtConstants.SIGNING_KEY;


@Component
public class JwtTokenUtil implements Serializable {

  public String getUsernameFromToken(String token) {
    return getClaimFromToken(token, Claims::getSubject);
  }

  public Date getExpirationDateFromToken(String token) {
    return getClaimFromToken(token, Claims::getExpiration);
  }

  public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
    final Claims claims = getAllClaimsFromToken(token);
    return claimsResolver.apply(claims);
  }

  private Claims getAllClaimsFromToken(String token) {
    return Jwts.parserBuilder()
      .setSigningKey(SIGNING_KEY)
      .build()
      .parseClaimsJws(token)
      .getBody();
  }

  private Boolean isTokenExpired(String token) {
    final Date expiration = getExpirationDateFromToken(token);
    return expiration.before(new Date());
  }

  public String generateToken(UserToken user) {
    return doGenerateToken(user.getUsername());
  }

  private String doGenerateToken(String subject) {
    Claims claims = Jwts.claims().setSubject(subject);
    // claims.put("authorities", Arrays.asList(new SimpleGrantedAuthority("ROLE_ADMIN")));
    return Jwts
      .builder()
      .setClaims(claims)
      .setIssuedAt(new Date())
      .setExpiration(new Date(
        System.currentTimeMillis() + ACCESS_TOKEN_VALIDITY_SECONDS * 1000))
      .signWith(SIGNING_KEY, SignatureAlgorithm.HS256)
      .compact();
  }

  public Boolean validateToken(String token, UserDetails userDetails) {
    final String username = getUsernameFromToken(token);
    return (
      username.equals(userDetails.getUsername())
      && !isTokenExpired(token));
  }

}