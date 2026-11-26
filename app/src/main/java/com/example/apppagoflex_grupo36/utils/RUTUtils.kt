package com.example.apppagoflex_grupo36.utils

object RUTUtils {

    fun cleanRut(rut: String): String {
        return rut.replace(".", "")
            .replace("-", "")
            .replace(" ", "")
            .trim()
            .uppercase()
    }

    fun validateRut(rut: String?): Boolean {
        if (rut.isNullOrBlank()) return false
        val cleaned = cleanRut(rut)
        if (cleaned.length < 8 || cleaned.length > 9) return false

        val body = cleaned.substring(0, cleaned.length - 1)
        val dv = cleaned.last()

        if (!body.all { it.isDigit() }) return false

        val calculatedDv = calculateDV(body)
        return dv == calculatedDv
    }

    fun calculateDV(rutBody: String): Char {
        var sum = 0
        var multiplier = 2

        for (i in rutBody.length - 1 downTo 0) {
            val digit = rutBody[i].digitToInt()
            sum += digit * multiplier
            multiplier = if (multiplier == 7) 2 else multiplier + 1
        }

        val remainder = sum % 11
        val expected = 11 - remainder

        return when (expected) {
            11 -> '0'
            10 -> 'K'
            else -> expected.digitToChar()
        }
    }

    fun formatRut(rut: String): String {
        val cleaned = cleanRut(rut)
        if (cleaned.length < 2) return rut

        val body = cleaned.substring(0, cleaned.length - 1)
        val dv = cleaned.last()

        val formattedBody = StringBuilder()
        var count = 0
        for (i in body.length - 1 downTo 0) {
            formattedBody.insert(0, body[i])
            count++
            if (count % 3 == 0 && i != 0) {
                formattedBody.insert(0, '.')
            }
        }

        return "$formattedBody-$dv"
    }

    fun maskRut(rut: String): String {
        val cleaned = cleanRut(rut)
        if (cleaned.length < 8) return rut

        val body = cleaned.substring(0, cleaned.length - 1)
        val dv = cleaned.last()

        val prefixLength = if (body.length >= 8) 2 else 1
        val prefix = body.substring(0, prefixLength)

        return "$prefix.*.*-$dv"
    }
}
