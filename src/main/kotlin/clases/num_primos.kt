package org.example.clases

import java.util.Scanner

// 1. CLASE DE LÓGICA Y PROCESAMIENTO
// Esta clase contiene las reglas matemáticas para determinar e identificar números primos.
class VerificadorPrimos {

    // Método que evalúa si un solo número es primo (retorna true si es primo, false si no lo es).
    fun esPrimo(numero: Int): Boolean {
        if (numero < 2) return false // Los números menores a 2 no se consideran primos.

        // Recorre todos los números desde el 2 hasta antes de llegar al número evaluado (numero - 1).
        for (divisor in 2 until numero) {
            if (numero % divisor == 0) { // Si el número es divisible exactamente entre otro, no es primo.
                return false
            }
        }
        return true // Si no encontró ningún divisor exacto, el número es primo.
    }

    // Método que devuelve la lista de todos los números primos dentro del rango [inicio..fin].
    fun obtenerPrimosEnRango(inicio: Int, fin: Int): List<Int> {
        val listaPrimos = mutableListOf<Int>() // Lista mutable para almacenar los primos hallados.
        for (numero in inicio..fin) {          // Bucle que recorre desde el número inicial hasta el final.
            if (esPrimo(numero)) {             // Evalúa si el número actual del rango es primo usando el método esPrimo().
                listaPrimos.add(numero)        // Si es primo, se agrega a la lista.
            }
        }
        return listaPrimos                     // Retorna la lista con los números primos encontrados.
    }
}

// 2. CLASE DE INTERFAZ DE USUARIO POR CONSOLA
// Esta clase maneja la captura de datos ingresados por el usuario y la presentación de resultados.
class MenuPrimos {
    // Objeto Scanner para capturar los datos ingresados desde la consola.
    private val scanner = Scanner(System.`in`)
    // Instancia de la clase de lógica para hacer las verificaciones.
    private val verificador = VerificadorPrimos()

    // Método principal que ejecuta la secuencia de entrada/salida en la consola.
    fun iniciar() {
        // Mensaje de bienvenida y encabezado decorativo.
        println("----------------------------------")
        println("         Números PRIMOS           ")
        println("----------------------------------")

        // Solicita al usuario el número inicial del rango.
        print("Ingresa el número inicial: ")
        // Lee la entrada, intenta convertirla a Int; si no se ingresa un número válido, asigna 0 por defecto.
        val numeroInicial = scanner.nextLine().toIntOrNull() ?: 0

        // Solicita al usuario el número final del rango.
        print("Ingresa el número final: ")
        // Lee la entrada, intenta convertirla a Int; si falla, asigna 0 por defecto.
        val numeroFinal = scanner.nextLine().toIntOrNull() ?: 0

        // Llama al método de la lógica para obtener la lista de números primos dentro del rango.
        val primosEncontrados = verificador.obtenerPrimosEnRango(numeroInicial, numeroFinal)

        // Imprime el resultado de la búsqueda.
        println("\nNúmeros primos:")
        if (primosEncontrados.isEmpty()) { // Comprueba si la lista devuelta está vacía.
            println("No se encontraron números primos en el rango especificado.")
        } else {
            // Recorre e imprime cada primo encontrado separado por un espacio.
            primosEncontrados.forEach { print("$it ") }
            println() // Imprime un salto de línea final para limpiar la salida de consola.
        }
    }
}

// 3. PUNTO DE ENTRADA (MAIN)
// Función principal que inicia la ejecución de todo el programa.
fun main() {
    // Instancia el menú de la aplicación.
    val app = MenuPrimos()
    // Llama al método para dar inicio a la interfaz por consola.
    app.iniciar()
}