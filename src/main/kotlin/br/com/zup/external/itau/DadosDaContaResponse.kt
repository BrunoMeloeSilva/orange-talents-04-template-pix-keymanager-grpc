package br.com.zup.external.itau

import java.util.*

data class DadosDaContaResponse(
    val tipo: String?,
    val instituicao: Instituicao?,
    val agencia: String?,
    val numero: String?,
    val titular: Titular?,
){
    data class Instituicao(
        val nome: String?,
        val ispb: String?
    )

    data class Titular(
        val id: UUID?,
        val nome: String?,
        val cpf: String?,
    )
}