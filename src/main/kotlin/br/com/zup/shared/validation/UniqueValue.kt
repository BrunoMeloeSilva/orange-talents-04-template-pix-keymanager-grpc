package br.com.zup.shared.validation

import br.com.zup.shared.exception.ValorJaExisteException
import io.micronaut.core.annotation.AnnotationValue
import io.micronaut.validation.validator.constraints.ConstraintValidator
import io.micronaut.validation.validator.constraints.ConstraintValidatorContext
import org.hibernate.SessionFactory
import javax.inject.Singleton
import javax.validation.Constraint
import javax.validation.Payload
import kotlin.reflect.KClass

@MustBeDocumented
@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [UniqueValueValidator::class])
annotation class UniqueValue(val table: String, val field: String,
                             val message: String = "O valor informado já existe.",
                             val groups: Array<KClass<Any>> = [],
                             val payload: Array<KClass<Payload>> = [])
@Singleton
class UniqueValueValidator(private val sessionFactory: SessionFactory) : ConstraintValidator<UniqueValue, Any> {
    override fun isValid(
        value: Any?,
        annotationMetadata: AnnotationValue<UniqueValue>,
        context: ConstraintValidatorContext
    ): Boolean {
        val table = annotationMetadata.values["table"] as String
        val field = annotationMetadata.values["field"] as String

        val openSession = sessionFactory.openSession()
        val entityManager = openSession.entityManagerFactory.createEntityManager()

        val query = entityManager.createQuery("Select Count(*) > 0 From $table t Where t.$field = :$field")
        query.setParameter(field, value)
        val existe = query.singleResult as Boolean

        openSession.close()

        if(existe){
            throw ValorJaExisteException("O valor informado para o campo [$field], já foi cadastrado.")
        }

        return !existe
    }
}