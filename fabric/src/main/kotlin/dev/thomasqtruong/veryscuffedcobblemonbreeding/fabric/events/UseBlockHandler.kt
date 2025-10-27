package dev.thomasqtruong.veryscuffedcobblemonbreeding.fabric.events

import com.cobblemon.mod.common.CobblemonItems
import dev.thomasqtruong.veryscuffedcobblemonbreeding.events.UseProgramOnPC
import dev.thomasqtruong.veryscuffedcobblemonbreeding.fabric.item.Programs
import net.fabricmc.fabric.api.event.player.UseBlockCallback
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Item
import net.minecraft.world.level.Level
import net.minecraft.world.phys.BlockHitResult

class UseBlockHandler : UseBlockCallback {

    private val PROGRAMS = setOf<Item>(Programs.CHECKSPAWN)
    override fun interact(player: Player, world: Level, hand: InteractionHand, hitResult: BlockHitResult?): InteractionResult {
        if (world.isClientSide() || hitResult == null || player !is ServerPlayer) {
            return InteractionResult.PASS
        }

        val heldItem = player.getItemInHand(hand).item

        if (world.getBlockState(hitResult.blockPos).block.asItem() == CobblemonItems.PC && heldItem in PROGRAMS) {
            return UseProgramOnPC.checkspawn(player as ServerPlayer)
        }

        return InteractionResult.PASS
    }

}
