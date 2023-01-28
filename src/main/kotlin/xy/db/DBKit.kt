package xy.db

/**
 * Created by Cesar Andres Arcila Buitrago from Colombia on 16/12/15.
 */

class DBKit(): AbstractDB {

    private val kind = "kit"
    val table = "xy$kind"
    val columns = "id,kitxy,kit01,kit02,kit03,kit04,kit05,kit06,kit07,kit08,kit09,kit10,kit11,kit12,kit13,kit14,kito3,kito4,kitif,kitto,kitof,kitby,kiton,kitat"
    val columnsList = columns.split(",")
    val dataTypes: List<String> = listOf("T","T","T","T","T","T","T","T","N","T","T","T","T","T","T","T","T","T","N","T","T","T","T","T")
    val sizes: List<Int?> = listOf(36,32,40,28,100,320,4000,40,null,16,40,240,12,50,12,120,100,320,null,40,40,40,40,40)
    val nulls: List<String> = listOf("-","-","-","-","-","+","+","+","-","-","-","+","+","+","+","+","+","+","-","+","+","+","+","+")
    val defaults: List<String?> = listOf(null,"SHEET",null,null,null,null,null,null,"0","1","+",null,null,null,"-",null,null,null,"4",null,null,null,null,null)
    val key = "id"
    val unique = "kit01"
    val prefix = "kit"

    val sqlSelect = "SELECT $columns FROM $table"
    val sqlInsert = "INSERT INTO $table ($columns) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)"
    val sqlUpdate = """
            UPDATE $table SET
                kitxy = ?,
                kit01 = ?,
                kit02 = ?,
                kit03 = ?,
                kit04 = ?,
                kit05 = ?,
                kit06 = ?,
                kit07 = ?,
                kit08 = ?,
                kit09 = ?,
                kit10 = ?,
                kit11 = ?,
                kit12 = ?,
                kit13 = ?,
                kit14 = ?,
                kito3 = ?,
                kito4 = ?,
                kitif = ?,
                kitto = ?,
                kitof = ?,
                kitby = ?,
                kiton = ?,
                kitat = ?
            """.trimIndent()
    val sqlDelete = "DELETE FROM $table"
    val template = "#{id},#{kitxy},#{kit01},#{kit02},#{kit03},#{kit04},#{kit05},#{kit06},#{kit07},#{kit08},#{kit09},#{kit10},#{kit11},#{kit12},#{kit13},#{kit14},#{kito3},#{kito4},#{kitif},#{kitto},#{kitof},#{kitby},#{kiton},#{kitat}"
    val tmpInsert = "INSERT INTO $table VALUES ($template)"
    val tmpUpdate: String get() {
            val cols = template.split(",")
            var tmp = sqlUpdate
            for (i in 1 until cols.size) { tmp = tmp.replaceFirst("?", cols[i]) }
            return tmp
        }

    fun tableDDL(driver: String? = null): String = xtableDDL(table,columnsList,dataTypes,sizes,nulls,defaults,key,unique,driver ?: "h2")

    fun values(row: XYKit) = arrayOf(
        row.id,
        row.kitxy,
        row.kit01,
        row.kit02,
        row.kit03,
        row.kit04,
        row.kit05,
        row.kit06,
        row.kit07,
        row.kit08,
        row.kit09,
        row.kit10,
        row.kit11,
        row.kit12,
        row.kit13,
        row.kit14,
        row.kito3,
        row.kito4,
        row.kitif,
        row.kitto,
        row.kitof,
        row.kitby,
        row.kiton,
        row.kitat)

    fun mapValues(row: XYKit) = mapOf(
        "id" to row.id,
        "kitxy" to row.kitxy,
        "kit01" to row.kit01,
        "kit02" to row.kit02,
        "kit03" to row.kit03,
        "kit04" to row.kit04,
        "kit05" to row.kit05,
        "kit06" to row.kit06,
        "kit07" to row.kit07,
        "kit08" to row.kit08,
        "kit09" to row.kit09,
        "kit10" to row.kit10,
        "kit11" to row.kit11,
        "kit12" to row.kit12,
        "kit13" to row.kit13,
        "kit14" to row.kit14,
        "kito3" to row.kito3,
        "kito4" to row.kito4,
        "kitif" to row.kitif,
        "kitto" to row.kitto,
        "kitof" to row.kitof,
        "kitby" to row.kitby,
        "kiton" to row.kiton,
        "kitat" to row.kitat)

    fun getInsert(map: MutableMap<String, Any?>): String {
        var mapColumns: String = ""
        var mapValues: String = ""
        val fakeStrings = listOf("kit07", "kitif")

        map.forEach { (k, v) ->
            if (v != null) {
                mapColumns += "$k,"
                mapValues += if (!fakeStrings.contains(k)) "'${v as String}'," else "${v},"
            }
        }

        return "INSERT INTO $table (${mapColumns.dropLast(1)}) values (${mapValues.dropLast(1)})"
    }

    fun getUpdate(map: MutableMap<String, Any?>, with: String): String {
        val fakeStrings = listOf("kit07", "kitif")
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
