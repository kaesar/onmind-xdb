package xy.db

/**
 * Created by Cesar Andres Arcila Buitrago from Colombia on 6/01/16.
 */

import java.time.LocalDateTime

class DBSet(duo: Boolean = false): AbstractDB {

    private val kind = "set"
    val table = if (!duo) "xy$kind" else "xz$kind"
    val columns = "id,setxy,set00,set01,set02,set03,set04,set05,set06,set07,set08,set09,set10,set11,set12,set13,set14,set15,set16,set17,set18,set19,set20,set21,set22,seto3,seto4,setdo,setas,setif,setto,setof,setby,seton,setat"
    val columnsList = columns.split(",")
    val dataTypes: List<String> = listOf("T","T","T","T","T","T","T","T","T","N","T","T","T","T","T","T","M","N","N","T","T","T","T","T","T","T","T","T","T","N","T","T","T","T","T")
    val sizes: List<Int?> = listOf(36,40,36,128,36,120,320,32,36,null,16,40,240,40,80,4000,null,null,null,40,12,36,36,36,120,120,320,10,50,null,40,40,40,40,40)
    val nulls: List<String> = listOf("-","-","+","+","+","+","+","+","+","-","-","-","+","+","+","+","+","+","+","+","+","+","+","+","+","+","+","+","+","-","+","+","+","+","+")
    val defaults: List<String?> = listOf(null,null,null,null,null,null,null,null,null,"0","1","+",null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,"4",null,null,null,null,null)
    val key = "id"
    val unique = "set01"
    val prefix = "set"

    val sqlSelect = "SELECT $columns FROM $table"
    val sqlInsert = "INSERT INTO $table ($columns) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)"
    val sqlUpdate = """
            UPDATE $table SET
                setxy = ?,
                set00 = ?,
                set01 = ?,
                set02 = ?,
                set03 = ?,
                set04 = ?,
                set05 = ?,
                set06 = ?,
                set07 = ?,
                set08 = ?,
                set09 = ?,
                set10 = ?,
                set11 = ?,
                set12 = ?,
                set13 = ?,
                set14 = ?,
                set15 = ?,
                set16 = ?,
                set17 = ?,
                set18 = ?,
                set19 = ?,
                set20 = ?,
                set21 = ?,
                set22 = ?,
                seto3 = ?,
                seto4 = ?,
                setdo = ?,
                setas = ?,
                setif = ?,
                setto = ?,
                setof = ?,
                setby = ?,
                seton = ?,
                setat = ?
            """.trimIndent()
    val sqlDelete = "DELETE FROM $table"
    val template = "#{id},#{setxy},#{set00},#{set01},#{set02},#{set03},#{set04},#{set05},#{set06},#{set07},#{set08},#{set09},#{set10},#{set11},#{set12},#{set13},#{set14},#{set15},#{set16},#{set17},#{set18},#{set19},#{set20},#{set21},#{set22},#{seto3},#{seto4},#{setdo},#{setas},#{setif},#{setto},#{setof},#{setby},#{seton},#{setat}"
    val tmpInsert = "INSERT INTO $table VALUES ($template)"
    val tmpUpdate: String get() {
            val cols = template.split(",")
            var tmp = sqlUpdate
            for (i in 1 until cols.size) { tmp = tmp.replaceFirst("?", cols[i]) }
            return tmp
        }

    fun tableDDL(driver: String? = null): String = xtableDDL(table,columnsList,dataTypes,sizes,nulls,defaults,key,unique,driver ?: "h2")

    fun values(row: XYSet) = arrayOf(
        row.id,
        row.setxy,
        row.set00,
        row.set01,
        row.set02,
        row.set03,
        row.set04,
        row.set05,
        row.set06,
        row.set07,
        row.set08,
        row.set09,
        row.set10,
        row.set11,
        row.set12,
        row.set13,
        row.set14,
        row.set15,
        row.set16,
        row.set17,
        row.set18,
        row.set19,
        row.set20,
        row.set21,
        row.set22,
        row.seto3,
        row.seto4,
        row.setdo,
        row.setas,
        row.setif,
        row.setto,
        row.setof,
        row.setby,
        row.seton,
        row.setat)

    fun mapValues(row: IOSet) = mapOf(
        "id" to row.id,
        "setxy" to row.setxy,
        "set00" to row.set00,
        "set01" to row.set01,
        "set02" to row.set02,
        "set03" to row.set03,
        "set04" to row.set04,
        "set05" to row.set05,
        "set06" to row.set06,
        "set07" to row.set07,
        "set08" to row.set08,
        "set09" to row.set09,
        "set10" to row.set10,
        "set11" to row.set11,
        "set12" to row.set12,
        "set13" to row.set13,
        "set14" to row.set14,
        "set15" to row.set15,
        "set16" to row.set16,
        "set17" to row.set17,
        "set18" to row.set18,
        "set19" to row.set19,
        "set20" to row.set20,
        "set21" to row.set21,
        "set22" to row.set22,
        "seto3" to row.seto3,
        "seto4" to row.seto4,
        "setdo" to row.setdo,
        "setas" to row.setas,
        "setif" to row.setif,
        "setto" to row.setto,
        "setof" to row.setof,
        "setby" to row.setby,
        "seton" to row.seton,
        "setat" to row.setat)

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
        val fakeStrings = listOf("set07", "set14", "set15", "set16", "setif")

        map["${prefix}xy"] = some
        if (map["id"] == null)
            map["id"] = id
        if (map["setof"] == null)
            map["setof"] = user
        if (map["setby"] == null)
            map["setby"] = user
        if (map["seton"] == null)
            map["seton"] = now.toString()
        if (map["setat"] == null)
            map["setat"] = now.toString()
        if (map["set01"] == null && map["set02"] != null) {
            map["set01"] = "${(map["set02"] as String).uppercase()}~${some.uppercase()}"
            if (pin != null)
                map["set01"] = "${(map["set02"] as String).uppercase()}~${some.uppercase()}~$pin"
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
        val fakeStrings = listOf("set07", "set14", "set15", "set16", "setif")
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
