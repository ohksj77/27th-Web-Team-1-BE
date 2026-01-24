package kr.co.lokit.api.domain.map.application

import kr.co.lokit.api.domain.map.domain.BBox
import kr.co.lokit.api.domain.map.domain.ClusterId
import kr.co.lokit.api.domain.map.domain.GridConfig
import kr.co.lokit.api.domain.map.dto.ClusterPhotosPageResponse
import kr.co.lokit.api.domain.map.dto.MapPhotosResponse
import kr.co.lokit.api.domain.map.infrastructure.MapRepository
import kr.co.lokit.api.domain.map.mapping.toClusterPhotosPageResponse
import kr.co.lokit.api.domain.map.mapping.toMapPhotoResponse
import kr.co.lokit.api.domain.map.mapping.toResponse
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class MapService(
    private val mapRepository: MapRepository,
) {
    companion object {
        private const val CLUSTER_ZOOM_THRESHOLD = 15
    }

    @Transactional(readOnly = true)
    fun getPhotos(
        zoom: Int,
        bbox: BBox,
    ): MapPhotosResponse =
        if (zoom < CLUSTER_ZOOM_THRESHOLD) {
            getClusteredPhotos(zoom, bbox)
        } else {
            getIndividualPhotos(bbox)
        }

    private fun getClusteredPhotos(
        zoom: Int,
        bbox: BBox,
    ): MapPhotosResponse {
        val gridSize = GridConfig.getGridSize(zoom)

        val clusters =
            mapRepository.findClustersWithinBBox(
                west = bbox.west,
                south = bbox.south,
                east = bbox.east,
                north = bbox.north,
                gridSize = gridSize,
            )

        return MapPhotosResponse(
            clusters = clusters.map { it.toResponse(zoom) },
        )
    }

    private fun getIndividualPhotos(bbox: BBox): MapPhotosResponse {
        val photos =
            mapRepository.findPhotosWithinBBox(
                west = bbox.west,
                south = bbox.south,
                east = bbox.east,
                north = bbox.north,
            )

        return MapPhotosResponse(
            photos = photos.map { it.toMapPhotoResponse() },
        )
    }

    @Transactional(readOnly = true)
    fun getClusterPhotos(
        clusterId: String,
        page: Int,
        size: Int,
    ): ClusterPhotosPageResponse {
        val gridCell = ClusterId.parse(clusterId)
        val bbox = gridCell.toBBox()

        return mapRepository
            .findPhotosInGridCell(
                west = bbox.west,
                south = bbox.south,
                east = bbox.east,
                north = bbox.north,
                page = page,
                size = size,
            ).toClusterPhotosPageResponse()
    }
}
