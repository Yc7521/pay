package org.yc7521.pay.model

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonIgnore
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import java.time.LocalDateTime
import java.util.*
import javax.persistence.*

@Entity
@Table(name = "secret_key")
open class SecretKey(
  @Id
  @Column(name = "secret_key", nullable = false)
  open var key: String? = null,

  @Column(name = "expired_time")
  @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "GMT+8")
  open var expired: LocalDateTime? = null,

  @OneToOne(cascade = [CascadeType.ALL], optional = false, orphanRemoval = true)
  @OnDelete(action = OnDeleteAction.CASCADE)
  @JoinColumn(name = "user_info_id", nullable = false)
  open var userInfo: UserInfo? = null,
)