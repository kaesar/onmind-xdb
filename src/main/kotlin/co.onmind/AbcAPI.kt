package co.onmind

import com.fasterxml.jackson.databind.exc.MismatchedInputException
import org.http4k.core.HttpHandler
import org.http4k.core.Request
import org.http4k.core.Response
import xy.db.*
import java.time.LocalDateTime
import java.lang.Exception
import java.lang.IllegalStateException
import java.sql.SQLException
//import xy.db.Back

/**
 * Created by Cesar Andres Arcila Buitrago from Colombia on 08/06/21.
 * ABC = Articulable Backend Controller
 */

class AbcAPI(): AbstractAPI() {

    private val dbc = onmindxdb.dbc
    private val xdb = RDB()

    fun useControl(): HttpHandler = { request: Request ->  control(request) }

    fun control(req: Request): Response {
        val body: AbcBody
        try {
            body = mapper.readValue(req.bodyString(), AbcBody::class.java)
        }
        catch (mie: MismatchedInputException) {
            return if (mie.message!!.contains("AbcBody[\"puts\"]"))
                sendError("It seems that 'puts' isn't expressed as valid JSON string. Try to stringify, dumps or pre-request script.")
            else
                sendError(mie.message ?: "ERROR")
        }
        val way = body.way
        val what = body.what
        val from = body.from
        var some = body.some
        val with = body.with  // ¿id?...
        val show = body.show ?: "*"
        var how = body.how
        var puts = body.puts
        var cast = body.cast
        var size = body.size
        val call = body.call  // ¿login?...
        val keys = body.keys
        val user = body.user
        val auth = body.auth
        val pin = body.pin
        var query = ""
        val prefix = if (from != "xyany") from.substring(2..4) else "any"
        var choice = call?.lowercase() ?: "?"
        val limit = onmindxdb.queryLimit ?: 1200
        if (size.toInt() > limit) size = limit.toString()
//println("way: ${way}\nwhat: ${what}\nfrom: ${from}\nsome: ${some}\nwith: ${with}\nputs: ${puts}\nshow: ${show}\nhow: ${how}\ncall: ${call}")

        try {
            if (what != "!") {  // find, insert, update, delete, create, drop, invoke
                choice = what.lowercase()

                if (listOf("find","insert","update","delete","create","drop","invoke").indexOf(what) < 0) {
                    return sendError("'WHAT' is wrong. Valid values are: find, insert, update, delete, create, drop, invoke")
                }

                if (!call.isNullOrEmpty() && what != "invoke") {
                    return sendError("That kind of 'WHAT' can't be mixed with 'CALL'")
                }

                if (what != "invoke") {
                    if (some.isNullOrEmpty()) {
                        return sendError("'SOME' is missing. You must set it!")
                    }
                    else if (listOf("xydot","xykit","xyone","xykey").indexOf(from.lowercase()) < 0 && !some.contains("."))  // check scheme
                        some = some.uppercase() + ".SHEET"
                    else
                        some = some.uppercase()

                    if (listOf("find","insert","update","delete","drop").indexOf(what) > -1 && from.lowercase() != "xykit") {
                        query = "SELECT * FROM xykit WHERE kit01='$some'"
                        val exists = xdb.forQuery(query)
                        if (exists?.size == 0) {
                            return sendError("Does not exists the object: $some")
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
                return sendSuccess(rows)
            }
            else if (choice == "insert") {
                val now = LocalDateTime.now()
                val id = putID(1, user, now)
                if (puts == null) {  // .isNullOrEmpty()
                    return sendError("'PUTS' is missing. You must set it!")
                }
                /*else if (how.isNullOrEmpty()) {
                    return sendError("'HOW' is missing. You must set it!")
                }

                how += ",${prefix.lowercase()}xy"
                puts += ";${some!!.uppercase()}"
                if (cast.isNullOrEmpty())
                    cast += ",string"
                if (listOf("dot","kit","one","key").indexOf(prefix.lowercase()) < 0) {
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
                //if (!how.contains("id")) {
                    how += ",id"
                    puts += ";$id"
                    if (cast.isNullOrEmpty())
                        cast += ",string"
                //}
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
                    return sendError("Column count does not match!")
                }

                if (!how.contains("01") && !how.contains("02")) {
                    return sendError("Column '${prefix}02' is missing and is required!")
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
                xdb.forUpdate(query)*/

                val dbAny = DBAny()
                val ioAny = mapper.readValue(puts, IOAny::class.java)
                val map = dbAny.mapValues(ioAny)
                if (map["any01"] == null && map["any02"] == null) {
                    return sendError("Column '${prefix}02' is missing and is required!")
                }
                query = dbAny.getInsert(map as MutableMap<String, Any?>, some!!, user, id, now, pin)
                xdb.forUpdate(query)

                query = "SELECT * FROM ${from.lowercase()} WHERE id='$id'"
                val rows = xdb.forQuery(query)
                val row = rows?.get(0) ?: mutableMapOf()
                xdb.savePointAny(row)
                return sendSuccess(row)
            }
            else if (choice == "update") {
                if (with.isNullOrEmpty()) {
                    return sendError("'WITH' is missing. You must set it!")
                }
                else if (puts.isNullOrEmpty()) {
                    return sendError("'PUTS' is missing. You must set it!")
                }
                /*else if (how.isNullOrEmpty()) {
                    return sendError("'HOW' is missing. You must set it!")
                }
                val howList = how.split(",")
                val putsList = puts.split(";")  // use ';' to avoid mix values with ','
                if (howList.size != putsList.size) {
                    return sendError("Column count does not match!")
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
                query += " WHERE id='$with'"
                xdb.forUpdate(query)*/

                val dbAny = DBAny()
                val ioAny = mapper.readValue(puts, IOAny::class.java)
                val map = dbAny.mapValues(ioAny)
                query = dbAny.getUpdate(map as MutableMap<String, Any?>, with)
                xdb.forUpdate(query)

                query = "SELECT * FROM ${from.lowercase()} WHERE id='$with'"
                val rows = xdb.forQuery(query)
                val row = rows?.get(0) ?: mutableMapOf()
                xdb.savePointAny(row)
                return sendSuccess(row)
            }
            else if (choice == "delete") {
                if (with.isNullOrEmpty()) {
                    return sendError("'WITH' is missing. You must set it!")
                }
                query = "DELETE FROM ${from.lowercase()} WHERE id='$with'"
                val rowCount = xdb.forUpdate(query)
                xdb.movePoint(with)
                return sendSuccess(rowCount.toString())
            }
            //else if (choice == "idlist") {
            //    if (with.isNullOrEmpty()) {
            //        return sendError("'WITH' is missing. You must set it!")
            //    }
            //    query = "SELECT id FROM ${from.lowercase()} WHERE ${prefix?.lowercase()}xy='${some}' AND $with"
            //    val rows = xdb.forQuery(query)
            //    return sendSuccess(rows)
            //}
            else if (choice == "create") {
                return create(req)
            }
            else if (choice == "drop") {
                return drop(req)
            }
            else if (choice == "whoami") {
                return whoami(req)
            }
            else if (choice == "login") {
                return login(req)
            }
            else {
                return sendError("Wrong Request, please check it!")
            }
        }
        catch (ise: IllegalStateException) {
            return sendError(ise.message ?: "ERROR")
        }
        catch (sqle: SQLException) {
            return if (sqle.message!!.uppercase().contains("UXY"))
                sendError("Already exists data with the same code")
            else
                sendError(sqle.message ?: "ERROR")
        }
    }

    fun create(req: Request): Response {
        val body: AbcBody = mapper.readValue(req.bodyString(), AbcBody::class.java)
        val from = body.from
        val name = body.some
        var scheme = body.with ?: "SHEET"
        val title = body.show ?: name?.uppercase()
        var how = body.how
        val spec = body.puts ?: "{}"
        var cast = body.cast
        var size = body.size
        val call = body.call
        val keys = body.keys
        val user = body.user
        val auth = body.auth
        val pin = body.pin
        val hint = body.hint ?: ""
        val eval = body.level
        val level: Int = if (eval.isNullOrEmpty() || eval == "null") 90 else eval.toInt()
        val kind = if (from != "xyany") from.substring(2..4) else "ANY"
        val repo = if (from.substring(0..1) == "xy") "BOX" else "DUO"
        val icon = body.icon ?: "table"

        if (!pin.isNullOrEmpty()) scheme = "SHEET"
        //val ok = ctx.queryParam("ok") ?: body.get("ok")?.toString() ?: "false"
        //if (pin.isNullOrEmpty()) {
        //    return sendError("Falta nomenclatura de identificacion privada")
        //}
        if (onmindxdb.os.contains("inux")) {  // Linux
            return sendError("This system is for production and you dont have this priviledge")
        }
        if (scheme.isEmpty()) {
            return sendError("The scheme is required")
        }
        else if (listOf("SETUP","SYSTEM","GLOBAL","COMMON","CARE","CUSTOM","SHEET").indexOf(scheme) < 0) {
            return sendError("The scheme is not recognized: $scheme")
        }
        if (name.isNullOrEmpty()) {
            return sendError("The internal code or name is required")
        }
        if (title.isNullOrEmpty()) {
            return sendError("The descriptive name (title) is required")
        }
        if (user.isNullOrEmpty()) {
            return sendError("The user is required")
        }
        else if (listOf("ONE","KEY","YOU","GET","SET","TOP","ASK","PUT","SUM","ADD","LAY","ANY").indexOf(kind) < 0) {
            return sendError("The archetype or object class is not recognized: $kind")
        }
        //if (listOf("BOX","DUO").indexOf(repo) < 0) {
        //    return sendError("The repository is not recognized: $repo")
        //}
        else if (repo != "BOX" && listOf("ONE","KEY","YOU").indexOf(kind) > -1) {
            return sendError("The archetype or object class does not correspond to the repository: $kind ~> $repo")
        }

        val code = "${name.uppercase()}.${scheme.uppercase()}"
        try {
            var now = LocalDateTime.now()
            val id = putID(1, user, now)
            val kit12 = "${code.lowercase()}.0"
            var kiton: String? = now.toString()  //.replace("T"," ")

            var query = """
                INSERT INTO xykit (id,kitxy,kit01,kit02,kit03,kit04,kit07,kit08,kit09,kit11,kit12,kit14,kitif,kitof,kitby,kiton,kitat)
                VALUES ('$id','$scheme','$code','${name.uppercase()}','$title','$hint',$level,'01','+','$scheme','$kit12','$icon',5,'$user','$user','$kiton','$kiton')
                """.trimIndent()

            //xdb.forUpdate(query)

            //now = LocalDateTime.now()
            //val idFit = putID(1, user, now)
            //kiton = now.toString().replace("T"," ")
            //query = """
            //    INSERT INTO xyfit (id,fitxy,fit01,fit02,fit03,fit07,fit09,fit16,fit17,fit18,fitof,fitby,fiton,fitat)
            //    VALUES ('$idFit','$code','$kit12','0','$repo',5,'$kind','IO','$spec','*','$user','$user','$kiton','$kiton')
            //    """.trimIndent()

            val dbKit = DBKit()
            val xyKit = XYKit(id,scheme,code,name.uppercase(),title,hint, null,null,level,"01","+",null,scheme,kit12,null,icon,null,null,5,null,user,user,kiton,kiton)
            val map = dbKit.mapValues(xyKit)
            query = dbKit.getInsert(map as MutableMap<String, Any?>)
            val rowCount = xdb.forUpdate(query)

            query = "SELECT * FROM xykit WHERE id='$id'"
            val rows = xdb.forQuery(query)
            val row = rows?.get(0) ?: mutableMapOf()
            xdb.savePointKit(row)
            //return sendSuccess(row)
            return sendSuccess(rowCount.toString())
        }
        catch (sqle: SQLException) {
            if (sqle.message!!.uppercase().contains("KIT01"))
                return sendError("Already exists the object: $code")
            else
                return sendError(sqle.message ?: "ERROR")
        }
    }

    fun drop(req: Request): Response {  // POST => name, scheme, kind, user
        val body: AbcBody = mapper.readValue(req.bodyString(), AbcBody::class.java)
        val from = body.from
        val name = body.some
        var scheme = body.with ?: "SHEET"
        val user = body.user
        val pin = body.pin
        val kind = if (from != "xyany") from.substring(2..4) else "ANY"
        val repo = if (from.substring(0..1) == "xy") "BOX" else "DUO"

        if (!pin.isNullOrEmpty()) scheme = "SHEET"
        //val ok = ctx.queryParam("ok") ?: body.get("ok")?.toString() ?: "false"
        //if (pin.isNullOrEmpty()) {
        //    return sendError("Falta nomenclatura de identificacion privada")
        //}
        if (onmindxdb.os.contains("inux")) {  // Linux
            return sendError("This system is for production and you dont have this priviledge")
        }
        if (scheme.isEmpty()) {
            return sendError("The scheme is required")
        }
        else if (listOf("SETUP","SYSTEM","GLOBAL","COMMON","CARE","CUSTOM","SHEET").indexOf(scheme) < 0) {
            return sendError("The scheme is not recognized: $scheme")
        }
        if (name.isNullOrEmpty()) {
            return sendError("The internal code or name is required")
        }
        if (user.isNullOrEmpty()) {
            return sendError("The user is required")
        }
        else if (listOf("ONE","KEY","YOU","GET","SET","TOP","ASK","PUT","SUM","ADD","LAY","ANY").indexOf(kind) < 0) {
            return sendError("The archetype or object class is not recognized: $kind")
        }
        //if (listOf("BOX","DUO").indexOf(repo) < 0) {
        //    return sendError("The repository is not recognized: $repo")
        //}
        else if (repo != "BOX" && listOf("ONE","KEY","YOU").indexOf(kind) > -1) {
            return sendError("The archetype or object class does not correspond to the repository: $kind ~> $repo")
        }

        val code = "${name.uppercase()}.${scheme.uppercase()}"
        try {
            val prefix = kind.lowercase()
            val kit12 = "${code.lowercase()}.0"

            //var query = "SELECT * FROM xykit WHERE kit01='$code'"
            var query = "SELECT * FROM $from WHERE ${prefix}xy='$code' LIMIT 1"
            val res2 = xdb.forQuery(query)
            if (!res2.isNullOrEmpty()) {
                return sendError("This action is not allowed if there is data")
            }

            query = "DELETE FROM xykit WHERE kit01='$code'"
            val rowCount = xdb.forUpdate(query)
            xdb.movePoint(code, "kit")
            return sendSuccess(rowCount.toString())
        }
        catch (sqle: SQLException) {
            return sendError(sqle.message ?: "ERROR")
        }
    }

    private fun whoami(ctx: Request): Response {  // For Windows
        //if (onmindxdb.os.contains("inux")) {  // Linux
        //    return sendError("This system is for production and you don't have this priviledge")
        //}
        val result = mapOf(
            //"success" to true,
            "user" to System.getProperty("user.name"),
            "home" to System.getProperty("user.home"),
            "os" to System.getProperty("os.name")
        )
        return sendSuccess(result)
    }

    private fun login(ctx: Request): Response {
        return sendSuccess("OK")
    }

}
