package org.yc7521.pay.service.data

import org.springframework.stereotype.Service
import org.yc7521.pay.model.TradingCode
import java.util.concurrent.Callable
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ScheduledExecutorService

@Service
class TokenCache(
  private val executor: ScheduledExecutorService,
) {
  private val cache: ConcurrentHashMap<Long, TradingCode> = ConcurrentHashMap()

  operator fun set(id: Long, code: TradingCode) {
    cache[id] = code
    executor.schedule({
      cache.remove(id)
    }, 3, java.util.concurrent.TimeUnit.MINUTES)
  }
}