package com.example.apppagoflex_grupo36.ui.screen

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.apppagoflex_grupo36.ui.components.ImagenInteligente
import com.example.apppagoflex_grupo36.utils.RUTUtils
import com.example.apppagoflex_grupo36.viewmodel.EstadoViewModel
import com.example.apppagoflex_grupo36.viewmodel.PerfilViewModel
import com.example.apppagoflex_grupo36.viewmodel.SessionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerfilScreen(
    sessionViewModel: SessionViewModel,
    perfilViewModel: PerfilViewModel = viewModel(),
    estadoViewModel: EstadoViewModel = viewModel(),
    onVolver: () -> Unit = {}
) {
    val context = LocalContext.current
    val sessionState by sessionViewModel.sessionState.collectAsState()
    val usuario = sessionState.usuarioActual
    val idUsuario = usuario?.idUsuario

    val imagenUri by perfilViewModel.imagenUri.collectAsState()
    val mensajePerfil by perfilViewModel.mensaje.collectAsState()
    val estado by estadoViewModel.activo.collectAsState()
    val mostrarMensaje by estadoViewModel.mostrarMensaje.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(idUsuario) {
        if (idUsuario != null) perfilViewModel.cargarFoto(idUsuario)
    }

    LaunchedEffect(mensajePerfil) {
        mensajePerfil?.let {
            snackbarHostState.showSnackbar(it)
            perfilViewModel.limpiarMensaje()
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null && idUsuario != null) perfilViewModel.guardarDesdeGaleria(idUsuario, uri)
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success: Boolean ->
        if (success && idUsuario != null) {
            perfilViewModel.confirmarFotoCamara(idUsuario)
        } else {
            perfilViewModel.cancelarFotoCamara()
        }
    }

    val abrirCamara: () -> Unit = {
        try {
            cameraLauncher.launch(perfilViewModel.crearUriTemporalCamara())
        } catch (e: ActivityNotFoundException) {
            perfilViewModel.mostrarMensaje("Este dispositivo no tiene una aplicación de cámara.")
        } catch (e: Exception) {
            perfilViewModel.mostrarMensaje("No se pudo abrir la cámara.")
        }
    }

    val permisoCamaraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { concedido: Boolean ->
        if (concedido) {
            abrirCamara()
        } else {
            perfilViewModel.mostrarMensaje("Sin permiso de cámara no se puede tomar la foto. Puedes usar la galería.")
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(title = { Text("Mi Perfil y Preferencias") })
        }
    ) { innerPadding ->
        if (estado == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            val estaActivo = estado == true
            val verdeActivo = Color(0xFF2E7D32)
            val colorAnimado by animateColorAsState(
                targetValue = if (estaActivo) verdeActivo else MaterialTheme.colorScheme.primary,
                animationSpec = tween(durationMillis = 500),
                label = "colorAnimado"
            )
            val colorTexto = if (estaActivo) Color.White else MaterialTheme.colorScheme.onPrimary
            val textoBoton = if (estaActivo) "Desactivar modo oscuro" else "Activar modo oscuro"

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ImagenInteligente(imageUri = imagenUri)

                Text(
                    text = usuario?.nombreUsuario ?: "Usuario",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                if (usuario != null) {
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

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = { galleryLauncher.launch("image/*") },
                        enabled = idUsuario != null,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Galería")
                    }

                    Button(
                        onClick = {
                            val tienePermiso = ContextCompat.checkSelfPermission(
                                context, Manifest.permission.CAMERA
                            ) == PackageManager.PERMISSION_GRANTED
                            if (tienePermiso) abrirCamara()
                            else permisoCamaraLauncher.launch(Manifest.permission.CAMERA)
                        },
                        enabled = idUsuario != null,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cámara")
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                Button(
                    onClick = { estadoViewModel.alternarEstado() },
                    colors = ButtonDefaults.buttonColors(containerColor = colorAnimado),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Text(text = textoBoton, color = colorTexto)
                }

                AnimatedVisibility(visible = mostrarMensaje) {
                    Text(
                        text = if (estaActivo) "¡Modo oscuro activado y guardado!" else "¡Modo claro activado y guardado!",
                        color = verdeActivo,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                OutlinedButton(
                    onClick = onVolver,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Volver al Inicio")
                }
            }
        }
    }
}
