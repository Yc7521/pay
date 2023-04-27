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
import java.time.LocalDateTime.now
import java.util.UUID

@RestController
@RequestMapping("/api/request/secret")
@Tag(name = "/Api/ApiKey Request")
class SecretRequestApi(
  private val secretKeyRepository: SecretKeyRepository,
) : BaseApi() {
  @GetMapping
  @Operation(
    operationId = "listApiKey",
    summary = "List all SecretKey.",
  )
  @PreAuthorize("hasRole('admin')")
  fun list(
    @RequestParam("page", defaultValue = "0")
    page: Int = 0,
    @RequestParam("size", defaultValue = "10")
    size: Int = 10,
  ) = ResponseEntity.ok(secretKeyRepository.findAll(PageRequest.of(page, size)))

  @GetMapping("/{key}")
  @Operation(
    operationId = "getApiKey",
    summary = "Get a SecretKey by id.",
  )
  @PreAuthorize("hasRole('admin')")
  fun get(
    @PathVariable
    key: String,
  ) = ResponseEntity.ok(secretKeyRepository.findById(key))

  @PostMapping("/user/{id}")
  @Operation(
    operationId = "createApiKey",
    summary = "Create a SecretKey.",
  )
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
  @Operation(
    operationId = "getApiKeyByUserId",
    summary = "List SecretKeys by user id.",
  )
  @PreAuthorize("hasRole('admin')")
  fun getByUserId(
    @PathVariable
    id: Long,
  ) = ResponseEntity.ok(secretKeyRepository.findByUserInfoId(id))

  @DeleteMapping("/{key}")
  @Operation(
    operationId = "deleteApiKey",
    summary = "Delete a SecretKey by key.",
  )
  @PreAuthorize("hasRole('admin')")
  fun delete(
    @PathVariable
    key: String,
  ) = ResponseEntity.ok(secretKeyRepository.deleteById(key))

  @GetMapping("/user/me")
  @Operation(
    operationId = "listApiKeyForMe",
    summary = "List SecretKeys for current user.",
  )
  @PreAuthorize("hasRole('business')")
  fun listForMe() =
    ResponseEntity.ok(
      secretKeyRepository.findByUserInfoId(
        currentUserInfo.id
          ?: throw IllegalArgumentException("Error.CurrentUser.not_found")
      )
    )

  @PostMapping("/user/me")
  @Operation(
    operationId = "createApiKeyForMe",
    summary = "Create a SecretKey for current user.",
  )
  @PreAuthorize("hasRole('business')")
  fun createForMe(
    @RequestBody
    secretReqVM: SecretReqVM,
  ) = secretReqVM.toSecretKey().let {
    if (it.expired?.isBefore(now()) != false) {
      throw IllegalArgumentException("Error.ApiKey.expired")
    }
    it.key = secretKey()
    it.username = currentUser.username
    it.userInfo = currentUserInfo
    ResponseEntity.ok(secretKeyRepository.save(it))
  }

  @GetMapping("/test")
  @Operation(
    operationId = "generateSecretKeyTest",
    summary = "Test for admin.",
    deprecated = true,
  )
  @PreAuthorize("hasRole('admin')")
  fun generateSecretKeyTest() = ResponseEntity.ok(secretKey())

  private fun generateSecretKey(): String =
    // generate a random string, with UUID
    StringBuilder(
      UUID.randomUUID().toString().replace("-", "").reversed()
    ).let { // insert the random character into the string
      it.insert(8, ('0'..'9').random())
      it.insert(13, ('0'..'9').random())
      it.insert(18, ('0'..'9').random())
      it.insert(23, ('0'..'9').random()) // return the string
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