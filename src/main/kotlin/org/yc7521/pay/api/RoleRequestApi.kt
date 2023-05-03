package org.yc7521.pay.api

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.data.domain.PageRequest
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import org.yc7521.pay.api.base.BaseApi
import org.yc7521.pay.model.enums.RoleRequestState
import org.yc7521.pay.model.vm.RoleReq
import org.yc7521.pay.service.impl.RoleRequestServiceImpl
import java.sql.SQLException
import javax.validation.Valid

@RestController
@RequestMapping("/api/request/role")
@Tag(name = "/Api/Role Request")
class RoleRequestApi(
  private val roleRequestServiceImpl: RoleRequestServiceImpl,
) : BaseApi() {
  @GetMapping
  @Operation(
    operationId = "listRoleRequest",
    summary = "List all RoleRequest.",
  )
  @PreAuthorize("hasRole('admin')")
  fun list(
    @RequestParam("page", defaultValue = "0")
    page: Int = 0,
    @RequestParam("size", defaultValue = "10")
    size: Int = 10,
  ) = ResponseEntity.ok(roleRequestServiceImpl.list(PageRequest.of(page, size)))

  @GetMapping("/{id}")
  @Operation(
    operationId = "getRoleRequest",
    summary = "Get a RoleRequest by id.",
  )
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
   *  - apply
   */
  @GetMapping("/state/{state}")
  @Operation(
    operationId = "listRoleRequestByState",
    summary = "List RoleRequest by state.",
  )
  @PreAuthorize("hasRole('admin')")
  fun listByState(
    @PathVariable
    state: RoleRequestState,
    @RequestParam("page", defaultValue = "0")
    page: Int = 0,
    @RequestParam("size", defaultValue = "10")
    size: Int = 10,
  ) = ResponseEntity.ok(
    roleRequestServiceImpl.getRoleRequestByState(
      state,
      PageRequest.of(page, size)
    )
  )

  @GetMapping("/applicant/{applicantId}")
  @Operation(
    operationId = "listRoleRequestByApplicantId",
    summary = "List RoleRequest by applicantId.",
  )
  @PreAuthorize("hasRole('admin')")
  fun listByApplicantId(
    @PathVariable
    applicantId: Long,
    @RequestParam("page", defaultValue = "0")
    page: Int = 0,
    @RequestParam("size", defaultValue = "10")
    size: Int = 10,
  ) = ResponseEntity.ok(
    roleRequestServiceImpl.listRoleRequestByApplicantId(
      applicantId,
      PageRequest.of(page, size)
    )
  )

  @GetMapping("/me")
  @Operation(summary = "List RoleRequest by applicantId.")
  fun me() = ResponseEntity.ok(
    roleRequestServiceImpl.listRoleRequestByApplicantId(
      currentUserInfo.id!!
    )
  )

  @PostMapping("/{id}/approve")
  @Operation(
    operationId = "approveRoleRequest",
    summary = "Approve a RoleRequest by id.",
  )
  @PreAuthorize("hasRole('admin')")
  fun approve(
    @PathVariable
    id: Long,
  ) = ResponseEntity.ok(roleRequestServiceImpl.admit(id, currentUserInfo))

  @PostMapping("/{id}/reject")
  @Operation(
    operationId = "rejectRoleRequest",
    summary = "Reject a RoleRequest by id.",
  )
  @PreAuthorize("hasRole('admin')")
  fun reject(
    @PathVariable
    id: Long,
    @RequestParam("force", defaultValue = "false")
    force: Boolean = false,
  ) = ResponseEntity.ok(roleRequestServiceImpl.reject(id, currentUserInfo, force))

  @PostMapping
  @Operation(
    operationId = "applyRoleRequest",
    summary = "Apply for a role.",
  )
  fun apply(
    @Valid
    @RequestBody
    roleRequest: RoleReq,
  ) = try {
    ResponseEntity.ok(
      roleRequestServiceImpl.applyForRole(
        currentUserInfo,
        roleRequest
      )
    )
  } catch (e: SQLException) {
    throw IllegalStateException("Error.RoleReq.duplicate_id_card")
  }
}