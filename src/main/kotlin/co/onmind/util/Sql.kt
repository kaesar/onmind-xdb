package co.onmind.util

/**
 * Helpers for the express SQL query language.
 *
 * The ABC endpoint materializes in-memory SQL by string interpolation (that is its
 * documented behaviour, see HELP.md). These helpers quote VALUES so legitimate data
 * (apostrophes, names like "O'Hara", etc.) is preserved and injection through values
 * is neutralized, without changing the express flow or its configuration.
 */
object Sql {
    /** Quote a value as an SQL string literal, escaping embedded single quotes. */
    fun literal(value: Any?): String = "'" + (value?.toString() ?: "").replace("'", "''") + "'"
}
