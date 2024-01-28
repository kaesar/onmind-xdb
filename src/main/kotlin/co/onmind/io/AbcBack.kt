package co.onmind.io

import com.fasterxml.jackson.annotation.JsonAutoDetect

/**
 * Created by Cesar Andres Arcila Buitrago from Colombia on 29/01/16.
 */

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
data class AbcBack(
    val ok: Boolean = false,
    val status: String,
    val message: String? = null,
    val total: Int? = null,
    val data: List<Any?>? = null
) {
    constructor(message: String): this(true,"ok",message,null,null) { }

    constructor(ok: Boolean, status: String, message: String): this(ok,status,message,null,null) { }

    constructor(ok: Boolean, status: String, data:List<Any?>?, total: Int?): this(ok,status,null,total,data) { }

    constructor(data: List<Any?>?): this(true,"ok",null,null,data) { }

    constructor(data: List<Any?>?, total: Int?): this(true,"ok",null,total,data) { }
}
