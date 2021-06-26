package br.com.zup.cadastro

import br.com.zup.TipoChavePix
import br.com.zup.TipoContaBancaria
import java.time.LocalDateTime
import javax.persistence.*

@Entity
@Table(name = "CHAVEPIX")
class PixModel(
    @Column(nullable = false)
    val idClienteBancario: String?,

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    val tipoChavePix: TipoChavePix?,

    @Column(length = 77, unique = true)
    var valorChavePix: String?,

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    val tipoContaBancaria: TipoContaBancaria?
){
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null

    @Column(nullable = false, unique = true)
    val pixId: String? = java.util.UUID.randomUUID().toString()

    @Column(nullable = false)
    val dataCriacao: LocalDateTime = LocalDateTime.now()

    init {
        if (TipoChavePix.ALEATORIA == tipoChavePix)
            valorChavePix = java.util.UUID.randomUUID().toString()
    }
}
