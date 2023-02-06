package org.yc7521.pay.model

import com.fasterxml.jackson.annotation.JsonFormat
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import org.yc7521.pay.model.enums.RoleRequestState
import org.yc7521.pay.model.enums.UserType
import java.time.LocalDateTime
import javax.persistence.*

@Entity
@Table(
  name = "role_request",
  indexes = [Index(
    name = "idx_role_request",
    columnList = "state, applicant_id, to_type",
    unique = true
  )]
)
open class RoleRequest(
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", nullable = false)
  open var id: Long? = null,

  @Column(name = "create_time")
  @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
  open var create: LocalDateTime? = null,

  @Column(name = "finish_time")
  @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
  open var finish: LocalDateTime? = null,

  @ManyToOne(optional = false, cascade = [CascadeType.REMOVE])
  @OnDelete(action = OnDeleteAction.CASCADE)
  @JoinColumn(name = "applicant_id", nullable = false)
  open var applicant: UserInfo? = null,

  @Column(name = "name", nullable = false, length = 15)
  open var name: String? = null,

  @Column(name = "id_card", nullable = false, unique = true, length = 31)
  open var idCard: String? = null,

  @Column(name = "remarks")
  open var remarks: String? = null,

  @Enumerated(EnumType.STRING)
  @Column(name = "to_type", nullable = false)
  open var to: UserType? = null,

  @ManyToOne(cascade = [CascadeType.REMOVE])
  @OnDelete(action = OnDeleteAction.CASCADE)
  @JoinColumn(name = "approver_id")
  open var approver: UserInfo? = null,

  @Enumerated(EnumType.STRING)
  @Column(name = "state", nullable = false)
  open var state: RoleRequestState? = RoleRequestState.Unprocessed,
)