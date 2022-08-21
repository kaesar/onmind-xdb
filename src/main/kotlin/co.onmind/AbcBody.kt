package co.onmind

data class AbcBody(
    val way: String = "sql",    // sql, mql, abc (abc = mql-like)
    val what: String = "!",     // find, insert, update, delete, create, drop, invoke
    val from: String = "xyany",
    val some: String? = null,
    val with: String? = null,
    val show: String? = null,   // "*"
    val how: String? = null,
    val puts: String? = null,
    val cast: String? = null,
    val size: String = "1200",
    val call: String? = null,   // ¿login?...
    val keys: String? = null,
    val user: String? = null,
    val auth: String? = null,
    val pin: String? = null,
    val hint: String? = null,
    val icon: String? = null,
    val level: String? = null
)
