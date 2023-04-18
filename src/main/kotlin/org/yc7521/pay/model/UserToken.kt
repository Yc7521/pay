package org.yc7521.pay.model

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.User

class UserToken : User {
  constructor(account: UserAccount, authorities: Collection<GrantedAuthority>) : super(
    account.username, account.password, authorities
  ) {
    id = account.id ?: throw IllegalArgumentException("Error.UserToken.null_id")
  }

  constructor(
    secretKey: SecretKey,
    authorities: Collection<GrantedAuthority>,
  ) : super(
    secretKey.username, secretKey.key, authorities
  ) {
    id = -1
  }

  var id: Long

  var token: String? = null

  /**
   * 创建一个对应账户和角色的UserToken.
   *
   * @param account 账户
   * @param role    角色
   */
  constructor(account: UserAccount, role: GrantedAuthority) : this(
    account, listOf(role)
  )

  /**
   * 创建一个对应账户和角色的UserToken.
   *
   * @param account 账户
   * @param role    角色
   */
  constructor(account: SecretKey, role: GrantedAuthority) : this(
    account, listOf(role)
  )

  /**
   * 创建一个对应账户的UserToken.
   *
   * @param account 账户
   */
  constructor(account: UserAccount) : this(
    account,
    account.userInfo?.userType?.getSimpleGrantedAuthority()
      ?: SimpleGrantedAuthority("anonymous")
  )

  /**
   * 创建一个对应账户的UserToken.
   *
   * @param account 账户
   */
  constructor(account: SecretKey) : this(
    account,
    account.userInfo?.userType?.getSimpleGrantedAuthority()
      ?: SimpleGrantedAuthority("anonymous")
  )

}