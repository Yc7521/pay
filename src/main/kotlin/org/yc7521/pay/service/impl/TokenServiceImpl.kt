package org.yc7521.pay.service.impl

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Service
import org.yc7521.pay.model.UserAccount
import org.yc7521.pay.model.UserToken
import org.yc7521.pay.service.TokenService
import org.yc7521.pay.util.JwtTokenUtil

@Service
class TokenServiceImpl(private val jwtTokenUtil: JwtTokenUtil) : TokenService {
  override fun validateToken(token: String, userDetails: UserDetails): Boolean {
    return jwtTokenUtil.validateToken(token, userDetails)
  }

  override fun getToken(account: UserAccount): String {
    return getToken(UserToken(account))
  }

  override fun getToken(
    account: UserAccount,
    authorities: Collection<GrantedAuthority>,
  ): String {
    return getToken(UserToken(account, authorities))
  }

  override fun getToken(user: UserToken): String {
    return jwtTokenUtil.generateToken(user)
  }

  override fun getUsernameFromToken(token: String): String {
    return jwtTokenUtil.getUsernameFromToken(token)
  }
}