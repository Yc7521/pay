package org.yc7521.pay.service

import org.yc7521.pay.model.UserAccount
import java.util.*

interface UserAccountService {
  /**
   * find by id
   */
  fun findById(id: Long): UserAccount

  /**
   * login
   */
  fun login(username: String, password: String): UserAccount

  /**
   * register
   */
  fun register(username: String, password: String): UserAccount

  /**
   * update
   */
  fun update(userAccount: UserAccount): UserAccount

  /**
   * delete
   */
  fun delete(userAccount: UserAccount)

  /**
   * delete
   */
  fun deleteById(id: Long)

  /**
   * change password
   */
  fun changePassword(it: UserAccount, oldPassword: String, newPassword: String)
}