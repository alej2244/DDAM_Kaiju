package com.example.kaijuinventory

import com.example.kaijuinventory.model.TipoMovimiento
import com.example.kaijuinventory.validation.MovimientoValidator
import com.example.kaijuinventory.validation.ProductoValidator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class ProductoValidatorTest {

    @Test
    fun codigo_valido_y_invalido() {
        assertNull(ProductoValidator.codigo("BEB-002"))
        assertNotNull(ProductoValidator.codigo(""))
        assertNotNull(ProductoValidator.codigo("AB"))
        assertNotNull(ProductoValidator.codigo("COD 01"))
    }

    @Test
    fun nombre_requiere_largo_minimo() {
        assertNotNull(ProductoValidator.nombre("  "))
        assertNotNull(ProductoValidator.nombre("Té"))
        assertNull(ProductoValidator.nombre("Café molido"))
    }

    @Test
    fun precio_debe_ser_entero_positivo() {
        assertNull(ProductoValidator.precio("1490"))
        assertNotNull(ProductoValidator.precio("0"))
        assertNotNull(ProductoValidator.precio("-5"))
        assertNotNull(ProductoValidator.precio("12,5"))
        assertNotNull(ProductoValidator.precio(""))
    }

    @Test
    fun stock_no_negativo() {
        assertNull(ProductoValidator.stock("0"))
        assertNotNull(ProductoValidator.stock("-1"))
        assertNotNull(ProductoValidator.stockMinimo("abc"))
    }

    @Test
    fun categoria_debe_existir() {
        assertNull(ProductoValidator.categoria("Bebidas"))
        assertNotNull(ProductoValidator.categoria(""))
        assertNotNull(ProductoValidator.categoria("Juguetes"))
    }
}

class MovimientoValidatorTest {

    @Test
    fun venta_no_puede_superar_stock() {
        assertNotNull(MovimientoValidator.cantidad("11", TipoMovimiento.VENTA, 10))
        assertNull(MovimientoValidator.cantidad("10", TipoMovimiento.VENTA, 10))
    }

    @Test
    fun entrada_debe_ser_mayor_a_cero() {
        assertNotNull(MovimientoValidator.cantidad("0", TipoMovimiento.ENTRADA, 5))
        assertNull(MovimientoValidator.cantidad("3", TipoMovimiento.ENTRADA, 5))
    }

    @Test
    fun ajuste_requiere_motivo_y_valor_distinto() {
        assertNotNull(MovimientoValidator.cantidad("5", TipoMovimiento.AJUSTE, 5))
        assertNull(MovimientoValidator.cantidad("0", TipoMovimiento.AJUSTE, 5))
        assertNotNull(MovimientoValidator.nota("", TipoMovimiento.AJUSTE))
        assertNull(MovimientoValidator.nota("", TipoMovimiento.VENTA))
    }

    @Test
    fun tipo_calcula_stock_resultante() {
        assertEquals(15, TipoMovimiento.ENTRADA.aplicar(10, 5))
        assertEquals(5, TipoMovimiento.VENTA.aplicar(10, 5))
        assertEquals(7, TipoMovimiento.AJUSTE.aplicar(10, 7))
    }
}
