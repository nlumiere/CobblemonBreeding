package dev.thomasqtruong.veryscuffedcobblemonbreeding.fabric.packets

import com.cobblemon.mod.common.api.net.NetworkPacket
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import dev.thomasqtruong.veryscuffedcobblemonbreeding.events.PokeMount
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.phys.Vec3

class PokeMountMovePacket : NetworkPacket<PokeMountMovePacket> {
    override val id = ID
    
    override fun encode(buffer: RegistryFriendlyByteBuf) {}
    
    override fun type(): CustomPacketPayload.Type<PokeMountMovePacket> = TYPE

    companion object {
        val ID = ResourceLocation.fromNamespaceAndPath("cobblemonextras", "pokemount_move_packet")
        val TYPE: CustomPacketPayload.Type<PokeMountMovePacket> = CustomPacketPayload.Type(ID)
        
        val CODEC: StreamCodec<RegistryFriendlyByteBuf, PokeMountMovePacket> = StreamCodec.of(
            { _, _ -> }, // encoder (no data to encode)
            { _ -> PokeMountMovePacket() } // decoder
        )
        
        fun decode(buffer: RegistryFriendlyByteBuf) = PokeMountMovePacket()
        
        // Client-side: Send packet to server
        fun sendToServer() {
            ClientPlayNetworking.send(PokeMountMovePacket())
        }
    }
}

// Server-side handler
fun handlePokeMountMovePacket(packet: PokeMountMovePacket, player: ServerPlayer) {
    val entity = player.vehicle ?: return
    try {
        val pokemonEntity = entity as? PokemonEntity ?: return

        pokemonEntity.xRot = player.xRot
        pokemonEntity.yRot = player.yRot
        pokemonEntity.yHeadRot = player.yHeadRot
        val lookVec = player.getViewVector(1.0f)
        val x = lookVec.x
        val z = lookVec.z
        var vector3: Vec3 = Vec3(x, 0.0, z)
        vector3 = vector3.normalize()
        val yVelocity = pokemonEntity.deltaMovement.y
        val existingVelocity = Vec3(pokemonEntity.deltaMovement.x, 0.0, pokemonEntity.deltaMovement.z)
        val velocityStatMultiplier = PokeMount.getSpeedMultiplier(pokemonEntity)
        val newVelocity = (existingVelocity.add(vector3)).normalize().scale(.4).scale(velocityStatMultiplier)
        pokemonEntity.deltaMovement = Vec3(newVelocity.x, yVelocity, newVelocity.z)
    } catch (e: Exception) {
        // Silently catch any exceptions
    }
}
