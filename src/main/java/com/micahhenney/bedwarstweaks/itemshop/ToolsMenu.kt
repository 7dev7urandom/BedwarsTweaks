package com.micahhenney.bedwarstweaks.itemshop

import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.screamingsandals.bedwars.api.game.Game

class ToolsMenu(player: Player, game: Game) : ShopMenu(player, game, items) {
    companion object {
        private val items = ArrayList<ShopItem>()

        init {
            items.add(addItem("wooden_pickaxe", { player, game ->
                when {
                    player.inventory.contains(Material.WOODEN_PICKAXE) -> ItemStack(Material.IRON_PICKAXE)
                    player.inventory.contains(Material.IRON_PICKAXE) -> ItemStack(Material.GOLDEN_PICKAXE)
                    player.inventory.contains(Material.GOLDEN_PICKAXE) -> ItemStack(Material.DIAMOND_PICKAXE)
                    else -> ItemStack(Material.WOODEN_PICKAXE)
                }
            }, null))
            items[items.size - 1].priceFunc = { player: Player, game: Game ->
                var pick: ItemStack? = null
                for(i in player.inventory) {
                    if (i == null) continue
                    if(!i.type.name.contains("pickaxe", ignoreCase = true)) continue
                    pick = i
                    break
                }
                when(pick) {
                    null -> Price(Material.IRON_INGOT, 10)
                    else -> when(pick.type) {
                        Material.WOODEN_PICKAXE -> Price(Material.IRON_INGOT, 10)
                        Material.IRON_PICKAXE -> Price(Material.GOLD_INGOT, 3)
                        Material.GOLDEN_PICKAXE -> Price(Material.GOLD_INGOT, 6)
                        else -> null
                    }
                }
            }
            items.add(addItem("wooden_axe", { player, game ->
                when {
                    player.inventory.contains(Material.WOODEN_AXE) -> ItemStack(Material.STONE_AXE)
                    player.inventory.contains(Material.STONE_AXE) -> ItemStack(Material.IRON_AXE)
                    player.inventory.contains(Material.IRON_AXE) -> ItemStack(Material.DIAMOND_AXE)
                    player.inventory.contains(Material.DIAMOND_AXE) -> ItemStack(Material.DIAMOND_AXE)
                    else -> ItemStack(Material.WOODEN_AXE)
                }
            }, null))
            items[items.size - 1].priceFunc = { player: Player, game: Game ->
                var pick: ItemStack? = null
                for(i in player.inventory) {
                    if (i == null) continue
                    if(!i.type.name.contains("_axe", ignoreCase = true)) continue
                    pick = i
                    break
                }
                when(pick) {
                    null -> Price(Material.IRON_INGOT, 10)
                    else -> when(pick!!.type) {
                        Material.WOODEN_AXE -> Price(Material.IRON_INGOT, 10)
                        Material.STONE_AXE -> Price(Material.GOLD_INGOT, 3)
                        Material.IRON_AXE -> Price(Material.GOLD_INGOT, 6)
                        else -> null
                    }
                }
            }
            items.add(addItem("shears", { _, _ -> ItemStack(Material.SHEARS) }, Price(Material.IRON_INGOT, 20)))

        }
        fun hasItem(item: ItemStack, p: Player, g: Game): Boolean {
            return item.type.name.contains("axe", ignoreCase = true) || item.type == Material.SHEARS
        }
        private fun addItem(name: String, getItemStack: (Player, Game) -> ItemStack, price: Price?): ShopItem {
            return ShopItem(name, getItemStack, price, 'T')
        }
    }

    override fun getItems() = items
}