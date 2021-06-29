package br.com.zup.shared

import br.com.zup.shared.exception.RegrasNegociosException
import br.com.zup.shared.exception.ValorJaExisteException
import br.com.zup.shared.exception.ValorNaoExisteException
import io.grpc.Status
import io.grpc.stub.StreamObserver
import io.micronaut.aop.InterceptorBean
import io.micronaut.aop.MethodInterceptor
import io.micronaut.aop.MethodInvocationContext
import javax.inject.Singleton
import javax.validation.ConstraintViolationException

@Singleton
@InterceptorBean(ErrorHandler::class)
class ErrorInterceptor : MethodInterceptor<Any, Any> {
    override fun intercept(context: MethodInvocationContext<Any, Any>): Any {
        return try {
            context.proceed()
        } catch (error: Exception) {
            val status = when(error) {
                is ConstraintViolationException -> Status.INVALID_ARGUMENT
                    .withCause(error)
                    .withDescription(error.message)
                    .asRuntimeException()

                is ValorJaExisteException -> Status.ALREADY_EXISTS
                    .withCause(error)
                    .withDescription(error.message)
                    .asRuntimeException()

                is ValorNaoExisteException -> Status.NOT_FOUND
                    .withCause(error)
                    .withDescription(error.message)
                    .asRuntimeException()

                is RegrasNegociosException -> Status.DATA_LOSS
                    .withCause(error)
                    .withDescription(error.message)
                    .asRuntimeException()

                else -> Status.UNKNOWN
                    .withCause(error)
                    .withDescription("Erro inesperado. É provavel que algum serviço terceiro que se comunica conosco, esteja fora do ar.")
                    .asRuntimeException()
            }
            val responseObserver = context.parameterValues[1] as StreamObserver<*>
            responseObserver.onError(status)
        }
    }
}