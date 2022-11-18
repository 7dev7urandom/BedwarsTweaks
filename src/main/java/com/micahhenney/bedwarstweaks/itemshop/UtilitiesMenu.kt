package com.micahhenney.bedwarstweaks.itemshop

import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.screamingsandals.bedwars.api.game.Game

class UtilitiesMenu(player: Player, game: Game) : ShopMenu(player, game, items) {
    companion object {
        private val items = ArrayList<ShopItem>()

        init {
            items.add(addItem("golden_apple", { _, _ -> ItemStack(Material.GOLDEN_APPLE) }, Price(Material.GOLD_INGOT, 3)))
            items.add(addItem("dream_defender", { _, _ ->
                val item = ItemStack(Material.GHAST_SPAWN_EGG)
                val meta = item.itemMeta
                meta?.setDisplayName("Dream Defender")
                item.itemMeta = meta
                item
            }, Price(Material.IRON_INGOT, 120)))
            items.add(addItem("fireball", { _, _ -> ItemStack(Material.FIRE_CHARGE) }, Price(Material.IRON_INGOT, 40)))
            items.add(addItem("tnt", { _, _ -> ItemStack(Material.TNT) }, Price(Material.GOLD_INGOT, 8)))
            items.add(addItem("ender_pearl", { _, _ -> ItemStack(Material.ENDER_PEARL) }, Price(Material.EMERALD, 4)))
            items.add(addItem("water_bucket", { _, _ -> ItemStack(Material.WATER_BUCKET) }, Price(Material.GOLD_INGOT, 6)))
            items.add(addItem("bridge_egg", { _, _ ->
                val item = ItemStack(Material.EGG)
                val meta = item.itemMeta
                meta?.setDisplayName("Bridge Egg")
                item.itemMeta = meta
                item
            }, Price(Material.EMERALD, 1)))
            items.add(addItem("popup_tower", { _, _ ->
                val item = ItemStack(Material.CHEST)
                val meta = item.itemMeta
                meta?.setDisplayName("Pop-up Tower")
                item.itemMeta = meta
                item
            }, Price(Material.IRON_INGOT, 16)))
            items.add(addItem("magic_milk", { _, _ ->
                val item = ItemStack(Material.MILK_BUCKET)
                val meta = item.itemMeta
                meta?.setDisplayName("Magic Milk")
                item.itemMeta = meta
                item
            }, Price(Material.GOLD_INGOT, 4)))
        }
        fun hasItem(item: ItemStack, p: Player, g: Game): Boolean {
            for (localItem in items) {
                if (item.type == localItem.itemStack.invoke(p, g).type) return true
            }
            return false
        }
        private fun addItem(name: String, getItemStack: (Player, Game) -> ItemStack, price: Price): ShopItem {
            return ShopItem(name, getItemStack, price, 'U')
        }
    }

    override fun getItems(): List<ShopItem> = items
}