package dev.thomasqtruong.veryscuffedcobblemonbreeding.ai

import com.cobblemon.mod.common.api.pokemon.stats.Stats
import com.cobblemon.mod.common.pokemon.Pokemon
import kotlin.math.min

class Optimization {
    companion object {
        fun optimizeEVs(pokemon: Pokemon) {
            // Ensure stats are not null before proceeding
            if (pokemon.maxHealth == 0 || pokemon.attack == 0) {
                // Pokemon stats not initialized, skip optimization
                return
            }
            
            val stats = hashMapOf(
                Stats.HP to pokemon.maxHealth,
                Stats.ATTACK to pokemon.attack,
                Stats.DEFENCE to pokemon.defence,
                Stats.SPECIAL_ATTACK to pokemon.specialAttack,
                Stats.SPECIAL_DEFENCE to pokemon.specialDefence,
                Stats.SPEED to pokemon.speed
            )

            val sortedStats = stats.entries.sortedByDescending { it.value }.take(3).map { it.key to it.value }

            var points = 510
            var defensesDone = false
            sortedStats.forEach {
                if ((it.first == Stats.DEFENCE || it.first == Stats.SPECIAL_DEFENCE) && !defensesDone) {
                    val pointsToSpend = min(points, 252)
                    pokemon.setEV(Stats.HP, pointsToSpend)
                    points -= pointsToSpend
                    val pointsToSpend2 = min(points, 120)
                    if (it.first == Stats.DEFENCE) {
                        pokemon.setEV(Stats.SPECIAL_DEFENCE, pointsToSpend2)
                    } else {
                        pokemon.setEV(Stats.DEFENCE, pointsToSpend2)
                    }
                    points -= pointsToSpend2
                    defensesDone = true
                }
                else {
                    val pointsToSpend = min(points, 252)
                    pokemon.setEV(it.first, pointsToSpend)
                    points -= pointsToSpend
                }
            }
        }

        fun optimizeIVs(pokemon: Pokemon) {
            val allStats = arrayOf(Stats.HP, Stats.ATTACK, Stats.DEFENCE, Stats.SPECIAL_DEFENCE, Stats.SPECIAL_ATTACK, Stats.SPEED)
            allStats.forEach {
                pokemon.setIV(it, 31)
            }
        }
    }
}