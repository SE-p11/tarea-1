package org.example.clases

import java.util.Scanner

// 1. CLASE MODELO
// Data class que representa la estructura de datos de una Persona (almacena nombre, DNI y edad).
data class Persona(
    var nombre: String, // Campo para almacenar el nombre de la persona.
    var dni: String,    // Campo para almacenar el documento nacional de identidad (DNI).
    var edad: Int       // Campo para almacenar la edad en años.
)

// 2. CLASE DE GESTIÓN (Maneja las operaciones sobre la lista)
// Esta clase administra la colección de datos aplicando las operaciones CRUD (Crear, Leer, Actualizar, Eliminar).
class GestorPersonas {
    // Colección mutable para almacenar la lista de objetos de tipo Persona en memoria.
    private val listaPersonas = mutableListOf<Persona>()

    // Registra o agrega una nueva persona a la lista.
    fun registrar(persona: Persona): Boolean {
        return listaPersonas.add(persona) // Devuelve true si la adición fue exitosa.
    }

    // Devuelve la lista completa con todas las personas registradas.
    fun listar(): List<Persona> {
        return listaPersonas
    }

    // Busca y retorna un objeto Persona coincidente con el DNI proporcionado (o null si no lo encuentra).
    fun buscarPorDni(dni: String): Persona? {
        return listaPersonas.find { it.dni == dni }
    }

    // Modifica los datos de una persona existente identificada por su DNI.
    fun editar(dni: String, nuevoNombre: String, nuevaEdad: Int): Boolean {
        val persona = buscarPorDni(dni) // Busca a la persona antes de actualizar.
        return if (persona != null) {
            persona.nombre = nuevoNombre // Asigna el nuevo nombre.
            persona.edad = nuevaEdad     // Asigna la nueva edad.
            true                          // Indica que la edición se completó con éxito.
        } else {
            false                         // Indica que no se pudo editar al no encontrar la persona.
        }
    }

    // Elimina a una persona de la lista según su DNI.
    fun eliminar(dni: String): Boolean {
        val persona = buscarPorDni(dni) // Busca a la persona para verificar existencia.
        return if (persona != null) {
            listaPersonas.remove(persona) // Remueve el objeto de la lista.
            true                            // Confirma que se eliminó correctamente.
        } else {
            false                           // Retorna false si el DNI no fue localizado.
        }
    }
}

// 3. CLASE DE INTERFAZ DE USUARIO POR CONSOLA
// Controla las interacciones de entrada/salida y el flujo del menú del sistema.
class MenuPersonas {
    // Lector de datos desde la consola estándar.
    private val scanner = Scanner(System.`in`)
    // Instancia de la clase que gestiona las operaciones lógicas de la lista.
    private val gestor = GestorPersonas()

    // Método principal que despliega el ciclo de menú interactivo.
    fun iniciar() {
        var continuar = true // Bandera de control para mantener activo el menú.

        while (continuar) {
            // Despliegue de opciones disponibles para el usuario.
            println("\n----------------------------------")
            println("      REGISTRO DE PERSONAS        ")
            println("----------------------------------")
            println("1. Registrar persona(s)")
            println("2. Eliminar persona")
            println("3. Editar persona")
            println("4. Listar personas registradas")
            println("5. Salir")
            print("Selecciona una opción: ")

            // Evaluación de la opción ingresada utilizando un bloque when.
            when (scanner.nextLine().trim()) {
                "1" -> registrarPersonas()
                "2" -> eliminarPersona()
                "3" -> editarPersona()
                "4" -> listarPersonas()
                "5" -> {
                    println("\n¡Hasta luego!")
                    continuar = false // Rompe el ciclo while para finalizar.
                }
                else -> println("\n⚠️ Opción no válida. Intenta de nuevo.")
            }
        }
    }

