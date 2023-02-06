package org.yc7521.pay.model

import com.fasterxml.jackson.annotation.JsonIgnore
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import javax.persistence.*

@Entity
@Table(
  name = "user",
  indexes = [Index(name = "idx_useraccount_username", columnList = "username")]
)
open class UserAccount(
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", nullable = false)
  open var id: Long? = null,

  @Column(name = "username", nullable = false, unique = true, length = 31)
  open var username: String? = null,

  @Column(name = "password", nullable = false, length = 63)
  @JsonIgnore
  open var password: String? = null,

  @OneToOne(cascade = [CascadeType.ALL], optional = false, orphanRemoval = true)
  @OnDelete(action = OnDeleteAction.CASCADE)
  @JoinColumn(name = "user_info_id", nullable = false)
  open var userInfo: UserInfo? = null,
)