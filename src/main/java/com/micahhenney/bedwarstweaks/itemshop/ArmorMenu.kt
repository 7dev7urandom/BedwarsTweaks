package com.micahhenney.bedwarstweaks.itemshop

import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.screamingsandals.bedwars.api.game.Game

class ArmorMenu(player: Player, game: Game) : ShopMenu(player, game, items) {
    companion object {
        private val items = ArrayList<ShopItem>()

        init {
            items.add(ShopItem("chainmail_boots", { _, _ ->
                val item = ItemStack(Material.CHAINMAIL_BOOTS)
                val meta = item.itemMeta
                meta?.setDisplayName("Chainmail Armor")
                item.itemMeta = meta
                item
            }, Price(Material.IRON_INGOT, 40)))
            items.add(ShopItem("iron_boots", { _, _ ->
                val item = ItemStack(Material.IRON_BOOTS)
                val meta = item.itemMeta
                meta?.setDisplayName("Iron Armor")
                item.itemMeta = meta
                item
            }, Price(Material.GOLD_INGOT, 12)))
            items.add(ShopItem("diamond_boots", { _, _ ->
                val item = ItemStack(Material.DIAMOND_BOOTS)
                val meta = item.itemMeta
                meta?.setDisplayName("Diamond Armor")
                item.itemMeta = meta
                item
            }, Price(Material.EMERALD, 6)))
        }
    }

    override fun getItems() = items
}