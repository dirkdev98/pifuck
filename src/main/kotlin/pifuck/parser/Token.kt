package pifuck.parser

import pifuck.PI_CHAR_LIST
import pifuck.parser.TokenType.*
import java.util.regex.Pattern

// 3.                  line ending
// 3.1                 Variable
// 3.14                Start if statement
// 3.141               Start for loop
// 3.1415              +
// 3.14159             -
// 3.141592            *
// 3.1415926           /
// 3.14159265          end LineManager
// 3.141592653         start foreach
// 3.1415926535        Call function
// 3.14159265358       ==
// 3.141592653589      !=
// 3.1415926535897     \>
// 3.14159265358979    \<

enum class TokenType {
    EOF, ERROR, COMMENT, PI, DASH_DASH, STAR_STAR, USER_VALUE,

    // PI
    EOL,
    VAR_DECL, IF, FOR, ADD, SUBTRACT, MULTIPLY, DIVIDE, END_BLOCK, FOREACH, FUN_CALL, EQUAL, NOT_EQUAL, GREATER, SMALLER
}

data class Token(val type: TokenType, val value: String)

fun tokenizer(source: String): List<Token> {
    val list: MutableList<Token> =
        source.split("\n").map { it.split(Pattern.compile("\\s+")) }.map { parseTokens(it) }.flatten().toMutableList()
    list.add(Token(EOF, ""))

    return list.toList()
}

fun parseTokens(line: List<String>): List<Token> {
    return line.map { it.trim() }.filter { it != "" }.takeWhile { !(it.startsWith("//")) }.map {
        if (it == "--") {
            Token(DASH_DASH, it)
        } else if (it == "**") {
            Token(STAR_STAR, it)
        } else if (it.startsWith("+") && it.endsWith("+")) {
            Token(USER_VALUE, it)
        } else if (it.startsWith("3.")) {
            piToken(it)
        } else {
            return arrayListOf(Token(ERROR, it))
        }
    }
}

fun piToken(value: String): Token {
    value.forEachIndexed { index, c ->
        if (c != PI_CHAR_LIST[index]) {
            return Token(ERROR, value)
        }
    }
    return when (value) {
        "3."               -> Token(EOL, value)
        "3.1"              -> Token(VAR_DECL, value)
        "3.14"             -> Token(IF, value)
        "3.141"            -> Token(FOR, value)
        "3.1415"           -> Token(ADD, value)
        "3.14159"          -> Token(SUBTRACT, value)
        "3.141592"         -> Token(MULTIPLY, value)
        "3.1415926"        -> Token(DIVIDE, value)
        "3.14159265"       -> Token(END_BLOCK, value)
        "3.141592653"      -> Token(FOREACH, value)
        "3.1415926535"     -> Token(FUN_CALL, value)
        "3.14159265358"    -> Token(EQUAL, value)
        "3.141592653589"   -> Token(NOT_EQUAL, value)
        "3.1415926535897"  -> Token(GREATER, value)
        "3.14159265358979" -> Token(SMALLER, value)
        else               -> Token(ERROR, value)
    }
}
