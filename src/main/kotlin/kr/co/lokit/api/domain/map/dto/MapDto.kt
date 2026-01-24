package kr.co.lokit.api.domain.map.dto

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

@Schema(description = "클러스터 응답")
data class ClusterResponse(
    @Schema(description = "클러스터 ID (줌 레벨 + 그리드 셀 인덱스)", example = "z14_130234_38456")
    val clusterId: String,
    @Schema(description = "클러스터 내 사진 개수", example = "42")
    val count: Int,
    @Schema(description = "대표 썸네일 URL (가장 최근 생성된 사진)", example = "https://example.com/photo.jpg")
    val thumbnailUrl: String,
    @Schema(description = "클러스터 중심 경도", example = "127.0276")
    val longitude: Double,
    @Schema(description = "클러스터 중심 위도", example = "37.4979")
    val latitude: Double,
)

@Schema(description = "개별 사진 응답 (줌 >= 15)")
data class MapPhotoResponse(
    @Schema(description = "사진 ID", example = "1")
    val id: Long,
    @Schema(description = "썸네일 URL", example = "https://example.com/photo.jpg")
    val thumbnailUrl: String,
    @Schema(description = "경도", example = "127.0276")
    val longitude: Double,
    @Schema(description = "위도", example = "37.4979")
    val latitude: Double,
)

@Schema(description = "지도 사진 조회 응답")
data class MapPhotosResponse(
    @Schema(description = "클러스터 목록 (줌 < 15일 때)")
    val clusters: List<ClusterResponse>? = null,
    @Schema(description = "개별 사진 목록 (줌 >= 15일 때)")
    val photos: List<MapPhotoResponse>? = null,
)

@Schema(description = "클러스터 내 사진 상세 응답")
data class ClusterPhotoResponse(
    @Schema(description = "사진 ID", example = "1")
    val id: Long,
    @Schema(description = "사진 URL", example = "https://example.com/photo.jpg")
    val url: String,
    @Schema(description = "경도", example = "127.0276")
    val longitude: Double,
    @Schema(description = "위도", example = "37.4979")
    val latitude: Double,
    @Schema(description = "생성일시", example = "2024-01-15T10:30:00")
    val createdAt: LocalDateTime,
)

@Schema(description = "클러스터 사진 목록 페이지네이션 응답")
data class ClusterPhotosPageResponse(
    @Schema(description = "사진 목록")
    val photos: List<ClusterPhotoResponse>,
    @Schema(description = "현재 페이지 번호 (0부터 시작)", example = "0")
    val page: Int,
    @Schema(description = "페이지 크기", example = "20")
    val size: Int,
    @Schema(description = "전체 사진 개수", example = "42")
    val totalElements: Long,
    @Schema(description = "전체 페이지 수", example = "3")
    val totalPages: Int,
    @Schema(description = "마지막 페이지 여부", example = "false")
    val last: Boolean,
)