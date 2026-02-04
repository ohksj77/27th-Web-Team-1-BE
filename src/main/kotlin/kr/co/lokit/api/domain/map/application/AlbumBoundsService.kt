package kr.co.lokit.api.domain.map.application

import kr.co.lokit.api.domain.map.application.port.AlbumBoundsRepositoryPort
import kr.co.lokit.api.domain.map.domain.AlbumBounds
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AlbumBoundsService(
    private val albumBoundsRepository: AlbumBoundsRepositoryPort,
) {
    @Transactional
    fun updateBoundsOnPhotoAdd(
        albumId: Long,
        userId: Long,
        longitude: Double,
        latitude: Double,
    ) {
        updateAlbumBounds(albumId, longitude, latitude)
        updateAlbumBounds(userId, longitude, latitude)
    }

    private fun updateAlbumBounds(id: Long, longitude: Double, latitude: Double) {
        val existingBounds = albumBoundsRepository.findByAlbumId(id)
        if (existingBounds != null) {
            val expanded = existingBounds.expandedWith(longitude, latitude)
            albumBoundsRepository.apply(expanded)
        } else {
            albumBoundsRepository.save(
                AlbumBounds.createInitial(id, longitude, latitude)
            )
        }
    }
}
