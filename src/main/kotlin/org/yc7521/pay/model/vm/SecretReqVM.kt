package org.yc7521.pay.model.vm

import org.yc7521.pay.model.SecretKey
import java.time.LocalDateTime

data class SecretReqVM(
  val expired: LocalDateTime,
)

fun SecretReqVM.toSecretKey() = SecretKey(
  expired = expired,
)