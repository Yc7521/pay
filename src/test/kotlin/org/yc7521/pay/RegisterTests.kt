package org.yc7521.pay

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post
import org.yc7521.pay.model.vm.LoginVM
import javax.annotation.Resource

@SpringBootTest
@AutoConfigureMockMvc
class RegisterTests {
  val logger = java.util.logging.Logger.getLogger(this.javaClass.name)!!

  @Resource
  lateinit var mockMvc: MockMvc

  @ParameterizedTest()
  @MethodSource("registerProvider")
  fun register(loginVM: LoginVM) {
    mockMvc.post("/api/register") {
      this.contentType = MediaType.APPLICATION_JSON
      content = """
        {
          "username": "${loginVM.username}",
          "password": "${loginVM.password}"
        }
      """.trimIndent()
    }.andExpect {
      status {
        isOk()
      }
      content {
        contentTypeCompatibleWith("application/json")
        jsonPath("$.id") {
          isNumber()
        }
        jsonPath("$.username") {
          isString()
        }
        jsonPath("$.userInfo") {
          isMap()
        }
        jsonPath("$.userInfo.nickname") {
          isString()
        }
        jsonPath("$.userInfo.money") {
          isNumber()
        }
        jsonPath("$.userInfo.credible") {
          isBoolean()
        }
        jsonPath("$.userInfo.userType") {
          isString()
        }
      }
    }.andDo {
      handle { res ->
        res.response.contentAsString.let {
          logger.info(it)
        }
      }
    }
  }

  @ParameterizedTest()
  @MethodSource("registerProvider")
  fun login(loginVM: LoginVM) {
    mockMvc.post("/api/login") {
      param("username", loginVM.username!!)
      param("password", loginVM.password!!)
    }.andExpect {
      status {
        isOk()
      }
      content {
        contentTypeCompatibleWith("application/json")
        jsonPath("$.msg") {
          isString()
        }
        jsonPath("$.idToken") {
          isString()
        }
      }
    }.andDo {
      handle { res ->
        res.response.contentAsString.let {
          logger.info(it)
        }
      }
    }
  }

  companion object {
    @JvmStatic
    fun registerProvider(): List<LoginVM> = listOf(
      LoginVM("admin", "admin"),
      LoginVM("1", "1"),
      LoginVM("2", "2"),
    )
  }
}