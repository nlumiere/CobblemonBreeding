package dev.thomasqtruong.veryscuffedcobblemonbreeding.config;

import com.cobblemon.mod.common.api.pokemon.egg.EggGroup;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonWriter;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;

public class VeryScuffedCobblemonBreedingConfig {
    Gson GSON = new GsonBuilder()
            .disableHtmlEscaping()
            .setPrettyPrinting()
            .create();
    public static int COMMAND_POKEBREED_PERMISSION_LEVEL = 3;      // Default for MC is 2.
    public static int VIP_COMMAND_POKEBREED_PERMISSION_LEVEL = 3;  // VIP permission level.
    public static int COOLDOWN_IN_MINUTES = 5;      // Default: 5 minutes cooldown.
    public static int VIP_COOLDOWN_IN_MINUTES = 0;  // VIP breeding cooldown, default: 3.

    public static int POKEMON_COOLDOWN_IN_MINUTES = 15;
    public static int DITTO_BREEDING = 1;  // Whether breeding with a ditto is allowed or not, default: 1 (true).
    public static int HIDDEN_ABILITY = 1;  // Whether passing down hidden abilities is enabled: 1 (true).

    public static int CONSUME_BREEDING_HELD_ITEMS = 0; // Whether breeding should consume held items: 1 (true).

    public static int CONSUME_BREEDING_STIMULUS_ITEM = 1; // Rate at which player's items they use to cause breeding should be consumed: 1

    public static int USE_SINGULAR_BREEDING_ITEM = 1;

    public static String SINGULAR_ITEM = "minecraft:diamond";

    public static HashMap<EggGroup, String> EGG_GROUP_ITEMS = new HashMap<>();

    public VeryScuffedCobblemonBreedingConfig() {
        init();
    }

