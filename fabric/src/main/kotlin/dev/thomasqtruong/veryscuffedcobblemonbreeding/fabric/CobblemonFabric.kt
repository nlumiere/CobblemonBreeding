package dev.thomasqtruong.veryscuffedcobblemonbreeding.fabric

import dev.thomasqtruong.veryscuffedcobblemonbreeding.VeryScuffedCobblemonBreeding
import dev.thomasqtruong.veryscuffedcobblemonbreeding.fabric.events.UseEntityHandler
import dev.thomasqtruong.veryscuffedcobblemonbreeding.item.MoreBreedingItems
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.event.player.UseEntityCallback

class CobblemonFabric : ModInitializer {
    override fun onInitialize() {
        System.out.println("Fabric Mod init");
        VeryScuffedCobblemonBreeding.initialize();
        UseEntityCallback.EVENT.register(UseEntityHandler())
        MoreBreedingItems.registerItems()
    }
}
