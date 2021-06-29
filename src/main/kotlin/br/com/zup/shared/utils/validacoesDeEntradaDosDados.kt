package br.com.zup.shared.utils

import io.micronaut.validation.validator.Validator
import javax.validation.ConstraintViolationException

fun validacoesDeEntradaDosDados(
    validator: Validator,
    Dto: Any,
) {
    val errosValidacao = validator.validate(Dto)
    if (errosValidacao.isNotEmpty())
        throw ConstraintViolationException(errosValidacao)
}