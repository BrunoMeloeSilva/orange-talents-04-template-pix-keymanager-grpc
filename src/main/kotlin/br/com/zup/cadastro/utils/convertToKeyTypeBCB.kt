package br.com.zup.cadastro.utils

import br.com.zup.TipoChavePix
import br.com.zup.external.bcb.cadastrar.KeyType

fun TipoChavePix.convertToKeyTypeBCB(): KeyType? {
    return when(this){
        TipoChavePix.CPF -> KeyType.CPF
        TipoChavePix.CELULAR -> KeyType.PHONE
        TipoChavePix.EMAIL -> KeyType.EMAIL
        TipoChavePix.ALEATORIA -> KeyType.RANDOM
        else -> null
    }
}