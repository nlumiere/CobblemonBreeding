package dev.thomasqtruong.veryscuffedcobblemonbreeding.events

import com.cobblemon.mod.common.Cobblemon.config
import com.cobblemon.mod.common.api.spawning.CobblemonWorldSpawnerManager
import com.cobblemon.mod.common.api.spawning.SpawnBucket
import com.cobblemon.mod.common.api.spawning.SpawnCause
import com.cobblemon.mod.common.api.spawning.spawner.SpawningArea
import com.cobblemon.mod.common.api.text.*
import com.cobblemon.mod.common.command.CheckSpawnsCommand
import com.cobblemon.mod.common.util.lang
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.InteractionResult
import java.text.DecimalFormat
import kotlin.math.ceil

class UseProgramOnPC {
    companion object {
        private val df = DecimalFormat("#.####")

        // These are fine to make not-const. They just don't have any other use yet.
        private const val COMMON_RARITY = .938f
        private const val UNCOMMON_RARITY = .05f
        private const val RARE_RARITY = .01f
        private const val ULTRA_RARE_RARITY = .002f

        // Code copied from Cobblemon https://gitlab.com/cable-mc/cobblemon, and modified for use without commands.
        fun checkspawn(playerEntity: ServerPlayer): InteractionResult {
            var player: ServerPlayer? = null
            try {
                player = playerEntity as ServerPlayer
            }
            catch (e: Exception) {
                playerEntity.sendSystemMessage(Component.literal("Player cannot be cast to ServerPlayer"))
            }

            if (!config.enableSpawning || player == null) {
                return InteractionResult.PASS
            }

            val spawner = CobblemonWorldSpawnerManager.spawnersForPlayers[player.uuid] ?: return InteractionResult.SUCCESS
            val buckets = arrayOf("common", "uncommon", "rare", "ultra-rare")
            player.sendSystemMessage(lang("command.checkspawns.spawns").underline())

            val messages = mutableListOf<MutableComponent>()
            val spawnNames = mutableMapOf<String, MutableComponent>()
            val namedProbabilities = mutableMapOf<MutableComponent, Float>()

            buckets.forEach {bucketString ->
                val bucket = SpawnBucket(bucketString, .1f)
                val cause = SpawnCause(spawner, bucket, player)

                val slice = spawner.prospector.prospect(
                    spawner = spawner,
                    area = SpawningArea(
                        cause = cause,
                        world = player.level() as ServerLevel,
                        baseX = ceil(player.x - config.worldSliceDiameter / 2F).toInt(),
                        baseY = ceil(player.y - config.worldSliceHeight / 2F).toInt(),
                        baseZ = ceil(player.z - config.worldSliceDiameter / 2F).toInt(),
                        length = config.worldSliceDiameter,
                        height = config.worldSliceHeight,
                        width = config.worldSliceDiameter
                    )
                )

                val contexts = spawner.resolver.resolve(spawner, spawner.contextCalculators, slice)

                var multiplier = 1f
                if (bucketString == "common") {
                    multiplier = COMMON_RARITY
                }
                else if (bucketString == "uncommon") {
                    multiplier = UNCOMMON_RARITY
                }
                else if (bucketString == "rare") {
                    multiplier = RARE_RARITY
                }
                else {
                    multiplier = ULTRA_RARE_RARITY
                }

                val spawnProbabilities = spawner.getSpawningSelector().getProbabilities(spawner, contexts)

                spawnProbabilities.entries.forEach {
                    val nameText = it.key.getName()
                    val nameString = nameText.string
                    if (!spawnNames.containsKey(nameString)) {
                        spawnNames[nameString] = it.key.getName()
                    }

                    val standardizedNameText = spawnNames[nameString]!!
                    namedProbabilities[standardizedNameText] =
                        (namedProbabilities[standardizedNameText] ?: 0F) + (it.value * multiplier)
                }
            }

            val sortedEntries = namedProbabilities.entries.sortedByDescending { it.value }
            sortedEntries.forEach { (name, percentage) ->
                val twoDfPercentage = CheckSpawnsCommand.df.format(percentage)
                val percentageString = if (twoDfPercentage.equals("0")) {
                    df.format(percentage)
                } else {
                    twoDfPercentage
                }
                val message = name + ": " + CheckSpawnsCommand.applyColour(
                    "${percentageString}%".text(),
                    percentage
                )
                messages.add(message)
            }

            if (messages.isEmpty()) {
                player.sendSystemMessage(lang("command.checkspawns.nothing").red())
            } else {
                val msg = messages[0]
                for (nextMessage in messages.subList(1, messages.size)) {
                    msg.add(", ".text() + nextMessage)
                }
                player.sendSystemMessage(msg)
            }

            return InteractionResult.SUCCESS
        }
    }
}