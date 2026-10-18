package co.onmind.util

/**
 * Masks secret values in `onmind.ini` for the UI (`/app/config`) and restores them
 * on save, so secrets are never shipped to the browser while a normal round-trip
 * edit does not wipe them.
 *
 * Contract:
 *  - [mask] replaces the value of every known secret key with [PLACEHOLDER]
 *    (only when a value is present).
 *  - [restore] replaces [PLACEHOLDER] with the value currently stored on disk;
 *    any other value (typed by the operator) is kept as-is. If the placeholder
 *    appears for a key with no stored value, it becomes empty.
 *
 * This adds no configuration and does not change how secrets are loaded.
 */
object ConfigSecrets {
    const val PLACEHOLDER = "********"

    val KEYS: Set<String> = setOf(
        "auth.basic.pass",
        "db.password",
        "auth.jwt.secret",
        "jwt.secret",
        "s3.access_key",
        "s3.secret_key",
        "auth.otp.smtp_pass",
        "auth.otp.session_key",
        "kv.cosmosdb.key"
    )

    fun mask(content: String): String = mapLines(content) { key, value ->
        if (key in KEYS && value.isNotBlank()) PLACEHOLDER else value
    }

    fun restore(incoming: String, current: String): String {
        val stored = values(current)
        return mapLines(incoming) { key, value ->
            if (key in KEYS && value == PLACEHOLDER) stored[key] ?: "" else value
        }
    }

    /** key -> last recorded value for non-comment `key = value` lines. */
    private fun values(content: String): Map<String, String> {
        val map = HashMap<String, String>()
        content.lineSequence().forEach { line ->
            parse(line)?.let { (k, v) -> map[k] = v }
        }
        return map
    }

    private fun mapLines(content: String, f: (String, String) -> String): String {
        val endsWithNewline = content.endsWith("\n")
        val out = content.split("\n").joinToString("\n") { line ->
            val parsed = parse(line)
            if (parsed == null) {
                line
            } else {
                val (key, value) = parsed
                val newValue = f(key, value)
                if (newValue == value) line else "$key = $newValue"
            }
        }
        return if (endsWithNewline && !out.endsWith("\n")) "$out\n" else out
    }

    /** Parse `key = value`, ignoring comments/blank lines. Null when it is not a property line. */
    private fun parse(line: String): Pair<String, String>? {
        val trimmed = line.trim()
        if (trimmed.isEmpty() || trimmed.startsWith("#") || trimmed.startsWith(";")) return null
        val idx = line.indexOf('=')
        if (idx < 0) return null
        val key = line.substring(0, idx).trim()
        if (key.isEmpty()) return null
        return key to line.substring(idx + 1).trim()
    }
}
