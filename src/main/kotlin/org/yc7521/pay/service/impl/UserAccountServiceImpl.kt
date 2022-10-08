package org.yc7521.pay.service.impl

import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.yc7521.pay.model.UserAccount
import org.yc7521.pay.model.UserInfo
import org.yc7521.pay.model.enums.UserType
import org.yc7521.pay.repository.UserRepository
import org.yc7521.pay.service.UserAccountService


@Service
class UserAccountServiceImpl(
  private val userRepository: UserRepository, private val passwordEncoder: PasswordEncoder
) : UserAccountService {
  /**
   * login
   */
  override fun login(username: String, password: String): UserAccount {
    userRepository.findAllByUsername(username).firstOrNull {
      passwordEncoder.matches(password, it.password)
    }?.let {
      return it
    }
    throw Exception("用户名或密码错误")
  }

  /**
   * register
   */
  override fun register(username: String, password: String): UserAccount {
    if (username.isBlank() || password.isBlank()) {
      throw Exception("用户名或密码不能为空")
    }
    if (userRepository.findAllByUsername(username).isEmpty()) {
      return userRepository.save(
        UserAccount(
          username = username,
          password = passwordEncoder.encode(password),
          userInfo = UserInfo(
            money = 0.toBigDecimal(), credible = true, userType = UserType.Personal
          )
        )
      )
    }
    throw Exception("用户名已存在")
  }

  /**
   * update
   */
  override fun update(userAccount: UserAccount) = userRepository.save(userAccount)

  /**
   * delete
   */
  override fun delete(userAccount: UserAccount) = userRepository.delete(userAccount)

  /**
   * delete
   */
  override fun deleteById(id: Long) {
    userRepository.deleteById(id)
  }
}