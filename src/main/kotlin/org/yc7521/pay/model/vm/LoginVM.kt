package org.yc7521.pay.model.vm

import javax.validation.constraints.NotBlank

data class LoginVM(
  @field:NotBlank(message = "{Error.LoginVM.blank_username}")
  val username: String? = null,
  @field:NotBlank(message = "{Error.LoginVM.blank_password}")
  val password: String? = null,
)

data class LoginRes(
  val error: Boolean? = null,
  val msg: String? = null,
  val idToken: String? = null,
)