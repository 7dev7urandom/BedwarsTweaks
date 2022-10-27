package com.micahhenney.bedwarstweaks.upgradeshop

import com.micahhenney.bedwarstweaks.BedwarsListener
import com.micahhenney.bedwarstweaks.BedwarsTweaks
import com.micahhenney.bedwarstweaks.itemshop.MeleeMenu
import com.micahhenney.bedwarstweaks.itemshop.Price
import de.themoep.inventorygui.DynamicGuiElement
import de.themoep.inventorygui.InventoryGui
import de.themoep.inventorygui.StaticGuiElement
import io.github.pronze.sba.SBA
import io.github.pronze.sba.utils.ShopUtil
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.screamingsandals.bedwars.api.RunningTeam
import org.screamingsandals.bedwars.api.game.Game
import java.util.*
import kotlin.collections.HashSet

class UpgradeShop {
    companion object {
        val teamsWithHaste = HashMap<RunningTeam, Int>()
        fun generateUpgradeShop(player: Player, game: Game) {
            val items = arrayOf(
                "",
                " sph bca ",
                " fHd m   ",
                "xxxxxxxxx",
                "   B M   ",
                ""
            )
            val gui = InventoryGui(BedwarsTweaks.instance, player, "Team Upgrades", items)

            gui.addElement(DynamicGuiElement('s') { _ ->
                val item = ItemStack(Material.IRON_SWORD)
                MeleeMenu.applySharpness(player, game, item)
                val meta = item.itemMeta
                meta?.setDisplayName("Sharpness")
                if(item.containsEnchantment(Enchantment.DAMAGE_ALL)) {
                    meta?.lore = arrayListOf("",
                        ChatColor.GRAY.toString() + "Price: 8 " + ChatColor.AQUA + "Diamonds", "",
                        ChatColor.GREEN.toString() + "UNLOCKED"
                    )
                } else {
                    meta?.lore = arrayListOf("",
                        ChatColor.GRAY.toString() + "Price: 8 " + ChatColor.AQUA + "Diamonds", "",
                        if(BedwarsListener.canAfford(player, Price(Material.DIAMOND, 8)))
                            ChatColor.YELLOW.toString() + "Click to purchase!"
                        else ChatColor.RED.toString() + "You don't have enough resources!"
                    )
                }
                item.itemMeta = meta
                StaticGuiElement('s', item, { click ->
                    click.whoClicked.setItemOnCursor(ItemStack(Material.AIR))
                    if(item.containsEnchantment(Enchantment.DAMAGE_ALL)) return@StaticGuiElement true
                    else {
                        if(BedwarsListener.chargePrice(player, Price(Material.DIAMOND, 8))) {
                            SBA.getInstance().getGameStorage(game).get().setSharpnessLevel(game.getTeamOfPlayer(player), 1)
                            for( tPlayer in game.getTeamOfPlayer(player).connectedPlayers) {
                                tPlayer.sendMessage(player.displayName + " just bought " + ChatColor.GREEN.toString() + "Sharpness")
                                tPlayer.playSound(tPlayer.location, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f)
                                tPlayer.inventory.contents
                                    .filterNotNull()
                                    .forEach { item: ItemStack ->
                                        if (item.type.name.endsWith("SWORD")) {
                                            item.addEnchantment(
                                                Enchantment.DAMAGE_ALL,
                                                1
                                            )
                                        }
                                    }
                            }
                        }
                    }
                    click.gui.draw()
                    true
                })
            })
            gui.addElement(DynamicGuiElement('p') { _ ->
                val item = ItemStack(Material.IRON_CHESTPLATE)
                val currentProtLevel = SBA.getInstance().getGameStorage(game).get().getProtectionLevel(game.getTeamOfPlayer(player)).get()
                val meta = item.itemMeta
                meta?.setDisplayName("Reinforced Armor " + if(currentProtLevel >= 3) "IV" else "I".repeat(currentProtLevel + 1))
                fun getColorFromLevel(level: Int) = if(currentProtLevel >= level) ChatColor.GREEN.toString() else ChatColor.GRAY.toString()
                meta?.lore = arrayListOf("",
                    getColorFromLevel(1) + "Tier 1: Protection I, " + ChatColor.AQUA.toString() + "5 Diamonds",
                    getColorFromLevel(2) + "Tier 2: Protection II, " + ChatColor.AQUA.toString() + "10 Diamonds",
                    getColorFromLevel(3) + "Tier 3: Protection III, " + ChatColor.AQUA.toString() + "20 Diamonds",
                    getColorFromLevel(4) + "Tier 4: Protection IV, " + ChatColor.AQUA.toString() + "30 Diamonds",
                    ""
                )
                fun getPrice(): Price = Price(Material.DIAMOND, when (SBA.getInstance().getGameStorage(game).get().getProtectionLevel(game.getTeamOfPlayer(player)).get()) {
                    0 -> 5
                    1 -> 10
                    2 -> 20
                    3 -> 30
                    else -> 0
                })
                val lore = meta?.lore
                if(currentProtLevel < 4 && BedwarsListener.canAfford(player, getPrice())) {
                    lore?.add(ChatColor.YELLOW.toString() + "Click to purchase!")
                } else if (currentProtLevel >= 4) {
                    lore?.add(ChatColor.GREEN.toString() + "You've maxed out this upgrade!")
                } else  {
                    lore?.add(ChatColor.RED.toString() + "You don't have enough resources!")
                }
                meta?.lore = lore
                item.itemMeta = meta
                StaticGuiElement('s', item, { click ->
                    click.whoClicked.setItemOnCursor(ItemStack(Material.AIR))
                    if(BedwarsListener.chargePrice(player, getPrice())) {
                        val newLevel = SBA.getInstance()
                            .getGameStorage(game).get()
                            .getProtectionLevel(game.getTeamOfPlayer(player)).get() + 1
                        if(newLevel > 4) return@StaticGuiElement true
                        SBA
                            .getInstance()
                            .getGameStorage(game).get()
                            .setProtectionLevel(game.getTeamOfPlayer(player), newLevel)
                        for( tPlayer in game.getTeamOfPlayer(player).connectedPlayers) {
                            tPlayer.sendMessage(player.displayName + " just bought " + ChatColor.GREEN.toString() + "Reinforced Armor " + newLevel)
                            tPlayer.playSound(tPlayer.location, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f)
                            ShopUtil.addEnchantsToPlayerArmor(tPlayer, newLevel)
                        }
                    }
                    click.gui.draw()
                    true
                })
            })
            gui.addElement(DynamicGuiElement('h') { _ ->
                val item = ItemStack(Material.GOLDEN_PICKAXE)
                val meta = item.itemMeta
                meta?.setDisplayName("Maniac Miner")
                val currentHasteLevel = teamsWithHaste[game.getTeamOfPlayer(player)] ?: 0
                fun getColorFromLevel(level: Int) = if(currentHasteLevel >= level) ChatColor.GREEN.toString() else ChatColor.GRAY.toString()
                fun getPrice(): Price = Price(Material.DIAMOND, when (teamsWithHaste[game.getTeamOfPlayer(player)] ?: 0) {
                    0 -> 4
                    1 -> 6
                    else -> 0
                })
                val lore = arrayListOf(
                    "",
                    getColorFromLevel(1) + "Tier 1: Haste I, " + ChatColor.AQUA.toString() + "4 Diamonds",
                    getColorFromLevel(2) + "Tier 2: Haste II, " + ChatColor.AQUA.toString() + "6 Diamonds",
                    ""
                )
                if(currentHasteLevel < 2 && BedwarsListener.canAfford(player, getPrice())) {
                    lore.add(ChatColor.YELLOW.toString() + "Click to purchase!")
                } else if (currentHasteLevel >= 2) {
                    lore.add(ChatColor.GREEN.toString() + "You've maxed out this upgrade!")
                } else  {
                    lore.add(ChatColor.RED.toString() + "You don't have enough resources!")
                }
                meta?.lore = lore
                item.itemMeta = meta
                StaticGuiElement('h', item, { click ->
                    click.whoClicked.setItemOnCursor(ItemStack(Material.AIR))
                    if(BedwarsListener.chargePrice(player, getPrice())) {
                        val newLevel = (teamsWithHaste[game.getTeamOfPlayer(player)] ?: 0) + 1
                        teamsWithHaste[game.getTeamOfPlayer(player)] = newLevel
                        for(tPlayer in game.getTeamOfPlayer(player).connectedPlayers) {
                            tPlayer.addPotionEffect(PotionEffect(PotionEffectType.FAST_DIGGING, 99999, newLevel - 1, false, false))
                            tPlayer.sendMessage(player.displayName + " just bought " + ChatColor.GREEN.toString() + "Maniac Miner " + newLevel)
                            tPlayer.playSound(tPlayer.location, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f)
                        }
                    }
                    click.gui.draw()
                    true
                })
            })
            val forge = ItemStack(Material.FURNACE)
            val fMeta = forge.itemMeta
            fMeta?.setDisplayName("Upgrade forge (Unsupported)")
            forge.itemMeta = fMeta
            gui.addElement(StaticGuiElement('f', forge, { click ->
                click.whoClicked.openInventory.setItem(click.slot, ItemStack(Material.AIR))
                true
            }))
            gui.addElement(DynamicGuiElement('H') { _ ->
                val healPool = ItemStack(Material.BEACON)
                val hpMeta = healPool.itemMeta
                hpMeta?.setDisplayName("Heal Pool")
                val lore = arrayListOf("", ChatColor.GRAY.toString() + "Price: " + ChatColor.AQUA + "3 Diamonds", "")
                if(!SBA.getInstance().getGameStorage(game).get().arePoolEnabled(game.getTeamOfPlayer(player))) {
                    lore.add(if(BedwarsListener.canAfford(player, Price(Material.DIAMOND, 3)))
                        ChatColor.YELLOW.toString() + "Click to purchase!"
                    else ChatColor.RED.toString() + "You don't have enough resources!")
                } else {
                    lore.add(ChatColor.GREEN.toString() + "UNLOCKED!")
                }
                healPool.itemMeta = hpMeta
                StaticGuiElement('H', healPool, { click ->
                    click.whoClicked.openInventory.setItem(click.slot, ItemStack(Material.AIR))
                    if(BedwarsListener.chargePrice(player, Price(Material.DIAMOND, 3))) {
                        SBA.getInstance().getGameStorage(game).get().setPurchasedPool(game.getTeamOfPlayer(player), true)
                        for(tPlayer in game.getTeamOfPlayer(player).connectedPlayers) {
                            tPlayer.sendMessage(player.displayName + " just bought " + ChatColor.GREEN.toString() + "Heal Pool")
                            tPlayer.playSound(tPlayer.location, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f)
                        }
                    }
                    click.gui.draw()
                    true
                })
            })
            val dragons = ItemStack(Material.DRAGON_EGG)
            val dMeta = dragons.itemMeta
            dMeta?.setDisplayName("Dragon Buff (Unsupported)")
            dragons.itemMeta = dMeta
            gui.addElement(StaticGuiElement('d', dragons, { click ->
                click.whoClicked.openInventory.setItem(click.slot, ItemStack(Material.AIR))
                true
            }))
            gui.addElement(DynamicGuiElement('b') { _ ->
                val item = ItemStack(Material.TRIPWIRE_HOOK)
                val meta = item.itemMeta
                meta?.setDisplayName("Blindness trap")
                val lore = arrayListOf("")
                lore.add(if(BedwarsListener.canAfford(player, Price(Material.DIAMOND, 1)))
                    ChatColor.YELLOW.toString() + "Click to purchase!"
                else ChatColor.RED.toString() + "You don't have enough resources!")
                meta?.lore = lore
                item.itemMeta = meta
                StaticGuiElement('b', item, { click ->
                    click.whoClicked.openInventory.setItem(click.slot, ItemStack(Material.AIR))
                    if(SBA.getInstance().getGameStorage(game).get().areBlindTrapEnabled(game.getTeamOfPlayer(player)))
                        return@StaticGuiElement true
                    else {
                        if(BedwarsListener.chargePrice(player, Price(Material.DIAMOND, 1))) {
                            SBA.getInstance().getGameStorage(game).get().setPurchasedBlindTrap(game.getTeamOfPlayer(player), true)
                            for(tPlayer in game.getTeamOfPlayer(player).connectedPlayers) {
                                tPlayer.sendMessage(player.displayName + " just bought " + ChatColor.GREEN.toString() + "Blindness trap")
                                tPlayer.playSound(tPlayer.location, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f)
                            }
                        }
                    }
                    click.gui.draw()
                    true
                })
            })
            gui.addElement(DynamicGuiElement('m') { _ ->
                val item = ItemStack(Material.IRON_PICKAXE)
                val meta = item.itemMeta
                meta?.setDisplayName("Mining Fatigue trap")
                val lore = arrayListOf("")
                lore.add(if(BedwarsListener.canAfford(player, Price(Material.DIAMOND, 1)))
                    ChatColor.YELLOW.toString() + "Click to purchase!"
                else ChatColor.RED.toString() + "You don't have enough resources!")
                meta?.lore = lore
                item.itemMeta = meta
                StaticGuiElement('m', item, { click ->
                    click.whoClicked.openInventory.setItem(click.slot, ItemStack(Material.AIR))
                    if(SBA.getInstance().getGameStorage(game).get().areMinerTrapEnabled(game.getTeamOfPlayer(player)))
                        return@StaticGuiElement true
                    else {
                        if(BedwarsListener.chargePrice(player, Price(Material.DIAMOND, 1))) {
                            SBA.getInstance().getGameStorage(game).get().setPurchasedMinerTrap(game.getTeamOfPlayer(player), true)
                            for(tPlayer in game.getTeamOfPlayer(player).connectedPlayers) {
                                tPlayer.sendMessage(player.displayName + " just bought " + ChatColor.GREEN.toString() + "Mining Fatigue trap")
                                tPlayer.playSound(tPlayer.location, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f)
                            }
                        }
                    }
                    click.gui.draw()
                    true
                })
            })
            gui.addElement(DynamicGuiElement('B') { _ ->
                val hasBlindnessTrap = SBA.getInstance().getGameStorage(game).get().areBlindTrapEnabled(game.getTeamOfPlayer(player))
                val item: ItemStack
                if(!hasBlindnessTrap) {
                    item = ItemStack(Material.GRAY_STAINED_GLASS)
                    val meta = item.itemMeta
                    meta?.setDisplayName("Empty Blindness trap slot")
                    meta?.lore = arrayListOf("", ChatColor.RED.toString() + "You don't have a blindness trap yet")
                    item.itemMeta = meta
                } else {
                    item = ItemStack(Material.TRIPWIRE_HOOK)
                    val meta = item.itemMeta
                    meta?.setDisplayName("Blindness trap active!")
                    item.itemMeta = meta
                }
                StaticGuiElement('B', item, { click ->
                    click.whoClicked.openInventory.setItem(click.slot, ItemStack(Material.AIR))
                    true
                })
            })
            gui.addElement(DynamicGuiElement('M') { _ ->
                val hasBlindnessTrap = SBA.getInstance().getGameStorage(game).get().areMinerTrapEnabled(game.getTeamOfPlayer(player))
                val item: ItemStack
                if(!hasBlindnessTrap) {
                    item = ItemStack(Material.GRAY_STAINED_GLASS)
                    val meta = item.itemMeta
                    meta?.setDisplayName("Empty Mining Fatigue trap slot")
                    meta?.lore = arrayListOf("", ChatColor.RED.toString() + "You don't have a Mining Fatigue trap yet")
                    item.itemMeta = meta
                } else {
                    item = ItemStack(Material.IRON_PICKAXE)
                    val meta = item.itemMeta
                    meta?.setDisplayName("Mining Fatigue trap active!")
                    item.itemMeta = meta
                }
                StaticGuiElement('M', item, { click ->
                    click.whoClicked.openInventory.setItem(click.slot, ItemStack(Material.AIR))
                    true
                })
            })
            gui.addElement(StaticGuiElement('x', ItemStack(Material.GRAY_STAINED_GLASS_PANE), { click ->
                click.whoClicked.openInventory.setItem(click.slot, ItemStack(Material.AIR))
                true
            }))
            val unsupportedTrap = ItemStack(Material.GRAY_STAINED_GLASS_PANE)
            val meta = unsupportedTrap.itemMeta
            meta?.setDisplayName("Unsupported trap")
            unsupportedTrap.itemMeta = meta
            gui.addElement(StaticGuiElement('c', unsupportedTrap, { click ->
                click.whoClicked.openInventory.setItem(click.slot, ItemStack(Material.AIR))
                true
            }))
            gui.addElement(StaticGuiElement('a', unsupportedTrap, { click ->
                click.whoClicked.openInventory.setItem(click.slot, ItemStack(Material.AIR))
                true
            }))
            gui.show(player)
        }
    }
}