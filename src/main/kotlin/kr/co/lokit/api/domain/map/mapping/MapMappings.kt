package kr.co.lokit.api.domain.map.mapping

import kr.co.lokit.api.common.dto.PageResult
import kr.co.lokit.api.domain.map.domain.ClusterId
import kr.co.lokit.api.domain.map.dto.ClusterPhotoResponse
import kr.co.lokit.api.domain.map.dto.ClusterPhotosPageResponse
import kr.co.lokit.api.domain.map.dto.ClusterResponse
import kr.co.lokit.api.domain.map.dto.MapPhotoResponse
import kr.co.lokit.api.domain.map.infrastructure.ClusterPhotoProjection
import kr.co.lokit.api.domain.map.infrastructure.ClusterProjection
import kr.co.lokit.api.domain.map.infrastructure.PhotoProjection

fun ClusterProjection.toResponse(zoom: Int): ClusterResponse =
    ClusterResponse(
        clusterId = ClusterId.format(zoom, cellX, cellY),
        count = count,
        thumbnailUrl = thumbnailUrl,
        longitude = centerLongitude,
        latitude = centerLatitude,
    )

fun PhotoProjection.toMapPhotoResponse(): MapPhotoResponse =
    MapPhotoResponse(
        id = id,
        thumbnailUrl = url,
        longitude = longitude,
        latitude = latitude,
    )

fun ClusterPhotoProjection.toClusterPhotoResponse(): ClusterPhotoResponse =
    ClusterPhotoResponse(
        id = id,
        url = url,
        longitude = longitude,
        latitude = latitude,
        createdAt = createdAt,
    )

fun PageResult<ClusterPhotoProjection>.toClusterPhotosPageResponse(): ClusterPhotosPageResponse =
    ClusterPhotosPageResponse(
        photos = content.map { it.toClusterPhotoResponse() },
        page = page,
        size = size,
        totalElements = totalElements,
        totalPages = totalPages,
        last = isLast,
    )