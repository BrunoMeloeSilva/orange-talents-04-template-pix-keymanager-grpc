package br.com.zup.endpoint

import br.com.zup.*
import br.com.zup.external.itau.ErpItauExternalRequest
import br.com.zup.utils.getCadastrarChavePixRequest
import br.com.zup.utils.getDadosDaContaResponse
import io.grpc.Channel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Bean
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.http.HttpResponse
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import javax.inject.Inject

@MicronautTest(transactional = false)
internal class DeletarChavePixEndPointTest(
    private val deletarChavePixChannel: DeletarChavePixGrpc.DeletarChavePixBlockingStub,
    private val cadastrarChavePixChannel: CadastrarChavePixGrpc.CadastrarChavePixBlockingStub
){
    @Inject
    lateinit var erpItauExternalRequestMockado: ErpItauExternalRequest

    @Test
    fun `Nao deve deletar com UUID invalido do pixId`(){
        val deletarChavePixRequest = DeletarChavePixRequest
                                        .newBuilder()
                                        .setPixId("2-bc4504e-d342-4ecc-8efb-32b66bc7a173")
                                        .setIdClienteBancario("2bc4504e-d342-4ecc-8efb-32b66bc7a173")
                                        .build()
        val assertThrows = assertThrows<StatusRuntimeException>() {
            deletarChavePixChannel.deletar(deletarChavePixRequest)
        }
        with(assertThrows) {
            assertEquals(Status.INVALID_ARGUMENT.code, assertThrows.status.code)
            assertEquals("pixId: Deve conter um UUID válido.", this.status.description)
        }
    }

    @Test
    fun `Nao deve deletar com UUID invalido do idClienteBancario`(){
        val deletarChavePixRequest = DeletarChavePixRequest
            .newBuilder()
            .setPixId("2bc4504e-d342-4ecc-8efb-32b66bc7a173")
            .setIdClienteBancario("2-bc4504e-d342-4ecc-8efb-32b66bc7a173")
            .build()
        val assertThrows = assertThrows<StatusRuntimeException>() {
            deletarChavePixChannel.deletar(deletarChavePixRequest)
        }
        with(assertThrows) {
            assertEquals(Status.INVALID_ARGUMENT.code, assertThrows.status.code)
            assertEquals("idClienteBancario: Deve conter um UUID válido.", this.status.description)
        }
    }

    @Test
    fun `Nao deve deletar ChavePix para pixId nao pertencente ao idClienteBancario`(){
        val deletarChavePixRequest = DeletarChavePixRequest
            .newBuilder()
            .setPixId("2bc4504e-d342-4ecc-8efb-32b66bc7a173")
            .setIdClienteBancario("2bc4504e-d342-4ecc-8efb-32b66bc7a173")
            .build()
        val assertThrows = assertThrows<StatusRuntimeException>() {
            deletarChavePixChannel.deletar(deletarChavePixRequest)
        }
        with(assertThrows) {
            assertEquals(Status.NOT_FOUND.code, assertThrows.status.code)
            assertEquals("Não foi encontrado o [pixId] para o [idClienteBancario] informado.", this.status.description)
        }
    }

    @Test
    fun `Deve deletar ChavePix com pixId e idClienteBancario validos`(){
        // Cadastrar
        val cadastrarChavePixRequest = getCadastrarChavePixRequest(TipoChavePix.ALEATORIA, "")
        val dadosDaContaResponse = getDadosDaContaResponse(cadastrarChavePixRequest)

        `when`(erpItauExternalRequestMockado.consultaTipoContaCliente(
            cadastrarChavePixRequest.idClienteBancario,
            cadastrarChavePixRequest.tipoContaBancaria.name))
            .thenReturn(HttpResponse.ok(dadosDaContaResponse))

        val cadastrarChavePixResponse = cadastrarChavePixChannel.cadastrar(cadastrarChavePixRequest)

        // Deletar
        val deletarChavePixRequest = DeletarChavePixRequest
            .newBuilder()
            .setPixId(cadastrarChavePixResponse.pixId)
            .setIdClienteBancario(cadastrarChavePixRequest.idClienteBancario)
            .build()
        val deletarChavePixResponse = deletarChavePixChannel.deletar(deletarChavePixRequest)

        // Validacao
        assertTrue(deletarChavePixResponse.deletado)

    }

    @MockBean(ErpItauExternalRequest::class)
    fun erpItauExternalRequestMock(): ErpItauExternalRequest{
        return Mockito.mock(ErpItauExternalRequest::class.java)
    }

    @Factory
    class DeletarChavePixFactory{
        @Bean
        fun DeletarChavePixChannel(@GrpcChannel(GrpcServerChannel.NAME) channel: Channel)
                : DeletarChavePixGrpc.DeletarChavePixBlockingStub{
            return DeletarChavePixGrpc.newBlockingStub(channel)
        }
    }
}