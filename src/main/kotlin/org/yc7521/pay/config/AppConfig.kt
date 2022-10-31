package org.yc7521.pay.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.concurrent.Executors.newScheduledThreadPool
import java.util.concurrent.ScheduledExecutorService

@Configuration
class AppConfig {
  // executor
  @Bean
  fun executor(): ScheduledExecutorService = newScheduledThreadPool(8)
}