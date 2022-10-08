package org.yc7521.pay.service.impl

import org.springframework.stereotype.Service
import org.yc7521.pay.model.PayInfo
import org.yc7521.pay.model.enums.PayState
import org.yc7521.pay.model.vm.PayVM
import org.yc7521.pay.repository.PayInfoRepository
import org.yc7521.pay.repository.UserInfoRepository
import java.time.LocalDateTime

@Service
class PaymentServiceImpl(
  private val userInfoRepository: UserInfoRepository,
  private val payInfoRepository: PayInfoRepository,
) {
  /**
   * 创建交易记录(未交易)
   */
  fun createPayment(userId: Long, toUserId: Long, pay: PayVM) = userInfoRepository
    .findById(userId)
    .orElseThrow { Exception("User not found") }
    .let { from ->
      userInfoRepository
        .findById(toUserId)
        .orElseThrow { Exception("User not found") }
        .let { to ->
          if (from.money!! < pay.money!!) {
            throw Exception("Not enough money")
          }
          if (from.id == to.id) {
            throw Exception("Can't pay to yourself")
          }
          payInfoRepository.save(
            pay.toPayInfo(
              from = from, to = to
            )
          )
        }
    }

  /**
   * 完成
   */
  fun pay(payInfoId: Long): PayInfo = payInfoRepository
    .findById(payInfoId)
    .orElseThrow { Exception("PayInfo not found") }
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