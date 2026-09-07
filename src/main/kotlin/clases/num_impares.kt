package org.example.clases

import java.util.Scanner

// 1. CLASE DE LÓGICA (Modelo/Servicio)
// Esta clase se encarga de realizar la lógica de negocio: filtrar los números dentro de un rango dado.
class ClasificadorNumeros(private val inicio: Int, private val fin: Int) {

    // Método que recorre el rango de números y devuelve una lista solo con los números pares.
    fun obtenerPares(): List<Int> {
        val pares = mutableListOf<Int>() // Lista mutable para ir guardando los pares encontrados.
        for (numero in inicio..fin) {    // Bucle que recorre desde el número inicial hasta el final.
            if (numero % 2 == 0) {       // Verificación: si el residuo al dividir entre 2 es 0, es par.
                pares.add(numero)        // Agrega el número par a la lista.
            }
        }
        return pares                     // Retorna la lista con todos los pares encontrados.
    }

    // Método que recorre el rango de números y devuelve una lista solo con los números impares.
    fun obtenerImpares(): List<Int> {
        val impares = mutableListOf<Int>() // Lista mutable para ir guardando los impares encontrados.
        for (numero in inicio..fin) {      // Bucle que recorre desde el número inicial hasta el final.
            if (numero % 2 != 0) {         // Verificación: si el residuo al dividir entre 2 es diferente de 0, es impar.
                impares.add(numero)        // Agrega el número impar a la lista.
            }
        }
        return impares                     // Retorna la lista con todos los impares encontrados.
    }
}

// 2. CLASE DE INTERFAZ DE USUARIO (Controlador/Vista)
// Esta clase maneja la interacción con el usuario: solicita datos por consola y muestra los resultados.
class MenuNumeros {
    // Objeto Scanner para leer lo que el usuario escribe desde la consola.
    private val scanner = Scanner(System.`in`)

    // Método principal que ejecuta la secuencia del menú interactivo.
    fun iniciar() {
        // Mensaje de bienvenida y encabezado del programa.
        println("----------------------------------")
        println("Selector de numero PARES e IMPARES")
        println("----------------------------------")

        // Solicita el número inicial al usuario.
        print("Ingresa el número inicial: ")
        // Lee el texto escrito, intenta convertirlo a número entero (Int). Si la conversión falla, usa 0 por defecto.
        val numeroInicial = scanner.nextLine().toIntOrNull() ?: 0

        // Solicita el número final al usuario.
        print("Ingresa el número final: ")
        // Lee el texto escrito e intenta convertirlo a Int. Si falla o está vacío, asigna 0 por defecto.
        val numeroFinal = scanner.nextLine().toIntOrNull() ?: 0

        // Instanciamos la clase que procesa los datos pasando el rango de números capturado.
        val clasificador = ClasificadorNumeros(numeroInicial, numeroFinal)

        // Mostramos los resultados de los números pares.
        println("\nNúmeros pares:")
        // Llama al método para obtener la lista de pares y los imprime uno a uno separados por un espacio.
        clasificador.obtenerPares().forEach { print("$it ") }

        // Mostramos los resultados de los números impares.
        println("\n\nNúmeros impares:")
        // Llama al método para obtener la lista de impares y los imprime uno a uno separados por un espacio.
        clasificador.obtenerImpares().forEach { print("$it ") }
        println() // Imprime un salto de línea final para limpiar la consola.
    }
}

// 3. PUNTO DE ENTRADA (MAIN)
// Función principal que sirve como punto de inicio para la ejecución de la aplicación.
fun main() {
    // Crea una instancia de la clase MenuNumeros.
    val app = MenuNumeros()
    // Inicia la ejecución de la aplicación.
    app.iniciar()
}