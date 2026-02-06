package com.example.electronicazytron.utils

import aws.sdk.kotlin.services.dynamodb.DynamoDbClient
import kotlinx.coroutines.runBlocking
import aws.smithy.kotlin.runtime.auth.awscredentials.Credentials
import aws.sdk.kotlin.runtime.auth.credentials.StaticCredentialsProvider


object DynamoDbClientFactory {

    // Usamos un patrón Singleton para asegurarnos de que solo haya un cliente de DynamoDB
    @Volatile
    private var client: DynamoDbClient? = null

    fun getClient(): DynamoDbClient {
        return client ?: synchronized(this) {
            client ?: buildClient().also { client = it }
        }
    }

    private fun buildClient(): DynamoDbClient {
        // runBlocking se usa aquí por simplicidad en este patrón de fábrica,
        // pero en la lógica de la app usaremos corutinas de forma estándar.
        return runBlocking {
            DynamoDbClient {
                // ¡IMPORTANTE! Cambia "us-east-1" a la región donde creaste tu tabla de DynamoDB.
                region = "us-east-1"

                // Por ahora, el SDK buscará credenciales de forma predeterminada.
                // Más adelante, configuraremos Cognito aquí para un acceso más seguro.
                // CONFIGURACIÓN PARA LABORATORIO
                credentialsProvider = StaticCredentialsProvider(
                    Credentials(
                        accessKeyId = "",
                        secretAccessKey = "",
                        sessionToken = "" // ¡Obligatorio en labs!
                    )
                )
            }
        }
    }
}