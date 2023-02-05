package org.yc7521.pay.service.impl

import org.springframework.stereotype.Service
import org.yc7521.pay.model.ApplyBusiness
import org.yc7521.pay.model.UserInfo
import org.yc7521.pay.model.enums.UserType
import org.yc7521.pay.repository.ApplyBusinessRepository
import org.yc7521.pay.repository.UserInfoRepository
import java.util.*

@Service
class ApplyBusinessServiceImpl(
  val applyBusinessRepository: ApplyBusinessRepository,
  val userInfoRepository: UserInfoRepository,
) {
  /**
   * 列出所有申请
   */
  fun list(): List<ApplyBusiness> = applyBusinessRepository.findAll()

  /**
   * 列出所有申请
   */
  fun list(userId: Long): List<ApplyBusiness> =
    applyBusinessRepository.findAllByUserId(userId)

  /**
   * 申请商家身份
   */
  protected fun apply(userInfo: UserInfo, name: String, idNum: String) =
    applyBusinessRepository.save(
      ApplyBusiness(
        user = userInfo, name = name, idCard = idNum, createTime = Date()
      )
    )

  /**
   * 申请商家身份
   */
  fun apply(userInfoId: Long, name: String, idNum: String) =
    apply(UserInfo(id = userInfoId), name, idNum)

  /**
   * 审核商家身份 - 通过
   */
  fun admit(id: Long) {
    val applyBusiness =
      applyBusinessRepository.findById(id).orElseThrow { RuntimeException("申请不存在") }
    applyBusiness.user?.let {
      it.userType = UserType.Business
      userInfoRepository.save(applyBusiness.user!!)
    } ?: throw RuntimeException("申请用户不存在")
    applyBusinessRepository.delete(applyBusiness)
  }

  /**
   * 审核商家身份 - 拒绝
   */
  fun reject(id: Long) {
    applyBusinessRepository.deleteById(id)
  }
}