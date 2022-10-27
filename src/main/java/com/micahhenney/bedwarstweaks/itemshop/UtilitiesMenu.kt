package com.micahhenney.bedwarstweaks.itemshop

import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.screamingsandals.bedwars.api.game.Game

class UtilitiesMenu(player: Player, game: Game) : ShopMenu(player, game, items) {
    companion object {
        private val items = ArrayList<ShopItem>()

        init {
            items.add(ShopItem("golden_apple", { _, _ -> ItemStack(Material.GOLDEN_APPLE) }, Price(Material.GOLD_INGOT, 3)))
            items.add(ShopItem("dream_defender", { _, _ ->
                val item = ItemStack(Material.GHAST_SPAWN_EGG)
                val meta = item.itemMeta
                meta?.setDisplayName("Dream Defender")
                item.itemMeta = meta
                item
            }, Price(Material.IRON_INGOT, 120)))
            items.add(ShopItem("fireball", { _, _ -> ItemStack(Material.FIRE_CHARGE) }, Price(Material.IRON_INGOT, 40)))
            items.add(ShopItem("tnt", { _, _ -> ItemStack(Material.TNT) }, Price(Material.GOLD_INGOT, 8)))
            items.add(ShopItem("ender_pearl", { _, _ -> ItemStack(Material.ENDER_PEARL) }, Price(Material.EMERALD, 4)))
            items.add(ShopItem("water_bucket", { _, _ -> ItemStack(Material.WATER_BUCKET) }, Price(Material.GOLD_INGOT, 6)))
            items.add(ShopItem("bridge_egg", { _, _ ->
                val item = ItemStack(Material.EGG)
                val meta = item.itemMeta
                meta?.setDisplayName("Bridge Egg")
                item.itemMeta = meta
                item
            }, Price(Material.EMERALD, 1)))
            items.add(ShopItem("popup_tower", { _, _ ->
                val item = ItemStack(Material.CHEST)
                val meta = item.itemMeta
                meta?.setDisplayName("Pop-up Tower")
                item.itemMeta = meta
                item
            }, Price(Material.IRON_INGOT, 16)))
        }
    }

    override fun getItems(): List<ShopItem> = items
}