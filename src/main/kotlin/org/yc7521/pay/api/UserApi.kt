package org.yc7521.pay.api

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.yc7521.pay.api.base.BaseApi
import org.yc7521.pay.model.enums.UserType
import org.yc7521.pay.model.genPaymentCode
import org.yc7521.pay.model.genReceiptCode
import org.yc7521.pay.model.vm.RoleReq
import org.yc7521.pay.model.vm.toVm
import org.yc7521.pay.repository.PayInfoRepository
import org.yc7521.pay.service.UserAccountService
import org.yc7521.pay.service.data.TradingCodeCache
import org.yc7521.pay.service.impl.PaymentServiceImpl
import org.yc7521.pay.service.impl.RoleRequestServiceImpl
import org.yc7521.pay.service.impl.UserInfoServiceImpl
import java.math.BigDecimal

@RestController
@RequestMapping("/api/user")
@Tag(name = "/Api/User/Info")
class UserApi(
  private val roleRequestServiceImpl: RoleRequestServiceImpl,
  private val userInfoServiceImpl: UserInfoServiceImpl,
  private val payInfoRepository: PayInfoRepository,
  private val paymentService: PaymentServiceImpl,
  private val userAccountService: UserAccountService,
  private val tradingCodeCache: TradingCodeCache,
) : BaseApi() {

  /**
   * GET: get current user
   */
  @GetMapping("/me")
  @Operation(
    operationId = "me",
    summary = "Get user info by current user.",
  )
  fun me() = ResponseEntity.ok(currentUserInfo)

  /**
   * PUT: update the nickname of current user
   */
  @PutMapping("/me")
  @Operation(
    operationId = "changeNickname",
    summary = "Update the nickname of current user.",
  )
  fun updateNickname(nickname: String) =
    ResponseEntity.ok(
      userInfoServiceImpl.changeNickname(
        currentUserInfo,
        nickname
      )
    )

  /**
   * POST: apply for role
   */
  @PostMapping("/me/to/{role}")
  @Operation(
    operationId = "applyForRole",
    summary = "Apply for role.",
  )
  fun applyForRole(
    @PathVariable
    role: UserType,
    @RequestBody
    roleReq: RoleReq,
  ) = ResponseEntity.ok(
    roleRequestServiceImpl.applyForRole(
      currentUserInfo,
      roleReq,
      role
    )
  )

  /**
   * DELETE: delete current user
   */
  @DeleteMapping("/me")
  @Operation(
    operationId = "deleteMe",
    summary = "Delete current user.",
  )
  fun deleteMe() = userAccountService.delete(currentUser)

  /**
   * PUT: change password
   */
  @PutMapping("/me/password")
  @Operation(
    operationId = "changePassword",
    summary = "Change the password of current user.",
  )
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
  @Operation(
    operationId = "getUserInfo",
    summary = "Get user info by user id.",
  )
  fun get(
    @PathVariable
    id: Long,
  ) = ResponseEntity.ok(
    when (currentUserInfo.userType) {
      UserType.Admin -> userInfoServiceImpl.getUserInfo(id)
      else -> userInfoServiceImpl.getUserInfo(id).toVm()
    }
  )

  /**
   * GET: gen payment code
   */
  @GetMapping("/me/payment-code")
  @Operation(
    operationId = "genPaymentCode",
    summary = "Gen payment code.",
  )
  fun genPaymentCode() = ResponseEntity.ok(
    tradingCodeCache.put(currentUserInfo.genPaymentCode()).toVM()
  )

  /**
   * GET: gen receipt code
   */
  @GetMapping("/me/receipt-code")
  @Operation(
    operationId = "genReceiptCode",
    summary = "Gen receipt code.",
  )
  fun genReceiptCode(money: BigDecimal? = null) = ResponseEntity.ok(
    // TODO: need to check if current UserType is Business
    currentUserInfo.let {
      tradingCodeCache
        .put(it.genReceiptCode(money = money), it.userType == UserType.Personal)
        .toVM()
    }
  )
}
