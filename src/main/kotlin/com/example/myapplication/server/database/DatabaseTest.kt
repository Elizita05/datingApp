package com.example.myapplication.server.database

fun main() {
    try {
        val connection = Database.getConnection()
        println("✅ ¡Conexión exitosa a la base de datos Tinus!")

        // También puedes probar con una consulta simple si quieres
        val statement = connection.createStatement()
        val resultSet = statement.executeQuery("SHOW TABLES")

        println("📋 Tablas en la base de datos Tinus:")
        while (resultSet.next()) {
            println(" - ${resultSet.getString(1)}")
        }

        resultSet.close()
        statement.close()
        connection.close()
    } catch (e: Exception) {
        println("❌ Falló la conexión:")
        e.printStackTrace()
    }
}
