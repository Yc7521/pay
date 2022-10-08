package org.yc7521.pay.model

import com.fasterxml.jackson.annotation.JsonFormat
import org.yc7521.pay.model.enums.TradingState
import org.yc7521.pay.model.vm.TradingCodeVM
import java.math.BigDecimal
import java.time.LocalDateTime

enum class TradingCodeState {
  Created, Notified, Used, finished,
}

open class TradingCode(
  open var id: Long? = null,
  open var userInfoId: Long? = null,
  open var tradingState: TradingState? = null,
  open var money: BigDecimal? = null,
  // json LocalDateTime format: yyyy-MM-ddTHH:mm:ss
  @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
  open var create: LocalDateTime? = null,
  open var state: TradingCodeState = TradingCodeState.Created,
  open var payId: Long? = null,
) {
  fun toVM(): TradingCodeVM = TradingCodeVM(
    id = id,
    userInfoId = userInfoId,
    tradingState = tradingState,
    money = money,
  )
}

fun UserInfo.genTradingCode(
  tradingState: TradingState = TradingState.Payment,
  money: BigDecimal? = null,
) = TradingCode(
  userInfoId = this.id,
  tradingState = tradingState,
  money = money,
  create = LocalDateTime.now()
)