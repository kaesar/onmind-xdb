package co.onmind.io

/**
 * Created by Cesar Andres Arcila Buitrago from Colombia on 29/01/16.
 */

data class AbcBack(
    val ok: Boolean = false
    ,val status: String
    ,val message: String? = null
    ,val total: Int? = null
    ,val data: Any? = null
) {
    constructor(message: String): this(true,"ok",message,null,null) { }

    constructor(ok: Boolean, status: String, message: String): this(ok,status,message,null,null) { }

    constructor(ok: Boolean, status: String, message: String, data: Any?): this(ok,status,message,null,data) { }

    constructor(data: Any?): this(true,"ok",null,null,data) { }

    constructor(data: Any?, total: Int?): this(true,"ok",null,total,data) { }
}
