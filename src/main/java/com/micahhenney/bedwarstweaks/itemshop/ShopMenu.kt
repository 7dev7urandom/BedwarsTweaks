package com.micahhenney.bedwarstweaks.itemshop

import com.micahhenney.bedwarstweaks.BedwarsListener
import com.micahhenney.bedwarstweaks.BedwarsTweaks
import de.themoep.inventorygui.*
import io.github.pronze.lib.simpleinventories.inventory.Price
import io.github.pronze.sba.SBA
import io.github.pronze.sba.utils.ShopUtil
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.scheduler.BukkitRunnable
import org.screamingsandals.bedwars.api.game.Game

abstract class ShopMenu(protected val player: Player, protected  val game: Game, items: List<ShopItem>? = null) {
    init {
        if (items != null) {
            items.forEach {
                allItems[it.name] = it
            }
        }
    }
    companion object {
        private val allItems: HashMap<String, ShopItem> = HashMap()
//        private val reverseLookup: HashMap<Material, String> = HashMap()
//        private val prices: HashMap<Material, Price> = HashMap()
//        fun getPriceOfItem(itemStack: ItemStack): Price {
//            return prices[itemStack.type] ?: Price.of(10000, "emerald")
//        }
        fun getItemFromName(name: String) = allItems[name]
        fun generateItemShopGui(p: Player, g: Game): InventoryGui {
            val categories = "fbmatrpu"
            val shape = arrayOf(
                "$categories ", // Favorites, blocks, melee, armor, tools, ranged, potions, utility
                "012345678",    // Glass panes
                " zzzzzzz ",    // Items
                " zzzzzzz ",
                " zzzzzzz ",
                "        q"     // Blank line
            )
            val gui = InventoryGui(BedwarsTweaks.instance, p, "Item Shop", shape)

            var currentSelected = 0
            for(i in 0..8) {
                gui.addElement(
                    DynamicGuiElement(i.toString()[0]) { _ ->
                        StaticGuiElement(
                            i.toString()[0],
                            if(currentSelected == i) ItemStack(Material.GREEN_STAINED_GLASS_PANE, 1) else ItemStack(Material.GRAY_STAINED_GLASS_PANE, 1),
                            { click ->
                                click.whoClicked.setItemOnCursor(ItemStack(Material.AIR))
                                true
                            },
                            ""
                        )
                    })
            }
            val categoryMenus = arrayOf(PlayerFavoritesMenu(p, g), BlocksMenu(p, g), MeleeMenu(p, g), ArmorMenu(p, g), ToolsMenu(p, g), RangedMenu(p, g), PotionsMenu(p, g), UtilitiesMenu(p, g))
            val items = GuiElementGroup('z')
            fun purchaseItem(it: ShopItem): GuiElement.Action {
                return GuiElement.Action { click ->
                    click.whoClicked.setItemOnCursor(ItemStack(Material.AIR))
                    if(it.getPrice(p, g) == null) return@Action true // Nonbuyable item
                    if(BedwarsListener.chargePrice(p, it.getPrice(p, g)!!)) {
                        val itemStack = it.itemStack(p, g)
                        when {
                            itemStack.type.name.endsWith("SWORD", ignoreCase = true) -> {
//                            val sharpnessLevel = SBA.getInstance().getGameStorage(g).get().getSharpnessLevel(g.getTeamOfPlayer(p)).get()
//                            if(sharpnessLevel > 0) itemStack.addEnchantment(Enchantment.DAMAGE_ALL, sharpnessLevel)
                                MeleeMenu.applySharpness(p, g, itemStack)
                                var foundWoodSword = false
                                for ((i, v) in p.inventory.withIndex()) {
                                    if(v == null) continue
                                    if(v.type == Material.WOODEN_SWORD) {
                                        p.inventory.setItem(i, itemStack)
                                        foundWoodSword = true
                                        break
                                    }
                                }
                                if(!foundWoodSword) p.inventory.addItem(itemStack)
                            }
                            itemStack.type.name.endsWith("boots", ignoreCase = true) ||
                                    itemStack.type.name.endsWith("leggings", ignoreCase = true) ||
                                    itemStack.type.name.endsWith("chestplate", ignoreCase = true) ||
                                    itemStack.type.name.endsWith("helmet", ignoreCase = true) ->
                                ShopUtil.buyArmor(p, itemStack.type, SBA.getInstance().getGameStorage(g).get(), g)
                            itemStack.type.name.endsWith("axe", ignoreCase = true) -> { // pickaxe and axe
                                val efficiencyLevel = SBA.getInstance().getGameStorage(g).get().getEfficiencyLevel(g.getTeamOfPlayer(p)).get()
                                if (efficiencyLevel > 0) itemStack.addEnchantment(Enchantment.DIG_SPEED, efficiencyLevel)
                                if(itemStack.type.name.contains("wood", ignoreCase = true))
                                    p.inventory.addItem(itemStack)
                                else {
                                    val name = if (itemStack.type.name.contains("pickaxe", ignoreCase = true)) "pickaxe" else "_axe"
                                    var found = false
                                    for ((i, v) in p.inventory.withIndex()) {
                                        if(v == null) continue
                                        if(v.type.name.contains(name, ignoreCase = true)) {
                                            p.inventory.setItem(i, itemStack)
                                            found = true
                                            break
                                        }
                                    }
                                    if(!found) BedwarsTweaks.instance?.logger?.severe("Tool bought from shop is not wooden, but the player did not have a wooden version")
                                }
                            }
                            else -> p.inventory.addItem(itemStack)
                        }
                        p.playSound(p.location, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f)
                    } else p.sendMessage(ChatColor.RED.toString() + "You don't have enough resources to buy that item")
                    object : BukkitRunnable() {
                        override fun run() {
                            click.gui.draw()
                            click.whoClicked.openInventory.setItem(click.slot, click.gui.getElement(click.slot).getItem(p, click.slot))
                        }
                    }.runTaskLater(BedwarsTweaks.instance!!, 1)
                    true

                }

            }
            fun createElementFromShopItem(shopItem: ShopItem): DynamicGuiElement {
                return DynamicGuiElement('e') { _ ->
                    val item = shopItem.itemStack(p, g)
                    val meta = item.itemMeta
                    val price = shopItem.getPrice(p, g)
                    if(meta == null || price == null) {
//                        BedwarsTweaks.instance?.logger?.info("Meta was null for item $item")
                    } else {
                        var lore = meta.lore
                        if(meta.hasLore()) {
                            lore!!.add(ChatColor.GRAY.toString() + "Price: " + price.toInGameString())
                        } else
                            lore = arrayListOf(ChatColor.GRAY.toString() + "Price: " + price.toInGameString())
                        lore.add(if(BedwarsListener.canAfford(p, price)) ChatColor.YELLOW.toString() + "Click to purchase!" else ChatColor.RED.toString() + "You don't have enough resources!")
                        meta.lore = lore
                        item.itemMeta = meta
                    }
                    StaticGuiElement('e', item, purchaseItem(shopItem))
                }
            }
            val categoryClickAction = GuiElement.Action { click ->
                currentSelected = categories.indexOf(click.element.slotChar)
                val currentCategory = categoryMenus[currentSelected]
                items.clearElements()
                currentCategory.getItems().forEach {
                    items.addElement(createElementFromShopItem(it))
                }
                click.gui.draw()
                click.whoClicked.setItemOnCursor(ItemStack(Material.AIR))
                true
            }

            gui.addElement(StaticGuiElement('f', ItemStack(Material.NETHER_STAR), 1, categoryClickAction, "Favorites"))
            gui.addElement(StaticGuiElement('b', ItemStack(Material.CLAY), 1, categoryClickAction, "Blocks"))
            gui.addElement(StaticGuiElement('m', ItemStack(Material.GOLDEN_SWORD), 1, categoryClickAction, "Melee"))
            gui.addElement(StaticGuiElement('a', ItemStack(Material.CHAINMAIL_BOOTS), 1, categoryClickAction, "Armor"))
            gui.addElement(StaticGuiElement('t', ItemStack(Material.STONE_PICKAXE), 1, categoryClickAction, "Tools"))
            gui.addElement(StaticGuiElement('r', ItemStack(Material.BOW), 1, categoryClickAction, "Ranged"))
            gui.addElement(StaticGuiElement('p', ItemStack(Material.BREWING_STAND), 1, categoryClickAction, "Potions"))
            gui.addElement(StaticGuiElement('u', ItemStack(Material.TNT), 1, categoryClickAction, "Utility"))

            // Populate items with favorites
            categoryMenus[0].getItems().forEach { items.addElement(createElementFromShopItem(it)) }
//        for(c in 1..7) {
//            for(r in 1..3) {
//                items.addElement(StaticGuiElement('e', ItemStack(Material.REDSTONE), 1, { click ->
//                    click.whoClicked.sendMessage("You clicked a redstone!");
//                    click.whoClicked.itemOnCursor = ItemStack(Material.AIR)
//                    true
//                }, "Some item"))
//            }
//        }
            gui.addElement(items)
            val infoItem = ItemStack(Material.BLAZE_POWDER)
            val infoMeta = infoItem.itemMeta
            infoMeta?.setDisplayName("Type /hypixelimport to import your favorites from Hypixel")
            infoItem.itemMeta = infoMeta
            gui.addElement(StaticGuiElement('q', infoItem, { _ -> true }))
            gui.show(p)
            return gui
        }

    }
    abstract fun getItems(): List<ShopItem>
}