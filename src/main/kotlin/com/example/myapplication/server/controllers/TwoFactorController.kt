package com.example.myapplication.server.controllers

import com.example.myapplication.server.database.Database
import com.example.myapplication.server.models.Verify2FARequest
import io.ktor.server.util.toLocalDateTime
import java.sql.SQLException
import java.time.Instant
import java.sql.Timestamp


object TwoFactorController {

    fun verificarCodigo2FA(data: Verify2FARequest): Boolean {
        val conn = Database.getConnection()

        val getUserIdQuery = "SELECT idUsuario FROM usuario WHERE correoElectronico = ?"
        val getTokenQuery = """
        SELECT idToken, expiracion, usado FROM token2fa 
        WHERE idUsuario = ? AND codigo = ? 
        ORDER BY expiracion DESC LIMIT 1
    """
        val updateTokenQuery = "UPDATE token2fa SET usado = true WHERE idToken = ?"

        conn.use { connection ->
            try {
                var userId = -1

                // 1. Obtener ID del usuario por email
                connection.prepareStatement(getUserIdQuery).use { stmt ->
                    stmt.setString(1, data.email)
                    val rs = stmt.executeQuery()
                    if (rs.next()) {
                        userId = rs.getInt("idUsuario")
                    } else {
                        println("❌ Usuario no encontrado con email: ${data.email}")
                        return false
                    }
                }

                // 2. Buscar token válido
                connection.prepareStatement(getTokenQuery).use { stmt ->
                    stmt.setInt(1, userId)
                    stmt.setString(2, data.codigo)
                    val rs = stmt.executeQuery()
                    if (rs.next()) {
                        val expiracion: Timestamp = rs.getTimestamp("expiracion")
                        val usado: Boolean = rs.getBoolean("usado")
                        val ahora = Timestamp(System.currentTimeMillis())

                        if (usado) {
                            println("⚠️ El código ya fue usado")
                            return false
                        }

                        if (ahora.after(expiracion)) {
                            println("⏰ Código expirado")
                            return false
                        }

                        val idToken = rs.getInt("idToken")

                        // 3. Marcar como usado
                        connection.prepareStatement(updateTokenQuery).use { updateStmt ->
                            updateStmt.setInt(1, idToken)
                            updateStmt.executeUpdate()
                        }

                        println("✅ Código 2FA verificado correctamente")
                        return true
                    } else {
                        println("❌ Código incorrecto para el usuario")
                        return false
                    }
                }

            } catch (e: SQLException) {
                println("❌ Error durante verificación 2FA: ${e.message}")
                return false
            }
        }
    }
}
