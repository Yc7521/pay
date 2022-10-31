package org.yc7521.pay.api

import org.springframework.data.domain.PageRequest
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.ok
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import org.yc7521.pay.api.base.BaseApi
import org.yc7521.pay.model.TradingCode
import org.yc7521.pay.service.data.TradingCodeCache

@RestController
@RequestMapping("/api/code")
@PreAuthorize("isAuthenticated()")
class TradingCodeApi(
  private val tradingCodeCache: TradingCodeCache,
) : BaseApi() {
  @GetMapping
  @PreAuthorize("hasAuthority('role_admin')")
  fun list(
    @RequestParam("page", defaultValue = "0")
    page: Int = 0,
    @RequestParam("size", defaultValue = "10")
    size: Int = 10,
  ) = ok(tradingCodeCache.list(PageRequest.of(page, size)))

  @GetMapping("/{id}")
  fun get(
    @PathVariable
    id: Long,
  ) = ok(tradingCodeCache[id])

  @GetMapping("/has/{id}")
  fun has(
    @PathVariable
    id: Long,
  ) = ok(tradingCodeCache.has(id))

  @GetMapping("/user/{userId}")
  fun getByUserId(
    @PathVariable
    userId: Long,
  ) = tradingCodeCache.getByUserId(userId)?.let { ok(it) } ?: ResponseEntity
    .notFound()
    .build()

  @PostMapping
  fun put(
    @RequestBody
    tradingCode: TradingCode,
  ) = ok(tradingCodeCache.put(tradingCode))

  @DeleteMapping("/{id}")
  fun checkAndRemove(
    @PathVariable
    id: Long,
  ) = ok(tradingCodeCache.checkAndRemove(id))

  /**
   * only for test
   */
  @GetMapping("/getId")
  @PreAuthorize("hasAuthority('role_admin')")
  fun getId() = ok(tradingCodeCache.getId())
}