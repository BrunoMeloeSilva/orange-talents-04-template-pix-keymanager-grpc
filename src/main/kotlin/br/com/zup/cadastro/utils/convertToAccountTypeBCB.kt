package br.com.zup.cadastro.utils

import br.com.zup.TipoContaBancaria
import br.com.zup.external.bcb.cadastrar.AccountType

fun TipoContaBancaria.convertToAccountTypeBCB(): AccountType? {
    return when(this){
        TipoContaBancaria.CONTA_CORRENTE -> AccountType.CACC
        TipoContaBancaria.CONTA_POUPANCA -> AccountType.SVGS
        else -> null
    }
}