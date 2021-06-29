package br.com.zup.endpoint

import br.com.zup.ConsultaChavePixRequest
import br.com.zup.ConsultarChavePixGrpc
import br.com.zup.ConsultaPixIdAndIdClienteBancarioRequest
import br.com.zup.ConsultarChavePixResponse
import br.com.zup.cadastro.PixModel
import br.com.zup.cadastro.PixRepository
import br.com.zup.consulta.PixIdAndIdClienteBancarioRequest
import br.com.zup.consulta.ValorChavePixRequest
import br.com.zup.external.bcb.BcbExternalRequest
import br.com.zup.external.bcb.consultar.PixKeyDetailsResponse
import br.com.zup.shared.ErrorHandler
import br.com.zup.shared.exception.ValorNaoExisteException
import io.grpc.stub.StreamObserver
import io.micronaut.validation.validator.Validator
import br.com.zup.shared.utils.validacoesDeEntradaDosDados
import com.google.protobuf.Timestamp
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*
import javax.inject.Singleton

@Singleton
@ErrorHandler
class ConsultarChavePixEndPoint(
    private val validator: Validator,
    private val pixRepository: PixRepository,
    private val bcbExternalRequest: BcbExternalRequest
) : ConsultarChavePixGrpc.ConsultarChavePixImplBase() {

    override fun consultaPorPixIdAndIdClienteBancario(request: ConsultaPixIdAndIdClienteBancarioRequest,
                           responseObserver: StreamObserver<ConsultarChavePixResponse>) {

        val consultarChavePixResponse = request.realizaAsValidacoes(validator)

        responseObserver.onNext(consultarChavePixResponse)
        responseObserver.onCompleted()
    }

    override fun consultaPorChavePix(request: ConsultaChavePixRequest,
                                     responseObserver: StreamObserver<ConsultarChavePixResponse>) {

        val consultarChavePixResponse = request.realizaAsValidacoes(validator)

        responseObserver.onNext(consultarChavePixResponse)
        responseObserver.onCompleted()
    }

    private fun ConsultaPixIdAndIdClienteBancarioRequest.realizaAsValidacoes(validator: Validator): ConsultarChavePixResponse? {

        val dtoIn = PixIdAndIdClienteBancarioRequest(this.pixId, this.idClienteBancario)

        validacoesDeEntradaDosDados(validator, dtoIn)

        val optionalPixModel = verificaSePixIdExisteParaIdClienteBancario()

        val consultaChavePixBCB = verificaSeChavePixExisteNoBCB(optionalPixModel)

        return montaObjetoConsultarChavePixResponse(consultaChavePixBCB, optionalPixModel.get())
    }

    private fun ConsultaChavePixRequest.realizaAsValidacoes(validator: Validator): ConsultarChavePixResponse? {
        val valorChavePixRequest = ValorChavePixRequest(this.valorChavePix)
        validacoesDeEntradaDosDados(validator, valorChavePixRequest)

        val optionalPixModel = verificaSeChavePixExisteNoDB()

        val pixKeyDetailsResponse = verificaSeChavePixExisteNoBCB()

        return montaObjetoConsultarChavePixResponse(pixKeyDetailsResponse, optionalPixModel.get())
    }

    private fun ConsultaChavePixRequest.verificaSeChavePixExisteNoBCB(): HttpResponse<PixKeyDetailsResponse> {
        val pixKeyDetailsResponse = bcbExternalRequest.consultaChavePixBCB(this.valorChavePix)
        if (pixKeyDetailsResponse.status.code == HttpStatus.NOT_FOUND.code)
            throw ValorNaoExisteException("O [valorChavePix] informado não existe no BCB.")
        return pixKeyDetailsResponse
    }

    private fun ConsultaChavePixRequest.verificaSeChavePixExisteNoDB(): Optional<PixModel> {
        val optionalPixModel = pixRepository.findByValorChavePix(this.valorChavePix)
        if (optionalPixModel.isEmpty)
            throw ValorNaoExisteException("O [valorChavePix] informado não existe.")
        return optionalPixModel
    }

    private fun montaObjetoConsultarChavePixResponse(
        consultaChavePixBCB: HttpResponse<PixKeyDetailsResponse>,
        pixModel: PixModel
    ): ConsultarChavePixResponse? {
        val titular = ConsultarChavePixResponse
            .Titular
            .newBuilder()
            .setNome(consultaChavePixBCB.body().owner?.name)
            .setCpf(consultaChavePixBCB.body().owner?.taxIdNumber)
            .build()
        val contaBancaria = ConsultarChavePixResponse
            .ContaBancaria
            .newBuilder()
            .setBanco("ITAU UNIBANCO S.A.")
            .setAgencia(consultaChavePixBCB.body().bankAccount?.branch)
            .setNumero(consultaChavePixBCB.body().bankAccount?.accountNumber)
            .setTipo(consultaChavePixBCB.body().bankAccount?.accountType?.name)

        val createAt = pixModel.dataCriacao.atZone(ZoneId.of("UTC")).toInstant()
        val timestamp = Timestamp.newBuilder().setSeconds(createAt.epochSecond).setNanos(createAt.nano).build()
        return ConsultarChavePixResponse
            .newBuilder()
            .setPixId(pixModel.pixId)
            .setIdClienteBancario(pixModel.idClienteBancario)
            .setTipoChavePix(consultaChavePixBCB.body().keyType?.name)
            .setValorChavePix(consultaChavePixBCB.body().key)
            .setTitular(titular)
            .setContaBancaria(contaBancaria)
            .setCriadoEm(timestamp)
            .build()
    }

    private fun verificaSeChavePixExisteNoBCB(optionalPixModel: Optional<PixModel>): HttpResponse<PixKeyDetailsResponse> {
        val consultaChavePixBCB = bcbExternalRequest.consultaChavePixBCB(optionalPixModel.get().valorChavePix!!)
        if (consultaChavePixBCB.status.code == 404)
            throw ValorNaoExisteException("O [pixId] e o [idClienteBancario] informados, são válidos, mas o valor da chave pix não está registrado no BCB.")
        return consultaChavePixBCB
    }

    private fun ConsultaPixIdAndIdClienteBancarioRequest.verificaSePixIdExisteParaIdClienteBancario(): Optional<PixModel> {
        val optionalPixModel =
            pixRepository.findByPixIdAndIdClienteBancario(this.pixId, this.idClienteBancario)
        if (optionalPixModel.isEmpty)
            throw ValorNaoExisteException("O [pixId] informado não existe para o [idClienteBancario].")
        return optionalPixModel
    }
}
