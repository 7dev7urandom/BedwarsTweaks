package com.micahhenney.bedwarstweaks.itemshop

import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Item
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.screamingsandals.bedwars.api.game.Game

class RangedMenu(player: Player, game: Game) : ShopMenu(player, game, items) {
    companion object {
        private val items = ArrayList<ShopItem>()

        init {
            items.add(ShopItem("bow", { _, _ -> ItemStack(Material.BOW) }, Price(Material.GOLD_INGOT, 12)))

            items.add(ShopItem("bow_(power_i)", { _, _ ->
                val item = ItemStack(Material.BOW)
                item.addEnchantment(Enchantment.ARROW_DAMAGE, 1)
                item
             }, Price(Material.GOLD_INGOT, 24)))
            items.add(ShopItem("bow_(power_i_punch_i)", { _, _ ->
                val item = ItemStack(Material.BOW)
                item.addEnchantment(Enchantment.ARROW_DAMAGE, 1)
                item.addEnchantment(Enchantment.ARROW_KNOCKBACK, 1)
                item
            }, Price(Material.EMERALD, 6)))
            items.add(ShopItem("arrow", { _, _ -> ItemStack(Material.ARROW, 8) }, Price(Material.GOLD_INGOT, 2)))
        }
    }

    override fun getItems(): List<ShopItem> = items
}