package org.yc7521.pay.util.log.util

import javax.servlet.http.HttpServletRequest

object IpUtil {
    fun get(request: HttpServletRequest): String {
        var ip: String? = null;
        //有些网络通过多层代理，那么获取到的ip就会有多个，一般都是通过逗号（,）分割开来，并且第一个ip为客户端的真实IP
        getIpAddresses(request)?.let {
            if (!"unknown".equals(it, true) && it.isNotEmpty()) {
                ip = it.split(",")[0]
            }
        }

        //还是不能获取到，最后再通过request.getRemoteAddr();获取
        if (ip.isNullOrEmpty()) {
            ip = request.remoteAddr;
        }
        return ip ?: "";
    }

    private fun getIpAddresses(request: HttpServletRequest): String? {
        //X-Forwarded-For：Squid 服务代理
        request.getHeader("X-Forwarded-For")?.let {
            if (it.isNotEmpty() && "unknown" != it) {
                return it
            }
        }

        //Proxy-Client-IP：apache 服务代理
        request.getHeader("Proxy-Client-IP")?.let {
            if (it.isNotEmpty() && "unknown" != it) {
                return it
            }
        }
        //WL-Proxy-Client-IP：weblogic 服务代理
        request.getHeader("WL-Proxy-Client-IP")?.let {
            if (it.isNotEmpty() && "unknown" != it) {
                return it
            }
        }
        //HTTP_CLIENT_IP：有些代理服务器
        request.getHeader("HTTP_CLIENT_IP")?.let {
            if (it.isNotEmpty() && "unknown" != it) {
                return it
            }
        }
        //X-Real-IP：nginx服务代理
        request.getHeader("X-Real-IP")?.let {
            if (it.isNotEmpty() && "unknown" != it) {
                return it
            }
        }
        return null
    }
}