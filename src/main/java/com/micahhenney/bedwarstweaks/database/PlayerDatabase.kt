package com.micahhenney.bedwarstweaks.database

import com.micahhenney.bedwarstweaks.BedwarsTweaks
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
                val ps = connection!!.prepareStatement("CREATE TABLE IF NOT EXISTS players (uuid char(36) NOT NULL, quickBuy varchar(672) NOT NULL, PRIMARY KEY (uuid))")
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
                    rs.close()
                    return PlayerInfo(uuid, quickBuy)
                } else {
                    return null
                }
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            } finally {
                try {
                    ps?.close()
                } catch(e: java.lang.Exception) {}
            }
            return null
        }

        fun savePlayerInfo(playerInfo: PlayerInfo) {
            var ps: PreparedStatement? = null

            try {
                ps = connection!!.prepareStatement("INSERT OR REPLACE INTO players (uuid, quickBuy) VALUES (?, ?)")
                ps.setString(1, playerInfo.uuid.toString())
                ps.setString(2, playerInfo.quickBuy)
                ps.executeUpdate()
            } catch (e: SQLException) {
                e.printStackTrace()
            } finally {
                try {
                    ps?.close()
                } catch (e: java.lang.Exception) {}
            }
        }

        fun Disconnect() {
            try {
                connection?.close()
            } catch (e: SQLException) {}
        }
    }
}


