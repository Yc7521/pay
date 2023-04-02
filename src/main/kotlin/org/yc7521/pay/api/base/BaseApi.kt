package org.yc7521.pay.api.base

import org.springframework.security.core.context.SecurityContextHolder
import org.yc7521.pay.model.UserAccount
import org.yc7521.pay.model.UserInfo
import org.yc7521.pay.model.UserToken
import org.yc7521.pay.repository.UserRepository
import org.yc7521.pay.util.autowired
import java.util.ResourceBundle
import javax.annotation.Resource

open class BaseApi {
  private val userRepository: UserRepository by autowired()

  val currentUser: UserAccount
    get() = userRepository
      .findById((SecurityContextHolder.getContext().authentication.principal as UserToken).id)
      .orElseThrow {
        NoSuchElementException("\$Error.current.user_not_found")
      }
  val currentUserInfo: UserInfo
    get() = currentUser.userInfo
      ?: throw NoSuchElementException("\$Error.current.info_not_found")
}