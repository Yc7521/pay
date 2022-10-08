package org.yc7521.pay.model;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class UserToken extends User {
  private Long   id;
  private String token;

  /**
   * 创建一个对应账户的UserToken.
   *
   * @param account 账户
   */
  public UserToken(UserAccount account) {
    this(account, new ArrayList<>());
  }

  /**
   * 创建一个对应账户和角色的UserToken.
   *
   * @param account 账户
   * @param role    角色
   */
  public UserToken(UserAccount account, String role) {
    this(account, List.of(new SimpleGrantedAuthority(role)));
  }

  /**
   * 创建一个对应账户和角色的UserToken.
   *
   * @param account     账户
   * @param authorities 角色
   */
  public UserToken(UserAccount account,
                   Collection<? extends GrantedAuthority> authorities) {
    super(account.getUsername(), account.getPassword(), authorities);
    this.id = account.getId();
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getToken() {
    return token;
  }

  public void setToken(String token) {
    this.token = token;
  }
}
