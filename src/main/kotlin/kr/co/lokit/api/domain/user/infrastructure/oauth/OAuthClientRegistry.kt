package kr.co.lokit.api.domain.user.infrastructure.oauth

import org.springframework.stereotype.Component

@Component
class OAuthClientRegistry(
    clients: List<OAuthClient>,
) {
    private val clientMap: Map<OAuthProvider, OAuthClient> =
        clients.associateBy { it.provider }

    fun getClient(provider: OAuthProvider): OAuthClient =
        clientMap[provider]
            ?: throw IllegalArgumentException("지원하지 않는 OAuth 제공자입니다: $provider")
}