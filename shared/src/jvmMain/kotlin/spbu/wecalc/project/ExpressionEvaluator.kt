package spbu.wecalc.project

import net.objecthunter.exp4j.ExpressionBuilder

class ExpressionEvaluator : Calculator {
    override fun evaluate(expression: String): CalcResult {
        if (expression.isBlank()) {
            return CalcResult.Failure(CalcError.SyntaxError)
        }

        return try {
            val result = ExpressionBuilder(expression).build().evaluate()
            if (result.isInfinite() || result.isNaN()) {
                CalcResult.Failure(CalcError.DivisionByZero)
            } else {
                CalcResult.Success(result)
            }
        } catch (_: ArithmeticException) {
            CalcResult.Failure(CalcError.DivisionByZero)
        } catch (_: IllegalArgumentException) {
            CalcResult.Failure(CalcError.SyntaxError)
        } catch (e: Exception) {
            CalcResult.Failure(CalcError.InternalError(e.message ?: "Unknown internal error"))
        }
    }
}
