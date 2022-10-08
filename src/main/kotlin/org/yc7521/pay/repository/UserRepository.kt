package org.yc7521.pay.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.yc7521.pay.model.UserAccount

interface UserRepository : JpaRepository<UserAccount, Long> {
  fun findByUsername(username: String): UserAccount?
  fun findAllByUsername(username: String): List<UserAccount>
}