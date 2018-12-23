package pifuck

import pifuck.interpreter.Interpreter
import pifuck.parser.Program
import pifuck.parser.TokenType
import pifuck.parser.parse
import pifuck.parser.tokenizer
import java.io.FileReader

fun readFileAndParse(path: String): Program {
    val source = FileReader(path).readText()
    val tokens = tokenizer(source)
    return parse(tokens.filter { !it.value.isBlank() || it.type == TokenType.EOF })
}

fun printUsage() {
    println("Usage: ./pifuck file.pifuck")
}

fun main(args: Array<String>) {
    if (args.size == 1) {
        if (args[0].endsWith(".pifuck")) {
            val program = readFileAndParse(args[0])

            Interpreter().interpretProgram(program)
        } else {
            printUsage()
        }
    }
}
