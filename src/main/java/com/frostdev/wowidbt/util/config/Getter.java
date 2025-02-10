package com.frostdev.wowidbt.util.config;

import com.frostdev.wowidbt.event.MobEventRegister;
import com.frostdev.wowidbt.util.Async;
import com.frostdev.wowidbt.util.tier.*;
import com.frostdev.wowidbt.wowidbt;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.lukebemish.codecextras.comments.CommentMapCodec;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.WorldGenLevel;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
public class Getter {
     public static JsonObject jsonObject;
     public static final String CONFIG_FILE_PATH = "config/wowid/wowidbt.json";

    public static String getDimName(LevelAccessor world) {
        return Optional.ofNullable(world)
                .map(w -> w instanceof Level _lvl ? _lvl.dimension() : (w instanceof WorldGenLevel _wgl ? _wgl.getLevel().dimension() : Level.OVERWORLD))
                .map(Object::toString)
                .map(dimensionName -> dimensionName.substring(34, dimensionName.length() - 1))
                .orElse("");
    }

    public static String getGenericEntityType(Entity entity) {
        return Optional.ofNullable(entity)
                .map(Getter::getGeneric)
                .orElse("null");
    }

    private static String getGeneric(Entity entity) {
        return entity.getType().toString()
                .replaceFirst("\\.", "")
                .replaceFirst("entity", "")
                .replaceFirst("\\.", ":");
    }

    public static Map<Direction.Axis, Double> getEntitySpeed(Entity entity) {
        return Map.of(
                Direction.Axis.X, entity.getDeltaMovement().x,
                Direction.Axis.Y, entity.getDeltaMovement().y,
                Direction.Axis.Z, entity.getDeltaMovement().z
        );
    }

    public static void initializeJson() {
        File file = new File(CONFIG_FILE_PATH);
        if (!file.exists()) {
            file.getParentFile().mkdirs();
        }
        try (FileReader reader = new FileReader(CONFIG_FILE_PATH)) {
            jsonObject = JsonParser.parseReader(reader).getAsJsonObject();
            if (getDebug()) {
                wowidbt.log("config file loaded!");
            }
        } catch (Exception e) {
            if (getDebug()) {
                wowidbt.log("Error reading JSON file");
            }
        }
    }

    public static void safeInit(boolean async) {
        if (async) {
            Async.SafeInitAsync();
            return;
        }
        if (jsonObject == null) {
            if (getDebug()) {
                wowidbt.log("config file not loaded...");
            }
            initializeJson();
        } else {
            reloadJsonIfModified();
        }
    }

    private static void reloadJsonIfModified() {
        File file = new File(CONFIG_FILE_PATH);
        if (!file.exists()) {
            file.getParentFile().mkdirs();
        } else {
            try (FileReader reader = new FileReader(CONFIG_FILE_PATH)) {
                JsonObject check = JsonParser.parseReader(reader).getAsJsonObject();
                if (!check.equals(jsonObject)) {
                    if (getDebug()) {
                        wowidbt.log("Config file has been modified, reloading");
                    }
                    initializeJson();
                }
            } catch (IOException e) {
                if (getDebug()) {
                    wowidbt.log("Error reading JSON file");
                }
            }
        }
    }

    public static boolean getDebug() {
        return Optional.ofNullable(jsonObject)
                .map(obj -> obj.getAsJsonObject("global_settings"))
                .map(settings -> settings.get("debug"))
                .map(JsonElement::getAsBoolean)
                .orElse(false);
    }

    public static List<String> getCreativeFlight() {
        return getListFromJson("global_settings", "creative_flight");
    }

    public static List<String> getFlyingDisabledDims() {
        return getListFromJson("global_settings", "Flight_Disabled_dims");
    }

    private static List<String> getListFromJson(String parentKey, String childKey) {
        safeInit(true);
        return Optional.ofNullable(jsonObject)
                .map(obj -> obj.getAsJsonObject(parentKey))
                .map(settings -> settings.getAsJsonArray(childKey))
                .map(array -> array.asList().stream().map(JsonElement::getAsString).collect(Collectors.toList()))
                .orElse(Collections.emptyList());
    }

    public static Map<String, Double> getDimVariance(String dimension) {
        return getMapFromJson("dimensions", dimension, "variance");
    }

    public static Double getGlobalVariance(String attribute) {
        return getDoubleFromJson("global_settings", "global_variance", attribute);
    }

    public static Map<String, Double> getTierVariance(int tier) {
        return getMapFromJson("tiers", String.valueOf(tier), "variance");
    }

    public static Map<String, Double> getOverrideVariance(String dimension, String mob) {
        return getMapFromJson("dimensions", dimension, "overrides", mob, "variance");
    }

    public static Map<String, Double> getGlobalOverrideVariance(String mob) {
        return getMapFromJson("global_overrides", mob, "variance");
    }

