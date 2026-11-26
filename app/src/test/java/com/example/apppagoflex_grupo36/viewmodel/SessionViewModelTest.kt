package com.example.apppagoflex_grupo36.viewmodel

import com.example.apppagoflex_grupo36.data.local.DatabaseSeeder
import com.example.apppagoflex_grupo36.data.local.dao.PagoFlexDao
import com.example.apppagoflex_grupo36.data.local.entity.CompromisoEntity
import com.example.apppagoflex_grupo36.data.local.entity.EmpresaClienteEntity
import com.example.apppagoflex_grupo36.data.local.entity.PagoEntity
import com.example.apppagoflex_grupo36.data.local.entity.RolEntity
import com.example.apppagoflex_grupo36.data.local.entity.UsuarioEntity
import com.example.apppagoflex_grupo36.utils.RUTUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SessionViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeDao: FakePagoFlexDao
    private lateinit var viewModel: SessionViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeDao = FakePagoFlexDao()
        viewModel = SessionViewModel(fakeDao)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testInitialSessionState_isEmpty() {
        val state = viewModel.sessionState.value
        assertNull(state.usuarioActual)
        assertNull(state.rolActual)
        assertNull(state.empresaActual)
        assertFalse(state.isLoggedIn)
    }

    @Test
    fun testSelectUser_endUser_updatesSessionWithUserAndRole() = runTest {
        val user = DatabaseSeeder.usuariosInitial[0] // Juan Pérez (idRol = 1)
        viewModel.selectUser(user)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.sessionState.value
        assertEquals(user, state.usuarioActual)
        assertEquals(1, state.rolActual?.idRol)
        assertNull(state.empresaActual)
        assertTrue(state.isLoggedIn)
    }

    @Test
    fun testSelectUser_executive_updatesSessionWithCompany() = runTest {
        val user = DatabaseSeeder.usuariosInitial[3] // Ana Morales (idRol = 3, idEmpresa = 1)
        viewModel.selectUser(user)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.sessionState.value
        assertEquals(user, state.usuarioActual)
        assertEquals(3, state.rolActual?.idRol)
        assertNotNull(state.empresaActual)
        assertEquals(1L, state.empresaActual?.idEmpresa)
        assertTrue(state.isLoggedIn)
    }

    @Test
    fun testSelectRole_agentRole_selectsAgentUser() = runTest {
        viewModel.selectRole(2) // R-02 Agente
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.sessionState.value
        assertNotNull(state.usuarioActual)
        assertEquals(2, state.rolActual?.idRol)
        assertEquals("Carlos Silva", state.usuarioActual?.nombreUsuario)
    }

    @Test
    fun testLoginByRut_validRegisteredRut_success() = runTest {
        val rut = "12.345.678-5" // Juan Pérez
        val success = viewModel.loginByRut(rut)
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(success)
        val state = viewModel.sessionState.value
        assertNotNull(state.usuarioActual)
        assertEquals("Juan Pérez", state.usuarioActual?.nombreUsuario)
        assertNull(state.error)
    }

    @Test
    fun testLoginByRut_invalidRutFormat_returnsFalseAndSetsError() = runTest {
        val invalidRut = "12.345.678-9" // Incorrect check digit
        val success = viewModel.loginByRut(invalidRut)
        testDispatcher.scheduler.advanceUntilIdle()

        assertFalse(success)
        val state = viewModel.sessionState.value
        assertNull(state.usuarioActual)
        assertNotNull(state.error)
        assertTrue(state.error!!.contains("RUT no válido"))
    }

    @Test
    fun testLoginByRut_unregisteredValidRut_returnsFalseAndSetsError() = runTest {
        val unregisteredRut = "11.111.112-K" // Valid M11 RUT, not in seeder
        val success = viewModel.loginByRut(unregisteredRut)
        testDispatcher.scheduler.advanceUntilIdle()

        assertFalse(success)
        val state = viewModel.sessionState.value
        assertNull(state.usuarioActual)
        assertNotNull(state.error)
        assertTrue(state.error!!.contains("no está registrado"))
    }

    @Test
    fun testLogout_clearsSession() = runTest {
        viewModel.selectUser(DatabaseSeeder.usuariosInitial[0])
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.sessionState.value.isLoggedIn)

        viewModel.logout()

        val state = viewModel.sessionState.value
        assertNull(state.usuarioActual)
        assertNull(state.rolActual)
        assertFalse(state.isLoggedIn)
    }
}

