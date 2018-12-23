package pifuck.parser

import pifuck.parser.UserValue.Str
import java.math.BigDecimal

class Program(val lines: List<Statement>)

sealed class Statement {
    class Include(val value: Str) : Statement()
    class Print(val expr: Expression?) : Statement()
    class VariableDeclaration(val name: Str, val initializer: Expression?) : Statement()
    class ForEach(val till: Str, val tempVar: Str, val block: List<Statement>) : Statement()
    class For(val condition: Expression, val afterLoop: Expression, val block: List<Statement>) : Statement()
    class IfStatement(val condition: Expression, val block: List<Statement>) : Statement()
    class Function(val name: Str, val block: List<Statement>) : Statement()
    class Call(val name: Str) : Statement()
}

sealed class Expression {
    class Value(val value: UserValue) : Expression()

    class Add(val left: UserValue, val right: UserValue) : Expression()
    class Subtract(val left: UserValue, val right: UserValue) : Expression()
    class Multiply(val left: UserValue, val right: UserValue) : Expression()
    class Divide(val left: UserValue, val right: UserValue) : Expression()

    class Equal(val left: UserValue, val right: UserValue) : Expression()
    class NotEqual(val left: UserValue, val right: UserValue) : Expression()
    class Greater(val left: UserValue, val right: UserValue) : Expression()
    class Smaller(val left: UserValue, val right: UserValue) : Expression()
}

sealed class UserValue {
    class Str(val value: String) : UserValue()
    class Decimal(val value: BigDecimal) : UserValue()
    class Bool(val value: Boolean) : UserValue()
    object NULL : UserValue()
}
