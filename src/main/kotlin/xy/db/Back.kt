package xy.db

/**
 * Created by Cesar Andres Arcila Buitrago from Colombia on 29/01/16.
 */

data class Back(
     val success: Boolean = false
    ,val status: String
    ,val message: String? = null
    ,val total: Long? = null
    ,val data: Any? = null
) {
    constructor(message: String): this(true,"ok",message,null,null) { }

    constructor(success: Boolean, status: String, message: String): this(success,status,message,null,null) { }

    constructor(success: Boolean, status: String, message: String, data: Any?): this(success,status,message,null,data) { }

    constructor(data: Any?): this(true,"ok",null,null,data) { }

    constructor(data: Any?, total: Long): this(true,"ok",null,total,data) { }
}
