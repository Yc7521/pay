package org.yc7521.pay.model

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.User

class UserToken(
  account: UserAccount,
  authorities: Collection<GrantedAuthority>,
) : User(account.username, account.password, authorities) {
  var id: Long

  var token: String? = null

  /**
   * 创建一个对应账户和角色的UserToken.
   *
   * @param account 账户
   * @param role    角色
   */
  constructor(account: UserAccount, role: GrantedAuthority) : this(
    account,
    listOf(role)
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

  init {
    id = account.id ?: throw IllegalArgumentException("Error.UserToken.null_id")
  }
}