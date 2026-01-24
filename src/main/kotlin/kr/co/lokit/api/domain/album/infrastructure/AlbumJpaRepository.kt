package kr.co.lokit.api.domain.album.infrastructure

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface AlbumJpaRepository : JpaRepository<AlbumEntity, Long> {
    @Query("select a from AlbumEntity a order by a.updatedAt, a.createdAt desc")
    fun findAllByUserId(userId: Long): List<AlbumEntity>

    @Query(
        "select distinct a from AlbumEntity a " +
            "left join fetch a.photos " +
            "order by a.photoAddedAt desc, a.createdAt desc",
    )
    fun findAllWithPhotos(): List<AlbumEntity>
}
