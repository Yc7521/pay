package org.yc7521.pay.repository

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.transaction.annotation.Transactional
import org.yc7521.pay.model.PayInfo

interface PayInfoRepository : JpaRepository<PayInfo, Long> {
  @Transactional
  @Modifying
  @Query("update PayInfo p set p.state = :#{#payInfo.state}, p.finish = :#{#payInfo.finish} where p.id = :#{#payInfo.id}")
  fun updateState(payInfo: PayInfo): Int

  @Query("select p from PayInfo p where p.payingUser.id = ?1 or p.receivingUser.id = ?1 order by p.create DESC")
  fun findAllByUserInfoId(
    id: Long,
    pageable: Pageable,
  ): Page<PayInfo>

}