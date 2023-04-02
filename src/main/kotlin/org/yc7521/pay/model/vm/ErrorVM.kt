package org.yc7521.pay.model.vm

data class ErrorVM(
  val message: String,
  val type: String,
  val stack: List<String>? = null,
)
