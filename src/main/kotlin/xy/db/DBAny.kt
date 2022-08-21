package xy.db

/**
 * Created by Cesar Andres Arcila Buitrago from Colombia on 01/12/19.
 */

import org.apache.commons.dbutils.DbUtils
import org.apache.commons.dbutils.QueryRunner
import org.apache.commons.dbutils.ResultSetHandler
import org.apache.commons.dbutils.handlers.BeanHandler
import java.sql.SQLException
import java.time.LocalDateTime

class DBAny(duo: Boolean = false): AbstractDB {

    private val kind = "any"
    val table = if (!duo) "xy$kind" else "xz$kind"
    val columns = "id,anyxy,anyis,any00,any01,any02,any03,any04,any05,any06,any07,any08,any09,any10,any11,any12,any13,any14,any15,any16,any17,any18,any19,any20,any21,any22,any23,any24,any25,any26,any27,any28,any29,any30,any31,any32,any33,any34,any35,any36,any37,any38,any39,any40,any41,any42,any43,any44,any45,any46,any47,any48,any49,any50,any51,any52,any53,any54,any55,any56,any57,any58,any59,any60,anydo,anyto,anyio,anyas,anyif,anyof,anyby,anyon,anyat"
    val columnsList = columns.split(",")
    val dataTypes: List<String> = listOf("T","T","T","T","T","T","T","T","T","T","N","T","T","T","T","T","T","T","T","T","T","T","T","T","T","T","T","T","T","T","T","T","T","T","T","T","T","T","T","T","T","T","T","T","T","T","T","T","T","T","T","T","T","T","T","T","T","T","T","T","T","T","T","T","T","T","T","T","N","T","T","T","T")
    val sizes: List<Int?> = listOf(36,40,8,36,128,36,120,400,32,36,null,16,40,4000,320,320,320,320,320,320,320,320,320,320,320,320,320,320,320,320,320,320,320,320,320,320,320,320,320,320,320,320,320,320,320,320,320,320,320,320,320,320,320,320,320,320,320,320,320,320,320,320,320,320,10,36,240,50,null,40,40,40,40)
    val nulls: List<String> = listOf("-","-","+","+","+","+","+","+","+","+","-","-","-","+","+","+","+","+","+","+","+","+","+","+","+","+","+","+","+","+","+","+","+","+","+","+","+","+","+","+","+","+","+","+","+","+","+","+","+","+","+","+","+","+","+","+","+","+","+","+","+","+","+","+","+","+","+","+","-","+","+","+","+")
    val defaults: List<String?> = listOf(null,null,null,null,null,null,null,null,null,null,"0","1","+",null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,"4",null,null,null,null)
    val key = "id"
    val unique = "any01"
    val prefix = "any"

    val sqlSelect = "SELECT $columns FROM $table"
    val sqlInsert = "INSERT INTO $table ($columns) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)"
    val sqlUpdate = """
            UPDATE $table SET
                anyxy = ?,
                anyis = ?,
                any00 = ?,
                any01 = ?,
                any02 = ?,
                any03 = ?,
                any04 = ?,
                any05 = ?,
                any06 = ?,
                any07 = ?,
                any08 = ?,
                any09 = ?,
                any10 = ?,
                any11 = ?,
                any12 = ?,
                any13 = ?,
                any14 = ?,
                any15 = ?,
                any16 = ?,
                any17 = ?,
                any18 = ?,
                any19 = ?,
                any20 = ?,
                any21 = ?,
                any22 = ?,
                any23 = ?,
                any24 = ?,
                any25 = ?,
                any26 = ?,
                any27 = ?,
                any28 = ?,
                any29 = ?,
                any30 = ?,
                any31 = ?,
                any32 = ?,
                any33 = ?,
                any34 = ?,
                any35 = ?,
                any36 = ?,
                any37 = ?,
                any38 = ?,
                any39 = ?,
                any40 = ?,
                any41 = ?,
                any42 = ?,
                any43 = ?,
                any44 = ?,
                any45 = ?,
                any46 = ?,
                any47 = ?,
                any48 = ?,
                any49 = ?,
                any50 = ?,
                any51 = ?,
                any52 = ?,
                any53 = ?,
                any54 = ?,
                any55 = ?,
                any56 = ?,
                any57 = ?,
                any58 = ?,
                any59 = ?,
                any60 = ?,
                anydo = ?,
                anyto = ?,
                anyio = ?,
                anyas = ?,
                anyif = ?,
                anyof = ?,
                anyby = ?,
                anyon = ?,
                anyat = ?
            """.trimIndent()
    val sqlDelete = "DELETE FROM $table"
    val template = "#{id},#{anyxy},#{anyis},#{any00},#{any01},#{any02},#{any03},#{any04},#{any05},#{any06},#{any07},#{any08},#{any09},#{any10},#{any11},#{any12},#{any13},#{any14},#{any15},#{any16},#{any17},#{any18},#{any19},#{any20},#{any21},#{any22},#{any23},#{any24},#{any25},#{any26},#{any27},#{any28},#{any29},#{any30},#{any31},#{any32},#{any33},#{any34},#{any35},#{any36},#{any37},#{any38},#{any39},#{any40},#{any41},#{any42},#{any43},#{any44},#{any45},#{any46},#{any47},#{any48},#{any49},#{any50},#{any51},#{any52},#{any53},#{any54},#{any55},#{any56},#{any57},#{any58},#{any59},#{any60},#{anydo},#{anyto},#{anyio},#{anyas},#{anyif},#{anyof},#{anyby},#{anyon},#{anyat}"
    val tmpInsert = "INSERT INTO $table VALUES ($template)"
    val tmpUpdate: String get() {
            val cols = template.split(",")
            var tmp = sqlUpdate
            for (i in 1 until cols.size) { tmp = tmp.replaceFirst("?", cols[i]) }
            return tmp
        }

