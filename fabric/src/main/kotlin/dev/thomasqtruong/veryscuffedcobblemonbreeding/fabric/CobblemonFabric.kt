package dev.thomasqtruong.veryscuffedcobblemonbreeding.fabric

import dev.thomasqtruong.veryscuffedcobblemonbreeding.fabric.events.UseBlockHandler
import dev.thomasqtruong.veryscuffedcobblemonbreeding.fabric.item.Programs
import dev.thomasqtruong.veryscuffedcobblemonbreeding.fabric.listeners.BattleRegistryListener
import dev.thomasqtruong.veryscuffedcobblemonbreeding.fabric.packets.JumpPacket
import dev.thomasqtruong.veryscuffedcobblemonbreeding.fabric.packets.PokeMountMovePacket
import dev.thomasqtruong.veryscuffedcobblemonbreeding.fabric.packets.handleJumpPacket
import dev.thomasqtruong.veryscuffedcobblemonbreeding.fabric.packets.handlePokeMountMovePacket
import dev.thomasqtruong.veryscuffedcobblemonbreeding.VeryScuffedCobblemonBreeding
import dev.thomasqtruong.veryscuffedcobblemonbreeding.fabric.events.UseEntityHandler
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.event.player.UseBlockCallback
import net.fabricmc.fabric.api.event.player.UseEntityCallback
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking

class CobblemonFabric : ModInitializer {
    override fun onInitialize() {
        System.out.println("Fabric Mod init")
        VeryScuffedCobblemonBreeding.initialize()
        CommandRegistrationCallback.EVENT.register { dispatcher, _, _ ->
            VeryScuffedCobblemonBreeding.registerCommands(dispatcher)
        }

        // Register custom packets
        PayloadTypeRegistry.playC2S().register(JumpPacket.TYPE, JumpPacket.CODEC)
        PayloadTypeRegistry.playC2S().register(PokeMountMovePacket.TYPE, PokeMountMovePacket.CODEC)
        
        ServerPlayNetworking.registerGlobalReceiver(JumpPacket.TYPE) { packet, context ->
            context.server().execute {
                handleJumpPacket(packet, context.player())
            }
        }
        
        ServerPlayNetworking.registerGlobalReceiver(PokeMountMovePacket.TYPE) { packet, context ->
            context.server().execute {
                handlePokeMountMovePacket(packet, context.player())
            }
        }

        Programs.registerItems()
        BattleRegistryListener.initialize()
        UseEntityCallback.EVENT.register(UseEntityHandler())
        UseBlockCallback.EVENT.register(UseBlockHandler())
    }
}
