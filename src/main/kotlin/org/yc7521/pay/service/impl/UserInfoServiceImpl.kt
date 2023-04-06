package org.yc7521.pay.service.impl

import org.springframework.stereotype.Service
import org.yc7521.pay.model.UserInfo
import org.yc7521.pay.repository.UserInfoRepository

@Service
class UserInfoServiceImpl(
  private val userInfoRepository: UserInfoRepository,
) {
  /**
   * 获取用户信息
   * @param id 用户id
   */
  fun getUserInfo(id: Long) = userInfoRepository
    .findById(id)
    .orElseThrow { NoSuchElementException("Error.User.not_found") }!!

  /**
   * 修改用户昵称
   * @param userInfo 用户信息
   * @param nickname 新昵称
   */
  fun changeNickname(userInfo: UserInfo, nickname: String): UserInfo {
    userInfo.nickname = nickname
    return userInfoRepository.save(userInfo)
  }
}