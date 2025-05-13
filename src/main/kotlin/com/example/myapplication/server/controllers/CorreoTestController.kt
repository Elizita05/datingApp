package com.example.myapplication.server.controllers

import com.example.myapplication.server.services.EmailService
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.http.*

fun Route.testCorreoRoute() {
    get("/test-correo") {
        val destinatario = "elizabethcardenas910@gmail.com" // A donde quieres que llegue la prueba
        val asunto = "Correo de prueba desde Tinus 🧪"
        val cuerpo = "Hola, este es un correo de prueba enviado desde el backend de Tinus."

        val enviado = EmailService.enviarCorreo(destinatario, asunto, cuerpo)

        if (enviado) {
            call.respondText("✅ Correo enviado exitosamente a $destinatario")
        } else {
            call.respond(HttpStatusCode.InternalServerError, "❌ No se pudo enviar el correo")
        }
    }
}
