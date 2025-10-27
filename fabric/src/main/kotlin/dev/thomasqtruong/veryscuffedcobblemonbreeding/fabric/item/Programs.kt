package dev.thomasqtruong.veryscuffedcobblemonbreeding.fabric.item

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup
import net.minecraft.core.Registry
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.CreativeModeTab
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack

object Programs {
    // Register the item first
    val CHECKSPAWN: Item = registerItem("checkspawn", Item(Item.Properties()))
    
    // Create the custom item group
    private val PROGRAMS_TAB: CreativeModeTab = FabricItemGroup.builder()
        .title(Component.translatable("itemGroup.veryscuffedcobblemonbreeding.programs"))
        .icon { ItemStack(CHECKSPAWN) }
        .displayItems { _, entries ->
            entries.accept(CHECKSPAWN)
        }
        .build()
    
    fun registerItems() {
        // Register the custom item group
        Registry.register(
            BuiltInRegistries.CREATIVE_MODE_TAB,
            ResourceLocation.fromNamespaceAndPath("veryscuffedcobblemonbreeding", "programs"),
            PROGRAMS_TAB
        )
    }
    
    private fun registerItem(itemName: String, item: Item): Item {
        return Registry.register(
            BuiltInRegistries.ITEM,
            ResourceLocation.fromNamespaceAndPath("veryscuffedcobblemonbreeding", itemName),
            item
        )
    }
}