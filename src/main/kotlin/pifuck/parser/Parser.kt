package pifuck.parser

import pifuck.parser.Expression.Add
import pifuck.parser.Expression.Divide
import pifuck.parser.Expression.Equal
import pifuck.parser.Expression.Greater
import pifuck.parser.Expression.Multiply
import pifuck.parser.Expression.NotEqual
import pifuck.parser.Expression.Smaller
import pifuck.parser.Expression.Subtract
import pifuck.parser.Expression.Value
import pifuck.parser.Statement.Call
import pifuck.parser.Statement.For
import pifuck.parser.Statement.ForEach
import pifuck.parser.Statement.Function
import pifuck.parser.Statement.IfStatement
import pifuck.parser.Statement.Include
import pifuck.parser.Statement.Print
import pifuck.parser.Statement.VariableDeclaration
import pifuck.parser.TokenType.*
import pifuck.parser.UserValue.Bool
import pifuck.parser.UserValue.Decimal
import pifuck.parser.UserValue.NULL
import pifuck.parser.UserValue.Str
import java.math.BigDecimal

data class Parser(val tokens: List<Token>, var idx: Int) {
    fun peekType(): TokenType {
        return if (idx >= tokens.size) {
            TokenType.EOF
        } else {
            tokens[idx].type
        }
    }

    fun increaseIdx() {
        this.idx += 1
    }

    fun getNext(): Token {
        return if (idx >= tokens.size) {
            Token(TokenType.EOF, "")
        } else {
            this.increaseIdx()
            tokens[idx - 1]
        }

    }
}

fun parse(tokens: List<Token>): Program {
    tokens.forEach {
        if (it.type == ERROR) {
            throw Error("Unexpected token ${it.value}")
        }
    }
    val parser = Parser(tokens, 0)
    val statements = mutableListOf<Statement?>()
    while (parser.peekType() != TokenType.EOF) {
        statements.add(parseLine(parser))
    }
    return Program(statements.mapNotNull { it })
}

fun getUserValueOfRaw(token: Token): UserValue {
    val cleaned = token.value.substring(1, token.value.length - 1)
    return when {
        cleaned == "null"  -> NULL
        cleaned == "true"  -> Bool(true)
        cleaned == "false" -> Bool(false)
        cleaned.matches(
            Regex(
                """-?[0-9]+(\.[0-9]+([eE]\+[0-9]+)?)?"""
            )
        )                  -> Decimal(BigDecimal(cleaned))
        else               -> Str(cleaned)
    }
}

fun Parser.getLineEnding() {
    if (peekType() !== EOL) {
        throw Error("Expecting new line. ${peekType()}")
    }
    getNext()
}

fun parseBlock(parser: Parser): List<Statement> {
    val result = mutableListOf<Statement?>()

    while (parser.peekType() != EOF && parser.peekType() != END_BLOCK) {
        result.add(parseLine(parser))
    }
    if (parser.peekType() == EOF) {
        throw Error("Expected end of block statement.")
    }

    // End of block line
    parser.increaseIdx()
    parser.getLineEnding()

    return result.mapNotNull { it }
}

fun parseLine(parser: Parser): Statement? {
    return when (parser.peekType()) {
        EOL        -> {
            parser.getLineEnding()
            null
        }
        EOF        -> null
        DASH_DASH  -> parseInclude(parser)
        USER_VALUE -> parsePrint(parser)
        VAR_DECL   -> parseVariableDeclaration(parser)
        FOREACH    -> parseForeach(parser)
        FOR        -> parseFor(parser)
        IF         -> parseIf(parser)
        STAR_STAR  -> parseFunction(parser)
        FUN_CALL   -> parseFunCall(parser)
        else       -> null
    }
}

fun parseInclude(parser: Parser): Statement {
    parser.increaseIdx()
    if (parser.peekType() != USER_VALUE) {
        throw Error("Missing include file name")
    }
    val name = getUserValueOfRaw(parser.getNext()) as? Str
        ?: throw Error("Expecting file name to be a string")
    parser.getLineEnding()
    return Include(name)
}

fun parsePrint(parser: Parser): Statement {
    val printValue = parseExpression(parser)
    parser.getLineEnding()
    return Print(printValue)
}

