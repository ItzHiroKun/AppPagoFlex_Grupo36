package com.example.apppagoflex_grupo36.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.example.apppagoflex_grupo36.data.local.dao.PagoFlexDao
import com.example.apppagoflex_grupo36.ui.screen.HomeAgenteScreen
import com.example.apppagoflex_grupo36.ui.screen.HomeEjecutivoScreen
import com.example.apppagoflex_grupo36.ui.screen.HomeUsuarioScreen
import com.example.apppagoflex_grupo36.ui.screen.PerfilScreen
import com.example.apppagoflex_grupo36.ui.screen.RoleSelectorScreen
import com.example.apppagoflex_grupo36.viewmodel.SessionViewModel
import com.example.apppagoflex_grupo36.viewmodel.SessionViewModelFactory

@Composable
fun AppNavigation(
    dao: PagoFlexDao,
    sessionViewModel: SessionViewModel = viewModel(factory = SessionViewModelFactory(dao))
) {
    val backStack = rememberNavBackStack(Screen.RoleSelector)

    NavDisplay(
        backStack = backStack,
        onBack = {
            if (backStack.size > 1) {
                backStack.removeAt(backStack.lastIndex)
            }
        }
    ) { key ->
        when (key) {
            is Screen.RoleSelector -> NavEntry(key) {
                RoleSelectorScreen(
                    sessionViewModel = sessionViewModel,
                    onNavigateToHome = { targetScreen ->
                        backStack.add(targetScreen)
                    }
                )
            }
            is Screen.HomeUsuario -> NavEntry(key) {
                HomeUsuarioScreen(
                    dao = dao,
                    sessionViewModel = sessionViewModel,
                    onLogout = {
                        sessionViewModel.logout()
                        backStack.clear()
                        backStack.add(Screen.RoleSelector)
                    },
                    onNavigateToPerfil = {
                        backStack.add(Screen.Perfil)
                    }
                )
            }
            is Screen.HomeAgente -> NavEntry(key) {
                HomeAgenteScreen(
                    dao = dao,
                    sessionViewModel = sessionViewModel,
                    onLogout = {
                        sessionViewModel.logout()
                        backStack.clear()
                        backStack.add(Screen.RoleSelector)
                    }
                )
            }
            is Screen.HomeEjecutivo -> NavEntry(key) {
                HomeEjecutivoScreen(
                    dao = dao,
                    sessionViewModel = sessionViewModel,
                    onLogout = {
                        sessionViewModel.logout()
                        backStack.clear()
                        backStack.add(Screen.RoleSelector)
                    }
                )
            }
            is Screen.Perfil -> NavEntry(key) {
                PerfilScreen(
                    sessionViewModel = sessionViewModel,
                    onVolver = {
                        if (backStack.size > 1) {
                            backStack.removeAt(backStack.lastIndex)
                        }
                    }
                )
            }
            else -> error("Unknown screen key: $key")
        }
    }
}