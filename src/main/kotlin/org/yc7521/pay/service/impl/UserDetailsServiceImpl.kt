package org.yc7521.pay.service.impl

import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.stereotype.Service
import org.yc7521.pay.model.UserAccount
import org.yc7521.pay.model.UserToken
import org.yc7521.pay.repository.UserRepository
import javax.annotation.Resource
import javax.persistence.EntityNotFoundException

/*
 * UserDetailsService接口实现
 */
@Service
class UserDetailsServiceImpl : UserDetailsService {
  @Resource
  private val accountService: UserRepository? = null

  /*
   *  通过用户名称，查找用户信息，认证相关的逻辑都在loadUserByUsername方法中进行
   */
  override fun loadUserByUsername(username: String): UserDetails {
    val account = accountService!!.findByUsername(username)
    return account?.let {
      // 如果找到，封装该用户的用户名、密码、角色
      createUser(it)
    }
      ?: // 如果没找到用户信息，抛出用户没找到异常
      throw EntityNotFoundException("name：$username not found")
  }

  /*
   *  封装认证需要的UserDetails该用户的用户名、密码、角色
   */
  fun createUser(user: UserAccount): UserDetails {
    return UserToken(user, user.userInfo?.userType?.role ?: "role_user")
  }
}