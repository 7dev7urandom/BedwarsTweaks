package com.micahhenney.bedwarstweaks

import com.micahhenney.bedwarstweaks.itemshop.*
import com.micahhenney.bedwarstweaks.upgradeshop.UpgradeShop
import io.github.pronze.sba.events.SBATeamTrapTriggeredEvent
import net.minecraft.world.level.Explosion
import net.minecraft.world.level.ExplosionDamageCalculator
import org.bukkit.*
import org.bukkit.craftbukkit.v1_19_R1.CraftWorld
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerBucketEmptyEvent
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.player.PlayerGameModeChangeEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerItemConsumeEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.PotionMeta
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.scheduler.BukkitRunnable
import org.screamingsandals.bedwars.Main
import org.screamingsandals.bedwars.api.events.BedwarsGameEndEvent
import org.screamingsandals.bedwars.api.events.BedwarsGameEndingEvent
import org.screamingsandals.bedwars.api.events.BedwarsOpenShopEvent
import org.screamingsandals.bedwars.api.events.BedwarsPlayerBuildBlock
import org.screamingsandals.bedwars.api.game.Game
import org.screamingsandals.bedwars.api.game.GameStatus
import org.screamingsandals.bedwars.special.Golem
import org.screamingsandals.bedwars.special.ThrowableFireball
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


/**
 * Example Events
 *
 * @author Dean B
 */
