package com.example.apppagoflex_grupo36.ui.screen

import android.net.Uri
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ExitToApp
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.CreditCard
import androidx.compose.material.icons.rounded.Event
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.apppagoflex_grupo36.data.local.FotoPerfilStorage
import com.example.apppagoflex_grupo36.data.local.dao.PagoFlexDao
import com.example.apppagoflex_grupo36.data.local.entity.CompromisoEntity
import com.example.apppagoflex_grupo36.data.local.entity.EmpresaClienteEntity
import com.example.apppagoflex_grupo36.ui.components.ComprobanteDialog
import com.example.apppagoflex_grupo36.ui.components.ImagenInteligente
import com.example.apppagoflex_grupo36.ui.components.ResumenCard
import com.example.apppagoflex_grupo36.utils.AnchoPantalla
import com.example.apppagoflex_grupo36.utils.CalculoUtils
import com.example.apppagoflex_grupo36.utils.RUTUtils
import com.example.apppagoflex_grupo36.utils.obtenerAnchoPantalla
import com.example.apppagoflex_grupo36.viewmodel.SessionViewModel
import com.example.apppagoflex_grupo36.viewmodel.UsuarioHomeViewModel
import com.example.apppagoflex_grupo36.viewmodel.UsuarioHomeViewModelFactory
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeUsuarioScreen(
    dao: PagoFlexDao,
    sessionViewModel: SessionViewModel,
    onLogout: () -> Unit,
    onNavigateToPerfil: () -> Unit
) {
    val sessionState by sessionViewModel.sessionState.collectAsState()
    val usuario = sessionState.usuarioActual
    val idUsuario = usuario?.idUsuario ?: 1L

    val viewModel: UsuarioHomeViewModel = viewModel(
        key = "user_$idUsuario",
        factory = UsuarioHomeViewModelFactory(dao, idUsuario)
    )

    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    val context = LocalContext.current
    val fotoPerfil: Uri? = remember(idUsuario) { FotoPerfilStorage.obtenerUri(context, idUsuario) }
    val currencyFormatter = remember { NumberFormat.getCurrencyInstance(Locale.forLanguageTag("es-CL")) }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearError()
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
                title = { Text("PagoFlex - Portal de Pagos") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            UsuarioHeaderCard(
                nombreUsuario = usuario?.nombreUsuario ?: "Usuario Final",
                rutUsuario = usuario?.rutUsuario ?: "12.345.678-5",
                correo = usuario?.correo ?: "juan.perez@email.com",
                rol = sessionState.rolActual?.nombreRol ?: "R-01 Usuario Final",
                fotoPerfil = fotoPerfil,
                valorUf = uiState.valorUf,
                currencyFormatter = currencyFormatter,
                onNavigateToPerfil = onNavigateToPerfil
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ResumenCard(
                    title = "Total a Pagar",
                    value = currencyFormatter.format(uiState.totalAPagar),
                    subtitle = "Incluye recargo si hay mora",
                    icon = Icons.Rounded.CreditCard,
                    badgeColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )

                ResumenCard(
                    title = "Cumplimiento 12M",
                    value = "%.1f%%".format(uiState.porcentajeCumplimiento),
                    subtitle = "Historial de pagos a tiempo",
                    icon = Icons.Rounded.CheckCircle,
                    progress = (uiState.porcentajeCumplimiento / 100.0).toFloat(),
                    percentageBadge = if (uiState.porcentajeCumplimiento >= 80.0) "Excelente" else "Atención",
                    badgeColor = if (uiState.porcentajeCumplimiento >= 80.0) Color(0xFF2E7D32) else Color(0xFFE65100),
                    modifier = Modifier.weight(1f)
                )
            }

            // Executive Payment Action
            Button(
                onClick = { viewModel.pagarProximoCompromiso() },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                enabled = !uiState.isProcessingPayment && uiState.totalAPagar > 0,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                contentPadding = PaddingValues(16.dp)
            ) {
                if (uiState.isProcessingPayment) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Procesando pago en orden...")
                } else {
                    Icon(
                        imageVector = Icons.Rounded.Payments,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = if (uiState.totalAPagar > 0) "PAGAR DEUDA MÁS ANTIGUA" else "SIN DEUDAS PENDIENTES",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }

            Text(
                text = "Regla de pago: Se abonará primero el vencimiento vencido más antiguo, o en su defecto el pendiente más próximo.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = "Mis Compromisos de Pago (${uiState.compromisos.size})",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )

            // Commitments List
            if (uiState.compromisos.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No tienes compromisos de pago registrados.",
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
                        items = uiState.compromisos.sortedWith(
                            compareBy<CompromisoEntity> {
                                when (it.estado) {
                                    CompromisoEntity.ESTADO_VENCIDO -> 0
                                    CompromisoEntity.ESTADO_PENDIENTE -> 1
                                    CompromisoEntity.ESTADO_PAGADO -> 2
                                    else -> 3
                                }
                            }.thenBy { it.fechaVencimiento }
                        ),
                        key = { it.idCompromiso }
                    ) { compromiso ->
                        CompromisoItemCard(
                            compromiso = compromiso,
                            empresa = uiState.empresasMap[compromiso.idEmpresa],
                            currencyFormatter = currencyFormatter
                        )
                    }
                }
            }
        }
    }

    // Receipt Popup Dialog
    uiState.comprobanteReciente?.let { comprobante ->
        ComprobanteDialog(
            comprobante = comprobante,
            onDismiss = { viewModel.dismissComprobanteDialog() }
        )
    }
}

@Composable
private fun UsuarioHeaderCard(
    nombreUsuario: String,
    rutUsuario: String,
    correo: String,
    rol: String,
    fotoPerfil: Uri?,
    valorUf: Double?,
    currencyFormatter: NumberFormat,
    onNavigateToPerfil: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ImagenInteligente(
                    imageUri = fotoPerfil,
                    size = 56.dp,
                    borderWidth = 2.dp,
                    modifier = Modifier.clickable(onClick = onNavigateToPerfil)
                )
                Column(
                    modifier = Modifier
                        .padding(start = 16.dp)
                        .weight(1f)
                ) {
                    Text(
                        text = nombreUsuario,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "RUT: ${RUTUtils.maskRut(rutUsuario)}",
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
                        label = { Text(rol) }
                    )
                }

                IconButton(onClick = onNavigateToPerfil) {
                    Icon(
                        imageVector = Icons.Rounded.CameraAlt,
                        contentDescription = "Abrir Cámara y Perfil",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            // Franja informativa de API Externa (mindicador.cl - UF del día)
            valorUf?.let { uf ->
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Valor UF hoy (API mindicador.cl):",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Text(
                            text = currencyFormatter.format(uf),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CompromisoItemCard(
    compromiso: CompromisoEntity,
    empresa: EmpresaClienteEntity?,
    currencyFormatter: NumberFormat
) {
    val isVencido = compromiso.estado.equals(CompromisoEntity.ESTADO_VENCIDO, ignoreCase = true)
    val recargo = if (isVencido) CalculoUtils.calcularRecargoPorAtraso(compromiso) else 0.0
    val montoFinal = CalculoUtils.calcularTotalAPagar(compromiso)

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
            containerColor = if (isVencido) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.15f)
            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
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
                Text(
                    text = empresa?.razonSocial ?: "Empresa #${compromiso.idEmpresa}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )

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
                    text = currencyFormatter.format(montoFinal),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isVencido) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                )
            }

            if (isVencido) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 2.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Warning,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Monto base: ${currencyFormatter.format(compromiso.monto)} + Recargo por mora 10%: ${currencyFormatter.format(recargo)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}