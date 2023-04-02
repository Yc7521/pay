package org.yc7521.pay.config

import com.google.gson.JsonParseException
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.security.SignatureException
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.authentication.dao.DaoAuthenticationProvider
import org.springframework.security.config.Customizer
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer.AuthorizationManagerRequestMatcherRegistry
import org.springframework.security.config.annotation.web.configurers.ExceptionHandlingConfigurer
import org.springframework.security.config.core.GrantedAuthorityDefaults
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.core.Authentication
import org.springframework.security.core.AuthenticationException
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.yc7521.pay.filter.JwtAuthenticationFilter
import org.yc7521.pay.filter.JwtCheckAuthenticationFilter
import org.yc7521.pay.model.UserToken
import org.yc7521.pay.service.TokenService
import org.yc7521.pay.util.ResponseUtil.write
import java.util.Map
import java.util.logging.Logger
import javax.annotation.Resource
import javax.servlet.Filter
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Configuration
@EnableWebSecurity // 开启注解设置权限
@EnableGlobalMethodSecurity(prePostEnabled = true)
class WebSecurityConfig {
  private val logger = Logger.getLogger(
    WebSecurityConfig::class.java.name
  )

  //实现UserDetailService接口用来做登录认证
  @Resource
  private lateinit var userDetailsService: UserDetailsService

  @Resource
  private lateinit var tokenService: TokenService

  // 配置密码加密器
  @Bean
  fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

  @Bean(name = ["authenticationManager"])
  @Throws(Exception::class)
  fun authenticationManager(auth: AuthenticationConfiguration) =
    auth.authenticationManager!!

  // 配置安全策略
  @Bean
  @Throws(Exception::class)
  fun filterChain(
    http: HttpSecurity,
    authenticationManager: AuthenticationManager,
  ): SecurityFilterChain {
    return http
      .authorizeHttpRequests { auth ->
        auth
          .antMatchers(HttpMethod.POST, "/api/login", "/api/register")
          .permitAll()
          .antMatchers("/swagger-ui/**", "/v3/api-docs/**")
          .permitAll()
          .anyRequest()
          .authenticated()
      }
      .httpBasic(Customizer.withDefaults())
      .csrf()
      .disable()
      .sessionManagement()
      .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
      .and()
      .authenticationManager(authenticationManager)
      .addFilterBefore(
        JwtCheckAuthenticationFilter(userDetailsService, tokenService),
        UsernamePasswordAuthenticationFilter::class.java
      )
      .addFilterBefore(
        JwtAuthenticationFilter(authenticationManager),
        UsernamePasswordAuthenticationFilter::class.java
      )
      .logout() //自定义登出
      .logoutUrl("/api/logout")
      .logoutSuccessUrl("/api/hello")
      .logoutSuccessHandler { _, _, _ ->
        logger.info("logout success")
      }
      .and()
      .exceptionHandling { hs ->
        hs.accessDeniedHandler { req, response, _ ->
          val obj = req.getAttribute("exception")
          val msg = if (obj is Exception) obj.message else "forbidden"
          if (obj is ExpiredJwtException ||
            obj is SignatureException ||
            obj is JsonParseException
          ) {
            write(
              ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("msg", msg)),
              response
            )
          } else {
            write(
              ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(Map.of("msg", msg)),
              response
            )
          }
        }
        hs.authenticationEntryPoint { req, response, _ ->
          val obj = req.getAttribute("exception")
          val msg = if (obj is Exception) obj.message else "unauthorized"
          write(
            ResponseEntity
              .status(HttpStatus.UNAUTHORIZED)
              .body(Map.of("msg", msg)),
            response
          )
        }
      }
      .build()
  }

  @Bean
  fun grantedAuthorityDefaults() = // Remove the ROLE_ prefix
    GrantedAuthorityDefaults("")

  /**
   * 创建认证提供者Bean
   * DaoAuthenticationProvider是SpringSecurity提供的AuthenticationProvider默认实现类
   * 授权方式提供者，判断授权有效性，用户有效性，在判断用户是否有效性，
   * 它依赖于UserDetailsService实例，可以自定义UserDetailsService的实现。
   *
   * @return 认证提供者
   */
  @Bean
  fun authenticationProvider(passwordEncoder: PasswordEncoder) =
    // 创建DaoAuthenticationProvider实例
    object : DaoAuthenticationProvider() {
      @Throws(AuthenticationException::class)
      override fun authenticate(authentication: Authentication): Authentication {
        // 获取前端表单中输入后返回的用户名、密码
        val userName = authentication.principal as String
        val password = authentication.credentials as String
        val userInfo = userDetailsService.loadUserByUsername(userName) as UserToken
        val isValid = passwordEncoder.matches(password, userInfo.password)
        // 验证密码
        if (!isValid) {
          throw BadCredentialsException("密码错误！")
        }
        val token = tokenService!!.getToken(userInfo)
        userInfo.token = token
        return UsernamePasswordAuthenticationToken(
          userInfo,
          token,
          userInfo.authorities
        )
      }

      override fun supports(aClass: Class<*>?): Boolean {
        return true
      }
    }.also {
      // 将自定义的认证逻辑添加到DaoAuthenticationProvider
      it.setUserDetailsService(userDetailsService)
      // 设置自定义的密码加密
      it.setPasswordEncoder(passwordEncoder)
    }
}