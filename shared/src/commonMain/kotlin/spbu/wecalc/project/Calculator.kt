package spbu.wecalc.project

interface Calculator {
    fun evaluate(expression: String): CalcResult
}