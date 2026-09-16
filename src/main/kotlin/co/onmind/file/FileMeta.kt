package co.onmind.file

import co.onmind.db.DBAny
import co.onmind.db.RDB
import co.onmind.xy.XYAny

/** Meta helpers + auto-create for the reserved FILES sheet. */
object FileMeta {

    private val xdb = RDB()

    private fun sheetExists(): Boolean {
        val rows = xdb.forQuery("SELECT id FROM xykit WHERE kit01='FILES.SHEET'")
        return !rows.isNullOrEmpty()
    }

    private fun createSheet(user: String): Boolean {
        return try {
            val dbKit = co.onmind.db.DBKit()
            val xyKit = co.onmind.xy.XYKit(
                id = "", kitxy = "SHEET", kit01 = "FILES.SHEET", kit02 = "FILES",
                kit03 = "Archivos", kit04 = null, kit05 = FILES_SPEC, kit06 = null,
                kit07 = 0, kit08 = "1", kit09 = "+", kit10 = null, kit11 = null,
                kit12 = "files.sheet.0", kit13 = null, kit14 = null, kito3 = null,
                kito4 = null, kitif = 4, kitto = null, kitof = null,
                kitby = user, kiton = java.time.LocalDateTime.now().toString(),
                kitat = java.time.LocalDateTime.now().toString()
            )
            val map = dbKit.mapValues(xyKit)
            xdb.forUpdate(dbKit.getInsert(map as MutableMap<String, Any?>))
            xdb.savePointKit(map)
            true
        } catch (ex: Exception) {
            co.onmind.util.Trace.logError("FILES sheet create failed", ex)
            false
        }
    }

    /** Ensure FILES.SHEET kit row exists (called at startup when file.enabled=+). */
    fun ensureSheet(user: String = "system"): Boolean {
        if (sheetExists()) return true
        return createSheet(user)
    }

    /** Insert metadata row via the same path as abc insert (any01 derived). */
    fun insert(meta: FileMetadata, user: String): String? {
        return try {
            val dbAny = DBAny()
            val map: MutableMap<String, Any?> = HashMap(meta)
            val some = FILES_SHEET
            val now = java.time.LocalDateTime.now()
            val id = co.onmind.util.AbstractIds.putId(user, now)
            map["id"] = id
            map["anyof"] = user
            map["anyby"] = map["anyby"] ?: user
            map["anyon"] = now.toString()
            map["anyat"] = now.toString()
            map["any07"] = 0
            map["any08"] = "1"
            map["any09"] = "+"
            map["anyif"] = 4
            if (map["any01"] == null && map["any02"] != null) {
                // any01 is UNIQUE (XYAny schema): derive from name + id to allow
                // multiple files sharing the same original name.
                map["any01"] = "${(map["any02"] as String).uppercase()}~${some}~${id}"
            }
            xdb.forUpdate(dbAny.getInsert(map, some, user, id, now, null))
            xdb.savePointAny(map)
            id
        } catch (ex: Exception) {
            co.onmind.util.Trace.logError("FILES metadata insert failed", ex)
            null
        }
    }

    fun findById(id: String): MutableMap<String, Any?>? {
        val rows = xdb.forQuery("SELECT * FROM xyany WHERE id='$id' AND anyxy='${FILES_SHEET}'")
        return rows?.firstOrNull()
    }

    fun list(filter: String? = null): List<MutableMap<String, Any?>> {
        val where = filter?.let { " AND $it" } ?: ""
        return xdb.forQuery("SELECT * FROM xyany WHERE anyxy='${FILES_SHEET}'$where ORDER BY anyat DESC")
            ?: emptyList()
    }

    fun delete(id: String): Boolean {
        return try {
            xdb.forUpdate("DELETE FROM xyany WHERE id='$id' AND anyxy='${FILES_SHEET}'")
            co.onmind.util.CoherenceStore.decrementMemoryCount("any")
            xdb.movePoint(id, "any")
            true
        } catch (ex: Exception) {
            co.onmind.util.Trace.logError("FILES metadata delete failed", ex)
            false
        }
    }
}