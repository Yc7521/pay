package org.yc7521.pay.api.base

import org.springframework.security.core.context.SecurityContextHolder
import org.yc7521.pay.model.UserAccount
import org.yc7521.pay.model.UserInfo
import org.yc7521.pay.model.UserToken
import org.yc7521.pay.repository.UserRepository
import javax.annotation.Resource

open class BaseApi {
  @Resource
  private lateinit var userRepository: UserRepository

  val currentUser: UserAccount
    get() = userRepository
      .findById((SecurityContextHolder.getContext().authentication.principal as UserToken).id)
      .orElseThrow { NoSuchElementException("Current user not found") }
  val currentUserInfo: UserInfo
    get() = currentUser.userInfo
      ?: throw NoSuchElementException("Current user info not found")
}