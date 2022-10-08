package org.yc7521.pay.api.base

import org.springframework.security.core.context.SecurityContextHolder
import org.yc7521.pay.model.UserAccount
import org.yc7521.pay.model.UserInfo
import org.yc7521.pay.model.UserToken
import org.yc7521.pay.repository.UserRepository

open class BaseApi(
  private val userRepository: UserRepository
) {
  val currentUser: UserAccount?
    get() = userRepository
      .findById((SecurityContextHolder.getContext().authentication.principal as UserToken).id)
      .orElse(null)
  val currentUserInfo: UserInfo?
    get() = currentUser?.userInfo
}