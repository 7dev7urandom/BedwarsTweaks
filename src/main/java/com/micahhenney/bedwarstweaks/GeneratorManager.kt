package com.micahhenney.bedwarstweaks

import io.github.pronze.lib.screaminglib.plugin.ServiceManager
import io.github.pronze.sba.game.BedwarsSBAResourceSpawnEvent
import io.github.pronze.sba.service.DynamicSpawnerLimiterService
import io.github.pronze.sba.utils.SBAUtil
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
    companion object {
        val generatorDrops = SBAUtil.parseMaterialFromConfig("running-generator-drops")
//        val generatorTimes = HashMap<Material, Int>()
//        val generatorTiers = HashMap<Material, Int>()

    }
    init {
//        Reflect.getMethod(, "setAccordingly")
        HandlerList.unregisterAll(ServiceManager.get(DynamicSpawnerLimiterService::class.java))
        BedwarsTweaks.instance?.let { Bukkit.getPluginManager().registerEvents(this, it) }
        val task = object : BukkitRunnable() {
            override fun run() {
                for(game in BedwarsTweaks.instance?.BedWarsPlugin?.games!!) {
                    game.statusBar.isVisible = game.status != GameStatus.RUNNING
                    for(generator in game.itemSpawners) {
                        if(generator.itemSpawnerType.material == Material.EMERALD || generator.itemSpawnerType.material == Material.DIAMOND) {
                            if((generator as ItemSpawner).maxSpawnedResources != 10 && generator.maxSpawnedResources != -1)
                                BedwarsTweaks.instance?.logger?.info("maxSpawnedResources wrong " + generator.maxSpawnedResources)
                            generator.maxSpawnedResources = 10
                            if(generator.spawnedItems.size > 8) {
                                BedwarsTweaks.instance?.logger?.warning("Generator " + generator.type.name + " has an unexpected number of spawned items")
                            }
//                            if(generatorTimes[generator.type.material] == null) generatorTimes[generator.type.material] = getTimeFromTypeAndTier(generator, 1)
//                            else {
//                                val timeRemaining = generatorTimes[generator.type.material]!!
//                                generatorTimes[generator.type.material] = timeRemaining - 1
//                                if(timeRemaining <= 0) {
//                                    BedwarsTweaks.instance?.logger?.info("${generator.type.material} gen tier ${generator.currentLevel}")
//                                    generatorTimes[generator.type.material] = getTimeFromTypeAndTier(generator, generator.currentLevel as Int)
//                                }
//                            }
                        } else {
                            if((generator as ItemSpawner).maxSpawnedResources != 8 && generator.maxSpawnedResources != 48 && generator.maxSpawnedResources != -1)
                                BedwarsTweaks.instance?.logger?.info("maxSpawnedResources wrong " + generator.maxSpawnedResources)
                            generator.maxSpawnedResources = if(generator.itemSpawnerType.material == Material.IRON_INGOT) 48 else 8
                            if(generator.spawnedItems.size > 48) {
                                BedwarsTweaks.instance?.logger?.warning("Generator " + generator.type.name + " has an unexpected number of spawned items")
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
        e.isCancelled = ((e !is BedwarsSBAResourceSpawnEvent) && (e.spawner.itemSpawnerType.material == Material.DIAMOND || e.spawner.itemSpawnerType.material == Material.EMERALD))
                || (e.spawner as ItemSpawner).spawnedItems.size >= when(e.resource.type) {
            Material.DIAMOND -> 8
            Material.EMERALD -> 6
            else -> 100
        }
//        e.resource.amount = 1
//        e.isCancelled =
    }
    @EventHandler
    fun onItemDespawn(e: ItemDespawnEvent) {
        if(generatorDrops.contains(e.entity.itemStack.type)) e.isCancelled = true
    }
}