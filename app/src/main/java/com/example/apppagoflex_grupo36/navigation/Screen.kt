package com.example.apppagoflex_grupo36.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

sealed interface Screen : NavKey {

    @Serializable
    data object RoleSelector : Screen

    @Serializable
    data object HomeUsuario : Screen

    @Serializable
    data object HomeAgente : Screen

    @Serializable
    data object HomeEjecutivo : Screen

    @Serializable
    data object Perfil : Screen
}
