package co.onmind

import io.javalin.http.Handler
import io.javalin.http.Context
import java.time.LocalDateTime
import java.lang.Exception
import java.lang.IllegalStateException
import java.sql.SQLException
//import xy.db.Back
import xy.db.RDB
//import xy.db.DBKit
//import xy.db.DBAny

/**
 * Created by Cesar Andres Arcila Buitrago from Colombia on 08/06/21.
 * ABC = Articulable Backend Controller
 */

class AbcAPI(): AbstractAPI() {

    private val dbc = onminddai.dbc
    private val xdb = RDB()

    val useControl: Handler = Handler { ctx -> control(ctx) }

    fun control(ctx: Context) {
        val body = ctx.getBodyAsJson()
        val way: String = ctx.queryParam("way") ?: body.get("way")?.toString() ?: "sql"   // sql, mql, abc (abc = mql-like)
        val what: String = ctx.queryParam("what") ?: body.get("what")?.toString() ?: "!"  // find, insert, update, delete, create, drop, invoke
        val from: String = ctx.queryParam("from") ?: body.get("from")?.toString() ?: "xyany"
        var some: String? = ctx.queryParam("some") ?: body.get("some")?.toString()
        val with: String? = ctx.queryParam("with") ?: body.get("with")?.toString()
        val show: String? = ctx.queryParam("show") ?: body.get("show")?.toString() ?: "*"
        var how: String? = ctx.queryParam("how") ?: body.get("how")?.toString()
        var puts: String? = ctx.queryParam("puts") ?: body.get("puts")?.toString()
        var cast: String? = ctx.queryParam("cast") ?: body.get("cast")?.toString()
        var size: String = ctx.queryParam("size") ?: body.get("size")?.toString() ?: "1200"
        val call: String? = ctx.queryParam("call") ?: body.get("call")?.toString()        // ¿login?...
        val keys: String? = ctx.queryParam("keys") ?: body.get("keys")?.toString()
        val user: String? = ctx.queryParam("user") ?: body.get("user")?.toString()
        val auth: String? = ctx.queryParam("auth") ?: body.get("auth")?.toString()
        val pin: String? = ctx.queryParam("pin") ?: body.get("pin")?.toString()
        var query = ""
        val prefix: String? = if (from != "xyany") from.substring(2..4) else "any"
        var choice = call?.lowercase() ?: "?"
        val limit = onminddai.queryLimit ?: 1200
        if (size.toInt() > limit) size = limit.toString()
//println("way: ${way}\nwhat: ${what}\nfrom: ${from}\nsome: ${some}\nwith: ${with}\nputs: ${puts}\nshow: ${show}\nhow: ${how}\ncall: ${call}")

        try {
            if (what != "!") {  // find, insert, update, delete, create, drop, invoke
                choice = what.lowercase()

                if (listOf("find","insert","update","delete","create","drop","invoke").indexOf(what) < 0) {
                    ctx.error("'WHAT' is wrong. Valid values are: find, insert, update, delete, create, drop, invoke")
                    return
                }

                if (!call.isNullOrEmpty() && what != "invoke") {
                    ctx.error("That kind of 'WHAT' can't be mixed with 'CALL'")
                    return
                }

                if (what != "invoke") {
                    if (some.isNullOrEmpty()) {
                        ctx.error("'SOME' is missing. You must set it!")
                        return
                    }
                    else if (listOf("xydot","xykit","xyone","xykey").indexOf(from.lowercase()) < 0 && !some.contains("."))  // check scheme
                        some = some.uppercase() + ".SHEET"
                    else
                        some = some.uppercase()

                    if (listOf("find","insert","update","delete","drop").indexOf(what) > -1 && from.lowercase() != "xykit") {
                        query = "SELECT * FROM xykit WHERE kit01='$some'"
                        val exists = xdb.forQuery(query)
                        if (exists?.size == 0) {
                            ctx.error("Does not exists the object: $some")
                            return
                        }
                    }
                }
            }

            if (choice == "find") {
                query = "SELECT ${show?.replace(";",",")} FROM ${from.lowercase()} WHERE ${prefix?.lowercase()}xy='${some}' "
                if (!with.isNullOrEmpty())
                    query += "AND $with"
                query += " LIMIT ${size}"

                val rows = xdb.forQuery(query)
                ctx.success(rows)
                /*xdb.forQuery(query)
                    .onSuccess { rows ->
                        ctx.header()
                        ctx.response()
                            .setStatusCode(200)
                            .end(toRows(rows))
                    }
                    .onFailure { ctx.error(it.message ?: "ERROR") }*/
            }
            else if (choice == "insert") {
                if (puts.isNullOrEmpty()) {
                    ctx.error("'PUTS' is missing. You must set it!")
                    return
                }
                else if (how.isNullOrEmpty()) {
                    ctx.error("'HOW' is missing. You must set it!")
                    return
                }
                how += ",${prefix?.lowercase()}xy"
                puts += ";${some!!.uppercase()}"
                if (cast.isNullOrEmpty())
                    cast += ",string"
                if (listOf("dot","kit","one","key").indexOf(prefix!!.lowercase()) < 0) {
                    how += ",${prefix.lowercase()}as"
                    puts += ";${some.lowercase()}.0"
                    if (cast.isNullOrEmpty())
                        cast += ",string"
                    if (prefix == "any" && !how.contains("anyis")) {
                        how += ",${prefix.lowercase()}is"
                        puts += ";${prefix.uppercase()}"
                        if (cast.isNullOrEmpty())
                            cast += ",string"
                    }
                }
                val now = LocalDateTime.now()
                if (!how.contains("id")) {
                    val id = putID(1, user, now)
                    how += ",id"
                    puts += ";$id"
                    if (cast.isNullOrEmpty())
                        cast += ",string"
                }
                if (prefix == "any" && !how.contains("of")) {
                    how += ",${prefix.lowercase()}of"
                    puts += ";$user"
                    if (cast.isNullOrEmpty())
                        cast += ",string"
                }
                if (prefix == "any" && !how.contains("by")) {
                    how += ",${prefix.lowercase()}by"
                    puts += ";$user"
                    if (cast.isNullOrEmpty())
                        cast += ",string"
                }
                if (prefix == "any" && !how.contains("on")) {
                    how += ",${prefix.lowercase()}on"
                    puts += ";$now"
                    if (cast.isNullOrEmpty())
                        cast += ",string"
                }
                if (prefix == "any" && !how.contains("at")) {
                    how += ",${prefix.lowercase()}at"
                    puts += ";$now"
                    if (cast.isNullOrEmpty())
                        cast += ",string"
                }
                var howList = how.split(",")
                var putsList = puts.split(";")  // use ';' to avoid mix values with ','
                if (howList.size != putsList.size) {
                    ctx.error("Column count does not match!")
                    return
                }

                if (!how.contains("01") && !how.contains("02")) {
                    ctx.error("Column '${prefix}02' is missing and is required!")
                    return
                }
                else if (!how.contains("01")) {
                    val col02 = putsList[howList.indexOf("${prefix}02")]
                    var col01 = "${col02.uppercase()}~${some.uppercase()}"
                    if (pin != null) col01 += "~$pin"
                    how += ",${prefix}01"
                    puts += ";$col01"
                    if (cast.isNullOrEmpty())
                        cast += ",string"
                    howList = how.split(",")
                    putsList = puts.split(";")  // use ';' to avoid mix values with ','
                }

                query = "INSERT INTO ${from.lowercase()} (${how.replace(";",",")}) VALUES ("
                val castList = if (cast.isNullOrEmpty()) cast?.split(",") else listOf()
                var vary = ""
                var i = 0
                while (i < putsList.size) {
                    vary = "'${putsList[i]}'"
                    if ((castList?.size ?: 0) > 0 && castList!![i] != "string")
                        vary = putsList[i]
                    if (i > 0)
                        vary = ",$vary"
                    query += vary
                    i++
                }
                query += ")"
                val rowCount = xdb.forUpdate(query)
                ctx.success(rowCount.toString())
            }
            else if (choice == "update") {
                if (with.isNullOrEmpty()) {
                    ctx.error("'WITH' is missing. You must set it!")
                    return
                }
                else if (puts.isNullOrEmpty()) {
                    ctx.error("'PUTS' is missing. You must set it!")
                    return
                }
                else if (how.isNullOrEmpty()) {
                    ctx.error("'HOW' is missing. You must set it!")
                    return
                }
                val howList = how.split(",")
                val putsList = puts.split(";")  // use ';' to avoid mix values with ','
                if (howList.size != putsList.size) {
                    ctx.error("Column count does not match!")
                    return
                }

                query = "UPDATE ${from.lowercase()} SET "
                val castList = if (cast.isNullOrEmpty()) cast?.split(",") else listOf()
                var vary = ""
                var i = 0
                while (i < putsList.size) {
                    vary = "${howList[i]}='${putsList[i]}'"
                    if ((castList?.size ?: 0) > 0 && castList!![i] != "string")
                        vary = "${howList[i]}=${putsList[i]}"
                    if (i > 0)
                        vary = ", $vary"
                    query += vary
                    i++
                }
                query += " WHERE $with"
                val rowCount = xdb.forUpdate(query)
                ctx.success(rowCount.toString())
            }
            else if (choice == "delete") {
                if (with.isNullOrEmpty()) {
                    ctx.error("'WITH' is missing. You must set it!")
                    return
                }
                query = "DELETE FROM ${from.lowercase()} WHERE " + with
                val rowCount = xdb.forUpdate(query)
                ctx.success(rowCount.toString())
            }
            else if (choice == "create")
                create(ctx)
            else if (choice == "drop")
                drop(ctx)
            else if (choice == "whoami")
                whoami(ctx)
            else if (choice == "login")
                login(ctx)
            else
                ctx.error("Wrong Request, please check it!")
            //ctx.error("way: ${way}\nwhat: ${what}\nfrom: ${from}\nsome: ${some}\nwith: ${with}\nputs: ${puts}\nshow: ${show}\nhow: ${how}\nkeys: ${keys}\ncall: ${call}")
        }
        catch (ise: IllegalStateException) {
            ctx.error(ise.message ?: "ERROR")
        }
        catch (sqle: SQLException) {
            if (sqle.message!!.contains("UXY"))
                ctx.error("Already exists a row with the same code")
            else
                ctx.error(sqle.message ?: "ERROR")
        }
    }

