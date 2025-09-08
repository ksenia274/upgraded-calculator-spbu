package spbu.wecalc.project

import kotlinx.serialization.Serializable

@Serializable
data class CalculationRequest(
    val expression: String
)

@Serializable
data class CalculationResponse(
    val success: Boolean,
    val result: String? = null,
    val error: String? = null
)