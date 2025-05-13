package com.example.myapplication.server.controllers

import com.example.myapplication.server.models.FotoPerfil
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.sql.Connection

fun Route.fotoPerfilRoutes(dbConnection: Connection) {

    // Guardar foto de perfil
    post("/fotoperfil") {
        try {
            val foto = call.receive<FotoPerfil>()

            if (foto.esPrincipal) {
                // Desmarcar cualquier otra foto principal
                val updateStmt = dbConnection.prepareStatement(
                    "UPDATE fotoperfil SET esPrincipal = 0 WHERE idUsuario = ?"
                )
                updateStmt.setInt(1, foto.idUsuario)
                updateStmt.executeUpdate()
            }

            val insertStmt = dbConnection.prepareStatement(
                "INSERT INTO fotoperfil (idUsuario, rutaFoto, esPrincipal) VALUES (?, ?, ?)"
            )
            insertStmt.setInt(1, foto.idUsuario)
            insertStmt.setString(2, foto.rutaFoto)
            insertStmt.setBoolean(3, foto.esPrincipal)

            val rowsInserted = insertStmt.executeUpdate()

            if (rowsInserted > 0) {
                call.respond(HttpStatusCode.OK, mapOf("message" to "Foto guardada exitosamente"))
            } else {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "No se pudo guardar la foto"))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Error al guardar la foto"))
        }
    }

    // Obtener todas las fotos del usuario
    get("/fotoperfil/{id}") {
        val id = call.parameters["id"]?.toIntOrNull()
        if (id == null) {
            call.respond(HttpStatusCode.BadRequest, "ID inválido")
            return@get
        }

        try {
            val statement = dbConnection.prepareStatement(
                "SELECT * FROM fotoperfil WHERE idUsuario = ?"
            )
            statement.setInt(1, id)
            val result = statement.executeQuery()

            val fotos = mutableListOf<FotoPerfil>()
            while (result.next()) {
                fotos.add(
                    FotoPerfil(
                        idUsuario = result.getInt("idUsuario"),
                        rutaFoto = result.getString("rutaFoto"),
                        esPrincipal = result.getBoolean("esPrincipal")
                    )
                )
            }

            call.respond(fotos)
        } catch (e: Exception) {
            e.printStackTrace()
            call.respond(HttpStatusCode.InternalServerError, "Error al obtener las fotos")
        }
    }
}
