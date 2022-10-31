package org.yc7521.pay.api

import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.*
import org.springframework.web.bind.annotation.*
import org.yc7521.pay.api.base.*
import org.yc7521.pay.model.PayInfo
import org.yc7521.pay.model.vm.*
import org.yc7521.pay.repository.*
import org.yc7521.pay.service.data.TradingCodeCache
import org.yc7521.pay.service.impl.*

@RestController
@RequestMapping("/api/user/me/pay")
@PreAuthorize("isAuthenticated()")
class UserPaymentApi(
  private val userInfoRepository: UserInfoRepository,
  private val payInfoRepository: PayInfoRepository,
  private val paymentService: PaymentServiceImpl,
  private val tradingCodeCache: TradingCodeCache,
) : BaseApi() {

  /**
   * GET: list all pay info
   * 列出所有交易记录
   * @param page 页码
   * @param size 每页数量
   */
  @GetMapping
  fun list(
    page: Int = 0,
    size: Int = 10,
  ) = ResponseEntity.ok(
    payInfoRepository.findAllByUserInfoId(
      currentUserInfo.id!!,
      PageRequest.of(page, size, Sort.Direction.DESC, PayInfo::create.name)
    )
  )

  /**
   * POST: pay to user
   * 创建订单
   */
  @PostMapping("")
  fun pay(
    @RequestBody
    payVM: PayVM,
  ) = try {
    ResponseEntity.ok(
      paymentService.createPayment(currentUserInfo.id!!, payVM.userId!!, payVM)
    )
  } catch (e: Exception) {
    ResponseEntity.badRequest().body(e.message)
  }

  /**
   * PUT: pay
   * 支付
   */
  @PutMapping("{id}")
  fun pay(
    @PathVariable
    id: Long,
  ) = try {
    paymentService.checkPaymentPayer(currentUserInfo.id!!, id)
    ResponseEntity.ok(paymentService.pay(id))
  } catch (e: Exception) {
    ResponseEntity.badRequest().body(e.message ?: "Unknown error")
  }

  /**
   * PUT: cancel pay
   */
  @PutMapping("{id}/cancel")
  fun cancel(
    @PathVariable
    id: Long,
  ) = try {
    paymentService.checkPaymentPayer(currentUserInfo.id!!, id)
    ResponseEntity.ok(paymentService.cancel(id))
  } catch (e: Exception) {
    ResponseEntity.badRequest().body(e.message ?: "Unknown error")
  }
}
