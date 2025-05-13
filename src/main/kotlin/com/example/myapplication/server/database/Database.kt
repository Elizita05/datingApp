package com.example.myapplication.server.database

import java.sql.Connection
import java.sql.DriverManager

object Database {
    private const val jdbcUrl = "jdbc:mysql://10.10.2.253:3306/tinus"
    private const val user = "tinus_user"
    private const val password = "Tinus123$@!?"

    fun getConnection(): Connection {
        return try {
            // Cargar el driver de MySQL
            Class.forName("com.mysql.cj.jdbc.Driver")
            DriverManager.getConnection(jdbcUrl, user, password)
        } catch (e: Exception) {
            e.printStackTrace()
            throw RuntimeException("Error al conectar con la base de datos", e)
        }
    }
}
