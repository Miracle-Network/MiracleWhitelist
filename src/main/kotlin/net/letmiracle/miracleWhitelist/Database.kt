package net.letmiracle.miracleWhitelist

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import java.nio.file.Path

private val CREATE_TABLE_SQL = """
            CREATE TABLE IF NOT EXISTS "MiracleWhitelist" (
                "id" SERIAL PRIMARY KEY,
                "nickname" VARCHAR(255) UNIQUE NOT NULL,
                "addedAt" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                "isActive" BOOLEAN NOT NULL DEFAULT TRUE
            );
        """.trimIndent()

/**
 * Create database instance
 * @param configDir
 */
class Database(configDir: Path) {
    private var _dataSource: HikariDataSource

    val dataSource: HikariDataSource
        get() = _dataSource

    init {
        val config = HikariConfig(configDir.toString())
        _dataSource = HikariDataSource(config)

        initTable()
    }

    /**
     * Create table if not exists
     */
    private fun initTable() {
        _dataSource.connection.use { connection ->
            connection.createStatement().use { stmt ->
                stmt.execute(CREATE_TABLE_SQL)
            }
        }
    }
}
