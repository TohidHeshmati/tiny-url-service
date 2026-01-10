package org.tohid.tinyurlservice.validators

import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import jakarta.validation.Payload
import java.time.Instant
import java.time.Instant.now
import kotlin.reflect.KClass

@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [FutureInstantValidator::class])
annotation class FutureInstant(
    val message: String = "The date must be in the future",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = [],
)

class FutureInstantValidator : ConstraintValidator<FutureInstant, Instant?> {
    override fun isValid(
        value: Instant?,
        context: ConstraintValidatorContext,
    ): Boolean = value == null || value.isAfter(now())
}
