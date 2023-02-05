package org.yc7521.pay.service.schedule

import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.yc7521.pay.service.impl.PaymentServiceImpl

@Component
class ScheduledTask(
  val paymentServiceImpl: PaymentServiceImpl,
) {
  val log = org.slf4j.LoggerFactory.getLogger(this::class.java)!!

  // run every day at 00:00
  @Scheduled(cron = "0 0 0 * * *")
  fun deleteUnpaid() {
    val count = paymentServiceImpl.deleteUnpaid()
    log.info("Scheduled Task: deleteUnpaid: $count")
  }

  // run every a minute
  @Scheduled(cron = "0 * * * * *")
  fun scheduled2() {
    println(">>>>>>>>> 1 minute")
  }
}