package net.letmiracle.miracleWhitelist;

import com.google.inject.Inject
import com.velocitypowered.api.command.CommandManager
import com.velocitypowered.api.command.SimpleCommand
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.connection.PreLoginEvent
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent
import com.velocitypowered.api.plugin.Plugin
import com.velocitypowered.api.plugin.annotation.DataDirectory
import com.velocitypowered.api.proxy.ProxyServer
import net.kyori.adventure.text.Component
import net.letmiracle.miracleWhitelist.commands.MiracleWhitelistCommand
import org.slf4j.Logger
import java.nio.file.Path


@Plugin(
    id = "miraclewhitelist", name = "MiracleWhitelist", version = BuildConstants.VERSION
)
class MiracleWhitelist @Inject constructor(
    private val logger: Logger, private val proxy: ProxyServer, @DataDirectory private val dataDirectory: Path
) {
    private val database = Database(Config().getDatabaseConfig(dataDirectory))

    /**
     * Initialize the database using the previously created configuration file
     */
    @Subscribe
    fun onProxyInitialization(event: ProxyInitializeEvent) {
        val commandManager: CommandManager = proxy.commandManager
        val commandMeta = commandManager.metaBuilder("mw").plugin(this).build()

        val miracleWhitelistCommand: SimpleCommand = MiracleWhitelistCommand(database)
        commandManager.register(commandMeta, miracleWhitelistCommand);
    }

    /**
     * Check if player is in the whitelist
     */
    @Subscribe
    fun prePlayerLogin(event: PreLoginEvent) {
        val playerName = event.username

        database.dataSource.connection.use { connection ->
            connection.prepareStatement(
                """SELECT * FROM "MiracleWhitelist" WHERE "nickname" = ? AND "isActive" = true"""
            ).use { stmt ->
                stmt.setString(1, playerName)
                stmt.executeQuery().use { rs ->
                    if (!rs.next()) {
                        event.result = PreLoginEvent.PreLoginComponentResult.denied(
                            // You are not in the whitelist, you may apply an application on our website
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
