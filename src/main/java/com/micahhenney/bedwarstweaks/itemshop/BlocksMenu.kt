package com.micahhenney.bedwarstweaks.itemshop

import com.micahhenney.bedwarstweaks.BedwarsTweaks
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.screamingsandals.bedwars.api.TeamColor
import org.screamingsandals.bedwars.api.game.Game

class BlocksMenu(player: Player, game: Game) : ShopMenu(player, game, items) {

    companion object {
        private val items = ArrayList<ShopItem>()

        init {
            items.add(ShopItem("wool", { player, game ->
                ItemStack(when (game.getTeamOfPlayer(player).color) {
                    TeamColor.BLUE -> Material.BLUE_WOOL
                    TeamColor.GREEN -> Material.GREEN_WOOL
                    TeamColor.RED -> Material.RED_WOOL
                    TeamColor.YELLOW -> Material.YELLOW_WOOL
                    else -> {
                        BedwarsTweaks.instance?.logger?.info("Invalid team color " + game.getTeamOfPlayer(player).color)
                        Material.WHITE_WOOL
                    }
                }, 16)
            }, Price(Material.IRON_INGOT, 4)))
            items.add(ShopItem("hard_clay", { player, game ->
                ItemStack(when (game.getTeamOfPlayer(player).color) {
                    TeamColor.BLUE -> Material.BLUE_TERRACOTTA
                    TeamColor.GREEN -> Material.GREEN_TERRACOTTA
                    TeamColor.RED -> Material.RED_TERRACOTTA
                    TeamColor.YELLOW -> Material.YELLOW_TERRACOTTA
                    else -> Material.WHITE_TERRACOTTA
                }, 16)
            }, Price(Material.IRON_INGOT, 12)))
            items.add(ShopItem("oak_wood_planks", { _, _ ->
                ItemStack(Material.BIRCH_LOG, 16)
            }, Price(Material.GOLD_INGOT, 4)))
            items.add(ShopItem("blast-proof_glass", { player, game ->
                val item = ItemStack(when (game.getTeamOfPlayer(player).color) {
                    TeamColor.BLUE -> Material.BLUE_STAINED_GLASS
                    TeamColor.GREEN -> Material.GREEN_STAINED_GLASS
                    TeamColor.RED -> Material.RED_STAINED_GLASS
                    TeamColor.YELLOW -> Material.YELLOW_STAINED_GLASS
                    else -> Material.GLASS
                }, 4)
                val meta = item.itemMeta
                meta?.setDisplayName("Blast-proof glass")
                item.itemMeta = meta
                item
            }, Price(Material.IRON_INGOT, 12)))
            items.add(ShopItem("end_stone", { _, _ ->
                ItemStack(
                    Material.END_STONE,
                    12
                )
            }, Price(Material.IRON_INGOT, 24)))
            items.add(ShopItem("ladder", { _, _ ->
                ItemStack(
                    Material.LADDER,
                    8
                )
            }, Price(Material.IRON_INGOT, 4)))
            items.add(ShopItem("obsidian", { _, _ ->
                ItemStack(
                    Material.OBSIDIAN,
                    4
                )
            }, Price(Material.EMERALD, 4)))
        }
    }

    override fun getItems() = items
}