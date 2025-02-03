package com.frostdev.wowidbt.util;

import com.frostdev.wowidbt.wowidbt;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.WorldGenLevel;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class Getter {
     static JsonObject jsonObject;
     static final String CONFIG_FILE_PATH = "config/wowid/wowidbt.json";

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

    static void initializeJson() {
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
        return Optional.ofNullable(jsonObject)
                .map(obj -> {
                    JsonObject current = obj;
                    for (String key : keys) {
                        current = current.getAsJsonObject(key);
                        if (current == null) return false;
                    }
                    return current.has(keys[keys.length - 1]);
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
        return jsonObject.getAsJsonObject(dimension).has("tier");
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
        return getIntFromJson("dimensions", dimension, "tier");
    }

    private static Integer getIntFromJson(String... keys) {
        safeInit(true);
        return Optional.ofNullable(jsonObject)
                .map(obj -> {
                    JsonObject current = obj;
                    for (String key : keys) {
                        current = current.getAsJsonObject(key);
                        if (current == null) return 0;
                    }
                    return current.get(keys[keys.length - 1]).getAsInt();
                })
                .orElse(0);
    }

    public static boolean hasOverrides(String dimension) {
        return hasKey("dimensions", dimension, "overrides");
    }

    public static boolean doesMobHaveOverrides(String dimension, String mob) {
        return hasKey("dimensions", dimension, "overrides", mob);
    }
}