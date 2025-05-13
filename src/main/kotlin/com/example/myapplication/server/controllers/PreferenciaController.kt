package com.example.myapplication.server.controllers

import com.example.myapplication.server.models.Preferencia
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.sql.Connection
import io.ktor.http.*

fun Route.preferenciasRoutes(dbConnection: Connection) {

    // Guardar preferencias
    post("/preferencias") {
        try {
            val pref = call.receive<Preferencia>()

            val stmt = dbConnection.prepareStatement(
                """
                INSERT INTO preferencias (idUsuario, rangoEdadMin, rangoEdadMax, generoDeseado, distanciaMaxima)
                VALUES (?, ?, ?, ?, ?)
                """
            )

            stmt.setInt(1, pref.idUsuario)
            stmt.setInt(2, pref.rangoEdadMin)
            stmt.setInt(3, pref.rangoEdadMax)
            stmt.setString(4, pref.generoDeseado)
            stmt.setInt(5, pref.distanciaMaxima)

            val rows = stmt.executeUpdate()
            if (rows > 0) {
                call.respond(HttpStatusCode.OK, mapOf("message" to "Preferencias guardadas"))
            } else {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "No se pudieron guardar las preferencias"))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Error al guardar las preferencias"))
        }
    }

    // Obtener preferencias
    get("/preferencias/{id}") {
        val idUsuario = call.parameters["id"]?.toIntOrNull()

        if (idUsuario == null) {
            call.respond(HttpStatusCode.BadRequest, "ID inválido")
            return@get
        }

        try {
            val stmt = dbConnection.prepareStatement(
                "SELECT * FROM preferencias WHERE idUsuario = ?"
            )
            stmt.setInt(1, idUsuario)
            val result = stmt.executeQuery()

            if (result.next()) {
                val pref = Preferencia(
                    idUsuario = result.getInt("idUsuario"),
                    rangoEdadMin = result.getInt("rangoEdadMin"),
                    rangoEdadMax = result.getInt("rangoEdadMax"),
                    generoDeseado = result.getString("generoDeseado"),
                    distanciaMaxima = result.getInt("distanciaMaxima")
                )
                call.respond(pref)
            } else {
                call.respond(HttpStatusCode.NotFound, "Preferencias no encontradas")
            }

        } catch (e: Exception) {
            e.printStackTrace()
            call.respond(HttpStatusCode.InternalServerError, "Error al obtener preferencias")
        }
    }
}
