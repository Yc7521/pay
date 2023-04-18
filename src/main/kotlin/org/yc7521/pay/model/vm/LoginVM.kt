package org.yc7521.pay.model.vm

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonValue
import javax.validation.constraints.NotBlank

data class LoginVM(
  @field:NotBlank(message = "{Error.LoginVM.blank_username}")
  @JsonProperty(value = "username")
  val username: String? = null,
  @field:NotBlank(message = "{Error.LoginVM.blank_password}")
  @JsonProperty(value = "password")
  val password: String? = null,
)

data class SecretLoginVM(
  @field:NotBlank(message = "{Error.SecretLoginVM.blank_key}")
  @JsonProperty
  val key: String? = null,
)

data class LoginRes(
  val error: Boolean? = null,
  val msg: String? = null,
  val idToken: String? = null,
)