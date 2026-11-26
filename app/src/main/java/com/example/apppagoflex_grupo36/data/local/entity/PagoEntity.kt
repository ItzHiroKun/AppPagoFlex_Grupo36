package com.example.apppagoflex_grupo36.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "pagos",
    foreignKeys = [
        ForeignKey(
            entity = CompromisoEntity::class,
            parentColumns = ["idCompromiso"],
            childColumns = ["idCompromiso"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("idCompromiso")]
)
data class PagoEntity(
    @PrimaryKey(autoGenerate = true)
    val idPago: Long = 0,
    val idCompromiso: Long,
    val fechaPago: String,
    val montoPagado: Double,
    val comprobante: String
)
