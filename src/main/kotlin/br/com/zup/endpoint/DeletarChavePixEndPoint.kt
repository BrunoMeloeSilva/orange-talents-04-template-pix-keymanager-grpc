package br.com.zup.endpoint

import br.com.zup.DeletarChavePixGrpc
import br.com.zup.DeletarChavePixRequest
import br.com.zup.DeletarChavePixResponse
import br.com.zup.cadastro.PixModel
import br.com.zup.cadastro.PixRepository
import br.com.zup.delete.DeletaPixDtoIn
import br.com.zup.external.bcb.BcbExternalRequest
import br.com.zup.external.bcb.deletar.DeletePixKeyRequest
import br.com.zup.shared.ErrorHandler
import br.com.zup.shared.exception.RegrasNegociosException
import br.com.zup.shared.exception.ValorNaoExisteException
import br.com.zup.shared.utils.validacoesDeEntradaDosDados
import io.grpc.stub.StreamObserver
import io.micronaut.context.annotation.Value
import io.micronaut.http.HttpStatus
import io.micronaut.validation.validator.Validator
import java.util.*
import javax.inject.Singleton
import javax.validation.ConstraintViolationException

@Singleton
@ErrorHandler
class DeletarChavePixEndPoint(
    private val validator: Validator,
    private val pixRepository: PixRepository,
    private val bcbExternalRequest: BcbExternalRequest
) : DeletarChavePixGrpc.DeletarChavePixImplBase() {

    @Value("\${itau.ispb}")
    lateinit var itauIspb: String

    override fun deletar(request: DeletarChavePixRequest, responseObserver: StreamObserver<DeletarChavePixResponse>) {

        val deletaPixDtoIn = request.realizaAsValidacoes(validator, pixRepository)

        deletaChavePixDoBcb(deletaPixDtoIn)

        pixRepository.deleteById(deletaPixDtoIn.id)

        responseObserver.onNext(DeletarChavePixResponse.newBuilder().setDeletado(true).build())
        responseObserver.onCompleted()
    }

    private fun deletaChavePixDoBcb(deletaPixDtoIn: DeletaPixDtoIn) {
        val deletePixKeyRequest = DeletePixKeyRequest(deletaPixDtoIn.valorChavePix!!, itauIspb)
        val httpResponseResult = bcbExternalRequest.deletaChavePixBCB(deletePixKeyRequest.key!!, deletePixKeyRequest)

        if (httpResponseResult.status.code != HttpStatus.OK.code
            && httpResponseResult.status.code != HttpStatus.NOT_FOUND.code)
            throw RegrasNegociosException("Não conseguimos deletar a ChavePix do Banco Central Brasileiro.")
    }
}

private fun DeletarChavePixRequest.realizaAsValidacoes(validator: Validator,
                                                      pixRepository: PixRepository): DeletaPixDtoIn {
    val deletaPixDtoIn = DeletaPixDtoIn(this.pixId, this.idClienteBancario, )
    validacoesDeEntradaDosDados(validator, deletaPixDtoIn)

    val pixModel = validaSePixIdExisteParaIdClienteBancario(pixRepository, deletaPixDtoIn)

    // Retorna o Dto valido, com dados complementares preenchidos.
    deletaPixDtoIn.id = pixModel.get().id
    deletaPixDtoIn.valorChavePix = pixModel.get().valorChavePix
    return deletaPixDtoIn
}

private fun validaSePixIdExisteParaIdClienteBancario(
    pixRepository: PixRepository,
    deletaPixDtoIn: DeletaPixDtoIn,
): Optional<PixModel> {
    val pixModel =
        pixRepository.findByPixIdAndIdClienteBancario(deletaPixDtoIn.pixId!!, deletaPixDtoIn.idClienteBancario!!)
    if (pixModel.isEmpty)
        throw ValorNaoExisteException("Não foi encontrado o [pixId] para o [idClienteBancario] informado.")
    return pixModel
}
