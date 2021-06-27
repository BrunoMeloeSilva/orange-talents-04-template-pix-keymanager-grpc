package br.com.zup.external.bcb

import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.PathVariable
import io.micronaut.http.annotation.Post
import io.micronaut.http.client.annotation.Client


@Client("\${bcb.url}")
interface BcbExternalRequest {
    @Get(value = "/api/v1/pix/keys/{valorChavePix}",
        consumes = [MediaType.APPLICATION_XML])
    fun consultaChavePixBCB(@PathVariable valorChavePix: String): HttpResponse<PixKeyDetailsResponse>

    @Post(value = "/api/v1/pix/keys",
        produces = [MediaType.APPLICATION_XML],
        consumes = [MediaType.APPLICATION_XML])
    fun cadastraChavePixBCB(@Body createPixKeyRequest: CreatePixKeyRequest): HttpResponse<CreatePixKeyResponse>
}