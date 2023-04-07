package org.yc7521.pay

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.yc7521.pay.model.vm.LoginVM
import org.yc7521.pay.util.autowired

@SpringBootTest
@AutoConfigureMockMvc
class SecretTests {
  private val logger = java.util.logging.Logger.getLogger(this.javaClass.name)!!
  private val mockMvc: MockMvc by autowired()
  private val objectMapperBuilder: Jackson2ObjectMapperBuilder by autowired()
  private val objectMapper: ObjectMapper
    get() = objectMapperBuilder.build()

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
        jsonPath("$.idToken") {
          isString()
        }
      }
    }.andDo {
      handle { res ->
        res.response.contentAsString.let {
          logger.info(it)
          objectMapper.readTree(it).let { root ->
            token = root["idToken"].asText()
          }
        }
      }
    }
    Assertions.assertNotNull(token)
    return token!!
  }

  @Test
  fun test() {
    val token = login(LoginVM("admin", "admin"))

    mockMvc.get("/api/request/secret/test") {
      header("Authorization", "Bearer $token")
    }.andExpect {
      status { isOk() }
    }.andDo {
      handle {
        logger.info(it.response.contentAsString)
      }
    }
  }
}