package org.yc7521.pay.service.data

import org.springframework.stereotype.Service
import org.yc7521.pay.model.CodeState
import org.yc7521.pay.model.TradingCode
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

// const val CACHE_EXPIRE_TIME = 3L
// val CACHE_EXPIRE_TIMEUNIT = TimeUnit.MINUTES
const val CACHE_EXPIRE_TIME = 2L
val CACHE_EXPIRE_TIMEUNIT = TimeUnit.SECONDS

@Service
class TokenCache(
  private val executor: ScheduledExecutorService,
) {
  private val cache: ConcurrentHashMap<Long, TradingCode> = ConcurrentHashMap()

  operator fun set(id: Long, code: TradingCode) {
    // remove old code if exists
    getByUserId(code.userInfoId!!)?.let {
      cache.remove(it.id)
    }
    code.id = id
    cache[id] = code
    executor.schedule({
      if (has(id)) {
        cache.remove(id)
      }
    }, CACHE_EXPIRE_TIME, CACHE_EXPIRE_TIMEUNIT)
  }

  operator fun get(id: Long): TradingCode = cache[id] ?: throw NoSuchElementException()

  fun has(id: Long): Boolean = cache.containsKey(id)

  fun getByUserId(userId: Long): TradingCode? =
    cache.values.find { it.userInfoId == userId }

  fun put(tradingCode: TradingCode) = (tradingCode.id ?: getId()).let { id ->
    set(id, tradingCode)
    tradingCode
  }

  fun checkAndRemove(id: Long) {
    if (has(id)) {
      val code = get(id)
      if (code.state == CodeState.Finished || code.state == CodeState.Canceled) {
        cache.remove(id)
      } else throw IllegalStateException("Code is not finished or canceled")
    } else throw NoSuchElementException("Code not found")
  }

  fun getId(): Long {
    while (true) getRandomId().let {
      if (!cache.contains(it)) return it
    }
  }

  private fun getRandomId() = UUID.randomUUID().let {
    it.mostSignificantBits xor it.leastSignificantBits
  }

  fun findAllByUserInfoId(userInfoId: Long?): List<TradingCode> {
    return cache.values.filter { it.userInfoId == userInfoId }
  }

  fun findAllNotNotifiedByUserInfoId(userInfoId: Long?): List<TradingCode> {
    return findAllByUserInfoId(userInfoId).filter { it.state == CodeState.NotNotified }
  }

}