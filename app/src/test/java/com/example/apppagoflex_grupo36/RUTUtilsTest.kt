package com.example.apppagoflex_grupo36.utils

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class RUTUtilsTest : FunSpec({

    test("cleanRut elimina puntos, guiones, espacios y pasa a mayúsculas") {
        RUTUtils.cleanRut(" 12.345.678-k ") shouldBe "12345678K"
        RUTUtils.cleanRut("11.111.111-1") shouldBe "111111111"
    }

    test("calculateDV calcula correctamente dígitos numéricos, 0 y K") {
        RUTUtils.calculateDV("12345678") shouldBe '5'
        RUTUtils.calculateDV("11111111") shouldBe '1'
        RUTUtils.calculateDV("16000004") shouldBe 'K'
        RUTUtils.calculateDV("10000004") shouldBe '0'
    }

    test("validateRut retorna true para RUTs válidos con y sin formato") {
        RUTUtils.validateRut("12.345.678-5") shouldBe true
        RUTUtils.validateRut("123456785") shouldBe true
        RUTUtils.validateRut("16.000.004-k") shouldBe true
        RUTUtils.validateRut("9.876.543-3") shouldBe true
    }

    test("validateRut retorna false para RUTs nulos, vacíos, cortos, largos o con DV incorrecto") {
        RUTUtils.validateRut(null) shouldBe false
        RUTUtils.validateRut("") shouldBe false
        RUTUtils.validateRut("   ") shouldBe false
        RUTUtils.validateRut("123456") shouldBe false
        RUTUtils.validateRut("1234567890") shouldBe false
        RUTUtils.validateRut("12.345.67A-5") shouldBe false
        RUTUtils.validateRut("12.345.678-9") shouldBe false
    }

    test("formatRut formatea correctamente RUTs de 7 y 8 dígitos y retorna original si es muy corto") {
        RUTUtils.formatRut("123456785") shouldBe "12.345.678-5"
        RUTUtils.formatRut("98765433") shouldBe "9.876.543-3"
        RUTUtils.formatRut("1") shouldBe "1"
    }

    test("maskRut enmascara correctamente el cuerpo del RUT") {
        RUTUtils.maskRut("12.345.678-5") shouldBe "12.*.*-5"
        RUTUtils.maskRut("9.876.543-3") shouldBe "9.*.*-3"
        RUTUtils.maskRut("12345") shouldBe "12345"
    }
})