object BedwarsListener : Listener {
    val Cooldowns = HashMap<Player, Long>()
    val PlayersWithMilk = HashMap<Player, BukkitRunnable>()
    @EventHandler
    fun onJoin(e: PlayerJoinEvent) {
        val gameName = (if (Main.isPlayerGameProfileRegistered(e.player)) Main.getPlayerGameProfile(e.player).latestGameName else null)

        if(gameName == null) {
            BedwarsTweaks.teleportHub(e.player)
            return
        }

        Main.getGame(gameName)?.joinToGame(e.player)
        e.player.sendMessage("Your game is not finished, so you automatically rejoined. To exit, use /bw leave")
    }
    @EventHandler
    fun onShopOpen(e: BedwarsOpenShopEvent) {
        e.result = BedwarsOpenShopEvent.Result.DISALLOW_UNKNOWN
        if(e.store.shopFile?.equals("shop.yml", ignoreCase = true) != false) { // true or null
            ShopMenu.generateItemShopGui(e.player, e.game)
        } else if (e.store.shopFile.equals("upgradeShop.yml", ignoreCase = true)) {
            UpgradeShop.generateUpgradeShop(e.player, e.game)
        } //else {
//            BedwarsTweaks.instance?.logger?.info("Unknown shop type " + e.store.shopFile)
//        }
    }
    @EventHandler
    fun onPlayerInteract(e: PlayerInteractEvent) {
        if(!e.player.isOp
            && e.clickedBlock?.location?.world?.name == "world"
            && e.clickedBlock!!.location.distanceSquared(
                Location(Bukkit.getWorld("world"),
                    1000.0, 50.0, 0.0)
            ) < 200*200) {
            if(e.action != Action.RIGHT_CLICK_BLOCK || e.player.location.y > 55) e.isCancelled = true
        }
        if (!Main.isPlayerInGame(e.player)) return
        val gamePlayer = Main.getPlayerGameProfile(e.player)
        val game = gamePlayer.game
        if(e.action == Action.RIGHT_CLICK_AIR && e.item != null && e.item!!.type == Material.BLAZE_ROD && game.status == GameStatus.GAME_END_CELEBRATING) {
            if(Cooldowns.contains(e.player) && System.currentTimeMillis() - Cooldowns[e.player]!! < 500) {
                e.player.sendMessage(
                    "Please wait for the cooldown of %.2f more seconds"
                        .format((500 - (System.currentTimeMillis() - Cooldowns[e.player]!!)).toFloat() / 1000
                        ))
            } else {
                val pos = e.player.location.add(e.player.eyeLocation.direction.multiply(12))
                e.player.location.world?.createExplosion(pos, 5f, false, false)
                //     public Explosion a(@Nullable Entity entity, @Nullable DamageSource damagesource, @Nullable ExplosionDamageCalculator explosiondamagecalculator, double d0, double d1, double d2, float f, boolean flag, Explosion.Effect explosion_effect) {
                (e.player.world as CraftWorld).handle.a(null, null, object : ExplosionDamageCalculator() {

                }, pos.x, pos.y, pos.z, 5f, false, Explosion.Effect.b)
                Cooldowns[e.player] = System.currentTimeMillis()
            }
            return
        }
        if(e.action == Action.RIGHT_CLICK_AIR || e.action == Action.RIGHT_CLICK_BLOCK) {
            if (game.status == GameStatus.RUNNING && !gamePlayer.isSpectator && e.item != null) {
                if(e.item!!.type == Material.GHAST_SPAWN_EGG) {
                    e.isCancelled = true
                    val loc = if(e.clickedBlock == null) e.player.location else e.clickedBlock!!.getRelative(e.blockFace).location.add(.5, .5, .5)
                    val golem = Golem(game, e.player, game.getTeamOfPlayer(e.player), e.item, loc, .25, 16.0, 50.0, "%teamcolor%%team% Golem", true)
                    golem.spawn()
                } else if (e.item!!.type == Material.FIRE_CHARGE) {
                    e.isCancelled = true
                    val plugin = BedwarsTweaks.instance?.BedWarsPlugin
                    val pGame = plugin?.getGameOfPlayer(e.player)
                    val fb = ThrowableFireball(pGame, e.player, pGame?.getTeamOfPlayer(e.player), 3.0F, true, false)
                    fb.run()
                    if (e.item!!.amount > 1) {
                        e.item!!.amount -= 1
                    } else {
                        try {
                            if (e.player.inventory.itemInOffHand == e.item) {
                                e.player.inventory.setItemInOffHand(ItemStack(Material.AIR))
                            } else {
                                e.player.inventory.remove(e.item!!)
                            }
                        } catch (err: Throwable) {
                            e.player.inventory.remove(e.item!!)
                        }
                        e.player.updateInventory()
                    }
                }
            }
        }
    }
    @EventHandler
    fun onBedwarsPlaceBlock(e: BedwarsPlayerBuildBlock) {
        if(!Main.isPlayerInGame(e.player)) return
        val game = Main.getInstance().getGameOfPlayer(e.player)
        for(team in game.availableTeams) {
            if(team.teamSpawn.distanceSquared(e.block.location) < when(game.name) {
                    "invasion" -> 8*8
                    "archway" -> 5*5
                    "aquarium" -> 6*6
                    else -> 6*6
                }) {
                e.isCancelled = true
                e.player.sendMessage(ChatColor.RED.toString() + "You can't place blocks here!")
                return
            }
        }
        for(generator in game.itemSpawners) {
            if(generator.location.distanceSquared(e.block.location) < 3*3) {
                e.isCancelled = true
                e.player.sendMessage(ChatColor.RED.toString() + "You can't place blocks here!")
                return
            }
        }
        if(e.block.type == Material.TNT) {
            e.block.type = Material.AIR
            val loc = e.block.location.add(.5, .5, .5)
            val tnt = BedwarsTNT(e.game, e.player, 52) // 2.6 * 20
            tnt.spawn(loc)
        }
    }
    @EventHandler
    fun onEntityDamageEntity(e: EntityDamageByEntityEvent) {
        if(e.damager is Player && e.entity is Player) {
            val game = Main.getInstance().getGameOfPlayer(e.damager as Player)
            if(game != null && game.status == GameStatus.RUNNING) {
                if(game.isProtectionActive(e.damager as Player)) {
                    (game as org.screamingsandals.bedwars.game.Game).removeProtectedPlayer(e.damager as Player)
                    (e.damager as Player).sendMessage(ChatColor.RED.toString() + "You hit somebody and lost your respawn protection")
                }
            }
        } else if (e.damager.type == EntityType.FIREBALL && e.entity.type == EntityType.PRIMED_TNT) e.isCancelled = true
    }
    @EventHandler
    fun onItemThrow(e: PlayerDropItemEvent) {
        object : BukkitRunnable() {
            override fun run() {
                e.player.updateInventory()
            }
        }.runTaskLater(BedwarsTweaks.instance!!, 1)
    }
    @EventHandler
    fun onBucket(e: PlayerBucketEmptyEvent) {
        if(Main.getInstance().getGameOfPlayer(e.player) != null)
            e.itemStack = ItemStack(Material.AIR)
    }
    @EventHandler
    fun onGameEnd(e: BedwarsGameEndEvent) {
        for(team in e.game.runningTeams) {
            UpgradeShop.teamsWithHaste.remove(team)
        }
        val players = e.game.connectedPlayers.toMutableList()
        object : BukkitRunnable() {
            override fun run() {
                val game = Main.getInstance().gameWithHighestPlayers
                for(p in players) {
                    if(!p.isOnline || Main.getInstance().getGameOfPlayer(p) !== null) continue
                    if(game == null) {
                        p.sendMessage(ChatColor.RED.toString() + "There are no available games to join")
                    } else {
                        game.joinToGame(p)
                    }
                }
            }
        }.runTaskLater(BedwarsTweaks.instance!!, 5*20)
    }
    @EventHandler
    fun onGameEnding(e: BedwarsGameEndingEvent) {
        for(p in e.winningTeam.connectedPlayers) {
            p.allowFlight = true
            p.isFlying = true
            val flyItem = ItemStack(Material.BLAZE_ROD)
            flyItem.addUnsafeEnchantment(Enchantment.ARROW_INFINITE, 1)
            val meta = flyItem.itemMeta
            meta?.setDisplayName("Rod of Celebration")
            flyItem.itemMeta = meta
            p.inventory.setItemInMainHand(flyItem)
        }
    }
    @EventHandler(priority = EventPriority.MONITOR)
    fun onGameModeChanged(e: PlayerGameModeChangeEvent) {
        if(e.isCancelled) return
        if(Main.isPlayerInGame(e.player) && e.newGameMode == GameMode.SURVIVAL) {
            BedwarsTweaks.instance?.logger?.info("Handling gm change")
            val hasteLevel = UpgradeShop.teamsWithHaste[Main.getInstance().getGameOfPlayer(e.player).getTeamOfPlayer(e.player)]?: 0
            if(hasteLevel > 0) e.player.addPotionEffect(PotionEffect(PotionEffectType.FAST_DIGGING, 99999, hasteLevel - 1, false, false))
            BedwarsTweaks.instance?.logger?.info("Handling items in .1 second")
            object : BukkitRunnable() {
                override fun run() {
                    val tools = ArrayList<ItemStack>()
//                    tools.add(ItemStack(Material.WOODEN_SWORD))
                    for (i in e.player.inventory.contents.size - 1 downTo 0) {
                        val item = e.player.inventory.contents[i] ?: continue
                        if(ToolsMenu.hasItem(item, e.player, Main.getInstance().getGameOfPlayer(e.player))) {
                            e.player.inventory.remove(item)
                            BedwarsTweaks.instance?.logger?.info("Found $item, removed and adding to slot")
                            tools.add(item)
                        }
                        if (item.type == Material.WOODEN_SWORD) {
                            e.player.inventory.remove(item)
                            BedwarsTweaks.instance?.logger?.info("Found sword, removed and adding to slot")
//                            tools.add(item)
                        }
                    }
                    ShopMenu.addItemToCorrectHotbarSlot(e.player, Main.getInstance().getGameOfPlayer(e.player), ItemStack(Material.WOODEN_SWORD), 'M')
                    for(tool in tools) {
                        ShopMenu.addItemToCorrectHotbarSlot(e.player, Main.getInstance().getGameOfPlayer(e.player), tool, 'T')
                    }
                }
            }.runTaskLater(BedwarsTweaks.instance!!, 2)
        }
    }
    @EventHandler
    fun onMove(e: PlayerMoveEvent) {
        if(e.to == null) return
        if(e.to!!.blockY <= -30) e.player.damage(100.0, e.player.killer)
    }
    @EventHandler
    fun itemConsume(e: PlayerItemConsumeEvent) {
        if(e.item.type == Material.POTION) {
            e.player.inventory.setItem(e.hand, ItemStack(Material.AIR))
            e.isCancelled = true
            val potionMeta = e.item.itemMeta as PotionMeta
            for(eff in potionMeta.customEffects) {
                if(eff.hasParticles()) {
                    BedwarsTweaks.instance?.logger?.warning("Particles on effect!")
                }
                e.player.addPotionEffect(eff)
            }
        } else if (e.item.type == Material.MILK_BUCKET) {
            e.player.inventory.setItem(e.hand, ItemStack(Material.AIR))
            e.isCancelled = true
            e.player.sendMessage("You will not trigger any traps for 30 seconds!")
            PlayersWithMilk[e.player]?.cancel()
            PlayersWithMilk[e.player] = object : BukkitRunnable() {
                override fun run() {
                    e.player.sendMessage(ChatColor.RED.toString() + "Your magic milk has run out!")
                    PlayersWithMilk.remove(e.player)
                }
            }
            PlayersWithMilk[e.player]?.run()
        }
    }
    @EventHandler
    fun blockBreak(e: BlockBreakEvent) {
        if(e.player != null
            && !e.player.isOp
            && e.block.location.world?.name == "world"
            && e.block.location.distanceSquared(
                Location(Bukkit.getWorld("world"),
                    1000.0, 50.0, 0.0)
            ) < 200*200) {
            e.isCancelled = true
        }
    }
    @EventHandler
    fun onBlockPlace(e: BlockPlaceEvent) {
        if(BedwarsTweaks.hubLocation().world == e.player.world && BedwarsTweaks.hubLocation().distanceSquared(e.player.location) < 200*200) {
            if(e.block.type == Material.WHITE_WOOL) {
                Bukkit.getScheduler().scheduleSyncDelayedTask(BedwarsTweaks.instance!!, {
                    e.block.type = Material.AIR
                }, 20 * 5)
            }
        }
    }
    @EventHandler
    fun onPlayerDie(e: PlayerDeathEvent) {
        if(e.entity.location.world == BedwarsTweaks.hubLocation().world && e.entity.location.distanceSquared(BedwarsTweaks.hubLocation()) < 100*100) {
            e.drops.clear()
        }
    }

