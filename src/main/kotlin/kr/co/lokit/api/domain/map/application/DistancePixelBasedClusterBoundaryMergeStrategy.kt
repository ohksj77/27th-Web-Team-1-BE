package kr.co.lokit.api.domain.map.application

import kr.co.lokit.api.domain.map.domain.ClusterId
import kr.co.lokit.api.domain.map.dto.ClusterResponse
import java.time.LocalDateTime
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.ln
import kotlin.math.pow
import kotlin.math.sin

class DistancePixelBasedClusterBoundaryMergeStrategy : ClusterBoundaryMergeStrategy {
    override fun mergeClusters(
        clusters: List<ClusterResponse>,
        zoomLevel: Double,
    ): List<ClusterResponse> {
        if (clusters.size < 2) return clusters

        val z = normalizeZoomLevel(zoomLevel)
        val zoomDiscrete = floor(z).toInt()

        val parsed =
            clusters.mapNotNull { cluster ->
                runCatching { ClusterId.parse(cluster.clusterId) }
                    .getOrNull()
                    ?.let { cell ->
                        MergeNode(
                            cellX = cell.cellX,
                            cellY = cell.cellY,
                            count = cluster.count,
                            thumbnailUrl = cluster.thumbnailUrl,
                            longitude = cluster.longitude,
                            latitude = cluster.latitude,
                            takenAt = cluster.takenAt,
                        )
                    }
            }

        if (parsed.size < 2) return clusters

        val groups =
            buildGroupsByPixelOverlap(
                zoomLevel = z,
                nodes = parsed,
                cellExtractor = { CellCoord(it.cellX, it.cellY) },
                lonLatExtractor = { it.longitude to it.latitude },
            )

        return groups.map { group ->
            val nodes = group.map { parsed[it] }

            val representative =
                nodes.minWith(
                    compareBy<MergeNode> { it.cellY }
                        .thenBy { it.cellX },
                )

            val totalCount = nodes.sumOf { it.count }
            val sumLon = nodes.sumOf { it.longitude * it.count }
            val sumLat = nodes.sumOf { it.latitude * it.count }

            val latestTakenAtNode =
                nodes.maxByOrNull { it.takenAt ?: LocalDateTime.MIN } ?: representative

            ClusterResponse(
                clusterId = ClusterId.format(zoomDiscrete, representative.cellX, representative.cellY),
                count = totalCount,
                thumbnailUrl = latestTakenAtNode.thumbnailUrl,
                longitude = if (totalCount > 0) sumLon / totalCount else representative.longitude,
                latitude = if (totalCount > 0) sumLat / totalCount else representative.latitude,
                takenAt = latestTakenAtNode.takenAt,
            )
        }
    }

    override fun resolveClusterCells(
        zoom: Int,
        photosByCell: Map<CellCoord, List<GeoPoint>>,
        targetCell: CellCoord,
    ): Set<CellCoord> {
        if (photosByCell.size < 2) {
            return if (targetCell in photosByCell.keys) setOf(targetCell) else emptySet()
        }

        val cells = photosByCell.keys.toList()

        val cellCenters =
            cells.associateWith { cell ->
                val points = photosByCell[cell].orEmpty()
                val totalWeight = points.sumOf { it.weight }.coerceAtLeast(1)
                val lon = points.sumOf { it.longitude * it.weight } / totalWeight
                val lat = points.sumOf { it.latitude * it.weight } / totalWeight
                GeoPoint(lon, lat, totalWeight)
            }

        val z = zoom.toDouble()

        val groups =
            buildGroupsByPixelOverlap(
                zoomLevel = z,
                nodes = cells,
                cellExtractor = { it },
                lonLatExtractor = { cell ->
                    val center = cellCenters[cell] ?: GeoPoint(0.0, 0.0)
                    center.longitude to center.latitude
                },
            )

        val idxByCell = cells.withIndex().associate { it.value to it.index }
        val targetIdx = idxByCell[targetCell] ?: return setOf(targetCell)
        val matched = groups.firstOrNull { targetIdx in it } ?: return setOf(targetCell)

        return matched.map { cells[it] }.toSet()
    }

    private fun lonLatToWorldPx(
        lon: Double,
        lat: Double,
        zoom: Double,
    ): Pair<Double, Double> {
        val worldSize = 256.0 * 2.0.pow(zoom)
        val x = (lon + 180.0) / 360.0 * worldSize

        val siny = sin(Math.toRadians(lat)).coerceIn(-0.9999, 0.9999)
        val y = (0.5 - ln((1 + siny) / (1 - siny)) / (4 * Math.PI)) * worldSize

        return x to y
    }

    private fun shouldMergeByPixelOverlap(
        ax: Double,
        ay: Double,
        bx: Double,
        by: Double,
    ): Boolean {
        val dx = abs(ax - bx)
        val dy = abs(ay - by)
        return dx <= MERGE_DX_PX && dy <= MERGE_DY_PX
    }

    private fun <T> buildGroupsByPixelOverlap(
        zoomLevel: Double,
        nodes: List<T>,
        cellExtractor: (T) -> CellCoord,
        lonLatExtractor: (T) -> Pair<Double, Double>,
    ): List<List<Int>> {
        val parent = IntArray(nodes.size) { it }

        fun find(x: Int): Int {
            var cur = x
            while (parent[cur] != cur) {
                parent[cur] = parent[parent[cur]]
                cur = parent[cur]
            }
            return cur
        }

        fun union(
            a: Int,
            b: Int,
        ) {
            val ra = find(a)
            val rb = find(b)
            if (ra != rb) parent[rb] = ra
        }

        val cells = nodes.map(cellExtractor)
        val px =
            nodes.map { n ->
                val (lon, lat) = lonLatExtractor(n)
                val (x, y) = lonLatToWorldPx(lon, lat, zoomLevel)
                x to y
            }

        val maxCellDx = ceil(MERGE_DX_PX / GRID_PIXELS).toLong()
        val maxCellDy = ceil(MERGE_DY_PX / GRID_PIXELS).toLong()

        for (i in nodes.indices) {
            for (j in i + 1 until nodes.size) {
                val a = cells[i]
                val b = cells[j]

                if (abs(a.x - b.x) > maxCellDx || abs(a.y - b.y) > maxCellDy) continue

                val (ax, ay) = px[i]
                val (bx, by) = px[j]

                if (shouldMergeByPixelOverlap(ax, ay, bx, by)) {
                    union(i, j)
                }
            }
        }

        return nodes.indices
            .groupBy { find(it) }
            .values
            .map { it.toList() }
    }

    private fun normalizeZoomLevel(zoomLevel: Double): Double = zoomLevel.coerceIn(0.0, MAX_ZOOM_LEVEL.toDouble())

    private data class MergeNode(
        val cellX: Long,
        val cellY: Long,
        val count: Int,
        val thumbnailUrl: String,
        val longitude: Double,
        val latitude: Double,
        val takenAt: LocalDateTime?,
    )

    companion object {
        private const val MAX_ZOOM_LEVEL = 22

        private const val POI_WIDTH_PX = 74.0
        private const val POI_HEIGHT_PX = 100.0
        private const val REQUIRED_OVERLAP_RATIO = 1.0 / 3.0

        private const val MERGE_DX_PX = (1.0 - REQUIRED_OVERLAP_RATIO) * POI_WIDTH_PX
        private const val MERGE_DY_PX = (1.0 - REQUIRED_OVERLAP_RATIO) * POI_HEIGHT_PX

        private const val GRID_PIXELS = 60.0
    }
}
