package br.com.zup.external.itau

import io.micronaut.http.client.annotation.Client
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.PathVariable
import io.micronaut.http.annotation.QueryValue

@Client("\${itau.erp.url}")
interface ErpItauExternalRequest {
    @Get("/api/v1/clientes/{idClienteBancario}/contas?tipo={tipoContaBancaria}")
    fun consultaTipoContaCliente(@PathVariable idClienteBancario: String, @QueryValue tipoContaBancaria: String): HttpResponse<DadosDaContaResponse>
}