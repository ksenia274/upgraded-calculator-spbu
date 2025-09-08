package spbu.wecalc.project

class RemoteCalculator(private val apiService: ApiService) {
    suspend fun evaluate(expression: String): CalcResult {
        return try {
            val result = apiService.calculate(expression)
            result.fold(
                onSuccess = { response ->
                    val resultValue = response.result
                    if (response.success && resultValue != null) {
                        try {
                            val value = resultValue.toDouble()
                            CalcResult.Success(value)
                        } catch (e: NumberFormatException) {
                            CalcResult.Failure(CalcError.InternalError("Invalid result format: $resultValue"))
                        }
                    } else {
                        val errorMessage = response.error ?: "Unknown calculation error"
                        when {
                            errorMessage.contains("Division by zero", ignoreCase = true) ->
                                CalcResult.Failure(CalcError.DivisionByZero)
                            errorMessage.contains("syntax", ignoreCase = true) ||
                            errorMessage.contains("Invalid", ignoreCase = true) ->
                                CalcResult.Failure(CalcError.SyntaxError)
                            else ->
                                CalcResult.Failure(CalcError.InternalError(errorMessage))
                        }
                    }
                },
                onFailure = { exception ->
                    CalcResult.Failure(CalcError.InternalError("Network error: ${exception.message}"))
                }
            )
        } catch (e: Exception) {
            CalcResult.Failure(CalcError.InternalError("Unexpected error: ${e.message}"))
        }
    }
}