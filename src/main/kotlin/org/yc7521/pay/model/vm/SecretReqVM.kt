package org.yc7521.pay.model.vm

import com.fasterxml.jackson.annotation.JsonFormat
import org.yc7521.pay.model.SecretKey
import java.time.LocalDateTime

data class SecretReqVM(
  @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
  val expired: LocalDateTime,
)

fun SecretReqVM.toSecretKey() = SecretKey(
  expired = expired,
)