package dev.thomasqtruong.veryscuffedcobblemonbreeding.fabric.events

import dev.thomasqtruong.veryscuffedcobblemonbreeding.BreedingInitializer
import net.fabricmc.fabric.api.event.player.UseEntityCallback
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.level.Level
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.phys.EntityHitResult

class UseEntityHandler : UseEntityCallback {

    private var executed = false

    override fun interact(
        player: Player,
        world: Level,
        hand: InteractionHand,
        entity: Entity,
        hitResult: EntityHitResult?
    ): InteractionResult {
        if (entity !is LivingEntity) {
            return InteractionResult.PASS
        }

        // Only handle server-side and non-spectators
        if (world.isClientSide || player.isSpectator) {
            return InteractionResult.PASS
        }

        // Ensure we have a server player and living entity
        if (player !is ServerPlayer) {
            return InteractionResult.PASS
        }

        if (!executed) {
            val response = BreedingInitializer.attemptBreeding(player, hand, entity)
            if (response == InteractionResult.SUCCESS) {
                executed = true
            }
            return response
        }

        executed = false

        return InteractionResult.PASS
    }
}