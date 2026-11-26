package com.example.apppagoflex_grupo36.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ExitToApp
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Analytics
import androidx.compose.material.icons.rounded.Business
import androidx.compose.material.icons.rounded.Cancel
import androidx.compose.material.icons.rounded.Domain
import androidx.compose.material.icons.rounded.Event
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.apppagoflex_grupo36.data.local.dao.PagoFlexDao
import com.example.apppagoflex_grupo36.data.local.entity.CompromisoEntity
import com.example.apppagoflex_grupo36.data.local.entity.UsuarioEntity
import com.example.apppagoflex_grupo36.ui.components.ResumenCard
import com.example.apppagoflex_grupo36.utils.RUTUtils
import com.example.apppagoflex_grupo36.viewmodel.EjecutivoViewModel
import com.example.apppagoflex_grupo36.viewmodel.EjecutivoViewModelFactory
import com.example.apppagoflex_grupo36.viewmodel.SessionViewModel
import java.text.NumberFormat
import java.util.Locale
import com.example.apppagoflex_grupo36.utils.AnchoPantalla
import com.example.apppagoflex_grupo36.utils.obtenerAnchoPantalla

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeEjecutivoScreen(
    dao: PagoFlexDao,
    sessionViewModel: SessionViewModel,
    onLogout: () -> Unit
) {
    val sessionState by sessionViewModel.sessionState.collectAsState()
    val usuario = sessionState.usuarioActual
    val empresa = sessionState.empresaActual
    val idEmpresa = empresa?.idEmpresa ?: 1L

    val viewModel: EjecutivoViewModel = viewModel(
        key = "empresa_$idEmpresa",
        factory = EjecutivoViewModelFactory(dao, idEmpresa)
    )

    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val currencyFormatter = remember { NumberFormat.getCurrencyInstance(Locale.forLanguageTag("es-CL")) }

    var showRegistrarDialog by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearMessages()
        }
    }

    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearMessages()
        }
    }

    val ancho = obtenerAnchoPantalla()
    val margenHorizontal = when (ancho) {
        AnchoPantalla.COMPACTA -> 16.dp
        AnchoPantalla.MEDIANA -> 48.dp
        AnchoPantalla.EXPANDIDA -> 120.dp
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("PagoFlex - Panel Ejecutivo") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onTertiaryContainer
                ),
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ExitToApp,
                            contentDescription = "Cerrar Sesión"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showRegistrarDialog = true },
                icon = { Icon(imageVector = Icons.Rounded.Add, contentDescription = null) },
                text = { Text("Registrar Deuda") },
                containerColor = MaterialTheme.colorScheme.tertiary,
                contentColor = MaterialTheme.colorScheme.onTertiary
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = margenHorizontal),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Spacer(modifier = Modifier.height(2.dp))

            EjecutivoHeaderCard(
                nombreEjecutivo = usuario?.nombreUsuario ?: "Ana Morales",
                rutEjecutivo = usuario?.rutUsuario ?: "16.789.012-1",
                correo = usuario?.correo ?: "ana.morales@electricas.cl",
                rol = sessionState.rolActual?.nombreRol ?: "R-03 Ejecutivo Empresa",
                nombreEmpresa = empresa?.razonSocial ?: "Servicios Eléctricos S.A.",
                rutEmpresa = empresa?.rutEmpresa ?: "76.082.121-7"
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ResumenCard(
                    title = "Tasa de Cobranza",
                    value = "%.1f%%".format(uiState.tasaCobranza),
                    subtitle = "Pagados / Validos",
                    icon = Icons.Rounded.Analytics,
                    progress = (uiState.tasaCobranza / 100.0).toFloat(),
                    percentageBadge = if (uiState.tasaCobranza >= 70.0) "Óptima" else "En Seguimiento",
                    badgeColor = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.weight(1f)
                )

                MetricasDesgloseCard(
                    countPagado = uiState.countPagado,
                    countPendiente = uiState.countPendiente,
                    countVencido = uiState.countVencido,
                    countAnulado = uiState.countAnulado,
                    modifier = Modifier.weight(1f)
                )
            }

            Text(
                text = "Compromisos de Deuda Emitidos (${uiState.compromisos.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.tertiary
            )

            // Commitment List
            if (uiState.compromisos.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No se han registrado compromisos de deuda para esta empresa.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(
                        items = uiState.compromisos,
                        key = { it.idCompromiso }
                    ) { compromiso ->
                        EjecutivoCompromisoCard(
                            compromiso = compromiso,
                            usuario = uiState.usuariosMap[compromiso.idUsuario],
                            currencyFormatter = currencyFormatter,
                            onAnularCompromiso = { viewModel.anularCompromiso(compromiso.idCompromiso) }
                        )
                    }
                }
            }
        }
    }

    // Dialog for registering new debt commitment
    if (showRegistrarDialog) {
        RegistrarCompromisoDialog(
            onDismiss = { showRegistrarDialog = false },
            onConfirm = { rut, monto, fecha ->
                viewModel.registrarCompromiso(rut, monto, fecha)
                showRegistrarDialog = false
            }
        )
    }
}

