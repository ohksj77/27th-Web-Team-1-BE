package kr.co.lokit.api.domain.user.presentation

import kr.co.lokit.api.domain.user.application.AuthService
import kr.co.lokit.api.domain.user.dto.AuthResponse
import kr.co.lokit.api.domain.user.dto.RegisterRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/auth")
class AuthController(
    private val authService: AuthService,
) {
    @PostMapping("/register")
    fun register(
        @RequestBody request: RegisterRequest,
    ): ResponseEntity<AuthResponse> {
        val result = authService.register(request.email, request.name)

        return ResponseEntity.status(HttpStatus.CREATED).body(
            AuthResponse(
                token = result.token,
                email = result.user.email,
                name = result.user.name,
            ),
        )
    }
}
