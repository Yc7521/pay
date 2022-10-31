package org.yc7521.pay.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.transaction.annotation.Transactional
import org.yc7521.pay.model.UserInfo
import java.math.BigDecimal

interface UserInfoRepository : JpaRepository<UserInfo, Long> {
  @Transactional
  @Modifying
  @Query("update UserInfo u set u.money = ?2 where u.id = ?1")
  fun saveMoney(
    id: Long, money: BigDecimal?,
  ): Int

}