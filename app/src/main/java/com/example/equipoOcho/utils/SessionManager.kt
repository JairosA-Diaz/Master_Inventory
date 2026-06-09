package com.example.equipoOcho.utils

import android.content.Context

object SessionManager {

    private const val PREFS_NAME = "inventory_session_prefs"
    private const val KEY_SESSION_ACTIVE = "session_active"

    /** Guarda si la sesión está activa o no */
    fun setSessionActive(context: Context, active: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putBoolean(KEY_SESSION_ACTIVE, active)
            .apply()
    }

    /** Devuelve true si la sesión sigue activa */
    fun isSessionActive(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_SESSION_ACTIVE, false)
    }

    /** Atajo para cerrar sesión */
    fun clearSession(context: Context) {
        setSessionActive(context, false)
    }
}