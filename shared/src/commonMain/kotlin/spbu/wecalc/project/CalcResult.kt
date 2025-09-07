package spbu.wecalc.project

sealed class CalcResult {
    data class Success(val value: Double) : CalcResult()
    data class Failure(val error: CalcError) : CalcResult()
}
