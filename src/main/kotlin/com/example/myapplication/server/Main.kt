package com.example.myapplication.server

import com.example.myapplication.server.controllers.MensajesController
import com.example.myapplication.server.controllers.emparejamientoRoutes
import com.example.myapplication.server.controllers.fotoPerfilRoutes
import com.example.myapplication.server.controllers.perfilRoutes
import com.example.myapplication.server.controllers.preferenciasRoutes
import com.example.myapplication.server.controllers.testCorreoRoute
import com.example.myapplication.server.routes.authRoutes
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.routing.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.response.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.http.*
import com.example.myapplication.server.routes.testDbRoute
import com.example.myapplication.server.database.Database
import com.example.myapplication.server.routes.chatRoutes
import com.example.myapplication.server.routes.mensajesRoutes
import io.ktor.server.websocket.* // Para instalar WebSockets
import java.time.Duration // Para el pingPeriod y timeout


fun main() {
    val dbConnection = Database.getConnection()

    embeddedServer(Netty, port = 8080, host = "0.0.0.0") {
        install(ContentNegotiation) {
            json()
        }

        install(CORS) {
            allowMethod(HttpMethod.Get)
            allowMethod(HttpMethod.Post)
            allowMethod(HttpMethod.Put)
            allowMethod(HttpMethod.Delete)
            allowHeader(HttpHeaders.Authorization)
            allowHeader(HttpHeaders.ContentType)
            allowCredentials = true
            anyHost()
        }

        install(WebSockets) {
            pingPeriod = Duration.ofSeconds(15)
            timeout = Duration.ofSeconds(30)
            maxFrameSize = Long.MAX_VALUE
            masking = false
        }


        routing {
            get("/") {
                call.respondText("Servidor activo")

            }



            authRoutes() // Aquí se registran login y register
            testDbRoute()
            perfilRoutes(dbConnection)
            fotoPerfilRoutes(dbConnection)
            preferenciasRoutes(dbConnection)
            emparejamientoRoutes(dbConnection)
            testCorreoRoute()
            MensajesController.init(dbConnection)
            mensajesRoutes()
            chatRoutes()

        }
    }.start(wait = true)

    println("🔥 Este es el main.kt correcto")



}
