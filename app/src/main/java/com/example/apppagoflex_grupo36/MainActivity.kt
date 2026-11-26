package com.example.apppagoflex_grupo36

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.apppagoflex_grupo36.data.local.AppDatabase
import com.example.apppagoflex_grupo36.navigation.AppNavigation
import com.example.apppagoflex_grupo36.ui.theme.AppPagoFlex_Grupo36Theme
import com.example.apppagoflex_grupo36.viewmodel.EstadoViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val db = AppDatabase.getDatabase(applicationContext)

        setContent {
            val estadoViewModel: EstadoViewModel = viewModel()
            val modoOscuro by estadoViewModel.modoOscuro.collectAsState()

            modoOscuro?.let { oscuro ->
                AppPagoFlex_Grupo36Theme(darkTheme = oscuro) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        AppNavigation(dao = db.pagoFlexDao())
                    }
                }
            }
        }
    }
}
