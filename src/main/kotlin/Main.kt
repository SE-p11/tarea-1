package org.example

// Importación de librerías necesarias para el manejo de bases de datos SQLite y lectura de consola
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException
import java.sql.Statement
import java.util.Scanner

// ==========================================
// 1. CLASES DE MODELO (ENTIDADES)
// ==========================================

// Data class que representa la estructura de la entidad 'Categoria' en la base de datos
data class Categoria(
    val id: Int = 0, // Identificador único de la categoría (clave primaria autoincrementable)
    val nombre: String // Nombre descriptivo de la categoría
)

// Data class que representa la estructura de la entidad 'Producto'
data class Producto(
    val id: Int = 0, // Identificador único del producto
    val nombre: String, // Nombre del producto
    val precio: Double, // Precio unitario del producto
    val categoriaId: Int, // Clave foránea que referencia al ID de la categoría a la que pertenece
    val nombreCategoria: String = "" // Propiedad auxiliar para almacenar el nombre de la categoría al realizar JOINs
)

// ==========================================
// 2. GESTOR DE CONEXIÓN Y BASE DE DATOS
// ==========================================

// Clase encargada de administrar la conexión a la base de datos SQLite y la creación de su esquema
class DatabaseManager(private val dbPath: String) {

    // Método que abre y retorna una nueva conexión activa con el archivo de la base de datos
    fun getConnection(): Connection {
        return DriverManager.getConnection("jdbc:sqlite:$dbPath")
    }

    // Método encargado de crear las tablas en la base de datos si aún no existen
    fun inicializarTablas() {
        // Sentencia SQL para crear la tabla de categorías
        val sqlCategorias = """
            CREATE TABLE IF NOT EXISTS categorias (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                nombre TEXT NOT NULL
            );
        """.trimIndent()

        // Sentencia SQL para crear la tabla de productos vinculada mediante una clave foránea a categorías
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
            // Se obtiene la conexión y se ejecutan las sentencias SQL dentro de un bloque 'use' para asegurar el cierre de recursos
            getConnection().use { conn ->
                conn.createStatement().use { stmt ->
                    stmt.execute("PRAGMA foreign_keys = ON;") // Activa el soporte de claves foráneas en SQLite (para eliminación en cascada)
                    stmt.execute(sqlCategorias) // Crea la tabla categorias
                    stmt.execute(sqlProductos)  // Crea la tabla productos
                }
            }
        } catch (e: SQLException) {
            // Captura e informa cualquier error durante la inicialización de la BD
            println("Error al inicializar la base de datos: ${e.message}")
        }
    }
}

// ==========================================
// 3. DAO DE CATEGORÍAS
// ==========================================

// Objeto de Acceso a Datos (DAO) para realizar operaciones CRUD sobre la tabla 'categorias'
class CategoriaDAO(private val dbManager: DatabaseManager) {

    // Registra una nueva categoría en la BD y retorna el ID autogenerado
    fun insertar(categoria: Categoria): Int {
        val sql = "INSERT INTO categorias (nombre) VALUES (?)"
        try {
            dbManager.getConnection().use { conn ->
                // Pide retornar las claves generadas (IDs) al insertar
                conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS).use { stmt ->
                    stmt.setString(1, categoria.nombre) // Asigna el nombre al parámetro SQL
                    stmt.executeUpdate()
                    val keys = stmt.generatedKeys
                    if (keys.next()) return keys.getInt(1) // Retorna el ID generado para la nueva categoría
                }
            }
        } catch (e: SQLException) {
            println("Error al insertar categoría: ${e.message}")
        }
        return -1 // Retorna -1 si ocurre algún fallo en la inserción
    }

    // Modifica el nombre de una categoría existente según su ID
    fun actualizar(categoria: Categoria): Boolean {
        val sql = "UPDATE categorias SET nombre = ? WHERE id = ?"
        return try {
            dbManager.getConnection().use { conn ->
                conn.prepareStatement(sql).use { stmt ->
                    stmt.setString(1, categoria.nombre) // Nuevo nombre
                    stmt.setInt(2, categoria.id)        // ID a buscar
                    stmt.executeUpdate() > 0             // Retorna verdadero si se afectó al menos una fila
                }
            }
        } catch (e: SQLException) {
            println("Error al actualizar categoría: ${e.message}")
            false
        }
    }

    // Elimina una categoría por su ID
    fun eliminar(id: Int): Boolean {
        val sql = "DELETE FROM categorias WHERE id = ?"
        return try {
            dbManager.getConnection().use { conn ->
                conn.prepareStatement(sql).use { stmt ->
                    stmt.setInt(1, id) // ID de la categoría a eliminar
                    stmt.executeUpdate() > 0 // Retorna verdadero si la eliminación fue exitosa
                }
            }
        } catch (e: SQLException) {
            println("Error al eliminar categoría: ${e.message}")
            false
        }
    }

    // Obtiene y retorna el listado completo de categorías guardadas
    fun obtenerTodas(): List<Categoria> {
        val lista = mutableListOf<Categoria>()
        val sql = "SELECT id, nombre FROM categorias"
        try {
            dbManager.getConnection().use { conn ->
                conn.createStatement().use { stmt ->
                    val rs = stmt.executeQuery(sql) // Ejecuta la consulta de selección
                    while (rs.next()) {
                        // Construye objetos Categoria y los añade a la lista
                        lista.add(Categoria(rs.getInt("id"), rs.getString("nombre")))
                    }
                }
            }
        } catch (e: SQLException) {
            println("Error al consultar categorías: ${e.message}")
        }
        return lista // Retorna la lista de categorías
    }

    // Cuenta cuántos productos están asociados a una categoría específica
    fun contarProductosAsociados(categoriaId: Int): Int {
        val sql = "SELECT COUNT(*) FROM productos WHERE categoria_id = ?"
        try {
            dbManager.getConnection().use { conn ->
                conn.prepareStatement(sql).use { stmt ->
                    stmt.setInt(1, categoriaId)
                    val rs = stmt.executeQuery()
                    if (rs.next()) return rs.getInt(1) // Retorna el número de productos encontrados
                }
            }
        } catch (e: SQLException) {
            println("Error al verificar productos asociados: ${e.message}")
        }
        return 0 // Retorna 0 si no hay vinculados o si ocurre un error
    }
}

