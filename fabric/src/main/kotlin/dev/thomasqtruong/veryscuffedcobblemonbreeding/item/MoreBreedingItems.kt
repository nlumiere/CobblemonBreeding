package dev.thomasqtruong.veryscuffedcobblemonbreeding.item

import com.cobblemon.mod.common.item.group.CobblemonItemGroups
import net.fabricmc.fabric.api.item.v1.FabricItemSettings
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroupEntries
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents
import net.minecraft.item.Item
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.util.Identifier

class MoreBreedingItems {
    companion object {
        val EVERSTONE: Item = registerItem("everstone", Item(FabricItemSettings()))

        private fun addToCreative(entries: FabricItemGroupEntries) {
            entries.add(EVERSTONE)
        }

        fun registerItems() {
            ItemGroupEvents.modifyEntriesEvent(CobblemonItemGroups.HELD_ITEMS_KEY).register(MoreBreedingItems::addToCreative)
        }

        fun registerItem(itemName: String, item: Item): Item {
            return Registry.register(Registries.ITEM, Identifier("veryscuffedcobblemonbreeding", itemName), item)
        }
    }
}