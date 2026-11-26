package com.example.apppagoflex_grupo36.data.local

import android.content.Context
import android.net.Uri
import java.io.File

object FotoPerfilStorage {

    private const val CARPETA = "perfil"

    private fun carpeta(context: Context): File =
        File(context.filesDir, CARPETA).apply { if (!exists()) mkdirs() }

    private fun archivosDe(context: Context, idUsuario: Long): List<File> =
        carpeta(context).listFiles { f -> f.isFile && f.name.startsWith("user_${idUsuario}_") }
            ?.toList()
            ?: emptyList()

    fun obtener(context: Context, idUsuario: Long): File? =
        archivosDe(context, idUsuario).maxByOrNull { it.lastModified() }

    fun obtenerUri(context: Context, idUsuario: Long): Uri? =
        obtener(context, idUsuario)?.let { Uri.fromFile(it) }

    fun guardar(context: Context, idUsuario: Long, origen: Uri): Uri? {
        val destino = File(carpeta(context), "user_${idUsuario}_${System.currentTimeMillis()}.jpg")
        return try {
            val entrada = context.contentResolver.openInputStream(origen) ?: return null
            entrada.use { input -> destino.outputStream().use { output -> input.copyTo(output) } }
            archivosDe(context, idUsuario).filter { it != destino }.forEach { it.delete() }
            Uri.fromFile(destino)
        } catch (e: Exception) {
            destino.delete()
            null
        }
    }
}
