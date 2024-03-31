package calculator

import java.math.BigInteger
import kotlin.math.pow

class Calc {
    val variables = mutableMapOf<String, BigInteger>()
    val operatorsMap = mapOf("+" to 0, "-" to 0, "*" to 1, "/" to 1, "^" to 2)

    fun execute(str: String) {
        if (str.contains("="))
            assignVariable(str)
        else
            equation(str)
    }

    private fun assignVariable(str: String) {
        val parts = str.split("=")
        if (parts.size > 2) {
            println("Invalid assignment")
            return
        }
        val identifier = parts[0].trim()
        if (!Regex("[a-zA-Z]+").matches(identifier)) {
            println("Invalid identifier")
            return
        }
        val v = parts[1].trim()
        (v.toBigIntegerOrNull() ?: variables[v]).let {
            if (it != null)
                variables[identifier] = it
            else
                println("Invalid assignment")
        }
    }

    private fun equation(str: String) {
        val numbers = mutableListOf<BigInteger>(BigInteger("0"))
        val eq = checkEquation(str)

        if (eq.isEmpty()) {
            println("Invalid expression"); return
        }

        val postEquation = infixToPostfix(eq)
        while (postEquation.isNotEmpty()) {
            val u = postEquation.removeFirst()
            if (u in operatorsMap.keys) {
                with(numbers) { performOperation(removeLast(), removeLast(), u).also { add(it) } }
            } else {
                val result = u.toBigIntegerOrNull() ?: variables[u]
                if (result == null) {
                    println("Unknown variable"); return
                }
                numbers.add(result)
            }
        }
        println(numbers.removeLast())
    }

    fun performOperation(right: BigInteger, left: BigInteger, operator: String): BigInteger {
        return when (operator) {
            "+" -> left + right
            "-" -> left - right
            "*" -> left * right
            "/" -> left / right
            else -> BigInteger(left.toDouble().pow(right.toDouble()).toString())
        }
    }

    fun checkEquation(eq: String): List<String> {
        val el = mutableListOf<String>()
        var parenthesesCount = 0
        var variable = ""
        var operator = "+"
        var space = false
        val clearAll = { variable = ""; operator = ""; space = false }

        for (c in eq.trim()) {
            when {
                c == '(' -> {
                    if (variable.isNotEmpty()) return emptyList()
                    clearAll()
                    parenthesesCount++; el.add(c.toString())
                    operator = c.toString()
                }

                c == ')' -> {
                    if (operator.isNotEmpty()) return emptyList()
                    clearAll()
                    parenthesesCount--; el.add(c.toString())
                }

                c == '+' -> {
                    if (operator.isEmpty()) {
                        clearAll()
                        operator = c.toString()
                        el.add(operator)
                    }
                    if (operator != "+") return emptyList()
                }

                c == '-' -> {
                    if (operator.isEmpty()) {
                        clearAll()
                        operator = c.toString()
                    } else if (operator == "-") {
                        operator = "+"; el.removeLastOrNull()
                    } else if (operator == "+") {
                        operator = "-"; el.removeLastOrNull()
                    } else return emptyList()
                    el.add(operator)
                }

                c == '*' || c == '/' || c == '^' -> {
                    if (operator.isNotEmpty()) return emptyList()
                    clearAll()
                    operator = c.toString(); el.add(operator)
                }

                c.isLetterOrDigit() -> {
                    if (operator.isEmpty() && space) {
                        return emptyList()
                    } else {
                        operator = ""; space = false
                    }
                    if (variable.isNotEmpty()) el.removeLast()
                    variable = "$variable$c"
                    el.add(variable)
                }

                c == ' ' -> {
                    space = true
                }

                else -> {
                    return emptyList()
                }
            }
        }
        if (parenthesesCount != 0 || (variable.isEmpty() && el.last() != ")")) el.clear()
        return el
    }

    fun infixToPostfix(equation: List<String>): MutableList<String> {
        val postfix = mutableListOf<String>()
        val operators = mutableListOf<String>()

        for (s in equation) {
            if (s in operatorsMap.keys) {
                if (operators.isEmpty() || operators.last() == "(") {
                    operators.add(s)
                } else if (operators.last() != "(" && operatorsMap[s]!! > operatorsMap[operators.last()]!!) {
                    operators.add(s)
                } else if (operators.last() != "(" && operatorsMap[s]!! <= operatorsMap[operators.last()]!!) {
                    while (operators.isNotEmpty()
                        && operators.last() != "("
                        && operatorsMap[operators.last()]!! >= operatorsMap[s]!!
                    ) {
                        postfix.add(operators.removeLast())
                    }
                    operators.add(s)
                }
            } else if (s == "(") {
                operators.add(s)
            } else if (s == ")") {
                var o = operators.removeLast()
                while (o != "(") {
                    postfix.add(o)
                    o = operators.removeLast()
                }
            } else {
                postfix.add(s)
            }
        }
        while (operators.isNotEmpty()) {
            postfix.add(operators.removeLast())
        }
        return postfix
    }
}

fun checkCommand(input: String): Boolean {
    when (input) {
        "/help" -> println(
            """
            The program performs all major arithmetic operations:
                + : addition of two operands
                - : subtraction of two operands
                * : multiplication of two operands
                / : division between two operands
                ^ : power operator
                (): parentheses to group operations in higher priority
                [Supports sign { - or + } at the beginning. ex: -(34+5-7)]
                [Space between numbers and variables is not allowed : 54 43 + 45 -> Invalid expression]
                [Equations can't end with an operator : (4 + 2 +  ) -> Invalid expression]
            """.trimIndent()
        )

        "/exit" -> {
            println("Bye!"); return true
        }

        else -> println("Unknown command")
    }
    return false
}

fun main() {

    val advancedCalculator = Calc()

    while (true) {
        val input = readln().trim()
        when {
            input.startsWith('/') -> if (checkCommand(input)) break
            input.isEmpty() -> continue
            else -> advancedCalculator.execute(input)
        }
    }
}