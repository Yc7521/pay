package org.yc7521.pay.model.vm

import org.yc7521.pay.model.enums.TradingState
import java.math.BigDecimal

class TradingCodeVM(
  var id: Long? = null,
  var userInfoId: Long? = null,
  var tradingState: TradingState? = null,
  var money: BigDecimal? = null,
)