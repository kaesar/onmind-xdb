package co.onmind.db

import co.onmind.xy.XYAny
import co.onmind.xy.XYDoc
import co.onmind.xy.XYKey
import co.onmind.xy.XYKit
import co.onmind.xy.XYSet
import co.onmind.kv.KVStoreFactory
import co.onmind.trait.KVStore
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.apache.commons.dbutils.QueryRunner
import org.apache.commons.dbutils.handlers.MapListHandler
import java.sql.Connection
import java.sql.SQLException
import java.text.DecimalFormat

/**
 * Created by Cesar Andres Arcila Buitrago from Colombia on 21/08/16.
 * Refactorized on 21/12/20
 */

class RDB() {

    val XYBOX = "xybox"
    val mapper = jacksonObjectMapper()
    
    companion object {
        private var storeInstance: KVStore? = null
        
        private fun getStore(): KVStore {
            if (storeInstance == null) {
                val config = onmindxdb.config ?: throw IllegalStateException("Configuration not initialized")
                storeInstance = KVStoreFactory.createStore(config)
            }
            return storeInstance!!
        }
        
        // Método público para acceder al store desde CoherenceStore
        fun getStoreInstance(): KVStore? = storeInstance
    }

    private fun lapsed(startTime: Long) {
        val endTime = System.currentTimeMillis()
        val formatter = DecimalFormat("#0.000")
        val totalTime = formatter.format((endTime - startTime) / 1000.0)
        println("[  OK!  ] => $totalTime seconds")
    }

    @Throws(SQLException::class)
    fun forQuery(sql: String): List<MutableMap<String, Any?>>? {
        val qr = QueryRunner()
        onmindxdb.dataSource?.connection.use { conn ->
            return qr.query(conn, sql, MapListHandler())
        }
    }

    @Throws(SQLException::class)
    fun forUpdate(sql: String): Int {
        val qr = QueryRunner()
        onmindxdb.dataSource?.connection.use { conn ->
            return qr.update(conn, sql)
        }
    }

    fun loadPoint(startTime: Long) {
        val qr = QueryRunner()
        try {
            qr.update(onmindxdb.dbc, DBKit().tableDDL("box"))
            qr.update(onmindxdb.dbc, DBKey().tableDDL("box"))
            qr.update(onmindxdb.dbc, DBSet().tableDDL("box"))
            qr.update(onmindxdb.dbc, DBDoc().tableDDL("box"))
            qr.update(onmindxdb.dbc, DBAny().tableDDL("box"))
            print("Loading data in memory .... ")
        } catch (sqle: SQLException) {
            if (sqle.errorCode == 42101 || sqle.sqlState == "42S01") {  // Already
                print("Loading data from file .... ")
                lapsed(startTime)
                return
            } else
                print("Loading data in memory .... ")
        }
    }