    // Solicita datos al usuario e inserta una o más personas en el sistema.
    private fun registrarPersonas() {
        println("\n=== REGISTRAR PERSONAS ===")
        print("¿Cuántas personas deseas registrar?: ")
        // Convierte el valor a Int; si no es válido o está vacío, asigna 0.
        val cantidad = scanner.nextLine().toIntOrNull() ?: 0

        // Valida que la cantidad especificada sea válida para iterar.
        if (cantidad <= 0) {
            println("⚠️ La cantidad debe ser mayor a 0.")
            return
        }

        // Bucle para iterar segun la cantidad de registros especificada.
        for (i in 1..cantidad) {
            println("\n--- Persona $i de $cantidad ---")

            print("Nombre: ")
            val nombre = scanner.nextLine().trim() // Captura el nombre y limpia espacios vacíos.

            print("DNI: ")
            val dni = scanner.nextLine().trim()    // Captura el DNI y limpia espacios vacíos.

            print("Edad: ")
            val edad = scanner.nextLine().toIntOrNull() ?: 0 // Captura la edad convirtiendo a entero.

            // Crea la nueva instancia del modelo Persona con los datos capturados.
            val nuevaPersona = Persona(nombre, dni, edad)
            gestor.registrar(nuevaPersona) // Envía el registro al gestor.
            println("✓ Persona '$nombre' registrada correctamente.")
        }

        println("\n✓ Se completó el registro de $cantidad persona(s).")
    }

    // Gestiona la búsqueda y confirmación para remover a una persona por DNI.
    private fun eliminarPersona() {
        println("\n=== ELIMINAR PERSONA ===")
        // Verifica si la lista está vacía antes de solicitar un DNI.
        if (gestor.listar().isEmpty()) {
            println("No hay personas registradas.")
            return
        }

        print("Ingresa el DNI de la persona a eliminar: ")
        val dni = scanner.nextLine().trim()

        // Búsqueda del registro en el gestor.
        val persona = gestor.buscarPorDni(dni)
        if (persona == null) {
            println("⚠️ No se encontró ninguna persona con el DNI ingresado.")
            return
        }

        // Pide una confirmación de seguridad previa a la eliminación.
        println("\n⚠️ ¡ADVERTENCIA!")
        print("¿Estás seguro de que deseas eliminar a '${persona.nombre}' (DNI: $dni)? (S/N): ")
        val confirmacion = scanner.nextLine().trim()

        // Si confirma con "S" o "s", se procede a borrar el registro.
        if (confirmacion.equals("S", ignoreCase = true)) {
            if (gestor.eliminar(dni)) {
                println("✓ Persona eliminada correctamente.")
            }
        } else {
            println("Operación cancelada.")
        }
    }

    // Permite la búsqueda y modificación de los datos de un registro por DNI.
    private fun editarPersona() {
        println("\n=== EDITAR PERSONA ===")
        // Verifica si hay registros existentes en la lista.
        if (gestor.listar().isEmpty()) {
            println("No hay personas registradas.")
            return
        }

        print("Ingresa el DNI de la persona a editar: ")
        val dni = scanner.nextLine().trim()

        // Valida la existencia del DNI ingresado.
        val persona = gestor.buscarPorDni(dni)
        if (persona == null) {
            println("⚠️ No se encontró ninguna persona con ese DNI.")
            return
        }

        // Lee el nuevo nombre; si el usuario lo deja en blanco, mantiene el nombre actual.
        print("Nuevo nombre (actual: ${persona.nombre}): ")
        val nuevoNombre = scanner.nextLine().trim().ifEmpty { persona.nombre }

        // Lee la nueva edad; si la entrada es vacía o inválida, mantiene la edad actual.
        print("Nueva edad (actual: ${persona.edad}): ")
        val inputEdad = scanner.nextLine().trim()
        val nuevaEdad = if (inputEdad.isEmpty()) persona.edad else inputEdad.toIntOrNull() ?: persona.edad

        // Aplica la modificación en la capa del gestor.
        if (gestor.editar(dni, nuevoNombre, nuevaEdad)) {
            println("✓ Datos actualizados correctamente.")
        }
    }

    // Imprime en consola todos los registros guardados en la lista.
    private fun listarPersonas() {
        println("\n----------------------------------")
        println("       PERSONAS REGISTRADAS       ")
        println("----------------------------------")
        val personas = gestor.listar()

        // Evalúa si hay personas cargadas para mostrar en pantalla.
        if (personas.isEmpty()) {
            println("(No hay personas registradas aún)")
        } else {
            // Recorre e imprime la información de cada persona junto con su índice.
            personas.forEachIndexed { i, p ->
                println("Persona ${i + 1}:")
                println("Nombre: ${p.nombre}")
                println("DNI: ${p.dni}")
                println("Edad: ${p.edad}")
                println()
            }
        }
    }
}

// 4. MAIN
// Función de punto de entrada donde arranca la aplicación de consola.
fun main() {
    val app = MenuPersonas() // Crea la instancia del menú.
    app.iniciar()            // Inicia la ejecución de la interfaz gráfica/consola.
}