    fun create(ctx: Context) {
        val body = ctx.getBodyAsJson()
        val name = ctx.queryParam("some") ?: body.get("some")?.toString()
        var scheme = ctx.queryParam("with")?.uppercase() ?: body.get("with")?.toString()?.uppercase() ?: "SHEET"
        val title = ctx.queryParam("show") ?: body.get("show")?.toString() ?: name?.uppercase()
        val from = ctx.queryParam("from")?.uppercase() ?: body.get("from")?.toString()?.uppercase() ?: "xyany"
        val user = ctx.queryParam("user") ?: body.get("user")?.toString()
        val spec = ctx.queryParam("puts") ?: body.get("puts")?.toString() ?: "{}"
        val hint = ctx.queryParam("hint") ?: body.get("hint")?.toString() ?: ""
        val eval = ctx.queryParam("level") ?: body.get("level")?.toString()
        val level: Int = if (eval.isNullOrEmpty() || eval == "null") 90 else eval.toInt()
        val kind = if (from != "xyany") from.substring(2..4) else "ANY"
        val repo = if (from.substring(0..1) == "xy") "BOX" else "DUO"
        val icon = ctx.queryParam("icon") ?: body.get("icon")?.toString() ?: "table"
        val pin = ctx.queryParam("pin") ?: body.get("pin")?.toString()
        if (!pin.isNullOrEmpty()) scheme = "SHEET"
        //val ok = ctx.queryParam("ok") ?: body.get("ok")?.toString() ?: "false"
        //if (pin.isNullOrEmpty()) {
        //    ctx.error("Falta nomenclatura de identificacion privada")
        //    return
        //}
        if (onminddai.os.contains("inux")) {  // Linux
            ctx.error("This system is for production and you dont have this priviledge")
            return
        }
        if (scheme.isEmpty()) {
            ctx.error("El esquema es requerido")
            return
        }
        else if (listOf("SETUP","SYSTEM","GLOBAL","COMMON","CARE","CUSTOM","SHEET").indexOf(scheme) < 0) {
            ctx.error("El esquema no se reconoce: $scheme")
            return
        }
        if (name.isNullOrEmpty()) {
            ctx.error("El codigo o nombre interno es requerido")
            return
        }
        if (title.isNullOrEmpty()) {
            ctx.error("El nombre descriptivo (titulo) es requerido")
            return
        }
        if (user.isNullOrEmpty()) {
            ctx.error("El usuario es requerido")
            return
        }
        else if (listOf("ONE","KEY","YOU","GET","SET","TOP","ASK","PUT","SUM","ADD","LAY","ANY").indexOf(kind) < 0) {
            ctx.error("El arquetipo o clase de objeto no se reconoce: $kind")
            return
        }
        //if (listOf("BOX","DUO").indexOf(repo) < 0) {
        //    ctx.error("El repositorio no se reconoce: $repo")
        //    return
        //}
        else if (repo != "BOX" && listOf("ONE","KEY","YOU").indexOf(kind) > -1) {
            ctx.error("El arquetipo o clase de objeto no corresponde al repositorio: $kind ~> $repo")
            return
        }

        val code = "${name.uppercase()}.${scheme.uppercase()}"
        try {
            var now = LocalDateTime.now()
            val id = putID(1, user, now)
            val kit12 = "${code.lowercase()}.0"
            var kiton: String? = now.toString().replace("T"," ")

            var query = """
                INSERT INTO xykit (id,kitxy,kit01,kit02,kit03,kit04,kit07,kit08,kit09,kit11,kit12,kit14,kitif,kitof,kitby,kiton,kitat)
                VALUES ('$id','$scheme','$code','${name.uppercase()}','$title','$hint',$level,'01','+','$scheme','$kit12','$icon',5,'$user','$user','$kiton','$kiton')
                """.trimIndent()

            xdb.forUpdate(query)

            now = LocalDateTime.now()
            val idFit = putID(1, user, now)
            kiton = now.toString().replace("T"," ")
            query = """
                INSERT INTO xyfit (id,fitxy,fit01,fit02,fit03,fit07,fit09,fit16,fit17,fit18,fitof,fitby,fiton,fitat)
                VALUES ('$idFit','$code','$kit12','0','$repo',5,'$kind','IO','$spec','*','$user','$user','$kiton','$kiton')
                """.trimIndent()

            val rowCount = xdb.forUpdate(query)
            ctx.success(rowCount.toString())
        }
        catch (sqle: SQLException) {
            if (sqle.message!!.contains("KIT01"))
                ctx.error("Already exists the object: $code")
            else
                ctx.error("Ha surgido un imprevisto creando $code: ${sqle.message}")
        }
    }

