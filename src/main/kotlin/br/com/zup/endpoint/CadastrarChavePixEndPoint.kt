package br.com.zup.endpoint

import br.com.zup.CadastrarChavePixGrpc
import br.com.zup.CadastrarChavePixRequest
import br.com.zup.CadastrarChavePixResponse
import io.grpc.stub.StreamObserver
import javax.inject.Singleton

@Singleton
class CadastrarChavePixEndPoint : CadastrarChavePixGrpc.CadastrarChavePixImplBase() {

    override fun cadastrar(
        request: CadastrarChavePixRequest?,
        responseObserver: StreamObserver<CadastrarChavePixResponse>?
    ) {
        // todo: Implementar ...
    }
}