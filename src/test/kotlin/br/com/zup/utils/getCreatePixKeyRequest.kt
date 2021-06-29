package br.com.zup.utils

import br.com.zup.CadastrarChavePixRequest
import br.com.zup.cadastro.utils.convertToAccountTypeBCB
import br.com.zup.cadastro.utils.convertToKeyTypeBCB
import br.com.zup.external.bcb.cadastrar.CreatePixKeyRequest
import br.com.zup.external.bcb.cadastrar.Type
import br.com.zup.external.itau.DadosDaContaResponse

fun getCreatePixKeyRequest(dadosDaContaResponse: DadosDaContaResponse,
                           cadastrarChavePixRequest: CadastrarChavePixRequest
    ): CreatePixKeyRequest {
        val bankAccount = CreatePixKeyRequest.BankAccount(
            dadosDaContaResponse.instituicao?.ispb,
            dadosDaContaResponse.agencia,
            dadosDaContaResponse.numero,
            cadastrarChavePixRequest.tipoContaBancaria.convertToAccountTypeBCB())

        val owner = CreatePixKeyRequest.Owner(
            Type.NATURAL_PERSON,
            dadosDaContaResponse.titular?.nome,
            dadosDaContaResponse.titular?.cpf)

        return CreatePixKeyRequest(
            cadastrarChavePixRequest.tipoChavePix.convertToKeyTypeBCB(),
            cadastrarChavePixRequest.valorChavePix,
            bankAccount,
            owner)
    }