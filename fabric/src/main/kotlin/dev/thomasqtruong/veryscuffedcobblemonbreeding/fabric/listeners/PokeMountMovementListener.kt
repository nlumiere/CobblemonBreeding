package dev.thomasqtruong.veryscuffedcobblemonbreeding.fabric.listeners

import com.cobblemon.mod.common.api.types.ElementalTypes
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import dev.thomasqtruong.veryscuffedcobblemonbreeding.fabric.packets.JumpPacket
import dev.thomasqtruong.veryscuffedcobblemonbreeding.fabric.packets.PokeMountMovePacket
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.minecraft.client.Minecraft
import net.minecraft.network.chat.Component

class PokeMountMovementListener : ClientModInitializer {
    override fun onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register {
            tick(it)
        }
    }

    fun tick(client: Minecraft) {
        val player = client.player ?: return

        if (player.vehicle == null) {
            return
        }

        if (player.vehicle is PokemonEntity) {
            val pokemonEntity = player.vehicle as PokemonEntity
            val flying = pokemonEntity.pokemon.types.contains(ElementalTypes.FLYING)
            val water = pokemonEntity.pokemon.types.contains(ElementalTypes.WATER)

            val jumpKey = client.options.keyJump.isDown
            val isJumpValid = pokemonEntity.onGround() || flying || (water && pokemonEntity.isInWater)
            if (jumpKey && isJumpValid) {
                JumpPacket.sendToServer()
            }

            if (client.options.keyUp.isDown) {
                PokeMountMovePacket.sendToServer()
            }
        }
    }
}