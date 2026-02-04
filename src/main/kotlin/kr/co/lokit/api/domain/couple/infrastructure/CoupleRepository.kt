package kr.co.lokit.api.domain.couple.infrastructure

import kr.co.lokit.api.domain.couple.domain.Couple

interface CoupleRepository {
    fun save(couple: Couple): Couple

    fun findById(id: Long): Couple?

    fun saveWithUser(couple: Couple, userId: Long): Couple

    fun findByInviteCode(inviteCode: String): Couple?

    fun addUser(coupleId: Long, userId: Long): Couple

    fun findByUserId(userId: Long): Couple?
}
