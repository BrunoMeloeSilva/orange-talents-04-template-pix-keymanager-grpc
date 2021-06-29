package br.com.zup.endpoint

import br.com.zup.*
import br.com.zup.cadastro.PixDto
import br.com.zup.cadastro.PixRepository
import br.com.zup.cadastro.utils.convertToAccountTypeBCB
import br.com.zup.cadastro.utils.convertToKeyTypeBCB
import br.com.zup.external.bcb.*
import br.com.zup.external.bcb.cadastrar.*
import br.com.zup.external.itau.DadosDaContaResponse
import br.com.zup.external.itau.ErpItauExternalRequest
import br.com.zup.shared.ErrorHandler
import br.com.zup.shared.exception.ValorJaExisteException
import br.com.zup.shared.exception.ValorNaoExisteException
import br.com.zup.shared.utils.validacoesDeEntradaDosDados
import br.com.zup.shared.utils.verificaSeIdClienteBancarioAndTipoContaBancariaExisteNoErpItau
import io.grpc.stub.StreamObserver
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.validation.validator.Validator
import javax.inject.Singleton
import javax.validation.ConstraintViolationException

@Singleton
@ErrorHandler
class CadastrarChavePixEndPoint(
    private val validator: Validator,
    private val pixRepository: PixRepository,
    private val erpItauExternalRequest: ErpItauExternalRequest,
    private val bcbExternalRequest: BcbExternalRequest
) : CadastrarChavePixGrpc.CadastrarChavePixImplBase() {

    override fun cadastrar( request: CadastrarChavePixRequest,
                            responseObserver: StreamObserver<CadastrarChavePixResponse>) {
        // Valida a entrada
        val pixDto = request.realizaAsValidacoes(validator, erpItauExternalRequest, bcbExternalRequest)

        // Salva ChavePix no DB
        val pixModel = pixRepository.save(pixDto.toPixModel())

        // Resposta de sucesso ao usuário
        responseObserver.onNext(CadastrarChavePixResponse.newBuilder().setPixId(pixModel.pixId).build())
        responseObserver.onCompleted()
    }
}

private fun CadastrarChavePixRequest
        .realizaAsValidacoes(validator: Validator,
                             erpItauExternalRequest: ErpItauExternalRequest,
                             bcbExternalRequest: BcbExternalRequest): PixDto {

    var pixDtoEntrada = PixDto(this.idClienteBancario, this.tipoChavePix, this.valorChavePix, this.tipoContaBancaria)

    validacoesDeEntradaDosDados(validator, pixDtoEntrada)

    val dadosDaContaResponse = verificaSeIdClienteBancarioAndTipoContaBancariaExisteNoErpItau(
        erpItauExternalRequest,
        this.idClienteBancario,
        this.tipoContaBancaria.name
    )

    seChavePixNaoExistirNoBcbRegistra(dadosDaContaResponse, bcbExternalRequest, pixDtoEntrada)

    // todo: poderia verificar se o cpf informado é o mesmo do proprietario da conta bancaria.
    return pixDtoEntrada
}

private fun CadastrarChavePixRequest.seChavePixNaoExistirNoBcbRegistra(
    dadosDaContaResponse: DadosDaContaResponse,
    bcbExternalRequest: BcbExternalRequest,
    pixDtoEntrada: PixDto,
) {
    val createPixKeyRequest = dadosDaContaResponse.getCreatePixKeyRequest(this, dadosDaContaResponse)
    val HttpResponseResult: HttpResponse<CreatePixKeyResponse>
    try {
        HttpResponseResult = bcbExternalRequest.cadastraChavePixBCB(createPixKeyRequest!!)
        if (this.tipoChavePix == TipoChavePix.ALEATORIA) {
            if (HttpResponseResult.status.code == HttpStatus.CREATED.code) {
                pixDtoEntrada.valorChavePix = HttpResponseResult.body().key!!
            }
        }
    } catch (e: HttpClientResponseException) {
        if (e.status.code == HttpStatus.UNPROCESSABLE_ENTITY.code)
            throw ValorJaExisteException("A Chave Pix informada, já existe no Banco Central do Brasil.")
    }
}

private fun DadosDaContaResponse.getCreatePixKeyRequest(
    cadastrarChavePixRequest: CadastrarChavePixRequest,
    dadosDaContaResponse: DadosDaContaResponse
): CreatePixKeyRequest? {
    val keyTypeBCB = cadastrarChavePixRequest.tipoChavePix.convertToKeyTypeBCB()
    val accountTypeBCB = cadastrarChavePixRequest.tipoContaBancaria.convertToAccountTypeBCB()
    val bankAccount = CreatePixKeyRequest.BankAccount(
        dadosDaContaResponse.instituicao?.ispb,
        dadosDaContaResponse.agencia,
        dadosDaContaResponse.numero,
        accountTypeBCB)
    val owner = CreatePixKeyRequest.Owner(
        Type.NATURAL_PERSON,
        dadosDaContaResponse.titular?.nome,
        dadosDaContaResponse.titular?.cpf)

    return CreatePixKeyRequest(keyTypeBCB, cadastrarChavePixRequest.valorChavePix, bankAccount, owner)
}
