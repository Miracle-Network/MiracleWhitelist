package net.letmiracle.miracleWhitelist

import java.nio.file.Files
import java.nio.file.Path

class Config {

    /**
     * Creates the configuration file
     */
    fun getDatabaseConfig(dataDirectory: Path): Path {
        if (!Files.exists(dataDirectory)) {
            Files.createDirectories(dataDirectory)
        }

        val configFile = dataDirectory.resolve("database.properties")

        if (!Files.exists(configFile)) {

            Files.write(
                configFile, """
            dataSourceClassName=org.postgresql.ds.PGSimpleDataSource
            dataSource.serverName=localhost
            dataSource.portNumber=1488
            dataSource.databaseName=database
            dataSource.user=postgres
            dataSource.password=yourpassword
            maximumPoolSize=5
            connectionTimeout=30000
        """.trimIndent().toByteArray()
            )
        }

        return configFile
    }
}