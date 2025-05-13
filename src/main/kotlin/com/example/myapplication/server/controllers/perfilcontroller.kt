package com.example.myapplication.server.controllers

import io.ktor.http.*
import com.example.myapplication.server.models.Perfil
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.sql.Connection

fun Route.perfilRoutes(dbConnection: Connection) {

    post("/perfil") {
        try {

            val perfil = call.receive<Perfil>()

            val statement = dbConnection.prepareStatement(
                """
                INSERT INTO Perfiles (user_id, bio, location, interests, favorite_music, favorite_movies)
                VALUES (?, ?, ?, ?, ?, ?)
                """
            )

            statement.setInt(1, perfil.userId)
            statement.setString(2, perfil.bio)
            statement.setString(3, perfil.location)
            statement.setString(4, perfil.interests)
            statement.setString(5, perfil.favoriteMusic)
            statement.setString(6, perfil.favoriteMovies)

            val rowsInserted = statement.executeUpdate()

            if (rowsInserted > 0) {
                call.respond(mapOf("message" to "Perfil guardado exitosamente"))
            } else {
                call.respond(mapOf("message" to "No se pudo guardar el perfil"))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            call.respond(mapOf("error" to "Error al guardar el perfil"))
        }
    }

    // Obtener perfil
    get("/perfil/{id}") {
        val id = call.parameters["id"]?.toIntOrNull()

        if (id == null) {
            call.respond(HttpStatusCode.BadRequest, "ID inválido")
            return@get
        }

        try {
            val statement = dbConnection.prepareStatement(
                "SELECT * FROM Perfiles WHERE user_id = ?"
            )
            statement.setInt(1, id)
            val result = statement.executeQuery()

            if (result.next()) {
                val perfil = Perfil(
                    userId = result.getInt("user_id"),
                    bio = result.getString("bio") ?: "",
                    location = result.getString("location") ?: "",
                    interests = result.getString("interests") ?: "",
                    favoriteMusic = result.getString("favorite_music") ?: "",
                    favoriteMovies = result.getString("favorite_movies") ?: ""
                )
                call.respond(perfil)
            } else {
                call.respond(HttpStatusCode.NotFound, "Perfil no encontrado")
            }

        } catch (e: Exception) {
            e.printStackTrace()
            call.respond(HttpStatusCode.InternalServerError, "Error al buscar el perfil")
        }
    }

}
