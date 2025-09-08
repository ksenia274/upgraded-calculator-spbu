package spbu.wecalc.project

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import spbu.wecalc.project.history.HistoryAddRequest
import spbu.wecalc.project.history.ServerHistoryRepository

fun Application.calculationModule() {
    val calculator = ExpressionEvaluator()
    val historyRepo = ServerHistoryRepository()

    routing {
        route("/api") {
            post("/calculate") {
                val userHeader = call.request.headers["X-User-Id"]
                if (userHeader == null || userHeader == "") {
                    call.respond(HttpStatusCode.Unauthorized, "Missing X-User-Id")
                    return@post
                }

                try {
                    val request = call.receive<CalculationRequest>()
                    val expression = request.expression.trim()
                    
                    if (expression.isEmpty()) {
                        call.respond(CalculationResponse(
                            success = false,
                            error = "Expression cannot be empty"
                        ))
                        return@post
                    }

                    val result = calculator.evaluate(expression)
                    
                    when (result) {
                        is CalcResult.Success -> {
                            val resultString = result.value.toString()
                            
                            // Save to history
                            historyRepo.add(userHeader, expression, resultString, System.currentTimeMillis())
                            
                            call.respond(CalculationResponse(
                                success = true,
                                result = resultString
                            ))
                        }
                        is CalcResult.Failure -> {
                            val errorMessage = when (val error = result.error) {
                                is CalcError.SyntaxError -> "Invalid expression syntax"
                                is CalcError.DivisionByZero -> "Division by zero"
                                is CalcError.InternalError -> error.message
                            }
                            
                            call.respond(CalculationResponse(
                                success = false,
                                error = errorMessage
                            ))
                        }
                    }
                } catch (e: Exception) {
                    call.respond(CalculationResponse(
                        success = false,
                        error = "Failed to process calculation: ${e.message}"
                    ))
                }
            }
        }
    }
}