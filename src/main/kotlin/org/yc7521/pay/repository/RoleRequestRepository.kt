package org.yc7521.pay.repository;

import org.springframework.data.jpa.repository.JpaRepository
import org.yc7521.pay.model.RoleRequest
import org.yc7521.pay.model.enums.RoleRequestState

interface RoleRequestRepository : JpaRepository<RoleRequest, Long> {
  fun findAllByState(state: RoleRequestState): MutableList<RoleRequest>
  fun findAllByApplicantId(id: Long): MutableList<RoleRequest>
}