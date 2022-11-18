package com.micahhenney.bedwarstweaks

import org.bukkit.Location
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.entity.TNTPrimed
import org.bukkit.metadata.FixedMetadataValue
import org.bukkit.scheduler.BukkitRunnable
import org.screamingsandals.bedwars.Main
import org.screamingsandals.bedwars.api.game.Game
import org.screamingsandals.bedwars.special.AutoIgniteableTNT

class BedwarsTNT(game: Game, player: Player, private val explosionTicks: Int) : AutoIgniteableTNT(game, player, game.getTeamOfPlayer(player), 4, true, 4F) {

    override fun spawn(location: Location?) {
        val tnt = location!!.world!!.spawnEntity(location, EntityType.PRIMED_TNT) as TNTPrimed
        Main.getInstance().registerEntityToGame(tnt, game)
        tnt.yield = damage
        tnt.fuseTicks = explosionTicks
//        if (!damagePlacer) {
//            tnt.setMetadata(player.uniqueId.toString(), FixedMetadataValue(Main.getInstance(), null as Any?))
//        }

        tnt.setMetadata("autoignited", FixedMetadataValue(Main.getInstance(), null as Any?))
        object : BukkitRunnable() {
            override fun run() {
                Main.getInstance().unregisterEntityFromGame(tnt)
            }
        }.runTaskLater(Main.getInstance(), (explosionTime + 10).toLong())

    }
}