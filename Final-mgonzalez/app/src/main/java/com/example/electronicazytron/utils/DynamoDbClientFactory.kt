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
                        accessKeyId = "ASIAVE3FVN5P42ZM4WNB",
                        secretAccessKey = "2xcMJ55kEegpxo7+WQh+0BV7ZJIoQz2cpvhL6fKf",
                        sessionToken = "IQoJb3JpZ2luX2VjEN7//////////wEaCXVzLXdlc3QtMiJHMEUCIER9Y2dp4ohEPM/eBARQsdVO0YGAq76TOmcFiWVAfz5RAiEA1vJcShrDC7x/KG/zP7UCSg/gmbbjxG87D0TN+C4X5VIqqgIIp///////////ARABGgwzNTQwMTExNDgxMjciDAVB+q1JrHnGm1wJvyr+AeCrrf3uXxgTdpyTilp5yMI4godmow+hW7oXhsIK5bzLVLCtPIUxoEsVfqXU67+Otl5H00gJ2hZCqUR/rEqb7/hblmOGWVHXRQAaJ910FBj0s9SL0telqvFd0KxEg+xKdxXQMYjOue2xF7bs4E3zMZZhqCfHZmOSQ0uCzbk7iHx5e84PUzpeiJ4qxTZiwWk0UZRY+i787orGTt6osywJrPkh0um+6+tEbuN6fA0S/FE2BpN0TdPPp+T7+Fgd8xrl16Q1E1GRQw/sp+Rs6QhTMIoIFg9UtHlqN1kIRRuFC9dkwbhgcXq052HhSe2wrdYIU/3kjY9e+QSmIZ8nvKoMMJH0rMwGOp0B8e/Xb/m/OdjLni6GberUqFHi7NAaBGpeoa44H2PW0OBjpuU8og9PEPCrrJWsKhXpT63wcicCHvqJNbUoSzuy/eApnaXt8i2+Zgaucaxdug68BRCQOaDcSu2HpFRLPHUfz3DlKoCrRswhQx4oUBcFdA8yctGhs5w2KMIQV/0qNlvrC5EowOiIW2F1GLnHsHrBC3ndhP6w0o2uBzdHuA==" // ¡Obligatorio en labs!
                    )
                )
            }
        }
    }
}