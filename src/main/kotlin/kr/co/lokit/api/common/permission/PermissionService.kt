package kr.co.lokit.api.common.permission

import kr.co.lokit.api.common.constant.UserRole
import kr.co.lokit.api.common.exception.entityNotFound
import kr.co.lokit.api.domain.album.domain.Album
import kr.co.lokit.api.domain.album.infrastructure.AlbumRepository
import kr.co.lokit.api.domain.couple.domain.Couple
import kr.co.lokit.api.domain.couple.infrastructure.CoupleRepository
import kr.co.lokit.api.domain.photo.infrastructure.PhotoRepository
import kr.co.lokit.api.domain.user.domain.User
import kr.co.lokit.api.domain.user.infrastructure.UserRepository
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class PermissionService(
    private val coupleRepository: CoupleRepository,
    private val albumRepository: AlbumRepository,
    private val photoRepository: PhotoRepository,
    private val userRepository: UserRepository,
) {
    fun isAdmin(userId: Long): Boolean {
        val user = userRepository.findById(userId)
            ?: throw entityNotFound<User>(userId)
        return user.role == UserRole.ADMIN
    }

    @Cacheable(cacheNames = ["coupleMembership"], key = "#userId + ':' + #coupleId")
    fun isCoupleMember(userId: Long, coupleId: Long): Boolean {
        if (isAdmin(userId)) {
            return true
        }

        val couple =
            coupleRepository.findById(coupleId)
                ?: throw entityNotFound<Couple>(coupleId)

        return userId in couple.userIds
    }

    @Cacheable(cacheNames = ["albumCouple"], key = "#albumId")
    fun getAlbumCoupleId(albumId: Long): Long {
        val album =
            albumRepository.findById(albumId)
                ?: throw entityNotFound<Album>(albumId)
        return album.coupleId
    }

    @Cacheable(cacheNames = ["album"], key = "#userId + ':' + #albumId")
    fun canAccessAlbum(userId: Long, albumId: Long): Boolean {
        if (isAdmin(userId)) {
            return true
        }

        val coupleId = getAlbumCoupleId(albumId)
        return isCoupleMember(userId, coupleId)
    }

    fun canModifyAlbum(userId: Long, albumId: Long): Boolean {
        if (isAdmin(userId)) {
            return true
        }

        val album =
            albumRepository.findById(albumId)
                ?: throw entityNotFound<Album>(albumId)

        return album.createdById == userId
    }

    fun canDeleteAlbum(userId: Long, albumId: Long): Boolean {
        if (isAdmin(userId)) {
            return true
        }

        val album =
            albumRepository.findById(albumId)
                ?: throw entityNotFound<Album>(albumId)

        return album.createdById == userId
    }

    @Cacheable(cacheNames = ["photo"], key = "#userId + ':' + #photoId")
    fun canReadPhoto(userId: Long, photoId: Long): Boolean {
        if (isAdmin(userId)) {
            return true
        }

        val photo =
            photoRepository.findById(photoId)

        val album =
            albumRepository.findById(photo.albumId!!)
                ?: throw entityNotFound<Album>(photo.albumId)

        return isCoupleMember(userId, album.coupleId)
    }

    fun canModifyPhoto(userId: Long, photoId: Long): Boolean {
        if (isAdmin(userId)) {
            return true
        }

        val photo =
            photoRepository.findById(photoId)

        return photo.uploadedById == userId
    }

    fun canDeletePhoto(userId: Long, photoId: Long): Boolean {
        if (isAdmin(userId)) {
            return true
        }

        val photo =
            photoRepository.findById(photoId)

        return photo.uploadedById == userId
    }
}
