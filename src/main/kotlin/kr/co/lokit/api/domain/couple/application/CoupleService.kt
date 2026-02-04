package kr.co.lokit.api.domain.couple.application

import kr.co.lokit.api.common.exception.entityNotFound
import kr.co.lokit.api.domain.couple.domain.Couple
import kr.co.lokit.api.domain.couple.infrastructure.CoupleRepository
import org.springframework.cache.annotation.CacheEvict
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CoupleService(
    private val coupleRepository: CoupleRepository,
) {
    @Transactional
    @CacheEvict(cacheNames = ["coupleMembership"], key = "#userId + ':' + #result.id", condition = "#result != null")
    fun createIfNone(couple: Couple, userId: Long): Couple =
        coupleRepository.findByUserId(userId) ?: coupleRepository.saveWithUser(couple, userId)

    @Transactional
    @CacheEvict(cacheNames = ["coupleMembership"], key = "#userId + ':' + #result.id", condition = "#result != null")
    fun joinByInviteCode(inviteCode: String, userId: Long): Couple {
        val couple = coupleRepository.findByInviteCode(inviteCode)
            ?: throw entityNotFound<Couple>("inviteCode", inviteCode)
        return coupleRepository.addUser(couple.id, userId)
    }
}
