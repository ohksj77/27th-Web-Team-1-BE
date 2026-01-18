package kr.co.lokit.api.config.security

import org.springframework.core.annotation.Order
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.stereotype.Component

@Component
class CompositeAuthenticationResolver(
    resolvers: List<AuthenticationResolver>,
) {
    private val orderedResolvers =
        resolvers.sortedBy {
            javaClass.getAnnotation(Order::class.java)?.value ?: Int.MAX_VALUE
        }

    fun authenticate(credentials: String): UsernamePasswordAuthenticationToken? {
        val resolver =
            orderedResolvers.firstOrNull { it.support(credentials) }
                ?: return null
        return resolver.authenticate(credentials)
    }
}
