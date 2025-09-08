package spbu.wecalc.project

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import spbu.wecalc.project.history.HistoryListResponse

class ApiService {
    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
            })
        }
    }
    
    private val baseUrl = "http://localhost:9090"
    private val userId = "default-user"
    
    suspend fun calculate(expression: String): Result<CalculationResponse> {
        return try {
            val response = client.post("$baseUrl/api/calculate") {
                contentType(ContentType.Application.Json)
                header("X-User-Id", userId)
                setBody(CalculationRequest(expression))
            }
            Result.success(response.body<CalculationResponse>())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getHistory(limit: Int = 10): Result<HistoryListResponse> {
        return try {
            val response = client.get("$baseUrl/api/history") {
                header("X-User-Id", userId)
                parameter("limit", limit)
            }
            Result.success(response.body<HistoryListResponse>())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    fun close() {
        client.close()
    }
}