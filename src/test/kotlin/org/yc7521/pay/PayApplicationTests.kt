package org.yc7521.pay

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.servlet.*
import org.yc7521.pay.model.vm.LoginVM
import java.math.BigDecimal
import javax.annotation.Resource

@SpringBootTest
@AutoConfigureMockMvc
class PayApplicationTests {
  val logger = java.util.logging.Logger.getLogger(this.javaClass.name)!!

  @Resource
  lateinit var mockMvc: MockMvc

  private fun login(loginVM: LoginVM): String {
    var token: String? = null
    // login
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
        jsonPath("$.id_token") {
          isString()
        }
      }
    }.andDo {
      handle { res ->
        res.response.contentAsString.let {
          logger.info(it)
          ObjectMapper().readTree(it).let { root ->
            token = root["id_token"].asText()
          }
        }
      }
    }
    assertNotNull(token)
    return token!!
  }

  @Test
  fun realPayment() {
    val u1 = login(LoginVM("1", "1"))
    val u2 = login(LoginVM("2", "2"))
    payment_code("payment", token = u1) {
      this.jsonPath("$.money") {
        isEmpty()
      }
    }
    payment_code("receipt", token = u1, money = 1.toBigDecimal()) {
      this.jsonPath("$.money") {
        isNumber()
      }
    }
  }

  private fun payment_code(
    payment: String = "payment",
    token: String,
    money: BigDecimal? = null,
    resDsl: MockMvcResultMatchersDsl.() -> Unit = {},
  ) {
    mockMvc.get("/api/user/me/$payment-code") {
      header("Authorization", "Bearer $token")
      money?.let { param("money", it.toString()) }
    }.andExpect {
      status {
        isOk()
      }
      content {
        contentTypeCompatibleWith("application/json")
        jsonPath("$.id") {
          isNumber()
        }
        jsonPath("$.userInfoId") {
          isNumber()
        }
        jsonPath("$.tradingType") {
          isString()
        }
        resDsl()
      }
    }.andDo {
      handle { res ->
        res.response.contentAsString.let {
          logger.info(it)
        }
      }
    }
  }
}
