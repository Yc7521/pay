package org.yc7521.pay.util

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.http.ResponseEntity
import java.io.OutputStreamWriter
import javax.servlet.http.HttpServletResponse

object ResponseUtil {
  @JvmStatic
  fun write(
    responseEntity: ResponseEntity<*>,
    servletResponse: HttpServletResponse,
  ) {
    for ((headerKey, value1) in responseEntity.headers) {
      for (value in value1) {
        servletResponse.addHeader(headerKey, value)
      }
    }
    servletResponse.status = responseEntity.statusCodeValue
    ObjectMapper().writeValueAsString(responseEntity.body)?.let { body ->
      OutputStreamWriter(
        servletResponse.outputStream, Charsets.UTF_8
      ).use { it.write(body) }
    }
    servletResponse.characterEncoding = "UTF-8"
  }
}