// ==========================================
// 4. DAO DE PRODUCTOS
// ==========================================

// Objeto de Acceso a Datos (DAO) para realizar operaciones CRUD sobre la tabla 'productos'
class ProductoDAO(private val dbManager: DatabaseManager) {

    // Registra un nuevo producto en la base de datos
    fun insertar(producto: Producto): Boolean {
        val sql = "INSERT INTO productos (nombre, precio, categoria_id) VALUES (?, ?, ?)"
        return try {
            dbManager.getConnection().use { conn ->
                conn.prepareStatement(sql).use { stmt ->
                    stmt.setString(1, producto.nombre)       // Asigna nombre
                    stmt.setDouble(2, producto.precio)       // Asigna precio
                    stmt.setInt(3, producto.categoriaId)     // Asigna clave foránea de la categoría
                    stmt.executeUpdate() > 0                 // Retorna true si se registró exitosamente
                }
            }
        } catch (e: SQLException) {
            println("Error al insertar producto: ${e.message}")
            false
        }
    }

    // Modifica los datos de un producto existente según su ID
    fun actualizar(producto: Producto): Boolean {
        val sql = "UPDATE productos SET nombre = ?, precio = ?, categoria_id = ? WHERE id = ?"
        return try {
            dbManager.getConnection().use { conn ->
                conn.prepareStatement(sql).use { stmt ->
                    stmt.setString(1, producto.nombre)
                    stmt.setDouble(2, producto.precio)
                    stmt.setInt(3, producto.categoriaId)
                    stmt.setInt(4, producto.id)
                    stmt.executeUpdate() > 0 // Retorna true si fue actualizado correctamente
                }
            }
        } catch (e: SQLException) {
            println("Error al actualizar producto: ${e.message}")
            false
        }
    }

    // Elimina un producto de la base de datos por su ID
    fun eliminar(id: Int): Boolean {
        val sql = "DELETE FROM productos WHERE id = ?"
        return try {
            dbManager.getConnection().use { conn ->
                conn.prepareStatement(sql).use { stmt ->
                    stmt.setInt(1, id)
                    stmt.executeUpdate() > 0 // Retorna verdadero si la fila se eliminó
                }
            }
        } catch (e: SQLException) {
            println("Error al eliminar producto: ${e.message}")
            false
        }
    }

    // Realiza una consulta JOIN para obtener todos los productos con el nombre de su categoría asociada
    fun obtenerTodosConCategoria(): List<Producto> {
        val lista = mutableListOf<Producto>()
        // Consulta SQL combinando tablas productos y categorias
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
                        // Mapea los resultados de la consulta SQL a la lista de objetos Producto
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
        return lista // Retorna la lista de productos obtenida
    }
}

// ==========================================
// 5. CONTROLADOR DEL MENÚ DE CONSOLA
// ==========================================

