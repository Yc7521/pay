package org.yc7521.pay.util.log.model

import org.yc7521.pay.util.log.enums.BusinessStatus
import org.yc7521.pay.util.log.enums.BusinessType
import org.yc7521.pay.util.log.enums.OperatorType

data class OpLog(
  var status: BusinessStatus?,
  var ip: String?,
  var url: String?,
  var username: String?,
  var method: String?,
  var opParam: String?,
  var requestMethod: String?,
  var businessType: BusinessType?,
  var title: String?,
  var operatorType: OperatorType?,
  var jsonResult: String?,
) {
    constructor() : this(
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
    )
}