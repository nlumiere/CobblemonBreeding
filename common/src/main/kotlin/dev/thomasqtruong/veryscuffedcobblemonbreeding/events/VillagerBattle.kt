package dev.thomasqtruong.veryscuffedcobblemonbreeding.events

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.CobblemonItems
import com.cobblemon.mod.common.CobblemonSounds
import com.cobblemon.mod.common.api.pokemon.stats.Stats
import com.cobblemon.mod.common.api.storage.party.PartyStore
import com.cobblemon.mod.common.battles.*
import com.cobblemon.mod.common.battles.BattleRegistry.getBattleByParticipatingPlayer
import com.cobblemon.mod.common.battles.actor.PlayerBattleActor
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon
import dev.thomasqtruong.veryscuffedcobblemonbreeding.ai.Optimization
import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.pokemon.evolution.requirements.LevelRequirement
import com.cobblemon.mod.common.util.giveOrDropItemStack
import com.cobblemon.mod.common.util.party
import dev.thomasqtruong.veryscuffedcobblemonbreeding.ai.VillagerActor
import dev.thomasqtruong.veryscuffedcobblemonbreeding.util.Rewards
import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.npc.Villager
import net.minecraft.world.item.ItemStack
import java.util.*
import kotlin.random.Random

class VillagerBattle {

    enum class BattleLevel(val num: Int) {
        EASY(0),
        MEDIUM(1),
        DIFFICULT(2),
        EXTREME(3),
        UNFAIR(4)
    }

