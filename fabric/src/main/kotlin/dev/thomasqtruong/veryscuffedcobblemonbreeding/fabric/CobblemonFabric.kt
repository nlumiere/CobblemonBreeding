package dev.thomasqtruong.veryscuffedcobblemonbreeding.fabric

import dev.thomasqtruong.veryscuffedcobblemonbreeding.VeryScuffedCobblemonBreeding
import dev.thomasqtruong.veryscuffedcobblemonbreeding.fabric.events.UseEntityHandler
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.event.player.UseEntityCallback

class CobblemonFabric : ModInitializer {
    override fun onInitialize() {
        System.out.println("Fabric Mod init");
        VeryScuffedCobblemonBreeding.initialize();
        CommandRegistrationCallback.EVENT.register { dispatcher, _, _ ->
            VeryScuffedCobblemonBreeding.registerCommands(dispatcher)
        };
        UseEntityCallback.EVENT.register(UseEntityHandler());
    }
}
