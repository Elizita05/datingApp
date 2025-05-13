package com.example.myapplication.server.controllers

import com.example.myapplication.server.models.MensajeRequest
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

import com.example.myapplication.server.models.Mensaje
import com.example.myapplication.server.utils.AesEncryption
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object MensajesController {

    lateinit var db: Connection

    fun init(database: Connection) {
        db = database
    }

    fun enviarMensaje(idRemitente: Int, idDestinatario: Int, contenido: String): Boolean {
        val sql = """
            INSERT INTO mensajes (idRemitente, idDestinatario, contenido, fechaEnvio, mensajeEncriptado)
            VALUES (?, ?, ?, ?, ?)
        """.trimIndent()

        val encriptado = AesEncryption.encrypt(contenido)
        val now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))

        db.prepareStatement(sql).use { stmt ->
            stmt.setInt(1, idRemitente)
            stmt.setInt(2, idDestinatario)
            stmt.setString(3, encriptado)
            stmt.setString(4, now)
            stmt.setBoolean(5, true)
            return stmt.executeUpdate() > 0
        }
    }

    fun obtenerMensajes(id1: Int, id2: Int): List<Mensaje> {
        val mensajes = mutableListOf<Mensaje>()
        val sql = """
            SELECT * FROM mensajes 
            WHERE (idRemitente = ? AND idDestinatario = ?)
               OR (idRemitente = ? AND idDestinatario = ?)
            ORDER BY fechaEnvio ASC
        """.trimIndent()

        db.prepareStatement(sql).use { stmt ->
            stmt.setInt(1, id1)
            stmt.setInt(2, id2)
            stmt.setInt(3, id2)
            stmt.setInt(4, id1)

            val rs: ResultSet = stmt.executeQuery()
            while (rs.next()) {
                val mensaje = Mensaje(
                    idMensaje = rs.getInt("idMensaje"),
                    idRemitente = rs.getInt("idRemitente"),
                    idDestinatario = rs.getInt("idDestinatario"),
                    contenido = if (rs.getBoolean("mensajeEncriptado")) {
                        AesEncryption.decrypt(rs.getString("contenido"))
                    } else {
                        rs.getString("contenido")
                    },
                    fechaEnvio = rs.getString("fechaEnvio")
                )
                mensajes.add(mensaje)
            }
        }

        return mensajes
    }
}