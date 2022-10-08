package org.yc7521.pay.service.impl;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.yc7521.pay.model.UserAccount;
import org.yc7521.pay.model.UserToken;
import org.yc7521.pay.service.TokenService;
import org.yc7521.pay.util.JwtTokenUtil;

import java.util.Collection;

@Service
public class TokenServiceImpl implements TokenService {
  private final JwtTokenUtil jwtTokenUtil;

  public TokenServiceImpl(JwtTokenUtil jwtTokenUtil) {
    this.jwtTokenUtil = jwtTokenUtil;
  }

  @Override
  public boolean validateToken(String token, UserDetails userDetails) {
    return jwtTokenUtil.validateToken(token, userDetails);
  }

  @Override
  public String getUsernameFromToken(String token) {
    return jwtTokenUtil.getUsernameFromToken(token);
  }

  @Override
  public String getToken(UserAccount account) {
    return getToken(new UserToken(
      account,
      account.getUserInfo().getUserType().getRole()
    ));
  }

  @Override
  public String getToken(UserAccount account,
                         Collection<? extends GrantedAuthority> authorities) {
    return getToken(new UserToken(account, authorities));
  }

  @Override
  public String getToken(UserToken user) {
    return jwtTokenUtil.generateToken(user);
  }
}
