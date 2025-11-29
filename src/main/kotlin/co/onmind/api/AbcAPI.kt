package co.onmind.api

import co.onmind.db.DBAny
import co.onmind.db.DBKey
import co.onmind.db.DBSet
import co.onmind.db.DBDoc
import co.onmind.db.DBKit
import co.onmind.db.RDB
import co.onmind.io.AbcBody
import co.onmind.io.AbcPair
import co.onmind.io.IOAny
import co.onmind.io.IOKey
import co.onmind.io.IOSet
import co.onmind.io.IODoc
import co.onmind.trait.AbstractAPI
import co.onmind.xy.XYKit
import com.fasterxml.jackson.databind.exc.MismatchedInputException
import org.http4k.core.HttpHandler
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.ContentType
import org.http4k.core.Status
import java.time.LocalDateTime
import java.lang.IllegalStateException
import java.sql.SQLException

/**
 * Created by Cesar Andres Arcila Buitrago from Colombia on 08/06/21.
 * ABC = Articulable Backend Controller
 */

class AbcAPI(): AbstractAPI() {

    private val dbc = onmindxdb.dbc
    private val xdb = RDB()

    fun useControl(): HttpHandler = { request: Request ->  mainControl(request) }

    fun mainControl(req: Request): Response {
        return try {
            val body = parseRequestBody(req)
            val context = RequestContext(body)
            
            validateRequest(context)?.let { return it }
            
            when (context.choice) {
                "find" -> handleFind(context)
                "insert" -> handleInsert(context)
                "update" -> handleUpdate(context)
                "delete" -> handleDelete(context)
                "create" -> create(body)
                "drop" -> drop(body)
                "define" -> define(body)
                "list" -> list(body)
                "whoami" -> whoami(req)
                "login" -> login(req)
                else -> sendError("Wrong Request, please check it!")
            }
        } catch (ise: IllegalStateException) {
            sendError(ise.message ?: "ERROR")
        } catch (sqle: SQLException) {
            if (sqle.message!!.uppercase().contains("UXY"))
                sendError("Already exists data with the same code")
            else
                sendError(sqle.message ?: "ERROR")
        } catch (ex: Exception) {
            sendError(ex.message ?: "ERROR")
        }
    }

    private fun parseRequestBody(req: Request): AbcBody {
        return try {
            mapper.readValue(req.bodyString(), AbcBody::class.java)
        } catch (jsone: MismatchedInputException) {
            throw IllegalArgumentException(
                if (jsone.message!!.contains("AbcBody[\"puts\"]"))
                    "It seems that 'puts' isn't expressed as valid JSON string. Try to stringify, dumps or pre-request script."
                else
                    jsone.message ?: "ERROR"
            )
        }
    }

    private data class RequestContext(
        val body: AbcBody
    ) {
        val prefix: String = if (body.from != "xyany") body.from.substring(2..4) else "any"
        val choice: String = body.call?.lowercase() ?: body.what?.lowercase() ?: "?"
        val size: String = run {
            val limit = onmindxdb.queryLimit ?: 1200
            if (body.size.toInt() > limit) limit.toString() else body.size
        }
        val some: String? = run {
            val result = body.some
            if (body.what != "invoke" && body.what != "list" && !result.isNullOrEmpty()) {
                if (listOf("xydot", "xykit", "xyone", "xykey").indexOf(body.from.lowercase()) < 0 && !result.contains("."))
                    result.uppercase() + ".SHEET"
                else
                    result.uppercase()
            } else {
                result
            }
        }
    }

    private fun validateRequest(context: RequestContext): Response? {
        val body = context.body
        
        if (body.what != "!") {
            val validOperations = listOf("find", "insert", "update", "delete", "create", "drop", "define", "list", "invoke")
            if (body.what !in validOperations) {
                return sendError("'WHAT' is wrong. Valid values are: ${validOperations.joinToString(", ")}")
            }

            if (!body.call.isNullOrEmpty() && body.what != "invoke") {
                return sendError("That kind of 'WHAT' can't be mixed with 'CALL'")
            }

            if (body.what != "invoke" && body.what != "list") {
                if (context.some.isNullOrEmpty()) {
                    return sendError("'SOME' is missing. You must set it!")
                }

                if (listOf("find", "insert", "update", "delete", "drop").contains(body.what) && body.from.lowercase() != "xykit") {
                    val query = "SELECT * FROM xykit WHERE kit01='${context.some}'"
                    val exists = xdb.forQuery(query)
                    if (exists?.size == 0) {
                        return sendError("Does not exists the object: ${context.some}")
                    }
                }
            }
        }
        return null
    }

