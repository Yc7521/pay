package org.yc7521.pay.filter

import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.ObjectError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.yc7521.pay.model.vm.ErrorVM
import org.yc7521.pay.util.PayException
import java.util.*
import java.util.stream.Collectors
import kotlin.NoSuchElementException

@ControllerAdvice
class ExceptionAdvice(
  @Value("\${debug:false}")
  private val debug: Boolean,
  private val resourceBundle: ResourceBundle,
) {
  private final fun getMessage(e: Exception): String =
    e.message?.let {
      if (it.startsWith("\$"))
        resourceBundle.getString(it.substring(1))
      else
        it
    } ?: ""

  private final fun getErrorVM(e: Exception): ErrorVM =
    if (debug)
      ErrorVM(
        getMessage(e),
        e::class.simpleName ?: "",
        e.stackTrace.map { it.toString() }
      )
    else
      ErrorVM(
        getMessage(e),
        e::class.simpleName ?: ""
      )

  @ExceptionHandler(
    MethodArgumentNotValidException::class
  )
  fun handler(e: MethodArgumentNotValidException): ResponseEntity<*> {
    val message = e.bindingResult
      .allErrors
      .stream().map { it.defaultMessage }
      .collect(Collectors.joining())
    return ResponseEntity
      .badRequest()
      .body(
        ErrorVM(message, e::class.simpleName ?: "")
      )
  }

  @ExceptionHandler(value = [NullPointerException::class])
  fun process(e: NullPointerException): ResponseEntity<*> =
    ResponseEntity
      .internalServerError()
      .body(getErrorVM(e))

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
      .body(getErrorVM(e))
}