package org.yc7521.pay.api

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.data.domain.PageRequest
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.ok
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import org.yc7521.pay.util.log.annotation.Log
import org.yc7521.pay.util.log.enums.BusinessType
import org.yc7521.pay.api.base.BaseApi
import org.yc7521.pay.model.TradingCode
import org.yc7521.pay.service.data.TradingCodeCache

@RestController
@RequestMapping("/api/code")
@Tag(name = "/Api/Trading Code")
class TradingCodeApi(
  private val tradingCodeCache: TradingCodeCache,
) : BaseApi() {
  @GetMapping
  @Operation(summary = "List all TradingCode.")
  @PreAuthorize("hasRole('admin')")
  fun list(
    @RequestParam("page", defaultValue = "0")
    page: Int = 0,
    @RequestParam("size", defaultValue = "10")
    size: Int = 10,
  ) = ok(tradingCodeCache.list(PageRequest.of(page, size)).map { it.toVM() })

  @GetMapping("/{id}")
  @Operation(summary = "Get a TradingCode by id.")
  fun get(
    @PathVariable
    id: String,
  ) = ok(tradingCodeCache[id])

  @GetMapping("/has/{id}")
  @Operation(summary = "Exist a TradingCode by id.")
  fun has(
    @PathVariable
    id: String,
  ): ResponseEntity<Any> =
    if (tradingCodeCache.has(id)) ok().build() else ResponseEntity.notFound().build()

  @GetMapping("/user/{userId}")
  @Operation(summary = "List TradingCode by userId.")
  fun getByUserId(
    @PathVariable
    userId: Long,
  ) = tradingCodeCache.getByUserId(userId)?.let { ok(it.toVM()) } ?: ResponseEntity
    .notFound()
    .build()

  @PostMapping
  @Operation(summary = "Submit a TradingCode.")
  fun put(
    @RequestBody
    tradingCode: TradingCode,
  ) = ok(tradingCodeCache.put(tradingCode).toVM())

  @DeleteMapping("/{id}")
  @Operation(summary = "Remove a TradingCode by id if it's Finished.")
  fun checkAndRemove(
    @PathVariable
    id: String,
  ) = ok(tradingCodeCache.checkAndRemove(id))

  /**
   * only for test
   */
  @GetMapping("/getId")
  @Operation(summary = "Gen id, only test for admin users.")
  @PreAuthorize("hasRole('admin')")
  fun getId() = ok(tradingCodeCache.getId())
}