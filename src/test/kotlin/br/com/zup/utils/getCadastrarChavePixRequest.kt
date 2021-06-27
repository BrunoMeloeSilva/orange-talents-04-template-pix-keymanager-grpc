package br.com.zup.utils

import br.com.zup.CadastrarChavePixRequest
import br.com.zup.TipoChavePix
import br.com.zup.TipoContaBancaria

fun getCadastrarChavePixRequest(tipoChavePix: TipoChavePix, valorChavePix: String): CadastrarChavePixRequest {
        return CadastrarChavePixRequest.newBuilder()
            .setIdClienteBancario("c56dfef4-7901-44fb-84e2-a2cefb157890")
            .setTipoChavePix(tipoChavePix)
            .setValorChavePix(valorChavePix)
            .setTipoContaBancaria(TipoContaBancaria.CONTA_CORRENTE)
            .build()
}