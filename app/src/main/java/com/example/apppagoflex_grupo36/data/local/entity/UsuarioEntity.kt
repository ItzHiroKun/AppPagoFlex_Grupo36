package com.example.apppagoflex_grupo36.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "usuarios",
    foreignKeys = [
        ForeignKey(
            entity = RolEntity::class,
            parentColumns = ["idRol"],
            childColumns = ["idRol"],
            onDelete = ForeignKey.RESTRICT
        ),
        ForeignKey(
            entity = EmpresaClienteEntity::class,
            parentColumns = ["idEmpresa"],
            childColumns = ["idEmpresa"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("idRol"), Index("idEmpresa")]
)
data class UsuarioEntity(
    @PrimaryKey(autoGenerate = true)
    val idUsuario: Long = 0,
    val rutUsuario: String,
    val nombreUsuario: String,
    val idRol: Int,
    val idEmpresa: Long? = null,
    val correo: String
)
