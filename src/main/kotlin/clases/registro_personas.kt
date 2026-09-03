package org.example

import java.util.Scanner

// 1. CLASE MODELO
data class Persona(
    var nombre: String,
    var dni: String,
    var edad: Int
)

// 2. CLASE DE GESTIÓN (Maneja las operaciones sobre la lista)
class GestorPersonas {
    private val listaPersonas = mutableListOf<Persona>()

    fun registrar(persona: Persona): Boolean {
        return listaPersonas.add(persona)
    }

    fun listar(): List<Persona> {
        return listaPersonas
    }

    fun buscarPorDni(dni: String): Persona? {
        return listaPersonas.find { it.dni == dni }
    }

    fun editar(dni: String, nuevoNombre: String, nuevaEdad: Int): Boolean {
        val persona = buscarPorDni(dni)
        return if (persona != null) {
            persona.nombre = nuevoNombre
            persona.edad = nuevaEdad
            true
        } else {
            false
        }
    }

    fun eliminar(dni: String): Boolean {
        val persona = buscarPorDni(dni)
        return if (persona != null) {
            listaPersonas.remove(persona)
            true
        } else {
            false
        }
    }
}

// 3. CLASE DE INTERFAZ DE USUARIO POR CONSOLA
class MenuPersonas {
    private val scanner = Scanner(System.`in`)
    private val gestor = GestorPersonas()

    fun iniciar() {
        var continuar = true

        while (continuar) {
            println("\n----------------------------------")
            println("      REGISTRO DE PERSONAS        ")
            println("----------------------------------")
            println("1. Registrar persona(s)")
            println("2. Eliminar persona")
            println("3. Editar persona")
            println("4. Listar personas registradas")
            println("5. Salir")
            print("Selecciona una opción: ")

            when (scanner.nextLine().trim()) {
                "1" -> registrarPersonas()
                "2" -> eliminarPersona()
                "3" -> editarPersona()
                "4" -> listarPersonas()
                "5" -> {
                    println("\n¡Hasta luego!")
                    continuar = false
                }
                else -> println("\n⚠️ Opción no válida. Intenta de nuevo.")
            }
        }
    }

    private fun registrarPersonas() {
        println("\n=== REGISTRAR PERSONAS ===")
        print("¿Cuántas personas deseas registrar?: ")
        val cantidad = scanner.nextLine().toIntOrNull() ?: 0

        if (cantidad <= 0) {
            println("⚠️ La cantidad debe ser mayor a 0.")
            return
        }

        for (i in 1..cantidad) {
            println("\n--- Persona $i de $cantidad ---")

            print("Nombre: ")
            val nombre = scanner.nextLine().trim()

            print("DNI: ")
            val dni = scanner.nextLine().trim()

            print("Edad: ")
            val edad = scanner.nextLine().toIntOrNull() ?: 0

            val nuevaPersona = Persona(nombre, dni, edad)
            gestor.registrar(nuevaPersona)
            println("✓ Persona '$nombre' registrada correctamente.")
        }

        println("\n✓ Se completó el registro de $cantidad persona(s).")
    }

    private fun eliminarPersona() {
        println("\n=== ELIMINAR PERSONA ===")
        if (gestor.listar().isEmpty()) {
            println("No hay personas registradas.")
            return
        }

        print("Ingresa el DNI de la persona a eliminar: ")
        val dni = scanner.nextLine().trim()

        val persona = gestor.buscarPorDni(dni)
        if (persona == null) {
            println("⚠️ No se encontró ninguna persona con el DNI ingresado.")
            return
        }

        println("\n⚠️ ¡ADVERTENCIA!")
        print("¿Estás seguro de que deseas eliminar a '${persona.nombre}' (DNI: $dni)? (S/N): ")
        val confirmacion = scanner.nextLine().trim()

        if (confirmacion.equals("S", ignoreCase = true)) {
            if (gestor.eliminar(dni)) {
                println("✓ Persona eliminada correctamente.")
            }
        } else {
            println("Operación cancelada.")
        }
    }

    private fun editarPersona() {
        println("\n=== EDITAR PERSONA ===")
        if (gestor.listar().isEmpty()) {
            println("No hay personas registradas.")
            return
        }

        print("Ingresa el DNI de la persona a editar: ")
        val dni = scanner.nextLine().trim()

        val persona = gestor.buscarPorDni(dni)
        if (persona == null) {
            println("⚠️ No se encontró ninguna persona con ese DNI.")
            return
        }

        print("Nuevo nombre (actual: ${persona.nombre}): ")
        val nuevoNombre = scanner.nextLine().trim().ifEmpty { persona.nombre }

        print("Nueva edad (actual: ${persona.edad}): ")
        val inputEdad = scanner.nextLine().trim()
        val nuevaEdad = if (inputEdad.isEmpty()) persona.edad else inputEdad.toIntOrNull() ?: persona.edad

        if (gestor.editar(dni, nuevoNombre, nuevaEdad)) {
            println("✓ Datos actualizados correctamente.")
        }
    }

    private fun listarPersonas() {
        println("\n----------------------------------")
        println("       PERSONAS REGISTRADAS       ")
        println("----------------------------------")
        val personas = gestor.listar()

        if (personas.isEmpty()) {
            println("(No hay personas registradas aún)")
        } else {
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
fun main() {
    val app = MenuPersonas()
    app.iniciar()
}