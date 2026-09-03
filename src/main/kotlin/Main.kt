package org.example

import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException
import java.sql.Statement
import java.util.Scanner

// ==========================================
// 1. CLASES DE MODELO (ENTIDADES)
// ==========================================
data class Categoria(
    val id: Int = 0,
    val nombre: String
)

data class Producto(
    val id: Int = 0,
    val nombre: String,
    val precio: Double,
    val categoriaId: Int,
    val nombreCategoria: String = ""
)

// ==========================================
// 2. GESTOR DE CONEXIÓN Y BASE DE DATOS
// ==========================================
class DatabaseManager(private val dbPath: String) {

    fun getConnection(): Connection {
        return DriverManager.getConnection("jdbc:sqlite:$dbPath")
    }

    fun inicializarTablas() {
        val sqlCategorias = """
            CREATE TABLE IF NOT EXISTS categorias (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                nombre TEXT NOT NULL
            );
        """.trimIndent()

        val sqlProductos = """
            CREATE TABLE IF NOT EXISTS productos (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                nombre TEXT NOT NULL,
                precio REAL NOT NULL,
                categoria_id INTEGER,
                FOREIGN KEY (categoria_id) REFERENCES categorias(id) ON DELETE CASCADE
            );
        """.trimIndent()

        try {
            getConnection().use { conn ->
                conn.createStatement().use { stmt ->
                    stmt.execute("PRAGMA foreign_keys = ON;")
                    stmt.execute(sqlCategorias)
                    stmt.execute(sqlProductos)
                }
            }
        } catch (e: SQLException) {
            println("Error al inicializar la base de datos: ${e.message}")
        }
    }
}

// ==========================================
// 3. DAO DE CATEGORÍAS
// ==========================================
class CategoriaDAO(private val dbManager: DatabaseManager) {

    fun insertar(categoria: Categoria): Int {
        val sql = "INSERT INTO categorias (nombre) VALUES (?)"
        try {
            dbManager.getConnection().use { conn ->
                conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS).use { stmt ->
                    stmt.setString(1, categoria.nombre)
                    stmt.executeUpdate()
                    val keys = stmt.generatedKeys
                    if (keys.next()) return keys.getInt(1)
                }
            }
        } catch (e: SQLException) {
            println("Error al insertar categoría: ${e.message}")
        }
        return -1
    }

    fun actualizar(categoria: Categoria): Boolean {
        val sql = "UPDATE categorias SET nombre = ? WHERE id = ?"
        return try {
            dbManager.getConnection().use { conn ->
                conn.prepareStatement(sql).use { stmt ->
                    stmt.setString(1, categoria.nombre)
                    stmt.setInt(2, categoria.id)
                    stmt.executeUpdate() > 0
                }
            }
        } catch (e: SQLException) {
            println("Error al actualizar categoría: ${e.message}")
            false
        }
    }

    fun eliminar(id: Int): Boolean {
        val sql = "DELETE FROM categorias WHERE id = ?"
        return try {
            dbManager.getConnection().use { conn ->
                conn.prepareStatement(sql).use { stmt ->
                    stmt.setInt(1, id)
                    stmt.executeUpdate() > 0
                }
            }
        } catch (e: SQLException) {
            println("Error al eliminar categoría: ${e.message}")
            false
        }
    }

    fun obtenerTodas(): List<Categoria> {
        val lista = mutableListOf<Categoria>()
        val sql = "SELECT id, nombre FROM categorias"
        try {
            dbManager.getConnection().use { conn ->
                conn.createStatement().use { stmt ->
                    val rs = stmt.executeQuery(sql)
                    while (rs.next()) {
                        lista.add(Categoria(rs.getInt("id"), rs.getString("nombre")))
                    }
                }
            }
        } catch (e: SQLException) {
            println("Error al consultar categorías: ${e.message}")
        }
        return lista
    }

    fun contarProductosAsociados(categoriaId: Int): Int {
        val sql = "SELECT COUNT(*) FROM productos WHERE categoria_id = ?"
        try {
            dbManager.getConnection().use { conn ->
                conn.prepareStatement(sql).use { stmt ->
                    stmt.setInt(1, categoriaId)
                    val rs = stmt.executeQuery()
                    if (rs.next()) return rs.getInt(1)
                }
            }
        } catch (e: SQLException) {
            println("Error al verificar productos asociados: ${e.message}")
        }
        return 0
    }
}

