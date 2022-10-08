package org.yc7521.pay.api

import org.springframework.data.domain.Pageable
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.*
import org.springframework.web.bind.annotation.*
import org.yc7521.pay.api.base.*
import org.yc7521.pay.model.vm.*
import org.yc7521.pay.repository.*
import org.yc7521.pay.service.impl.*

@RestController
@RequestMapping("/api/user/me/pay")
@PreAuthorize("isAuthenticated()")
class PaymentApi(
  private val userInfoRepository: UserInfoRepository,
  private val payInfoRepository: PayInfoRepository,
  private val paymentService: PaymentServiceImpl,
  private val userRepository: UserRepository,
) : BaseApi(userRepository) {
  /**
   * GET: list all pay info
   */
  @GetMapping
  fun list() = ResponseEntity.ok(
    payInfoRepository.findAllByUserInfoId(currentUserInfo!!.id!!, Pageable.ofSize(10))
  )

  /**
   * POST: pay to user
   */
  @PostMapping("")
  fun pay(
    @RequestBody
    payVM: PayVM,
  ) = try {
    ResponseEntity.ok(
      paymentService.createPayment(currentUserInfo!!.id!!, payVM.userId!!, payVM)
    )
  } catch (e: Exception) {
    ResponseEntity.badRequest().body(e.message)
  }

  /**
   * PUT: pay
   */
  @PutMapping("{id}")
  fun pay(
    @PathVariable
    id: Long,
  ) = try {
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
    ResponseEntity.ok(paymentService.cancel(id))
  } catch (e: Exception) {
    ResponseEntity.badRequest().body(e.message ?: "Unknown error")
  }
}