    public static Map<String, Double> getTierOverrideVariance(int tier, String mob) {
        return getMapFromJson("tiers", String.valueOf(tier), "overrides", mob, "variance");
    }

    private static Map<String, Double> getMapFromJson(String... keys) {
        safeInit(true);
        return Optional.ofNullable(jsonObject)
                .map(obj -> {
                    JsonObject current = obj;
                    for (String key : keys) {
                        current = current.getAsJsonObject(key);
                        if (current == null) return null;
                    }
                    return current;
                })
                .map(jsonObj -> jsonObj.entrySet().stream().collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().getAsDouble()
                )))
                .orElse(Collections.emptyMap());
    }

    private static Double getDoubleFromJson(String parentKey, String childKey, String attribute) {
        safeInit(true);
        return Optional.ofNullable(jsonObject)
                .map(obj -> obj.getAsJsonObject(parentKey))
                .map(settings -> settings.getAsJsonObject(childKey))
                .map(variance -> variance.get(attribute))
                .map(JsonElement::getAsDouble)
                .orElse(0.0);
    }

    public static @NotNull Boolean globalAttributeHasVariance(String attribute) {
        return hasVariance("global_settings", "global_variance", attribute);
    }

    public static @NotNull Boolean attributeHasDimVariance(String dimension, String attribute) {
        return hasVariance("dimensions", dimension, "variance", attribute);
    }

    public static @NotNull Boolean tierAttributeHasVariance(int tier, String attribute) {
        return hasVariance("tiers", String.valueOf(tier), "variance", attribute);
    }

    public static @NotNull Boolean mobHasGlobalOverrideVariance(String mob, String attribute) {
        return hasVariance("global_overrides", mob, "variance", attribute);
    }

    public static @NotNull Boolean overrideHasVariance(String dimension, String mob, String attribute) {
        return hasVariance("dimensions", dimension, "overrides", mob, "variance", attribute);
    }

    public static @NotNull Boolean tierOverrideHasVariance(int tier, String mob, String attribute) {
        return hasVariance("tiers", String.valueOf(tier), "overrides", mob, "variance", attribute);
    }

    private static @NotNull Boolean hasVariance(String... keys) {
        safeInit(true);
        if (getDebug()) {
            wowidbt.log("hasVariance called with keys: " + Arrays.toString(keys));
        }
        return Optional.ofNullable(jsonObject)
                .map(obj -> {
                    JsonObject current = obj;
                    for (String key : keys) {
                        if (getDebug()) {
                            wowidbt.log("Checking key: " + key);
                        }
                        if (!current.has(key)) {
                            if (getDebug()) {
                                wowidbt.log("Key not found: " + key);
                            }
                            return false;
                    }
                        if (current.has(key) && key.equals(keys[keys.length - 1])) {
                            if (getDebug()) {
                                wowidbt.log("variance definition found for: " + key);
                            }
                            return true;
                        }
                        JsonElement element = current.get(key);

                        if (!element.isJsonObject()) {
                            if (getDebug()) {
                                wowidbt.log("Element is not a JsonObject: " + key);
                            }
                            return false;
                        }
                        current = element.getAsJsonObject();
                    }
                    return true;
                })
                .orElse(false);
    }

    public static Set<String> getDimensions() {
        safeInit(true);
        return Optional.ofNullable(jsonObject)
                .map(obj -> obj.getAsJsonObject("dimensions"))
                .map(JsonObject::keySet)
                .orElse(Collections.emptySet());
    }

    public static boolean isJsonInitialized() {
        return jsonObject != null;
    }

    public static boolean areTiersDefined() {
        return hasKey("tiers");
    }

    public static boolean dimHasTier(String dimension) {
        return jsonObject.getAsJsonObject("dimensions").getAsJsonObject(dimension).has("tier");
    }
    public static Map<Integer, Map<String, Double>> getTiers() {
        safeInit(true);
        return Optional.ofNullable(jsonObject)
                .map(obj -> obj.getAsJsonObject("tiers"))
                .orElseThrow().entrySet().stream().collect(Collectors.toMap(
                        entry -> Integer.parseInt(entry.getKey()),
                        entry -> entry.getValue().getAsJsonObject().entrySet().stream().collect(Collectors.toMap(
                                Map.Entry::getKey,
                                e -> e.getValue().getAsDouble()
                        ))
                ));
    }

    private static boolean hasKey(String... keys) {
        safeInit(true);
        if (getDebug()) {
            wowidbt.log("hasKey called with keys: " + Arrays.toString(keys));
        }
        return Optional.ofNullable(jsonObject)
                .map(obj -> {
                    JsonObject current = obj;
                    for (String key : keys) {
                        if (getDebug()) {
                            wowidbt.log("Checking key: " + key);
                        }
                        JsonElement element = current.get(key);
                        if (!(element instanceof JsonObject)) {
                            if (getDebug()) {
                                wowidbt.log("Key not found or not a JsonObject: " + key);
                            }
                            return false;
                        }
                        current = element.getAsJsonObject();
                    }
                    return true;
                })
                .orElse(false);
    }

    public static @NotNull Map<String, Double> getTierAttributes(int tier) {
        return getMapFromJson("tiers", String.valueOf(tier), "attributes");
    }

    public static @NotNull Map<Integer, Map<String, Double>> getTierOverrides() {
        safeInit(true);
        return Optional.ofNullable(jsonObject)
                .map(obj -> obj.getAsJsonObject("tiers"))
                .map(tiers -> tiers.entrySet().stream().collect(Collectors.toMap(
                        entry -> Integer.parseInt(entry.getKey()),
                        entry -> entry.getValue().getAsJsonObject().entrySet().stream().collect(Collectors.toMap(
                                Map.Entry::getKey,
                                e -> e.getValue().getAsDouble()
                        ))
                )))
                .orElse(Collections.emptyMap());
    }

    public static Map<String, Double> getGlobalOverrideAttributes(String mob) {
        return getMapFromJson("global_overrides", mob, "attributes");
    }

    public static Boolean mobHasGlobalOverrides(String mob) {
        return hasKey("global_overrides", mob);
    }

    public static Boolean hasTierOverrides(int tier) {
        return hasKey("tiers", String.valueOf(tier), "overrides");
    }

    public static Boolean hasTierAttributes(int tier) {
        return hasKey("tiers", String.valueOf(tier), "attributes");
    }

    public static boolean hasGlobalOverrides() {
        return hasKey("global_overrides");
    }

    public static Map<String, Double> getTierAttributes(String dimension) {
        return getTierAttributes(getTier(dimension));
    }

    public static boolean hasAttributes(String dimension) {
        return hasKey("dimensions", dimension, "attributes");
    }

    public static Map<String, Float> getAttributes(String dimension) {
        return getMapFromJson("dimensions", dimension, "attributes").entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().floatValue()));
    }

    public static Map<String, Float> getOverrides(String dimension, String mob) {
        return getMapFromJson("dimensions", dimension, "overrides", mob, "attributes").entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().floatValue()));
    }

    public static Integer getTier(String dimension) {
        return jsonObject.getAsJsonObject("dimensions").getAsJsonObject(dimension).get("tier").getAsInt();
    }

    public static boolean hasOverrides(String dimension) {
        return hasKey("dimensions", dimension, "overrides");
    }

    public static boolean doesMobHaveOverrides(String dimension, String mob) {
        return hasKey("dimensions", dimension, "overrides", mob);
    }

    public static boolean isBlackListDefined() {
        File file = new File("config/wowid/entity_blacklist.json");
        return file.exists();
    }

    public static void loadBlackList() {
        File file = new File("config/wowid/entity_blacklist.json");
        if (MobEventRegister.attributeBlacklist == null) {
            MobEventRegister.attributeBlacklist = new HashMap<>();
        }
        try (FileReader reader = new FileReader(file)) {
            JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
            for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
                JsonArray jsonArray = entry.getValue().getAsJsonArray();
                List<String> list = new ArrayList<>();
                for (JsonElement element : jsonArray) {
                    list.add(element.getAsString());
                }
                MobEventRegister.attributeBlacklist.put(entry.getKey(), list);
            }
        } catch (IOException e) {
            wowidbt.log("Error reading blacklist from entity_blacklist.json: " + e.getMessage());
        }
    }
    public static void writeBlackListToFile(Map<String, List<String>> blackList) {
        File file = new File("config/wowid/entity_blacklist.json");
        JsonObject json = new JsonObject();

        for (Map.Entry<String, List<String>> entry : blackList.entrySet()) {
            JsonArray jsonArray = new JsonArray();
            for (String string : entry.getValue()) {
                jsonArray.add(string);
            }
            json.add(entry.getKey(), jsonArray);
        }

        try (FileWriter writer = new FileWriter(file)) {
            writer.write(json.toString());
            if (getDebug()) {
                wowidbt.log("Blacklist written to entity_blacklist.json");
            }
        } catch (IOException e) {
            if (getDebug()) {
                wowidbt.log("Error writing blacklist to entity_blacklist.json: " + e.getMessage());
            }
        }
    }
    public static List<Tier> loadTiers() {
         for (Map.Entry<String, JsonElement> entry : jsonObject.getAsJsonObject("weapon_tuning").getAsJsonObject("tiers").entrySet()) {
             JsonObject tier = entry.getValue().getAsJsonObject();
             TierRegistry.register(new Tier(entry.getKey(), TierType.WEAPON));
             TierRegistry.getTierByName(entry.getKey()).setMultiplier(tier.get("multiplier").getAsDouble());
             for (JsonElement setElement : tier.getAsJsonArray("sets")) {
                 JsonObject set = setElement.getAsJsonObject();
                 TierRegistry.getTierByName(entry.getKey()).addSet(new TierSet(setElement.getAsString(), TierSetType.MELEE_WEAPON));
                 TierRegistry.getTierByName(entry.getKey()).getSetByName(setElement.getAsString()).setMultiplier(set.get("multiplier").getAsDouble());
                 for (JsonElement attributeElement : set.getAsJsonArray("items")) {
                     JsonObject item = attributeElement.getAsJsonObject();
                     TierRegistry.getTierByName(entry.getKey()).getSetByName(setElement.getAsString()).addItem(item.getAsString());
                 }
             }
         }
         return TierRegistry.getTierList();
    }
}
record GetRecord(boolean logItems) {
    public static final MapCodec<GetRecord> CODEC = CommentMapCodec.of(RecordCodecBuilder.mapCodec(inst -> inst.group(
            Codec.BOOL.fieldOf("log_items").forGetter(GetRecord::logItems)
    ).apply(inst, GetRecord::new)), Map.of(
            "log_items", """
            Indicates whether to log all items' properties. Only non-default data components will be logged.
            Default data components are: {
                        "minecraft:max_stack_size": 64,
                        "minecraft:lore": [],
                        "minecraft:enchantments": {
                            "levels": {},
                            "show_in_tooltip": true
                        },
                        "minecraft:repair_cost": 0,
                        "minecraft:attribute_modifiers": {
                            "modifiers": [],
                            "show_in_tooltip": true
                        },
                        "minecraft:rarity": "common"
                    }
                   \s"""
    ));
    public static final GetRecord DEFAULT = new GetRecord(false);

