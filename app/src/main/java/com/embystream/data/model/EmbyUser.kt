package com.embystream.data.model

data class EmbyUser(
    val Id: String?,
    val Name: String?
)

data class LoginRequest(
    val Username: String,
    val Password: String
)

data class LoginResponse(
    val User: EmbyUser?,
    val AccessToken: String?
)
