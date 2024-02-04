package dev.thomasqtruong.veryscuffedcobblemonbreeding

import dev.thomasqtruong.veryscuffedcobblemonbreeding.config.VeryScuffedCobblemonBreedingConfig.POKEMON_COOLDOWN_IN_MINUTES
import dev.thomasqtruong.veryscuffedcobblemonbreeding.config.VeryScuffedCobblemonBreedingConfig.CONSUME_BREEDING_STIMULUS_ITEM
import com.cobblemon.mod.common.Cobblemon.storage
import com.cobblemon.mod.common.api.storage.party.PlayerPartyStore
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.pokemon.Pokemon
import dev.thomasqtruong.veryscuffedcobblemonbreeding.config.VeryScuffedCobblemonBreedingConfig
import net.minecraft.entity.Entity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.registry.Registries
import net.minecraft.text.Text
import net.minecraft.util.ActionResult
import net.minecraft.util.Formatting
import net.minecraft.util.Hand
import net.minecraft.util.Identifier
import java.util.*

class BreedingInitializer {
    companion object {
        private val pokemonBreedingMap: HashMap<UUID, Boolean> = HashMap()
        // Just like a player's time since last bred, but for pokemon. Gotta let them rest.
        private val pokemonRefactoryPeriod: HashMap<UUID, Double> = HashMap()
        private var run = false // Used to prevent spamming user with duplicates of same message
        fun attemptBreeding(player: PlayerEntity, hand: Hand, entity: Entity): ActionResult {
            run = !run
            if (!run) { // There are two valid actions being sent for some reason? This eliminates one.
                return ActionResult.PASS
            }
            val heldItemStack: ItemStack = player.getStackInHand(hand) ?: return ActionResult.PASS
            if (shouldTryBreeding(player, heldItemStack, entity)) {
                val pokemonEntity = entity as PokemonEntity
                val pokemon = pokemonEntity.pokemon
                pokemonBreedingMap[pokemon.uuid] = true
                try {
                    val breederParty = storage.getParty(player.uuid)
                    val mate: Pokemon? = checkTrainerPokemonForMate(player, breederParty, pokemon)
                    if (mate != null) {
                        if (tryRunBreed(player, pokemon, mate) == ActionResult.SUCCESS) {
                            pokemonBreedingMap[pokemon.uuid] = false
                            pokemonBreedingMap[mate.uuid] = false
                            val millis: Double = POKEMON_COOLDOWN_IN_MINUTES.toDouble()*60*1000
                            pokemonRefactoryPeriod[pokemon.uuid] = millis + System.currentTimeMillis()
                            pokemonRefactoryPeriod[mate.uuid] = millis + System.currentTimeMillis()
                        }
                        else {
                            return ActionResult.PASS
                        }
                    }
                    else {
                        player.sendMessage(Text.literal("${pokemon.species} is trying to breed."))
                    }

                    val newItemStack = ItemStack(heldItemStack.item, heldItemStack.count - CONSUME_BREEDING_STIMULUS_ITEM)
                    player.setStackInHand(hand, newItemStack)
                    return ActionResult.SUCCESS
                }
                catch (_: Exception) {}
            }

            return ActionResult.PASS
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

        private fun shouldTryBreeding(player: PlayerEntity, itemStack: ItemStack, entity: Entity): Boolean {
            if (itemStack.count < CONSUME_BREEDING_STIMULUS_ITEM) {
                return false;
            }

            if (VeryScuffedCobblemonBreedingConfig.USE_SINGULAR_BREEDING_ITEM == 1 && itemStack.item != Registries.ITEM.get(Identifier.tryParse(VeryScuffedCobblemonBreedingConfig.SINGULAR_ITEM))){
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
                        if (itemStack.item == Registries.ITEM.get(Identifier.tryParse(VeryScuffedCobblemonBreedingConfig.EGG_GROUP_ITEMS[it]))) {
                            eggGroupItemMatch = true
                        }
                    }

                    if (!eggGroupItemMatch) {
                        return false
                    }
                }

                val time = secondsUntilPokemonCanBreed(pokemon.pokemon.uuid)
                if (time > 0) {
                    player.sendMessage(Text.literal("Pokemon will be ready for breeding again in $time seconds").formatted(Formatting.RED))
                    return false
                }

                if (player == pokemon.owner && !pokemonWantsToBreed(pokemon.uuid)) {
                    return true
                }
            } catch (_: Exception) {}

            return false
        }


        private fun checkTrainerPokemonForMate(player: PlayerEntity, party: PlayerPartyStore, pokemon: Pokemon): Pokemon? {
            try {
                party.iterator().forEach {
                    if (pokemonWantsToBreed(it.uuid) && it.uuid != pokemon.uuid && it.gender != pokemon.gender) {
                        return it
                    }
                }
            }
            catch (e: Exception) {
                player.sendMessage(Text.literal("Unexpected error."))
            }
            return null;
        }


        private fun tryRunBreed(player: PlayerEntity, pokemon1: Pokemon, pokemon2: Pokemon): ActionResult {
            try {
                return VeryScuffedCobblemonBreeding.pokebreed.hijackBreed(player, pokemon1, pokemon2)
            }
            catch (e: Exception) {
                player.sendMessage(Text.literal("Something went wrong while trying to breed Pokemon. :("))
            }

            return ActionResult.PASS
        }
    }
}