package org.yc7521.pay

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling
import java.util.*

@SpringBootApplication
@EnableScheduling
class PayApplication

fun main(args: Array<String>) {
  val context = runApplication<PayApplication>(*args)
  val rb = ResourceBundle.getBundle("i18n/messages", Locale.CHINESE)
  println(rb.getString("App.start"))
}