fun parseVariableDeclaration(parser: Parser): Statement {
    parser.increaseIdx()
    if (parser.peekType() != USER_VALUE) {
        throw Error("Expecting variable name.")
    }
    val name = getUserValueOfRaw(parser.getNext()) as? Str
        ?: throw Error("Variable name should be a string.")

    if (parser.peekType() !== TokenType.VAR_DECL) {
        throw Error("Unfinished variable declaration.")
    } else {
        parser.increaseIdx()
    }

    val expr = parseExpression(parser)
    parser.getLineEnding()
    return VariableDeclaration(name, expr)
}

fun parseForeach(parser: Parser): Statement {
    parser.increaseIdx()
    if (parser.peekType() != USER_VALUE) {
        throw Error("Expecting variable name.")
    }
    val initialized = getUserValueOfRaw(parser.getNext()) as? Str
        ?: throw Error("Variable name should be a string.")
    if (parser.peekType() != USER_VALUE) {
        throw Error("Expecting variable name.")
    }
    val tmpVar = getUserValueOfRaw(parser.getNext()) as? Str
        ?: throw Error("Variable name should be a string.")
    parser.getLineEnding()

    return ForEach(initialized, tmpVar, parseBlock(parser))
}

fun parseFor(parser: Parser): Statement {
    parser.increaseIdx()
    val condition = parseExpression(parser)
    val afterLoop = parseExpression(parser)
    if (condition == null || afterLoop == null) {
        throw Error("Expecting condition expression and after loop expression in for-loop.")
    }
    parser.getLineEnding()
    return For(condition, afterLoop, parseBlock(parser))
}

fun parseIf(parser: Parser): Statement {
    parser.increaseIdx()
    val condition = parseExpression(parser)
        ?: throw Error("Expecting condition in if statement")

    parser.getLineEnding()
    return IfStatement(condition, parseBlock(parser))
}

fun parseFunction(parser: Parser): Statement {
    parser.increaseIdx()
    if (parser.peekType() != USER_VALUE) {
        throw Error("Expecting function name")
    }
    val name = getUserValueOfRaw(parser.getNext()) as? Str
        ?: throw Error("Function name should be a string.")
    parser.getLineEnding()
    return Function(name, parseBlock(parser))
}

fun parseFunCall(parser: Parser): Statement {
    parser.increaseIdx()
    if (parser.peekType() != USER_VALUE) {
        throw Error("Function name should be a string.")
    }
    val name = getUserValueOfRaw(parser.getNext()) as? Str
        ?: throw Error("Function name should be a string.")
    parser.getLineEnding()

    return Call(name)
}

fun parseExpression(parser: Parser): Expression? {
    if (parser.peekType() == EOL || parser.peekType() == EOF) {
        return null
    }
    if (parser.peekType() != USER_VALUE) {
        throw Error("Expecting user value. ${parser.tokens[parser.idx]}")
    }
    val left = getUserValueOfRaw(parser.getNext())

    val getNextAndUserValue: () -> UserValue = helper@{
        parser.getNext()
        if (parser.peekType() != USER_VALUE) {
            throw Error("Expecting user value.")
        }
        return@helper getUserValueOfRaw(parser.getNext())
    }

    when (parser.peekType()) {
        ADD       -> {
            val right = getNextAndUserValue()
            return Add(left, right)
        }
        SUBTRACT  -> {
            val right = getNextAndUserValue()
            return Subtract(left, right)
        }
        MULTIPLY  -> {
            val right = getNextAndUserValue()
            return Multiply(left, right)
        }
        DIVIDE    -> {
            val right = getNextAndUserValue()
            return Divide(left, right)
        }
        EQUAL     -> {
            val right = getNextAndUserValue()
            return Equal(left, right)
        }
        NOT_EQUAL -> {
            val right = getNextAndUserValue()
            return NotEqual(left, right)
        }
        GREATER   -> {
            val right = getNextAndUserValue()
            return Greater(left, right)
        }
        SMALLER   -> {
            val right = getNextAndUserValue()
            return Smaller(left, right)
        }
        else      -> return Value(left)
    }
}
