package org.yc7521.pay.service.impl

import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.yc7521.pay.model.RoleRequest
import org.yc7521.pay.model.UserInfo
import org.yc7521.pay.model.enums.RoleRequestState
import org.yc7521.pay.model.enums.RoleRequestState.*
import org.yc7521.pay.model.enums.UserType
import org.yc7521.pay.model.vm.RequestReq
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
    .orElseThrow { NoSuchElementException() }!!

  /**
   * 列出所有申请
   * @param state 申请状态
   */
  fun getRoleRequestByState(state: RoleRequestState) =
    roleRequestRepository.findAllByState(state)

  /**
   * 列出所有申请
   * @param id 用户id
   */
  fun getRoleRequestByApplicantId(id: Long) =
    roleRequestRepository.findAllByApplicantId(id)

  /**
   * 申请商家身份
   */
  fun applyForRole(
    currentUser: UserInfo,
    requestReq: RequestReq,
    role: UserType = UserType.Business,
  ) = applyForRole(
    currentUser,
    requestReq.name,
    requestReq.idCard,
    role,
    requestReq.remarks ?: ""
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
    val roleRequest = getRoleRequestByApplicantId(currentUser.id!!)
    return when {
      currentUser.credible == false ->
        throw IllegalStateException("You are not credible")

      currentUser.userType!!.ordinal >= role.ordinal ->
        throw IllegalStateException("You have already been ${currentUser.userType}")

      roleRequest.any { it.state == Unprocessed } ->
        throw IllegalStateException("Your application has been submitted")

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
            throw IllegalStateException("You have been rejected. Please contact the administrator")

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

          else -> throw IllegalStateException("You have already been ${it.to}")
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
      .orElseThrow { NoSuchElementException("申请不存在") }!!
      .also { roleRequest ->
        roleRequest.applicant?.let {
          it.userType = roleRequest.to
          userInfoRepository.save(it)
          roleRequest.finish = LocalDateTime.now()
          roleRequest.state = Permit
          roleRequest.approver = approver
          roleRequestRepository.save(roleRequest)
        } ?: throw NoSuchElementException("申请用户不存在")
      }

  /**
   * 审核商家身份 - 拒绝
   */
  fun reject(id: Long, approver: UserInfo) =
    roleRequestRepository
      .findById(id)
      .orElseThrow { NoSuchElementException("申请不存在") }!!
      .also { roleRequest ->
        roleRequest.finish = LocalDateTime.now()
        roleRequest.state = Reject
        roleRequest.approver = approver
        roleRequestRepository.save(roleRequest)
      }
}