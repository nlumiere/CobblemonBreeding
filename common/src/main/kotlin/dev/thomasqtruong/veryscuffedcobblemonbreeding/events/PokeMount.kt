package dev.thomasqtruong.veryscuffedcobblemonbreeding.events

import com.cobblemon.mod.common.Cobblemon.storage
import com.cobblemon.mod.common.api.pokemon.stats.Stats
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult

class PokeMount {
    companion object {
        fun handleUseEntity(player: ServerPlayer, hand: InteractionHand, pokemonEntity: PokemonEntity): InteractionResult {
            val pokemon = pokemonEntity.pokemon

            var isPokemonInParty = false
            storage.getParty(player).forEach {
                if (it.uuid == pokemon.uuid) {
                    isPokemonInParty = true
                }
            }

            if (!isPokemonInParty || pokemon.evolutions.toList().isNotEmpty() || pokemon.friendship < 100) {
                if (pokemon.evolutions.toList().isNotEmpty()) {
                    player.sendSystemMessage(Component.literal("Pokemon must be fully evolved to ride."))
                }

                if (pokemon.friendship < 100) {
                    player.sendSystemMessage(Component.literal("Your Pokemon doesn't like you enough to ride"))
                }
                return InteractionResult.PASS
            }

            player.startRiding(pokemonEntity)

            return InteractionResult.PASS
        }

        fun logisticMapping(input: Double, minInput: Double = 0.0, maxInput: Double = 400.0, minOutput: Double = 0.0, maxOutput: Double = 2.0): Double {
            input.coerceAtLeast(0.0).coerceAtMost(maxInput)
            val k = 0.03
            val midpoint = (maxInput - minInput) / 2.0

            val logisticValue = 1 / (1 + Math.exp(-k * (input - midpoint)))

            return minOutput + (maxOutput - minOutput) * logisticValue
        }

        fun getSpeedMultiplier(pokemonEntity: PokemonEntity): Double {
            val pokemon = pokemonEntity.pokemon
            val levelMult = logisticMapping(pokemon.level.toDouble(), 1.0, 100.0, .8, 1.3)
            val evsMult = pokemon.evs[Stats.SPEED]?.let { logisticMapping(it.toDouble(), 0.0, 252.0, .8, 1.3) } ?: 0.8
            val ivsMult = pokemon.ivs[Stats.SPEED]?.let { logisticMapping(it.toDouble(), 0.0, 31.0, .9, 1.2) } ?: 0.9
            val baseStatsMult =
                pokemon.form.baseStats[Stats.SPEED]?.let { logisticMapping(it.toDouble(), 30.0, 150.0, .6, 1.8) } ?: .6
            val natureMult = if (pokemon.nature.increasedStat == Stats.SPEED) 1.1 else 1.0

            return levelMult * evsMult * ivsMult * baseStatsMult * natureMult
        }

        fun getStrengthMultiplier(pokemonEntity: PokemonEntity): Double {
            val pokemon = pokemonEntity.pokemon
            val levelMult = logisticMapping(pokemon.level.toDouble(), 1.0, 100.0, .8, 1.3)
            val evsMult = pokemon.evs[Stats.HP]?.let { logisticMapping(it.toDouble(), 0.0, 252.0, .8, 1.3) } ?: 0.8
            val ivsMult = pokemon.ivs[Stats.HP]?.let { logisticMapping(it.toDouble(), 0.0, 31.0, .9, 1.2) } ?: 0.9
            val baseStatsMult =
                pokemon.form.baseStats[Stats.HP]?.let { logisticMapping(it.toDouble(), 30.0, 150.0, .6, 1.8) } ?: .6

            return levelMult * evsMult * ivsMult * baseStatsMult
        }
    }
}