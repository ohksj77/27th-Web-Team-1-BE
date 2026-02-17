package kr.co.lokit.api.domain.map.application

enum class ClusterBoundaryMergeType(
    val propertyValue: String,
) {
    DISTANCE("distance"),
    DISTANCE_PIXEL("distance-pixel"),
    LEGACY("legacy"),
    ;

    companion object {
        fun from(raw: String?): ClusterBoundaryMergeType =
            entries.firstOrNull { it.propertyValue.equals(raw, ignoreCase = true) } ?: DISTANCE
    }
}
