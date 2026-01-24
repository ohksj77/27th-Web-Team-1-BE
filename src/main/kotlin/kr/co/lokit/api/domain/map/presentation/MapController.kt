package kr.co.lokit.api.domain.map.presentation

import kr.co.lokit.api.domain.map.application.MapService
import kr.co.lokit.api.domain.map.domain.BBox
import kr.co.lokit.api.domain.map.dto.ClusterPhotosPageResponse
import kr.co.lokit.api.domain.map.dto.MapPhotosResponse
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("map")
class MapController(
    private val mapService: MapService,
) : MapApi {
    // TODO 응답 response에 위치 데이터 포함 예정, resource 변경 예정
    @GetMapping("/photos")
    override fun getPhotos(
        @RequestParam zoom: Int,
        @RequestParam bbox: String,
    ): MapPhotosResponse {
        val bboxParsed = BBox.fromString(bbox)
        return mapService.getPhotos(zoom, bboxParsed)
    }

    @GetMapping("/clusters/{clusterId}/photos")
    override fun getClusterPhotos(
        @PathVariable clusterId: String,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
    ): ClusterPhotosPageResponse = mapService.getClusterPhotos(clusterId, page, size)
}
