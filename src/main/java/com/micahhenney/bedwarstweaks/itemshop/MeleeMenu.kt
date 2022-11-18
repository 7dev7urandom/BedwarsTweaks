package com.micahhenney.bedwarstweaks.itemshop

import io.github.pronze.sba.SBA
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.screamingsandals.bedwars.api.game.Game

class MeleeMenu(player: Player, game: Game) : ShopMenu(player, game, items) {
    companion object {
        private val items = ArrayList<ShopItem>()

        init {
            items.add(addItem("stone_sword", buildSword(Material.STONE_SWORD), Price(Material.IRON_INGOT, 10)))
            items.add(addItem("iron_sword", buildSword(Material.IRON_SWORD), Price(Material.GOLD_INGOT, 7)))
            items.add(addItem("diamond_sword", buildSword(Material.DIAMOND_SWORD), Price(Material.EMERALD, 4)))
            items.add(addItem("stick_(knockback_i)", { _, _ ->
                val item = ItemStack(
                    Material.STICK
                )
                item.addUnsafeEnchantment(Enchantment.KNOCKBACK, 1)
                item
            }, Price(Material.GOLD_INGOT, 5)))
        }
        fun hasItem(item: ItemStack, p: Player, g: Game): Boolean {
            if(item.type == Material.WOODEN_SWORD) return true
            for (localItem in items) {
                if (item.type == localItem.itemStack.invoke(p, g).type) return true
            }
            return false
        }
        fun applySharpness(p: Player, g: Game, item: ItemStack) {
            val sharpnessLevel = SBA.getInstance().getGameStorage(g).get().getSharpnessLevel(g.getTeamOfPlayer(p)).get()
            if(sharpnessLevel > 0) item.addEnchantment(Enchantment.DAMAGE_ALL, sharpnessLevel)

        }
        fun buildSword(material: Material): (Player, Game) -> ItemStack {
            return { p, g ->
                val itemStack = ItemStack(material)
                applySharpness(p, g, itemStack)
                itemStack
            }
        }
        private fun addItem(name: String, getItemStack: (Player, Game) -> ItemStack, price: Price): ShopItem {
            return ShopItem(name, getItemStack, price, 'M')
        }
    }

    override fun getItems() = items
}