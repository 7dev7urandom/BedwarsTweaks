package com.micahhenney.bedwarstweaks

import org.bukkit.DyeColor

class ColorDecoder {
    companion object {
        fun getIntFromColor(color: String): Short {
            return when(color) {
                "RED" -> 14
                "YELLOW" -> 4
                "BLUE" -> 11
                "GREEN" -> 13
                else -> -1
            }
        }
        fun getEnumFromColor(color: String): DyeColor {
            return when(color) {
                "RED" -> DyeColor.RED
                "YELLOW" -> DyeColor.YELLOW
                "BLUE" -> DyeColor.BLUE
                "GREEN" -> DyeColor.GREEN
                else -> DyeColor.MAGENTA
            }
        }
    }
}