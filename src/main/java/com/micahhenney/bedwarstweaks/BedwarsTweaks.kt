package com.micahhenney.bedwarstweaks

import com.micahhenney.bedwarstweaks.database.PlayerDatabase
import io.github.pronze.sba.SBA
import net.hypixel.api.HypixelAPI
import net.hypixel.api.apache.ApacheHttpClient
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.state.BlockBase
import net.minecraft.world.level.block.state.BlockBase.Info
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.util.Vector
import org.screamingsandals.bedwars.Main
import sun.misc.Unsafe
import java.lang.reflect.Field
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
        fun hubLocation() = Location(Bukkit.getWorld("world"), 1000.0, 52.0, 0.0)
        val hubBlocks = HashMap<Block, Int>()
        fun teleportHub(player: Player) {
            player.teleport(hubLocation())
            player.inventory.clear()
        }
    }
    val hypixelAPI = HypixelAPI(ApacheHttpClient(UUID.fromString("a3a72a45-9a64-4694-8a53-99218de4817a")))
    var BedWarsPlugin: Main? = null
    var SBAPlugin: SBA? = null
    override fun onEnable() {
        instance = this
        getCommand("hub")?.setExecutor(CommandExecutor(function = { commandSender: CommandSender, _: Command, _: String, _: Array<String> ->
            if(commandSender !is Player) return@CommandExecutor false
            teleportHub(commandSender)
            if(Main.isPlayerInGame(commandSender)) {
                BedWarsPlugin?.getGameOfPlayer(commandSender)?.leaveFromGame(commandSender)
            }
            return@CommandExecutor true
        }))
        getCommand("hypixelImport")?.setExecutor(CommandExecutor { commandSender: CommandSender, _: Command, _: String, _: Array<String> ->
            if (commandSender !is Player) return@CommandExecutor false
            PlayerDatabase.importHypixel(commandSender)
            return@CommandExecutor true
        })
        getCommand("printnbt")?.setExecutor(CommandExecutor { commandSender, command, s, strings ->
            if (commandSender !is Player) return@CommandExecutor false
            logger.info(commandSender.inventory.itemInMainHand.toString())
            logger.info(commandSender.inventory.itemInMainHand.type.toString())
            true
        })
        getCommand("test")?.setExecutor(CommandExecutor { commandSender, command, s, strings ->
            if (commandSender !is Player) return@CommandExecutor false
            val block = commandSender.world.getBlockAt(commandSender.location.subtract(Vector(0, 1, 0)))
            commandSender.sendMessage("Block below is " + block.type)
//            val mcWorld = (commandSender.world as CraftWorld).handle.
            val infoField = Info::class.java.getDeclaredField("f")
            infoField.isAccessible = true
            val blockBaseInfo: Field = BlockBase::class.java.getDeclaredField("aO")
            blockBaseInfo.isAccessible = true
            commandSender.sendMessage("Blast resistance of " + Blocks.eG.f() + " is " + infoField.get(blockBaseInfo.get(Blocks.eG)) as Float)
            commandSender.sendMessage("Blast resistance of " + Blocks.aD.f() + " is " + infoField.get(blockBaseInfo.get(Blocks.aD)) as Float)
            commandSender.sendMessage("Blast resistance of " + Blocks.dF.f() + " is " + infoField.get(blockBaseInfo.get(Blocks.dF)) as Float)
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
        try {
            val f = Unsafe::class.java.getDeclaredField("theUnsafe")
            f.isAccessible = true
            val unsafe = f.get(null) as Unsafe

            // ExplResist
            val blockBaseER: Field = BlockBase::class.java.getDeclaredField("aH")
            val offset = unsafe.objectFieldOffset(blockBaseER)

            unsafe.putFloat(Blocks.eG, offset, 12f)
            // All the glasses
            unsafe.putFloat(Blocks.aD, offset, 100000f)
            unsafe.putFloat(Blocks.ds, offset, 100000f)
            unsafe.putFloat(Blocks.dt, offset, 100000f)
            unsafe.putFloat(Blocks.du, offset, 100000f)
            unsafe.putFloat(Blocks.dv, offset, 100000f)
            unsafe.putFloat(Blocks.dw, offset, 100000f)
            unsafe.putFloat(Blocks.dx, offset, 100000f)
            unsafe.putFloat(Blocks.dy, offset, 100000f)
            unsafe.putFloat(Blocks.dz, offset, 100000f)
            unsafe.putFloat(Blocks.dA, offset, 100000f)
            unsafe.putFloat(Blocks.dB, offset, 100000f)
            unsafe.putFloat(Blocks.dC, offset, 100000f)
            unsafe.putFloat(Blocks.dD, offset, 100000f)
            unsafe.putFloat(Blocks.dE, offset, 100000f)
            unsafe.putFloat(Blocks.dF, offset, 100000f)
            unsafe.putFloat(Blocks.dG, offset, 100000f)
            unsafe.putFloat(Blocks.dH, offset, 100000f)
        } catch (e: NoSuchFieldException) {
            e.printStackTrace()
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        }
        object : BukkitRunnable() {
            override fun run() {
                for(p in hubLocation().world!!.getNearbyEntities(hubLocation(), 50.0, 50.0, 50.0) {
                    it.type == EntityType.PLAYER
                }) {
//                    (p as Player).inventory.clear()
                    if((p as Player).inventory.getItem(4)?.amount != 64){
                        if(!p.isOp) p.inventory.clear()
                        p.inventory.setItem(4, ItemStack(Material.WHITE_WOOL, 64))
                    }
                }
                for(game in BedWarsPlugin!!.games) {
                    for(player in game.connectedPlayers) {
                        for(item in player.inventory) {
                            if(item != null && item.hasItemMeta() && item.itemMeta!!.lore != null) {
                                if(item.itemMeta!!.lore!!.any { s -> s.contains("Price: ")}) {
                                    player.inventory.remove(item)
                                }
                            }
                        }
                    }
                }
            }
        }.runTaskTimer(this, 0, 10)

        Bukkit.getLogger().info("Enabled BedWars modification plugin!")
    }

    override fun onDisable() {
        PlayerDatabase.Disconnect()
        for(block in hubBlocks) {
            block.key.type = Material.AIR
        }
    }
}