// Clase responsable de la interacción con el usuario mediante mensajes en consola y captura de entradas
class MenuApp(
    private val categoriaDAO: CategoriaDAO,
    private val productoDAO: ProductoDAO
) {
    private val scanner = Scanner(System.`in`) // Instancia del escáner para lecturas de consola

    // Bucle principal que mantiene desplegado el menú interactivo
    fun iniciar() {
        var continuar = true

        while (continuar) {
            // Impresión de opciones del menú
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

            // Evaluación de la opción ingresada por el usuario
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
                    continuar = false // Termina el bucle y cierra el programa
                }
                else -> println("\n⚠️ Opción no válida. Intenta de nuevo.")
            }
        }
    }

    // Solicita los datos e inserta una nueva categoría
    private fun procesarNuevaCategoria(): Int {
        println("\n=== REGISTRAR NUEVA CATEGORÍA ===")
        print("Ingrese el nombre de la categoría: ")
        val nombre = scanner.nextLine().trim()

        // Validación de entrada vacía
        if (nombre.isEmpty()) {
            println("El nombre no puede estar vacío.")
            return -1
        }

        // Llama al DAO para insertar la categoría
        val idGenerado = categoriaDAO.insertar(Categoria(nombre = nombre))
        if (idGenerado != -1) {
            println("✓ Categoría '$nombre' registrada con éxito (ID: $idGenerado).")
        }
        return idGenerado
    }

    // Solicita los datos para registrar un nuevo producto
    private fun procesarNuevoProducto() {
        println("\n=== REGISTRAR NUEVO PRODUCTO ===")
        print("Ingrese el nombre del producto: ")
        val nombre = scanner.nextLine().trim()

        print("Ingrese el precio del producto: ")
        val precio = scanner.nextLine().toDoubleOrNull() ?: 0.0 // Convierte la entrada a Double o asigna 0.0 por defecto

        // Permite elegir o crear una categoría para asignarla al producto
        val categoriaId = seleccionarOCrearCategoria()

        // Inserta el producto en la base de datos a través del DAO
        if (productoDAO.insertar(Producto(nombre = nombre, precio = precio, categoriaId = categoriaId))) {
            println("✓ Producto '$nombre' registrado con éxito.")
        }
    }

    // Solicita el ID y nuevo nombre para modificar una categoría
    private fun editarCategoria() {
        println("\n=== EDITAR CATEGORÍA ===")
        val categorias = mostrarCategorias()
        if (categorias.isEmpty()) return

        print("\nIngresa el ID de la categoría a editar: ")
        val id = scanner.nextLine().toIntOrNull() ?: return

        // Valida la existencia del ID especificado
        if (categorias.none { it.id == id }) {
            println("⚠️ El ID ingresado no existe.")
            return
        }

        print("Ingrese el nuevo nombre para la categoría: ")
        val nuevoNombre = scanner.nextLine().trim()

        // Ejecuta la actualización en el DAO
        if (categoriaDAO.actualizar(Categoria(id = id, nombre = nuevoNombre))) {
            println("✓ Categoría actualizada correctamente.")
        }
    }

    // Solicita la eliminación de una categoría previa advertencia si contiene productos
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

        // Verifica si la categoría a eliminar tiene productos vinculados
        val cantidadProductos = categoriaDAO.contarProductosAsociados(id)

        println("\n⚠️ ¡ADVERTENCIA DE ELIMINACIÓN!")
        if (cantidadProductos > 0) {
            println("La categoría '${catSeleccionada.nombre}' tiene $cantidadProductos producto(s) vinculado(s).")
            println("Si la eliminas, ¡TAMBIÉN SE ELIMINARÁN ESOS $cantidadProductos PRODUCTOS!")
        } else {
            println("Vas a eliminar la categoría '${catSeleccionada.nombre}'.")
        }

        // Pide confirmación explícita al usuario
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

    // Permite actualizar el nombre, precio o categoría de un producto registrado
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

        // Si se presiona Enter sin escribir nada, se conserva el dato actual
        print("Nuevo nombre (actual: ${prodActual.nombre}): ")
        val nuevoNombre = scanner.nextLine().trim().ifEmpty { prodActual.nombre }

        print("Nuevo precio (actual: S/ ${prodActual.precio}): ")
        val inputPrecio = scanner.nextLine().trim()
        val nuevoPrecio = if (inputPrecio.isEmpty()) prodActual.precio else inputPrecio.toDoubleOrNull() ?: prodActual.precio

        print("¿Deseas cambiar la categoría del producto? (S/N): ")
        val cambiarCat = scanner.nextLine().trim()

        // Determina si se cambia o se mantiene la categoría
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

    // Elimina un producto por su ID previa confirmación del usuario
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

        // Confirmación de seguridad
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

    // Submenú para seleccionar una categoría disponible o crear una nueva al registrar o editar productos
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
                // Reitera la lectura hasta obtener un ID existente en la lista
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

    // Muestra en consola el listado de categorías registradas
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

    // Muestra en consola la lista completa de productos con sus respectivas categorías
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

// Punto de entrada principal de la aplicación Kotlin
fun main() {
    // 1. Instancia el gestor de la base de datos apuntando al archivo 'identifier.sqlite'
    val dbManager = DatabaseManager("identifier.sqlite")

    // 2. Ejecuta la creación/inicialización de las tablas si no existen
    dbManager.inicializarTablas()

    // 3. Instancia los objetos DAO inyectando la dependencia de la base de datos
    val categoriaDAO = CategoriaDAO(dbManager)
    val productoDAO = ProductoDAO(dbManager)

    // 4. Instancia la interfaz de usuario por consola e inicia el programa
    val app = MenuApp(categoriaDAO, productoDAO)
    app.iniciar()
}