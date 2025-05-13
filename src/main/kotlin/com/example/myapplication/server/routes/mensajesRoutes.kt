package com.example.myapplication.server.routes

import com.example.myapplication.server.controllers.MensajesController
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import com.example.myapplication.server.models.MensajeRequest
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.* // Frame, WebSocketSession, etc.
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.plugins.*
import java.time.Duration
import com.example.myapplication.server.controllers.ChatSessionManager


fun Route.mensajesRoutes() {

    route("/mensajes") {

        post("/enviar") {
            val body = call.receive<MensajeRequest>()
            val exito = MensajesController.enviarMensaje(
                body.idRemitente,
                body.idDestinatario,
                body.contenido
            )
            call.respond(mapOf("exito" to exito))
        }

        get("/conversacion/{id1}/{id2}") {
            val id1 = call.parameters["id1"]?.toIntOrNull()
            val id2 = call.parameters["id2"]?.toIntOrNull()

            if (id1 == null || id2 == null) {
                call.respond(mapOf("error" to "Parámetros inválidos"))
                return@get
            }

            val mensajes = MensajesController.obtenerMensajes(id1, id2)
            call.respond(mensajes)
        }
    }
}

fun Route.chatRoutes() {
    webSocket("/chat/{idUsuario}") {
        val userId = call.parameters["idUsuario"]?.toIntOrNull()
        if (userId == null) {
            close(CloseReason(CloseReason.Codes.CANNOT_ACCEPT, "ID de usuario inválido"))
            return@webSocket
        }

        ChatSessionManager.addUserSession(userId, this)
        println("🔌 Usuario conectado al chat: $userId")

        try {
            for (frame in incoming) {
                if (frame is Frame.Text) {
                    val incomingText = frame.readText()
                    println("📨 Mensaje recibido de $userId: $incomingText")

                    // Aquí parseamos y enviamos al destinatario
                    val parts = incomingText.split(":", limit = 2)
                    if (parts.size == 2) {
                        val receiverId = parts[0].toIntOrNull()
                        val message = parts[1]

                        if (receiverId != null) {
                            val receiverSession = ChatSessionManager.getUserSession(receiverId)
                            if (receiverSession != null) {
                                receiverSession.send("[Usuario $userId] $message")
                            } else {
                                send("Usuario $receiverId no está conectado")
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            println("❌ Error en WebSocket: ${e.localizedMessage}")
        } finally {
            ChatSessionManager.removeUserSession(userId)
            println("🔌 Usuario desconectado del chat: $userId")
        }
    }
}
