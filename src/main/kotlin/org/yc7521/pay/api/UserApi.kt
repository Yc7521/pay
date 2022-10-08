package org.yc7521.pay.api

import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import org.yc7521.pay.api.base.BaseApi
import org.yc7521.pay.model.genTradingCode
import org.yc7521.pay.repository.PayInfoRepository
import org.yc7521.pay.repository.UserInfoRepository
import org.yc7521.pay.repository.UserRepository
import org.yc7521.pay.service.impl.PaymentServiceImpl
import java.math.BigDecimal

@RestController
@RequestMapping("/api/user")
@PreAuthorize("isAuthenticated()")
class UserApi(
  private val userInfoRepository: UserInfoRepository,
  private val payInfoRepository: PayInfoRepository,
  private val paymentService: PaymentServiceImpl,
  private val userRepository: UserRepository,
) : BaseApi(userRepository) {

  /**
   * GET: get current user
   */
  @GetMapping("/me")
  fun me() = ResponseEntity.ok(currentUserInfo!!)


  /**
   * DELETE: delete current user
   */
  @DeleteMapping("/me")
  fun deleteMe() = userRepository.delete(currentUser!!)

  /**
   * GET: get user by id
   */
  @GetMapping("/{id}")
  fun get(
    @PathVariable
    id: Long,
  ) = ResponseEntity.ok(
    userRepository.findById(id).orElseThrow {
      RuntimeException("User not found")
    }.userInfo!!
  )

  /**
   * GET: gen payment code
   */
  @GetMapping("/me/payment-code")
  fun genPaymentCode() = ResponseEntity.ok(
    currentUserInfo!!.genTradingCode(
      tradingState = org.yc7521.pay.model.enums.TradingState.Payment
    ).toVM()
  )

  /**
   * GET: gen receipt code
   */
  @GetMapping("/me/receipt-code")
  fun genReceiptCode(money: BigDecimal?) = ResponseEntity.ok(
    currentUserInfo!!.genTradingCode(
      tradingState = org.yc7521.pay.model.enums.TradingState.Receipt, money = money
    ).toVM()
  )

}



