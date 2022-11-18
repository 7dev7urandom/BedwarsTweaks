package com.micahhenney.bedwarstweaks.itemshop

import com.micahhenney.bedwarstweaks.BedwarsListener
import com.micahhenney.bedwarstweaks.BedwarsTweaks
import com.micahhenney.bedwarstweaks.database.PlayerDatabase
import com.micahhenney.bedwarstweaks.database.PlayerInfo
import de.themoep.inventorygui.*
import io.github.pronze.sba.SBA
import io.github.pronze.sba.utils.ShopUtil
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.scheduler.BukkitRunnable
import org.screamingsandals.bedwars.api.game.Game

abstract class ShopMenu(protected val player: Player, protected  val game: Game, items: List<ShopItem>? = null) {
    init {
        items?.forEach {
            allItems[it.name] = it
        }
    }
    companion object {
        val hotbarManagerMap = HashMap<Char, ItemStack>()
        private val allItems: HashMap<String, ShopItem> = HashMap()
//        private val reverseLookup: HashMap<Material, String> = HashMap()
//        private val prices: HashMap<Material, Price> = HashMap()
//        fun getPriceOfItem(itemStack: ItemStack): Price {
//            return prices[itemStack.type] ?: Price.of(10000, "emerald")
//        }
        fun findSlotForType(player: Player, game: Game, type: Char, material: Material): Int? {
            val favSlots = PlayerDatabase.getPlayerInfo(player)?.hotbarManager?: "xxxxxxxxx"

            for(slot in 0..8) {
                if(player.inventory.getItem(slot)?.type == material) return null
                if(favSlots[slot] == type) {
                    val item = player.inventory.getItem(slot) ?: return slot
                    if(!itemIsType(item, player, game, type)) return slot
                }
            }
            for(slot in 0..8) {
                if((player.inventory.getItem(slot)?: return slot).type == Material.AIR) return slot
            }
            return null
        }
        fun itemIsType(item: ItemStack, player: Player, game: Game, type: Char): Boolean {
            return when (type) {
                'B' -> BlocksMenu.hasItem(item, player, game)
                'M' -> MeleeMenu.hasItem(item, player, game)
                'T' -> ToolsMenu.hasItem(item, player, game)
                'R' -> RangedMenu.hasItem(item, player, game)
                'U' -> UtilitiesMenu.hasItem(item, player, game)
                'P' -> PotionsMenu.hasItem(item, player, game)
                else -> false
            }
        }
        fun addItemToCorrectHotbarSlot(player: Player, game: Game, item: ItemStack, type: Char) {
            for (slot in 0..8) {
                if(player.inventory.getItem(slot)?.type == item.type) {
                    player.inventory.addItem(item)
                    return
                }
            }
            val slot = findSlotForType(player, game, type, item.type)
            if(slot == null) player.inventory.addItem(item)
            else if (player.inventory.getItem(slot) == null || player.inventory.getItem(slot)!!.type == Material.AIR) player.inventory.setItem(slot, item)
            else if(itemIsType(player.inventory.getItem(slot)!!, player, game, type)) {
                // TODO: Doesn't overflow into another correct slot
                player.inventory.addItem(item)
            } else {
                val tmpItem = player.inventory.getItem(slot)
                player.inventory.setItem(slot, item)
                player.inventory.addItem(tmpItem)
            }
        }

        init {
            fun getItemStack(material: Material, name: String): ItemStack {
                val item = ItemStack(material)
                val meta = item.itemMeta
                meta?.setDisplayName(name)
                item.itemMeta = meta
                return item
            }
            hotbarManagerMap['B'] = getItemStack(Material.GRAY_TERRACOTTA, "Blocks")
            hotbarManagerMap['M'] = getItemStack(Material.GOLDEN_SWORD, "Melee")
            hotbarManagerMap['T'] = getItemStack(Material.IRON_PICKAXE, "Tools")
            hotbarManagerMap['R'] = getItemStack(Material.BOW, "Ranged")
            hotbarManagerMap['P'] = getItemStack(Material.BREWING_STAND, "Potions")
            hotbarManagerMap['U'] = getItemStack(Material.TNT, "Utility")
            hotbarManagerMap['x'] = getItemStack(Material.GRAY_STAINED_GLASS_PANE, "Blank")
        }

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
                    if(BedwarsListener.canAfford(p, it.getPrice(p, g)!!)) {
                        val itemStack = it.itemStack(p, g)
                        when {
                            itemStack.type.name.endsWith("SWORD", ignoreCase = true) -> {
                                BedwarsListener.chargePrice(p, it.getPrice(p, g)!!)
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
                                if(!foundWoodSword) addItemToCorrectHotbarSlot(p, g, itemStack, 'M')
                            }
                            itemStack.type.name.endsWith("boots", ignoreCase = true) ||
                                    itemStack.type.name.endsWith("leggings", ignoreCase = true) ||
                                    itemStack.type.name.endsWith("chestplate", ignoreCase = true) ||
                                    itemStack.type.name.endsWith("helmet", ignoreCase = true) ->
                                if(ShopUtil.buyArmor(p, itemStack.type, SBA.getInstance().getGameStorage(g).get(), g)) BedwarsListener.chargePrice(p, it.getPrice(p, g)!!)
                            itemStack.type.name.endsWith("axe", ignoreCase = true) -> { // pickaxe and axe
                                BedwarsListener.chargePrice(p, it.getPrice(p, g)!!)
                                val efficiencyLevel = SBA.getInstance().getGameStorage(g).get().getEfficiencyLevel(g.getTeamOfPlayer(p)).get()
                                if (efficiencyLevel > 0) itemStack.addEnchantment(Enchantment.DIG_SPEED, efficiencyLevel)
                                if(itemStack.type.name.contains("wood", ignoreCase = true))
                                    addItemToCorrectHotbarSlot(p, g, itemStack, 'T')
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
                            else -> {
                                BedwarsListener.chargePrice(p, it.getPrice(p, g)!!)
                                if(it.category != null) addItemToCorrectHotbarSlot(p, g, itemStack, it.category)
                                else p.inventory.addItem(itemStack)
                            }
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
            infoMeta?.setDisplayName("Hotbar Manager")
            infoMeta?.lore = mutableListOf("", ChatColor.GRAY.toString() + "Set default hotbar slots for your items", ChatColor.GRAY.toString() + "Type /hypixelimport to import your favorites from Hypixel")
            infoItem.itemMeta = infoMeta
            gui.addElement(StaticGuiElement('q', infoItem, { click ->
                click.whoClicked.setItemOnCursor(ItemStack(Material.AIR))
                gui.close()
                val hotbarCategories = " BMTRPUx "
                val alpha = "abcdefghi"
                val hotbarManager = InventoryGui(BedwarsTweaks.instance, click.whoClicked, "Hotbar Manager", arrayOf(
                    "        q",
                    hotbarCategories,
                    alpha
                ))
                var selectedCategory = 0
                val currentData = (PlayerDatabase.getPlayerInfo(p)?.hotbarManager?: "xxxxxxxxx")
                    .toCharArray()
                    .map { c -> if(c == ' ') 'x' else c }
                    .toCharArray()
                val importButton = ItemStack(Material.BLAZE_POWDER)

                hotbarManager.addElement(StaticGuiElement('q', importButton, { _ ->
                    PlayerDatabase.importHypixel(p)
                    hotbarManager.close()
                    true
                }, "Import from Hypixel", ChatColor.GRAY.toString() + "Import your QuickBuy and HotbarManager from Hypixel"))
                for(en in hotbarManagerMap) {
                    hotbarManager.addElement(DynamicGuiElement(en.key) { _ ->
                        val itemStack = en.value.clone()

                        if(hotbarCategories[selectedCategory] == en.key) {
                            val itemMeta = itemStack.itemMeta
                            itemMeta?.itemFlags?.add(ItemFlag.HIDE_ENCHANTS)
                            itemMeta?.lore = mutableListOf("", ChatColor.GREEN.toString() + "SELECTED")
                            itemStack.itemMeta = itemMeta
                            itemStack.addUnsafeEnchantment(Enchantment.ARROW_INFINITE, 1)
                        }
                        StaticGuiElement(en.key, itemStack, {
                            selectedCategory = hotbarCategories.indexOf(en.key)
                            hotbarManager.draw()
                            click.whoClicked.setItemOnCursor(ItemStack(Material.AIR))
                            true
                        })
                    })
                }
                for(i in 0..8) {
                    hotbarManager.addElement(DynamicGuiElement(alpha[i]) { _ ->
                        StaticGuiElement(alpha[i], hotbarManagerMap[currentData[i]], {
                            val tmp = hotbarCategories[selectedCategory]
                            if(tmp != ' ') currentData[i] = tmp
                            click.whoClicked.setItemOnCursor(ItemStack(Material.AIR))
                            hotbarManager.draw()
                            true
                        })
                    })
                }
                hotbarManager.setCloseAction {
                    val info = PlayerDatabase.getPlayerInfo(p)?: PlayerInfo(p.uniqueId, "", "")
                    info.hotbarManager = currentData.joinToString("")
                    PlayerDatabase.savePlayerInfo(info)
                    p.sendMessage(ChatColor.GREEN.toString() + "Saving your hotbar information")
                    false
                }
                hotbarManager.show(p)
                true
            }))
            gui.show(p)
            return gui
        }

    }
    abstract fun getItems(): List<ShopItem>
}