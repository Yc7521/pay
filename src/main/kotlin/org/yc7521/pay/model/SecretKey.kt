package org.yc7521.pay.model

import com.fasterxml.jackson.annotation.JsonIgnore
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import javax.persistence.*

@Entity
@Table(name = "secret_key")
open class SecretKey(
  @Id
  @Column(name = "secret_key", nullable = false)
  open var key: String? = null,

  @OneToOne(cascade = [CascadeType.ALL], optional = false, orphanRemoval = true)
  @OnDelete(action = OnDeleteAction.CASCADE)
  @JoinColumn(name = "user_info_id", nullable = false)
  open var userInfo: UserInfo? = null,
)