    private fun handleFind(context: RequestContext): Response {
        val body = context.body
        val show = body.show?.replace(";", ",") ?: "*"
        var query = "SELECT $show FROM ${body.from.lowercase()} WHERE ${context.prefix.lowercase()}xy='${context.some}' "
        
        if (!body.with.isNullOrEmpty()) {
            query += "AND ${body.with}"
        }
        query += " LIMIT ${context.size}"

        val rows = xdb.forQuery(query)
        return sendSuccess(rows)
    }

    private fun handleInsert(context: RequestContext): Response {
        val body = context.body
        if (body.puts == null) {
            return sendError("'PUTS' is missing. You must set it!")
        }

        val now = LocalDateTime.now()
        val id = putID(1, body.user, now)
        val query = buildInsertQuery(context, id, now)
        
        xdb.forUpdate(query)
        return getInsertedRecord(context, id)
    }

    private fun buildInsertQuery(context: RequestContext, id: String, now: LocalDateTime): String {
        val body = context.body
        val map: Map<String, Any?>
        
        when (context.prefix) {
            "key" -> {
                val dbKey = DBKey()
                val ioKey = mapper.readValue(body.puts, IOKey::class.java)
                map = dbKey.mapValues(ioKey)
                validateRequiredColumns(map, "key01", "key02", context.prefix)
                return dbKey.getInsert(map as MutableMap<String, Any?>, context.some ?: "", body.user, id, now, body.pin)
            }
            "set" -> {
                val dbSet = DBSet()
                val ioSet = mapper.readValue(body.puts, IOSet::class.java)
                map = dbSet.mapValues(ioSet)
                validateRequiredColumns(map, "set01", "set02", context.prefix)
                return dbSet.getInsert(map as MutableMap<String, Any?>, context.some ?: "", body.user, id, now, body.pin)
            }
            "doc" -> {
                val dbDoc = DBDoc()
                val ioDoc = mapper.readValue(body.puts, IODoc::class.java)
                map = dbDoc.mapValues(ioDoc)
                validateRequiredColumns(map, "doc01", "doc02", context.prefix)
                return dbDoc.getInsert(map as MutableMap<String, Any?>, context.some ?: "", body.user, id, now, body.pin)
            }
            else -> {
                val dbAny = DBAny()
                val ioAny = mapper.readValue(body.puts, IOAny::class.java)
                map = dbAny.mapValues(ioAny)
                validateRequiredColumns(map, "any01", "any02", context.prefix)
                return dbAny.getInsert(map as MutableMap<String, Any?>, context.some ?: "", body.user, id, now, body.pin)
            }
        }
    }

    private fun validateRequiredColumns(map: Map<String, Any?>, col1: String, col2: String, prefix: String) {
        if (map[col1] == null && map[col2] == null) {
            throw IllegalArgumentException("Column '${prefix}02' is missing and is required!")
        }
    }

    private fun getInsertedRecord(context: RequestContext, id: String): Response {
        val query = "SELECT * FROM ${context.body.from.lowercase()} WHERE id='$id'"
        val rows = xdb.forQuery(query)
        val row = rows?.get(0) ?: mutableMapOf()
        
        savePoint(context.prefix, row)
        return sendSuccess(listOf(row))
    }

    private fun handleUpdate(context: RequestContext): Response {
        val body = context.body
        if (body.with.isNullOrEmpty()) {
            return sendError("'WITH' is missing. You must set it!")
        }
        if (body.puts.isNullOrEmpty()) {
            return sendError("'PUTS' is missing. You must set it!")
        }

        val query = buildUpdateQuery(context)
        xdb.forUpdate(query)
        return getUpdatedRecord(context)
    }

    private fun buildUpdateQuery(context: RequestContext): String {
        val body = context.body
        val withValue = body.with ?: throw IllegalArgumentException("'WITH' is missing")
        val map: Map<String, Any?>
        
        when (context.prefix) {
            "key" -> {
                val dbKey = DBKey()
                val ioKey = mapper.readValue(body.puts, IOKey::class.java)
                map = dbKey.mapValues(ioKey)
                return dbKey.getUpdate(map as MutableMap<String, Any?>, withValue)
            }
            "set" -> {
                val dbSet = DBSet()
                val ioSet = mapper.readValue(body.puts, IOSet::class.java)
                map = dbSet.mapValues(ioSet)
                return dbSet.getUpdate(map as MutableMap<String, Any?>, withValue)
            }
            "doc" -> {
                val dbDoc = DBDoc()
                val ioDoc = mapper.readValue(body.puts, IODoc::class.java)
                map = dbDoc.mapValues(ioDoc)
                return dbDoc.getUpdate(map as MutableMap<String, Any?>, withValue)
            }
            else -> {
                val dbAny = DBAny()
                val ioAny = mapper.readValue(body.puts, IOAny::class.java)
                map = dbAny.mapValues(ioAny)
                return dbAny.getUpdate(map as MutableMap<String, Any?>, withValue)
            }
        }
    }