    public void process() {
        ConfigResults.RECORD = this;
    }

    public void run() {
        if (logItems) {
            logItemProperties();
        }
    }

    private void logItemProperties() {
        wowidbt.LOGGER.info("Item Properties:");
        for (Item item : BuiltInRegistries.ITEM) {
            StringBuilder sb = new StringBuilder(BuiltInRegistries.ITEM.getKey(item).toString());
            sb.append(": ");
            ItemStack stack = new ItemStack(item);
            removeDefaultComponents(stack);
            if (!stack.getComponents().isEmpty()) {
                String json = ConfigHelper.encodeJson(DataComponentMap.CODEC, stack.getComponents()).toJson();
                sb.append("Default Components: ").append(json).append(", ");
            }
            if (!stack.getCraftingRemainingItem().isEmpty()) {
                sb.append("Crafting Remaining Item: ").append(stack.getCraftingRemainingItem().getItemHolder().getKey().location()).append(", ");
            }
            wowidbt.LOGGER.info(sb.substring(0, sb.length() - 2));
        }
    }

    private void removeDefaultComponents(ItemStack stack) {
        if (stack.has(DataComponents.MAX_STACK_SIZE) &&
                stack.get(DataComponents.MAX_STACK_SIZE) != null &&
                stack.get(DataComponents.MAX_STACK_SIZE) == 64) {
            stack.remove(DataComponents.MAX_STACK_SIZE);
        }
        if (stack.has(DataComponents.LORE) &&
                stack.get(DataComponents.LORE) != null &&
                stack.get(DataComponents.LORE) == ItemLore.EMPTY) {
            stack.remove(DataComponents.LORE);
        }
        if (stack.has(DataComponents.ENCHANTMENTS) &&
                stack.get(DataComponents.ENCHANTMENTS) != null &&
                stack.get(DataComponents.ENCHANTMENTS) == ItemEnchantments.EMPTY) {
            stack.remove(DataComponents.ENCHANTMENTS);
        }
        if (stack.has(DataComponents.REPAIR_COST) &&
                stack.get(DataComponents.REPAIR_COST) != null &&
                stack.get(DataComponents.REPAIR_COST) == 0) {
            stack.remove(DataComponents.REPAIR_COST);
        }
        if (stack.has(DataComponents.ATTRIBUTE_MODIFIERS) &&
                stack.get(DataComponents.ATTRIBUTE_MODIFIERS) != null &&
                stack.get(DataComponents.ATTRIBUTE_MODIFIERS) == ItemAttributeModifiers.EMPTY) {
            stack.remove(DataComponents.ATTRIBUTE_MODIFIERS);
        }
        if (stack.has(DataComponents.RARITY) &&
                stack.get(DataComponents.RARITY) != null &&
                stack.get(DataComponents.RARITY) == Rarity.COMMON) {
            stack.remove(DataComponents.RARITY);
        }
    }
}