package spbu.wecalc.project

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue


class CalculatorServiceTest {
    private val calculator = ExpressionEvaluator()

    private fun assertSuccess(expr: String, expected: Double) {
        val result = calculator.evaluate(expr)
        assertTrue(result is CalcResult.Success, "Expected Success but got $result")
        assertEquals(expected, result.value, 1e-14, "Wrong result for $expr")
    }

    private fun assertFailure(expr: String, expectedError: CalcError? = null) {
        val result = calculator.evaluate(expr)
        assertTrue(result is CalcResult.Failure, "Expected Failure but got $result")
        if (expectedError != null) {
            assertEquals(expectedError, result.error)
        }
    }

    @Test
    fun `basic arithmetic operations`() {
        assertSuccess("2 + 3", 5.0)
        assertSuccess("10 - 4", 6.0)
        assertSuccess("3 * 4", 12.0)
        assertSuccess("15 / 3", 5.0)
        assertSuccess("2 ^ 3", 8.0)
        assertSuccess("2 ^ 0", 1.0)
    }

    @Test
    fun `operator precedence`() {
        assertSuccess("2 + 3 * 4", 14.0)
        assertSuccess("(2 + 3) * 4", 20.0)
        assertSuccess("8 / 2 * 4", 16.0)
    }
    @Test
    fun `negative numbers`() {
        assertSuccess("-5 + 3", -2.0)
        assertSuccess("5 + (-3)", 2.0)
        assertSuccess("-2 * -3", 6.0)
        assertSuccess("--5", 5.0)
        assertSuccess("2 ++3", 5.0)
        assertSuccess("2 -- 3", 5.0)
    }

    @Test
    fun `division by zero errors`() {
        assertFailure("5 / 0", CalcError.DivisionByZero)
        assertFailure("10 / (5-5)", CalcError.DivisionByZero)
        assertFailure("0 / 0", CalcError.DivisionByZero)
    }

    @Test
    fun `syntax errors`() {
        assertFailure("", CalcError.SyntaxError)
        assertFailure("   ", CalcError.SyntaxError)
        assertFailure(")", CalcError.InternalError("Unknown internal error"))
        assertFailure("(", CalcError.SyntaxError)
        assertFailure("()", CalcError.InternalError("Unknown internal error"))
        assertFailure("2 ** 3", CalcError.SyntaxError)
        assertFailure("(2 + 3", CalcError.SyntaxError)
        assertFailure("2 + 3)", CalcError.InternalError("Unknown internal error"))
        assertFailure("--", CalcError.SyntaxError)
        assertFailure("*-1+", CalcError.SyntaxError)
    }

    @Test
    fun `multiple operations`() {
        assertSuccess("1 + 2 + 3 + 4", 10.0)
        assertSuccess("2 * 3 * 4", 24.0)
        assertSuccess("20 / 2 / 2", 5.0)
    }

    @Test
    fun `nested parentheses`() {
        assertSuccess("((2 + 3) * 4) / 2", 10.0)
        assertSuccess("(3 * (2 + 1)) - 4", 5.0)
    }

    @Test
    fun `decimal  notation`() {
        assertSuccess("0.1 + 0.2", 0.3)
        assertSuccess("2.500000001*2", 5.000000002)
        assertSuccess("1/3", 0.33333333333333)
    }

    @Test
    fun `complex expression`() {
        assertSuccess("(2 + 3) * 4 - 6 / 2", 17.0)
        assertSuccess("(12 + 22*7) / (33 + (12*3 -8)) * 3", 8.163934426229508)
    }

    @Test
    fun `with spaces`() {
        assertSuccess("  2 + 3 * ( 4 - 1 ) ", 11.0)
        assertSuccess("(2    + 3) *   4 - 6   / 2  ", 17.0)
        assertSuccess("  (  12   + 22 * 7)  /    (  33   +  (  12  * 3  - 8  )  )  *  3  ", 8.163934426229508)
    }
    @Test
    fun `large number`() {
        assertSuccess("1000000000000*9", 9000000000000.0)
        assertSuccess("2000000000000*1000000000000", 2000000000000000000000000.0)
    }

    @Test
    fun `small number`() {
        assertSuccess("0.0000000000000000000000001*9", 0.0000000000000000000000009) //25 нулей
        assertSuccess("0.00000000000000000000000001*9", 0.00000000000000000000000009) //26 нулей
        assertSuccess("0.000000000000000000000000001*9", 0.000000000000000000000000009) //27 нулей
        assertSuccess("0.0000000000000000000000000001*9", 0.0000000000000000000000000009) //28 нулей
        assertSuccess("0.00000000000000000000000000001*9", 0.00000000000000000000000000009) //29 нулей
        assertSuccess("0.000000000001*0.000000000001", 0.000000000000000000000001)
    }

}