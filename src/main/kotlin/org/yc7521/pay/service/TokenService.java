package org.yc7521.pay.service;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.yc7521.pay.model.UserAccount;
import org.yc7521.pay.model.UserToken;

import java.util.Collection;

public interface TokenService {
  boolean validateToken(String token, UserDetails userDetails);
  String getToken(UserAccount account);
  String getToken(UserAccount account,
                  Collection<? extends GrantedAuthority> authorities);
  String getToken(UserToken user);
  String getUsernameFromToken(String token);
}

