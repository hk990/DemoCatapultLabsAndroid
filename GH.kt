package io.connect.app.utils

import io.connect.app.ConnectIOApp

class GH {

    // live
    val baseUrl = "https://connectio-api.vpnsystem.dev/"

    // dev
    // val baseUrl = "http://192.168.1.127:3000/"
    companion object {
        val instance = GH()
    }

    enum class KEYS {
        IS_LOGGED_IN,
        EMAIL,
        PASSWORD,
        SESSION_ID,
        IS_SERVER_ON,
        BROWSER_LINK
    }

    val isLoggedIn: Boolean
        get() = EasyPreference.with(ConnectIOApp.instance).getBoolean(KEYS.IS_LOGGED_IN.name, false)
    val isServerOn: Boolean
        get() = EasyPreference.with(ConnectIOApp.instance).getBoolean(KEYS.IS_SERVER_ON.name, false)
    val email: String?
        get() = EasyPreference.with(ConnectIOApp.instance).getString(KEYS.EMAIL.name, "")
    val sessionId: String?
        get() = EasyPreference.with(ConnectIOApp.instance).getString(KEYS.SESSION_ID.name, "")
    val browserLink: String?
        get() = EasyPreference.with(ConnectIOApp.instance).getString(KEYS.BROWSER_LINK.name, "")

}