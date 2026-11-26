package com.example.apppagoflex_grupo36.viewmodel

import com.example.apppagoflex_grupo36.data.local.DatabaseSeeder
import com.example.apppagoflex_grupo36.data.local.dao.PagoFlexDao
import com.example.apppagoflex_grupo36.data.local.entity.CompromisoEntity
import com.example.apppagoflex_grupo36.data.local.entity.EmpresaClienteEntity
import com.example.apppagoflex_grupo36.data.local.entity.PagoEntity
import com.example.apppagoflex_grupo36.data.local.entity.RolEntity
import com.example.apppagoflex_grupo36.data.local.entity.UsuarioEntity
import com.example.apppagoflex_grupo36.utils.RUTUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakePagoFlexDao(
    initialRoles: List<RolEntity> = DatabaseSeeder.rolesInitial,
    initialEmpresas: List<EmpresaClienteEntity> = DatabaseSeeder.empresasInitial,
    initialUsuarios: List<UsuarioEntity> = DatabaseSeeder.usuariosInitial,
    initialCompromisos: List<CompromisoEntity> = DatabaseSeeder.compromisosInitial,
    initialPagos: List<PagoEntity> = DatabaseSeeder.pagosInitial
) : PagoFlexDao {

    val rolesFlow = MutableStateFlow(initialRoles.toList())
    val empresasFlow = MutableStateFlow(initialEmpresas.toList())
    val usuariosFlow = MutableStateFlow(initialUsuarios.toList())
    val compromisosFlow = MutableStateFlow(initialCompromisos.toList())
    val pagosFlow = MutableStateFlow(initialPagos.toList())

    override suspend fun insertRoles(roles: List<RolEntity>) {
        rolesFlow.value = rolesFlow.value + roles
    }
    override fun getAllRoles(): Flow<List<RolEntity>> = rolesFlow
    override suspend fun getAllRolesList(): List<RolEntity> = rolesFlow.value
    override suspend fun getRolById(idRol: Int): RolEntity? = rolesFlow.value.find { it.idRol == idRol }

    override suspend fun insertEmpresas(empresas: List<EmpresaClienteEntity>): List<Long> {
        empresasFlow.value = empresasFlow.value + empresas
        return empresas.map { it.idEmpresa }
    }
    override suspend fun insertEmpresa(empresa: EmpresaClienteEntity): Long {
        empresasFlow.value = empresasFlow.value + empresa
        return empresa.idEmpresa
    }
    override fun getAllEmpresas(): Flow<List<EmpresaClienteEntity>> = empresasFlow
    override suspend fun getAllEmpresasList(): List<EmpresaClienteEntity> = empresasFlow.value
    override suspend fun getEmpresaById(idEmpresa: Long): EmpresaClienteEntity? =
        empresasFlow.value.find { it.idEmpresa == idEmpresa }

    override suspend fun insertUsuarios(usuarios: List<UsuarioEntity>): List<Long> {
        usuariosFlow.value = usuariosFlow.value + usuarios
        return usuarios.map { it.idUsuario }
    }
    override suspend fun insertUsuario(usuario: UsuarioEntity): Long {
        val newId = (usuariosFlow.value.maxOfOrNull { it.idUsuario } ?: 0L) + 1L
        val saved = usuario.copy(idUsuario = newId)
        usuariosFlow.value = usuariosFlow.value + saved
        return newId
    }
    override fun getAllUsuarios(): Flow<List<UsuarioEntity>> = usuariosFlow
    override suspend fun getAllUsuariosList(): List<UsuarioEntity> = usuariosFlow.value
    override suspend fun getUsuarioById(idUsuario: Long): UsuarioEntity? =
        usuariosFlow.value.find { it.idUsuario == idUsuario }
    override suspend fun getUsuarioByRut(rutUsuario: String): UsuarioEntity? {
        val clean = RUTUtils.cleanRut(rutUsuario)
        return usuariosFlow.value.find { RUTUtils.cleanRut(it.rutUsuario) == clean }
    }

    override suspend fun insertCompromisos(compromisos: List<CompromisoEntity>): List<Long> {
        compromisosFlow.value = compromisosFlow.value + compromisos
        return compromisos.map { it.idCompromiso }
    }
    override suspend fun insertCompromiso(compromiso: CompromisoEntity): Long {
        val newId = (compromisosFlow.value.maxOfOrNull { it.idCompromiso } ?: 0L) + 1L
        val saved = compromiso.copy(idCompromiso = newId)
        compromisosFlow.value = compromisosFlow.value + saved
        return newId
    }
    override suspend fun updateCompromiso(compromiso: CompromisoEntity) {
        compromisosFlow.value = compromisosFlow.value.map {
            if (it.idCompromiso == compromiso.idCompromiso) compromiso else it
        }
    }
    override fun getAllCompromisos(): Flow<List<CompromisoEntity>> = compromisosFlow
    override suspend fun getAllCompromisosList(): List<CompromisoEntity> = compromisosFlow.value

    override fun getCompromisosByUsuario(idUsuario: Long): Flow<List<CompromisoEntity>> =
        compromisosFlow.map { list -> list.filter { it.idUsuario == idUsuario } }

    override suspend fun getCompromisosListByUsuario(idUsuario: Long): List<CompromisoEntity> =
        compromisosFlow.value.filter { it.idUsuario == idUsuario }

    override fun getCompromisosByEmpresa(idEmpresa: Long): Flow<List<CompromisoEntity>> =
        compromisosFlow.map { list -> list.filter { it.idEmpresa == idEmpresa } }

    override suspend fun getCompromisosListByEmpresa(idEmpresa: Long): List<CompromisoEntity> =
        compromisosFlow.value.filter { it.idEmpresa == idEmpresa }

    override suspend fun getCompromisoById(idCompromiso: Long): CompromisoEntity? =
        compromisosFlow.value.find { it.idCompromiso == idCompromiso }

    override suspend fun insertPagos(pagos: List<PagoEntity>): List<Long> {
        pagosFlow.value = pagosFlow.value + pagos
        return pagos.map { it.idPago }
    }
    override suspend fun insertPago(pago: PagoEntity): Long {
        val newId = (pagosFlow.value.maxOfOrNull { it.idPago } ?: 0L) + 1L
        val saved = pago.copy(idPago = newId)
        pagosFlow.value = pagosFlow.value + saved
        return newId
    }
    override fun getAllPagos(): Flow<List<PagoEntity>> = pagosFlow
    override suspend fun getAllPagosList(): List<PagoEntity> = pagosFlow.value
    override suspend fun getPagoByCompromisoId(idCompromiso: Long): PagoEntity? =
        pagosFlow.value.find { it.idCompromiso == idCompromiso }
}
