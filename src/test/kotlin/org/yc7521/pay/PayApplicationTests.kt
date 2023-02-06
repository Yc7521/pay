package org.yc7521.pay

import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import org.hamcrest.text.IsEmptyString.emptyOrNullString
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder
import org.springframework.test.web.servlet.*
import org.yc7521.pay.model.CodeState
import org.yc7521.pay.model.PayInfo
import org.yc7521.pay.model.TradingCode
import org.yc7521.pay.model.UserInfo
import org.yc7521.pay.model.enums.PayState.*
import org.yc7521.pay.model.vm.LoginVM
import org.yc7521.pay.model.vm.TradingCodeVM
import org.yc7521.pay.util.autowired
import java.math.BigDecimal
import java.util.concurrent.ScheduledExecutorService

@SpringBootTest
@AutoConfigureMockMvc
class PayApplicationTests {
  val logger = org.slf4j.LoggerFactory.getLogger(this::class.java)!!

  private val mockMvc: MockMvc by autowired()
  private val executor: ScheduledExecutorService by autowired()
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
        jsonPath("$.id_token") {
          isString()
        }
      }
    }.andDo {
      handle { res ->
        res.response.contentAsString.let {
          logger.info(it)
          objectMapper.readTree(it).let { root ->
            token = root["id_token"].asText()
          }
        }
      }
    }
    assertNotNull(token)
    return token!!
  }

  private fun CoroutineScope.loginAsync(
    username: String,
    password: String,
    func: suspend (token: String) -> Unit,
  ) = async {
    val token = login(LoginVM(username, password))
    hasCode(0, token, false)
    func(token)
  }

  @Test
  fun changeNickname() {
    val u1 = login(LoginVM("1", "1"))
    mockMvc.put("/api/user/me") {
      param("nickname", "new nickname")
      header("Authorization", "Bearer $u1")
    }.andExpect {
      status {
        isOk()
      }
      content {
        contentTypeCompatibleWith("application/json")
        jsonPath("$.nickname") {
          isString()
          value("new nickname")
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

  @Test
  fun u1CodeTest() {
    runBlocking(Dispatchers.IO) {
      loginAsync("1", "1") { u1 ->
        val paymentCode = payment_code("payment", token = u1) {
          this.jsonPath("$.money") {
            isEmpty()
          }
        }
        hasCode(paymentCode.id!!, u1)
        val receiptCode = payment_code("receipt", token = u1, money = 1.toBigDecimal()) {
          this.jsonPath("$.money") {
            isNumber()
          }
        }
        hasCode(paymentCode.id!!, u1, false)
        hasCode(receiptCode.id!!, u1)
      }.await()
    }
  }

  @Test
  fun realPayment(): Unit = runBlocking(Dispatchers.IO) {
    val channel = Channel<TradingCodeVM>()
    val u1Code: () -> Deferred<Unit> = {
      loginAsync("1", "1") { u1 ->
        val receiptCode = payment_code("receipt", token = u1, money = 1.toBigDecimal())
        logger.info("u1 已创建收款码")
        channel.send(receiptCode)
        getCode(receiptCode.id!!, u1, true)
        while (true) {
          val code = getCode(receiptCode.id!!, u1)
          if (code.state != CodeState.NotNotified) {
            // 通知 已扫码
            logger.info("u1 [已扫码]")
            break
          }
          delay(100)
        }
        hasCode(receiptCode.id!!, u1)
        while (true) {
          // 轮询付款码状态
          val code = getCode(receiptCode.id!!, u1)
          assertNotNull(code)
          when (code.state) {
            CodeState.NotNotified -> throw IllegalStateException("Cannot be NotNotified")
            CodeState.Created -> delay(100)
            CodeState.Finished -> {
              logger.info("u1 [已完成]")
              break
            }

            CodeState.Canceled -> {
              logger.info("u1 [已取消]")
              break
            }
          }
        }
        deleteCode(receiptCode.id!!, u1)
        hasCode(receiptCode.id!!, u1, false)
      }
    }
    val u2Pay: (Boolean) -> Deferred<Unit> = { isCancel: Boolean ->
      loginAsync("2", "2") { u2 ->
        val receiptCode = channel.receive()
        hasCode(receiptCode.id!!, u2)
        val code = getCode(receiptCode.id!!, u2, true)
        createPayment(receiptCode.id!!, u2)
        val userInfo = getUserInfo(code.userInfoId!!, u2)
        logger.info("u2 已扫码 {${code.money} to ${userInfo.nickname}}")
        if (isCancel) {
          cancel(receiptCode.id!!, u2)
        } else {
          pay(receiptCode.id!!, u2)
        }
        logger.info("u2 已支付")
      }

    }

    arrayOf(u1Code(), u2Pay(true)).forEach { it.await() }
    arrayOf(u1Code(), u2Pay(false)).forEach { it.await() }
  }

  private fun payment_code(
    payment: String = "payment",
    token: String,
    money: BigDecimal? = null,
    resDsl: MockMvcResultMatchersDsl.() -> Unit = {},
  ): TradingCodeVM {
    var tradingCodeVM: TradingCodeVM? = null
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
          tradingCodeVM = objectMapper.readValue(it, TradingCodeVM::class.java)
        }
      }
    }
    assertNotNull(tradingCodeVM)
    return tradingCodeVM!!
  }

  private fun hasCode(
    id: Long,
    token: String,
    exists: Boolean = true,
  ) {
    mockMvc.get("/api/code/has/$id") {
      header("Authorization", "Bearer $token")
    }.andExpect {
      status {
        if (exists) isOk() else isNotFound()
      }
      content {
        string(emptyOrNullString())
      }
    }
  }

  private fun getCode(
    id: Long,
    token: String,
    log: Boolean = false,
  ): TradingCode {
    var tradingCode: TradingCode? = null
    mockMvc.get("/api/code/$id") {
      header("Authorization", "Bearer $token")
    }.andExpect {
      status {
        isOk()
      }
    }.andDo {
      handle { res ->
        res.response.contentAsString.let {
          if (log) logger.info(it)
          tradingCode = objectMapper.readValue(it, TradingCode::class.java)
        }
      }
    }
    return tradingCode!!
  }

  private fun deleteCode(
    id: Long,
    token: String,
  ) {
    mockMvc.delete("/api/code/$id") {
      header("Authorization", "Bearer $token")
    }
  }

  private fun createPayment(
    code: Long,
    token: String,
    money: BigDecimal? = null,
  ): PayInfo {
    var payInfo: PayInfo? = null
    mockMvc.post("/api/user/me/pay/code/$code") {
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
        jsonPath("$.payingUser.id") {
          isNumber()
        }
        jsonPath("$.receivingUser.id") {
          isNumber()
        }
        jsonPath("$.money") {
          isNumber()
        }
        jsonPath("$.state") {
          value(Unpaid.name)
        }
        jsonPath("$.create") {
          isString()
        }
        jsonPath("$.finish") {
          isEmpty()
        }
      }
    }.andDo {
      handle { res ->
        res.response.contentAsString.let {
          logger.info(it)
          payInfo = objectMapper.readValue(it, PayInfo::class.java)
        }
      }
    }
    assertNotNull(payInfo)
    return payInfo!!
  }

  private fun pay(id: Long, token: String) {
    mockMvc.put("/api/user/me/pay/code/$id") {
      header("Authorization", "Bearer $token")
    }.andExpect {
      status {
        isOk()
      }
      content {
        contentTypeCompatibleWith("application/json")
        jsonPath("$.id") {
          isNumber()
        }
        jsonPath("$.payingUser.id") {
          isNumber()
        }
        jsonPath("$.receivingUser.id") {
          isNumber()
        }
        jsonPath("$.money") {
          isNumber()
        }
        jsonPath("$.state") {
          value(Paid.name)
        }
        jsonPath("$.create") {
          isString()
        }
        jsonPath("$.finish") {
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

  private fun cancel(id: Long, token: String) {
    mockMvc.put("/api/user/me/pay/code/$id/cancel") {
      header("Authorization", "Bearer $token")
    }.andExpect {
      status {
        isOk()
      }
      content {
        contentTypeCompatibleWith("application/json")
        jsonPath("$.id") {
          isNumber()
        }
        jsonPath("$.payingUser.id") {
          isNumber()
        }
        jsonPath("$.receivingUser.id") {
          isNumber()
        }
        jsonPath("$.money") {
          isNumber()
        }
        jsonPath("$.state") {
          value(Canceled.name)
        }
        jsonPath("$.create") {
          isString()
        }
        jsonPath("$.finish") {
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

  private fun getUserInfo(id: Long, token: String): UserInfo {
    var userInfo: UserInfo? = null
    mockMvc.get("/api/user/$id") {
      header("Authorization", "Bearer $token")
    }.andExpect {
      status {
        isOk()
      }
      content {
        contentTypeCompatibleWith("application/json")
        jsonPath("$.id") {
          isNumber()
        }
        jsonPath("$.money") {
          isNumber()
        }
        jsonPath("$.credible") {
          isBoolean()
        }
        jsonPath("$.userType") {
          isString()
        }
      }
    }.andDo {
      handle { res ->
        res.response.contentAsString.let {
          logger.info(it)
          userInfo = objectMapper.readValue(it, UserInfo::class.java)
        }
      }
    }
    return userInfo!!
  }
}
