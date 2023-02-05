package org.yc7521.pay.service.impl

import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.springframework.boot.test.context.SpringBootTest
import org.yc7521.pay.model.enums.UserType
import org.yc7521.pay.service.UserAccountService
import org.yc7521.pay.util.autowired

@SpringBootTest
class ApplyBusinessServiceImplTest {
  private val applyBusinessService: ApplyBusinessServiceImpl by autowired()
  private val userAccountService: UserAccountService by autowired()

  @Test
  fun apply() {
    userAccountService.register("test-01", "123456")
    val account = userAccountService.login("test-01", "123456")
    val userInfo = account.userInfo!!
    try {
      val apply = applyBusinessService.apply(userInfo.id!!, "张三", "123456789012345678")
      userAccountService.login("test-01", "123456").userInfo?.let {
        assertEquals(it.userType, UserType.Personal)
      } ?: fail("用户不存在")
      applyBusinessService.admit(apply.id!!)
      userAccountService.login("test-01", "123456").userInfo?.let {
        assertEquals(it.userType, UserType.Business)
      } ?: fail("用户不存在")
    } finally {
      userAccountService.delete(userAccountService.login("test-01", "123456"))
    }
  }
}