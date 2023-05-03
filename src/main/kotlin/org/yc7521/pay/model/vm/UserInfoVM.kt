package org.yc7521.pay.model.vm

import org.yc7521.pay.model.UserInfo
import org.yc7521.pay.model.enums.UserType

data class UserInfoVM(
  var id: Long? = null,
  var nickname: String? = null,
  var userType: UserType? = null,
)

fun UserInfo.toVm() = UserInfoVM(
  id = id,
  nickname = nickname,
  userType = userType,
)
