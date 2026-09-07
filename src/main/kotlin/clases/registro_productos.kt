package org.example.clases

import java.util.Scanner

// 1. CLASE MODELO
// Data class que representa la estructura de un Producto (código único, nombre, precio y stock disponible).
data class ProductoReg(
    val codigo: Int,    // Código identificador único del producto (no modificable).
    var nombre: String, // Nombre o descripción del producto.
    var precio: Double, // Precio unitario del producto.
    var stock: Int      // Cantidad de unidades disponibles en inventario.
)

// 2. CLASE DE GESTIÓN (Maneja las operaciones sobre la lista en memoria)
// Administra las operaciones CRUD (Crear, Leer, Actualizar y Eliminar) sobre la colección de productos.
class GestorProductos {
    // Lista mutable interna para almacenar los productos creados durante la ejecución.
    private val listaProductos = mutableListOf<ProductoReg>()

    // Registra un nuevo producto añadiéndolo a la lista.
    fun registrar(producto: ProductoReg): Boolean {
        return listaProductos.add(producto) // Retorna true si la adición fue exitosa.
    }

    // Devuelve la lista completa de productos almacenados.
    fun listar(): List<ProductoReg> {
        return listaProductos
    }

    // Busca un producto por su código numérico; retorna el objeto o null si no existe.
    fun buscarPorCodigo(codigo: Int): ProductoReg? {
        return listaProductos.find { it.codigo == codigo }
    }

    // Edita los datos de un producto existente identificado por su código.
    fun editar(codigo: Int, nuevoNombre: String, nuevoPrecio: Double, nuevoStock: Int): Boolean {
        val producto = buscarPorCodigo(codigo) // Ubica el producto en la lista.
        return if (producto != null) {
            producto.nombre = nuevoNombre // Asigna el nuevo nombre.
            producto.precio = nuevoPrecio // Asigna el nuevo precio.
            producto.stock = nuevoStock   // Asigna el nuevo stock.
            true                           // Retorna true indicando actualización exitosa.
        } else {
            false                          // Retorna false si el producto no fue hallado.
        }
    }

    // Elimina un producto de la lista según su código.
    fun eliminar(codigo: Int): Boolean {
        val producto = buscarPorCodigo(codigo) // Busca el producto antes de removerlo.
        return if (producto != null) {
            listaProductos.remove(producto) // Elimina la instancia de la lista.
            true                            // Confirma que se eliminó correctamente.
        } else {
            false                           // Retorna false si no existía dicho código.
        }
    }
}

// 3. CLASE DE INTERFAZ DE USUARIO POR CONSOLA
// Controla la lectura de entradas del usuario y el flujo de navegación mediante menús.
class MenuProductosApp {
    // Lector de datos desde la consola de entrada estándar.
    private val scanner = Scanner(System.`in`)
    // Instancia del gestor para ejecutar la lógica sobre la lista de productos.
    private val gestor = GestorProductos()

    // Bucle principal que despliega las opciones del sistema de productos.
    fun iniciar() {
        var continuar = true // Controla la continuidad del ciclo interactivo.

        while (continuar) {
            // Opciones de consola presentadas al usuario.
            println("\n----------------------------------")
            println("      REGISTRO DE PRODUCTOS       ")
            println("----------------------------------")
            println("1. Registrar producto(s)")
            println("2. Eliminar producto")
            println("3. Editar producto")
            println("4. Listar productos registrados")
            println("5. Salir")
            print("Selecciona una opción: ")

            // Redirecciona la ejecución según la opción elegida por el usuario.
            when (scanner.nextLine().trim()) {
                "1" -> registrarProductos()
                "2" -> eliminarProducto()
                "3" -> editarProducto()
                "4" -> listarProductos()
                "5" -> {
                    println("\n¡Hasta luego!")
                    continuar = false // Finaliza el bucle while.
                }
                else -> println("\n⚠️ Opción no válida. Intenta de nuevo.")
            }
        }
    }

