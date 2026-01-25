package kr.co.lokit.api.domain.map.infrastructure.geocoding

import kr.co.lokit.api.domain.map.dto.LocationInfoResponse

interface GeocodingClient {
    fun reverseGeocode(longitude: Double, latitude: Double): LocationInfoResponse
}
