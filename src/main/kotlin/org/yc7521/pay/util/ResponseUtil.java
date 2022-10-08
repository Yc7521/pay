package org.yc7521.pay.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.ResponseEntity;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class ResponseUtil {
  public static void write(ResponseEntity<?> responseEntity,
                           HttpServletResponse servletResponse) throws IOException {
    for (Map.Entry<String, List<String>> header : responseEntity
      .getHeaders()
      .entrySet()) {
      String headerKey = header.getKey();
      for (String value : header.getValue()) {
        servletResponse.addHeader(headerKey, value);
      }
    }

    servletResponse.setStatus(responseEntity.getStatusCodeValue());
    final String body = new ObjectMapper().writeValueAsString(responseEntity.getBody());
    if (body != null) {
      servletResponse.getWriter().write(body);
    }
  }
}
