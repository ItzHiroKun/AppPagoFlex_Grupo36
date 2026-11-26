package com.example.apppagoflex_grupo36.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Login
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.Badge
import androidx.compose.material.icons.rounded.Business
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Error
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.SupportAgent
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.apppagoflex_grupo36.data.local.DatabaseSeeder
import com.example.apppagoflex_grupo36.data.local.entity.RolEntity
import com.example.apppagoflex_grupo36.data.local.entity.UsuarioEntity
import com.example.apppagoflex_grupo36.navigation.Screen
import com.example.apppagoflex_grupo36.ui.theme.AppPagoFlex_Grupo36Theme
import com.example.apppagoflex_grupo36.utils.RUTUtils
import com.example.apppagoflex_grupo36.viewmodel.SessionViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoleSelectorScreen(
    sessionViewModel: SessionViewModel,
    onNavigateToHome: (Screen) -> Unit
) {
    val sessionState by sessionViewModel.sessionState.collectAsState()
    val usuarios by sessionViewModel.usuarios.collectAsState()
    val roles by sessionViewModel.roles.collectAsState()
    val scope = rememberCoroutineScope()

    RoleSelectorContent(
        usuarios = usuarios,
        roles = roles,
        errorMessage = sessionState.error,
        onSelectUser = { usuario ->
            sessionViewModel.selectUser(usuario)
            val targetScreen = when (usuario.idRol) {
                1 -> Screen.HomeUsuario
                2 -> Screen.HomeAgente
                3 -> Screen.HomeEjecutivo
                else -> Screen.HomeUsuario
            }
            onNavigateToHome(targetScreen)
        },
        onSelectRoleQuick = { idRol ->
            sessionViewModel.selectRole(idRol)
            val targetScreen = when (idRol) {
                1 -> Screen.HomeUsuario
                2 -> Screen.HomeAgente
                3 -> Screen.HomeEjecutivo
                else -> Screen.HomeUsuario
            }
            onNavigateToHome(targetScreen)
        },
        onLoginByRut = { rutInput ->
            scope.launch {
                val success = sessionViewModel.loginByRut(rutInput)
                if (success) {
                    val currentSession = sessionViewModel.sessionState.value
                    val user = currentSession.usuarioActual
                    if (user != null) {
                        val targetScreen = when (user.idRol) {
                            1 -> Screen.HomeUsuario
                            2 -> Screen.HomeAgente
                            3 -> Screen.HomeEjecutivo
                            else -> Screen.HomeUsuario
                        }
                        onNavigateToHome(targetScreen)
                    }
                }
            }
        },
        onClearError = {
            sessionViewModel.clearError()
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RoleSelectorContent(
    usuarios: List<UsuarioEntity>,
    roles: List<RolEntity>,
    errorMessage: String?,
    onSelectUser: (UsuarioEntity) -> Unit,
    onSelectRoleQuick: (Int) -> Unit,
    onLoginByRut: (String) -> Unit,
    onClearError: () -> Unit
) {
    var rutInput by remember { mutableStateOf("") }
    val isRutValid = remember(rutInput) { RUTUtils.validateRut(rutInput) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.AccountBalanceWallet,
                            contentDescription = null,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text("PagoFlex")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Selección de Perfil e Inicio de Sesión",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Selecciona un rol, un usuario registrado o ingresa tu RUT para acceder.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Acceso Rápido por Rol",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            QuickRoleButton(
                                label = "Usuario",
                                subLabel = "R-01",
                                icon = Icons.Rounded.Person,
                                onClick = { onSelectRoleQuick(1) },
                                modifier = Modifier.weight(1f)
                            )
                            QuickRoleButton(
                                label = "Agente",
                                subLabel = "R-02",
                                icon = Icons.Rounded.SupportAgent,
                                onClick = { onSelectRoleQuick(2) },
                                modifier = Modifier.weight(1f)
                            )
                            QuickRoleButton(
                                label = "Ejecutivo",
                                subLabel = "R-03",
                                icon = Icons.Rounded.Business,
                                onClick = { onSelectRoleQuick(3) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            item {
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Ingreso con RUT",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Valida tu RUT chileno con Módulo 11 (ej: 12.345.678-5)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = rutInput,
                            onValueChange = { input ->
                                rutInput = input
                                onClearError()
                            },
                            label = { Text("RUT Usuario") },
                            placeholder = { Text("12.345.678-5") },
                            isError = rutInput.isNotBlank() && !isRutValid,
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Rounded.Badge,
                                    contentDescription = null
                                )
                            },
                            trailingIcon = {
                                if (rutInput.isNotBlank()) {
                                    if (isRutValid) {
                                        Icon(
                                            imageVector = Icons.Rounded.CheckCircle,
                                            contentDescription = "RUT Válido",
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    } else {
                                        Icon(
                                            imageVector = Icons.Rounded.Error,
                                            contentDescription = "RUT Inválido",
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                    }
                                }
                            },
                            supportingText = {
                                if (rutInput.isNotBlank()) {
                                    if (isRutValid) {
                                        Text(
                                            text = "RUT válido (Módulo 11 correcto)",
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    } else {
                                        Text(
                                            text = "Dígito verificador o formato incorrecto",
                                            color = MaterialTheme.colorScheme.error
                                        )
                                    }
                                }
                            },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Text,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    if (isRutValid) {
                                        onLoginByRut(rutInput)
                                    }
                                }
                            ),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        if (!errorMessage.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = errorMessage,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = { onLoginByRut(rutInput) },
                            enabled = isRutValid,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.Login,
                                contentDescription = null,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            Text("Ingresar por RUT")
                        }
                    }
                }
            }

            item {
                Text(
                    text = "Usuarios Registrados",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            items(usuarios, key = { it.idUsuario }) { usuario ->
                UserProfileCard(
                    usuario = usuario,
                    roles = roles,
                    onClick = { onSelectUser(usuario) }
                )
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun QuickRoleButton(
    label: String,
    subLabel: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(vertical = 4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = subLabel,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun UserProfileCard(
    usuario: UsuarioEntity,
    roles: List<RolEntity>,
    onClick: () -> Unit
) {
    val rolName = remember(usuario.idRol, roles) {
        roles.find { it.idRol == usuario.idRol }?.nombreRol
            ?: DatabaseSeeder.rolesInitial.find { it.idRol == usuario.idRol }?.nombreRol
            ?: "Rol #${usuario.idRol}"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = when (usuario.idRol) {
                        2 -> Icons.Rounded.SupportAgent
                        3 -> Icons.Rounded.Business
                        else -> Icons.Rounded.Person
                    },
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier
                        .padding(12.dp)
                        .size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = usuario.nombreUsuario,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "RUT: ${RUTUtils.maskRut(usuario.rutUsuario)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = usuario.correo,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            AssistChip(
                onClick = onClick,
                label = {
                    Text(
                        text = when (usuario.idRol) {
                            1 -> "R-01 Usuario"
                            2 -> "R-02 Agente"
                            3 -> "R-03 Ejecutivo"
                            else -> rolName
                        },
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RoleSelectorScreenPreview() {
    AppPagoFlex_Grupo36Theme {
        RoleSelectorContent(
            usuarios = DatabaseSeeder.usuariosInitial,
            roles = DatabaseSeeder.rolesInitial,
            errorMessage = null,
            onSelectUser = {},
            onSelectRoleQuick = {},
            onLoginByRut = {},
            onClearError = {}
        )
    }
}
