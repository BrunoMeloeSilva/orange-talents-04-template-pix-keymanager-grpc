package br.com.zup.endpoint

import br.com.zup.CadastrarChavePixGrpc
import br.com.zup.CadastrarChavePixRequest
import br.com.zup.CadastrarChavePixResponse
import br.com.zup.cadastro.PixDto
import br.com.zup.cadastro.PixRepository
import br.com.zup.external.itau.DadosDaContaResponse
import br.com.zup.external.itau.ErpItauExternalRequest
import br.com.zup.shared.ErrorHandler
import br.com.zup.shared.exception.ValorNaoExisteException
import io.grpc.stub.StreamObserver
import io.micronaut.http.HttpResponse
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.validation.validator.Validator
import javax.inject.Singleton
import javax.validation.ConstraintViolationException

@Singleton
@ErrorHandler
class CadastrarChavePixEndPoint(
    private val validator: Validator,
    private val pixRepository: PixRepository,
    private val erpItauExternalRequest: ErpItauExternalRequest
) : CadastrarChavePixGrpc.CadastrarChavePixImplBase() {

    override fun cadastrar( request: CadastrarChavePixRequest,
                            responseObserver: StreamObserver<CadastrarChavePixResponse>) {
        // Valida a entrada
        val pixDto = request.validaDadosEntrada(validator, erpItauExternalRequest)
        // Salva ChavePix no DB
        val pixModel = pixRepository.save(pixDto.toPixModel())
        // Resposta de sucesso ao usuário
        responseObserver.onNext(CadastrarChavePixResponse.newBuilder().setPixId(pixModel.pixId).build())
        responseObserver.onCompleted()
    }
}

private fun CadastrarChavePixRequest
        .validaDadosEntrada(validator: Validator,
                            erpItauExternalRequest: ErpItauExternalRequest): PixDto {
    val pixDtoEntrada = PixDto(
        this.idClienteBancario,
        this.tipoChavePix,
        this.valorChavePix,
        this.tipoContaBancaria
    )
    val errosValidacao = validator.validate(pixDtoEntrada)
    if(errosValidacao.isNotEmpty())
        throw ConstraintViolationException(errosValidacao)

    validaDadosJuntoAoErpItau(erpItauExternalRequest)

    // todo: deveria verificar se o cpf informado é o mesmo do proprietario da conta bancaria.

    return pixDtoEntrada
}

private fun CadastrarChavePixRequest.validaDadosJuntoAoErpItau(erpItauExternalRequest: ErpItauExternalRequest): DadosDaContaResponse {
    val responseTipoContaCliente =
            erpItauExternalRequest.consultaTipoContaCliente(this.idClienteBancario,
                                                            this.tipoContaBancaria.name)
        if (responseTipoContaCliente.code() == 404)
            throw ValorNaoExisteException("O campo [idClienteBancario] e/ou o [tipoContaBancaria] informado(s) não foram encontrados no sistema do Itaú.")

    return responseTipoContaCliente.body()!!
}