    companion object {
        val requestSet: HashSet<UUID> = HashSet.newHashSet(0)

        fun startBattle(player: ServerPlayer, villagerEntity: Villager, battleLevel: BattleLevel = BattleLevel.EASY): InteractionResult {
            if (requestSet.contains(player.uuid))
            {
                requestSet.remove(player.uuid)
                return InteractionResult.PASS
            }

            requestSet.add(player.uuid)
            if (getBattleByParticipatingPlayer(player) != null) {
                return InteractionResult.PASS
            }

            val playerTeam = Cobblemon.storage.getParty(player).toBattleTeam(battleLevel == BattleLevel.UNFAIR)

            playerTeam.forEach {
                if (battleLevel == BattleLevel.UNFAIR) {
                    it.effectedPokemon.apply {
                        level -= 10
                        swapHeldItem(ItemStack.EMPTY)
                    }
                }

                it.effectedPokemon.apply {
                    heal()
                }
            }

            val playerAceLevel = player.party().maxOf { it -> it.level }

            val skill = 5
            // TODO: Make 6
            val npcParty = List<BattlePokemon>(1 ) { index ->
                var pkmn = Pokemon()
                // Only kids can have legies
                if (!villagerEntity.isBaby && (pkmn.isLegendary() || pkmn.isMythical()) && battleLevel != BattleLevel.EXTREME && battleLevel != BattleLevel.UNFAIR) {
                    pkmn.currentHealth = 0
                }
                if (battleLevel == BattleLevel.EASY) {
                    pkmn.level = 1.coerceAtLeast(Random.nextInt(playerAceLevel - 8, playerAceLevel - 3))
                }
                else if (battleLevel == BattleLevel.MEDIUM) {
                    pkmn.level = 1.coerceAtLeast(Random.nextInt(playerAceLevel - 2, playerAceLevel))
                }
                else if (battleLevel == BattleLevel.DIFFICULT) {
                    pkmn.level = 95.coerceAtMost(Random.nextInt(playerAceLevel, playerAceLevel + 4))
                }
                else {
                    pkmn.level = 100
                }

                pkmn = pkmn.initialize()
                val preEvolution = pkmn.preEvolution
                if (preEvolution?.species != null && (battleLevel == BattleLevel.EASY || battleLevel == BattleLevel.MEDIUM)) {
                    val newPokemon = Pokemon()
                    newPokemon.level = pkmn.level
                    newPokemon.species = preEvolution.species
                    pkmn = newPokemon.initialize()
                }

                var evolutions = pkmn.evolutions
                var shouldStopNow = battleLevel == BattleLevel.EASY
                while (evolutions.toList().isNotEmpty() && !shouldStopNow) {
                    var meetsRequirements = true
                    val evolutionRequirements = evolutions.first().requirements
                    evolutionRequirements.forEach {
                        if (it is LevelRequirement && !it.check(pkmn)) {
                            meetsRequirements = false
                        }
                    }
                    if (!meetsRequirements) {
                        break
                    }

                    pkmn.evolutions.first().applyTo(pkmn)
                    evolutions = pkmn.evolutions
                    shouldStopNow = battleLevel == BattleLevel.MEDIUM
                }

                if (battleLevel == BattleLevel.DIFFICULT || battleLevel == BattleLevel.EXTREME || battleLevel == BattleLevel.UNFAIR) {
                    val stats = intArrayOf(
                        pkmn.maxHealth,
                        pkmn.attack,
                        pkmn.defence,
                        pkmn.specialAttack,
                        pkmn.specialDefence,
                        pkmn.speed
                    )
                    val highestStatVal = stats.max()
                    val highestStatIndex = stats.indexOf(highestStatVal)
                    if (pkmn.evolutions.toList().isNotEmpty()) {
                        pkmn.swapHeldItem(ItemStack(CobblemonItems.EVIOLITE))
                    } else if (highestStatIndex == 0) {
                        pkmn.swapHeldItem(ItemStack(CobblemonItems.LEFTOVERS))
                    } else if (highestStatIndex == 1 && pkmn.specialAttack > 0 && (pkmn.attack / pkmn.specialAttack) > .7 && battleLevel == BattleLevel.EXTREME) {
                        pkmn.swapHeldItem(ItemStack(CobblemonItems.CHOICE_BAND))
                    } else if (highestStatIndex == 1) {
                        pkmn.swapHeldItem(ItemStack(CobblemonItems.LIFE_ORB))
                    } else if (highestStatIndex == 2) {
                        pkmn.swapHeldItem(ItemStack(CobblemonItems.WEAKNESS_POLICY))
                    } else if (highestStatIndex == 3 && pkmn.attack > 0 && (pkmn.specialAttack / pkmn.attack) > .7 && battleLevel == BattleLevel.EXTREME) {
                        pkmn.swapHeldItem(ItemStack(CobblemonItems.CHOICE_SPECS))
                    } else if (highestStatIndex == 3) {
                        pkmn.swapHeldItem(ItemStack(CobblemonItems.LIFE_ORB))
                    } else if (highestStatIndex == 4) {
                        pkmn.swapHeldItem(ItemStack(CobblemonItems.EJECT_BUTTON))
                    } else if (highestStatIndex == 5) {
                        pkmn.swapHeldItem(ItemStack(CobblemonItems.FOCUS_SASH))
                    } else {
                        pkmn.swapHeldItem(ItemStack(CobblemonItems.SITRUS_BERRY))
                    }

                    if (battleLevel == BattleLevel.UNFAIR) {
                        val allStats = arrayOf(Stats.HP, Stats.ATTACK, Stats.DEFENCE, Stats.SPECIAL_DEFENCE, Stats.SPECIAL_ATTACK, Stats.SPEED)
                        allStats.forEach {
                            pkmn.setEV(it, 252)
                        }
                    }
                    else {
                        Optimization.optimizeEVs(pkmn)
                    }
                }

                if (battleLevel == BattleLevel.EXTREME) {
                    Optimization.optimizeIVs(pkmn)
                }

                BattlePokemon.safeCopyOf(pkmn)
            }

            npcParty.forEach {
                it.effectedPokemon.apply {
                    heal()
                }
            }

            val battleFormat = BattleFormat.GEN_9_SINGLES
            val response = pvv(
                player,
                villagerEntity,
                npcParty,
                null,
                battleFormat,
                false,
                false,
                player.party(),
                battleLevel.num + 1
            )

            player.sendSystemMessage(Component.literal("Battle started!").withStyle(ChatFormatting.GREEN))
            return InteractionResult.SUCCESS
        }

        /**
         * Player vs Villager
         */
        fun pvv(
            player: ServerPlayer,
            npcEntity: Villager,
            npcParty: List<BattlePokemon>,
            leadingPokemon: UUID? = null,
            battleFormat: BattleFormat = BattleFormat.GEN_9_SINGLES,
            cloneParties: Boolean = false,
            healFirst: Boolean = false,
            party: PartyStore = player.party(),
            difficulty: Int = 1
        ): BattleStartResult {
            val playerTeam = party.toBattleTeam(clone = cloneParties, healPokemon = healFirst, leadingPokemon = leadingPokemon)
            val playerActor = PlayerBattleActor(player.uuid, playerTeam)
            val errors = ErroredBattleStart()

            if (playerActor.pokemonList.size < battleFormat.battleType.slotsPerActor) {
                errors.participantErrors[playerActor] += BattleStartError.insufficientPokemon(
                    actorEntity = player,
                    requiredCount = battleFormat.battleType.slotsPerActor,
                    hadCount = playerActor.pokemonList.size
                )
            }

            if (BattleRegistry.getBattleByParticipatingPlayer(player) != null) {
                errors.participantErrors[playerActor] += BattleStartError.alreadyInBattle(playerActor)
            }

            val npcActor = VillagerActor(npcEntity, npcParty, difficulty)

            if (npcActor.pokemonList.filter { it.health > 0 }.size < battleFormat.battleType.slotsPerActor) {
                errors.participantErrors[npcActor] += BattleStartError.insufficientPokemon(
                    actorEntity = npcEntity,
                    requiredCount = battleFormat.battleType.slotsPerActor,
                    hadCount = npcActor.pokemonList.size
                )
            }

            if (playerActor.pokemonList.any { it.entity?.isBusy == true }) {
                errors.participantErrors[playerActor] += BattleStartError.targetIsBusy(player.displayName ?: player.name)
            }

            playerActor.battleTheme = CobblemonSounds.PVN_BATTLE

            return if (errors.isEmpty) {
                BattleRegistry.startBattle(
                    battleFormat = battleFormat,
                    side1 = BattleSide(playerActor),
                    side2 = BattleSide(npcActor)
                ).ifSuccessful { battle ->
//                    npcEntity.entityData.update(NPCEntity.BATTLE_IDS) { it + battle.battleId }
                    requestSet.remove(player.uuid)
                    npcActor.pokemonList.forEach {
                        it.actor = npcActor
                        it.entity?.drops = null
                    }
                }
            } else {
                player.giveOrDropItemStack(ItemStack(Rewards.getWagerItem(difficulty)))
                errors.forError<BattleStartError> { it -> player.sendSystemMessage(Component.literal(it.toString()))}
            }
        }
    }
}