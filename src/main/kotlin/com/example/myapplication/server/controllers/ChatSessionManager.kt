package com.example.myapplication.server.controllers

import io.ktor.websocket.*
import java.util.concurrent.ConcurrentHashMap

object ChatSessionManager {
    private val sessions = ConcurrentHashMap<Int, WebSocketSession>()

    fun addUserSession(userId: Int, session: WebSocketSession) {
        sessions[userId] = session
    }

    fun removeUserSession(userId: Int) {
        sessions.remove(userId)
    }

    fun getUserSession(userId: Int): WebSocketSession? {
        return sessions[userId]
    }

    fun isUserOnline(userId: Int): Boolean {
        return sessions.containsKey(userId)
    }
}