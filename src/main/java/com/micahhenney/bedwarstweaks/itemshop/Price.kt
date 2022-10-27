package com.micahhenney.bedwarstweaks.itemshop

import org.bukkit.ChatColor
import org.bukkit.Material

data class Price(
    val currency: Material,
    val amount: Int
) {
    override fun toString(): String {
        return "$amount " + when (currency) {
            Material.EMERALD -> "Emerald" + if(amount != 1) "s" else ""
            Material.GOLD_INGOT -> "Gold"
            Material.DIAMOND -> "Diamond" + if(amount != 1) "s" else ""
            Material.IRON_INGOT -> "Iron"
            else -> "unknown"
        }
    }
    fun toInGameString(): String {
        return "$amount " + when (currency) {
            Material.EMERALD -> ChatColor.GREEN.toString() + "Emerald" + if(amount != 1) "s" else ""
            Material.GOLD_INGOT -> ChatColor.GOLD.toString() + "Gold"
            Material.DIAMOND -> ChatColor.AQUA.toString() + "Diamond" + if(amount != 1) "s" else ""
            Material.IRON_INGOT -> ChatColor.WHITE.toString() + "Iron"
            else -> "unknown"
        }
    }
}