package dev.thomasqtruong.veryscuffedcobblemonbreeding.ai

import com.cobblemon.mod.common.entity.npc.NPCEntity

/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

import com.cobblemon.mod.common.api.battles.model.actor.AIBattleActor
import com.cobblemon.mod.common.api.battles.model.actor.ActorType
import com.cobblemon.mod.common.api.battles.model.actor.BattleActor
import com.cobblemon.mod.common.api.battles.model.actor.EntityBackedBattleActor
import com.cobblemon.mod.common.api.net.NetworkPacket
import com.cobblemon.mod.common.battles.ai.StrongBattleAI
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon
import com.cobblemon.mod.common.net.messages.client.battle.BattleEndPacket
import com.cobblemon.mod.common.util.*
import dev.thomasqtruong.veryscuffedcobblemonbreeding.util.Rewards
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.npc.Villager
import net.minecraft.world.item.ItemStack
import java.util.concurrent.CompletableFuture

class VillagerActor(
    npc: Villager,
    pokemonList: List<BattlePokemon>,
    skill: Int,
) : AIBattleActor(
    gameId = npc.uuid,
    pokemonList = pokemonList,
    battleAI = StrongBattleAI(skill)
), EntityBackedBattleActor<LivingEntity> {
    override val entity = npc as LivingEntity
    override val type = ActorType.NPC
    override fun getName() = entity.effectiveName().copy()
    override fun nameOwned(name: String) = battleLang("owned_pokemon", this.getName(), name)
    override val initialPos = entity.position()
    val skill = skill

//    constructor(
//        npc: Villager,
//        party: List<BattlePokemon>,
//        skill: Int
//    ): this(
//        npc,
//        (party as PartyStore).toBattleTeam(healPokemon = true),
//        skill
//    )

    override fun sendUpdate(packet: NetworkPacket<*>) {
        super.sendUpdate(packet)
        if (packet is BattleEndPacket) {
            if (entity.isAlive) {
                val allEntities = pokemonList.mapNotNull { it.entity }.toMutableList()
                val finalFuture = CompletableFuture<Unit>()
                chainFutures(allEntities.map { pokemonEntity -> { pokemonEntity.recallWithAnimation() } }.iterator(), finalFuture)
                if (allEntities.isEmpty()) {
                    finalFuture.complete(Unit)
                }
                finalFuture.thenApply {
                    entity.entityData.update(NPCEntity.BATTLE_IDS) { it - battle.battleId }
                }
            } else {
                entity.entityData.update(NPCEntity.BATTLE_IDS) { it - battle.battleId }
            }
        }
    }

    override fun win(otherWinners: List<BattleActor>, losers: List<BattleActor>) {
        super.win(otherWinners, losers)
    }

    override fun lose(winners: List<BattleActor>, otherLosers: List<BattleActor>) {
        var player: ServerPlayer? = null
        val item = Rewards.getRandomItem(skill)
        winners.forEach {
            player = it.uuid.getPlayer()
        }
        player?.sendSystemMessage(Component.literal("You won! You recieved $item"))
        player?.giveOrDropItemStack(ItemStack(item))
        super.lose(winners, otherLosers)
    }
}