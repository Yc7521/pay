package org.yc7521.pay.service.impl

import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.yc7521.pay.model.UserAccount
import org.yc7521.pay.model.UserInfo
import org.yc7521.pay.model.enums.UserType
import org.yc7521.pay.repository.UserRepository
import org.yc7521.pay.service.UserAccountService
import org.yc7521.pay.util.PayException

@Service
class UserAccountServiceImpl(
  private val userRepository: UserRepository,
  private val passwordEncoder: PasswordEncoder,
) : UserAccountService {
  /**
   * find by id
   */
  override fun findById(id: Long): UserAccount {
    return userRepository
      .findById(id)
      .orElseThrow { NoSuchElementException("Error.User.not_found") }
  }

  /**
   * login
   */
  override fun login(username: String, password: String): UserAccount {
    userRepository.findAllByUsername(username).firstOrNull {
      passwordEncoder.matches(password, it.password)
    }?.let {
      return it
    }
    throw IllegalStateException("Login.failed")
  }

  /**
   * register
   */
  override fun register(username: String, password: String): UserAccount {
    if (username.isBlank() || password.isBlank()) {
      throw IllegalStateException("Login.blank")
    }
    if (userRepository.findAllByUsername(username).isEmpty()) {
      return userRepository.save(
        UserAccount(
          username = username,
          password = passwordEncoder.encode(password),
          userInfo = UserInfo(
            nickname = username,
            money = 0.toBigDecimal(),
            credible = true,
            userType = UserType.Personal,
          ),
        )
      )
    }
    throw IllegalStateException("Login.exists")
  }

  /**
   * update
   */
  override fun update(userAccount: UserAccount) =
    userRepository.save(userAccount)

  /**
   * delete
   */
  override fun delete(userAccount: UserAccount) =
    userRepository.delete(userAccount)

  /**
   * delete
   */
  override fun deleteById(id: Long) {
    userRepository.deleteById(id)
  }

  override fun changePassword(
    it: UserAccount,
    oldPassword: String,
    newPassword: String,
  ) {
    if (passwordEncoder.matches(oldPassword, it.password)) {
      it.password = passwordEncoder.encode(newPassword)
      userRepository.save(it)
    } else {
      throw IllegalStateException("Error.ChangePassword.old_password")
    }
  }
}