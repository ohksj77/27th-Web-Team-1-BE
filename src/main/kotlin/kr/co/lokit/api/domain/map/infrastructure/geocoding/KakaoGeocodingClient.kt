package kr.co.lokit.api.domain.map.infrastructure.geocoding

import kr.co.lokit.api.domain.map.dto.LocationInfoResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient

@Component
@ConditionalOnProperty(name = ["kakao.api.key"])
class KakaoGeocodingClient(
    @Value("\${kakao.api.key}")
    private val apiKey: String,
) : GeocodingClient {

    private val restClient = RestClient.builder()
        .baseUrl(BASE_URL)
        .defaultHeader(HttpHeaders.AUTHORIZATION, "KakaoAK $apiKey")
        .build()

    override fun reverseGeocode(longitude: Double, latitude: Double): LocationInfoResponse {
        val response = restClient.get()
            .uri { uriBuilder ->
                uriBuilder
                    .path("/v2/local/geo/coord2address.json")
                    .queryParam("x", longitude)
                    .queryParam("y", latitude)
                    .build()
            }
            .retrieve()
            .body(KakaoGeocodingResponse::class.java)

        val document = response?.documents?.firstOrNull()
        val roadAddress = document?.roadAddress
        val address = document?.address

        return LocationInfoResponse(
            address = roadAddress?.addressName ?: address?.addressName,
            placeName = roadAddress?.buildingName?.takeIf { it.isNotBlank() },
            regionName = address?.region2depthName ?: roadAddress?.region2depthName,
        )
    }

    companion object {
        private const val BASE_URL = "https://dapi.kakao.com"
    }
}
