package me.gurkz.gurkanslib

import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import me.gurkz.gurkanslib.Globals.LOGGER
import me.gurkz.gurkanslib.config.ConfigManager
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import me.lucko.fabric.api.permissions.v0.Permissions
import net.minecraft.text.Text

import net.minecraft.server.command.CommandManager.literal
import net.minecraft.server.command.CommandManager.argument

object GurkansLib : ModInitializer {
    override fun onInitialize() {
        ConfigManager.refreshAll()

        CommandRegistrationCallback.EVENT.register { dispatcher, _, _ ->
            dispatcher.register(
                literal("gurkanslib")
                    .requires(Permissions.require("gurkanslib.base", 2))
                    .then(
                        literal("reload")
                            .requires(Permissions.require("gurkanslib.base.reload-configs", 3))
                            .executes { ctx ->
                                ConfigManager.refreshAll()
                                ctx.source.sendFeedback({ Text.literal("reloading all configs") }, false)
                                return@executes 1
                            }.then(
                                argument("module", StringArgumentType.string())
                                    .suggests { _, builder ->
                                        val start = builder.remainingLowerCase
                                        ConfigManager.allRegistered.asSequence().map { it.simpleName }.sortedWith(String.CASE_INSENSITIVE_ORDER).filter { it.startsWith(start, ignoreCase = true) }.forEach { builder.suggest(it) }
                                        return@suggests builder.buildFuture()
                                    }
                                    .executes { ctx ->
                                        val module = StringArgumentType.getString(ctx, "module")
                                        val target = ConfigManager.allRegistered
                                            .asSequence()
                                            .filter { it.simpleName.equals(module) }
                                            .firstOrNull()

                                        if (target == null) {
                                            throw SimpleCommandExceptionType {
                                                Text.literal("something went wrong").toString()
                                            }.create()
                                        }
                                        ConfigManager.refresh(target)
                                        ctx.source.sendFeedback({ Text.literal("reloaded configs for $target") }, false)

                                        return@executes 1
                                    }
                            )
                    )

            )
        }

        LOGGER.info("Gurkan's Lib has now started")
    }

}