package spbu.wecalc.project

sealed class CalcError {
    object DivisionByZero : CalcError()
    object SyntaxError : CalcError()
}
