package org.example

import java.util.Scanner

// 1. CLASE DE LÓGICA (Modelo/Servicio)
class ClasificadorNumeros(private val inicio: Int, private val fin: Int) {

    fun obtenerPares(): List<Int> {
        val pares = mutableListOf<Int>()
        for (numero in inicio..fin) {
            if (numero % 2 == 0) {
                pares.add(numero)
            }
        }
        return pares
    }

    fun obtenerImpares(): List<Int> {
        val impares = mutableListOf<Int>()
        for (numero in inicio..fin) {
            if (numero % 2 != 0) {
                impares.add(numero)
            }
        }
        return impares
    }
}

// 2. CLASE DE INTERFAZ DE USUARIO (Controlador/Vista)
class MenuNumeros {
    private val scanner = Scanner(System.`in`)

    fun iniciar() {
        println("----------------------------------")
        println("Selector de numero PARES e IMPARES")
        println("----------------------------------")

        print("Ingresa el número inicial: ")
        val numeroInicial = scanner.nextLine().toIntOrNull() ?: 0

        print("Ingresa el número final: ")
        val numeroFinal = scanner.nextLine().toIntOrNull() ?: 0

        // Instanciamos la clase que procesa los datos
        val clasificador = ClasificadorNumeros(numeroInicial, numeroFinal)

        // Mostramos los resultados
        println("\nNúmeros pares:")
        clasificador.obtenerPares().forEach { print("$it ") }

        println("\n\nNúmeros impares:")
        clasificador.obtenerImpares().forEach { print("$it ") }
        println()
    }
}

// 3. PUNTO DE ENTRADA (MAIN)
fun main() {
    val app = MenuNumeros()
    app.iniciar()
}