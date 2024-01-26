package dev.thomasqtruong.veryscuffedcobblemonbreeding.forge.events

import dev.thomasqtruong.veryscuffedcobblemonbreeding.BreedingInitializer
import net.minecraft.util.ActionResult
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.event.entity.player.PlayerInteractEvent.EntityInteract
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.common.Mod


object ForgeEventHandler {
    fun register() {
        MinecraftForge.EVENT_BUS.register(this)
    }

    @SubscribeEvent
    fun handleEntityInteract(event: EntityInteract) {
        val player = event.entity
        val target = event.target
        val hand = event.hand

        event.isCanceled = BreedingInitializer.attemptBreeding(player, hand, target) != ActionResult.PASS
    }
}