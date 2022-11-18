package com.micahhenney.bedwarstweaks.database

import com.micahhenney.bedwarstweaks.BedwarsTweaks
import net.hypixel.api.reply.PlayerReply
import org.bukkit.entity.Player
import java.io.File
import java.io.IOException
import java.sql.Connection
import java.sql.DriverManager
import java.sql.PreparedStatement
import java.sql.SQLException
import java.util.UUID
import java.util.logging.Level


class PlayerDatabase {
    companion object {

        var connection: Connection? = null

        fun Connect() {
            val dataFolder = File(BedwarsTweaks.instance?.dataFolder, "database.db")
            if (!dataFolder.exists()) {
                try {
                    dataFolder.createNewFile()
                } catch (e: IOException) {
                    BedwarsTweaks.instance?.logger?.log(Level.SEVERE, "File write error: database.db")
                }
            }
            try {
                if (!(connection == null || connection?.isClosed == true)) {
                    return
                }
                Class.forName("org.sqlite.JDBC")
                connection = DriverManager.getConnection("jdbc:sqlite:$dataFolder")
                val ps = connection!!.prepareStatement("CREATE TABLE IF NOT EXISTS players (uuid char(36) NOT NULL, quickBuy varchar(672) NOT NULL, hotbarManager varchar(9) NOT NULL, PRIMARY KEY (uuid))")
                ps.executeUpdate()
                ps.close()
                return
            } catch (ex: SQLException) {
                BedwarsTweaks.instance?.logger?.log(Level.SEVERE, "SQLite exception on initialize", ex)
            } catch (ex: ClassNotFoundException) {
                BedwarsTweaks.instance?.logger
                    ?.log(Level.SEVERE, "You need the SQLite JBDC library. Google it. Put it in /lib folder.")
            }
            return
        }

        fun getPlayerInfo(player: Player): PlayerInfo? {
            var ps: PreparedStatement? = null

            try {
                ps = connection!!.prepareStatement("SELECT * FROM players WHERE uuid = ?")
                ps.setString(1, player.uniqueId.toString())

                val rs = ps.executeQuery()
                if(rs.next()) {
                    val uuid = UUID.fromString(rs.getString("uuid"))
                    val quickBuy = rs.getString("quickBuy")
                    val hotbarManager = rs.getString("hotbarManager")
                    rs.close()
                    return PlayerInfo(uuid, quickBuy, hotbarManager)
                } else {
                    return null
                }
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            } finally {
                try {
                    ps?.close()
                } catch(_: java.lang.Exception) {}
            }
            return null
        }

        fun savePlayerInfo(playerInfo: PlayerInfo) {
            var ps: PreparedStatement? = null

            try {
                ps = connection!!.prepareStatement("INSERT OR REPLACE INTO players (uuid, quickBuy, hotbarManager) VALUES (?, ?, ?)")
                ps.setString(1, playerInfo.uuid.toString())
                ps.setString(2, playerInfo.quickBuy)
                ps.setString(3, playerInfo.hotbarManager)
                ps.executeUpdate()
            } catch (e: SQLException) {
                e.printStackTrace()
            } finally {
                try {
                    ps?.close()
                } catch (_: java.lang.Exception) {}
            }
        }

        fun importHypixel(player: Player) {
            try {
                BedwarsTweaks.instance?.hypixelAPI?.getPlayerByUuid(player.uniqueId)?.thenAccept { value: PlayerReply ->
                    val bedwarsStats = value.player.getObjectProperty("stats").getAsJsonObject("Bedwars")
                    val favSlots = bedwarsStats.getAsJsonPrimitive("favourites_2").asString
                    val hotbar = bedwarsStats.getAsJsonPrimitive("favorite_slots")?.asString?.split(',')?.map {
                        when (it) {
                            "Blocks" -> 'B'
                            "Melee" -> 'M'
                            "Tools" -> 'T'
                            "Utility" -> 'U'
                            "Potions" -> 'P'
                            "null" -> 'x'
                            else -> 'x'
                        }
                    }?.joinToString("")?: "xxxxxxxxx"
                    if(hotbar.length != 9) throw Error("Invalid Hotbar from hypixel: $hotbar")
                    player.sendMessage("Found your Quick-Buy and HotbarManager information")
                    val pData = getPlayerInfo(player)?: PlayerInfo(player.uniqueId, favSlots, hotbar)
                    pData.quickBuy = favSlots
                    pData.hotbarManager = hotbar
                    savePlayerInfo(pData)
                }
            } catch (e: java.lang.Exception) {
                player.sendMessage("Error collecting your hypixel information")
            }

        }

        fun Disconnect() {
            try {
                connection?.close()
            } catch (_: SQLException) {}
        }
    }
}


