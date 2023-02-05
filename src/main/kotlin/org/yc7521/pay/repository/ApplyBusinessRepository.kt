package org.yc7521.pay.repository;

import org.springframework.data.jpa.repository.JpaRepository
import org.yc7521.pay.model.ApplyBusiness

interface ApplyBusinessRepository : JpaRepository<ApplyBusiness, Long> {
  fun findAllByUserId(userId: Long): List<ApplyBusiness>
}