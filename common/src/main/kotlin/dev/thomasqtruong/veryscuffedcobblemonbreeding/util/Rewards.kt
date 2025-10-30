package dev.thomasqtruong.veryscuffedcobblemonbreeding.util

import com.cobblemon.mod.common.CobblemonItems
import net.minecraft.world.item.Item
import net.minecraft.world.item.Items
import java.util.HashMap
import kotlin.random.Random

class Rewards {
    companion object {
        private val rewardMap = HashMap<Int, HashSet<Item>>()
        private val wagerMap = HashMap<Int, Item>()

        fun initialize() {
            wagerMap[1] = Items.IRON_INGOT
            wagerMap[2] = Items.EMERALD
            wagerMap[3] = Items.DIAMOND
            wagerMap[4] = Items.NETHERITE_INGOT
            wagerMap[5] = Items.NETHERITE_BLOCK

            rewardMap[1] = hashSetOf(
                CobblemonItems.HYPER_POTION,
                CobblemonItems.FULL_HEAL,
                CobblemonItems.REVIVE,
                CobblemonItems.CALCIUM,
                CobblemonItems.CARBOS,
                CobblemonItems.PROTEIN,
                CobblemonItems.HP_UP,
                CobblemonItems.ZINC,
                CobblemonItems.IRON,
                CobblemonItems.PP_UP,
                CobblemonItems.STICKY_BARB,
                CobblemonItems.POKE_BALL,
                CobblemonItems.GREAT_BALL
            )

            rewardMap[2] = hashSetOf(
                CobblemonItems.MAX_POTION,
                CobblemonItems.FULL_RESTORE,
                CobblemonItems.MAX_REVIVE,
                CobblemonItems.PP_MAX,
                CobblemonItems.RARE_CANDY,
                CobblemonItems.ADAMANT_MINT,
                CobblemonItems.JOLLY_MINT,
                CobblemonItems.MODEST_MINT,
                CobblemonItems.TIMID_MINT,
                CobblemonItems.IMPISH_MINT,
                CobblemonItems.CALM_MINT,
                CobblemonItems.SERIOUS_MINT,
                CobblemonItems.BOLD_MINT,
                CobblemonItems.MILD_MINT,
                CobblemonItems.BRAVE_MINT,
                CobblemonItems.GENTLE_MINT,
                CobblemonItems.HASTY_MINT,
                CobblemonItems.CAREFUL_MINT,
                CobblemonItems.LAX_MINT,
                CobblemonItems.NAIVE_MINT,
                CobblemonItems.RELAXED_MINT,
                CobblemonItems.SASSY_MINT,
                CobblemonItems.QUIET_MINT,
                CobblemonItems.RASH_MINT,
                CobblemonItems.ARMOR_FOSSIL,
                CobblemonItems.CLAW_FOSSIL,
                CobblemonItems.DOME_FOSSIL,
                CobblemonItems.COVER_FOSSIL,
                CobblemonItems.HELIX_FOSSIL,
                CobblemonItems.JAW_FOSSIL,
                CobblemonItems.SKY_TUMBLESTONE,
                CobblemonItems.RELIC_COIN,
                CobblemonItems.LINK_CABLE,
                CobblemonItems.METAL_COAT,
                CobblemonItems.RAZOR_CLAW,
                CobblemonItems.RAZOR_FANG,
                CobblemonItems.DEEP_SEA_SCALE,
                CobblemonItems.DEEP_SEA_TOOTH,
                CobblemonItems.ABSORB_BULB,
                CobblemonItems.PROTECTOR,
                CobblemonItems.BLACK_SLUDGE,
                CobblemonItems.CHOICE_BAND,
                CobblemonItems.CHOICE_SPECS,
                CobblemonItems.EVERSTONE,
                CobblemonItems.GHOST_GEM,
                CobblemonItems.GRASS_GEM,
                CobblemonItems.GROUND_GEM,
                CobblemonItems.BUG_GEM,
                CobblemonItems.DARK_GEM,
                CobblemonItems.DRAGON_GEM,
                CobblemonItems.ELECTRIC_GEM,
                CobblemonItems.FAIRY_GEM,
                CobblemonItems.FIGHTING_GEM,
                CobblemonItems.FIRE_GEM,
                CobblemonItems.FLYING_GEM,
                CobblemonItems.ICE_GEM,
                CobblemonItems.NORMAL_GEM,
                CobblemonItems.POISON_GEM,
                CobblemonItems.WATER_GEM,
                CobblemonItems.ROCK_GEM,
                CobblemonItems.STEEL_GEM,
                CobblemonItems.PSYCHIC_GEM,
                CobblemonItems.ULTRA_BALL,
                CobblemonItems.DUSK_BALL,
                CobblemonItems.TIMER_BALL
            )

            rewardMap[3] = hashSetOf(
                CobblemonItems.OVAL_STONE,
                CobblemonItems.DRAGON_SCALE,
                CobblemonItems.DUBIOUS_DISC,
                CobblemonItems.PRISM_SCALE,
                CobblemonItems.AUSPICIOUS_ARMOR,
                CobblemonItems.MALICIOUS_ARMOR,
                CobblemonItems.RELIC_COIN_POUCH,
                CobblemonItems.ABILITY_SHIELD,
                CobblemonItems.CHOICE_SCARF,
                CobblemonItems.EVIOLITE,
                CobblemonItems.LIFE_ORB,
                CobblemonItems.LEFTOVERS,
                CobblemonItems.EXP_SHARE,
                CobblemonItems.LUCKY_EGG,
                CobblemonItems.SHELL_BELL,
                CobblemonItems.FAIRY_FEATHER,
                Items.NAUTILUS_SHELL,
                Items.PRISMARINE_SHARD,
                Items.PRISMARINE_CRYSTALS,
                Items.SHULKER_SHELL,
                Items.TRIDENT,
                Items.ECHO_SHARD,
                Items.ENCHANTED_GOLDEN_APPLE,
            )

            rewardMap[4] = hashSetOf(
                Items.TOTEM_OF_UNDYING,
                Items.HEART_OF_THE_SEA,
                Items.NETHERITE_INGOT,
                Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE,
                Items.ELYTRA,
                CobblemonItems.ABILITY_PATCH,
                CobblemonItems.ABILITY_CAPSULE,
            )

            rewardMap[5] = hashSetOf(
                Items.NETHER_STAR,
                CobblemonItems.MASTER_BALL,
                Items.DRAGON_EGG
            )
        }

        fun getRandomItem(level: Int): Item {
            if (level < 1 || level > 5) {
                throw Exception("Invalid level.")
            }

            val items = rewardMap[level]
            return items!!.random()
        }

        fun getWagerItem(level: Int): Item {
            if (level < 1 || level > 5) {
                throw Exception("Invalid level.")
            }

            return wagerMap[level]!!
        }
    }
}