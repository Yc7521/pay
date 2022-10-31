package org.yc7521.pay.model

import com.fasterxml.jackson.annotation.JsonFormat
import org.yc7521.pay.model.vm.TradingCodeVM
import java.math.BigDecimal
import java.time.LocalDateTime

/**
 * 交易码类型
 */
enum class TradingType {
  /**
   * 收款
   */
  Receipt,

  /**
   * 付款
   */
  Payment
}

/**
 * 交易码状态
 */
enum class CodeState {
  /**
   * 未通知
   */
  NotNotified,

  /**
   * 已通知(废弃)
   */
//  Notified,

  /**
   * 已创建
   */
  Created,

  /**
   * 已完成
   */
  Finished,

  /**
   * 已取消
   */
  Canceled,
}

/**
 * 交易码
 */
open class TradingCode(
  open var id: Long? = null,
  open var userInfoId: Long? = null,
  open var tradingType: TradingType? = null,
  open var money: BigDecimal? = null,
  // json LocalDateTime format: yyyy-MM-ddTHH:mm:ss
  @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
  open var create: LocalDateTime? = LocalDateTime.now(),
  open var state: CodeState = CodeState.NotNotified,
  open var payId: Long? = null,
) {
  fun toVM(): TradingCodeVM = TradingCodeVM(
    id = id,
    userInfoId = userInfoId,
    tradingType = tradingType,
    money = money,
  )
}

private fun UserInfo.genTradingCode(
  tradingType: TradingType = TradingType.Payment,
  money: BigDecimal? = null,
) = TradingCode(
  userInfoId = this.id,
  tradingType = tradingType,
  money = money,
  create = LocalDateTime.now()
)

/**
 * 付款码
 */
fun UserInfo.genPaymentCode(
) = genTradingCode(
  TradingType.Payment
)

/**
 * 收款码
 */
fun UserInfo.genReceiptCode(
  money: BigDecimal? = null,
) = genTradingCode(
  TradingType.Receipt, money
)