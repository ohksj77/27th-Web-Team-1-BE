package kr.co.lokit.api.domain.album.infrastructure

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface AlbumJpaRepository : JpaRepository<AlbumEntity, Long> {
    @Query(
        """
        select distinct a from Album a
        left join fetch a.photos p
        left join fetch p.uploadedBy
        join a.workspace w
        join w.workspaceUsers wu
        where wu.user.id = :userId
        order by a.updatedAt desc, a.createdAt desc
        """
    )
    fun findAllByUserId(userId: Long): List<AlbumEntity>

    @Query(
        """
        select distinct a from Album a
        left join fetch a.photos p
        left join fetch p.uploadedBy
        order by a.photoAddedAt desc, a.createdAt desc
        """
    )
    fun findAllWithPhotos(): List<AlbumEntity>

    @Query(
        """
        select distinct a from Album a
        left join fetch a.photos p
        left join fetch p.uploadedBy
        where a.id = :id
        """
    )
    fun findByIdWithPhotos(id: Long): List<AlbumEntity>
}
