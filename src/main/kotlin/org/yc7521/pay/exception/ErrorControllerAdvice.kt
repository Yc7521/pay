package org.yc7521.pay.exception

import org.springframework.beans.factory.annotation.Value
import org.springframework.http.ResponseEntity.badRequest
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.method.HandlerMethod

@ControllerAdvice
class ErrorControllerAdvice(
  @Value("\${debug:false}")
  private val debug: Boolean,
) {
  //注解：出现异常会来到这个方法处理
  //参数：捕获控制器出现的异常，可传入集合捕获多种类型的异常
  @ResponseBody
  @ExceptionHandler(RuntimeException::class)
  fun handlerError(
    ex: RuntimeException,
    hm: HandlerMethod,
  ) = badRequest().body(
    if (debug) mapOf(
      "message" to (ex.message ?: "Unknown error"),
      "class" to hm.bean.javaClass,
      "method" to hm.method.name,
    ) else mapOf(
      "message" to (ex.message ?: "Unknown error"),
    )
  )
}