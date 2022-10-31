package org.yc7521.pay.service.data

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.yc7521.pay.model.CodeState
import org.yc7521.pay.model.TradingCode
import org.yc7521.pay.model.genPaymentCode
import org.yc7521.pay.model.genReceiptCode
import org.yc7521.pay.service.UserAccountService
import org.yc7521.pay.service.impl.PaymentServiceImpl
import java.util.concurrent.Future
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import java.util.logging.Logger
import javax.annotation.Resource

@SpringBootTest
class TradingCodeCacheTest {
  private val logger = Logger.getLogger(this.javaClass.name)

  @Resource
  private lateinit var cache: TradingCodeCache

  @Resource
  private lateinit var executor: ScheduledExecutorService

  @Resource
  private lateinit var userAccountService: UserAccountService

  @Resource
  private lateinit var paymentService: PaymentServiceImpl

  private fun setTimeout(
    ms: Long,
    func: () -> Unit,
  ) = executor.schedule(func, ms, TimeUnit.MILLISECONDS)

  @Test
  fun expireTest() {
    val userInfo = userAccountService.findById(2).userInfo!!
    val code = cache.put(userInfo.genPaymentCode())
    val id = code.id!!
    assertTrue(cache.has(id))
    assertTrue(cache[id] == code)
    val tasks = listOf(
      // tasks
      setTimeout(1000) {
        assertTrue(cache.has(id))
      }, setTimeout(3000) {
        assertTrue(!cache.has(id))
      })
    tasks.forEach { it.get() }
  }

  @Test
  fun getIdTest() {
    val ids = (1..100).map { cache.getId() }
    assertTrue(ids.distinct().size == ids.size)
  }

  @Test
  fun findAllByUserInfoIdTest() {
    val userInfo = userAccountService.findById(2).userInfo!!
    cache.put(
      userInfo.genReceiptCode(1000.toBigDecimal())
    )
    assertTrue(cache.findAllByUserInfoId(1).size == 1)
    assertTrue(cache.findAllNotNotifiedByUserInfoId(1).size == 1)
    assertTrue(cache.findAllByUserInfoId(2).isEmpty())
    setTimeout(3000) {
      assertTrue(cache.findAllByUserInfoId(1).isEmpty())
    }.get()
  }

  @Test
  fun paymentTest() {
    val u1 = userAccountService.findById(2).userInfo!!
    val u2 = userAccountService.findById(3).userInfo!!
    val code = cache.put(
      u1.genPaymentCode()
    )
    try {
      val payInfo = paymentService.createPayment(u2.id!!, code, 1.toBigDecimal())
      assertTrue(code.state == CodeState.Created)
      assertTrue(payInfo.id == code.payId)
      paymentService.pay(code)
      assertTrue(code.state == CodeState.Finished)
      code.id!!.let {
        assertTrue(cache.has(it))
        cache.checkAndRemove(it)
        assertTrue(!cache.has(it))
      }
    } catch (e: IllegalStateException) {
      logger.throwing(this.javaClass.name, "paymentTest", e)
    }
  }

  @Test
  fun realPaymentTest() {
    var code: TradingCode? = null
    val tasks = mutableListOf<Future<*>>()
    val paymentCodeGen = {
      userAccountService.findById(2).userInfo!!.let { u1 ->
        tasks.add(executor.submit {
          // 获取付款码
          code = cache.put(
            u1.genPaymentCode()
          )
          code!!.let { code ->
            assertTrue(cache.has(code.id!!))
            // 轮询付款码状态
            while (cache.has(code.id!!)) {
              if (cache[code.id!!].state != CodeState.NotNotified) {
                // 通知 已扫码
                logger.info("已扫码")
                break
              } else {
                Thread.sleep(10)
              }
            }
            if (!cache.has(code.id!!)) {
              logger.info("已过期")
            } else while (cache.has(code.id!!)) {
              // 轮询付款码状态
              when (cache[code.id!!].state) {
                CodeState.NotNotified -> throw IllegalStateException("Cannot be NotNotified")
                CodeState.Created -> Thread.sleep(10)
                CodeState.Finished -> {
                  logger.info("已完成")
                  break
                }

                CodeState.Canceled -> {
                  logger.info("已取消")
                  break
                }
              }
            }
            code.id!!.let {
              if (cache.has(it)) {
                cache.checkAndRemove(it)
                assertTrue(!cache.has(it))
              }
            }
          }
        })
      }
    }
    val paymentCodeScan = { process: (TradingCode) -> Unit ->
      userAccountService.findById(3).userInfo!!.let { u2 ->
        tasks.add(executor.submit {
          while (code == null) {
            Thread.sleep(10)
          }
          code!!.let { code ->
            val payInfo = paymentService.createPayment(u2.id!!, code, 1.toBigDecimal())
            assertTrue(code.state == CodeState.Created)
            assertTrue(payInfo.id == code.payId)
            Thread.sleep(100)
            process(code)
            assertTrue(code.state == CodeState.Finished || code.state == CodeState.Canceled)
            assertTrue(cache.has(code.id!!))
          }
        })
      }
    }

    paymentCodeGen()
    paymentCodeScan {
      paymentService.pay(it)
      logger.info("支付")
    }
    tasks.forEach { it.get() }
    tasks.clear()
    assertTrue(cache.findAllByUserInfoId(1).isEmpty())
    assertTrue(cache.findAllByUserInfoId(2).isEmpty())

    paymentCodeGen()
    paymentCodeScan {
      paymentService.cancel(it)
      logger.info("取消")
    }

    tasks.forEach { it.get() }
    assertTrue(cache.findAllByUserInfoId(1).isEmpty())
    assertTrue(cache.findAllByUserInfoId(2).isEmpty())
  }
}