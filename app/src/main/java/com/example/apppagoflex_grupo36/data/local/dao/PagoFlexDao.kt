package com.example.apppagoflex_grupo36.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.apppagoflex_grupo36.data.local.entity.CompromisoEntity
import com.example.apppagoflex_grupo36.data.local.entity.EmpresaClienteEntity
import com.example.apppagoflex_grupo36.data.local.entity.PagoEntity
import com.example.apppagoflex_grupo36.data.local.entity.RolEntity
import com.example.apppagoflex_grupo36.data.local.entity.UsuarioEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PagoFlexDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoles(roles: List<RolEntity>)

    @Query("SELECT * FROM roles")
    fun getAllRoles(): Flow<List<RolEntity>>

    @Query("SELECT * FROM roles")
    suspend fun getAllRolesList(): List<RolEntity>

    @Query("SELECT * FROM roles WHERE idRol = :idRol")
    suspend fun getRolById(idRol: Int): RolEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEmpresas(empresas: List<EmpresaClienteEntity>): List<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEmpresa(empresa: EmpresaClienteEntity): Long

    @Query("SELECT * FROM empresas")
    fun getAllEmpresas(): Flow<List<EmpresaClienteEntity>>

    @Query("SELECT * FROM empresas")
    suspend fun getAllEmpresasList(): List<EmpresaClienteEntity>

    @Query("SELECT * FROM empresas WHERE idEmpresa = :idEmpresa")
    suspend fun getEmpresaById(idEmpresa: Long): EmpresaClienteEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsuarios(usuarios: List<UsuarioEntity>): List<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsuario(usuario: UsuarioEntity): Long

    @Query("SELECT * FROM usuarios")
    fun getAllUsuarios(): Flow<List<UsuarioEntity>>

    @Query("SELECT * FROM usuarios")
    suspend fun getAllUsuariosList(): List<UsuarioEntity>

    @Query("SELECT * FROM usuarios WHERE idUsuario = :idUsuario")
    suspend fun getUsuarioById(idUsuario: Long): UsuarioEntity?

    @Query("SELECT * FROM usuarios WHERE rutUsuario = :rutUsuario LIMIT 1")
    suspend fun getUsuarioByRut(rutUsuario: String): UsuarioEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCompromisos(compromisos: List<CompromisoEntity>): List<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCompromiso(compromiso: CompromisoEntity): Long

    @Update
    suspend fun updateCompromiso(compromiso: CompromisoEntity)

    @Query("SELECT * FROM compromisos")
    fun getAllCompromisos(): Flow<List<CompromisoEntity>>

    @Query("SELECT * FROM compromisos")
    suspend fun getAllCompromisosList(): List<CompromisoEntity>

    @Query("SELECT * FROM compromisos WHERE idUsuario = :idUsuario")
    fun getCompromisosByUsuario(idUsuario: Long): Flow<List<CompromisoEntity>>

    @Query("SELECT * FROM compromisos WHERE idUsuario = :idUsuario")
    suspend fun getCompromisosListByUsuario(idUsuario: Long): List<CompromisoEntity>

    @Query("SELECT * FROM compromisos WHERE idEmpresa = :idEmpresa")
    fun getCompromisosByEmpresa(idEmpresa: Long): Flow<List<CompromisoEntity>>

    @Query("SELECT * FROM compromisos WHERE idEmpresa = :idEmpresa")
    suspend fun getCompromisosListByEmpresa(idEmpresa: Long): List<CompromisoEntity>

    @Query("SELECT * FROM compromisos WHERE idCompromiso = :idCompromiso")
    suspend fun getCompromisoById(idCompromiso: Long): CompromisoEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPagos(pagos: List<PagoEntity>): List<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPago(pago: PagoEntity): Long

    @Query("SELECT * FROM pagos")
    fun getAllPagos(): Flow<List<PagoEntity>>

    @Query("SELECT * FROM pagos")
    suspend fun getAllPagosList(): List<PagoEntity>

    @Query("SELECT * FROM pagos WHERE idCompromiso = :idCompromiso")
    suspend fun getPagoByCompromisoId(idCompromiso: Long): PagoEntity?

    @Transaction
    suspend fun registrarPagoTransaction(pago: PagoEntity): Long {
        val idPago = insertPago(pago)
        val compromiso = getCompromisoById(pago.idCompromiso)
        if (compromiso != null) {
            updateCompromiso(compromiso.copy(estado = CompromisoEntity.ESTADO_PAGADO))
        }
        return idPago
    }
}
