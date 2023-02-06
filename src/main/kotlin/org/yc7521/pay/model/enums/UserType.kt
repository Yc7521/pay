package org.yc7521.pay.model.enums

import org.springframework.security.core.authority.SimpleGrantedAuthority

enum class UserType() {
  Personal,
  Business,
  Admin;

  fun getSimpleGrantedAuthority() = when (this) {
    Personal -> SimpleGrantedAuthority("personal")
    Business -> SimpleGrantedAuthority("business")
    Admin -> SimpleGrantedAuthority("admin")
  }
}