@Composable
private fun EjecutivoHeaderCard(
    nombreEjecutivo: String,
    rutEjecutivo: String,
    correo: String,
    rol: String,
    nombreEmpresa: String,
    rutEmpresa: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Rounded.Business,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.size(48.dp)
                )
                Column(
                    modifier = Modifier
                        .padding(start = 12.dp)
                        .weight(1f)
                ) {
                    Text(
                        text = nombreEjecutivo,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "RUT: ${RUTUtils.maskRut(rutEjecutivo)} | $correo",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    AssistChip(
                        onClick = { },
                        label = { Text(rol) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Domain,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = nombreEmpresa,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "RUT Empresa: $rutEmpresa",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricasDesgloseCard(
    countPagado: Int,
    countPendiente: Int,
    countVencido: Int,
    countAnulado: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "Desglose por Estado",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Pagados:", style = MaterialTheme.typography.bodySmall)
                Text(
                    text = "$countPagado",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2E7D32)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Pendientes:", style = MaterialTheme.typography.bodySmall)
                Text(
                    text = "$countPendiente",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.tertiary
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Vencidos:", style = MaterialTheme.typography.bodySmall)
                Text(
                    text = "$countVencido",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Anulados:", style = MaterialTheme.typography.bodySmall)
                Text(
                    text = "$countAnulado",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun EjecutivoCompromisoCard(
    compromiso: CompromisoEntity,
    usuario: UsuarioEntity?,
    currencyFormatter: NumberFormat,
    onAnularCompromiso: () -> Unit
) {
    val isPagado = compromiso.estado.equals(CompromisoEntity.ESTADO_PAGADO, ignoreCase = true)
    val isAnulado = compromiso.estado.equals(CompromisoEntity.ESTADO_ANULADO, ignoreCase = true)

    val (badgeBg, badgeFg, statusLabel) = when (compromiso.estado) {
        CompromisoEntity.ESTADO_VENCIDO -> Triple(
            MaterialTheme.colorScheme.errorContainer,
            MaterialTheme.colorScheme.onErrorContainer,
            "VENCIDO"
        )
        CompromisoEntity.ESTADO_PENDIENTE -> Triple(
            MaterialTheme.colorScheme.tertiaryContainer,
            MaterialTheme.colorScheme.onTertiaryContainer,
            "PENDIENTE"
        )
        CompromisoEntity.ESTADO_PAGADO -> Triple(
            Color(0xFFE8F5E9),
            Color(0xFF2E7D32),
            "PAGADO"
        )
        else -> Triple(
            MaterialTheme.colorScheme.surfaceContainer,
            MaterialTheme.colorScheme.onSurfaceVariant,
            "ANULADO"
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = usuario?.nombreUsuario ?: "Cliente desconocido",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = badgeBg
                ) {
                    Text(
                        text = statusLabel,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = badgeFg,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Text(
                text = "RUT Deudor: ${usuario?.rutUsuario ?: "Desconocido"} | Compromiso #${compromiso.idCompromiso}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.Event,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Vence: ${compromiso.fechaVencimiento}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Text(
                    text = currencyFormatter.format(compromiso.monto),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.tertiary
                )
            }

            if (!isPagado && !isAnulado) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(
                        onClick = onAnularCompromiso,
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Cancel,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Anular Compromiso")
                    }
                }
            }
        }
    }
}

@Composable
private fun RegistrarCompromisoDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, Double, String) -> Unit
) {
    var rutInput by remember { mutableStateOf("") }
    var montoInput by remember { mutableStateOf("") }
    var fechaInput by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Registrar Nuevo Compromiso") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Ingrese los datos del cliente y el monto de la deuda a registrar:",
                    style = MaterialTheme.typography.bodyMedium
                )

                OutlinedTextField(
                    value = rutInput,
                    onValueChange = { rutInput = it },
                    label = { Text("RUT Cliente (ej: 12.345.678-5)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = montoInput,
                    onValueChange = { montoInput = it },
                    label = { Text("Monto ($)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = fechaInput,
                    onValueChange = { fechaInput = it },
                    label = { Text("Fecha Vencimiento (YYYY-MM-DD)") },
                    placeholder = { Text("2025-06-30") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val montoDouble = montoInput.toDoubleOrNull() ?: 0.0
                    onConfirm(rutInput, montoDouble, fechaInput)
                }
            ) {
                Text("Registrar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}