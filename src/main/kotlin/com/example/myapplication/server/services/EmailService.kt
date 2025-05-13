package com.example.myapplication.server.services

import java.util.*
import javax.mail.*
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

object EmailService {

    private val email = "soportetinus@gmail.com" // Tu correo
    private val password = "iafdthlzmimkggvr" // La contraseña específica de aplicación

    private val props = Properties().apply {
        put("mail.smtp.auth", "true")
        put("mail.smtp.starttls.enable", "true")
        put("mail.smtp.host", "smtp.gmail.com")
        put("mail.smtp.port", "587")
    }

    fun enviarCorreo(destinatario: String, asunto: String, cuerpo: String): Boolean {
        return try {
            val session = Session.getInstance(props, object : Authenticator() {
                override fun getPasswordAuthentication(): PasswordAuthentication {
                    return PasswordAuthentication(email, password)
                }
            })

            val message = MimeMessage(session).apply {
                setFrom(InternetAddress(email))
                setRecipients(Message.RecipientType.TO, InternetAddress.parse(destinatario))
                subject = asunto
                setText(cuerpo)
            }

            Transport.send(message)
            println("Correo enviado a $destinatario 📩")
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
