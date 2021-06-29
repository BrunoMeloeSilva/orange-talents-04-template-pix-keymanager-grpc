package br.com.zup.utils

import br.com.zup.CadastrarChavePixRequest
import br.com.zup.cadastro.utils.convertToKeyTypeBCB
import br.com.zup.external.bcb.cadastrar.CreatePixKeyResponse

fun getCreatePixKeyResponse(cadastrarChavePixRequest: CadastrarChavePixRequest): CreatePixKeyResponse {
        return CreatePixKeyResponse(
            cadastrarChavePixRequest.tipoChavePix.convertToKeyTypeBCB(),
            cadastrarChavePixRequest.valorChavePix,
        null, null, null)
    }