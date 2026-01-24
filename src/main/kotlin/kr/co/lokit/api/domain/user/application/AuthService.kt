package kr.co.lokit.api.domain.user.application

import kr.co.lokit.api.common.exception.BusinessException
import kr.co.lokit.api.config.security.JwtTokenProvider
import kr.co.lokit.api.domain.user.domain.User
import kr.co.lokit.api.domain.user.dto.AuthResult
import kr.co.lokit.api.domain.user.infrastructure.UserRepository
import kr.co.lokit.api.domain.workspace.domain.WorkSpace
import kr.co.lokit.api.domain.workspace.infrastructure.WorkspaceRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val jwtTokenProvider: JwtTokenProvider,
    private val workspaceRepository: WorkspaceRepository,
) {
    @Transactional
    fun register(user: User): AuthResult {
        if (userRepository.existsByEmail(user.email)) {
            throw BusinessException.UserAlreadyExistsException()
        }

        val savedUser = userRepository.save(user)
        val token = jwtTokenProvider.generateToken(savedUser)

        workspaceRepository.saveWithUser(WorkSpace.createDefault(), savedUser.id)

        return AuthResult(
            token = token,
            user = savedUser,
        )
    }
}
