package com.micahhenney.bedwarstweaks.itemshop

import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.screamingsandals.bedwars.api.game.Game

class ShopItem(
    val name: String,
    val itemStack: (player: Player, game: Game) -> ItemStack,
    private val price: Price?,
    val category: Char? = null
) {
    var priceFunc: ((Player, Game) -> Price?)? = null
    fun getPrice(player: Player, game: Game) = if(price != null) price else priceFunc?.invoke(player, game)
}