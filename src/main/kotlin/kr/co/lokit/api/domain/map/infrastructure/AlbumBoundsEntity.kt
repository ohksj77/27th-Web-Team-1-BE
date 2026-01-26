package kr.co.lokit.api.domain.map.infrastructure

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Index
import jakarta.persistence.Table
import kr.co.lokit.api.common.entity.BaseEntity

@Entity
@Table(
    name = "album_bounds",
    indexes = [Index(name = "idx_album_bounds_album_id", columnList = "album_id", unique = true)],
)
class AlbumBoundsEntity(
    @Column(name = "album_id", nullable = false, unique = true)
    val albumId: Long,
    @Column(name = "min_longitude", nullable = false)
    var minLongitude: Double,
    @Column(name = "max_longitude", nullable = false)
    var maxLongitude: Double,
    @Column(name = "min_latitude", nullable = false)
    var minLatitude: Double,
    @Column(name = "max_latitude", nullable = false)
    var maxLatitude: Double,
) : BaseEntity()
