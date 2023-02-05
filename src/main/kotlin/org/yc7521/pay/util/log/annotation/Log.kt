package org.yc7521.pay.util.log.annotation

import org.yc7521.pay.util.log.enums.BusinessType
import org.yc7521.pay.util.log.enums.OperatorType

/**
 * 自定义操作日志记录注解
 *
 * @author yc
 */
@Target(
    AnnotationTarget.VALUE_PARAMETER,
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY_GETTER,
    AnnotationTarget.PROPERTY_SETTER
)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class Log(
    /**
     * 模块
     */
    val title: String = "",
    /**
     * 功能
     */
    val businessType: BusinessType = BusinessType.OTHER,
    /**
     * 操作人类别
     */
    val operatorType: OperatorType = OperatorType.MANAGE,
    /**
     * 是否保存请求的参数
     */
    val isSaveRequestData: Boolean = true,
    /**
     * 是否保存响应的参数
     */
    val isSaveResponseData: Boolean = true
)