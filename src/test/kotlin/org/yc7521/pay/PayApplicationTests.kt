package org.yc7521.pay

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import java.util.concurrent.Executors

@SpringBootTest
class PayApplicationTests {

  @Test
  fun contextLoads() {
    val executor = Executors.newScheduledThreadPool(8)
    for (i in 0..10) {
      executor.submit {
        println("Hello $i")
      }
    }

  }

}
