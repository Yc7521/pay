package org.yc7521.pay.model.enums

enum class PayState {
  /**
   * 未支付
   */
  Unpaid,

  /**
   * 已支付
   */
  Paid,

  /**
   * 已取消
   */
  Canceled,

  /**
   * 已退款
   */
  Refunded
}