// ==========================================
// 4. DAO DE PRODUCTOS
// ==========================================
class ProductoDAO(private val dbManager: DatabaseManager) {

    fun insertar(producto: Producto): Boolean {
        val sql = "INSERT INTO productos (nombre, precio, categoria_id) VALUES (?, ?, ?)"
        return try {
            dbManager.getConnection().use { conn ->
                conn.prepareStatement(sql).use { stmt ->
                    stmt.setString(1, producto.nombre)
                    stmt.setDouble(2, producto.precio)
                    stmt.setInt(3, producto.categoriaId)
                    stmt.executeUpdate() > 0
                }
            }
        } catch (e: SQLException) {
            println("Error al insertar producto: ${e.message}")
            false
        }
    }

    fun actualizar(producto: Producto): Boolean {
        val sql = "UPDATE productos SET nombre = ?, precio = ?, categoria_id = ? WHERE id = ?"
        return try {
            dbManager.getConnection().use { conn ->
                conn.prepareStatement(sql).use { stmt ->
                    stmt.setString(1, producto.nombre)
                    stmt.setDouble(2, producto.precio)
                    stmt.setInt(3, producto.categoriaId)
                    stmt.setInt(4, producto.id)
                    stmt.executeUpdate() > 0
                }
            }
        } catch (e: SQLException) {
            println("Error al actualizar producto: ${e.message}")
            false
        }
    }

    fun eliminar(id: Int): Boolean {
        val sql = "DELETE FROM productos WHERE id = ?"
        return try {
            dbManager.getConnection().use { conn ->
                conn.prepareStatement(sql).use { stmt ->
                    stmt.setInt(1, id)
                    stmt.executeUpdate() > 0
                }
            }
        } catch (e: SQLException) {
            println("Error al eliminar producto: ${e.message}")
            false
        }
    }

    fun obtenerTodosConCategoria(): List<Producto> {
        val lista = mutableListOf<Producto>()
        val sql = """
            SELECT p.id, p.nombre AS producto, p.precio, p.categoria_id, c.nombre AS categoria
            FROM productos p
            JOIN categorias c ON p.categoria_id = c.id
        """.trimIndent()

        try {
            dbManager.getConnection().use { conn ->
                conn.createStatement().use { stmt ->
                    val rs = stmt.executeQuery(sql)
                    while (rs.next()) {
                        lista.add(
                            Producto(
                                id = rs.getInt("id"),
                                nombre = rs.getString("producto"),
                                precio = rs.getDouble("precio"),
                                categoriaId = rs.getInt("categoria_id"),
                                nombreCategoria = rs.getString("categoria")
                            )
                        )
                    }
                }
            }
        } catch (e: SQLException) {
            println("Error al consultar productos: ${e.message}")
        }
        return lista
    }
}

