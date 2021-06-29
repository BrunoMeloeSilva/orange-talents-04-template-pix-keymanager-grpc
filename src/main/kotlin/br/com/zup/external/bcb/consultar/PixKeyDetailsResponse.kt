package br.com.zup.external.bcb.consultar

import br.com.zup.external.bcb.cadastrar.AccountType
import br.com.zup.external.bcb.cadastrar.KeyType
import br.com.zup.external.bcb.cadastrar.Type
import java.time.LocalDateTime

data class PixKeyDetailsResponse(
    val keyType: KeyType?,
    val key: String?,
    val bankAccount: BankAccount?,
    val owner: Owner?,
    val createdAt: LocalDateTime?,
){
    data class BankAccount(
        val participant: String?,
        val branch: String?,
        val accountNumber: String?,
        val accountType: AccountType?,
    )

    data class Owner(
        val type: Type?,
        val name: String?,
        val taxIdNumber: String?,
    )
}