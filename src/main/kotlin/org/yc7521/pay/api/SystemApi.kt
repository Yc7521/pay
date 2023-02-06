package org.yc7521.pay.api

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirements
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import org.yc7521.pay.model.vm.LoginRes
import org.yc7521.pay.model.vm.LoginVM
import org.yc7521.pay.service.UserAccountService
import javax.annotation.security.PermitAll

@RestController
@RequestMapping("/api")
@Tag(name = "/Api/System")
class SystemApi(
  private val userAccountService: UserAccountService,
) {
  /**
   * POST: login
   */
  @PostMapping("/login")
  @SecurityRequirements
  @PermitAll
  @Operation(
    summary = "Login.",
    responses = [
      ApiResponse(
        content = [
          Content(schema = Schema(implementation = LoginRes::class)),
        ]
      )]
  )
  fun login(
    @RequestBody
    user: LoginVM,
  ) {
  }

  /**
   * GET: logout
   */
  @GetMapping("/logout")
  @SecurityRequirements
  @PermitAll
  @Operation(
    summary = "Logout.",
    responses = [
      ApiResponse(
        content = [
          Content(schema = Schema(implementation = String::class)),
        ]
      )]
  )
  fun logout() {
  }


  /**
   * POST: register
   */
  @PostMapping("/register")
  @SecurityRequirements
  @PermitAll
  @Operation(summary = "Register.")
  fun register(
    @RequestBody
    user: LoginVM,
  ) = ResponseEntity.ok(userAccountService.register(user.username!!, user.password!!))

  /**
   * DELETE: delete user
   */
  @DeleteMapping("/user/{id}")
  @Operation(summary = "Delete user.")
  @PreAuthorize("hasRole('admin')")
  fun delete(
    @PathVariable
    id: Long,
  ) = userAccountService.deleteById(id)

}