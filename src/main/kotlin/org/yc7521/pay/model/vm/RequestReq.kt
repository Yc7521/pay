package org.yc7521.pay.model.vm

import javax.validation.constraints.NotBlank

class RequestReq(
  @field:NotBlank(message = "{RequestReq.name.blank}")
  var name: String = "",
  @field:NotBlank(message = "{RequestReq.idCard.blank}")
  var idCard: String = "",
  var remarks: String? = null,
)