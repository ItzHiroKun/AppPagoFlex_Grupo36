package com.example.apppagoflex_grupo36.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "empresas")
data class EmpresaClienteEntity(
    @PrimaryKey(autoGenerate = true)
    val idEmpresa: Long = 0,
    val rutEmpresa: String,
    val razonSocial: String
)
