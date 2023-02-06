package org.yc7521.pay.api

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
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
import java.math.BigDecimal

@RestController
@RequestMapping("/api/user/me/pay")
@Tag(name = "/Api/User/Pay")
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
  @Operation(summary = "List all PayInfo.")
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
  @PostMapping("user")
  @Operation(summary = "Create PayInfo by userId.", description = "Pay to user")
  fun createPayment(
    @RequestBody
    payVM: PayVM,
  ) = ResponseEntity.ok(
    paymentService.createPayment(currentUserInfo.id!!, payVM.userId!!, payVM)
  )

  /**
   * POST: with trading code
   * 创建订单
   */
  @PostMapping("code/{code}")
  fun createPaymentWithCode(
    @PathVariable
    code: Long,
    @RequestParam(required = false)
    money: BigDecimal? = null,
  ) = ResponseEntity.ok(
    paymentService.createPayment(currentUserInfo.id!!, tradingCodeCache[code], money)
  )

  /**
   * PUT: pay with trading code
   * 支付
   */
  @PutMapping("code/{code}")
  fun payWithCode(
    @PathVariable
    code: Long
  ) = ResponseEntity.ok(
    paymentService.pay(tradingCodeCache[code])
  )

  /**
   * PUT: pay with trading code
   * 支付
   */
  @PutMapping("code/{code}/cancel")
  fun cancelWithCode(
    @PathVariable
    code: Long
  ) = ResponseEntity.ok(
    paymentService.cancel(tradingCodeCache[code])
  )

  /**
   * PUT: pay
   * 支付
   */
  @PutMapping("{id}")
  @Operation(summary = "Pay.", description = "Update PayInfo and UserInfo to finish this payment")
  fun pay(
    @PathVariable
    id: Long,
  ): ResponseEntity<PayInfo> =
    paymentService.checkPaymentPayer(currentUserInfo.id!!, id).let {
      return ResponseEntity.ok(paymentService.pay(id))
    }

  /**
   * PUT: cancel pay
   */
  @PutMapping("{id}/cancel")
  @Operation(summary = "Cancel.", description = "Update PayInfo to cancel this payment")
  fun cancel(
    @PathVariable
    id: Long,
  ): ResponseEntity<PayInfo> =
    paymentService.checkPaymentPayer(currentUserInfo.id!!, id).let {
      return ResponseEntity.ok(paymentService.cancel(id))
    }
}
