package com.example.myapplication.server.routes

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import com.example.myapplication.server.controllers.AuthController
import com.example.myapplication.server.controllers.TwoFactorController
import com.example.myapplication.server.models.RegisterRequest
import com.example.myapplication.server.models.LoginRequest
import com.example.myapplication.server.models.LoginResponse
import com.example.myapplication.server.models.UserResponse
import com.example.myapplication.server.models.Verify2FARequest



fun Route.authRoutes() {

    println("📌 com.example.myapplication.server.routes.authRoutes cargadas correctamente")

    route("/register") {
        post {
            val registerRequest = call.receive<RegisterRequest>()
            val result = AuthController.registerUser(registerRequest)
            if (result) {
                call.respond(HttpStatusCode.Created, "Usuario registrado exitosamente")
            } else {
                call.respond(HttpStatusCode.BadRequest, "Error al registrar usuario")
            }
        }
    }

    route("/login") {
        get {
            call.respondText("✅ Ruta GET /login activa")
        }

        post {
            println("Se recibió un POST a /login")
            val loginRequest = call.receive<LoginRequest>()
            val user = AuthController.loginUser(loginRequest)

            if (user != null) {
                val response = LoginResponse(
                    mensaje = "Inicio de sesión exitoso",
                    usuario = UserResponse(
                        id = user.id,
                        email = user.email,
                        nombreUsuario = user.nombreUsuario,
                        fechaNacimiento = user.fechaNacimiento.toString(),
                        genero = user.genero,
                        orientacionSexual = user.orientacionSexual
                    )
                )
                call.respond(HttpStatusCode.OK, response)
            } else {
                call.respond(HttpStatusCode.Unauthorized, "Credenciales incorrectas")
            }

        }

    }



    post("/verificar-2fa") {
        val request = call.receive<Verify2FARequest>()
        val verificado = TwoFactorController.verificarCodigo2FA(request)

        if (verificado) {
            call.respond(HttpStatusCode.OK, "✅ Código verificado")
        } else {
            call.respond(HttpStatusCode.BadRequest, "❌ Código inválido o expirado")
        }
    }





}

