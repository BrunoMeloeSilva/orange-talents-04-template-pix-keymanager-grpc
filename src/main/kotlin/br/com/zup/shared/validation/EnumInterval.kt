package br.com.zup.shared.validation

import io.micronaut.core.annotation.AnnotationValue
import io.micronaut.validation.validator.constraints.ConstraintValidator
import io.micronaut.validation.validator.constraints.ConstraintValidatorContext
import javax.inject.Singleton
import javax.validation.Constraint
import javax.validation.Payload
import kotlin.annotation.Target
import kotlin.annotation.AnnotationRetention.RUNTIME
import kotlin.annotation.AnnotationTarget.FIELD
import kotlin.reflect.KClass

@MustBeDocumented
@Target(FIELD)
@Retention(RUNTIME)
@Constraint(validatedBy = [EnumIntervalValidator::class])
annotation class EnumInterval(val from: Int, val to: Int,
                              val message: String = "Fora do intervalo definido.",
                              val groups: Array<KClass<Any>> = [],
                              val payload: Array<KClass<Payload>> = [])

@Singleton
class EnumIntervalValidator : ConstraintValidator<EnumInterval, Enum<*>> {
    override fun isValid(
        value: Enum<*>,
        annotationMetadata: AnnotationValue<EnumInterval>,
        context: ConstraintValidatorContext
    ): Boolean {
        val fromValue = annotationMetadata.values["from"] as Int
        val toValue = annotationMetadata.values["to"] as Int
        if(value.ordinal < fromValue || value.ordinal > toValue)
            return false
        return true
    }
}
