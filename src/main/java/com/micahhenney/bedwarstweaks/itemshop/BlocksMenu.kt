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

        fun hasItem(item: ItemStack, p: Player, g: Game): Boolean {
            if(item.type == Material.END_STONE
                || item.type == Material.BIRCH_LOG
                || item.type.name.contains("wool", ignoreCase = true)
                || item.type.name.contains("glass", ignoreCase = true)
                || item.type.name.contains("terracotta", ignoreCase = true)
                || item.type == Material.OBSIDIAN
                || item.type == Material.LADDER) {
                return true
            }
            for (localItem in items) {
                if (item.type == localItem.itemStack.invoke(p, g).type) return true
            }
            return false
        }
        init {
            items.add(addItem("wool", { player, game ->
                ItemStack(when (game.getTeamOfPlayer(player).color) {
                    TeamColor.LIGHT_BLUE -> Material.BLUE_WOOL
                    TeamColor.LIME -> Material.LIME_WOOL
                    TeamColor.RED -> Material.RED_WOOL
                    TeamColor.YELLOW -> Material.YELLOW_WOOL
                    else -> {
                        BedwarsTweaks.instance?.logger?.info("Invalid team color " + game.getTeamOfPlayer(player).color)
                        Material.WHITE_WOOL
                    }
                }, 16)
            }, Price(Material.IRON_INGOT, 4)))
            items.add(addItem("hard_clay", { player, game ->
                ItemStack(when (game.getTeamOfPlayer(player).color) {
                    TeamColor.LIGHT_BLUE -> Material.BLUE_TERRACOTTA
                    TeamColor.LIME -> Material.LIME_TERRACOTTA
                    TeamColor.RED -> Material.RED_TERRACOTTA
                    TeamColor.YELLOW -> Material.YELLOW_TERRACOTTA
                    else -> Material.WHITE_TERRACOTTA
                }, 16)
            }, Price(Material.IRON_INGOT, 12)))
            items.add(addItem("oak_wood_planks", { _, _ ->
                ItemStack(Material.BIRCH_LOG, 16)
            }, Price(Material.GOLD_INGOT, 4)))
            items.add(addItem("blast-proof_glass", { player, game ->
                val item = ItemStack(when (game.getTeamOfPlayer(player).color) {
                    TeamColor.LIGHT_BLUE -> Material.BLUE_STAINED_GLASS
                    TeamColor.LIME -> Material.LIME_STAINED_GLASS
                    TeamColor.RED -> Material.RED_STAINED_GLASS
                    TeamColor.YELLOW -> Material.YELLOW_STAINED_GLASS
                    else -> Material.GLASS
                }, 4)
                val meta = item.itemMeta
                meta?.setDisplayName("Blast-proof glass")
                item.itemMeta = meta
                item
            }, Price(Material.IRON_INGOT, 12)))
            items.add(addItem("end_stone", { _, _ ->
                ItemStack(
                    Material.END_STONE,
                    12
                )
            }, Price(Material.IRON_INGOT, 24)))
            items.add(addItem("ladder", { _, _ ->
                ItemStack(
                    Material.LADDER,
                    8
                )
            }, Price(Material.IRON_INGOT, 4)))
            items.add(addItem("obsidian", { _, _ ->
                ItemStack(
                    Material.OBSIDIAN,
                    4
                )
            }, Price(Material.EMERALD, 4)))
        }
        private fun addItem(name: String, getItemStack: (Player, Game) -> ItemStack, price: Price): ShopItem {
            return ShopItem(name, getItemStack, price, 'B')
        }
    }

    override fun getItems() = items
}