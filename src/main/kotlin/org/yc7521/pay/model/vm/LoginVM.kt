package org.yc7521.pay.model.vm

data class LoginVM(
  val username: String? = null,
  val password: String? = null,
)

data class LoginRes(
  val error: Boolean? = null,
  val msg: String? = null,
  val id_token: String? = null,
)