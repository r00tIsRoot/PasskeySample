package com.google.credentialmanager.sample.ui

import com.google.gson.Gson

data class AuthenticationResponse(
    val rawId: String,
    val authenticatorAttachment: String,
    val type: String,
    val id: String,
    val response: Response,
    val clientExtensionResults: Map<String, Any>
) {
    fun toJsonString(): String {
        return Gson().toJson(this)
    }
}

data class Response(
    val clientDataJSON: String,
    val attestationObject: String,
    val transports: List<String>,
    val authenticatorData: String,
    val publicKeyAlgorithm: Int,
    val publicKey: String
)

data class SigninPasskey(
    val rawId: String,
    val authenticatorAttachment: String,
    val type: String,
    val id: String,
    val response: SigninResponse,
    val clientExtensionResults: Map<String, Any>
)

data class SigninResponse(
    val clientDataJSON: String,
    val authenticatorData: String,
    val signature: String,
    val userHandle: String
)