package kr.co.lokit.api.domain.user.presentation

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.ResponseEntity

@Tag(name = "User", description = "사용자 API")
interface UserApi {
    @Operation(
        summary = "회원 탈퇴",
        description = "현재 로그인한 사용자의 계정을 탈퇴 처리합니다. 계정 상태가 WITHDRAWN으로 변경되고, 세션 및 토큰이 무효화됩니다.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "204", description = "탈퇴 성공"),
            ApiResponse(responseCode = "401", description = "인증 필요"),
        ],
    )
    fun withdraw(
        @Parameter(hidden = true) userId: Long,
        @Parameter(hidden = true) request: HttpServletRequest,
    ): ResponseEntity<Unit>
}
