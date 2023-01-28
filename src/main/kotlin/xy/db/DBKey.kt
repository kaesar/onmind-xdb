package xy.db

/**
 * Created by Cesar Andres Arcila Buitrago from Colombia on 21/12/15.
 */

import java.time.LocalDateTime

class DBKey(): AbstractDB {

    private val kind = "key"
    val table = "xy$kind"
    val columns = "id,keyxy,key00,key01,key02,key03,key04,key05,key06,key07,key08,key09,key10,key11,key12,key13,key14,key15,key16,key17,key18,key19,key20,key21,key22,key23,keydo,keyto,keyof,keyby,keyon,keyat"
    val columnsList = columns.split(",")
    val dataTypes: List<String> = listOf("T","T","T","T","T","T","T","T","T","N","T","T","T","T","T","T","T","T","T","T","T","T","T","T","T","T","T","T","T","T","T","T")
    val sizes: List<Int?> = listOf(36,32,36,80,40,60,60,36,36,null,512,6,12,80,60,4000,2,6,3,10,2,6,36,36,60,240,10,36,40,40,40,40)
    val nulls: List<String> = listOf("-","-","+","+","+","+","+","+","+","-","+","+","+","+","+","+","-","+","+","+","-","+","+","+","+","+","+","+","+","+","+","+")
    val defaults: List<String?> = listOf(null,"USER",null,null,null,null,null,null,null,"5",null,"U",null,null,null,null,"ES",null,null,null,"OK",null,null,null,null,null,null,null,null,null,null,null)
    val key = "id"
    val unique = "key01"
    val prefix = "key"

    val sqlSelect = "SELECT $columns FROM $table"
    val sqlInsert = "INSERT INTO $table ($columns) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)"
    val sqlUpdate = """
            UPDATE $table SET
                keyxy = ?,
                key00 = ?,
                key01 = ?,
                key02 = ?,
                key03 = ?,
                key04 = ?,
                key05 = ?,
                key06 = ?,
                key07 = ?,
                key08 = ?,
                key09 = ?,
                key10 = ?,
                key11 = ?,
                key12 = ?,
                key13 = ?,
                key14 = ?,
                key15 = ?,
                key16 = ?,
                key17 = ?,
                key18 = ?,
                key19 = ?,
                key20 = ?,
                key21 = ?,
                key22 = ?,
                key23 = ?,
                keydo = ?,
                keyto = ?,
                keyof = ?,
                keyby = ?,
                keyon = ?,
                keyat = ?
            """.trimIndent()
    val sqlDelete = "DELETE FROM $table"
    val template = "#{id},#{keyxy},#{key00},#{key01},#{key02},#{key03},#{key04},#{key05},#{key06},#{key07},#{key08},#{key09},#{key10},#{key11},#{key12},#{key13},#{key14},#{key15},#{key16},#{key17},#{key18},#{key19},#{key20},#{key21},#{key22},#{key23},#{keydo},#{keyto},#{keyof},#{keyby},#{keyon},#{keyat}"
    val tmpInsert = "INSERT INTO $table VALUES ($template)"
    val tmpUpdate: String get() {
            val cols = template.split(",")
            var tmp = sqlUpdate
            for (i in 1 until cols.size) { tmp = tmp.replaceFirst("?", cols[i]) }
            return tmp
        }

    fun tableDDL(driver: String? = null): String = xtableDDL(table,columnsList,dataTypes,sizes,nulls,defaults,key,unique,driver ?: "h2")

    fun values(row: XYKey) = arrayOf(
        row.id,
        row.keyxy,
        row.key00,
        row.key01,
        row.key02,
        row.key03,
        row.key04,
        row.key05,
        row.key06,
        row.key07,
        row.key08,
        row.key09,
        row.key10,
        row.key11,
        row.key12,
        row.key13,
        row.key14,
        row.key15,
        row.key16,
        row.key17,
        row.key18,
        row.key19,
        row.key20,
        row.key21,
        row.key22,
        row.key23,
        row.keydo,
        row.keyto,
        row.keyof,
        row.keyby,
        row.keyon,
        row.keyat)

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
        val fakeStrings = listOf("key07")

        map["${prefix}xy"] = some
        if (map["id"] == null)
            map["id"] = id
        if (map["keyof"] == null)
            map["keyof"] = user
        if (map["keyby"] == null)
            map["keyby"] = user
        if (map["keyon"] == null)
            map["keyon"] = now.toString()
        if (map["keyat"] == null)
            map["keyat"] = now.toString()
        if (map["key01"] == null && map["key02"] != null) {
            map["key01"] = "${(map["key02"] as String).uppercase()}~${some.uppercase()}"
            if (pin != null)
                map["key01"] = "${(map["key02"] as String).uppercase()}~${some.uppercase()}~$pin"
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
        val fakeStrings = listOf("key07")
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
