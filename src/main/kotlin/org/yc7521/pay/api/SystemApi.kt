package org.yc7521.pay.api

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.yc7521.pay.model.vm.LoginVM
import org.yc7521.pay.service.UserAccountService

@RestController
@RequestMapping("/api")
class SystemApi(
  private val userAccountService: UserAccountService
) {
  /**
   * POST: login
   */
  @PostMapping("/login")
  fun login(
    @RequestBody
    user: LoginVM
  ) {
  }

  /**
   * GET: logout
   */
  @GetMapping("/logout")
  fun logout() {
  }


  /**
   * POST: register
   */
  @PostMapping("/register")
  fun register(
    @RequestBody
    user: LoginVM
  ) = userAccountService.register(user.username!!, user.password!!).let {
    ResponseEntity.ok(it)
  }

  /**
   * DELETE: delete user
   */
  @DeleteMapping("/user/{id}")
  fun delete(
    @PathVariable
    id: Long
  ) = userAccountService.deleteById(id)

}