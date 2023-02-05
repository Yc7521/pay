package org.yc7521.pay.model

import java.util.*
import javax.persistence.*

@Entity
@Table(name = "apply_business")
open class ApplyBusiness(
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", nullable = false)
  open var id: Long? = null,

  @ManyToOne
  @JoinColumn(name = "user_id")
  open var user: UserInfo? = null,

  @Column(name = "name", nullable = false, length = 31)
  open var name: String? = null,

  @Column(name = "id_card", nullable = false, length = 31)
  open var idCard: String? = null,

  @Column(name = "remarks")
  open var remarks: String? = null,

  @Column(name = "create_time", nullable = false)
  @Temporal(TemporalType.TIMESTAMP)
  open var createTime: Date? = null,
)