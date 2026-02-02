package kr.co.lokit.api.domain.user.presentation

import kr.co.lokit.api.common.dto.IdResponse
import kr.co.lokit.api.domain.user.application.AuthService
import kr.co.lokit.api.domain.user.application.KakaoLoginService
import kr.co.lokit.api.domain.user.application.TempLoginService
import kr.co.lokit.api.domain.user.dto.JwtTokenResponse
import kr.co.lokit.api.domain.user.dto.KakaoLoginRequest
import kr.co.lokit.api.domain.user.dto.LoginRequest
import kr.co.lokit.api.domain.user.dto.RefreshTokenRequest
import kr.co.lokit.api.domain.user.infrastructure.oauth.KakaoOAuthProperties
import kr.co.lokit.api.domain.user.mapping.toJwtTokenResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseCookie
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.net.URI

@RestController
@RequestMapping("auth")
class AuthController(
    private val authService: AuthService,
    private val tempLoginService: TempLoginService,
    private val kakaoLoginService: KakaoLoginService,
    private val kakaoOAuthProperties: KakaoOAuthProperties,
    @Value("\${cookie.secure:false}") private val cookieSecure: Boolean,
) : AuthApi {
    @PostMapping("login")
    @ResponseStatus(HttpStatus.OK)
    override fun login(
        @RequestBody request: LoginRequest,
    ): IdResponse =
        IdResponse(
            tempLoginService
                .login(request.email)
        )

    @PostMapping("refresh")
    override fun refresh(
        @RequestBody request: RefreshTokenRequest,
    ): JwtTokenResponse =
        authService
            .refresh(request.refreshToken)
            .toJwtTokenResponse()

    @PostMapping("kakao")
    override fun kakaoLogin(
        @RequestBody request: KakaoLoginRequest,
    ): JwtTokenResponse =
        kakaoLoginService.login(request.code)

    @GetMapping("kakao/callback")
    fun kakaoCallback(
        @RequestParam code: String,
    ): ResponseEntity<Unit> {
        val tokens = kakaoLoginService.login(code)

        val accessTokenCookie = ResponseCookie.from("accessToken", tokens.accessToken)
            .httpOnly(true)
            .secure(cookieSecure)
            .path("/")
            .maxAge(86400)
            .sameSite("Lax")
            .build()

        val refreshTokenCookie = ResponseCookie.from("refreshToken", tokens.refreshToken)
            .httpOnly(true)
            .secure(cookieSecure)
            .path("/")
            .maxAge(604800)
            .sameSite("Lax")
            .build()

        return ResponseEntity.status(HttpStatus.FOUND)
            .header(HttpHeaders.SET_COOKIE, accessTokenCookie.toString())
            .header(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString())
            .location(URI.create(kakaoOAuthProperties.frontRedirectUri))
            .build()
    }
}