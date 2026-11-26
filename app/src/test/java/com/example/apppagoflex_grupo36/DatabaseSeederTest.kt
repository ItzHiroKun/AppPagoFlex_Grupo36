package com.example.apppagoflex_grupo36

import com.example.apppagoflex_grupo36.data.local.DatabaseSeeder
import com.example.apppagoflex_grupo36.utils.CalculoUtils
import com.example.apppagoflex_grupo36.utils.RUTUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DatabaseSeederTest {

    @Test
    fun testSeederInitialData_rolesAndEmpresasPopulated() {
        assertEquals(3, DatabaseSeeder.rolesInitial.size)
        assertEquals(3, DatabaseSeeder.empresasInitial.size)
        assertEquals(4, DatabaseSeeder.usuariosInitial.size)
        assertEquals(6, DatabaseSeeder.compromisosInitial.size)
        assertEquals(2, DatabaseSeeder.pagosInitial.size)
    }

    @Test
    fun testSeederRuts_allRutsAreValidChileanRuts() {
        for (empresa in DatabaseSeeder.empresasInitial) {
            assertTrue("Invalid RUT for empresa: ${empresa.rutEmpresa}", RUTUtils.validateRut(empresa.rutEmpresa))
        }

        for (usuario in DatabaseSeeder.usuariosInitial) {
            assertTrue("Invalid RUT for usuario: ${usuario.rutUsuario}", RUTUtils.validateRut(usuario.rutUsuario))
        }
    }

    @Test
    fun testSeederCalculations_collectionRatesAndTotals() {

        val tasaEmpresa1 = CalculoUtils.calcularTasaCobranzaPorEmpresa(1, DatabaseSeeder.compromisosInitial)
        assertEquals(0.0, tasaEmpresa1, 0.001)

        val tasaEmpresa2 = CalculoUtils.calcularTasaCobranzaPorEmpresa(2, DatabaseSeeder.compromisosInitial)
        assertEquals(50.0, tasaEmpresa2, 0.001)

        val tasaEmpresa3 = CalculoUtils.calcularTasaCobranzaPorEmpresa(3, DatabaseSeeder.compromisosInitial)
        assertEquals(100.0, tasaEmpresa3, 0.001)


        val compromisoVencido = DatabaseSeeder.compromisosInitial[1]
        val recargo = CalculoUtils.calcularRecargoPorAtraso(compromisoVencido)
        val totalAPagar = CalculoUtils.calcularTotalAPagar(compromisoVencido)

        assertEquals(2500.0, recargo, 0.001)
        assertEquals(27500.0, totalAPagar, 0.001)
    }
}
