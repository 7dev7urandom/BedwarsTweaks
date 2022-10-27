package com.micahhenney.bedwarstweaks.itemshop

import com.micahhenney.bedwarstweaks.database.PlayerDatabase
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.screamingsandals.bedwars.api.game.Game

class PlayerFavoritesMenu(player: Player, game: Game) : ShopMenu(player, game) {

    companion object {
        val BLANK_ITEM = ItemStack(Material.RED_STAINED_GLASS_PANE, 1)
    }
    override fun getItems(): List<ShopItem> {
        return PlayerDatabase.getPlayerInfo(player)?.quickBuy?.split(",")?.map { locatorFunction ->
            var item = getItemFromName(locatorFunction)
            if (item == null) item = ShopItem(locatorFunction, { _, _ ->
                val itemStack = BLANK_ITEM.clone()
                val meta = itemStack.itemMeta
                meta?.setDisplayName("Blank Quick-Buy Slot")
                meta?.lore = arrayListOf(locatorFunction)
                itemStack.itemMeta = meta
                itemStack
            }, null)
            item
        }?: defaultItems()
    }
    private fun defaultItems(): List<ShopItem> {
        return arrayOf(
            "wool", "stone_sword", "chainmail_boots", "null", "bow", "jump_v_potion_(45_seconds)", "tnt",
            "oak_wood_planks", "iron_sword", "iron_boots", "shears", "arrow", "invisibility_potion_(30_seconds)", "water_bucket",
            "null", "null", "null", "null", "null", "null", "null")
                .map { locatorFunction -> getItemFromName(locatorFunction)?: ShopItem(locatorFunction, { _, _ ->
                    val itemStack = BLANK_ITEM.clone()
                    val meta = itemStack.itemMeta
                    meta?.setDisplayName("Blank Quick-Buy Slot")
                    if(locatorFunction != "null") meta?.lore = arrayListOf(locatorFunction)
                    itemStack.itemMeta = meta
                    itemStack
                }, null) }
    }

}