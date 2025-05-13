package com.example.myapplication.server.controllers

import com.example.myapplication.server.models.LikeResponse
import com.example.myapplication.server.models.Like
import com.example.myapplication.server.models.LikeRequest
import com.example.myapplication.server.models.RechazoRequest
import com.example.myapplication.server.models.SugerenciaResponse
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.http.*
import java.sql.Connection

fun Route.emparejamientoRoutes(dbConnection: Connection) {

    post("/like") {
        try {
            val likeRequest = call.receive<LikeRequest>()
            val idUsuarioEmisor = likeRequest.idUsuarioEmisor
            val idUsuarioReceptor = likeRequest.idUsuarioReceptor

            // Insertar el like
            val insertStmt = dbConnection.prepareStatement(
                "INSERT INTO likes (idUsuarioEmisor, idUsuarioReceptor, estado) VALUES (?, ?, 'like') " +
                        "ON DUPLICATE KEY UPDATE estado = 'like'"
            )

            insertStmt.setInt(1, idUsuarioEmisor)
            insertStmt.setInt(2, idUsuarioReceptor)
            insertStmt.executeUpdate()

            // Verificar si el receptor también dio like al emisor
            val checkMatchStmt = dbConnection.prepareStatement(
                "SELECT * FROM likes WHERE idUsuarioEmisor = ? AND idUsuarioReceptor = ?"
            )
            checkMatchStmt.setInt(1, idUsuarioReceptor)
            checkMatchStmt.setInt(2, idUsuarioEmisor)

            val matchResult = checkMatchStmt.executeQuery()
            val esMatch = matchResult.next()

            if (esMatch) {
                val user1 = minOf(idUsuarioEmisor, idUsuarioReceptor)
                val user2 = maxOf(idUsuarioEmisor, idUsuarioReceptor)

                // Verificar si ya existe el emparejamiento
                val checkExisting = dbConnection.prepareStatement(
                    """
                SELECT * FROM emparejamiento 
                WHERE (idUsuario1 = ? AND idUsuario2 = ?) OR (idUsuario1 = ? AND idUsuario2 = ?)
                """
                )
                checkExisting.setInt(1, user1)
                checkExisting.setInt(2, user2)
                checkExisting.setInt(3, user2)
                checkExisting.setInt(4, user1)

                val alreadyExists = checkExisting.executeQuery().next()

                if (!alreadyExists) {
                    val insertMatchStmt = dbConnection.prepareStatement(
                        """
                    INSERT INTO emparejamiento (idUsuario1, idUsuario2, estadoEmparejamiento)
                    VALUES (?, ?, 'Aceptado')
                    """
                    )
                    insertMatchStmt.setInt(1, user1)
                    insertMatchStmt.setInt(2, user2)
                    insertMatchStmt.executeUpdate()
                }
            }

            call.respond(LikeResponse(match = esMatch, message = if (esMatch) "¡Es un match!" else "Like registrado"))

        } catch (e: Exception) {
            e.printStackTrace()
            call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Error al procesar el like"))
        }
    }


    get("/sugerencias/{idUsuario}") {
        val idUsuario = call.parameters["idUsuario"]?.toIntOrNull()

        if (idUsuario == null) {
            call.respond(HttpStatusCode.BadRequest, "ID de usuario inválido")
            return@get
        }

        try {
            val stmt = dbConnection.prepareStatement("""
            SELECT u.idUsuario, u.nombreUsuario, 
                   TIMESTAMPDIFF(YEAR, u.fechaNacimiento, CURDATE()) AS edad, 
                   u.genero,
                   (SELECT rutaFoto FROM fotoperfil 
                    WHERE idUsuario = u.idUsuario AND esPrincipal = 1 
                    LIMIT 1) AS fotoPerfil
            FROM usuario u
            JOIN preferencias p ON p.idUsuario = ?
            WHERE u.idUsuario != ?
              AND u.genero = p.generoDeseado
              AND TIMESTAMPDIFF(YEAR, u.fechaNacimiento, CURDATE()) 
                  BETWEEN p.rangoEdadMin AND p.rangoEdadMax
              AND u.idUsuario NOT IN (
                  SELECT idUsuarioReceptor 
                  FROM likes 
                  WHERE idUsuarioEmisor = ? 
                    AND estado = 'rechazado'
              )
              AND u.idUsuario NOT IN (
                  SELECT CASE 
                      WHEN idUsuario1 = ? THEN idUsuario2
                      ELSE idUsuario1
                  END
                  FROM emparejamiento
                  WHERE (idUsuario1 = ? OR idUsuario2 = ?) 
                    AND estadoEmparejamiento = 'Aceptado'
              )
              AND u.idUsuario NOT IN (
                  SELECT idUsuarioReceptor 
                  FROM likes 
                  WHERE idUsuarioEmisor = ?
              )
        """)

            stmt.setInt(1, idUsuario) // preferencias
            stmt.setInt(2, idUsuario) // u.idUsuario != ?
            stmt.setInt(3, idUsuario) // likes rechazados
            stmt.setInt(4, idUsuario) // emparejamiento CASE
            stmt.setInt(5, idUsuario) // emparejamiento WHERE idUsuario1
            stmt.setInt(6, idUsuario) // emparejamiento WHERE idUsuario2

            val rs = stmt.executeQuery()

            val sugerencias = mutableListOf<SugerenciaResponse>()

            while (rs.next()) {
                sugerencias.add(
                    SugerenciaResponse(
                        idUsuario = rs.getInt("idUsuario"),
                        nombre = rs.getString("nombreUsuario"),
                        edad = rs.getInt("edad"),
                        genero = rs.getString("genero"),
                        fotoPerfil = rs.getString("fotoPerfil")
                    )
                )
            }

            call.respond(sugerencias)

        } catch (e: Exception) {
            e.printStackTrace()
            call.respond(HttpStatusCode.InternalServerError, "Error al obtener sugerencias")
        }
    }


    post("/rechazar") {
        try {
            val rechazoRequest = call.receive<RechazoRequest>()
            val idUsuarioEmisor = rechazoRequest.idUsuarioEmisor
            val idUsuarioReceptor = rechazoRequest.idUsuarioReceptor

            // Insertar o actualizar el rechazo en la tabla likes
            val updateStmt = dbConnection.prepareStatement(
                """
            INSERT INTO likes (idUsuarioEmisor, idUsuarioReceptor, estado) 
            VALUES (?, ?, 'rechazado')
            ON DUPLICATE KEY UPDATE estado = 'rechazado'
            """
            )
            updateStmt.setInt(1, idUsuarioEmisor)
            updateStmt.setInt(2, idUsuarioReceptor)
            updateStmt.executeUpdate()

            call.respond(HttpStatusCode.OK, mapOf("message" to "Usuario rechazado correctamente"))

        } catch (e: Exception) {
            e.printStackTrace()
            call.respond(HttpStatusCode.InternalServerError, "Error al registrar el rechazo")
        }
    }



}
