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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ExitToApp
import androidx.compose.material.icons.rounded.AdminPanelSettings
import androidx.compose.material.icons.rounded.Cancel
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.EditNote
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.SupportAgent
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.apppagoflex_grupo36.data.local.dao.PagoFlexDao
import com.example.apppagoflex_grupo36.data.local.entity.CompromisoEntity
import com.example.apppagoflex_grupo36.data.local.entity.EmpresaClienteEntity
import com.example.apppagoflex_grupo36.data.local.entity.PagoEntity
import com.example.apppagoflex_grupo36.data.local.entity.UsuarioEntity
import com.example.apppagoflex_grupo36.utils.RUTUtils
import com.example.apppagoflex_grupo36.viewmodel.AgenteViewModel
import com.example.apppagoflex_grupo36.viewmodel.AgenteViewModelFactory
import com.example.apppagoflex_grupo36.viewmodel.SessionViewModel
import com.example.apppagoflex_grupo36.viewmodel.SupportTicketData
import java.text.NumberFormat
import java.util.Locale
import com.example.apppagoflex_grupo36.utils.AnchoPantalla
import com.example.apppagoflex_grupo36.utils.obtenerAnchoPantalla

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeAgenteScreen(
    dao: PagoFlexDao,
    sessionViewModel: SessionViewModel,
    onLogout: () -> Unit
) {
    val sessionState by sessionViewModel.sessionState.collectAsState()
    val usuario = sessionState.usuarioActual

    val viewModel: AgenteViewModel = viewModel(
        factory = AgenteViewModelFactory(dao)
    )

    val uiState by viewModel.uiState.collectAsState()
    val currencyFormatter = remember { NumberFormat.getCurrencyInstance(Locale.forLanguageTag("es-CL")) }

    var selectedCompromisoForDialog by remember { mutableStateOf<CompromisoEntity?>(null) }
    val ancho = obtenerAnchoPantalla()
    val margenHorizontal = when (ancho) {
        AnchoPantalla.COMPACTA -> 16.dp
        AnchoPantalla.MEDIANA -> 48.dp
        AnchoPantalla.EXPANDIDA -> 120.dp
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("PagoFlex - Soporte y Agente") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onSecondaryContainer
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

            AgentHeaderCard(
                nombreAgente = usuario?.nombreUsuario ?: "Carlos Silva",
                rutAgente = usuario?.rutUsuario ?: "15.234.567-4",
                correo = usuario?.correo ?: "carlos.agente@pagoflex.cl",
                rol = sessionState.rolActual?.nombreRol ?: "R-02 Agente PagoFlex"
            )

            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Buscar por RUT, Nombre de Cliente o Empresa...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Rounded.Search,
                        contentDescription = "Buscar"
                    )
                },
                trailingIcon = {
                    if (uiState.searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.setSearchQuery("") }) {
                            Icon(
                                imageVector = Icons.Rounded.Clear,
                                contentDescription = "Limpiar"
                            )
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            val filterOptions = listOf("Todos", "Pendiente", "Vencido", "Pagado", "Anulado")
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(filterOptions) { filter ->
                    FilterChip(
                        selected = uiState.selectedStatusFilter.equals(filter, ignoreCase = true),
                        onClick = { viewModel.setStatusFilter(filter) },
                        label = { Text(filter) },
                        leadingIcon = if (uiState.selectedStatusFilter.equals(filter, ignoreCase = true)) {
                            {
                                Icon(
                                    imageVector = Icons.Rounded.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        } else null
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Compromisos Encontrados (${uiState.compromisosFiltrados.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary
                )
            }

            if (uiState.compromisosFiltrados.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No se encontraron compromisos que coincidan con los filtros.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(
                        items = uiState.compromisosFiltrados,
                        key = { it.idCompromiso }
                    ) { compromiso ->
                        AgenteCompromisoCard(
                            compromiso = compromiso,
                            usuario = uiState.usuariosMap[compromiso.idUsuario],
                            empresa = uiState.empresasMap[compromiso.idEmpresa],
                            pago = uiState.pagosMap[compromiso.idCompromiso],
                            ticket = uiState.ticketsSoporteMap[compromiso.idCompromiso],
                            currencyFormatter = currencyFormatter,
                            onOpenNotaDialog = { selectedCompromisoForDialog = compromiso },
                            onToggleTicketStatus = { viewModel.toggleEstadoTicket(compromiso.idCompromiso) },
                            onAnularCompromiso = { viewModel.anularCompromiso(compromiso.idCompromiso) }
                        )
                    }
                }
            }
        }
    }

    selectedCompromisoForDialog?.let { compromiso ->
        val currentTicket = uiState.ticketsSoporteMap[compromiso.idCompromiso]
        SoporteNotaDialog(
            idCompromiso = compromiso.idCompromiso,
            initialNota = currentTicket?.nota ?: "",
            onDismiss = { selectedCompromisoForDialog = null },
            onSaveNota = { id, nota ->
                viewModel.guardarNotaSoporte(id, nota)
                selectedCompromisoForDialog = null
            }
        )
    }
}

@Composable
private fun AgentHeaderCard(
    nombreAgente: String,
    rutAgente: String,
    correo: String,
    rol: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Rounded.SupportAgent,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(52.dp)
            )
            Column(
                modifier = Modifier
                    .padding(start = 16.dp)
                    .weight(1f)
            ) {
                Text(
                    text = nombreAgente,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "RUT: ${RUTUtils.maskRut(rutAgente)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = correo,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                AssistChip(
                    onClick = { },
                    label = { Text(rol) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Rounded.AdminPanelSettings,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun AgenteCompromisoCard(
    compromiso: CompromisoEntity,
    usuario: UsuarioEntity?,
    empresa: EmpresaClienteEntity?,
    pago: PagoEntity?,
    ticket: SupportTicketData?,
    currencyFormatter: NumberFormat,
    onOpenNotaDialog: () -> Unit,
    onToggleTicketStatus: () -> Unit,
    onAnularCompromiso: () -> Unit
) {
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
            verticalArrangement = Arrangement.spacedBy(8.dp)
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
                        tint = MaterialTheme.colorScheme.secondary,
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
                text = "RUT Cliente: ${usuario?.rutUsuario ?: "N/A"} | Empresa: ${empresa?.razonSocial ?: "N/A"}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Compromiso #${compromiso.idCompromiso} | Vence: ${compromiso.fechaVencimiento}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = currencyFormatter.format(compromiso.monto),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary
                )
            }

            if (pago != null) {
                Text(
                    text = "Comprobante Pago: ${pago.comprobante} (${pago.fechaPago})",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2E7D32)
                )
            }

            HorizontalDivider()

            // Support section
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (ticket != null && ticket.nota.isNotBlank()) "Nota de Soporte: ${ticket.nota}" else "Sin nota de soporte adjunta",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )

                    AssistChip(
                        onClick = onToggleTicketStatus,
                        label = { Text(if (ticket?.resuelto == true) "Resuelto" else "Pendiente Soporte") },
                        leadingIcon = {
                            Icon(
                                imageVector = if (ticket?.resuelto == true) Icons.Rounded.Check else Icons.Rounded.EditNote,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onOpenNotaDialog,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.EditNote,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Nota Soporte")
                    }

                    if (!compromiso.estado.equals(CompromisoEntity.ESTADO_PAGADO, ignoreCase = true) &&
                        !compromiso.estado.equals(CompromisoEntity.ESTADO_ANULADO, ignoreCase = true)
                    ) {
                        Button(
                            onClick = onAnularCompromiso,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Cancel,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Anular")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SoporteNotaDialog(
    idCompromiso: Long,
    initialNota: String,
    onDismiss: () -> Unit,
    onSaveNota: (Long, String) -> Unit
) {
    var notaInput by remember { mutableStateOf(initialNota) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nota de Soporte - Compromiso #$idCompromiso") },
        text = {
            Column {
                Text(
                    text = "Ingrese los detalles de resolución o reporte de soporte para este compromiso:",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = notaInput,
                    onValueChange = { notaInput = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Ej: Cliente reporta pago por transferencia en revisión...") },
                    minLines = 3
                )
            }
        },
        confirmButton = {
            Button(onClick = { onSaveNota(idCompromiso, notaInput) }) {
                Text("Guardar Nota")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}