package com.micahhenney.bedwarstweaks

import com.micahhenney.bedwarstweaks.itemshop.*
import com.micahhenney.bedwarstweaks.upgradeshop.UpgradeShop
import de.themoep.inventorygui.*
import io.github.pronze.lib.cloud.bukkit.BukkitCommandSender.player
import io.github.pronze.sba.SBA
import io.github.pronze.sba.utils.ShopUtil
import net.md_5.bungee.api.ChatMessageType
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.ChatColor
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.player.PlayerBucketEmptyEvent
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.player.PlayerGameModeChangeEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerItemConsumeEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.player.PlayerRespawnEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.PotionMeta
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.scheduler.BukkitRunnable
import org.screamingsandals.bedwars.Main
import org.screamingsandals.bedwars.api.events.BedwarsGameEndEvent
import org.screamingsandals.bedwars.api.events.BedwarsOpenShopEvent
import org.screamingsandals.bedwars.api.events.BedwarsPlayerBuildBlock
import org.screamingsandals.bedwars.api.game.Game
import org.screamingsandals.bedwars.api.game.GameStatus
import org.screamingsandals.bedwars.special.AutoIgniteableTNT
import org.screamingsandals.bedwars.special.Golem
import org.screamingsandals.bedwars.special.ThrowableFireball


/**
 * Example Events
 *
 * @author Dean B
 */
object BedwarsListener : Listener {
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
        if(e.store.shopFile?.equals("shop.yml", ignoreCase = true) == true) {
            ShopMenu.generateItemShopGui(e.player, e.game)
        } else if (e.store.shopFile.equals("upgradeShop.yml", ignoreCase = true)) {
            UpgradeShop.generateUpgradeShop(e.player, e.game)
        } else {
            BedwarsTweaks.instance?.logger?.info("Unknown shop type " + e.store.shopFile)
        }
    }
    @EventHandler
    fun onPlayerInteract(e: PlayerInteractEvent) {
        if (!Main.isPlayerInGame(e.player)) return
        val gamePlayer = Main.getPlayerGameProfile(e.player)
        val game = gamePlayer.game

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
                    val fb = ThrowableFireball(pGame, e.player, pGame?.getTeamOfPlayer(e.player), 3.0F, true, true)
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
    fun onBlockPlace(e: BedwarsPlayerBuildBlock) {
        if(!Main.isPlayerInGame(e.player)) return
        val game = Main.getInstance().getGameOfPlayer(e.player)
        for(team in game.availableTeams) {
            if(team.teamSpawn.distanceSquared(e.block.location) < 8*8) {
                e.isCancelled = true
                e.player.sendMessage(ChatColor.RED.toString() + "You can't place blocks here!")
                return
            }
        }
        if(e.block.type == Material.TNT) {
            e.block.type = Material.AIR
            val loc = e.block.location.add(.5, .5, .5)
            val tnt = AutoIgniteableTNT(e.game, e.player, e.game.getTeamOfPlayer(e.player), 4, true, 4.0F)
            tnt.spawn(loc)
        }
    }
    @EventHandler
    fun onAttack(e: EntityDamageByEntityEvent) {
        if(e.damager is Player && e.entity is Player) {
            val game = Main.getInstance().getGameOfPlayer(e.damager as Player)
            if(game != null && game.status == GameStatus.RUNNING) {
                if(game.isProtectionActive(e.damager as Player)) {
                    (game as org.screamingsandals.bedwars.game.Game).removeProtectedPlayer(e.damager as Player)
                    (e.damager as Player).sendMessage(ChatColor.RED.toString() + "You hit somebody and lost your respawn protection")
                }
            }
        }
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
    }
    @EventHandler
    fun onGameModeChanged(e: PlayerGameModeChangeEvent) {
        if(Main.isPlayerInGame(e.player) && e.newGameMode == GameMode.SURVIVAL) {
            val hasteLevel = UpgradeShop.teamsWithHaste[Main.getInstance().getGameOfPlayer(e.player).getTeamOfPlayer(e.player)]?: 0
            if(hasteLevel > 0) e.player.addPotionEffect(PotionEffect(PotionEffectType.FAST_DIGGING, 99999, hasteLevel - 1, false, false))
        }
    }
    @EventHandler
    fun onMove(e: PlayerMoveEvent) {
        if(e.to == null) return
        if(e.to!!.blockY <= -64) e.player.damage(100.0, e.player.killer)
    }
    @EventHandler
    fun itemConsume(e: PlayerItemConsumeEvent) {
        if(e.item.type == Material.POTION) {
            e.player.inventory.setItem(e.hand, ItemStack(Material.AIR))
            e.isCancelled = true
            val potionMeta = e.item.itemMeta as PotionMeta
            for(eff in potionMeta.customEffects) e.player.addPotionEffect(eff)
        }
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