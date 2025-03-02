package net.letmiracle.miracleWhitelist;

import com.google.inject.Inject
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.connection.PreLoginEvent
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent
import com.velocitypowered.api.plugin.Plugin
import com.velocitypowered.api.plugin.annotation.DataDirectory
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import net.kyori.adventure.text.Component
import org.slf4j.Logger
import java.nio.file.Files
import java.nio.file.Path

@Plugin(
    id = "miraclewhitelist", name = "MiracleWhitelist", version = BuildConstants.VERSION
)
class MiracleWhitelist @Inject constructor(private val logger: Logger, @DataDirectory val dataDirectory: Path) {

    private lateinit var dataSource: HikariDataSource

    /**
     * Create configuration file at first launch.
     * Initialize the database using the previously created configuration file
     */
    @Subscribe
    fun onProxyInitialization(event: ProxyInitializeEvent) {
        if (!Files.exists(dataDirectory)) {
            Files.createDirectories(dataDirectory)
        }

        val configFile = dataDirectory.resolve("database.properties")

        if (!Files.exists(configFile)) {
            logger.info("First launch... Please fill out the configuration file")

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

        val config = HikariConfig(configFile.toString())
        dataSource = HikariDataSource(config)
    }

    /**
     * Check if player is in whitelist
     */
    @Subscribe
    fun prePlayerLogin(event: PreLoginEvent) {
        val playerName = event.username

        dataSource.connection.use { connection ->
            connection.prepareStatement(
                """SELECT * FROM "MiracleWhitelist" WHERE "nickname" = ? AND "isActive" = true"""
            ).use { stmt ->
                stmt.setString(1, playerName)
                stmt.executeQuery().use { rs ->
                    if (!rs.next()) {
                        event.result = PreLoginEvent.PreLoginComponentResult.denied(
                            // You are not in the whitelist, you may submit an application on our website
                            Component.text("Вы не в вайтлисте, подать заявку можно на нашем сайте.")
                        )
                        logger.info("$playerName tried to join, but he's not in the whitelist")

                        return
                    }

                    logger.info("$playerName successfully joined")
                }
            }
        }
    }
}
