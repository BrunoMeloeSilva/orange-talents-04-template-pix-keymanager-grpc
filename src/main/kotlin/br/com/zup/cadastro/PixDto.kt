package br.com.zup.cadastro

import br.com.zup.TipoChavePix
import br.com.zup.TipoContaBancaria
import br.com.zup.shared.validation.EnumInterval
import br.com.zup.shared.validation.UniqueValue
import br.com.zup.shared.validation.ValidUUID
import io.micronaut.core.annotation.Introspected
import javax.validation.constraints.Pattern
import javax.validation.constraints.Size

@ValorChavePixValidated
@Introspected
data class PixDto(
    @field:ValidUUID
    val idClienteBancario: String,
    @field:EnumInterval(from = 1, to = 4, message = "Os valores permitidos são: CPF = 1, CELULAR = 2, EMAIL = 3, ALEATORIA = 4.")
    val tipoChavePix: TipoChavePix,
    @field:Size(max = 77, message = "O valor máximo é 77 caracteres.")
    @field:UniqueValue(table = "PixModel", field = "valorChavePix")
    val valorChavePix: String,
    @field:EnumInterval(from = 1, to = 2, message = "Os valores permitidos são: CONTA_CORRENTE = 1, CONTA_POUPANCA = 2.")
    val tipoContaBancaria: TipoContaBancaria,
){
    fun toPixModel(): PixModel{
        return PixModel(
            this.idClienteBancario,
            this.tipoChavePix,
            this.valorChavePix,
            this.tipoContaBancaria)
    }
}
