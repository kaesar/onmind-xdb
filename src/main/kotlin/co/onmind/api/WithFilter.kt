package co.onmind.api

/**
 * Restricted-grammar parser for the ABC `with` filter (the free-form WHERE of the
 * express query language). It keeps simple filters usable while rejecting anything
 * that could smuggle arbitrary SQL (UNION/SELECT, sub-selects, stacked statements,
 * comments, parentheses).
 *
 * Grammar:
 *
 *     filter    := condition ( (AND | OR) condition )*
 *     condition := column OP value
 *     OP        := = | != | <> | < | > | <= | >= | LIKE | NOT LIKE
 *     value     := 'string' | "string" | number | column | NULL | TRUE | FALSE
 *
 * `LIKE` accepts the standard SQL pattern syntax (`value%`, `%value`, `%value%`,
 * multi-wildcard, ...); H2 evaluates it directly.
 */
object WithFilter {

    /**
     * Returns `null` when [input] matches the grammar, or a short human-readable
     * reason when it does not.
     */
    fun validate(input: String): String? = try {
        Parser(tokenize(input)).parse()
        null
    } catch (e: ParseException) {
        e.reason
    }

    private class ParseException(val reason: String) : Exception(reason)

    private val IDENT = Regex("[A-Za-z_][A-Za-z0-9_]*")
    private val NUMBER = Regex("-?\\d+(?:\\.\\d+)?")
    private val OPERATORS = setOf("=", "!=", "<>", "<", ">", "<=", ">=")
    private val BOOLEANS = setOf("NULL", "TRUE", "FALSE")

    // SQL keywords that must never appear as a bare column/value atom.
    private val RESERVED = setOf(
        "UNION", "SELECT", "FROM", "WHERE", "DROP", "INSERT", "UPDATE", "DELETE",
        "CREATE", "ALTER", "EXEC", "EXECUTE", "CALL", "MERGE", "TRUNCATE", "GRANT",
        "REVOKE", "INTO", "VALUES", "SET", "PRAGMA", "ATTACH", "DETACH", "DECLARE",
        "ORDER", "GROUP", "BY", "HAVING", "LIMIT", "OFFSET", "DISTINCT", "ALL",
        "RETURNING", "WITH", "CASE", "WHEN", "THEN", "ELSE", "END", "JOIN",
        "AND", "OR", "NOT", "LIKE", "IS", "IN", "BETWEEN", "EXISTS"
    )

    // Every character must be covered by exactly one of these atoms; the whole
    // input is scanned so that non-matching characters are rejected.
    private val TOKEN = Regex(
        "\\s+|'(?:[^']|'')*'|\"(?:[^\"]|\"\")*\"|<>|<=|>=|!=|=|<|>|[A-Za-z_][A-Za-z0-9_]*|-?\\d+(?:\\.\\d+)?"
    )

    private fun isQuoted(token: String): Boolean =
        token.length >= 2 && (
            (token.startsWith("'") && token.endsWith("'")) ||
                (token.startsWith("\"") && token.endsWith("\""))
        )

    private fun unquote(token: String): String = token.substring(1, token.length - 1)

    private fun tokenize(input: String): List<String> {
        val tokens = mutableListOf<String>()
        var pos = 0
        for (m in TOKEN.findAll(input)) {
            if (m.range.first != pos) throw ParseException("unexpected character '${input[pos]}'")
            if (m.value.isNotBlank()) tokens += m.value
            pos = m.range.last + 1
        }
        if (pos != input.length) throw ParseException("unexpected character '${input[pos]}'")
        return tokens
    }

    private class Parser(private val tokens: List<String>) {
        private var i = 0

        fun parse() {
            condition()
            while (i < tokens.size) {
                val connector = tokens[i].uppercase()
                if (connector != "AND" && connector != "OR") {
                    throw ParseException("expected AND/OR but found '${tokens[i]}'")
                }
                i++
                condition()
            }
        }

        private fun condition() {
            val column = tokens.getOrNull(i) ?: throw ParseException("unexpected end of filter")
            if (!IDENT.matches(column) || column.uppercase() in RESERVED) {
                throw ParseException("expected a column name, found '$column'")
            }
            i++
            val op = tokens.getOrNull(i)?.uppercase() ?: throw ParseException("missing operator after '$column'")
            when {
                op == "LIKE" -> { i++; likePattern(column) }
                op == "NOT" -> {
                    i++
                    if (tokens.getOrNull(i)?.uppercase() != "LIKE") {
                        throw ParseException("'NOT' must be followed by LIKE")
                    }
                    i++
                    likePattern(column)
                }
                op in OPERATORS -> { i++; value() }
                else -> throw ParseException("invalid operator '$op'")
            }
        }

        private fun likePattern(column: String) {
            val token = tokens.getOrNull(i) ?: throw ParseException("LIKE on '$column' requires a quoted pattern")
            if (!isQuoted(token)) throw ParseException("LIKE on '$column' requires a quoted pattern")
            i++
        }

        private fun value() {
            val token = tokens.getOrNull(i) ?: throw ParseException("missing value")
            if (isQuoted(token) || NUMBER.matches(token)) {
                i++
                return
            }
            val upper = token.uppercase()
            if (IDENT.matches(token) && (upper in BOOLEANS || upper !in RESERVED)) {
                i++
                return
            }
            throw ParseException("invalid value '$token'")
        }
    }
}
