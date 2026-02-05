package kr.co.lokit.api.common.annotation

import org.springframework.orm.ObjectOptimisticLockingFailureException
import org.springframework.retry.annotation.Backoff
import org.springframework.retry.annotation.Retryable

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@Retryable(
    retryFor = [ObjectOptimisticLockingFailureException::class],
    maxAttempts = 3,
    backoff = Backoff(delay = 50, multiplier = 2.0),
)
annotation class OptimisticRetry
