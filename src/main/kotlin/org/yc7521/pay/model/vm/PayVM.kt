package org.yc7521.pay.model.vm

import com.fasterxml.jackson.annotation.JsonFormat
import org.yc7521.pay.model.PayInfo
import org.yc7521.pay.model.UserInfo
import org.yc7521.pay.model.enums.PayState
import java.math.BigDecimal
import java.time.LocalDateTime

open class PayVM(
  open var userId: String? = null,
  open var money: BigDecimal? = null,
  @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
  open var create: LocalDateTime? = LocalDateTime.now(),
) {
  fun toPayInfo(
    from: UserInfo, to: UserInfo, goodsInfo: String? = null,
    goodsType: String? = null,
    remarks: String? = null,
  ) = PayInfo(
    payingUser = from,
    receivingUser = to,
    state = PayState.Unpaid,
    money = money,
    create = create,
    finish = null,
    goodsInfo = goodsInfo,
    goodsType = goodsType,
    remarks = remarks,
  )
}