    private fun getUpdatedRecord(context: RequestContext): Response {
        val query = "SELECT * FROM ${context.body.from.lowercase()} WHERE id='${context.body.with}'"
        val rows = xdb.forQuery(query)
        val row = rows?.get(0) ?: mutableMapOf()
        
        savePoint(context.prefix, row)
        return sendSuccess(listOf(row))
    }

    private fun handleDelete(context: RequestContext): Response {
        val body = context.body
        if (body.with.isNullOrEmpty()) {
            return sendError("'WITH' is missing. You must set it!")
        }

        val selectQuery = "SELECT * FROM ${body.from.lowercase()} WHERE id='${body.with}'"
        val rows = xdb.forQuery(selectQuery)
        val row = rows?.get(0) ?: mutableMapOf()

        val deleteQuery = "DELETE FROM ${body.from.lowercase()} WHERE id='${body.with}'"
        xdb.forUpdate(deleteQuery)
        xdb.movePoint(body.with, context.prefix)
        
        return sendSuccess(listOf(row))
    }

    private fun savePoint(prefix: String, row: MutableMap<String, Any?>) {
        when (prefix) {
            "key" -> xdb.savePointKey(row)
            "set" -> xdb.savePointSet(row)
            "doc" -> xdb.savePointDoc(row)
            else -> xdb.savePointAny(row)
        }
    }

