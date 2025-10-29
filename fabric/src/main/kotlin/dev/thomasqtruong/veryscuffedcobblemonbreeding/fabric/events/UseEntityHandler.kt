package dev.thomasqtruong.veryscuffedcobblemonbreeding.fabric.events

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.util.party
import dev.thomasqtruong.veryscuffedcobblemonbreeding.events.PokeMount
import dev.thomasqtruong.veryscuffedcobblemonbreeding.events.VillagerBattle
import dev.thomasqtruong.veryscuffedcobblemonbreeding.events.VillagerBattle.BattleLevel
import dev.thomasqtruong.veryscuffedcobblemonbreeding.fabric.listeners.BattleRegistryListener
import dev.thomasqtruong.veryscuffedcobblemonbreeding.fabric.listeners.BattleRegistryListener.Companion.timeBeforeVillagerCanBattle
import dev.thomasqtruong.veryscuffedcobblemonbreeding.BreedingInitializer
import net.fabricmc.fabric.api.event.player.UseEntityCallback
import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.level.Level
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.npc.Villager
import net.minecraft.world.entity.npc.VillagerProfession
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
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
        var result = TryBreed(player, world, hand, entity, hitResult);
        if (result == InteractionResult.PASS)
        {
            result = TryOther(player, world, hand, entity, hitResult);
        }

        return result;
    }

    private fun TryBreed(player: Player,
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

    private fun TryOther(
        player: Player,
        world: Level,
        hand: InteractionHand,
        entity: Entity,
        hitResult: EntityHitResult?
    ): InteractionResult {
        if (world.isClientSide()) {
            return InteractionResult.PASS
        }

        if (executed) {
            executed = false
            return InteractionResult.PASS
        }

        if (player !is ServerPlayer)
        {
            return InteractionResult.PASS;
        }

        val serverPlayer = player as ServerPlayer;

        executed = true

        try {
            val pokemonEntity = entity as PokemonEntity
            val pokemon = pokemonEntity.pokemon

            if (pokemon.heldItem().item != Items.SADDLE) {
                return InteractionResult.PASS
            }

            return PokeMount.handleUseEntity(serverPlayer, hand, entity)
        } catch (e: Exception) {
        }

        try {
            val party = Cobblemon.storage.getParty(serverPlayer);
            val villagerEntity = entity as Villager

            if (party.toList().isEmpty() || party.first().isFainted()) {
                serverPlayer.sendSystemMessage(
                    Component.literal("The first member of your party does not exist or is ineligible for battle. Please change your first party slot and try again.")
                        .withStyle(ChatFormatting.RED)
                )
            }

            var partyOut = false
            party.forEach { it ->
                if (it.entity != null) {
                    partyOut = true
                }
            }

            if (!partyOut) {
                player.sendSystemMessage(Component.literal("This villager will battle you. Send out a Pokemon and try your wager again."))
                return InteractionResult.PASS
            }

            // If villager is unemployed, they must be a pokemon trainer. I don't make the rules.
            if (villagerEntity.villagerData.profession == VillagerProfession.NONE || villagerEntity.villagerData.profession == VillagerProfession.NITWIT) {
                // Start trainer battle against villager
                val heldItemStack: ItemStack = player.getItemInHand(hand)
                val heldItem = heldItemStack.item
                var battleLevel = VillagerBattle.BattleLevel.EASY
                if (heldItem == Items.IRON_INGOT) {
                } else if (heldItem == Items.EMERALD) {
                    battleLevel = VillagerBattle.BattleLevel.MEDIUM
                } else if (heldItem == Items.DIAMOND) {
                    battleLevel = VillagerBattle.BattleLevel.DIFFICULT
                } else if (heldItem == Items.NETHERITE_INGOT) {
                    battleLevel = VillagerBattle.BattleLevel.EXTREME
                } else if (heldItem == Items.NETHERITE_BLOCK) {
                    battleLevel = VillagerBattle.BattleLevel.UNFAIR
                } else if (partyOut) {
                    player.sendSystemMessage(Component.literal("This villager can battle, but only will if there's a wager.\nEasy: Iron Ingot\nMedium: Emerald\nHard: Diamond\nExtreme: Netherite Ingot\nUnfair: Netherite Block"))
                    return InteractionResult.PASS
                }

                val timeLeft = timeBeforeVillagerCanBattle(villagerEntity.uuid)
                if (timeLeft > 0) {
                    player.sendSystemMessage(
                        Component.literal("You must wait another $timeLeft seconds before battling with this villager again.")
                            .withStyle(ChatFormatting.RED)
                    )
                    return InteractionResult.PASS
                }

                if (villagerEntity.villagerData.profession == VillagerProfession.NITWIT && (battleLevel == VillagerBattle.BattleLevel.DIFFICULT || battleLevel == VillagerBattle.BattleLevel.EXTREME || battleLevel == VillagerBattle.BattleLevel.UNFAIR)) {
                    player.sendSystemMessage(
                        Component.literal("This villager is a nitwit. Nitwits can only battle in Easy or Medium level battles. Try offering iron or emeralds.")
                            .withStyle(ChatFormatting.RED)
                    )
                    return InteractionResult.PASS
                }

                val playerAceLevel = (player as ServerPlayer).party().maxOf { it -> it.level }

                if (playerAceLevel < 40 && battleLevel == BattleLevel.DIFFICULT) {
                    player.sendSystemMessage(
                        Component.literal("Difficult battles unlocked at level 40.").withStyle(ChatFormatting.RED)
                    )
                    return InteractionResult.PASS
                }

                player.sendSystemMessage(Component.literal("Trying to start battle with villager..."))
                val result = VillagerBattle.startBattle(player as ServerPlayer, villagerEntity, battleLevel)
                if (result == InteractionResult.SUCCESS) {
                    BattleRegistryListener.put(player, villagerEntity, heldItem)
                    heldItemStack.shrink(1)
                }
                else {
                    player.sendSystemMessage(Component.literal("There was an error starting the battle.").withStyle(ChatFormatting.RED))
                }
            }
        } catch (e: Exception) {
            player.sendSystemMessage(Component.literal("There was an exception: $e").withStyle(ChatFormatting.RED))
        }

        return InteractionResult.PASS
    }
}