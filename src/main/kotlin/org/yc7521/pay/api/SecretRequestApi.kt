package org.yc7521.pay.api

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.data.domain.PageRequest
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import org.yc7521.pay.api.base.BaseApi
import org.yc7521.pay.model.SecretKey
import org.yc7521.pay.model.UserInfo
import org.yc7521.pay.model.vm.SecretReqVM
import org.yc7521.pay.model.vm.toSecretKey
import org.yc7521.pay.repository.SecretKeyRepository
import java.util.UUID

@RestController
@RequestMapping("/api/request/secret")
@Tag(name = "/Api/Role Request")
class SecretRequestApi(
  private val secretKeyRepository: SecretKeyRepository,
) : BaseApi() {
  @GetMapping
  @Operation(summary = "List all SecretKey.")
  @PreAuthorize("hasRole('admin')")
  fun list(
    @RequestParam("page", defaultValue = "0")
    page: Int = 0,
    @RequestParam("size", defaultValue = "10")
    size: Int = 10,
  ) = ResponseEntity.ok(secretKeyRepository.findAll(PageRequest.of(page, size)))

  @GetMapping("/{key}")
  @Operation(summary = "Get a SecretKey by id.")
  @PreAuthorize("hasRole('admin')")
  fun get(
    @PathVariable
    key: String,
  ) = ResponseEntity.ok(secretKeyRepository.findById(key))

  @PostMapping("/user/{id}")
  @Operation(summary = "Create a SecretKey.")
  @PreAuthorize("hasRole('admin')")
  fun create(
    @PathVariable
    id: Long,
    @RequestBody
    secretKey: SecretKey,
  ) = secretKey.let {
    it.userInfo = UserInfo(id)
    ResponseEntity.ok(secretKeyRepository.save(it))
  }

  @GetMapping("/user/{id}")
  @Operation(summary = "Get a SecretKey by user id.")
  @PreAuthorize("hasRole('admin')")
  fun getByUserId(
    @PathVariable
    id: Long,
  ) = ResponseEntity.ok(secretKeyRepository.findByUserInfoId(id))

  @DeleteMapping("/{key}")
  @Operation(summary = "Delete a SecretKey by key.")
  @PreAuthorize("hasRole('admin')")
  fun delete(
    @PathVariable
    key: String,
  ) = ResponseEntity.ok(secretKeyRepository.deleteById(key))

  @PostMapping("/user/me")
  @Operation(summary = "Create a SecretKey for current user.")
  @PreAuthorize("hasRole('business')")
  fun createForMe(
    @RequestBody
    secretReqVM: SecretReqVM,
  ) = secretReqVM.toSecretKey().let {
    it.key = secretKey()
    it.userInfo = currentUserInfo
    ResponseEntity.ok(secretKeyRepository.save(it))
  }

  @GetMapping("/test")
  @Operation(summary = "Test for admin.")
  @PreAuthorize("hasRole('admin')")
  fun generateSecretKeyTest() = ResponseEntity.ok(secretKey())

  private fun generateSecretKey(): String =
    // generate a random string, with UUID
    StringBuilder(
      UUID
        .randomUUID()
        .toString()
        .replace("-", "")
        .reversed()
    ).let {
      // insert the random character into the string
      it.insert(8, ('0'..'9').random())
      it.insert(13, ('0'..'9').random())
      it.insert(18, ('0'..'9').random())
      it.insert(23, ('0'..'9').random())
      // return the string
      it.toString()
    }

  private fun secretKey(): String {
    while (true) {
      val key = generateSecretKey()
      if (!secretKeyRepository.existsById(key)) {
        return key
      }
    }
  }
}