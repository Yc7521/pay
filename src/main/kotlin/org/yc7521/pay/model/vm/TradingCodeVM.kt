package org.yc7521.pay.model.vm

import org.yc7521.pay.model.TradingType
import java.math.BigDecimal

class TradingCodeVM(
  var id: Long? = null,
  var userInfoId: Long? = null,
  var tradingType: TradingType? = null,
  var money: BigDecimal? = null,
)