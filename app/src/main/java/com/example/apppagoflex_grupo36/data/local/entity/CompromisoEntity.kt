package com.example.apppagoflex_grupo36.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "compromisos",
    foreignKeys = [
        ForeignKey(
            entity = UsuarioEntity::class,
            parentColumns = ["idUsuario"],
            childColumns = ["idUsuario"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = EmpresaClienteEntity::class,
            parentColumns = ["idEmpresa"],
            childColumns = ["idEmpresa"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("idUsuario"), Index("idEmpresa")]
)
data class CompromisoEntity(
    @PrimaryKey(autoGenerate = true)
    val idCompromiso: Long = 0,
    val idUsuario: Long,
    val idEmpresa: Long,
    val monto: Double,
    val fechaVencimiento: String,
    val estado: String
) {
    companion object {
        const val ESTADO_PENDIENTE = "Pendiente"
        const val ESTADO_VENCIDO = "Vencido"
        const val ESTADO_PAGADO = "Pagado"
        const val ESTADO_ANULADO = "Anulado"
    }
}
