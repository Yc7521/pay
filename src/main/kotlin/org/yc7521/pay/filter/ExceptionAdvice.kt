package org.yc7521.pay.filter

import org.springframework.beans.factory.annotation.Value
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.yc7521.pay.util.PayException

@ControllerAdvice
class ExceptionAdvice(
  @Value("\${debug:false}")
  private val debug: Boolean,
) {
  @ExceptionHandler(value = [NullPointerException::class])
  fun process(e: NullPointerException): ResponseEntity<*> =
    ResponseEntity
      .internalServerError()
      .body(
        if (debug)
          mapOf(
            "msg" to "${e.message}",
            "stack" to e.stackTrace.map { it.toString() }.toList(),
            "type" to e::class.simpleName,
          )
        else
          mapOf(
            "msg" to "${e.message}",
            "type" to e::class.simpleName,
          )
      )

  @ExceptionHandler(
    value = [
      PayException::class,
      NoSuchElementException::class,
      IllegalStateException::class,
      IllegalArgumentException::class,
    ]
  )
  fun process(e: PayException): ResponseEntity<*> =
    ResponseEntity
      .badRequest()
      .body(
        mapOf(
          "msg" to "${e.message}",
          "type" to e::class.simpleName,
        )
      )
}