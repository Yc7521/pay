package org.yc7521.pay

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
class PayApplication

fun main(args: Array<String>) {
  runApplication<PayApplication>(*args)
  println("  ====== PayApplication started  ======  ")
}
