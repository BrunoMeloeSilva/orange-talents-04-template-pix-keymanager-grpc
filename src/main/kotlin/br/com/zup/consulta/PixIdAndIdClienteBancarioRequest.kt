package br.com.zup.consulta

import br.com.zup.shared.validation.ValidUUID
import io.micronaut.core.annotation.Introspected

@Introspected
data class PixIdAndIdClienteBancarioRequest(
    @field:ValidUUID
    val pixId: String?,
    @field:ValidUUID
    val idClienteBancario: String?,
)