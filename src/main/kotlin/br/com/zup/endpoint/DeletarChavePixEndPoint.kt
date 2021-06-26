package br.com.zup.endpoint

import br.com.zup.DeletarChavePixGrpc
import br.com.zup.DeletarChavePixRequest
import br.com.zup.DeletarChavePixResponse
import br.com.zup.cadastro.PixRepository
import br.com.zup.delete.DeletaPixDtoIn
import br.com.zup.shared.ErrorHandler
import br.com.zup.shared.exception.ValorNaoExisteException
import io.grpc.stub.StreamObserver
import io.micronaut.validation.validator.Validator
import javax.inject.Singleton
import javax.validation.ConstraintViolationException

@Singleton
@ErrorHandler
class DeletarChavePixEndPoint(
    private val validator: Validator,
    private val pixRepository: PixRepository
) : DeletarChavePixGrpc.DeletarChavePixImplBase() {
    override fun deletar(request: DeletarChavePixRequest, responseObserver: StreamObserver<DeletarChavePixResponse>) {
        // Valida dados de entrada
        val deletaPixDtoIn = request.validaDadosEntrada(validator, pixRepository)

        // Deleta a ChavePix solicitada do banco de dados
        pixRepository.deleteById(deletaPixDtoIn.id)

        // Envia resposta positiva ao solicitante
        responseObserver.onNext(DeletarChavePixResponse.newBuilder().setDeletado(true).build())
        responseObserver.onCompleted()
    }
}

private fun DeletarChavePixRequest.validaDadosEntrada(validator: Validator,
                                                      pixRepository: PixRepository): DeletaPixDtoIn {
    // Valida se são UUID válidos
    val deletaPixDtoIn = DeletaPixDtoIn(this.pixId, this.idClienteBancario)
    val errosValidacao = validator.validate(deletaPixDtoIn)
    if(errosValidacao.isNotEmpty())
        throw ConstraintViolationException(errosValidacao)
    // Valida se pixId existe para o idClienteBancario
    val pixModel =
        pixRepository.findByPixIdAndIdClienteBancario(deletaPixDtoIn.pixId!!, deletaPixDtoIn.idClienteBancario!!)
    if(pixModel.isEmpty)
        throw ValorNaoExisteException("Não foi encontrado o [pixId] para o [idClienteBancario] informado.")
    // Retorna o Dto de entrada validado
    deletaPixDtoIn.id = pixModel.get().id
    return deletaPixDtoIn
}
