package kr.co.lokit.api.domain.map.presentation

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import kr.co.lokit.api.domain.map.dto.ClusterPhotosPageResponse
import kr.co.lokit.api.domain.map.dto.MapPhotosResponse

@SecurityRequirement(name = "Authorization")
@Tag(name = "Map", description = "지도 API")
interface MapApi {
    @Operation(
        summary = "지도 사진 조회",
        description = """
            줌 레벨과 바운딩 박스를 기반으로 지도에 표시할 사진 또는 클러스터를 조회합니다.

            - **줌 레벨 < 15**: ST_SnapToGrid를 사용하여 사진을 클러스터링하여 반환
              - clusterId: 줌 레벨 + 그리드 셀 인덱스 (예: z14_130234_38456)
              - count: 클러스터 내 사진 개수
              - thumbnailUrl: 클러스터 내 가장 최근 생성된 사진의 URL

            - **줌 레벨 >= 15**: 개별 사진 썸네일을 반환
        """,
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "조회 성공",
                content = [Content(schema = Schema(implementation = MapPhotosResponse::class))],
            ),
            ApiResponse(
                responseCode = "400",
                description = "잘못된 요청 파라미터",
                content = [Content()],
            ),
            ApiResponse(
                responseCode = "401",
                description = "인증 필요",
                content = [Content()],
            ),
        ],
    )
    fun getPhotos(
        @Parameter(
            description = "줌 레벨 (0-20). 15 미만이면 클러스터링, 15 이상이면 개별 사진 반환",
            example = "12",
            required = true,
        )
        zoom: Int,
        @Parameter(
            description = "바운딩 박스 (west,south,east,north 형식의 경도/위도)",
            example = "126.9,37.4,127.1,37.6",
            required = true,
        )
        bbox: String,
    ): MapPhotosResponse

    @Operation(
        summary = "클러스터 내 사진 목록 조회",
        description = """
            클러스터 ID를 기반으로 해당 그리드 셀 영역 내의 모든 사진을 조회합니다.

            - clusterId는 GET /map/photos 응답에서 반환된 값을 사용
            - clusterId 형식: z{zoom}_{cellX}_{cellY} (예: z14_130234_38456)
            - 사진은 생성일시 기준 내림차순 정렬
            - 페이지네이션 지원 (page, size 파라미터)
        """,
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "조회 성공",
                content = [Content(schema = Schema(implementation = ClusterPhotosPageResponse::class))],
            ),
            ApiResponse(
                responseCode = "400",
                description = "잘못된 clusterId 형식",
                content = [Content()],
            ),
            ApiResponse(
                responseCode = "401",
                description = "인증 필요",
                content = [Content()],
            ),
        ],
    )
    fun getClusterPhotos(
        @Parameter(
            description = "클러스터 ID (z{zoom}_{cellX}_{cellY} 형식)",
            example = "z14_130234_38456",
            required = true,
        )
        clusterId: String,
        @Parameter(
            description = "페이지 번호 (0부터 시작)",
            example = "0",
        )
        page: Int,
        @Parameter(
            description = "페이지 크기",
            example = "20",
        )
        size: Int,
    ): ClusterPhotosPageResponse
}