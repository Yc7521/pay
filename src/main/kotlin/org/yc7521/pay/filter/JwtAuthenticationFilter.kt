package org.yc7521.pay.filter

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.ok
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.AuthenticationException
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.yc7521.pay.model.UserToken
import org.yc7521.pay.model.vm.LoginRes
import org.yc7521.pay.model.vm.LoginVM
import org.yc7521.pay.util.ResponseUtil
import org.yc7521.pay.util.autowired
import java.io.IOException
import java.util.*
import java.util.logging.Logger
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class JwtAuthenticationFilter(
  authenticationManager: AuthenticationManager?,
) :
  UsernamePasswordAuthenticationFilter(authenticationManager) {
  private val resourceBundle: ResourceBundle by autowired()

  private val logger = Logger.getLogger(
    JwtAuthenticationFilter::class.java.name
  )

  init {
    // 设置登录失败处理类
    setAuthenticationFailureHandler { _: HttpServletRequest?, rep: HttpServletResponse, e: AuthenticationException ->
      logger.info("Login failed")
      if (e is UsernameNotFoundException) {
        ResponseUtil.write(
          ok(
            LoginRes(
              true,
              resourceBundle.getString("Login.name_not_found")
            )
          ), rep
        )
      } else {
        ResponseUtil.write(
          ok(
            LoginRes(
              true,
              resourceBundle.getString("Login.failed")
            )
          ), rep
        )
      }
      // ResponseUtil.write(ok(LoginRes(true, e.message)), rep)
    }
    // 设置登录成功处理类
    setAuthenticationSuccessHandler { _: HttpServletRequest?, rep: HttpServletResponse, auth: Authentication ->
      val userToken = auth.principal as UserToken
      logger.info("${auth.name} login success; Authorities: ${auth.authorities}")
      ResponseUtil.write(
        ok(
          LoginRes(
            false,
            resourceBundle.getString("Login.success").format(auth.name),
            userToken.token
          )
        ),
        rep
      )
    }
    setFilterProcessesUrl("/api/login")
  }

  @Throws(AuthenticationException::class)
  override fun attemptAuthentication(
    req: HttpServletRequest, rep: HttpServletResponse,
  ): Authentication {
    if (req.contentType?.contains(MediaType.APPLICATION_JSON_VALUE) == true) {
      val mapper = ObjectMapper()
      try {
        req.inputStream.use { `in` ->
          // deserialize json to get loginInfo
          val (username, password) = mapper.readValue(`in`, LoginVM::class.java)
          val authRequest = UsernamePasswordAuthenticationToken(
            username, password
          )
          setDetails(req, authRequest)
          return authenticationManager.authenticate(authRequest)
        }
      } catch (e: IOException) {
        e.printStackTrace()
        val authRequest = UsernamePasswordAuthenticationToken("", "")
        setDetails(req, authRequest)
        return authenticationManager.authenticate(authRequest)
      }
    } else {
      return super.attemptAuthentication(req, rep)
    }
  }
}