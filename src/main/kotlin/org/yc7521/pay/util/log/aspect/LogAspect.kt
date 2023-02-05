package org.yc7521.pay.util.log.aspect

import org.aspectj.lang.JoinPoint
import org.aspectj.lang.annotation.AfterReturning
import org.aspectj.lang.annotation.AfterThrowing
import org.aspectj.lang.annotation.Aspect
import org.slf4j.LoggerFactory
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Component
import org.springframework.validation.BindingResult
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import org.springframework.web.multipart.MultipartFile
import org.yc7521.pay.util.log.annotation.Log
import org.yc7521.pay.util.log.enums.BusinessStatus
import org.yc7521.pay.util.log.model.OpLog
import org.yc7521.pay.util.log.util.IpUtil.get
import org.yc7521.pay.util.log.util.toJson
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import kotlin.math.min

/**
 * 操作日志记录处理
 *
 * @author yc
 */
@Aspect
@Component
class LogAspect {
  /**
   * 处理完请求后执行
   *
   * @param joinPoint 切点
   */
  @AfterReturning(pointcut = "@annotation(controllerLog)", returning = "jsonResult")
  fun doAfterReturning(joinPoint: JoinPoint, controllerLog: Log, jsonResult: Any?) {
    handleLog(joinPoint, controllerLog, null, jsonResult)
  }

  /**
   * 拦截异常操作
   *
   * @param joinPoint 切点
   * @param e 异常
   */
  @AfterThrowing(value = "@annotation(controllerLog)", throwing = "e")
  fun doAfterThrowing(joinPoint: JoinPoint, controllerLog: Log, e: Exception?) {
    handleLog(joinPoint, controllerLog, e, null)
  }

  protected fun handleLog(
    joinPoint: JoinPoint,
    controllerLog: Log,
    e: Exception?,
    jsonResult: Any?,
  ) {
    try {
      val requestAttributes =
        RequestContextHolder.getRequestAttributes() as ServletRequestAttributes?
      val request = requestAttributes!!.request
      // *========日志=========*//
      val opLog = OpLog().apply {
        status =
          (if (e != null) BusinessStatus.FAIL else BusinessStatus.SUCCESS)            // 请求的地址
        ip = get(request)
        url = request.requestURI
        // username = SecurityUtils.getUsername()
        // 设置方法名称
        val className = joinPoint.target.javaClass.name
        val methodName = joinPoint.signature.name
        method = "$className.$methodName()"
        // 设置请求方式
        requestMethod = request.method
      }

      // 处理设置注解上的参数
      getControllerMethodDescription(joinPoint, controllerLog, opLog, jsonResult)
      // Log
      opLog.apply {
        val p = opParam?.substring(0, min(100, opParam?.length ?: 0))
        val r = this.jsonResult?.substring(0, min(100, this.jsonResult?.length ?: 0))
        val str = "$title[$operatorType]($status): $url[$method]"
        log.info(
          str
              + (p?.let { "\n param: $p" } ?: "")
              + (r?.let { "\n result: $r" } ?: "")
        )
      }
    } catch (exp: Exception) {
      // 记录本地异常日志
      log.error("==前置通知异常==")
      log.error("异常信息:{}", exp.message)
      exp.printStackTrace()
    }
  }

  /**
   * 获取注解中对方法的描述信息 用于Controller层注解
   *
   * @param log 日志
   * @param opLog 操作日志
   * @throws Exception
   */
  @Throws(Exception::class)
  fun getControllerMethodDescription(
    joinPoint: JoinPoint,
    log: Log,
    opLog: OpLog,
    jsonResult: Any?,
  ) {
    // 设置action动作
    opLog.businessType = log.businessType
    // 设置标题
    opLog.title = log.title
    // 设置操作人类别
    opLog.operatorType = log.operatorType
    // 是否需要保存request，参数和值
    if (log.isSaveRequestData) {
      // 获取参数的信息，传入到数据库中。
      setRequestValue(joinPoint, opLog)
    }
    // 是否需要保存response，参数和值
    if (log.isSaveResponseData && jsonResult != null) {
      opLog.jsonResult = jsonResult.toJson().let { it.substring(0, min(2000, it.length)) }
    }
  }

  /**
   * 获取请求的参数，放到log中
   *
   * @param opLog 操作日志
   * @throws Exception 异常
   */
  @Throws(Exception::class)
  private fun setRequestValue(joinPoint: JoinPoint, opLog: OpLog) {
    opLog.requestMethod?.let { requestMethod ->
      if (HttpMethod.PUT.name == requestMethod || HttpMethod.POST.name == requestMethod) {
        val params = argsArrayToString(joinPoint.args)
        opLog.opParam = params.substring(0, min(2000, params.length))
      }
    }
  }

  /**
   * 参数拼装
   */
  private fun argsArrayToString(paramsArray: Array<Any?>?): String {
    var params = ""
    if (!paramsArray.isNullOrEmpty()) {
      for (o in paramsArray) {
        if (o != null && !isFilterObject(o)) {
          try {
            val jsonObj: Any = o.toJson()
            params += "$jsonObj "
          } catch (_: Exception) {
          }
        }
      }
    }
    return params.trim { it <= ' ' }
  }

  /**
   * 判断是否需要过滤的对象。
   *
   * @param o 对象信息。
   * @return 如果是需要过滤的对象，则返回true；否则返回false。
   */
  fun isFilterObject(o: Any): Boolean {
    val clazz: Class<*> = o.javaClass
    when {
      clazz.isArray -> {
        return clazz.componentType.isAssignableFrom(MultipartFile::class.java)
      }

      MutableCollection::class.java.isAssignableFrom(clazz) -> {
        val collection = o as Collection<*>
        for (value in collection) {
          return value is MultipartFile
        }
      }

      MutableMap::class.java.isAssignableFrom(clazz) -> {
        val map = o as Map<*, *>
        for (value in map.entries) {
          val (_, value1) = value
          return value1 is MultipartFile
        }
      }
    }
    return (o is MultipartFile || o is HttpServletRequest || o is HttpServletResponse || o is BindingResult)
  }

  companion object {
    private val log = LoggerFactory.getLogger(LogAspect::class.java)
  }
}