    @EventHandler
    fun onItemDrop(e: PlayerDropItemEvent) {
        if(Main.isPlayerInGame(e.player)) {
            if(e.itemDrop.itemStack.containsEnchantment(Enchantment.DAMAGE_ALL)) e.itemDrop.itemStack.removeEnchantment(Enchantment.DAMAGE_ALL)
            if(e.itemDrop.itemStack.type == Material.WOODEN_SWORD) e.isCancelled = true
        }
    }

    @EventHandler
    fun onTrapTriggered(e: SBATeamTrapTriggeredEvent) {
        if(PlayersWithMilk.contains(e.trapped)) e.isCancelled = true
    }

    fun chargePrice(player: Player, price: Price): Boolean {
        return chargePrice(player, price.currency, price.amount)
    }
    fun canAfford(player: Player, price: Price): Boolean {
        return canAfford(player, price.currency, price.amount)
    }
    fun canAfford(player: Player, resource: Material, amount: Int): Boolean {
        var total = 0
        for(item in player.inventory) {
            if(item == null) continue
            if(item.type == resource) total += item.amount
            if(total >= amount) break
        }
        if(total < amount) return false
        return true
    }
    fun chargePrice(player: Player, resource: Material, amount: Int): Boolean {
        if(!canAfford(player, resource, amount)) return false
        var remaining = amount
        for(item in player.inventory) {
            if(item == null) continue
            if(item.type == resource) {
                if(item.amount >= remaining) {
                    item.amount -= remaining
                    break
                } else {
                    remaining -= item.amount
                    item.amount = 0
                }
            }
        }
        return true
    }
}