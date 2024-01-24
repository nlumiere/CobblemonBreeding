package dev.thomasqtruong.veryscuffedcobblemonbreeding.fabric.events

import com.cobblemon.mod.common.util.isServerSide
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

    private var executed = false

    override fun interact(
        player: PlayerEntity?,
        world: World?,
        hand: Hand?,
        entity: Entity?,
        hitResult: EntityHitResult?
    ): ActionResult {
        if (player == null || player.isSpectator || entity == null || hand == null){
            return ActionResult.PASS
        }

        if (world == null || world.isClient()) {
            return ActionResult.PASS
        }

        if (executed) {
            executed = false
            return ActionResult.PASS
        }

        val response = BreedingInitializer.attemptBreeding(player, hand, entity)
        if (response == ActionResult.SUCCESS) {
            executed = true
        }
        return response
    }
}