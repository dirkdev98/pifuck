package pifuck.interpreter

import pifuck.interpreter.VariableValue.Operations.ADD
import pifuck.interpreter.VariableValue.Operations.DIVIDE
import pifuck.interpreter.VariableValue.Operations.EQUAL
import pifuck.interpreter.VariableValue.Operations.GREATER
import pifuck.interpreter.VariableValue.Operations.MULTIPLY
import pifuck.interpreter.VariableValue.Operations.NOT_EQUAL
import pifuck.interpreter.VariableValue.Operations.SMALLER
import pifuck.interpreter.VariableValue.Operations.SUBTRACT
import pifuck.parser.UserValue
import pifuck.parser.UserValue.Bool
import pifuck.parser.UserValue.Decimal
import pifuck.parser.UserValue.NULL
import pifuck.parser.UserValue.Str
import java.math.BigDecimal

sealed class VariableValue(val type: String) {
    companion object {
        fun fromUserValue(value: UserValue): VariableValue {
            return when (value) {
                is Str     -> VarStr(value.value)
                is Decimal -> VarDec(value.value)
                is Bool    -> VarBool(value.value)
                NULL       -> VarNull()
            }
        }
    }

    private enum class Operations(val strName: String) {
        ADD("add"), SUBTRACT("subtract"), MULTIPLY("multiply"), DIVIDE("divide"), EQUAL("equal"), NOT_EQUAL("not equal"), GREATER("greater"), SMALLER("smaller")
    }

    private fun throwTypeError(operation: Operations, other: VariableValue): Nothing {
        throw Error("Invalid operation '${operation.strName}' on ${this.type} and ${other.type}")
    }

    open fun add(other: VariableValue): VariableValue {
        throwTypeError(ADD, other)
    }

    open fun subtract(other: VariableValue): VariableValue {
        throwTypeError(SUBTRACT, other)
    }

    open fun multiply(other: VariableValue): VariableValue {
        throwTypeError(MULTIPLY, other)
    }

    open fun divide(other: VariableValue): VariableValue {
        throwTypeError(DIVIDE, other)
    }

    open fun equal(other: VariableValue): VariableValue {
        throwTypeError(EQUAL, other)
    }

    open fun notEqual(other: VariableValue): VariableValue {
        throwTypeError(NOT_EQUAL, other)
    }

    open fun greater(other: VariableValue): VariableValue {
        throwTypeError(GREATER, other)
    }

    open fun smaller(other: VariableValue): VariableValue {
        throwTypeError(SMALLER, other)
    }

    abstract fun toCustomString(): String

    class VarBool(val value: Boolean) : VariableValue("boolean") {
        override fun equal(other: VariableValue): VariableValue {
            return if (other !is VarBool) {
                VarBool(false)
            } else {
                VarBool(value == other.value)
            }
        }

        override fun notEqual(other: VariableValue): VariableValue {
            return if (other !is VarBool) {
                VarBool(true)
            } else {
                VarBool(value != other.value)
            }
        }

        override fun toCustomString(): String {
            return if (this.value) "true" else "false"
        }
    }

    class VarNull : VariableValue("null") {
        override fun toCustomString(): String {
            return "null"
        }

    }

    class VarStr(val value: String) : VariableValue("string") {
        override fun add(other: VariableValue): VariableValue {
            return VarStr(value + other.toCustomString())
        }

        override fun equal(other: VariableValue): VariableValue {
            return VarBool(value == other.toCustomString())
        }

        override fun notEqual(other: VariableValue): VariableValue {
            return VarBool(value != other.toCustomString())
        }

        override fun toCustomString(): String {
            return value
        }
    }

    class VarDec(val value: BigDecimal) : VariableValue("decimal") {
        override fun add(other: VariableValue): VariableValue {
            return if (other is VarDec) {
                VarDec(value.add(other.value))
            } else if (other is VarStr) {
                VarStr(this.toCustomString() + other.value)
            } else {
                super.add(other)
            }
        }

        override fun subtract(other: VariableValue): VariableValue {
            return if (other !is VarDec) {
                super.subtract(other)
            } else {
                VarDec(this.value.subtract(other.value))
            }
        }

        override fun multiply(other: VariableValue): VariableValue {
            return if (other !is VarDec) {
                super.subtract(other)
            } else {
                VarDec(this.value.multiply(other.value))
            }
        }

        override fun divide(other: VariableValue): VariableValue {
            return if (other !is VarDec) {
                super.subtract(other)
            } else {
                VarDec(this.value.divide(other.value))
            }
        }

        override fun equal(other: VariableValue): VariableValue {
            return if (other !is VarDec) {
                VarBool(false)
            } else {
                VarBool(this.value.compareTo(other.value) == 0)
            }
        }

        override fun notEqual(other: VariableValue): VariableValue {
            return if (other !is VarDec) {
                VarBool(true)
            } else {
                VarBool(this.value.compareTo(other.value) != 0)
            }
        }

        override fun greater(other: VariableValue): VariableValue {
            return if (other !is VarDec) {
                super.greater(other)
            } else {
                VarBool(this.value.compareTo(other.value) == 1)
            }
        }

        override fun smaller(other: VariableValue): VariableValue {
            return if (other !is VarDec) {
                super.greater(other)
            } else {
                VarBool(this.value.compareTo(other.value) == -1)
            }
        }

        override fun toCustomString(): String {
            return value.toString()
        }
    }
}
