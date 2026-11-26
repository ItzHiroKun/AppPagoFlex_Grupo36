package com.example.apppagoflex_grupo36.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "roles")
data class RolEntity(
    @PrimaryKey(autoGenerate = true)
    val idRol: Int = 0,
    val nombreRol: String
)
