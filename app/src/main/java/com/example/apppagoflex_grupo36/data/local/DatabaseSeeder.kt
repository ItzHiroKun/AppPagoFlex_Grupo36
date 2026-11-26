package com.example.apppagoflex_grupo36.data.local

import com.example.apppagoflex_grupo36.data.local.dao.PagoFlexDao
import com.example.apppagoflex_grupo36.data.local.entity.CompromisoEntity
import com.example.apppagoflex_grupo36.data.local.entity.EmpresaClienteEntity
import com.example.apppagoflex_grupo36.data.local.entity.PagoEntity
import com.example.apppagoflex_grupo36.data.local.entity.RolEntity
import com.example.apppagoflex_grupo36.data.local.entity.UsuarioEntity

object DatabaseSeeder {

    val rolesInitial = listOf(
        RolEntity(idRol = 1, nombreRol = "R-01 Usuario Final"),
        RolEntity(idRol = 2, nombreRol = "R-02 Agente PagoFlex"),
        RolEntity(idRol = 3, nombreRol = "R-03 Ejecutivo Empresa Cliente")
    )

    val empresasInitial = listOf(
        EmpresaClienteEntity(idEmpresa = 1, rutEmpresa = "76.082.121-7", razonSocial = "Servicios Eléctricos S.A."),
        EmpresaClienteEntity(idEmpresa = 2, rutEmpresa = "77.123.456-9", razonSocial = "Aguas Sanitarias del Sur"),
        EmpresaClienteEntity(idEmpresa = 3, rutEmpresa = "78.987.654-1", razonSocial = "Telecomunicaciones PagoFlex")
    )

    val usuariosInitial = listOf(
        UsuarioEntity(idUsuario = 1, rutUsuario = "12.345.678-5", nombreUsuario = "Juan Pérez", idRol = 1, idEmpresa = null, correo = "juan.perez@email.com"),
        UsuarioEntity(idUsuario = 2, rutUsuario = "11.111.111-1", nombreUsuario = "María González", idRol = 1, idEmpresa = null, correo = "maria.gonzalez@email.com"),
        UsuarioEntity(idUsuario = 3, rutUsuario = "15.234.567-4", nombreUsuario = "Carlos Silva", idRol = 2, idEmpresa = null, correo = "carlos.agente@pagoflex.cl"),
        UsuarioEntity(idUsuario = 4, rutUsuario = "16.789.012-1", nombreUsuario = "Ana Morales", idRol = 3, idEmpresa = 1, correo = "ana.morales@electricas.cl")
    )

    val compromisosInitial = listOf(
        CompromisoEntity(idCompromiso = 1, idUsuario = 1, idEmpresa = 1, monto = 45000.0, fechaVencimiento = "2025-04-15", estado = CompromisoEntity.ESTADO_PENDIENTE),
        CompromisoEntity(idCompromiso = 2, idUsuario = 1, idEmpresa = 2, monto = 25000.0, fechaVencimiento = "2025-01-10", estado = CompromisoEntity.ESTADO_VENCIDO),
        CompromisoEntity(idCompromiso = 3, idUsuario = 1, idEmpresa = 3, monto = 18000.0, fechaVencimiento = "2024-12-01", estado = CompromisoEntity.ESTADO_PAGADO),
        CompromisoEntity(idCompromiso = 4, idUsuario = 2, idEmpresa = 1, monto = 60000.0, fechaVencimiento = "2025-05-01", estado = CompromisoEntity.ESTADO_PENDIENTE),
        CompromisoEntity(idCompromiso = 5, idUsuario = 2, idEmpresa = 2, monto = 32000.0, fechaVencimiento = "2024-11-15", estado = CompromisoEntity.ESTADO_PAGADO),
        CompromisoEntity(idCompromiso = 6, idUsuario = 1, idEmpresa = 1, monto = 50000.0, fechaVencimiento = "2024-10-10", estado = CompromisoEntity.ESTADO_ANULADO)
    )

    val pagosInitial = listOf(
        PagoEntity(idPago = 1, idCompromiso = 3, fechaPago = "2024-11-28", montoPagado = 18000.0, comprobante = "CMP-20241128-001"),
        PagoEntity(idPago = 2, idCompromiso = 5, fechaPago = "2024-11-10", montoPagado = 32000.0, comprobante = "CMP-20241110-002")
    )

    suspend fun seed(dao: PagoFlexDao) {
        dao.insertRoles(rolesInitial)
        dao.insertEmpresas(empresasInitial)
        dao.insertUsuarios(usuariosInitial)
        dao.insertCompromisos(compromisosInitial)
        dao.insertPagos(pagosInitial)
    }
}
