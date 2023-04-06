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
import org.yc7521.pay.util.PayException
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
            money = tradingCode.money ?: money
            ?: throw IllegalStateException("Error.PayInfo.unknown")
          )
        )

        TradingType.Payment -> createPayment(
          tradingCode.userInfoId!!, currUserId, PayVM(
            money = tradingCode.money ?: money
            ?: throw IllegalStateException("Error.PayInfo.unknown")
          )
        )

        null -> throw IllegalStateException("Error.PayInfo.unknown")
      }
    }

    else -> throw IllegalStateException("Error.PayInfo.duplicate_new")
  }.also {
    tradingCode.state = CodeState.Created
    tradingCode.payId = it.id
  }

  /**
   * 创建交易记录(未交易)
   */
  fun createPayment(userId: Long, toUserId: Long, pay: PayVM) = userInfoRepository
    .findById(userId)
    .orElseThrow { NoSuchElementException("Error.User.not_found") }
    .let { from ->
      userInfoRepository
        .findById(toUserId)
        .orElseThrow { NoSuchElementException("Error.User.not_found") }
        .let { to ->
          if (from.money!! < pay.money!!) {
            throw IllegalStateException("Error.PayInfo.no_money")
          }
          if (from.id == to.id) {
            throw IllegalStateException("Error.PayInfo.pay_self")
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
    .orElseThrow { NoSuchElementException("Error.PayInfo.not_found") }
    .let { pay ->
      if (pay.payingUser!!.id != currUserId) {
        throw IllegalStateException("Error.PayInfo.paying_user_not_eq")
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
    .orElseThrow { NoSuchElementException("Error.PayInfo.not_found") }
    .let {
      if (it.state != PayState.Unpaid) throw IllegalStateException("Error.PayInfo.duplicate_pay")
      it.payingUser!!.let { from ->
        it.receivingUser!!.let { to ->
          if (to.id == from.id) throw IllegalStateException("Error.PayInfo.pay_self")
          it.money!!.let { m ->
            if (from.money!! < m) throw IllegalStateException("Error.PayInfo.no_money")

            userInfoRepository.saveMoney(from.id!!, from.money!! - m)
            userInfoRepository.saveMoney(to.id!!, to.money!! + m)
            it.finish = LocalDateTime.now()
            it.state = PayState.Paid
            payInfoRepository.updateState(it)
            payInfoRepository
              .findById(it.id!!)
              .orElseThrow { NoSuchElementException("Error.PayInfo.not_found") }
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
    .orElseThrow { NoSuchElementException("Error.PayInfo.not_found") }
    .let {
      if (it.state != PayState.Unpaid) throw IllegalStateException("Error.PayInfo.cannot_cancel")
      it.finish = LocalDateTime.now()
      it.state = PayState.Canceled
      payInfoRepository.updateState(it)
      payInfoRepository
        .findById(it.id!!)
        .orElseThrow { NoSuchElementException("Error.PayInfo.not_found") }
    }

  /**
   * 删除一小时前所有未支付的交易记录
   */
  fun deleteUnpaid() = payInfoRepository.deleteUnpaid(PayState.Unpaid, LocalDateTime.now().minusHours(1))
}