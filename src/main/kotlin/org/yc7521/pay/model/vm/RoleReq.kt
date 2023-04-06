package org.yc7521.pay.model.vm

import javax.validation.constraints.NotBlank
import javax.validation.constraints.Size

class RoleReq(
  @field:NotBlank(message = "{Error.RoleReq.blank_name}")
  var name: String = "",
  @field:NotBlank(message = "{Error.RoleReq.blank_idCard}")
  @field:Size(min = 18, max = 18, message = "{Error.RoleReq.idCard_len}")
  var idCard: String = "",
  var remarks: String? = null,
)