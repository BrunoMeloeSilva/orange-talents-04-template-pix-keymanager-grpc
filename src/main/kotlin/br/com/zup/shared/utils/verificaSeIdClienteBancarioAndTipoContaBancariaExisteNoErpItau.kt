package br.com.zup.shared.utils

import br.com.zup.external.itau.DadosDaContaResponse
import br.com.zup.external.itau.ErpItauExternalRequest
import br.com.zup.shared.exception.ValorNaoExisteException

fun verificaSeIdClienteBancarioAndTipoContaBancariaExisteNoErpItau(
    erpItauExternalRequest: ErpItauExternalRequest,
    idClienteBancario: String,
    tipoContaBancaria: String
): DadosDaContaResponse {
    val responseTipoContaCliente =
            erpItauExternalRequest.consultaTipoContaCliente(idClienteBancario, tipoContaBancaria)
        if (responseTipoContaCliente.code() == 404)
            throw ValorNaoExisteException("O campo [idClienteBancario] e/ou o [tipoContaBancaria] informado(s) não foram encontrados no sistema do Itaú.")

    return responseTipoContaCliente.body()!!
}