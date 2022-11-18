package com.micahhenney.bedwarstweaks.itemshop

import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.screamingsandals.bedwars.api.game.Game

class RangedMenu(player: Player, game: Game) : ShopMenu(player, game, items) {
    companion object {
        private val items = ArrayList<ShopItem>()

        init {
            items.add(addItem("bow", { _, _ -> ItemStack(Material.BOW) }, Price(Material.GOLD_INGOT, 12)))

            items.add(addItem("bow_(power_i)", { _, _ ->
                val item = ItemStack(Material.BOW)
                item.addEnchantment(Enchantment.ARROW_DAMAGE, 1)
                item
             }, Price(Material.GOLD_INGOT, 24)))
            items.add(addItem("bow_(power_i_punch_i)", { _, _ ->
                val item = ItemStack(Material.BOW)
                item.addEnchantment(Enchantment.ARROW_DAMAGE, 1)
                item.addEnchantment(Enchantment.ARROW_KNOCKBACK, 1)
                item
            }, Price(Material.EMERALD, 6)))
            items.add(addItem("arrow", { _, _ -> ItemStack(Material.ARROW, 8) }, Price(Material.GOLD_INGOT, 2)))
        }
        fun hasItem(item: ItemStack, p: Player, g: Game): Boolean {
            for (localItem in items) {
                if (item.type == localItem.itemStack.invoke(p, g).type) return true
            }
            return false
        }
        private fun addItem(name: String, getItemStack: (Player, Game) -> ItemStack, price: Price): ShopItem {
            return ShopItem(name, getItemStack, price, 'R')
        }
    }

    override fun getItems(): List<ShopItem> = items
}