    fun drop(ctx: Context) {  // POST => name, scheme, kind, user
        val body = ctx.getBodyAsJson()
        val name = ctx.queryParam("some") ?: body.get("some")?.toString()
        var scheme = ctx.queryParam("with")?.uppercase() ?: body.get("with")?.toString()?.uppercase() ?: "SHEET"
        val from = ctx.queryParam("from")?.uppercase() ?: body.get("from")?.toString()?.uppercase() ?: "xyany"
        val user = ctx.queryParam("user") ?: body.get("user")?.toString()
        val kind = if (from != "xyany") from.substring(2..4) else "ANY"
        val repo = if (from.substring(0..1) == "xy") "BOX" else "DUO"
        val pin = ctx.queryParam("pin") ?: body.get("pin")?.toString()
        if (!pin.isNullOrEmpty()) scheme = "SHEET"
        //val ok = ctx.queryParam("ok") ?: body.get("ok")?.toString() ?: "false"
        //if (pin.isNullOrEmpty()) {
        //    ctx.error("Falta nomenclatura de identificacion privada")
        //    return
        //}
        if (onminddai.os.contains("inux")) {  // Linux
            ctx.error("This system is for production and you dont have this priviledge")
            return
        }
        if (scheme.isEmpty()) {
            ctx.error("El esquema es requerido")
            return
        }
        else if (listOf("SETUP","SYSTEM","GLOBAL","COMMON","CARE","CUSTOM","SHEET").indexOf(scheme) < 0) {
            ctx.error("El esquema no se reconoce: $scheme")
            return
        }
        if (name.isNullOrEmpty()) {
            ctx.error("El codigo o nombre interno es requerido")
            return
        }
        if (user.isNullOrEmpty()) {
            ctx.error("El usuario es requerido")
            return
        }
        else if (listOf("ONE","KEY","YOU","GET","SET","TOP","ASK","PUT","SUM","ADD","LAY","ANY").indexOf(kind) < 0) {
            ctx.error("El arquetipo o clase de objeto no se reconoce: $kind")
            return
        }
        //if (listOf("BOX","DUO").indexOf(repo) < 0) {
        //    ctx.error("El repositorio no se reconoce: $repo")
        //    return
        //}
        else if (repo != "BOX" && listOf("ONE","KEY","YOU").indexOf(kind) > -1) {
            ctx.error("El arquetipo o clase de objeto no corresponde al repositorio: $kind ~> $repo")
            return
        }

        val code = "${name.uppercase()}.${scheme.uppercase()}"
        try {
            val prefix = kind.lowercase()
            val kit12 = "${code.lowercase()}.0"

            var query = "SELECT * FROM xykit WHERE kit01='$code'"
            /*val res = xdb.forQuery(query)
            if (!res.isNullOrEmpty()) {
                ctx.error("Does not exists the object: $code")
                return
            }*/

            query = "SELECT * FROM $from WHERE ${prefix}xy='$code' LIMIT 1"
            val res2 = xdb.forQuery(query)
            if (!res2.isNullOrEmpty()) {
                ctx.error("No se permite esta acción si existen datos.")
                return
            }

            query = "DELETE FROM xybit WHERE bit06='$kit12' AND bitxy='$code'"
            xdb.forUpdate(query)

            query = "DELETE FROM xyfit WHERE fit01='$kit12' AND fitxy='$code'"
            xdb.forUpdate(query)

            query = "DELETE FROM xykit WHERE kit01='$code'"
            val rowCount = xdb.forUpdate(query)
            ctx.success(rowCount.toString())
        }
        catch (e: Exception) {
            ctx.error("Ha surgido un imprevisto creando $code: ${e.message}")
        }
    }

    private fun whoami(ctx: Context) {  // For Windows
        if (onminddai.os.contains("inux")) {  // Linux
            ctx.error("This system is for production and you dont have this priviledge")
            return
        }
        //val result: Map<String, Any?> = mutableMapOf<String, Any?>()
        val result = mapOf(
            "success" to true,
            "user" to System.getProperty("user.name"),
            "home" to System.getProperty("user.home"),
            "os" to System.getProperty("os.name")
        )
        /*val result = buildJsonObject {
            val success = true
            val user = System.getProperty("user.name")
            val home = System.getProperty("user.home")
            val os = System.getProperty("os.name")
            put("success", "true")
            put("user", System.getProperty("user.name"))
            put("home", System.getProperty("user.home"))
            put("os", System.getProperty("os.name"))
        }*/
        ctx.success(result)
    }

    private fun login(ctx: Context) {
        ctx.success("OK")
    }
}
