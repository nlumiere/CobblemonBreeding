package dev.thomasqtruong.veryscuffedcobblemonbreeding.fabric.events

import dev.thomasqtruong.veryscuffedcobblemonbreeding.BreedingInitializer
import dev.thomasqtruong.veryscuffedcobblemonbreeding.VeryScuffedCobblemonBreeding
import net.fabricmc.fabric.api.event.player.UseEntityCallback
import net.minecraft.entity.Entity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.world.World
import net.minecraft.util.hit.EntityHitResult

class UseEntityHandler : UseEntityCallback {

    override fun interact(
        player: PlayerEntity?,
        world: World?,
        hand: Hand?,
        entity: Entity?,
        hitResult: EntityHitResult?
    ): ActionResult {
        if (player == null || entity == null || hand == null){
            return ActionResult.PASS
        }

        if (world == null || world.isClient()) {
            return ActionResult.SUCCESS
        }

        return BreedingInitializer.attemptBreeding(player, hand, entity)
    }
}