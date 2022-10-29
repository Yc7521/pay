package org.yc7521.pay.service.impl

import org.springframework.stereotype.Service
import org.yc7521.pay.model.CodeState
import org.yc7521.pay.model.PayInfo
import org.yc7521.pay.model.TradingCode
import org.yc7521.pay.model.TradingType
import org.yc7521.pay.model.enums.PayState
import org.yc7521.pay.model.vm.PayVM
import org.yc7521.pay.repository.PayInfoRepository
import org.yc7521.pay.repository.UserInfoRepository
import java.math.BigDecimal
import java.time.LocalDateTime

@Service
class PaymentServiceImpl(
  private val userInfoRepository: UserInfoRepository,
  private val payInfoRepository: PayInfoRepository,
//  private val tokenCache: TokenCache,
) {
  /**
   * 创建交易记录(未交易)
   * @param money 金额(仅Payment时有效)
   */
  fun createPayment(
    currUserId: Long,
    tradingCode: TradingCode,
    money: BigDecimal? = null,
  ) = when (tradingCode.state) {
    CodeState.NotNotified -> {
      when (tradingCode.tradingType) {
        TradingType.Receipt -> createPayment(
          currUserId, tradingCode.userInfoId!!, PayVM(
            money = tradingCode.money ?: throw IllegalStateException("money is null")
          )
        )

        TradingType.Payment -> createPayment(
          tradingCode.userInfoId!!, currUserId, PayVM(
            money = tradingCode.money ?: money
            ?: throw IllegalStateException("money is null")
          )
        )

        null -> throw IllegalStateException("tradingType is null")
      }
    }

    else -> throw IllegalStateException("交易记录已存在")
  }.also {
    tradingCode.state = CodeState.Created
    tradingCode.payId = it.id
  }

  /**
   * 创建交易记录(未交易)
   */
  fun createPayment(userId: Long, toUserId: Long, pay: PayVM) = userInfoRepository
    .findById(userId)
    .orElseThrow { NoSuchElementException("User not found") }
    .let { from ->
      userInfoRepository
        .findById(toUserId)
        .orElseThrow { NoSuchElementException("User not found") }
        .let { to ->
          if (from.money!! < pay.money!!) {
            throw IllegalStateException("Not enough money")
          }
          if (from.id == to.id) {
            throw IllegalStateException("Can't pay to yourself")
          }
          payInfoRepository.save(
            pay.toPayInfo(
              from = from, to = to
            )
          )
        }
    }

  /**
   * 检查交易记录付款人是否为当前用户
   */
  fun checkPaymentPayer(
    currUserId: Long,
    payId: Long,
  ) = payInfoRepository
    .findById(payId)
    .orElseThrow { NoSuchElementException("PayInfo not found") }
    .let { pay ->
      if (pay.payingUser!!.id != currUserId) {
        throw IllegalStateException("Not the payer")
      }
      pay!!
    }

  /**
   * 完成
   * 不会删除TradingCode
   */
  fun pay(tradingCode: TradingCode): PayInfo = pay(tradingCode.payId!!).also {
    tradingCode.state = CodeState.Finished
  }

  /**
   * 完成
   */
  fun pay(payInfoId: Long): PayInfo = payInfoRepository
    .findById(payInfoId)
    .orElseThrow { NoSuchElementException("PayInfo not found") }
    .let {
      if (it.state != PayState.Unpaid) throw Exception("Can't pay again")
      it.payingUser!!.let { from ->
        it.receivingUser!!.let { to ->
          if (to.id == from.id) throw Exception("Can't pay to yourself")
          it.money!!.let { m ->
            if (from.money!! < m) throw Exception("Not enough money")

            userInfoRepository.saveMoney(from.id!!, from.money!! - m)
            userInfoRepository.saveMoney(to.id!!, to.money!! + m)
            it.finish = LocalDateTime.now()
            it.state = PayState.Paid
            payInfoRepository.updateState(it)
            payInfoRepository
              .findById(it.id!!)
              .orElseThrow { Exception("PayInfo not found") }
          }
        }
      }
    }

  /**
   * 取消
   * 不会删除TradingCode
   */
  fun cancel(tradingCode: TradingCode): PayInfo = cancel(tradingCode.payId!!).also {
    tradingCode.state = CodeState.Canceled
  }

  /**
   * 取消
   */
  fun cancel(payInfoId: Long): PayInfo = payInfoRepository
    .findById(payInfoId)
    .orElseThrow { Exception("PayInfo not found") }
    .let {
      if (it.state != PayState.Unpaid) throw Exception("Can't cancel")
      it.finish = LocalDateTime.now()
      it.state = PayState.Cancelled
      payInfoRepository.updateState(it)
      payInfoRepository.findById(it.id!!).orElseThrow { Exception("PayInfo not found") }
    }

}