    fun create(body: AbcBody): Response {
        val from = body.from
        val name = body.some
        var scheme = body.with ?: "SHEET"
        val title = body.show ?: name?.uppercase()
        var how = body.how
        val spec = body.puts ?: "[]"
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
        val repo = if (from.substring(0..1) == "co/onmind/xy") "BOX" else "DUO"
        val icon = body.icon ?: "table"

        if (!pin.isNullOrEmpty()) scheme = "SHEET"
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
                INSERT INTO xykit (id,kitxy,kit01,kit02,kit03,kit04,kit05,kit07,kit08,kit09,kit11,kit12,kit14,kitif,kitof,kitby,kiton,kitat)
                VALUES ('$id','$scheme','$code','${name.uppercase()}','$title','$hint','$spec',$level,'01','+','$scheme','$kit12','$icon',5,'$user','$user','$kiton','$kiton')
                """.trimIndent()

            val dbKit = DBKit()
            val xyKit = XYKit(id,scheme,code,name.uppercase(),title,hint,spec,null,level,"01","+",null,scheme,kit12,null,icon,null,null,5,null,user,user,kiton,kiton)
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

    fun drop(body: AbcBody): Response {  // POST => name, scheme, kind, user
        val from = body.from
        val name = body.some
        var scheme = body.with ?: "SHEET"
        val user = body.user
        val pin = body.pin
        val kind = if (from != "xyany") from.substring(2..4) else "ANY"
        val repo = if (from.substring(0..1) == "co/onmind/xy") "BOX" else "DUO"

        if (!pin.isNullOrEmpty()) scheme = "SHEET"
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
        else if (repo != "BOX" && listOf("ONE","KEY","YOU").indexOf(kind) > -1) {
            return sendError("The archetype or object class does not correspond to the repository: $kind ~> $repo")
        }

        val code = "${name.uppercase()}.${scheme.uppercase()}"
        try {
            val prefix = kind.lowercase()
            val kit12 = "${code.lowercase()}.0"

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

    fun define(body: AbcBody): Response {
        val from = body.from
        val name = body.some
        var scheme = body.with ?: "SHEET"
        val spec = body.puts ?: "[]"
        val user = body.user
        val pin = body.pin
        val kind = if (from != "xyany") from.substring(2..4) else "ANY"
        val repo = if (from.substring(0..1) == "co/onmind/xy") "BOX" else "DUO"

        if (!pin.isNullOrEmpty()) scheme = "SHEET"
        //if (pin.isNullOrEmpty()) {
        //    return sendError("Falta nomenclatura de identificacion privada")
        //}
        if (scheme.isEmpty()) {
            return sendError("The scheme is required")
        }
        if (name.isNullOrEmpty()) {
            return sendError("The internal code or name is required")
        }

        val code = "${name.uppercase()}.${scheme.uppercase()}"
        try {
            //var query = "UPDATE xykit SET kit05 = '$spec' WHERE kit01 = '$code'"
            var query = "SELECT * FROM xykit WHERE kit01 = '$code'"
            val rows = xdb.forQuery(query)
            val row = rows?.get(0) ?: mutableMapOf()
            row.put("kit05", spec)
            xdb.savePointKit(row)

            val dbKit = DBKit()
            val id = row.get("id").toString()
            query = dbKit.getUpdate(row, id)
            val rowCount = xdb.forUpdate(query)

            //return sendSuccess(row)
            return sendSuccess(rowCount.toString())
        }
        catch (sqle: SQLException) {
            return sendError(sqle.message ?: "ERROR")
        }
    }

    fun list(body: AbcBody): Response {
        var scheme = body.with ?: "SHEET"
        try {
            val query = "SELECT id, kit01 as code, kit02 as name, kit03 as title, kit04 as hint, kit05 as spec FROM xykit WHERE kitxy = '$scheme'"
            val rows = xdb.forQuery(query)
            rows?.forEach {
                val inline = it.get("spec").toString()
                val spec = getSpecs(inline)
                it.put("spec", spec)
                it.put("inline", inline)
            }
            return sendSuccess(rows)
        }
        catch (sqle: SQLException) {
            return sendError(sqle.message ?: "ERROR")
        }
    }

    fun getSpecs(input: String): List<AbcPair> {
        val list: MutableList<AbcPair> = mutableListOf()
        val specs = planeDeserialize(input)
        specs.forEach {
            val item = AbcPair(
                    it.get("key").toString(),
                    it.get("name").toString(),
                    it.get("cast"),
                    it.get("field"))
            list.add(item)
        }
        return list
    }

    fun planeDeserialize(input: String): List<Map<String, String>> {
        val entries = input.split(";")
        val result = mutableListOf<Map<String, String>>()

        try {
            if (input.isNotEmpty() && input != "[]") {
                entries.forEach { entry ->
                    val attributes = entry.split(",")
                    val obj = mutableMapOf<String, String>()

                    attributes.forEach { attribute ->
                        val (key, value) = attribute.split("=")
                        obj[key] = value
                    }

                    result.add(obj)
                }
            }
        }
        catch (iobe: IndexOutOfBoundsException) {
            println("planeDeserialize => IndexOutOfBounds! input = $input")
        }

        return result
    }

    /*fun signup(body: AbcBody): Response {
        val from = body.from
        val name = body.some
        var scheme = body.with ?: "USER"
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

        if (!pin.isNullOrEmpty()) scheme = "USER"
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
        else if (listOf("USER","ROLE").indexOf(scheme) < 0) {
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
        else if (repo != "BOX" && listOf("ONE","KEY","YOU").indexOf(kind) > -1) {
            return sendError("The archetype or object class does not correspond to the repository: $kind ~> $repo")
        }

        val code = "${name.uppercase()}.${scheme.uppercase()}"
        try {
            var now = LocalDateTime.now()
            val id = putID(1, user, now)
            var keyon: String? = now.toString()  //.replace("T"," ")

            var query = """
                INSERT INTO xykey (id,keyxy,key01,key02,key03,key07,key14,keyon,keyat)
                VALUES ('$id','$scheme','$code','${name.uppercase()}','$user',$level,'EN','$keyon','$keyon')
                """.trimIndent()

            val dbKey = DBKey()
            val xyKey = XYKey(id,scheme,null,code,name.uppercase(),user,null,null,null,level,null,null,null,null,null,null,"EN",null,null,null,"OK",null,null,null,null,null,null,null,null,null,keyon,keyon)
            val map = dbKey.mapValues(xyKey)
            query = dbKey.getInsert(map as MutableMap<String, Any?>)
            val rowCount = xdb.forUpdate(query)

            query = "SELECT * FROM xykey WHERE id='$id'"
            val rows = xdb.forQuery(query)
            val row = rows?.get(0) ?: mutableMapOf()
            xdb.savePointKey(row)
            return sendSuccess(row)
        }
        catch (sqle: SQLException) {
            if (sqle.message!!.uppercase().contains("KIT01"))
                return sendError("Already exists the object: $code")
            else
                return sendError(sqle.message ?: "ERROR")
        }
    }*/

    private fun whoami(ctx: Request): Response {  // For Windows
        //if (onmindxdb.os.contains("inux")) {  // Linux
        //    return sendError("This system is for production and you don't have this priviledge")
        //}
        val config = co.onmind.util.Rote.getConfig(co.onmind.util.Rote.getConfigFile())
        val kvStore = config.getProperty("kv.store", "mvstore")
        val result = mapOf(
            "ok" to true,
            "user" to System.getProperty("user.name"),
            "home" to System.getProperty("user.home"),
            "os" to System.getProperty("os.name"),
            "engine" to if (onmindxdb.driver.contains("org.h2")) "default" else "duckdb",
            "kvstore" to kvStore
        )
        return sendSuccess(result)
    }

    private fun login(ctx: Request): Response {
        return sendSuccess("OK")
    }

}
