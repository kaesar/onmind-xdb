package co.onmind.db

/**
 * Created by Cesar Andres Arcila Buitrago from Colombia on 6/01/16.
 * DOC: Documents (texts)
 */

import co.onmind.trait.AbstractDB
import co.onmind.io.IODoc
import co.onmind.xy.XYDoc
import java.time.LocalDateTime

class DBDoc(duo: Boolean = false): AbstractDB {

    private val kind = "doc"
    val table = if (!duo) "xy$kind" else "xz$kind"
    val columns = "id,docxy,doc00,doc01,doc02,doc03,doc04,doc05,doc06,doc07,doc08,doc09,doc10,docdo,docas,docif,docto,docof,docby,docon,docat"
    val columnsList = columns.split(",")
    val dataTypes: List<String> = listOf("T","T","T","T","T","T","T","T","T","N","T","T","T","T","T","N","T","T","T","T","T")
    val sizes: List<Int?> = listOf(36,40,36,128,36,120,320,32,36,null,16,40,4000,10,50,null,40,40,40,40,40)
    val nulls: List<String> = listOf("-","-","+","+","+","+","+","+","+","-","-","-","+","+","+","-","+","+","+","+","+")
    val defaults: List<String?> = listOf(null,null,null,null,null,null,null,null,null,"0","1","+",null,null,null,"4",null,null,null,null,null)
    val key = "id"
    val unique = "doc01"
    val prefix = "doc"

    val sqlSelect = "SELECT $columns FROM $table"
    val sqlInsert = "INSERT INTO $table ($columns) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)"
    val sqlUpdate = """
            UPDATE $table SET
                docxy = ?,
                doc00 = ?,
                doc01 = ?,
                doc02 = ?,
                doc03 = ?,
                doc04 = ?,
                doc05 = ?,
                doc06 = ?,
                doc07 = ?,
                doc08 = ?,
                doc09 = ?,
                doc10 = ?,
                docdo = ?,
                docas = ?,
                docif = ?,
                docto = ?,
                docof = ?,
                docby = ?,
                docon = ?,
                docat = ?
            """.trimIndent()
    val sqlDelete = "DELETE FROM $table"
    val template = "#{id},#{docxy},#{doc00},#{doc01},#{doc02},#{doc03},#{doc04},#{doc05},#{doc06},#{doc07},#{doc08},#{doc09},#{doc10},#{docdo},#{docas},#{docif},#{docto},#{docof},#{docby},#{docon},#{docat}"
    val tmpInsert = "INSERT INTO $table VALUES ($template)"
    val tmpUpdate: String get() {
            val cols = template.split(",")
            var tmp = sqlUpdate
            for (i in 1 until cols.size) { tmp = tmp.replaceFirst("?", cols[i]) }
            return tmp
        }

    fun tableDDL(driver: String? = null): String = xtableDDL(table,columnsList,dataTypes,sizes,nulls,defaults,key,unique,driver ?: "h2")

    fun values(row: XYDoc) = arrayOf(
        row.id,
        row.docxy,
        row.doc00,
        row.doc01,
        row.doc02,
        row.doc03,
        row.doc04,
        row.doc05,
        row.doc06,
        row.doc07,
        row.doc08,
        row.doc09,
        row.doc10,
        row.docdo,
        row.docas,
        row.docif,
        row.docto,
        row.docof,
        row.docby,
        row.docon,
        row.docat)

    fun mapValues(row: IODoc) = mapOf(
        "id" to row.id,
        "docxy" to row.docxy,
        "doc00" to row.doc00,
        "doc01" to row.doc01,
        "doc02" to row.doc02,
        "doc03" to row.doc03,
        "doc04" to row.doc04,
        "doc05" to row.doc05,
        "doc06" to row.doc06,
        "doc07" to row.doc07,
        "doc08" to row.doc08,
        "doc09" to row.doc09,
        "doc10" to row.doc10,
        "docdo" to row.docdo,
        "docas" to row.docas,
        "docif" to row.docif,
        "docto" to row.docto,
        "docof" to row.docof,
        "docby" to row.docby,
        "docon" to row.docon,
        "docat" to row.docat)

    fun getInsert(
        map: MutableMap<String, Any?>,
        some: String,
        user: String?,
        id: String? = null,
        now: LocalDateTime? = null,
        pin: String?
    ): String {
        var mapColumns: String = ""
        var mapValues: String = ""
        val fakeStrings = listOf("doc07", "docif")

        map["${prefix}xy"] = some
        if (map["id"] == null)
            map["id"] = id
        if (map["docof"] == null)
            map["docof"] = user
        if (map["docby"] == null)
            map["docby"] = user
        if (map["docon"] == null)
            map["docon"] = now.toString()
        if (map["docat"] == null)
            map["docat"] = now.toString()
        if (map["doc01"] == null && map["doc02"] != null) {
            map["doc01"] = "${(map["doc02"] as String).uppercase()}~${some.uppercase()}"
            if (pin != null)
                map["doc01"] = "${(map["doc02"] as String).uppercase()}~${some.uppercase()}~$pin"
        }

        map.forEach { (k, v) ->
            if (v != null) {
                mapColumns += "$k,"
                mapValues += if (!fakeStrings.contains(k)) "'${v as String}'," else "${v},"
            }
        }

        return "INSERT INTO $table (${mapColumns.dropLast(1)}) values (${mapValues.dropLast(1)})"
    }

    fun getUpdate(map: MutableMap<String, Any?>, with: String): String {
        val fakeStrings = listOf("doc07", "docif")
        var query = "UPDATE $table SET "
        map.forEach { (k, v) ->
            if (v != null) {
                query += "$k="
                query += if (!fakeStrings.contains(k)) "'${v as String}'," else "${v},"
            }
        }
        query = "${query.dropLast(1)} WHERE id='$with'"
        return query
    }
}
