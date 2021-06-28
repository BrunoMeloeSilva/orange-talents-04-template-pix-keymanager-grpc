package br.com.zup.external.bcb

import br.com.zup.external.bcb.cadastrar.CreatePixKeyRequest
import br.com.zup.external.bcb.cadastrar.CreatePixKeyResponse
import br.com.zup.external.bcb.cadastrar.PixKeyDetailsResponse
import br.com.zup.external.bcb.deletar.DeletePixKeyRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.*
import io.micronaut.http.client.annotation.Client


@Client("\${bcb.url}")
interface BcbExternalRequest {
    @Post(value = "/api/v1/pix/keys",
        produces = [MediaType.APPLICATION_XML],
        consumes = [MediaType.APPLICATION_XML])
    fun cadastraChavePixBCB(@Body createPixKeyRequest: CreatePixKeyRequest): HttpResponse<CreatePixKeyResponse>

    @Delete(value = "/api/v1/pix/keys/{valorChavePix}",
            produces = [MediaType.APPLICATION_XML])
    fun deletaChavePixBCB(@PathVariable valorChavePix: String, @Body deletePixKeyRequest: DeletePixKeyRequest): HttpResponse<String>
}