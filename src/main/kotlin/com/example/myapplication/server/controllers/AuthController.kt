package com.example.myapplication.server.controllers

import androidx.annotation.RequiresApi
import com.example.myapplication.server.database.Database
import com.example.myapplication.server.models.LoginRequest
import com.example.myapplication.server.models.RegisterRequest
import com.example.myapplication.server.models.User
import com.example.myapplication.server.services.EmailService
import com.example.myapplication.server.utils.Hashing
import java.sql.SQLException
import java.sql.Date
import java.sql.Timestamp
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId







object AuthController {

    fun registerUser(data: RegisterRequest): Boolean {
        val conn = Database.getConnection()

        val checkQuery = "SELECT COUNT(*) FROM usuario WHERE correoElectronico = ?"
        val insertUserQuery = """
            INSERT INTO usuario (
                correoElectronico,
                contrasena,
                salt,
                nombreUsuario,
                fechaNacimiento,
                genero,
                orientacionSexual
            ) VALUES (?, ?, ?, ?, ?, ?, ?)
        """.trimIndent()

        val insertTokenQuery = """
            INSERT INTO token2fa (idUsuario, codigo, expiracion, usado)
            VALUES (?, ?, ?, false)
        """.trimIndent()

        conn.use { connection ->
            try {
                // Verificar si el correo ya está registrado
                connection.prepareStatement(checkQuery).use { checkStmt ->
                    checkStmt.setString(1, data.email)
                    val resultSet = checkStmt.executeQuery()
                    if (resultSet.next() && resultSet.getInt(1) > 0) {
                        println("⚠️ El correo ya está registrado: ${data.email}")
                        return false
                    }
                }

                // Insertar nuevo usuario
                var userId = -1
                connection.prepareStatement(insertUserQuery, java.sql.Statement.RETURN_GENERATED_KEYS).use { stmt ->
                    val salt = Hashing.generateSalt()
                    val hashedPassword = Hashing.hashPassword(data.password, salt)

                    stmt.setString(1, data.email)
                    stmt.setString(2, hashedPassword)
                    stmt.setString(3, salt)
                    stmt.setString(4, data.nombreUsuario)
                    stmt.setDate(5, Date.valueOf(data.fechaNacimiento))
                    stmt.setString(6, data.genero)
                    stmt.setString(7, data.orientacionSexual)

                    val filas = stmt.executeUpdate()
                    if (filas == 0) throw SQLException("❌ No se pudo insertar el usuario")

                    val generatedKeys = stmt.generatedKeys
                    if (generatedKeys.next()) {
                        userId = generatedKeys.getInt(1)
                        println("✅ Usuario insertado con ID: $userId")
                    } else {
                        throw SQLException("❌ No se pudo obtener el ID del nuevo usuario")
                    }
                }
                // Generar un código aleatorio de 6 dígitos para la autenticación 2FA
                val codigo2FA = (100000..999999).random().toString()

// Obtener la hora actual y sumarle 10 minutos para la expiración del código 2FA
// NOTA: LocalDateTime usa el formato "yyyy-MM-ddTHH:mm:ss", pero Timestamp.valueOf()
// espera un espacio entre la fecha y la hora ("yyyy-MM-dd HH:mm:ss"), por eso hacemos el replace.
                val expiracion = Timestamp.valueOf(
                    LocalDateTime.now()
                        .plusMinutes(10)
                        .toString()
                        .replace("T", " ") // Convierte el formato para que lo acepte Timestamp.valueOf()
                )

                /*
                  🔐 Esta fecha de expiración se guarda junto con el código en la tabla `token2fa`.
                  El código 2FA expira 10 minutos después del registro.
                  Puedes ajustar el tiempo modificando el `.plusMinutes(10)` a la cantidad que desees.
                */





                // Insertar token en la tabla token2fa
                connection.prepareStatement(insertTokenQuery).use { insertToken ->
                    insertToken.setInt(1, userId)
                    insertToken.setString(2, codigo2FA)
                    insertToken.setTimestamp(3, expiracion)
                    insertToken.executeUpdate()
                }

                // Enviar correo con el código
                val enviado = EmailService.enviarCorreo(
                    destinatario = data.email,
                    asunto = "Tu código de verificación para Tinus",
                    cuerpo = "Hola ${data.nombreUsuario}, tu código de verificación es: $codigo2FA.\nEste código expira en 10 minutos."
                )

                if (enviado) {
                    println("📩 Correo enviado con código 2FA")
                } else {
                    println("⚠️ No se pudo enviar el correo 2FA")
                }

                return true

            } catch (e: SQLException) {
                println("❌ Error durante el registro: ${e.message}")
                return false
            }
        }
    }

    fun loginUser(data: LoginRequest): User? {
        val conn = Database.getConnection()

        val query = """
        SELECT u.*, t.usado 
        FROM usuario u
        JOIN token2fa t ON u.idUsuario = t.idUsuario
        WHERE u.correoElectronico = ?
        ORDER BY t.expiracion DESC LIMIT 1
    """.trimIndent()

        conn.use { connection ->
            connection.prepareStatement(query).use { stmt ->
                stmt.setString(1, data.email)
                val resultSet = stmt.executeQuery()

                if (resultSet.next()) {
                    val storedHash = resultSet.getString("contrasena")
                    val storedSalt = resultSet.getString("salt")
                    val yaUsado = resultSet.getBoolean("usado")

                    // Validar contraseña
                    if (Hashing.hashPassword(data.password, storedSalt) != storedHash) {
                        println("❌ Contraseña incorrecta para: ${data.email}")
                        return null
                    }

                    // Verificar si el código 2FA ya fue usado
                    if (!yaUsado) {
                        println("🔐 El código 2FA aún no ha sido verificado para: ${data.email}")
                        return null
                    }

                    println("✅ Inicio de sesión exitoso para: ${data.email}")

                    val fechaNacimientoSql = resultSet.getDate("fechaNacimiento")
                    val fechaNacimiento = LocalDate.parse(fechaNacimientoSql.toString())

                    return User(
                        id = resultSet.getInt("idUsuario"),
                        email = resultSet.getString("correoElectronico"),
                        nombreUsuario = resultSet.getString("nombreUsuario"),
                        fechaNacimiento = fechaNacimiento,
                        genero = resultSet.getString("genero"),
                        orientacionSexual = resultSet.getString("orientacionSexual")
                    )
                }
            }
        }

        println("❌ Usuario no encontrado: ${data.email}")
        return null
    }

}