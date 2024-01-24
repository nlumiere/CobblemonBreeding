package dev.thomasqtruong.veryscuffedcobblemonbreeding

import dev.thomasqtruong.veryscuffedcobblemonbreeding.config.VeryScuffedCobblemonBreedingConfig.POKEMON_COOLDOWN_IN_MINUTES
import com.cobblemon.mod.common.Cobblemon.storage
import com.cobblemon.mod.common.api.storage.party.PlayerPartyStore
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.pokemon.Pokemon
import net.minecraft.entity.Entity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.text.Text
import net.minecraft.util.ActionResult
import net.minecraft.util.Formatting
import net.minecraft.util.Hand
import java.util.*

class BreedingInitializer {
    companion object {
        private val pokemonBreedingMap: HashMap<UUID, Boolean> = HashMap()
        // Just like a player's time since last bred, but for pokemon. Gotta let them rest.
        private val pokemonRefactoryPeriod: HashMap<UUID, Double> = HashMap()
        fun attemptBreeding(player: PlayerEntity, hand: Hand, entity: Entity): ActionResult {
            val heldItemStack: ItemStack = player.getStackInHand(hand) ?: return ActionResult.PASS
            if (shouldTryBreeding(player, heldItemStack.item, entity)) {
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

                    val newItemStack = ItemStack(heldItemStack.item, heldItemStack.count.dec())
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

        private fun shouldTryBreeding(player: PlayerEntity, item: Item, entity: Entity): Boolean {
            if (item != Items.DIAMOND){
                return false
            }

            try {
                val pokemon = entity as PokemonEntity
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