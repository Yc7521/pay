package org.yc7521.pay.util.log.util

import com.fasterxml.jackson.databind.ObjectMapper

fun <T> T.toJson(): String {
    val mapper = ObjectMapper()
    return mapper.writeValueAsString(this)
}

fun <T> T.toPrettyJson(): String {
    val mapper = ObjectMapper()
    return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(this)
}

inline fun <reified T> String.toObject(): T {
    val mapper = ObjectMapper()
    return mapper.readValue(this, T::class.java)
}
