package dev.thomasqtruong.veryscuffedcobblemonbreeding

import dev.thomasqtruong.veryscuffedcobblemonbreeding.config.VeryScuffedCobblemonBreedingConfig.POKEMON_COOLDOWN_IN_MINUTES
import dev.thomasqtruong.veryscuffedcobblemonbreeding.config.VeryScuffedCobblemonBreedingConfig.CONSUME_BREEDING_STIMULUS_ITEM
import com.cobblemon.mod.common.Cobblemon.storage
import com.cobblemon.mod.common.api.storage.party.PlayerPartyStore
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.pokemon.Pokemon
import dev.thomasqtruong.veryscuffedcobblemonbreeding.config.VeryScuffedCobblemonBreedingConfig
import net.minecraft.ChatFormatting
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.ItemStack
import java.util.*

class BreedingInitializer {
    companion object {
        private val pokemonBreedingMap: HashMap<UUID, Boolean> = HashMap()
        // Just like a player's time since last bred, but for pokemon. Gotta let them rest.
        private val pokemonRefactoryPeriod: HashMap<UUID, Double> = HashMap()
        private var run = false // Used to prevent spamming user with duplicates of same message
        fun attemptBreeding(player: ServerPlayer, hand: InteractionHand, entity: LivingEntity): InteractionResult {
            run = !run
            if (!run) { // There are two valid actions being sent for some reason? This eliminates one.
                return InteractionResult.PASS
            }
            val heldItemStack: ItemStack = player.getItemInHand(hand) ?: return InteractionResult.PASS
            if (shouldTryBreeding(player, heldItemStack, entity)) {
                val pokemonEntity = entity as PokemonEntity
                val pokemon = pokemonEntity.pokemon
                pokemonBreedingMap[pokemon.uuid] = true
                try {
                    val breederParty = storage.getParty(player)
                    val mate: Pokemon? = checkTrainerPokemonForMate(player, breederParty, pokemon)
                    if (mate != null) {
                        if (tryRunBreed(player, pokemon, mate) == InteractionResult.SUCCESS) {
                            pokemonBreedingMap[pokemon.uuid] = false
                            pokemonBreedingMap[mate.uuid] = false
                            val millis: Double = POKEMON_COOLDOWN_IN_MINUTES.toDouble()*60*1000
                            pokemonRefactoryPeriod[pokemon.uuid] = millis + System.currentTimeMillis()
                            pokemonRefactoryPeriod[mate.uuid] = millis + System.currentTimeMillis()
                        }
                        else {
                            return InteractionResult.PASS
                        }
                    }
                    else {
                        player.sendSystemMessage(Component.literal("${pokemon.species} is trying to breed."))
                    }

                    val newItemStack = ItemStack(heldItemStack.item, heldItemStack.count - CONSUME_BREEDING_STIMULUS_ITEM)
                    player.setItemInHand(hand, newItemStack)
                    return InteractionResult.SUCCESS
                }
                catch (_: Exception) {}
            }

            return InteractionResult.PASS
        }

        private fun pokemonWantsToBreed(uuid: UUID): Boolean {
            return pokemonBreedingMap.containsKey(uuid) && pokemonBreedingMap[uuid] == true
        }


        private fun secondsUntilPokemonCanBreed(uuid: UUID): Int {
            if (!pokemonRefactoryPeriod.containsKey(uuid)) {
                return 0
            }

            val milliseconds: Double = pokemonRefactoryPeriod[uuid] ?: return 0

            return (milliseconds - System.currentTimeMillis().toDouble()).toInt()/1000
        }

        private fun shouldTryBreeding(player: ServerPlayer, itemStack: ItemStack, entity: LivingEntity): Boolean {
            if (itemStack.count < CONSUME_BREEDING_STIMULUS_ITEM) {
                return false;
            }

            if (VeryScuffedCobblemonBreedingConfig.USE_SINGULAR_BREEDING_ITEM == 1 && itemStack.item != BuiltInRegistries.ITEM.get(ResourceLocation.parse(VeryScuffedCobblemonBreedingConfig.SINGULAR_ITEM))){
                return false
            }

            try {
                val pokemon = entity as PokemonEntity

                if (pokemonBreedingMap.containsKey(pokemon.pokemon.uuid) && pokemonBreedingMap[pokemon.pokemon.uuid] == true) {
                    return false
                }

                if (VeryScuffedCobblemonBreedingConfig.USE_SINGULAR_BREEDING_ITEM != 1) {
                    val eggGroups = pokemon.pokemon.form.eggGroups
                    var eggGroupItemMatch = false

                    eggGroups.forEach {
                        if (itemStack.item == BuiltInRegistries.ITEM.get(VeryScuffedCobblemonBreedingConfig.EGG_GROUP_ITEMS[it]?.let { it1 ->
                                ResourceLocation.parse(
                                    it1
                                )
                            })) {
                            eggGroupItemMatch = true
                        }
                    }

                    if (!eggGroupItemMatch) {
                        return false
                    }
                }

                val time = secondsUntilPokemonCanBreed(pokemon.pokemon.uuid)
                if (time > 0) {
                    player.sendSystemMessage(Component.literal("Pokemon will be ready for breeding again in $time seconds").withStyle(ChatFormatting.RED))
                    return false
                }

                if (player == pokemon.owner && !pokemonWantsToBreed(pokemon.uuid)) {
                    return true
                }
            } catch (_: Exception) {}

            return false
        }


        private fun checkTrainerPokemonForMate(player: ServerPlayer, party: PlayerPartyStore, pokemon: Pokemon): Pokemon? {
            try {
                party.iterator().forEach {
                    if (pokemonWantsToBreed(it.uuid) && it.uuid != pokemon.uuid && it.gender != pokemon.gender) {
                        return it
                    }
                }
            }
            catch (e: Exception) {
                player.sendSystemMessage(Component.literal("Unexpected error."))
            }
            return null;
        }


        private fun tryRunBreed(player: ServerPlayer, pokemon1: Pokemon, pokemon2: Pokemon): InteractionResult {
            try {
                return VeryScuffedCobblemonBreeding.pokebreed.hijackBreed(player, pokemon1, pokemon2)
            }
            catch (e: Exception) {
                player.sendSystemMessage(Component.literal("Something went wrong while trying to breed Pokemon. :(").withStyle(ChatFormatting.RED))
            }

            return InteractionResult.PASS
        }
    }
}