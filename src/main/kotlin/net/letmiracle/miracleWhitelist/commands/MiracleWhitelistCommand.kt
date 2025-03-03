package net.letmiracle.miracleWhitelist.commands

import com.velocitypowered.api.command.SimpleCommand
import com.velocitypowered.api.command.SimpleCommand.Invocation
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.letmiracle.miracleWhitelist.Database
import java.util.concurrent.CompletableFuture

class MiracleWhitelistCommand(private val database: Database) : SimpleCommand {

    override fun execute(invocation: Invocation) {
        val sender = invocation.source()
        val args = invocation.arguments()

        if (args.isEmpty()) {
            sender.sendMessage(Component.text("Arguments cannot be empty.", NamedTextColor.RED))
            return
        }

        when (args[0].lowercase()) {
            "add" -> {
                if (args.size < 2) {
                    sender.sendMessage(Component.text("Please provide a player nickname to add.", NamedTextColor.RED))
                    return
                }
                val nickname = args[1]
                try {
                    database.dataSource.connection.use { connection ->
                        connection.prepareStatement(
                            """SELECT * FROM "MiracleWhitelist" WHERE "nickname" = ?"""
                        ).use { stmt ->
                            stmt.setString(1, nickname)
                            stmt.executeQuery().use { rs ->
                                if (rs.next()) {
                                    connection.prepareStatement(
                                        """UPDATE "MiracleWhitelist" SET "isActive" = true WHERE "nickname" = ?"""
                                    ).use { updateStmt ->
                                        updateStmt.setString(1, nickname)
                                        updateStmt.executeUpdate()
                                    }
                                } else {
                                    connection.prepareStatement(
                                        """INSERT INTO "MiracleWhitelist" ("nickname", "isActive") VALUES (?, true)"""
                                    ).use { insertStmt ->
                                        insertStmt.setString(1, nickname)
                                        insertStmt.executeUpdate()
                                    }
                                }
                            }
                        }
                    }
                    sender.sendMessage(
                        Component.text(
                            "Player $nickname has been added to the whitelist.", NamedTextColor.GREEN
                        )
                    )
                } catch (e: Exception) {
                    sender.sendMessage(Component.text("Error adding player: ${e.message}", NamedTextColor.RED))
                    e.printStackTrace()
                }
            }

            "remove" -> {
                if (args.size < 2) {
                    sender.sendMessage(
                        Component.text(
                            "Please provide a player nickname to remove.", NamedTextColor.RED
                        )
                    )
                    return
                }
                val nickname = args[1]
                try {
                    database.dataSource.connection.use { connection ->
                        connection.prepareStatement(
                            """SELECT * FROM "MiracleWhitelist" WHERE "nickname" = ?"""
                        ).use { stmt ->
                            stmt.setString(1, nickname)
                            stmt.executeQuery().use { rs ->
                                if (rs.next()) {
                                    connection.prepareStatement(
                                        """UPDATE "MiracleWhitelist" SET "isActive" = false WHERE "nickname" = ?"""
                                    ).use { updateStmt ->
                                        updateStmt.setString(1, nickname)
                                        updateStmt.executeUpdate()
                                    }
                                } else {
                                    sender.sendMessage(
                                        Component.text(
                                            "Player $nickname not found in whitelist.", NamedTextColor.RED
                                        )
                                    )
                                    return
                                }
                            }
                        }
                    }
                    sender.sendMessage(
                        Component.text(
                            "Player $nickname has been removed from the whitelist.", NamedTextColor.YELLOW
                        )
                    )
                } catch (e: Exception) {
                    sender.sendMessage(Component.text("Error removing player: ${e.message}", NamedTextColor.RED))
                    e.printStackTrace()
                }
            }

            else -> sender.sendMessage(Component.text("Unknown argument", NamedTextColor.DARK_RED))
        }
    }

    override fun hasPermission(invocation: Invocation): Boolean {
        return invocation.source().hasPermission("miracle.whitelist.admin")
    }

    override fun suggestAsync(invocation: Invocation): CompletableFuture<List<String>> {
        if (invocation.arguments().size > 1) {
            return CompletableFuture.completedFuture(listOf("(Player)"))
        }

        return CompletableFuture.completedFuture(listOf("add", "remove"))
    }
}
