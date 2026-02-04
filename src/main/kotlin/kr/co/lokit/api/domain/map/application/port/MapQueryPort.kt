package kr.co.lokit.api.domain.map.application.port

import kr.co.lokit.api.common.dto.PageResult

interface MapQueryPort {
    fun findClustersWithinBBox(
        west: Double,
        south: Double,
        east: Double,
        north: Double,
        gridSize: Double,
        userId: Long? = null,
        albumId: Long? = null,
    ): List<ClusterProjection>

    fun findPhotosWithinBBox(
        west: Double,
        south: Double,
        east: Double,
        north: Double,
        userId: Long? = null,
        albumId: Long? = null,
    ): List<PhotoProjection>

    fun findPhotosInGridCell(
        west: Double,
        south: Double,
        east: Double,
        north: Double,
        userId: Long? = null,
        page: Int,
        size: Int,
    ): PageResult<ClusterPhotoProjection>
}
