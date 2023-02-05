package org.yc7521.pay.model

import org.yc7521.pay.model.enums.UserType
import java.math.BigDecimal
import javax.persistence.*

@Entity
@Table(name = "user_info")
open class UserInfo(
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", nullable = false)
  open var id: Long? = null,

  @Column(name = "nickname", nullable = false, length = 31)
  open var nickname: String? = null,

  @Column(name = "money", nullable = false, precision = 19, scale = 4)
  open var money: BigDecimal? = null,

  @Column(name = "credible", nullable = false)
  open var credible: Boolean? = null,

  @Enumerated(EnumType.STRING)
  @Column(name = "user_type", nullable = false)
  open var userType: UserType? = null,
)

