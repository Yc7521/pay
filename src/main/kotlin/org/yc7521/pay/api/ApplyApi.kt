package org.yc7521.pay.api

import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.yc7521.pay.api.base.BaseApi
import org.yc7521.pay.model.UserInfo
import org.yc7521.pay.service.data.TradingCodeCache
import org.yc7521.pay.service.impl.ApplyBusinessServiceImpl

@RestController
@RequestMapping("/api/applications")
@PreAuthorize("isAuthenticated()")
class ApplyApi(
  private val applyBusinessServiceImpl: ApplyBusinessServiceImpl,
) : BaseApi() {
  @PostMapping("")
  fun apply(
    userInfoId: Long, name: String, idNum: String,
  ) = applyBusinessServiceImpl.apply(userInfoId, name, idNum)
}
