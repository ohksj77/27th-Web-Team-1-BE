package kr.co.lokit.api.domain.photo.infrastructure.file

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.AbortIncompleteMultipartUpload
import software.amazon.awssdk.services.s3.model.BucketLifecycleConfiguration
import software.amazon.awssdk.services.s3.model.ExpirationStatus
import software.amazon.awssdk.services.s3.model.GetBucketLifecycleConfigurationRequest
import software.amazon.awssdk.services.s3.model.LifecycleRule
import software.amazon.awssdk.services.s3.model.LifecycleRuleFilter
import software.amazon.awssdk.services.s3.model.PutBucketLifecycleConfigurationRequest
import software.amazon.awssdk.services.s3.model.S3Exception

@Component
@ConditionalOnProperty(name = ["aws.s3.enabled"], havingValue = "true")
class S3LifecycleConfigurer(
    private val s3Client: S3Client,
    @Value("\${aws.s3.bucket}") private val bucket: String,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @Async
    @EventListener(ApplicationReadyEvent::class)
    fun configureLifecycle() {
        try {
            val existingRules = fetchExistingRules()
            if (existingRules.any { it.id() == RULE_ID }) {
                log.info("S3 lifecycle 규칙 '{}' 이미 존재, skip", RULE_ID)
                return
            }

            val newRule = LifecycleRule.builder()
                .id(RULE_ID)
                .filter(LifecycleRuleFilter.builder().prefix(PREFIX).build())
                .status(ExpirationStatus.ENABLED)
                .abortIncompleteMultipartUpload(
                    AbortIncompleteMultipartUpload.builder()
                        .daysAfterInitiation(1)
                        .build(),
                )
                .build()

            val allRules = existingRules + newRule
            s3Client.putBucketLifecycleConfiguration(
                PutBucketLifecycleConfigurationRequest.builder()
                    .bucket(bucket)
                    .lifecycleConfiguration(
                        BucketLifecycleConfiguration.builder()
                            .rules(allRules)
                            .build(),
                    )
                    .build(),
            )
            log.info("S3 lifecycle 규칙 '{}' 설정 완료", RULE_ID)
        } catch (e: S3Exception) {
            if (e.statusCode() == 403) {
                log.warn("S3 lifecycle 규칙 설정 권한 부족 (403), 앱은 정상 기동합니다")
            } else {
                log.warn("S3 lifecycle 규칙 설정 실패: {}", e.message)
            }
        }
    }

    private fun fetchExistingRules(): List<LifecycleRule> =
        try {
            s3Client.getBucketLifecycleConfiguration(
                GetBucketLifecycleConfigurationRequest.builder()
                    .bucket(bucket)
                    .build(),
            ).rules()
        } catch (e: S3Exception) {
            if (e.statusCode() == 404) emptyList() else throw e
        }

    companion object {
        private const val RULE_ID = "abort-incomplete-multipart-photos"
        private const val PREFIX = "photos/"
    }
}