    // Solicita los datos de cada producto y los registra en el gestor.
    private fun registrarProductos() {
        println("\n=== REGISTRAR PRODUCTOS ===")
        print("¿Cuántos productos deseas registrar?: ")
        // Convierte a Int; si no es número o está vacío, asigna 0 por defecto.
        val cantidad = scanner.nextLine().toIntOrNull() ?: 0

        // Valida que el número ingresado sea válido para iniciar el registro.
        if (cantidad <= 0) {
            println("⚠️ La cantidad debe ser mayor a 0.")
            return
        }

        // Bucle para iterar según la cantidad de productos a registrar.
        for (i in 1..cantidad) {
            println("\n--- Producto $i de $cantidad ---")

            var codigo: Int? = null
            // Bucle de validación para garantizar un código numérico válido y único.
            while (codigo == null) {
                print("Código: ")
                codigo = scanner.nextLine().toIntOrNull()
                if (codigo == null) {
                    println("⚠️ Debe ingresar un número entero válido para el código.")
                } else if (gestor.buscarPorCodigo(codigo) != null) {
                    println("⚠️ Ya existe un producto con el código $codigo. Ingresa otro.")
                    codigo = null // Reinicia para volver a solicitar la entrada.
                }
            }

            print("Nombre: ")
            val nombre = scanner.nextLine().trim() // Captura y limpia el nombre.

            print("Precio: ")
            // Lee el precio como valor decimal (Double); si falla, usa 0.0 por defecto.
            val precio = scanner.nextLine().toDoubleOrNull() ?: 0.0

            print("Stock: ")
            // Lee el stock disponible como entero (Int); si falla, asigna 0.
            val stock = scanner.nextLine().toIntOrNull() ?: 0

            // Crea la instancia de ProductoReg y la envía a la capa del gestor.
            val nuevoProducto = ProductoReg(codigo, nombre, precio, stock)
            gestor.registrar(nuevoProducto)
            println("✓ Producto '$nombre' registrado correctamente.")
        }

        println("\n✓ Se completó el registro de $cantidad producto(s).")
    }

    // Solicita el código de un producto y procede a eliminarlo tras confirmación.
    private fun eliminarProducto() {
        println("\n=== ELIMINAR PRODUCTO ===")
        // Verifica que existan registros guardados antes de solicitar datos.
        if (gestor.listar().isEmpty()) {
            println("No hay productos registrados.")
            return
        }

        print("Ingresa el código del producto a eliminar: ")
        val codigo = scanner.nextLine().toIntOrNull() ?: return

        // Verifica si el producto existe en el sistema.
        val producto = gestor.buscarPorCodigo(codigo)
        if (producto == null) {
            println("⚠️ No se encontró ningún producto con el código ingresado.")
            return
        }

        // Solicita confirmación antes de proceder con el borrado.
        println("\n⚠️ ¡ADVERTENCIA!")
        print("¿Estás seguro de que deseas eliminar '${producto.nombre}' (Código: $codigo)? (S/N): ")
        val confirmacion = scanner.nextLine().trim()

        // Si confirma con "S" o "s", se elimina el producto de la lista.
        if (confirmacion.equals("S", ignoreCase = true)) {
            if (gestor.eliminar(codigo)) {
                println("✓ Producto eliminado correctamente.")
            }
        } else {
            println("Operación cancelada.")
        }
    }

    // Permite actualizar los valores de nombre, precio o stock de un producto existente.
    private fun editarProducto() {
        println("\n=== EDITAR PRODUCTO ===")
        // Valida la existencia de productos en el inventario.
        if (gestor.listar().isEmpty()) {
            println("No hay productos registrados.")
            return
        }

        print("Ingresa el código del producto a editar: ")
        val codigo = scanner.nextLine().toIntOrNull() ?: return

        // Comprueba si el código corresponde a un producto registrado.
        val producto = gestor.buscarPorCodigo(codigo)
        if (producto == null) {
            println("⚠️ No se encontró ningún producto con ese código.")
            return
        }

        // Lee el nuevo nombre; si se deja en blanco, conserva el actual.
        print("Nuevo nombre (actual: ${producto.nombre}): ")
        val nuevoNombre = scanner.nextLine().trim().ifEmpty { producto.nombre }

        // Lee el nuevo precio; si está vacío o no es válido, mantiene el precio actual.
        print("Nuevo precio (actual: S/ ${producto.precio}): ")
        val inputPrecio = scanner.nextLine().trim()
        val nuevoPrecio = if (inputPrecio.isEmpty()) producto.precio else inputPrecio.toDoubleOrNull() ?: producto.precio

        // Lee el nuevo stock; si está vacío o no es válido, mantiene el stock actual.
        print("Nuevo stock (actual: ${producto.stock}): ")
        val inputStock = scanner.nextLine().trim()
        val nuevoStock = if (inputStock.isEmpty()) producto.stock else inputStock.toIntOrNull() ?: producto.stock

        // Ejecuta la actualización de datos en el gestor.
        if (gestor.editar(codigo, nuevoNombre, nuevoPrecio, nuevoStock)) {
            println("✓ Producto actualizado correctamente.")
        }
    }

    // Muestra en pantalla el listado de todos los productos y sus atributos.
    private fun listarProductos() {
        println("\n----------------------------------")
        println("       PRODUCTOS REGISTRADOS      ")
        println("----------------------------------")
        val productos = gestor.listar()

        // Evalúa si la lista contiene elementos para imprimir.
        if (productos.isEmpty()) {
            println("(No hay productos registrados aún)")
        } else {
            // Recorre e imprime la información completa de cada producto registrado.
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
// Función principal que actúa como punto de arranque del programa.
fun main() {
    val app = MenuProductosApp() // Instancia la interfaz del menú de productos.
    app.iniciar()                // Llama al método para iniciar la ejecución.
}