// ==========================================
// 5. CONTROLADOR DEL MENÚ DE CONSOLA
// ==========================================
class MenuApp(
    private val categoriaDAO: CategoriaDAO,
    private val productoDAO: ProductoDAO
) {
    private val scanner = Scanner(System.`in`)

    fun iniciar() {
        var continuar = true

        while (continuar) {
            println("\n------------------------------------------")
            println("              MENÚ PRINCIPAL              ")
            println("------------------------------------------")

            println("--- GESTIÓN DE CATEGORÍAS ---")
            println("1. Registrar Categoría")
            println("2. Editar Categoría")
            println("3. Eliminar Categoría")

            println("\n--- GESTIÓN DE PRODUCTOS ---")
            println("4. Registrar Producto")
            println("5. Listar Productos")
            println("6. Editar Producto")
            println("7. Eliminar Producto")

            println("\n--- GENERAL ---")
            println("8. Salir")
            println("------------------------------------------")
            print("Selecciona una opción: ")

            when (scanner.nextLine().trim()) {
                "1" -> procesarNuevaCategoria()
                "2" -> editarCategoria()
                "3" -> eliminarCategoria()
                "4" -> procesarNuevoProducto()
                "5" -> mostrarProductos()
                "6" -> editarProducto()
                "7" -> eliminarProducto()
                "8" -> {
                    println("\n¡Gracias por usar el sistema! Hasta luego.")
                    continuar = false
                }
                else -> println("\n⚠️ Opción no válida. Intenta de nuevo.")
            }
        }
    }

    private fun procesarNuevaCategoria(): Int {
        println("\n=== REGISTRAR NUEVA CATEGORÍA ===")
        print("Ingrese el nombre de la categoría: ")
        val nombre = scanner.nextLine().trim()

        if (nombre.isEmpty()) {
            println("El nombre no puede estar vacío.")
            return -1
        }

        val idGenerado = categoriaDAO.insertar(Categoria(nombre = nombre))
        if (idGenerado != -1) {
            println("✓ Categoría '$nombre' registrada con éxito (ID: $idGenerado).")
        }
        return idGenerado
    }

    private fun procesarNuevoProducto() {
        println("\n=== REGISTRAR NUEVO PRODUCTO ===")
        print("Ingrese el nombre del producto: ")
        val nombre = scanner.nextLine().trim()

        print("Ingrese el precio del producto: ")
        val precio = scanner.nextLine().toDoubleOrNull() ?: 0.0

        val categoriaId = seleccionarOCrearCategoria()

        if (productoDAO.insertar(Producto(nombre = nombre, precio = precio, categoriaId = categoriaId))) {
            println("✓ Producto '$nombre' registrado con éxito.")
        }
    }

    private fun editarCategoria() {
        println("\n=== EDITAR CATEGORÍA ===")
        val categorias = mostrarCategorias()
        if (categorias.isEmpty()) return

        print("\nIngresa el ID de la categoría a editar: ")
        val id = scanner.nextLine().toIntOrNull() ?: return

        if (categorias.none { it.id == id }) {
            println("⚠️ El ID ingresado no existe.")
            return
        }

        print("Ingrese el nuevo nombre para la categoría: ")
        val nuevoNombre = scanner.nextLine().trim()

        if (categoriaDAO.actualizar(Categoria(id = id, nombre = nuevoNombre))) {
            println("✓ Categoría actualizada correctamente.")
        }
    }

    private fun eliminarCategoria() {
        println("\n=== ELIMINAR CATEGORÍA ===")
        val categorias = mostrarCategorias()
        if (categorias.isEmpty()) return

        print("\nIngresa el ID de la categoría a eliminar: ")
        val id = scanner.nextLine().toIntOrNull() ?: return

        val catSeleccionada = categorias.find { it.id == id } ?: run {
            println("⚠️ El ID ingresado no existe.")
            return
        }

        val cantidadProductos = categoriaDAO.contarProductosAsociados(id)

        println("\n⚠️ ¡ADVERTENCIA DE ELIMINACIÓN!")
        if (cantidadProductos > 0) {
            println("La categoría '${catSeleccionada.nombre}' tiene $cantidadProductos producto(s) vinculado(s).")
            println("Si la eliminas, ¡TAMBIÉN SE ELIMINARÁN ESOS $cantidadProductos PRODUCTOS!")
        } else {
            println("Vas a eliminar la categoría '${catSeleccionada.nombre}'.")
        }

        print("¿Estás seguro de que deseas continuar? (S/N): ")
        val confirmacion = scanner.nextLine().trim()

        if (confirmacion.equals("S", ignoreCase = true)) {
            if (categoriaDAO.eliminar(id)) {
                println("✓ Categoría '${catSeleccionada.nombre}' eliminada con éxito.")
            }
        } else {
            println("Operación cancelada.")
        }
    }

    private fun editarProducto() {
        println("\n=== EDITAR PRODUCTO ===")
        val productos = productoDAO.obtenerTodosConCategoria()
        if (productos.isEmpty()) {
            println("No hay productos registrados para editar.")
            return
        }

        mostrarProductos()
        print("\nIngresa el ID del producto a editar: ")
        val id = scanner.nextLine().toIntOrNull() ?: return

        val prodActual = productos.find { it.id == id } ?: run {
            println("⚠️ El ID ingresado no existe.")
            return
        }

        print("Nuevo nombre (actual: ${prodActual.nombre}): ")
        val nuevoNombre = scanner.nextLine().trim().ifEmpty { prodActual.nombre }

        print("Nuevo precio (actual: S/ ${prodActual.precio}): ")
        val inputPrecio = scanner.nextLine().trim()
        val nuevoPrecio = if (inputPrecio.isEmpty()) prodActual.precio else inputPrecio.toDoubleOrNull() ?: prodActual.precio

        print("¿Deseas cambiar la categoría del producto? (S/N): ")
        val cambiarCat = scanner.nextLine().trim()

        val nuevaCategoriaId = if (cambiarCat.equals("S", ignoreCase = true)) {
            seleccionarOCrearCategoria()
        } else {
            prodActual.categoriaId
        }

        val prodEditado = Producto(id = id, nombre = nuevoNombre, precio = nuevoPrecio, categoriaId = nuevaCategoriaId)
        if (productoDAO.actualizar(prodEditado)) {
            println("✓ Producto '$nuevoNombre' actualizado con éxito.")
        }
    }

    private fun eliminarProducto() {
        println("\n=== ELIMINAR PRODUCTO ===")
        val productos = productoDAO.obtenerTodosConCategoria()
        if (productos.isEmpty()) {
            println("No hay productos registrados.")
            return
        }

        mostrarProductos()
        print("\nIngresa el ID del producto a eliminar: ")
        val id = scanner.nextLine().toIntOrNull() ?: return

        val prodSeleccionado = productos.find { it.id == id } ?: run {
            println("⚠️ El ID ingresado no existe.")
            return
        }

        println("\n⚠️ ¡ADVERTENCIA!")
        println("Estás a punto de eliminar permanentemente el producto: '${prodSeleccionado.nombre}' (Precio: S/ ${prodSeleccionado.precio}).")
        print("¿Deseas confirmar la eliminación? (S/N): ")

        if (scanner.nextLine().trim().equals("S", ignoreCase = true)) {
            if (productoDAO.eliminar(id)) {
                println("✓ Producto eliminado correctamente.")
            }
        } else {
            println("Operación cancelada.")
        }
    }

    private fun seleccionarOCrearCategoria(): Int {
        println("\n--- ASIGNAR CATEGORÍA ---")
        println("1. Seleccionar una categoría existente")
        println("2. Crear una nueva categoría")
        print("Selecciona una opción: ")

        return if (scanner.nextLine().trim() == "1") {
            val categorias = mostrarCategorias()
            if (categorias.isEmpty()) {
                println("No existen categorías. Se creará una nueva.")
                procesarNuevaCategoria()
            } else {
                var catId = -1
                val idsValidos = categorias.map { it.id }
                while (!idsValidos.contains(catId)) {
                    print("Ingresa el ID de la categoría a asignar: ")
                    catId = scanner.nextLine().toIntOrNull() ?: -1
                    if (!idsValidos.contains(catId)) println("⚠️ ID inválido. Intenta con uno de la lista.")
                }
                catId
            }
        } else {
            procesarNuevaCategoria()
        }
    }

    private fun mostrarCategorias(): List<Categoria> {
        val categorias = categoriaDAO.obtenerTodas()
        println("\n--- CATEGORÍAS REGISTRADAS ---")
        if (categorias.isEmpty()) {
            println("(No hay categorías registradas)")
        } else {
            categorias.forEach { println("ID: ${it.id} | Categoría: ${it.nombre}") }
        }
        return categorias
    }

    private fun mostrarProductos() {
        println("\n--- LISTADO DE PRODUCTOS EN TIENDA ---")
        val productos = productoDAO.obtenerTodosConCategoria()

        if (productos.isEmpty()) {
            println("(No hay productos registrados en la base de datos)")
        } else {
            productos.forEach { p ->
                println("ID: ${p.id} | Producto: ${p.nombre} | Precio: S/ ${p.precio} | Categoría: ${p.nombreCategoria}")
            }
        }
    }
}

// ==========================================
// 6. MAIN
// ==========================================
fun main() {
    val dbManager = DatabaseManager("identifier.sqlite")
    dbManager.inicializarTablas()

    val categoriaDAO = CategoriaDAO(dbManager)
    val productoDAO = ProductoDAO(dbManager)

    val app = MenuApp(categoriaDAO, productoDAO)
    app.iniciar()
}