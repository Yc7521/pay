package org.yc7521.pay.api

import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import org.yc7521.pay.api.base.BaseApi
import org.yc7521.pay.model.genPaymentCode
import org.yc7521.pay.model.genReceiptCode
import org.yc7521.pay.repository.PayInfoRepository
import org.yc7521.pay.repository.UserInfoRepository
import org.yc7521.pay.service.UserAccountService
import org.yc7521.pay.service.data.TradingCodeCache
import org.yc7521.pay.service.impl.PaymentServiceImpl
import java.math.BigDecimal

@RestController
@RequestMapping("/api/user")
@PreAuthorize("isAuthenticated()")
class UserApi(
  private val userInfoRepository: UserInfoRepository,
  private val payInfoRepository: PayInfoRepository,
  private val paymentService: PaymentServiceImpl,
  private val userAccountService: UserAccountService,
  private val tradingCodeCache: TradingCodeCache,
) : BaseApi() {

  /**
   * GET: get current user
   */
  @GetMapping("/me")
  fun me() = ResponseEntity.ok(currentUserInfo)


  /**
   * DELETE: delete current user
   */
  @DeleteMapping("/me")
  fun deleteMe() = userAccountService.delete(currentUser)

  /**
   * PUT: change password
   */
  @PutMapping("/me/password")
  fun changePassword(
    @RequestParam
    oldPassword: String,
    @RequestParam
    newPassword: String,
  ) = userAccountService.changePassword(currentUser, oldPassword, newPassword)

  /**
   * GET: get user by id
   */
  @GetMapping("/{id}")
  fun get(
    @PathVariable
    id: Long,
  ) = ResponseEntity.ok(
    userAccountService.findById(id).userInfo!!
  )

  /**
   * GET: gen payment code
   */
  @GetMapping("/me/payment-code")
  fun genPaymentCode() = ResponseEntity.ok(
    tradingCodeCache.put(currentUserInfo.genPaymentCode()).toVM()
  )

  /**
   * GET: gen receipt code
   */
  @GetMapping("/me/receipt-code")
  fun genReceiptCode(money: BigDecimal? = null) = ResponseEntity.ok(
    tradingCodeCache.put(currentUserInfo.genReceiptCode(money = money)).toVM()
  )

}



