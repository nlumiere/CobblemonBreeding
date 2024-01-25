package dev.thomasqtruong.veryscuffedcobblemonbreeding.forge.events

import dev.thomasqtruong.veryscuffedcobblemonbreeding.BreedingInitializer
import net.minecraftforge.event.entity.player.PlayerInteractEvent.EntityInteract
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.common.Mod

@Mod.EventBusSubscriber(modid = "veryscuffedcobblemonbreeding")
class ForgeEvents {
    @SubscribeEvent
    fun handleEntityInteract(event: EntityInteract) {
        val player = event.entity
        val target = event.target
        val hand = event.hand

        event.cancellationResult = BreedingInitializer.attemptBreeding(player, hand, target)
    }
}