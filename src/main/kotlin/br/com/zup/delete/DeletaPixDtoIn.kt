package br.com.zup.delete

import br.com.zup.shared.validation.ValidUUID
import io.micronaut.core.annotation.Introspected

@Introspected
data class DeletaPixDtoIn(
    @field:ValidUUID
    val pixId: String?,
    @field:ValidUUID
    val idClienteBancario: String?,
){
    var id: Long? = null
    var valorChavePix: String? = null
}
