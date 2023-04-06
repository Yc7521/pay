package org.yc7521.pay.repository;

import org.springframework.data.jpa.repository.JpaRepository
import org.yc7521.pay.model.SecretKey

interface SecretKeyRepository : JpaRepository<SecretKey, String> {
}