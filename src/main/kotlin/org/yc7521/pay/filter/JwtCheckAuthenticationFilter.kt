package org.yc7521.pay.filter

import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.security.SignatureException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.web.filter.OncePerRequestFilter
import org.yc7521.pay.config.JwtConstants.HEADER_STRING
import org.yc7521.pay.config.JwtConstants.TOKEN_PREFIX
import org.yc7521.pay.model.UserToken
import org.yc7521.pay.service.TokenService
import java.io.IOException
import javax.servlet.FilterChain
import javax.servlet.ServletException
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class JwtCheckAuthenticationFilter(
  private val userDetailsService: UserDetailsService,
  private val tokenService: TokenService,
) : OncePerRequestFilter() {
  @Throws(IOException::class, ServletException::class)
  override fun doFilterInternal(
    req: HttpServletRequest, res: HttpServletResponse, chain: FilterChain,
  ) {
    req.getHeader(HEADER_STRING)?.let { header ->
      val authToken: String =
        if (header.startsWith(TOKEN_PREFIX)) header.substring(7) else header
      try {
        tokenService.getUsernameFromToken(authToken)?.let { username ->
          if (SecurityContextHolder.getContext().authentication == null) {
            val userDetails = userDetailsService.loadUserByUsername(username) as UserToken
            if (tokenService.validateToken(authToken, userDetails)) {
              val authentication = UsernamePasswordAuthenticationToken(
                userDetails, authToken, userDetails.authorities
              )
              authentication.details = WebAuthenticationDetailsSource().buildDetails(req)
              logger.info("Authenticated user $username, setting security context")
              SecurityContextHolder.getContext().authentication = authentication
            }
          }
        }
      } catch (e: IllegalArgumentException) {
        logger.error("An error occurred during getting username from token", e)
      } catch (e: ExpiredJwtException) {
        logger.warn("The token is expired and not valid anymore", e)
      } catch (e: SignatureException) {
        logger.error("Authentication Failed. Username or Password not valid.")
      }
      chain.doFilter(req, res)
      return
    }
    logger.warn("Couldn't find bearer string, will ignore the header")
    chain.doFilter(req, res)
  }
}