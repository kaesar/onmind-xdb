package co.onmind.trait

/**
 * Created by Cesar Andres Arcila Buitrago from Colombia on 12/12/15.
 */

interface AbstractDB {
    //fun mapQuery(rows: RowSet<Row>): List<Any>

    //fun mapQueryOne(rows: RowSet<Row>): Any? = mapQuery(rows).first()

    //fun forQuery(xy: String, where: String?, order: String?, limit: Int?): Future<RowSet<Row>>

    fun setQuery(select: String, colxy: String, xy: String, where: String?, order: String?, limit: Int?): String {
        var sql = "$select WHERE $colxy='$xy'"
        if (where != null && xy != "*") sql += " AND $where"  // && !where.contains("id=")
        else if (where != null) sql = "$select WHERE $where"
        if (order != null) sql += " ORDER BY $order"
        if (limit != null) {
            //if (!onmindxdb.driver!!.contains("oracle")) {   // for databases like H2, PostgreSQL, MySQL, MariaDB, SQLite
            sql += " LIMIT $limit"
            //if (onmindxdb.driver!!.contains("firebird"))      // for FirebirdSQL database
            //    sql = sql.replace("LIMIT","ROWS")
            //else if (onmindxdb.driver!!.contains("derby"))    // for Java DB/Derby database
            //    sql = sql.replace("LIMIT","FETCH FIRST") + " ROWS ONLY"
            //}
            //else                                            // for Oracle database without order by clause
            //    sql += " rownum = $limit"
        }
        return sql
    }
/*
    fun getAny(xy: String, where: String?, order: String?, limit: Int?): List<Any> {
        val rows = forQuery(xy, where, order, limit).await()
        return mapQuery(rows)
    }

    fun exists(where: String): Future<Boolean> {
        val result = Promise.promise<Boolean>()
        val task = forQuery("*", where, null, 1)
        task.onComplete {
            if (it.succeeded() && it.result().size() > 0)
                result.complete(true)
            else
                result.complete(false)
        }
        return result.future()
    }

    fun toRows (rows: RowSet<Row>) = json {
        rows.map { row ->
            obj {
                for (i in 0 until row.size()) {
                    put(row.getColumnName(i).lowercase(), row.getValue(i))
                }
            }
        }
    }
*/
    fun xtableDDL(
            table: String,
            columnsList: List<String>,
            dataTypes: List<String>,
            sizes: List<Int?>,
            nulls: List<String>,
            defaults: List<String?>,
            key: String = "id",
            unique: String? = null,
            driver: String? = "mariadb",
            skip: Boolean = false): String
    {
        var ddl: String = "CREATE TABLE $table ("
        if (skip) ddl += "\n  "

        for (i in columnsList.indices) {
            if (columnsList[i] != key) {
                ddl += ", " + columnsList[i] + " "
                if (dataTypes[i] == "T")
                    ddl += "varchar(" + sizes[i] + ")"
                else if (dataTypes[i] == "N")
                    ddl += "integer"
                else if (dataTypes[i] == "M")
                    ddl += "real"

                if (nulls[i] == "-")
                    ddl += " not null"

                if (defaults[i] != null) {
                    ddl += " default "
                    if (dataTypes[i] == "T")
                        ddl += "'" + defaults[i] + "'"
                    else
                        ddl += defaults[i]
                }
            }
            else {
                ddl += "$key "
                if (dataTypes[i] == "T")
                    ddl += "varchar(" + sizes[i] + ")"
                if (driver != null && driver.indexOf("sqlite") > -1)
                    ddl += " primary key not null"
                else
                    ddl += " not null"
            }
            if (skip) ddl += "\n"
        }
        if (driver != null && driver.indexOf("sqlite") == -1)
            ddl += ", constraint p${table} primary key ($key)"
        if (unique != null)
            ddl += ", constraint u${table}01 unique ($unique));"
        else
            ddl += ");"

        if (ddl.contains("CREATE",true)) {  // TWEAK
            //if (driver!!.indexOf("mariadb") > -1)
            //    ddl = ddl.replace(");",") engine = rocksdb;")
            //else
            if (driver!!.indexOf("firebird") > -1)
                ddl = ddl.replace("not null default","default",true)
            else if (driver!!.indexOf("oracle") > -1) {
                ddl = ddl.replace("not null default","default",true)
                ddl = ddl.replace("integer","number(16)",true)
                ddl = ddl.replace("real","number(20,8)",true)
                ddl = ddl.replace(");",") TABLESPACE XY;")
            }
        }
        //if (skip)
            ddl += "\n"
        return ddl
    }
}
