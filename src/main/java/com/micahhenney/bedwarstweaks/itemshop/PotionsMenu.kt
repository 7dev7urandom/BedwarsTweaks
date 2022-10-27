package com.micahhenney.bedwarstweaks.itemshop

import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.PotionMeta
import org.bukkit.potion.PotionData
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.potion.PotionType
import org.screamingsandals.bedwars.api.game.Game

class PotionsMenu(player: Player, game: Game) : ShopMenu(player, game, items) {
    companion object {
        private val items = ArrayList<ShopItem>()

        init {
            items.add(ShopItem("jump_v_potion_(45_seconds)", { _, _ ->
                val item = ItemStack(Material.POTION)
                val meta = (item.itemMeta as PotionMeta)
                meta.basePotionData = PotionData(PotionType.JUMP)
                meta.addCustomEffect(PotionEffect(PotionEffectType.JUMP, 900, 4, false, false), true)
//                meta.color = PotionEffectType.JUMP.color
                meta.setDisplayName("Jump V Potion (45 seconds)")
                item.itemMeta = meta
                item
            }, Price(Material.EMERALD, 1)))
            items.add(ShopItem("speed_ii_potion_(45_seconds)", { _, _ ->
                val item = ItemStack(Material.POTION)
                val meta = (item.itemMeta as PotionMeta)
                meta.basePotionData = PotionData(PotionType.SPEED)
                meta.addCustomEffect(PotionEffect(PotionEffectType.SPEED, 900, 1, false, false), true)
//                meta.color = PotionEffectType.SPEED.color
                meta.setDisplayName("Speed II Potion (45 seconds)")
                item.itemMeta = meta
                item
            }, Price(Material.EMERALD, 1)))
            items.add(ShopItem("invisibility_potion_(30_seconds)", { _, _ ->
                val item = ItemStack(Material.POTION)
                val meta = (item.itemMeta as PotionMeta)
                meta.basePotionData = PotionData(PotionType.INVISIBILITY)
                meta.addCustomEffect(PotionEffect(PotionEffectType.INVISIBILITY, 600, 0, false, false), true)
//                meta.color = PotionEffectType.INVISIBILITY.color
                meta.setDisplayName("Invisibility Potion (30 seconds)")
                item.itemMeta = meta
                item
            }, Price(Material.EMERALD, 2)))
        }
    }

    override fun getItems(): List<ShopItem> = items
}