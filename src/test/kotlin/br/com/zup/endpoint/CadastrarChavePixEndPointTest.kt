package br.com.zup.endpoint

import br.com.zup.CadastrarChavePixGrpc
import br.com.zup.CadastrarChavePixRequest
import br.com.zup.TipoChavePix
import br.com.zup.TipoContaBancaria
import br.com.zup.cadastro.PixDto
import br.com.zup.cadastro.PixRepository
import br.com.zup.external.itau.DadosDaContaResponse
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
import io.micronaut.validation.validator.Validator
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import java.util.*
import javax.inject.Inject
import javax.validation.ConstraintViolationException

@MicronautTest(transactional = false)
internal class CadastrarChavePixEndPointTest(
    private val cadastrarChavePixChannel: CadastrarChavePixGrpc.CadastrarChavePixBlockingStub,
    private val repository: PixRepository,
) {
    @Inject
    lateinit var erpItauExternalRequestMockado: ErpItauExternalRequest

    @BeforeEach
    fun AntesDeCadaTeste(){
        repository.deleteAll()
    }

    @Test
    fun `Deve cadastrar ChavePix com valor valido para o tipo CPF`(){
        `Deve cadastrar ChavePix com valor valido`(TipoChavePix.CPF, "69226614008")
    }

    @Test
    fun `Nao deve cadastrar ChavePix com valor invalido para o tipo CPF`() {
        `Nao deve cadastrar ChavePix com valor invalido`(
            TipoChavePix.CPF,
            "11122233344",
            ": Para o campo [tipoChavePix], CPF = 1, você deve informar um CPF válido e sem os pontos ou traços."
        )
    }

    @Test
    fun `Deve cadastrar ChavePix com valor valido para o tipo EMAIL`(){
        `Deve cadastrar ChavePix com valor valido`(TipoChavePix.EMAIL, "email@email.com")
    }

    @Test
    fun `Nao deve cadastrar ChavePix com valor invalido para o tipo EMAIL`() {
        `Nao deve cadastrar ChavePix com valor invalido`(
            TipoChavePix.EMAIL,
            "email@email.com.",
            ": Para o campo [tipoChavePix], EMAIL = 3, você deve informar um EMAIL válido."
        )
    }

    @Test
    fun `Deve cadastrar ChavePix com valor valido para o tipo CELULAR`(){
        `Deve cadastrar ChavePix com valor valido`(TipoChavePix.CELULAR, "+5592982625437")
    }

    @Test
    fun `Nao deve cadastrar ChavePix com valor invalido para o tipo CELULAR`() {
        `Nao deve cadastrar ChavePix com valor invalido`(
            TipoChavePix.CELULAR,
            "92982415168",
            ": Para o campo [tipoChavePix], CELULAR = 2, você deve informar um CELULAR válido, iniciando com o símbolo de +, e contendo somente números. Exemplo: +5585988714077"
        )
    }

    @Test
    fun `Deve cadastrar ChavePix com valor valido para o tipo ALEATORIA`(){
        `Deve cadastrar ChavePix com valor valido`(TipoChavePix.ALEATORIA, "")
    }

    @Test
    fun `Nao deve cadastrar ChavePix com valor invalido para o tipo ALEATORIA`() {
        `Nao deve cadastrar ChavePix com valor invalido`(
            TipoChavePix.ALEATORIA,
            "xpto",
            ": Para o campo [tipoChavePix], ALEATORIA = 4, você não deve informar um valor."
        )
    }

    @Test
    fun `Nao deve cadastrar ChavePix com valor invalido para o tipo TIPO_CHAVE_PIX_NAO_DEFINIDO`() {
        val cadastrarChavePixRequest = getCadastrarChavePixRequest(TipoChavePix.TIPO_CHAVE_PIX_NAO_DEFINIDO,
            "")

        val assertThrows = assertThrows<StatusRuntimeException>() {
            cadastrarChavePixChannel.cadastrar(cadastrarChavePixRequest)
        }

        with(assertThrows) {
            assertEquals(Status.INVALID_ARGUMENT.code, assertThrows.status.code)
        }
    }

    @Test
    fun `Deve cadastrar ChavePix no Banco de dados`() {
        val pixDto = PixDto(
            "c56dfef4-7901-44fb-84e2-a2cefb157890",
            TipoChavePix.EMAIL,
            "email@email.com",
            TipoContaBancaria.CONTA_CORRENTE
        )
        val pixModel = repository.save(pixDto.toPixModel())

        assertNotNull(pixModel.idClienteBancario)
        assertNotNull(pixModel.tipoChavePix)
        assertNotNull(pixModel.valorChavePix)
        assertNotNull(pixModel.id)
        assertNotNull(pixModel.tipoContaBancaria)
    }

    @Test
    fun `Nao deve cadastrar chavePix quando o ERP Itau retornar 404`(){
        // Cenario
        val cadastrarChavePixRequest = getCadastrarChavePixRequest(TipoChavePix.EMAIL, "email@email.com")
        val dadosDaContaResponse = getDadosDaContaResponse(cadastrarChavePixRequest)
        `when`(erpItauExternalRequestMockado.consultaTipoContaCliente(
            cadastrarChavePixRequest.idClienteBancario,
            cadastrarChavePixRequest.tipoContaBancaria.name))
            .thenReturn(HttpResponse.notFound(dadosDaContaResponse))
        // Acao
        val assertThrows = assertThrows<StatusRuntimeException> {
            cadastrarChavePixChannel.cadastrar(cadastrarChavePixRequest)
        }
        // Validacao
        with(assertThrows){
            assertEquals(Status.NOT_FOUND.code, assertThrows.status.code)
            assertEquals("O campo [idClienteBancario] e/ou o [tipoContaBancaria] informado(s) não foram encontrados no sistema do Itaú.", assertThrows.status.description)
        }

    }

    @Test
    fun `Nao deve cadastrar chavePix se valorChavePix ja existir`(){
        // Cenario
        `Deve cadastrar ChavePix com valor valido para o tipo EMAIL`()

        val cadastrarChavePixRequest = getCadastrarChavePixRequest(TipoChavePix.EMAIL, "email@email.com")
        val dadosDaContaResponse = getDadosDaContaResponse(cadastrarChavePixRequest)
        `when`(erpItauExternalRequestMockado.consultaTipoContaCliente(
            cadastrarChavePixRequest.idClienteBancario,
            cadastrarChavePixRequest.tipoContaBancaria.name))
            .thenReturn(HttpResponse.ok(dadosDaContaResponse))
        // Acao
        val assertThrows = assertThrows<StatusRuntimeException> {
            cadastrarChavePixChannel.cadastrar(cadastrarChavePixRequest)
        }
        // Validacao
        with(assertThrows){
            assertEquals(Status.ALREADY_EXISTS.code, assertThrows.status.code)
            assertEquals("O valor informado para o campo [valorChavePix], já foi cadastrado.", assertThrows.status.description)
        }
    }

    @Test
    fun `Valida formato do JSON de resposta do ERP Itau`(){
        val cadastrarChavePixRequest = getCadastrarChavePixRequest(TipoChavePix.EMAIL, "email@email.com")
        val dadosDaContaResponse = getDadosDaContaResponse(cadastrarChavePixRequest)
        assertNotNull(dadosDaContaResponse.tipo)
        assertNotNull(dadosDaContaResponse.instituicao)
        assertNotNull(dadosDaContaResponse.agencia)
        assertNotNull(dadosDaContaResponse.numero)
        assertNotNull(dadosDaContaResponse.titular)
        assertNotNull(dadosDaContaResponse.instituicao!!.nome)
        assertNotNull(dadosDaContaResponse.instituicao!!.ispb)
        assertNotNull(dadosDaContaResponse.titular!!.id)
        assertNotNull(dadosDaContaResponse.titular!!.nome)
        assertNotNull(dadosDaContaResponse.titular!!.cpf)
    }

    @Factory
    class CadastrarChavePixFactory{
        @Bean
        fun CadastrarChavePixChannel(@GrpcChannel(GrpcServerChannel.NAME) channel: Channel):
                CadastrarChavePixGrpc.CadastrarChavePixBlockingStub {
            return CadastrarChavePixGrpc.newBlockingStub(channel)
        }
    }

    @MockBean(ErpItauExternalRequest::class)
    fun erpItauExternalRequestMock(): ErpItauExternalRequest{
        return Mockito.mock(ErpItauExternalRequest::class.java)
    }

    private fun `Nao deve cadastrar ChavePix com valor invalido`(tipoChavePix: TipoChavePix,
                                                                 valorChavePix: String,
                                                                 mensagemErro: String) {

        val cadastrarChavePixRequest = getCadastrarChavePixRequest(tipoChavePix, valorChavePix)

        val assertThrows = assertThrows<StatusRuntimeException>() {
            cadastrarChavePixChannel.cadastrar(cadastrarChavePixRequest)
        }

        with(assertThrows) {
            assertEquals(Status.INVALID_ARGUMENT.code, assertThrows.status.code)
            assertEquals(mensagemErro, this.status.description)
        }
    }

    private fun `Deve cadastrar ChavePix com valor valido`(tipoChavePix: TipoChavePix, valorChavePix: String) {
        // Cenario
        val cadastrarChavePixRequest = getCadastrarChavePixRequest(tipoChavePix, valorChavePix)
        val dadosDaContaResponse = getDadosDaContaResponse(cadastrarChavePixRequest)
        `when`(erpItauExternalRequestMockado.consultaTipoContaCliente(
            cadastrarChavePixRequest.idClienteBancario,
            cadastrarChavePixRequest.tipoContaBancaria.name))
            .thenReturn(HttpResponse.ok(dadosDaContaResponse))
        // Acao
        val cadastrarChavePixResponse = cadastrarChavePixChannel.cadastrar(cadastrarChavePixRequest)
        // Validacao
        assertNotNull(cadastrarChavePixResponse.pixId)
    }
}