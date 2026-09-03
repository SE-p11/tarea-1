package org.example

import java.util.Scanner

// 1. CLASE DE LÓGICA Y PROCESAMIENTO
class VerificadorPrimos {

    // Método que evalúa si un solo número es primo
    fun esPrimo(numero: Int): Boolean {
        if (numero < 2) return false

        for (divisor in 2 until numero) {
            if (numero % divisor == 0) {
                return false
            }
        }
        return true
    }

    // Método que devuelve la lista de números primos en un rango
    fun obtenerPrimosEnRango(inicio: Int, fin: Int): List<Int> {
        val listaPrimos = mutableListOf<Int>()
        for (numero in inicio..fin) {
            if (esPrimo(numero)) {
                listaPrimos.add(numero)
            }
        }
        return listaPrimos
    }
}

// 2. CLASE DE INTERFAZ DE USUARIO POR CONSOLA
class MenuPrimos {
    private val scanner = Scanner(System.`in`)
    private val verificador = VerificadorPrimos()

    fun iniciar() {
        println("----------------------------------")
        println("         Números PRIMOS           ")
        println("----------------------------------")

        print("Ingresa el número inicial: ")
        val numeroInicial = scanner.nextLine().toIntOrNull() ?: 0

        print("Ingresa el número final: ")
        val numeroFinal = scanner.nextLine().toIntOrNull() ?: 0

        val primosEncontrados = verificador.obtenerPrimosEnRango(numeroInicial, numeroFinal)

        println("\nNúmeros primos:")
        if (primosEncontrados.isEmpty()) {
            println("No se encontraron números primos en el rango especificado.")
        } else {
            primosEncontrados.forEach { print("$it ") }
            println()
        }
    }
}

// 3. PUNTO DE ENTRADA (MAIN)
fun main() {
    val app = MenuPrimos()
    app.iniciar()
}