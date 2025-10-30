package dev.thomasqtruong.veryscuffedcobblemonbreeding.fabric.listeners

import com.cobblemon.mod.common.CobblemonItems
import com.cobblemon.mod.common.battles.BattleRegistry
import com.cobblemon.mod.common.platform.events.PlatformEvents
import com.cobblemon.mod.common.util.getPlayer
import com.cobblemon.mod.common.util.giveOrDropItemStack
import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Component
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.npc.Villager
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import java.util.HashMap
import java.util.UUID

class BattleRegistryListener {
    companion object {
        private val playerNPCBattleMap = HashMap<UUID, Pair<UUID, Item>>()
        private val villagerPlayerBattleMap = HashMap<UUID, UUID>()
        private val villagerLockoutMap = HashMap<UUID, Int>()
        private var tickCount = 0

        fun initialize() {
            PlatformEvents.SERVER_TICK_POST.subscribe { tick(it.server) }
        }

        fun put(player: ServerPlayer, npc: Villager, item: Item) {
            playerNPCBattleMap[player.uuid] = Pair(npc.uuid, item)
            villagerLockoutMap[npc.uuid] = 600*20
        }

        private fun removeIfExists(uuid: UUID) {
            if (playerNPCBattleMap.containsKey(uuid) && playerNPCBattleMap[uuid] != null) {
                val value = playerNPCBattleMap[uuid]
                playerNPCBattleMap.remove(uuid)
                if (value != null) {
                    removeIfExists(value.first)
                }
            }

            if (villagerPlayerBattleMap.containsKey(uuid)) {
                val value = villagerPlayerBattleMap[uuid]
                villagerPlayerBattleMap.remove(uuid)
                if (value != null) {
                    removeIfExists(value)
                }
            }
        }

        fun timeBeforeVillagerCanBattle(uuid: UUID): Int {
            if (!villagerLockoutMap.containsKey(uuid)) {
                return 0
            }

            return villagerLockoutMap[uuid]!!/20
        }

        private fun tick(server: MinecraftServer) {
            tickCount++
            if (tickCount % 40 != 0) {
                return
            }
            else {
                tickCount = 0
            }

            villagerLockoutMap.forEach { it ->
                if (it.value > 0) {
                    val newVal = (it.value - 40).coerceAtLeast(0)
                    villagerLockoutMap[it.key] = newVal
                }
            }

            // Nightmare
            playerNPCBattleMap.forEach { it ->
                val battle = BattleRegistry.getBattleByParticipatingPlayerId(it.key)
                val player = server.playerList.getPlayer(it.key) ?: return
                if (battle == null || battle.ended) {
                    removeIfExists(player.uuid)
                }

                if (battle != null && battle.ended) {
                    player.sendSystemMessage(Component.literal("You Win!").withStyle(ChatFormatting.GREEN))
                }
            }
        }

        fun isPlayerBattling(uuid: UUID): Boolean {
            return playerNPCBattleMap.containsKey(uuid)
        }
    }
}