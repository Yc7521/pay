package org.yc7521.pay.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.yc7521.pay.filter.JwtAuthenticationFilter;
import org.yc7521.pay.filter.JwtCheckAuthenticationFilter;
import org.yc7521.pay.model.UserToken;
import org.yc7521.pay.service.TokenService;

import javax.annotation.Resource;
import javax.servlet.Filter;
import java.util.logging.Logger;

import static org.yc7521.pay.util.ResponseUtil.write;

@Configuration
@EnableWebSecurity
// 开启注解设置权限
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
  private final Logger logger = Logger.getLogger(WebSecurityConfig.class.getName());

  //实现UserDetailService接口用来做登录认证
  @Resource
  private UserDetailsService userDetailsService;
  @Resource
  private TokenService       tokenService;

  // 配置密码加密器
  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  // 配置认证管理器
  @Override
  protected void configure(AuthenticationManagerBuilder auth) {
    auth.authenticationProvider(authenticationProvider(passwordEncoder()));
  }

  @Bean
  @Override
  protected AuthenticationManager authenticationManager() throws Exception {
    return super.authenticationManager();
  }

  // 配置安全策略
  @Override
  protected void configure(HttpSecurity http) throws Exception {
    // 设置路径及要求的权限，支持 ant 风格路径写法
    http.authorizeRequests()
      // 设置 OPTIONS 尝试请求直接通过
      .antMatchers(HttpMethod.OPTIONS, "/**")
      .permitAll()
      .antMatchers("/api/sys/**")
      .authenticated()
      // 注意使用 hasAnyAuthority 角色需要以 ROLE_ 开头
      //      .antMatchers("/api/demo/admin").hasAnyAuthority("ROLE_admin")
      .anyRequest()
      .permitAll()
      .and()
      //禁用 CSRF,不然post调试的时候都403
      .csrf()
      .disable()
      .sessionManagement()
      .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
      .and()
      .addFilterBefore(authentication(), UsernamePasswordAuthenticationFilter.class)
      .addFilterBefore(
        new JwtAuthenticationFilter(authenticationManager()),
        UsernamePasswordAuthenticationFilter.class
      )
      .logout()//自定义登出
      .logoutUrl("/api/logout")
      .logoutSuccessHandler((request, response, authentication) -> {
        logger.info("logout success");
        write(ResponseEntity.ok("logout success"), response);
      })
      .and()
    // 开启表单登录
    // .formLogin().permitAll()
    // .and()
    // 开启注销
    // .logout().permitAll()
    ;
  }

  /**
   * 创建认证提供者Bean
   * DaoAuthenticationProvider是SpringSecurity提供的AuthenticationProvider默认实现类
   * 授权方式提供者，判断授权有效性，用户有效性，在判断用户是否有效性，
   * 它依赖于UserDetailsService实例，可以自定义UserDetailsService的实现。
   *
   * @return 认证提供者
   */
  @Bean
  public DaoAuthenticationProvider authenticationProvider(PasswordEncoder passwordEncoder) {
    // 创建DaoAuthenticationProvider实例
    DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider() {
      @Override
      public Authentication authenticate(Authentication authentication)
        throws AuthenticationException {
        // 获取前端表单中输入后返回的用户名、密码
        String userName = (String) authentication.getPrincipal();
        String password = (String) authentication.getCredentials();

        UserToken userInfo = (UserToken) userDetailsService.loadUserByUsername(userName);

        boolean isValid = passwordEncoder.matches(password, userInfo.getPassword());
        // 验证密码
        if (!isValid) {
          throw new BadCredentialsException("密码错误！");
        }
        final String token = tokenService.getToken(userInfo);
        userInfo.setToken(token);

        return new UsernamePasswordAuthenticationToken(
          userInfo,
          token,
          userInfo.getAuthorities()
        );
      }

      @Override
      public boolean supports(Class<?> aClass) {
        return true;
      }

    };
    // 将自定义的认证逻辑添加到DaoAuthenticationProvider
    authProvider.setUserDetailsService(userDetailsService);
    // 设置自定义的密码加密
    authProvider.setPasswordEncoder(passwordEncoder);
    return authProvider;
  }

  @Bean
  protected Filter authentication() {
    return new JwtCheckAuthenticationFilter(userDetailsService, tokenService);
  }

}
