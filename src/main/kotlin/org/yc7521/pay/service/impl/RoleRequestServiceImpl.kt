package org.yc7521.pay.service.impl

import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.yc7521.pay.model.RoleRequest
import org.yc7521.pay.model.UserInfo
import org.yc7521.pay.model.enums.RoleRequestState
import org.yc7521.pay.model.enums.RoleRequestState.*
import org.yc7521.pay.model.enums.UserType
import org.yc7521.pay.model.vm.RoleReq
import org.yc7521.pay.repository.RoleRequestRepository
import org.yc7521.pay.repository.UserInfoRepository
import java.time.LocalDateTime

@Service
class RoleRequestServiceImpl(
  private val userInfoRepository: UserInfoRepository,
  private val roleRequestRepository: RoleRequestRepository,
) {
  /**
   * 列出所有申请
   */
  fun list(page: PageRequest) = roleRequestRepository.findAll(page)

  /**
   * 获取申请
   * @param id 申请id
   */
  fun get(id: Long) = roleRequestRepository
    .findById(id)
    .orElseThrow { NoSuchElementException("Error.RoleReq.not_found") }!!

  /**
   * 列出所有申请
   * @param state 申请状态
   */
  fun getRoleRequestByState(state: RoleRequestState, page: PageRequest) =
    roleRequestRepository.findAllByState(state, page)

  /**
   * 列出所有申请
   * @param id 用户id
   */
  fun getRoleRequestByApplicantId(id: Long, page: PageRequest) =
    roleRequestRepository.findAllByApplicantId(id, page)

  /**
   * 申请商家身份
   */
  fun applyForRole(
    currentUser: UserInfo,
    roleReq: RoleReq,
    role: UserType = UserType.Business,
  ) = applyForRole(
    currentUser,
    roleReq.name,
    roleReq.idCard,
    role,
    roleReq.remarks ?: ""
  )

  /**
   * 申请商家身份
   */
  fun applyForRole(
    currentUser: UserInfo,
    name: String,
    idCard: String,
    role: UserType = UserType.Business,
    remarks: String = "",
  ): RoleRequest {
    val roleRequest = roleRequestRepository.findAllByApplicantId(currentUser.id!!)
    return when {
      currentUser.credible == false ->
        throw IllegalStateException("Error.User.not_credible")

      currentUser.userType!!.ordinal >= role.ordinal ->
        throw IllegalStateException("Error.RoleReq.duplicate_role")

      roleRequest.any { it.state == Unprocessed } ->
        throw IllegalStateException("Error.RoleReq.unprocessed")

      roleRequest.isEmpty() ->
        roleRequestRepository.save(
          RoleRequest(
            null,
            LocalDateTime.now(),
            null,
            currentUser,
            name,
            idCard,
            remarks,
            role,
          )
        )

      else -> roleRequest.first().let {
        when (it.state) {
          Reject ->
            throw IllegalStateException("Error.RoleReq.rejected")

          CanReapply -> {
            it.create = LocalDateTime.now()
            it.finish = null
            it.name = name
            it.idCard = idCard
            it.remarks = remarks
            it.to = role
            it.state = Unprocessed
            roleRequestRepository.save(it)
          }

          else -> throw IllegalStateException("Error.RoleReq.unknown")
        }
      }
    }
  }

  /**
   * 审核商家身份 - 通过
   */
  fun admit(id: Long, approver: UserInfo) =
    roleRequestRepository
      .findById(id)
      .orElseThrow { NoSuchElementException("Error.RoleReq.not_found") }!!
      .also { roleRequest ->
        roleRequest.applicant?.let {
          it.userType = roleRequest.to
          userInfoRepository.save(it)
          roleRequest.finish = LocalDateTime.now()
          roleRequest.state = Permit
          roleRequest.approver = approver
          roleRequestRepository.save(roleRequest)
        } ?: throw NoSuchElementException("Error.User.not_found")
      }

  /**
   * 审核商家身份 - 拒绝
   */
  fun reject(id: Long, approver: UserInfo) =
    roleRequestRepository
      .findById(id)
      .orElseThrow { NoSuchElementException("Error.RoleReq.not_found") }!!
      .also { roleRequest ->
        roleRequest.finish = LocalDateTime.now()
        roleRequest.state = Reject
        roleRequest.approver = approver
        roleRequestRepository.save(roleRequest)
      }
}