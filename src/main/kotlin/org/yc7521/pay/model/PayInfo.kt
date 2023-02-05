package org.yc7521.pay.model

import com.fasterxml.jackson.annotation.JsonFormat
import org.yc7521.pay.model.enums.PayState
import java.math.BigDecimal
import java.time.LocalDateTime
import javax.persistence.*

@Entity
@Table(name = "pay_info")
open class PayInfo(
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", nullable = false)
  open var id: Long? = null,

  @ManyToOne
  @JoinColumn(name = "paying_user_id")
  open var payingUser: UserInfo? = null,

  @ManyToOne
  @JoinColumn(name = "receiving_user_id")
  open var receivingUser: UserInfo? = null,

  @Enumerated(EnumType.STRING)
  @Column(name = "state")
  open var state: PayState? = null,

  @Column(name = "money", nullable = false, precision = 19, scale = 4)
  open var money: BigDecimal? = null,

  @Column(name = "create_time")
  @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "GMT+8")
  open var create: LocalDateTime? = null,

  @Column(name = "finish_time")
  @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "GMT+8")
  open var finish: LocalDateTime? = null,

  @Column(name = "goods_info", length = 127)
  open var goodsInfo: String? = null,

  @Column(name = "goods_type", length = 31)
  open var goodsType: String? = null,

  @Column(name = "remarks")
  open var remarks: String? = null,
)