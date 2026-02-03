package kr.co.lokit.api.config.web

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "cookie")
data class CookieProperties(
    val secure: Boolean = false,
    val domains: String,
)
