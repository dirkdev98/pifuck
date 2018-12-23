package pifuck.interpreter

import pifuck.interpreter.VariableValue.VarBool
import pifuck.interpreter.VariableValue.VarDec
import pifuck.interpreter.VariableValue.VarNull
import pifuck.interpreter.VariableValue.VarStr
import pifuck.parser.Expression
import pifuck.parser.Expression.Add
import pifuck.parser.Expression.Divide
import pifuck.parser.Expression.Equal
import pifuck.parser.Expression.Greater
import pifuck.parser.Expression.Multiply
import pifuck.parser.Expression.NotEqual
import pifuck.parser.Expression.Smaller
import pifuck.parser.Expression.Subtract
import pifuck.parser.Expression.Value
import pifuck.parser.Program
import pifuck.parser.Statement
import pifuck.parser.Statement.Call
import pifuck.parser.Statement.For
import pifuck.parser.Statement.ForEach
import pifuck.parser.Statement.Function
import pifuck.parser.Statement.IfStatement
import pifuck.parser.Statement.Include
import pifuck.parser.Statement.Print
import pifuck.parser.Statement.VariableDeclaration
import pifuck.parser.UserValue
import pifuck.readFileAndParse
import java.math.BigDecimal

class Interpreter {
    private val functions: MutableMap<String, List<Statement>> = mutableMapOf()
    private val variables: MutableMap<String, VariableValue> = mutableMapOf()

    private fun getVariableOrNothing(name: String): VariableValue {
        return variables[name]
            ?: throw Error("Variable $name is used before declaration.")
    }

    fun interpretProgram(program: Program) {
        program.lines.forEach {
            when (it) {
                is Include             -> parseAndExecuteInclude(it.value.value)
                is Print               -> printValue(it.expr)
                is VariableDeclaration -> declareVariable(it.name.value, it.initializer)
                is ForEach             -> runForEach(it.till, it.tempVar, it.block)
                is For                 -> runForLoop(it.condition, it.afterLoop, it.block)
                is IfStatement         -> runIfStatement(it.condition, it.block)
                is Function            -> declareFunction(it.name.value, it.block)
                is Call                -> callFunction(it.name.value)
            }
        }
        println()
    }

    private fun runBlock(list: List<Statement>) {
        this.interpretProgram(Program(list))
    }

    private fun parseAndExecuteInclude(path: String) {
        runBlock(readFileAndParse(path).lines)
    }

    private fun printValue(value: Expression?) {
        if (value == null) {
            println()
        } else {
            println(evaluateExpression(value).toCustomString())
        }
    }

    private fun declareVariable(name: String, value: Expression?) {
        if (value == null) {
            this.variables[name] = VarNull()
        } else {
            this.variables[name] = evaluateExpression(value)
        }
    }

    private fun runForEach(till: UserValue.Str, tempVarName: UserValue.Str, block: List<Statement>) {
        val tillValue = getVariableOrNothing(till.value)

        when (tillValue) {
            is VarDec -> {
                val realTillValue = tillValue.value.toInt()
                val intermediate = 1
                for (i in intermediate..realTillValue) {
                    this.variables[tempVarName.value] = VarDec(BigDecimal(i))
                    runBlock(block)
                }
                this.variables.remove(tempVarName.value)
            }
            is VarStr -> {
                val strValue = tillValue.value
                for (c in strValue) {
                    this.variables[tempVarName.value] = VarStr(Character.toString(c))
                    runBlock(block)
                }
                this.variables.remove(tempVarName.value)
            }
            else      -> throw Error("For each is not supported on ${tillValue.type}")
        }
    }

    private fun expandArithmaticExpression(expr: Expression) {
        when (expr) {
            is Add      -> {
                if (expr.left !is UserValue.Str) {
                    throw Error("Expect left handside to be a variable.")
                }
                declareVariable(expr.left.value, expr)
            }
            is Subtract -> {
                if (expr.left !is UserValue.Str) {
                    throw Error("Expect left handside to be a variable.")
                }
                declareVariable(expr.left.value, expr)
            }
            is Multiply -> {
                if (expr.left !is UserValue.Str) {
                    throw Error("Expect left handside to be a variable.")
                }
                declareVariable(expr.left.value, expr)
            }
            is Divide   -> {
                if (expr.left !is UserValue.Str) {
                    throw Error("Expect left handside to be a variable.")
                }
                declareVariable(expr.left.value, expr)
            }
            else        -> {
                throw Error("Expecting a arithmetic expression.")
            }
        }
    }

    private fun runForLoop(condition: Expression, afterLoop: Expression, block: List<Statement>) {
        var intermediate = evaluateExpression(condition) as? VarBool
            ?: throw Error("For condition should return a boolean.")

        while (intermediate.value) {
            runBlock(block)
            expandArithmaticExpression(afterLoop)
            intermediate = evaluateExpression(condition) as? VarBool ?:
                    throw Error("For condition should return a boolean.")
        }
    }

    private fun runIfStatement(condition: Expression, block: List<Statement>) {
        val result = evaluateExpression(condition)
        if (result is VarBool) {
            if (result.value) {
                runBlock(block)
            }
        } else {
            throw Error("${result.type} is not available in if condition.")
        }
    }

    private fun declareFunction(name: String, block: List<Statement>) {
        functions[name] = block
    }

    private fun callFunction(name: String) {
        val block = functions[name]
            ?: throw Error("Function $name should be declared before usage.")
        runBlock(block)
    }

    private fun evaluateExpression(expr: Expression): VariableValue {
        val variableOrValue: (value: UserValue) -> VariableValue = {
            if (it is UserValue.Str && variables.containsKey(it.value)) {
                variables[it.value]!!
            } else {
                VariableValue.fromUserValue(it)
            }
        }

        return when (expr) {
            is Value    -> variableOrValue(expr.value)
            is Add      -> variableOrValue(expr.left).add(variableOrValue(expr.right))
            is Subtract -> variableOrValue(expr.left).subtract(variableOrValue(expr.right))
            is Multiply -> variableOrValue(expr.left).multiply(variableOrValue(expr.right))
            is Divide   -> variableOrValue(expr.left).divide(variableOrValue(expr.right))
            is Equal    -> variableOrValue(expr.left).equal(variableOrValue(expr.right))
            is NotEqual -> variableOrValue(expr.left).notEqual(variableOrValue(expr.right))
            is Greater  -> variableOrValue(expr.left).greater(variableOrValue(expr.right))
            is Smaller  -> variableOrValue(expr.left).smaller(variableOrValue(expr.right))
        }
    }
}
