package br.com.zup.utils

import br.com.zup.CadastrarChavePixRequest
import br.com.zup.external.itau.DadosDaContaResponse
import java.util.*

fun getDadosDaContaResponse(cadastrarChavePixRequest: CadastrarChavePixRequest): DadosDaContaResponse {
        return DadosDaContaResponse(cadastrarChavePixRequest.tipoContaBancaria.name,
            DadosDaContaResponse.Instituicao(
                "ITAÚ UNIBANCO S.A.",
                "60701190"
            ),
            "0001",
            "291900",
            DadosDaContaResponse.Titular(
                UUID.fromString(cadastrarChavePixRequest.idClienteBancario),
                "Rafael M C Ponte",
                "02467781054"
            )
        )
}