package org.yc7521.pay.util

import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.springframework.stereotype.Component
import kotlin.reflect.KProperty

private lateinit var ctx: ApplicationContext

@Component
private class CtxVarConfigurer : ApplicationContextAware {
  override fun setApplicationContext(context: ApplicationContext) {
    ctx = context
  }
}

inline fun <reified T : Any> autowired(name: String? = null) =
  Autowired(T::class.java, name)

class Autowired<T : Any>(private val javaType: Class<T>, private val name: String?) {

  operator fun getValue(thisRef: Any?, property: KProperty<*>): T = if (name == null) {
    try {
      ctx.getBean(javaType)
    } catch (e: Exception) {
      ctx.getBean(property.name, javaType)
    }
  } else {
    ctx.getBean(name, javaType)
  }
}