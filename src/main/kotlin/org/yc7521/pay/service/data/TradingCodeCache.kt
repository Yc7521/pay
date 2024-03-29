package org.yc7521.pay.service.data

import org.springframework.context.annotation.Scope
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.yc7521.pay.model.CodeState
import org.yc7521.pay.model.TradingCode
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

const val CACHE_EXPIRE_TIME = 3L
val CACHE_EXPIRE_TIMEUNIT = TimeUnit.MINUTES
// const val CACHE_EXPIRE_TIME = 2L
// val CACHE_EXPIRE_TIMEUNIT = TimeUnit.SECONDS

@Service
class TradingCodeCache(
  private val executor: ScheduledExecutorService,
) {
  private val cache: ConcurrentHashMap<Long, TradingCode> = ConcurrentHashMap()

  fun list(page: PageRequest): Page<TradingCode> =
    cache.values.toList().let { list ->
      PageImpl(
        list.sortedBy { it.create }.subList(
          page.pageNumber * page.pageSize, (page.pageNumber + 1) * page.pageSize
        ), page, list.size.toLong()
      )
    }

  operator fun set(id: String, code: TradingCode) {
    code.id = id
    val num = id.toLong()
    cache[num] = code
    executor.schedule({
      if (has(id)) {
        cache.remove(num)
      }
    }, CACHE_EXPIRE_TIME, CACHE_EXPIRE_TIMEUNIT)
  }

  operator fun get(id: String): TradingCode =
    cache[id.toLong()]
      ?: throw NoSuchElementException("Error.TradingCode.not_found")

  fun has(id: String): Boolean = cache.containsKey(id.toLong())

  fun getByUserId(userId: Long): List<TradingCode> =
    cache.values.filter { it.userInfoId == userId }

  fun put(code: TradingCode, unique: Boolean = true) =
    (code.id?.toLong() ?: getId()).let { id ->
      if (unique) {
        // remove old code if exists
        getByUserId(code.userInfoId!!).forEach {
          cache.remove(it.id?.toLong())
        }
      }
      set(id.toString(), code)
      code
    }

  fun checkAndRemove(id: String) {
    if (has(id)) {
      val code = get(id)
      if (code.state == CodeState.Finished || code.state == CodeState.Canceled) {
        cache.remove(id.toLong())
      } else throw IllegalStateException("Error.TradingCode.not_finished")
    } else throw NoSuchElementException("Error.TradingCode.not_found")
  }

  fun getId(): Long {
    while (true) getRandomId().let {
      if (it > 0 && !cache.contains(it)) return it
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