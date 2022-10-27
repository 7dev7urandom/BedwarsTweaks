package com.micahhenney.bedwarstweaks

import io.github.pronze.lib.screaminglib.plugin.ServiceManager
import io.github.pronze.lib.screaminglib.utils.reflect.Reflect
import io.github.pronze.sba.SBA
import io.github.pronze.sba.SBA_BukkitImpl
import io.github.pronze.sba.service.DynamicSpawnerLimiterService
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import org.bukkit.event.entity.ItemDespawnEvent
import org.bukkit.scheduler.BukkitRunnable
import org.screamingsandals.bedwars.api.events.BedwarsResourceSpawnEvent
import org.screamingsandals.bedwars.api.game.GameStatus
import org.screamingsandals.bedwars.game.ItemSpawner

class GeneratorManager : Listener {
    init {
//        Reflect.getMethod(, "setAccordingly")
        HandlerList.unregisterAll(ServiceManager.get(DynamicSpawnerLimiterService::class.java))
        BedwarsTweaks.instance?.let { Bukkit.getPluginManager().registerEvents(this, it) }
        val task = object : BukkitRunnable() {
            override fun run() {
                for(game in BedwarsTweaks.instance?.BedWarsPlugin?.games!!) {
                    game.statusBar.isVisible = game.status != GameStatus.RUNNING
                    for(generator in game.itemSpawners) {
                        if(generator.itemSpawnerType.material != Material.EMERALD && generator.itemSpawnerType.material != Material.DIAMOND) continue
                        if((generator as ItemSpawner).maxSpawnedResources != 10 && generator.maxSpawnedResources != -1)
                            BedwarsTweaks.instance?.logger?.info("maxSpawnedResources wrong " + (generator as ItemSpawner).maxSpawnedResources)
                        generator.maxSpawnedResources = 10
                        if(generator.spawnedItems.size > 8) {
                            BedwarsTweaks.instance?.logger?.warning("Generator " + generator.type.name + " has an unexpected number of spawned items")
                        }
                    }
                    for(player in game.connectedPlayers) {
                        for((i, item) in player.inventory.withIndex()) {
                            if(item != null && item.hasItemMeta() && item.itemMeta!!.lore != null) {
                                if(item.itemMeta!!.lore!!.any { s -> s.contains("Price: ")}) {
                                    player.sendMessage("You got an illegal item from the shop! HOW?")
                                    player.inventory.remove(item)
                                }
                            }
                        }
                    }
                }
            }
        }
        BedwarsTweaks.instance?.let { task.runTaskTimer(it, 100, 20) }
    }

    @EventHandler
    fun onResourceSpawn(e: BedwarsResourceSpawnEvent) {
        e.resource.amount = 1
        e.isCancelled = (e.spawner as ItemSpawner).spawnedItems.size >= when(e.resource.type) {
            Material.DIAMOND -> 8
            Material.EMERALD -> 5
            else -> 100
        }
    }
    @EventHandler
    fun onItemDespawn(e: ItemDespawnEvent) {
        e.isCancelled = true
    }
}