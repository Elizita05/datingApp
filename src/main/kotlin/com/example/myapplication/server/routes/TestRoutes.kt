package com.example.myapplication.server.routes

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import com.example.myapplication.server.database.Database
import java.sql.SQLException


fun Route.testDbRoute() {
    get("/test-db") {
        try {
            val connection = Database.getConnection()
            connection.close()
            call.respondText("✅ Conexión exitosa a la base de datos Tinus")
        } catch (e: Exception) {
            call.respondText("❌ Error al conectar: ${e.message}")
        }
    }
}
