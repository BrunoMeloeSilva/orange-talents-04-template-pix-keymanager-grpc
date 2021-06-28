package br.com.zup.external.bcb.cadastrar

data class CreatePixKeyRequest(
    val keyType: KeyType?,
    val key: String?,
    val bankAccount: BankAccount?,
    val owner: Owner?,
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