package br.com.zup.consulta

import io.micronaut.core.annotation.Introspected
import javax.validation.constraints.Size

@Introspected
data class ValorChavePixRequest(
    @field:Size(max = 77, message = "O valor máximo é 77 caracteres.")
    val valorChavePix: String?)