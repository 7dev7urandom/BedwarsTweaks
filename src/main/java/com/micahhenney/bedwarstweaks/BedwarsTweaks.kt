package com.micahhenney.bedwarstweaks

import com.micahhenney.bedwarstweaks.database.PlayerDatabase
import com.micahhenney.bedwarstweaks.database.PlayerInfo
import io.github.pronze.sba.SBA
import net.hypixel.api.HypixelAPI
import net.hypixel.api.apache.ApacheHttpClient
import net.hypixel.api.reply.PlayerReply
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitRunnable
import org.screamingsandals.bedwars.Main
import java.util.*

/**
 * Example Kotlin Plugin
 *
 * @author Dean B
 */
class BedwarsTweaks: JavaPlugin() {
    //while this is singleton, a class must be initialized by Bukkit, so we can't use 'object'
    companion object {
        var instance: BedwarsTweaks? = null
        private set

        fun teleportHub(player: Player) {
            player.teleport(Location(Bukkit.getWorld("world"), 1000.0, 52.0, 0.0))
        }
    }
    val hypixelAPI = HypixelAPI(ApacheHttpClient(UUID.fromString("a3a72a45-9a64-4694-8a53-99218de4817a")))
    var BedWarsPlugin: Main? = null
    var SBAPlugin: SBA? = null
    override fun onEnable() {
        instance = this;
        getCommand("hub")?.setExecutor(CommandExecutor(function = { commandSender: CommandSender, _: Command, _: String, _: Array<String> ->
            if(commandSender !is Player) return@CommandExecutor false
            teleportHub(commandSender)
            return@CommandExecutor true
        }))
        getCommand("hypixelImport")?.setExecutor(CommandExecutor { commandSender: CommandSender, _: Command, _: String, _: Array<String> ->
            if (commandSender !is Player) return@CommandExecutor false
            try {
                hypixelAPI.getPlayerByUuid(commandSender.uniqueId).thenAccept { value: PlayerReply ->
                    val bedwarsStats = value.player.getObjectProperty("stats").getAsJsonObject("Bedwars")
                    val favSlots = bedwarsStats.getAsJsonPrimitive("favourites_2").asString
                    commandSender.sendMessage("Found your favorite slots: $favSlots")
                    PlayerDatabase.savePlayerInfo(PlayerInfo(commandSender.uniqueId, favSlots))
                }
            } catch (e: java.lang.Exception) {
                commandSender.sendMessage("Error collecting your hypixel information")
            }
            return@CommandExecutor true
        })
        getCommand("printnbt")?.setExecutor(CommandExecutor { commandSender, command, s, strings ->
            if (commandSender !is Player) return@CommandExecutor false
            logger.info(commandSender.inventory.itemInMainHand.toString())
            logger.info(commandSender.inventory.itemInMainHand.type.id.toString())
            true
        })
        getCommand("test")?.setExecutor(CommandExecutor { commandSender, command, s, strings ->
            if (commandSender !is Player) return@CommandExecutor false
            val item = ItemStack(Material.LEGACY_POTION, 1, strings[0].toShort())
            commandSender.inventory.addItem(item)
            true
        })
        Bukkit.getPluginManager().registerEvents(BedwarsListener, this)
        GeneratorManager()
//        Bukkit.getPluginManager().registerEvents(PlayerData, this)

//        Bukkit.getLogger().info("Config Val: ${config.getString("configVal") ?: "[no val listed]"}")

        // Let Spigot know we are using the plugins
        Bukkit.getPluginManager().getPlugin("BedWars") as Main
        Bukkit.getPluginManager().getPlugin("SBA")

        BedWarsPlugin = Main.getInstance()
        SBAPlugin = SBA.getInstance()
        PlayerDatabase.Connect()

        Bukkit.getLogger().info("Enabled BedWars modification plugin!")
    }

    override fun onDisable() {
        PlayerDatabase.Disconnect()
    }
}