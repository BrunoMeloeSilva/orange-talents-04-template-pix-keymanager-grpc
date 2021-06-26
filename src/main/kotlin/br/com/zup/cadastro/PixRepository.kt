package br.com.zup.cadastro

import io.micronaut.data.annotation.Repository
import io.micronaut.data.jpa.repository.JpaRepository
import java.util.*

@Repository
interface PixRepository : JpaRepository<PixModel, Long> {

    fun findByPixIdAndIdClienteBancario(pixId: String, idClienteBancario: String): Optional<PixModel>
}