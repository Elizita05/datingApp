package com.example.myapplication.server.utils


import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

object AesEncryption {
    private const val secretKey = "tinusAppClaveAES" // 16 caracteres
    private const val initVector = "RandomInitVector" // 16 caracteres

    private val keySpec = SecretKeySpec(secretKey.toByteArray(Charsets.UTF_8), "AES")
    private val ivSpec = IvParameterSpec(initVector.toByteArray(Charsets.UTF_8))

    fun encrypt(value: String): String {
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec)
        val encrypted = cipher.doFinal(value.toByteArray(Charsets.UTF_8))
        return Base64.getEncoder().encodeToString(encrypted)
    }

    fun decrypt(encrypted: String): String {
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec)
        val original = cipher.doFinal(Base64.getDecoder().decode(encrypted))
        return String(original, Charsets.UTF_8)
    }
}
