package br.com.zup.shared.exception

import javax.validation.ValidationException

open class RegrasNegociosException(override val message: String) : ValidationException(message)