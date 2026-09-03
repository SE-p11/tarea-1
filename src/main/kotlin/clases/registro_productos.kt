package org.example

import java.util.Scanner

// 1. CLASE MODELO
data class ProductoReg(
    val codigo: Int,
    var nombre: String,
    var precio: Double,
    var stock: Int
)

// 2. CLASE DE GESTIÓN (Maneja las operaciones sobre la lista en memoria)
class GestorProductos {
    private val listaProductos = mutableListOf<ProductoReg>()

    fun registrar(producto: ProductoReg): Boolean {
        return listaProductos.add(producto)
    }

    fun listar(): List<ProductoReg> {
        return listaProductos
    }

    fun buscarPorCodigo(codigo: Int): ProductoReg? {
        return listaProductos.find { it.codigo == codigo }
    }

    fun editar(codigo: Int, nuevoNombre: String, nuevoPrecio: Double, nuevoStock: Int): Boolean {
        val producto = buscarPorCodigo(codigo)
        return if (producto != null) {
            producto.nombre = nuevoNombre
            producto.precio = nuevoPrecio
            producto.stock = nuevoStock
            true
        } else {
            false
        }
    }

    fun eliminar(codigo: Int): Boolean {
        val producto = buscarPorCodigo(codigo)
        return if (producto != null) {
            listaProductos.remove(producto)
            true
        } else {
            false
        }
    }
}

// 3. CLASE DE INTERFAZ DE USUARIO POR CONSOLA
class MenuProductosApp {
    private val scanner = Scanner(System.`in`)
    private val gestor = GestorProductos()

    fun iniciar() {
        var continuar = true

        while (continuar) {
            println("\n----------------------------------")
            println("      REGISTRO DE PRODUCTOS       ")
            println("----------------------------------")
            println("1. Registrar producto(s)")
            println("2. Eliminar producto")
            println("3. Editar producto")
            println("4. Listar productos registrados")
            println("5. Salir")
            print("Selecciona una opción: ")

            when (scanner.nextLine().trim()) {
                "1" -> registrarProductos()
                "2" -> eliminarProducto()
                "3" -> editarProducto()
                "4" -> listarProductos()
                "5" -> {
                    println("\n¡Hasta luego!")
                    continuar = false
                }
                else -> println("\n⚠️ Opción no válida. Intenta de nuevo.")
            }
        }
    }

    private fun registrarProductos() {
        println("\n=== REGISTRAR PRODUCTOS ===")
        print("¿Cuántos productos deseas registrar?: ")
        val cantidad = scanner.nextLine().toIntOrNull() ?: 0

        if (cantidad <= 0) {
            println("⚠️ La cantidad debe ser mayor a 0.")
            return
        }

        for (i in 1..cantidad) {
            println("\n--- Producto $i de $cantidad ---")

            var codigo: Int? = null
            while (codigo == null) {
                print("Código: ")
                codigo = scanner.nextLine().toIntOrNull()
                if (codigo == null) {
                    println("⚠️ Debe ingresar un número entero válido para el código.")
                } else if (gestor.buscarPorCodigo(codigo) != null) {
                    println("⚠️ Ya existe un producto con el código $codigo. Ingresa otro.")
                    codigo = null
                }
            }

            print("Nombre: ")
            val nombre = scanner.nextLine().trim()

            print("Precio: ")
            val precio = scanner.nextLine().toDoubleOrNull() ?: 0.0

            print("Stock: ")
            val stock = scanner.nextLine().toIntOrNull() ?: 0

            val nuevoProducto = ProductoReg(codigo, nombre, precio, stock)
            gestor.registrar(nuevoProducto)
            println("✓ Producto '$nombre' registrado correctamente.")
        }

        println("\n✓ Se completó el registro de $cantidad producto(s).")
    }

    private fun eliminarProducto() {
        println("\n=== ELIMINAR PRODUCTO ===")
        if (gestor.listar().isEmpty()) {
            println("No hay productos registrados.")
            return
        }

        print("Ingresa el código del producto a eliminar: ")
        val codigo = scanner.nextLine().toIntOrNull() ?: return

        val producto = gestor.buscarPorCodigo(codigo)
        if (producto == null) {
            println("⚠️ No se encontró ningún producto con el código ingresado.")
            return
        }

        println("\n⚠️ ¡ADVERTENCIA!")
        print("¿Estás seguro de que deseas eliminar '${producto.nombre}' (Código: $codigo)? (S/N): ")
        val confirmacion = scanner.nextLine().trim()

        if (confirmacion.equals("S", ignoreCase = true)) {
            if (gestor.eliminar(codigo)) {
                println("✓ Producto eliminado correctamente.")
            }
        } else {
            println("Operación cancelada.")
        }
    }

    private fun editarProducto() {
        println("\n=== EDITAR PRODUCTO ===")
        if (gestor.listar().isEmpty()) {
            println("No hay productos registrados.")
            return
        }

        print("Ingresa el código del producto a editar: ")
        val codigo = scanner.nextLine().toIntOrNull() ?: return

        val producto = gestor.buscarPorCodigo(codigo)
        if (producto == null) {
            println("⚠️ No se encontró ningún producto con ese código.")
            return
        }

        print("Nuevo nombre (actual: ${producto.nombre}): ")
        val nuevoNombre = scanner.nextLine().trim().ifEmpty { producto.nombre }

        print("Nuevo precio (actual: S/ ${producto.precio}): ")
        val inputPrecio = scanner.nextLine().trim()
        val nuevoPrecio = if (inputPrecio.isEmpty()) producto.precio else inputPrecio.toDoubleOrNull() ?: producto.precio

        print("Nuevo stock (actual: ${producto.stock}): ")
        val inputStock = scanner.nextLine().trim()
        val nuevoStock = if (inputStock.isEmpty()) producto.stock else inputStock.toIntOrNull() ?: producto.stock

        if (gestor.editar(codigo, nuevoNombre, nuevoPrecio, nuevoStock)) {
            println("✓ Producto actualizado correctamente.")
        }
    }

    private fun listarProductos() {
        println("\n----------------------------------")
        println("       PRODUCTOS REGISTRADOS      ")
        println("----------------------------------")
        val productos = gestor.listar()

        if (productos.isEmpty()) {
            println("(No hay productos registrados aún)")
        } else {
            productos.forEachIndexed { i, p ->
                println("Producto ${i + 1}:")
                println("Código: ${p.codigo}")
                println("Nombre: ${p.nombre}")
                println("Precio: S/ ${p.precio}")
                println("Stock: ${p.stock}")
                println()
            }
        }
    }
}

// 4. MAIN
fun main() {
    val app = MenuProductosApp()
    app.iniciar()
}