    fun tableDDL(driver: String? = null): String = xtableDDL(table,columnsList,dataTypes,sizes,nulls,defaults,key,unique,driver ?: "h2")

    fun values(row: XYAny) = arrayOf(
        row.id,
        row.anyxy,
        row.anyis,
        row.any00,
        row.any01,
        row.any02,
        row.any03,
        row.any04,
        row.any05,
        row.any06,
        row.any07,
        row.any08,
        row.any09,
        row.any10,
        row.any11,
        row.any12,
        row.any13,
        row.any14,
        row.any15,
        row.any16,
        row.any17,
        row.any18,
        row.any19,
        row.any20,
        row.any21,
        row.any22,
        row.any23,
        row.any24,
        row.any25,
        row.any26,
        row.any27,
        row.any28,
        row.any29,
        row.any30,
        row.any31,
        row.any32,
        row.any33,
        row.any34,
        row.any35,
        row.any36,
        row.any37,
        row.any38,
        row.any39,
        row.any40,
        row.any41,
        row.any42,
        row.any43,
        row.any44,
        row.any45,
        row.any46,
        row.any47,
        row.any48,
        row.any49,
        row.any50,
        row.any51,
        row.any52,
        row.any53,
        row.any54,
        row.any55,
        row.any56,
        row.any57,
        row.any58,
        row.any59,
        row.any60,
        row.anydo,
        row.anyto,
        row.anyio,
        row.anyas,
        row.anyif,
        row.anyof,
        row.anyby,
        row.anyon,
        row.anyat)

    fun mapValues(row: IOAny) = mapOf(
        "id" to row.id,
        "anyxy" to row.anyxy,
        "anyis" to row.anyis,
        "any00" to row.any00,
        "any01" to row.any01,
        "any02" to row.any02,
        "any03" to row.any03,
        "any04" to row.any04,
        "any05" to row.any05,
        "any06" to row.any06,
        "any07" to row.any07,
        "any08" to row.any08,
        "any09" to row.any09,
        "any10" to row.any10,
        "any11" to row.any11,
        "any12" to row.any12,
        "any13" to row.any13,
        "any14" to row.any14,
        "any15" to row.any15,
        "any16" to row.any16,
        "any17" to row.any17,
        "any18" to row.any18,
        "any19" to row.any19,
        "any20" to row.any20,
        "any21" to row.any21,
        "any22" to row.any22,
        "any23" to row.any23,
        "any24" to row.any24,
        "any25" to row.any25,
        "any26" to row.any26,
        "any27" to row.any27,
        "any28" to row.any28,
        "any29" to row.any29,
        "any30" to row.any30,
        "any31" to row.any31,
        "any32" to row.any32,
        "any33" to row.any33,
        "any34" to row.any34,
        "any35" to row.any35,
        "any36" to row.any36,
        "any37" to row.any37,
        "any38" to row.any38,
        "any39" to row.any39,
        "any40" to row.any40,
        "any41" to row.any41,
        "any42" to row.any42,
        "any43" to row.any43,
        "any44" to row.any44,
        "any45" to row.any45,
        "any46" to row.any46,
        "any47" to row.any47,
        "any48" to row.any48,
        "any49" to row.any49,
        "any50" to row.any50,
        "any51" to row.any51,
        "any52" to row.any52,
        "any53" to row.any53,
        "any54" to row.any54,
        "any55" to row.any55,
        "any56" to row.any56,
        "any57" to row.any57,
        "any58" to row.any58,
        "any59" to row.any59,
        "any60" to row.any60,
        "anydo" to row.anydo,
        "anyto" to row.anyto,
        "anyio" to row.anyio,
        "anyas" to row.anyas,
        "anyif" to row.anyif,
        "anyof" to row.anyof,
        "anyby" to row.anyby,
        "anyon" to row.anyon,
        "anyat" to row.anyat)

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
        val fakeStrings = listOf("any07", "anyif")

        map["${prefix}xy"] = some
        if (map["id"] == null)
            map["id"] = id
        if (map["anyof"] == null)
            map["anyof"] = user
        if (map["anyby"] == null)
            map["anyby"] = user
        if (map["anyon"] == null)
            map["anyon"] = now.toString()
        if (map["anyat"] == null)
            map["anyat"] = now.toString()
        if (map["any01"] == null && map["any02"] != null) {
            map["any01"] = "${(map["any02"] as String).uppercase()}~${some.uppercase()}"
            if (pin != null)
                map["any01"] = "${(map["any02"] as String).uppercase()}~${some.uppercase()}~$pin"
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
        val fakeStrings = listOf("any07", "anyif")
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

    @Throws(SQLException::class)
    fun find() {
        val qr = QueryRunner()
        val rh: ResultSetHandler<XYAny> = BeanHandler<XYAny>(XYAny::class.java)
        val conn = onmindxdb.dbc  //?.getConnection()
        try {
            val row: XYAny = qr.query(conn, "$sqlSelect WHERE id=?", rh, "Sumit")
            println(row)
            //Display values
            //System.out.print("ID: " + emp.getId())
            //System.out.print(", Age: " + emp.getAge())
            //System.out.print(", First: " + emp.getFirst())
            //System.out.println(", Last: " + emp.getLast())
        }
        finally {
            DbUtils.close(conn)
        }
    }

    @Throws(SQLException::class)
    fun update(params: Any){
        val qr = QueryRunner()
        val conn = onmindxdb.dbc  //?.getConnection()
        try {
            val rows = qr.update(conn, sqlUpdate, params)
            println("$rows record(s) updated.")
        }
        finally {
            DbUtils.close(conn)
        }
    }
}
