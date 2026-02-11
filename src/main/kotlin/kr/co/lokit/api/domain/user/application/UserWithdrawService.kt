package kr.co.lokit.api.domain.user.application

import kr.co.lokit.api.common.exception.BusinessException
import kr.co.lokit.api.domain.couple.application.port.CoupleRepositoryPort
import kr.co.lokit.api.domain.user.application.port.UserRepositoryPort
import kr.co.lokit.api.domain.user.application.port.`in`.WithdrawUseCase
import kr.co.lokit.api.domain.user.infrastructure.RefreshTokenJpaRepository
import org.springframework.cache.CacheManager
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserWithdrawService(
    private val userRepository: UserRepositoryPort,
    private val coupleRepository: CoupleRepositoryPort,
    private val refreshTokenJpaRepository: RefreshTokenJpaRepository,
    private val cacheManager: CacheManager,
) : WithdrawUseCase {
    @Transactional
    override fun withdraw(userId: Long) {
        val user = userRepository.findById(userId)
            ?: throw BusinessException.UserNotFoundException(
                errors = mapOf("userId" to userId.toString()),
            )

        // 1. Refresh Token 전부 삭제
        refreshTokenJpaRepository.deleteByUserId(userId)

        // 2. 커플에서 유저 분리 (CoupleUser 삭제) — 커플 자체는 유지
        coupleRepository.removeCoupleUser(userId)

        // 3. User status → WITHDRAWN, withdrawnAt 설정
        userRepository.withdraw(userId)

        // 4. 캐시 무효화
        cacheManager.getCache("userDetails")?.evict(user.email)
        cacheManager.getCache("userCouple")?.evict(userId)
    }
}
