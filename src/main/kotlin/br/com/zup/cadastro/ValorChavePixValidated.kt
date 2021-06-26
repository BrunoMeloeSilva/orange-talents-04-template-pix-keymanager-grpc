package br.com.zup.cadastro

import br.com.zup.TipoChavePix
import io.micronaut.core.annotation.AnnotationValue
import io.micronaut.validation.validator.constraints.ConstraintValidator
import org.hibernate.validator.internal.constraintvalidators.hv.br.CPFValidator
import javax.inject.Singleton
import javax.validation.Constraint
import javax.validation.Payload
import kotlin.annotation.AnnotationTarget.CLASS
import kotlin.annotation.AnnotationTarget.TYPE
import kotlin.annotation.AnnotationRetention.RUNTIME
import kotlin.reflect.KClass

@MustBeDocumented
@Target(CLASS, TYPE)
@Retention(RUNTIME)
@Constraint(validatedBy = [ValidaValorChavePixValidator::class])
annotation class ValorChavePixValidated(
    val message: String = "o campo 'valorChavePix' é inválido para o 'tipoChavePix' informado.",
    val groups: Array<KClass<Any>> = [],
    val payload: Array<KClass<Payload>> = []
)

@Singleton
class ValidaValorChavePixValidator : ConstraintValidator<ValorChavePixValidated, PixDto> {
    override fun isValid(
        value: PixDto,
        annotationMetadata: AnnotationValue<ValorChavePixValidated>,
        context: io.micronaut.validation.validator.constraints.ConstraintValidatorContext
    ): Boolean {

        val valorChavePix = value.valorChavePix
        return when(value.tipoChavePix){
            TipoChavePix.CPF -> {
                if(valorChavePix.matches("^[0-9]{11}$".toRegex())
                    &&
                    CPFValidator().run {
                        initialize(null)
                        isValid(valorChavePix, null)
                    }) { return true }
                context.messageTemplate("Para o campo [tipoChavePix], CPF = 1, você deve informar um CPF válido e sem os pontos ou traços.")
                false
            }

            TipoChavePix.CELULAR -> {
                if(valorChavePix.matches("^\\+[1-9][0-9]\\d{1,14}$".toRegex())) return true

                context.messageTemplate("Para o campo [tipoChavePix], CELULAR = 2, você deve informar um CELULAR válido, iniciando com o símbolo de +, e contendo somente números. Exemplo: +5585988714077")
                false
            }

            TipoChavePix.EMAIL -> {
                if(valorChavePix.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$".toRegex())) { return true }

                context.messageTemplate("Para o campo [tipoChavePix], EMAIL = 3, você deve informar um EMAIL válido.")
                false
            }

            TipoChavePix.ALEATORIA -> {
                if(valorChavePix.isBlank()) return true

                context.messageTemplate("Para o campo [tipoChavePix], ALEATORIA = 4, você não deve informar um valor.")
                false
            }

            else -> false
        }
    }
}