    // Extracts data from the config file.
    public void init() {
        File configFolder = new File(System.getProperty("user.dir") + "/config/veryscuffedcobblemonbreeding");
        File configFile = new File(configFolder, "config.json");
        System.out.println("VeryScuffedCobblemonBreeding config -> " + configFolder.getAbsolutePath());
        initEggGroupMap();
        if (!configFolder.exists()) {
            configFolder.mkdirs();
            createConfig(configFolder);
        } else if (!configFile.exists()) {
            createConfig(configFolder);
        }

        try {
            Type type = new TypeToken<HashMap<String, Integer>>(){}.getType();
            Type type2 = new TypeToken<HashMap<String, String>>(){}.getType();
            JsonObject obj = GSON.fromJson(new FileReader(configFile), JsonObject.class);

            JsonObject permLevels = obj.get("permission_levels").getAsJsonObject();
            HashMap<String, Integer> permissionMap = GSON.fromJson(permLevels, type);
            COMMAND_POKEBREED_PERMISSION_LEVEL = permissionMap.getOrDefault("command.pokebreed",  3);
            VIP_COMMAND_POKEBREED_PERMISSION_LEVEL = permissionMap.getOrDefault("command.vippokebreed", 3);

            JsonObject cooldowns = obj.get("cooldowns").getAsJsonObject();
            HashMap<String, Integer> cooldownsMap = GSON.fromJson(cooldowns, type);
            COOLDOWN_IN_MINUTES = cooldownsMap.getOrDefault("command.pokebreed.cooldown", 5);
            VIP_COOLDOWN_IN_MINUTES = cooldownsMap.getOrDefault("command.pokebreed.vipcooldown", 0);
            POKEMON_COOLDOWN_IN_MINUTES = cooldownsMap.getOrDefault("command.pokebreed.pokemoncooldown", 15);

            JsonObject otherFeatures = obj.get("other_features").getAsJsonObject();
            HashMap<String, Integer> otherFeaturesMap = GSON.fromJson(otherFeatures, type);
            DITTO_BREEDING = otherFeaturesMap.getOrDefault("ditto.breeding", 1);
            HIDDEN_ABILITY = otherFeaturesMap.getOrDefault("hidden.ability", 1);

            JsonObject breedingItemConfig = obj.get("breeding_item_config").getAsJsonObject();
            HashMap<String, Integer> breedingItemConfigMap = GSON.fromJson(breedingItemConfig, type);
            CONSUME_BREEDING_HELD_ITEMS = breedingItemConfigMap.getOrDefault("consume.helditem", 0);
            CONSUME_BREEDING_STIMULUS_ITEM = breedingItemConfigMap.getOrDefault("consume.playeritem", 1);
            USE_SINGULAR_BREEDING_ITEM = breedingItemConfigMap.getOrDefault("item.use_singular_breeding_item", 1);
            
            JsonObject breedingItemChoicesConfig = obj.get("item_choices").getAsJsonObject();
            HashMap<String, String> breedingItemChoicesMap = GSON.fromJson(breedingItemChoicesConfig, type2);
            SINGULAR_ITEM = breedingItemChoicesMap.getOrDefault("singular_item", "minecraft:diamond");
            EGG_GROUP_ITEMS.put(EggGroup.MONSTER, breedingItemChoicesMap.getOrDefault("monster", "minecraft:shulker_shell"));
            EGG_GROUP_ITEMS.put(EggGroup.WATER_1, breedingItemChoicesMap.getOrDefault("water1", "minecraft:prismarine_crystals"));
            EGG_GROUP_ITEMS.put(EggGroup.WATER_2, breedingItemChoicesMap.getOrDefault("water2", "minecraft:prismarine_shard"));
            EGG_GROUP_ITEMS.put(EggGroup.WATER_3, breedingItemChoicesMap.getOrDefault("water3", "cobblemon:nautilus_shell"));
            EGG_GROUP_ITEMS.put(EggGroup.BUG, breedingItemChoicesMap.getOrDefault("bug", "minecraft:emerald"));
            EGG_GROUP_ITEMS.put(EggGroup.FLYING, breedingItemChoicesMap.getOrDefault("flying", "cobblemon:sky_tumblestone"));
            EGG_GROUP_ITEMS.put(EggGroup.FIELD, breedingItemChoicesMap.getOrDefault("field", "minecraft:goat_horn"));
            EGG_GROUP_ITEMS.put(EggGroup.FAIRY, breedingItemChoicesMap.getOrDefault("fairy", "minecraft:golden_apple"));
            EGG_GROUP_ITEMS.put(EggGroup.GRASS, breedingItemChoicesMap.getOrDefault("grass", "minecraft:scute"));
            EGG_GROUP_ITEMS.put(EggGroup.HUMAN_LIKE, breedingItemChoicesMap.getOrDefault("humanlike", "minecraft:waxed_oxidized_copper"));
            EGG_GROUP_ITEMS.put(EggGroup.MINERAL, breedingItemChoicesMap.getOrDefault("mineral", "minecraft:amethyst_cluster"));
            EGG_GROUP_ITEMS.put(EggGroup.AMORPHOUS, breedingItemChoicesMap.getOrDefault("amorphous", "minecraft:magma_cream"));
            EGG_GROUP_ITEMS.put(EggGroup.DITTO, breedingItemChoicesMap.getOrDefault("ditto", "minecraft:diamond"));
            EGG_GROUP_ITEMS.put(EggGroup.DRAGON, breedingItemChoicesMap.getOrDefault("dragon", "minecraft:dragon_breath"));

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void initEggGroupMap() {
        EGG_GROUP_ITEMS.put(EggGroup.MONSTER, "minecraft:shulker_shell");
        EGG_GROUP_ITEMS.put(EggGroup.WATER_1, "minecraft:prismarine_crystals");
        EGG_GROUP_ITEMS.put(EggGroup.WATER_2, "minecraft:prismarine_shard");
        EGG_GROUP_ITEMS.put(EggGroup.WATER_3, "cobblemon:nautilus_shell");
        EGG_GROUP_ITEMS.put(EggGroup.BUG, "minecraft:emerald");
        EGG_GROUP_ITEMS.put(EggGroup.FLYING, "cobblemon:sky_tumblestone");
        EGG_GROUP_ITEMS.put(EggGroup.FIELD, "minecraft:goat_horn");
        EGG_GROUP_ITEMS.put(EggGroup.FAIRY, "minecraft:golden_apple");
        EGG_GROUP_ITEMS.put(EggGroup.GRASS, "minecraft:scute");
        EGG_GROUP_ITEMS.put(EggGroup.HUMAN_LIKE, "minecraft:waxed_oxidized_copper");
        EGG_GROUP_ITEMS.put(EggGroup.MINERAL, "minecraft:amethyst_cluster");
        EGG_GROUP_ITEMS.put(EggGroup.AMORPHOUS, "minecraft:magma_cream");
        EGG_GROUP_ITEMS.put(EggGroup.DITTO, "minecraft:diamond");
        EGG_GROUP_ITEMS.put(EggGroup.DRAGON, "minecraft:dragon_breath");
    }

    private void createConfig(File configFolder) {
        File file = new File(configFolder, "config.json");
        try {
            file.createNewFile();
            JsonWriter writer = GSON.newJsonWriter(new FileWriter(file));
            writer.beginObject()
                    .name("permission_levels")
                        .beginObject()
                            .name("command.pokebreed")
                            .value(COMMAND_POKEBREED_PERMISSION_LEVEL)
                            .name("command.vippokebreed")
                            .value(VIP_COMMAND_POKEBREED_PERMISSION_LEVEL)
                        .endObject()
                    .name("cooldowns")
                        .beginObject()
                            .name("command.pokebreed.cooldown")
                            .value(COOLDOWN_IN_MINUTES)
                            .name("command.pokebreed.vipcooldown")
                            .value(VIP_COOLDOWN_IN_MINUTES)
                        .endObject()
                    .name("other_features")
                        .beginObject()
                            .name("ditto.breeding")
                            .value(DITTO_BREEDING)
                            .name("hidden.ability")
                            .value(HIDDEN_ABILITY)
                        .endObject()
                    .name("breeding_item_config")
                        .beginObject()
                            .name("consume.helditem")
                            .value(CONSUME_BREEDING_HELD_ITEMS)
                            .name("consume.playeritem")
                            .value(CONSUME_BREEDING_STIMULUS_ITEM)
                            .name("item.use_singular_breeding_item")
                            .value(USE_SINGULAR_BREEDING_ITEM)
                        .endObject()
                    .name("item_choices")
                        .beginObject()
                            .name("singular_item")
                            .value(SINGULAR_ITEM)
                            .name("monster")
                            .value(EGG_GROUP_ITEMS.get(EggGroup.MONSTER))
                            .name("water1")
                            .value(EGG_GROUP_ITEMS.get(EggGroup.WATER_1))
                            .name("water2")
                            .value(EGG_GROUP_ITEMS.get(EggGroup.WATER_2))
                            .name("water3")
                            .value(EGG_GROUP_ITEMS.get(EggGroup.WATER_3))
                            .name("bug")
                            .value(EGG_GROUP_ITEMS.get(EggGroup.BUG))
                            .name("flying")
                            .value(EGG_GROUP_ITEMS.get(EggGroup.FLYING))
                            .name("field")
                            .value(EGG_GROUP_ITEMS.get(EggGroup.FIELD))
                            .name("fairy")
                            .value(EGG_GROUP_ITEMS.get(EggGroup.FAIRY))
                            .name("grass")
                            .value(EGG_GROUP_ITEMS.get(EggGroup.GRASS))
                            .name("humanlike")
                            .value(EGG_GROUP_ITEMS.get(EggGroup.HUMAN_LIKE))
                            .name("mineral")
                            .value(EGG_GROUP_ITEMS.get(EggGroup.MINERAL))
                            .name("amorphous")
                            .value(EGG_GROUP_ITEMS.get(EggGroup.AMORPHOUS))
                            .name("ditto")
                            .value(EGG_GROUP_ITEMS.get(EggGroup.DITTO))
                            .name("dragon")
                            .value(EGG_GROUP_ITEMS.get(EggGroup.DRAGON))
                        .endObject()
                    .endObject()
                    .flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
