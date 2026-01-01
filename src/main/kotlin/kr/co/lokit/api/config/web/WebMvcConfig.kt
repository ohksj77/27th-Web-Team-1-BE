package kr.co.lokit.api.config.web

import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.ApiVersionConfigurer
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class WebMvcConfig : WebMvcConfigurer {
    override fun configureApiVersioning(configurer: ApiVersionConfigurer) {
        configurer.apply {
            usePathSegment(0)

            addSupportedVersions("1", "2")
            setDefaultVersion("1")
        }
    }

    override fun configurePathMatch(configurer: PathMatchConfigurer) {
        configurer.addPathPrefix("v{version}") {
            it.`package`.name.startsWith("kr.co.lokit.api")
        }
    }
}
