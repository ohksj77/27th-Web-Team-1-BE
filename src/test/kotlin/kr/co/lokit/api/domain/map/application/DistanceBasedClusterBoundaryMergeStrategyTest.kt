package kr.co.lokit.api.domain.map.application

import kr.co.lokit.api.domain.map.dto.ClusterResponse
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DistanceBasedClusterBoundaryMergeStrategyTest {
    private val strategy = DistanceBasedClusterBoundaryMergeStrategy()

    @Test
    fun `줌 11에서는 인접한 2km 클러스터가 병합되지 않는다`() {
        val result = strategy.mergeClusters(clustersAroundTwoKm(), 11)

        assertEquals(2, result.size)
    }

    @Test
    fun `줌 11에서는 인접한 700m 클러스터가 병합된다`() {
        val result = strategy.mergeClusters(clustersAroundSevenHundredMeters(), 11)

        assertEquals(1, result.size)
        assertEquals(2, result.first().count)
    }

    @Test
    fun `클러스터가 1개면 그대로 반환한다`() {
        val clusters = listOf(clustersAroundSevenHundredMeters().first())

        val result = strategy.mergeClusters(clusters, 11)

        assertEquals(1, result.size)
        assertEquals(clusters.first().clusterId, result.first().clusterId)
    }

    @Test
    fun `clusterId 파싱이 불가능한 항목은 병합 대상에서 제외한다`() {
        val clusters =
            listOf(
                ClusterResponse("invalid", 1, "a.jpg", 127.0, 37.3),
                ClusterResponse("z11_100_100", 2, "b.jpg", 127.001, 37.3001),
            )

        val result = strategy.mergeClusters(clusters, 11)

        assertEquals(2, result.size)
    }

    @Test
    fun `대표 셀은 y 이후 x가 가장 작은 셀을 사용한다`() {
        val clusters =
            listOf(
                ClusterResponse("z11_10_10", 1, "a.jpg", 127.0, 37.3),
                ClusterResponse("z11_9_10", 1, "b.jpg", 127.0005, 37.3002),
            )

        val result = strategy.mergeClusters(clusters, 11)

        assertEquals(1, result.size)
        assertEquals("z11_9_10", result.first().clusterId)
    }

    @Test
    fun `병합 결과 thumbnail은 count가 가장 큰 클러스터를 따른다`() {
        val clusters =
            listOf(
                ClusterResponse("z11_10_10", 1, "small.jpg", 127.0, 37.3),
                ClusterResponse("z11_10_11", 3, "dominant.jpg", 127.0025, 37.3001),
            )

        val result = strategy.mergeClusters(clusters, 11)

        assertEquals(1, result.size)
        assertEquals("dominant.jpg", result.first().thumbnailUrl)
        assertEquals(4, result.first().count)
    }

    @Test
    fun `resolveClusterCells는 target이 없으면 target 단독 셀을 반환한다`() {
        val photosByCell = mapOf(CellCoord(1, 1) to listOf(GeoPoint(127.0, 37.0)))

        val result = strategy.resolveClusterCells(11, photosByCell, CellCoord(2, 2))

        assertTrue(result.isEmpty())
    }

    @Test
    fun `resolveClusterCells는 인접하고 가까운 셀들을 함께 반환한다`() {
        val photosByCell =
            mapOf(
                CellCoord(100, 100) to listOf(GeoPoint(127.0, 37.3), GeoPoint(127.0005, 37.3001)),
                CellCoord(100, 101) to listOf(GeoPoint(127.003, 37.3002)),
            )

        val result = strategy.resolveClusterCells(11, photosByCell, CellCoord(100, 100))

        assertTrue(CellCoord(100, 100) in result)
        assertTrue(CellCoord(100, 101) in result)
    }

    @Test
    fun `resolveClusterCells는 멀리 떨어진 셀을 병합하지 않는다`() {
        val photosByCell =
            mapOf(
                CellCoord(100, 100) to listOf(GeoPoint(127.0, 37.3)),
                CellCoord(100, 101) to listOf(GeoPoint(127.05, 37.3)),
            )

        val result = strategy.resolveClusterCells(11, photosByCell, CellCoord(100, 100))

        assertEquals(setOf(CellCoord(100, 100)), result)
    }

    private fun clustersAroundTwoKm(): List<ClusterResponse> =
        listOf(
            ClusterResponse(
                clusterId = "z13_12330_3904",
                count = 1,
                thumbnailUrl = "a.jpg",
                longitude = 127.0,
                latitude = 37.3,
            ),
            ClusterResponse(
                clusterId = "z13_12330_3905",
                count = 1,
                thumbnailUrl = "b.jpg",
                longitude = 127.0225,
                latitude = 37.3,
            ),
        )

    private fun clustersAroundSevenHundredMeters(): List<ClusterResponse> =
        listOf(
            ClusterResponse(
                clusterId = "z13_12330_3904",
                count = 1,
                thumbnailUrl = "a.jpg",
                longitude = 127.0,
                latitude = 37.3,
            ),
            ClusterResponse(
                clusterId = "z13_12330_3905",
                count = 1,
                thumbnailUrl = "b.jpg",
                longitude = 127.008,
                latitude = 37.3,
            ),
        )
}
