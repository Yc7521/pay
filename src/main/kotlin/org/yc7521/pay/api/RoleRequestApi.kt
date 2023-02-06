package org.yc7521.pay.api

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.data.domain.PageRequest
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import org.yc7521.pay.service.impl.RoleRequestServiceImpl

@RestController
@RequestMapping("/api/request/role")
@Tag(name = "/Api/Role Request")
class RoleRequestApi(
  private val roleRequestServiceImpl: RoleRequestServiceImpl,
) {
  @GetMapping
  @Operation(summary = "List all RoleRequest.")
  @PreAuthorize("hasRole('admin')")
  fun list(
    @RequestParam("page", defaultValue = "0")
    page: Int = 0,
    @RequestParam("size", defaultValue = "10")
    size: Int = 10,
  ) = ResponseEntity.ok(roleRequestServiceImpl.list(PageRequest.of(page, size)))

  @GetMapping("/{id}")
  @Operation(summary = "Get a RoleRequest by id.")
  fun get(
    @PathVariable
    id: Long,
  ) = ResponseEntity.ok(roleRequestServiceImpl.get(id))

  /*
   * TODO: need to implement the following APIs
   *  - listByState
   *  - listByApplicantId
   *  - approve
   *  - reject
   */
}