    fun readPoint() {
        val store = getStore()
        val qr = QueryRunner()
        val boxDB = onmindxdb.dbc
        val startTime = System.currentTimeMillis()
        try {
            loadPoint(startTime)
            store.forEach { key, value ->
                processStoreEntry(key, value, qr, boxDB)
            }
            lapsed(startTime)
        }
        catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun processStoreEntry(key: String, value: String, qr: QueryRunner, boxDB: java.sql.Connection?) {
        val x = value
        val y = key
        var insert: String? = null
        var values: Array<out Any?> = emptyArray()

        if (y.contains("~kit~")) {
            val row = mapper.readValue(x, XYKit::class.java)
            insert = DBKit().sqlInsert
            values = DBKit().values(row)
        }
        else if (y.contains("~key~")) {
            val row = mapper.readValue(x, XYKey::class.java)
            insert = DBKey().sqlInsert
            values = DBKey().values(row)
        }
        else if (y.contains("~set~")) {
            val row = mapper.readValue(x, XYSet::class.java)
            insert = DBSet().sqlInsert
            values = DBSet().values(row)
        }
        else if (y.contains("~any~")) {
            val row = mapper.readValue(x, XYAny::class.java)
            insert = DBAny().sqlInsert
            values = DBAny().values(row)
        }
        else if (y.contains("~doc~")) {
            val row = mapper.readValue(x, XYDoc::class.java)
            insert = DBDoc().sqlInsert
            values = DBDoc().values(row)
        }

        if (insert != null) {
            try {
                qr.update(boxDB, insert, *values)
            }
            catch (sqle: SQLException) {
                if (sqle.errorCode != 23505)
                    println(sqle.message)
            }
        }
    }

    fun savePointKit(map: MutableMap<String, Any?>, forceDelete: Boolean = false) {
        val store = getStore()
        try {
            val row = XYKit(
                map["id"] as String,
                map["kitxy"] as String,
                map["kit01"] as String,
                map["kit02"] as String,
                map["kit03"] as String,
                map["kit04"] as String?,
                map["kit05"] as String?,
                map["kit06"] as String?,
                map["kit07"] as Int,
                map["kit08"] as String,
                map["kit09"] as String,
                map["kit10"] as String?,
                map["kit11"] as String?,
                map["kit12"] as String?,
                map["kit13"] as String?,
                map["kit14"] as String?,
                map["kito3"] as String?,
                map["kito4"] as String?,
                map["kitif"] as Int,
                map["kitto"] as String?,
                map["kitof"] as String?,
                map["kitby"] as String?,
                map["kiton"] as String?,
                map["kitat"] as String?
            )

            val jsonValue = mapper.writeValueAsString(row)
            val key = "${row.id}~kit~box"
            if (forceDelete)
                store.delete(key)
            store.put(key, jsonValue)
            store.commit()
            
            // Verificación de coherencia (cero overhead si está desactivado)
            co.onmind.util.CoherenceStore.verifyCoherenceQuick("kit")
        }
        catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun savePointKey(map: MutableMap<String, Any?>, forceDelete: Boolean = false) {
        val store = getStore()
        try {
            val row = XYKey(
                map["id"] as String,
                map["keyxy"] as String,
                map["key00"] as String?,
                map["key01"] as String?,
                map["key02"] as String?,
                map["key03"] as String?,
                map["key04"] as String?,
                map["key05"] as String?,
                map["key06"] as String?,
                map["key07"] as Int,
                map["key08"] as String?,
                map["key09"] as String?,
                map["key10"] as String?,
                map["key11"] as String?,
                map["key12"] as String?,
                map["key13"] as String?,
                map["key14"] as String,
                map["key15"] as String?,
                map["key16"] as String?,
                map["key17"] as String?,
                map["key18"] as String,
                map["key19"] as String?,
                map["key20"] as String?,
                map["key21"] as String?,
                map["key22"] as String?,
                map["key23"] as String?,
                map["keydo"] as String?,
                map["keyto"] as String?,
                map["keyof"] as String?,
                map["keyby"] as String?,
                map["keyon"] as String?,
                map["keyat"] as String?
            )

            val jsonValue = mapper.writeValueAsString(row)
            val key = "${row.id}~key~box"
            if (forceDelete)
                store.delete(key)
            store.put(key, jsonValue)
            store.commit()
            
            // Verificación de coherencia (cero overhead si está desactivado)
            co.onmind.util.CoherenceStore.verifyCoherenceQuick("key")
        }
        catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun savePointSet(map: MutableMap<String, Any?>, forceDelete: Boolean = false) {
        val store = getStore()
        try {
            val row = XYSet(
                map["id"] as String,
                map["setxy"] as String,
                map["set00"] as String?,
                map["set01"] as String?,
                map["set02"] as String?,
                map["set03"] as String?,
                map["set04"] as String?,
                map["set05"] as String?,
                map["set06"] as String?,
                map["set07"] as Int,
                map["set08"] as String,
                map["set09"] as String,
                map["set10"] as String?,
                map["set11"] as String?,
                map["set12"] as String?,
                map["set13"] as String?,
                map["set14"] as Double?,
                map["set15"] as Int?,
                map["set16"] as Int?,
                map["set17"] as String?,
                map["set18"] as String,
                map["set19"] as String?,
                map["set20"] as String?,
                map["set21"] as String?,
                map["set22"] as String?,
                map["seto3"] as String?,
                map["seto4"] as String?,
                map["setdo"] as String?,
                map["setas"] as String?,
                map["setif"] as Int,
                map["setto"] as String?,
                map["setof"] as String?,
                map["setby"] as String?,
                map["seton"] as String?,
                map["setat"] as String?
            )

            val jsonValue = mapper.writeValueAsString(row)
            val key = "${row.id}~set~box"
            if (forceDelete)
                store.delete(key)
            store.put(key, jsonValue)
            store.commit()
            
            // Verificación de coherencia (cero overhead si está desactivado)
            co.onmind.util.CoherenceStore.verifyCoherenceQuick("set")
        }
        catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun savePointAny(map: MutableMap<String, Any?>, forceDelete: Boolean = false) {
        val store = getStore()
        try {
            val row = XYAny(
                map["id"] as String,
                map["anyxy"] as String,
                map["anyis"] as String?,
                map["any00"] as String?,
                map["any01"] as String?,
                map["any02"] as String?,
                map["any03"] as String?,
                map["any04"] as String?,
                map["any05"] as String?,
                map["any06"] as String?,
                map["any07"] as Int,
                map["any08"] as String,
                map["any09"] as String,
                map["any10"] as String?,
                map["any11"] as String?,
                map["any12"] as String?,
                map["any13"] as String?,
                map["any14"] as String?,
                map["any15"] as String?,
                map["any16"] as String?,
                map["any17"] as String?,
                map["any18"] as String?,
                map["any19"] as String?,
                map["any20"] as String?,
                map["any21"] as String?,
                map["any22"] as String?,
                map["any23"] as String?,
                map["any24"] as String?,
                map["any25"] as String?,
                map["any26"] as String?,
                map["any27"] as String?,
                map["any28"] as String?,
                map["any29"] as String?,
                map["any30"] as String?,
                map["any31"] as String?,
                map["any32"] as String?,
                map["any33"] as String?,
                map["any34"] as String?,
                map["any35"] as String?,
                map["any36"] as String?,
                map["any37"] as String?,
                map["any38"] as String?,
                map["any39"] as String?,
                map["any40"] as String?,
                map["any41"] as String?,
                map["any42"] as String?,
                map["any43"] as String?,
                map["any44"] as String?,
                map["any45"] as String?,
                map["any46"] as String?,
                map["any47"] as String?,
                map["any48"] as String?,
                map["any49"] as String?,
                map["any50"] as String?,
                map["any51"] as String?,
                map["any52"] as String?,
                map["any53"] as String?,
                map["any54"] as String?,
                map["any55"] as String?,
                map["any56"] as String?,
                map["any57"] as String?,
                map["any58"] as String?,
                map["any59"] as String?,
                map["any60"] as String?,
                map["anydo"] as String?,
                map["anyto"] as String?,
                map["anyio"] as String?,
                map["anyas"] as String?,
                map["anyif"] as Int,
                map["anyof"] as String?,
                map["anyby"] as String?,
                map["anyon"] as String?,
                map["anyat"] as String?
            )

            val jsonValue = mapper.writeValueAsString(row)
            val key = "${row.id}~any~box"
            if (forceDelete)
                store.delete(key)
            store.put(key, jsonValue)
            store.commit()
            
            // Verificación de coherencia (cero overhead si está desactivado)
            co.onmind.util.CoherenceStore.verifyCoherenceQuick("any")
        }
        catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun savePointDoc(map: MutableMap<String, Any?>, forceDelete: Boolean = false) {
        val store = getStore()
        try {
            val row = XYDoc(
                map["id"] as String,
                map["docxy"] as String,
                map["doc00"] as String?,
                map["doc01"] as String?,
                map["doc02"] as String?,
                map["doc03"] as String?,
                map["doc04"] as String?,
                map["doc05"] as String?,
                map["doc06"] as String?,
                map["doc07"] as Int,
                map["doc08"] as String,
                map["doc09"] as String,
                map["doc10"] as String?,
                map["docdo"] as String?,
                map["docas"] as String?,
                map["docif"] as Int,
                map["docto"] as String?,
                map["docof"] as String?,
                map["docby"] as String?,
                map["docon"] as String?,
                map["docat"] as String?
            )

            val jsonValue = mapper.writeValueAsString(row)
            val key = "${row.id}~doc~box"
            if (forceDelete)
                store.delete(key)
            store.put(key, jsonValue)
            store.commit()
            
            // Verificación de coherencia (cero overhead si está desactivado)
            co.onmind.util.CoherenceStore.verifyCoherenceQuick("doc")
        }
        catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun movePoint(id: String, prefix: String = "any") {
        val store = getStore()
        try {
            store.delete("${id}~${prefix}~box")
            store.commit()
        }
        catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
