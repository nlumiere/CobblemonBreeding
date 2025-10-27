package dev.thomasqtruong.veryscuffedcobblemonbreeding.fabric.packets

import com.cobblemon.mod.common.api.net.NetworkPacket
import com.cobblemon.mod.common.api.types.ElementalTypes
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import dev.thomasqtruong.veryscuffedcobblemonbreeding.events.PokeMount
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.chat.Component
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.phys.Vec3

class JumpPacket : NetworkPacket<JumpPacket> {
    override val id = ID
    
    override fun encode(buffer: RegistryFriendlyByteBuf) {}
    
    override fun type(): CustomPacketPayload.Type<JumpPacket> = TYPE

    companion object {
        val ID = ResourceLocation.fromNamespaceAndPath("veryscuffedcobblemonbreeding", "jump_packet")
        val TYPE: CustomPacketPayload.Type<JumpPacket> = CustomPacketPayload.Type(ID)
        
        val CODEC: StreamCodec<RegistryFriendlyByteBuf, JumpPacket> = StreamCodec.of(
            { _, _ -> }, // encoder (no data to encode)
            { _ -> JumpPacket() } // decoder
        )
        
        fun decode(buffer: RegistryFriendlyByteBuf) = JumpPacket()
        
        // Client-side: Send packet to server
        fun sendToServer() {
            ClientPlayNetworking.send(JumpPacket())
        }
    }
}

// Server-side handler
fun handleJumpPacket(packet: JumpPacket, player: ServerPlayer) {
    val entity = player.vehicle ?: return
    try {
        val pokemonEntity = entity as? PokemonEntity ?: return
        val flying = pokemonEntity.pokemon.types.contains(ElementalTypes.FLYING)
        val water = pokemonEntity.pokemon.types.contains(ElementalTypes.WATER)
        if (entity.onGround() || flying || (water && entity.isInWater)) {
            val existingVelocity = entity.deltaMovement
            val jumpVelocity = .5 * PokeMount.getStrengthMultiplier(pokemonEntity)
            entity.deltaMovement = Vec3(existingVelocity.x, jumpVelocity, existingVelocity.z)
        }
    } catch (e: Exception) {
        // Silently catch any exceptions
    }
}
