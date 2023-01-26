package xy.db

import org.apache.commons.dbutils.DbUtils
import org.apache.commons.dbutils.QueryRunner
import org.apache.commons.dbutils.handlers.MapListHandler
import java.sql.SQLException

/**
 * Created by Cesar Andres Arcila Buitrago from Colombia on 21/08/16.
 * Refactorized on 21/12/20
 */

class RDB() {

    @Throws(SQLException::class)
    fun forQuery(sql: String): List<MutableMap<String, Any?>>? {
        val result: List<MutableMap<String, Any?>>?
        val qr = QueryRunner()
        //DbUtils.loadDriver(onminddai.driver)
        val conn = onminddai.dbc?.getConnection()
        try {
            result = qr.query(conn, sql, MapListHandler())
            //for (objects in result) { println(objects) }
        }
        finally {
            DbUtils.close(conn)
        }
        return result
    }

    @Throws(SQLException::class)
    fun forUpdate(sql: String): Int {
        var rows: Int = 0
        val qr = QueryRunner()
        //DbUtils.loadDriver(onminddai.driver)
        val conn = onminddai.dbc?.getConnection()
        try {
            rows = qr.update(conn, sql)
        }
        finally {
            DbUtils.close(conn)
        }
        return rows
    }
/*
    fun forQuery(sql: String): Future<RowSet<Row>> = SqlTemplate.forQuery(onminddai.dbc, sql).execute(null)

    fun <T> forQuery(sql: String, res: (RowSet<Row>) -> T): Future<RowSet<Row>> =
        SqlTemplate.forQuery(onminddai.dbc, sql)
            .execute(null)

    fun forUpdate(sql: String): Future<SqlResult<Void>> = SqlTemplate.forUpdate(onminddai.dbc, sql).execute(null)

    fun <T> forUpdate(sql: String, res: (SqlResult<Void>) -> T): Future<SqlResult<Void>> =
        SqlTemplate.forUpdate(onminddai.dbc, sql)
            .execute(null)
*/
}
