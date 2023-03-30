package org.yc7521.pay.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.support.ResourceBundleMessageSource
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean
import org.springframework.web.context.annotation.RequestScope
import org.springframework.web.servlet.LocaleResolver
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor
import org.springframework.web.servlet.i18n.SessionLocaleResolver
import java.net.http.HttpRequest
import java.util.*
import java.util.concurrent.Executors.newScheduledThreadPool
import java.util.concurrent.ScheduledExecutorService
import javax.servlet.http.HttpServletRequest

@Configuration
class AppConfig {
  // executor
  @Bean
  fun executor(): ScheduledExecutorService = newScheduledThreadPool(8)

  /**
   * 国际化
   */
  @Bean
  @RequestScope
  fun messages(httpRequest: HttpServletRequest): ResourceBundle =
    ResourceBundle.getBundle("i18n/messages", httpRequest.locale)

  @Bean
  fun messageSource(): ResourceBundleMessageSource {
    val messageSource = ResourceBundleMessageSource()
    messageSource.setBasename("i18n/messages")
    messageSource.setDefaultEncoding("UTF-8")
    return messageSource
  }

  @Bean
  fun localeResolver(): LocaleResolver = SessionLocaleResolver().also {
    it.setDefaultLocale(Locale.SIMPLIFIED_CHINESE)
  }

  @Bean
  fun localeChangeInterceptor(): LocaleChangeInterceptor = LocaleChangeInterceptor().also {
    it.paramName = "lang"
  }

  @Bean
  fun validator(messageSource: ResourceBundleMessageSource): LocalValidatorFactoryBean =
    LocalValidatorFactoryBean().also {
      it.setValidationMessageSource(messageSource)
    }
}