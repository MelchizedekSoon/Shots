package com.example.shots

data class NotificationResponse(
    val id: String?,
    val external_id: String?,
    val errors: Errors?
)

data class Errors(
    val invalid_aliases: InvalidAliases?
)

data class InvalidAliases(
    val external_id: List<String>?
)
