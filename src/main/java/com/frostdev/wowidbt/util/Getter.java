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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class Getter {
    static JsonObject jsonObject;
    static final String CONFIG_FILE_PATH = "config/wowid/wowidbt.json";

    public static String getDimName(LevelAccessor world) {
        String dimensionName = world instanceof Level _lvl ? _lvl.dimension().toString() :
                world instanceof WorldGenLevel _wgl ? _wgl.getLevel().dimension().toString() : Level.OVERWORLD.toString();
        return dimensionName.substring(34, dimensionName.length() - 1);
    }

    public static String getGenericEntityType(Entity entity) {
        return entity == null ? "null" : getGeneric(entity);
    }

    private static String getGeneric(Entity entity) {
        return entity.getType().toString().replaceFirst("\\.", "").replaceFirst("entity", "").replaceFirst("\\.", ":");
    }

    public static Map<Direction.Axis, Double> getEntitySpeed(Entity entity) {
        Map<Direction.Axis, Double> speed = new HashMap<>();
        speed.put(Direction.Axis.X, entity.getDeltaMovement().x);
        speed.put(Direction.Axis.Y, entity.getDeltaMovement().y);
        speed.put(Direction.Axis.Z, entity.getDeltaMovement().z);
        return speed;
    }

    static {
        initializeJson();
    }

    static void initializeJson() {
        File file = new File(CONFIG_FILE_PATH);
        if (!file.exists()) {
            file.getParentFile().mkdirs();
        }
        try (FileReader reader = new FileReader(CONFIG_FILE_PATH)) {
            jsonObject = JsonParser.parseReader(reader).getAsJsonObject();
            if (getDebug()) {
                wowidbt.log("Config file loaded!");
            }
        } catch (Exception e) {
            if (getDebug()) {
                wowidbt.log("Error reading JSON file");
            }
        }
    }

    public static void safeInit() {
        Async.SafeInitAsync();
    }

    public static boolean getDebug() {
        try {
            return jsonObject.getAsJsonObject("global_settings").get("debug").getAsBoolean();
        } catch (NullPointerException e) {
            return false;
        }
    }

    public static List<String> getCreativeFlight() {
        safeInit();
        return jsonObject.getAsJsonObject("global_settings").getAsJsonArray("creative_flight").asList().stream().map(JsonElement::getAsString).collect(Collectors.toList());
    }

    public static List<String> getFlyingDisabledDims() {
        safeInit();
        return jsonObject.getAsJsonObject("global_settings").getAsJsonArray("Flight_Disabled_dims").asList().stream().map(JsonElement::getAsString).collect(Collectors.toList());
    }

    public static Map<String, Double> getDimVariance(String dimension) {
        safeInit();
        return jsonObject.getAsJsonObject("dimensions").getAsJsonObject(dimension).getAsJsonObject("variance").entrySet().stream().collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> entry.getValue().getAsDouble()
        ));
    }

    public static Double getGlobalVariance(String attribute) {
        safeInit();
        return jsonObject.getAsJsonObject("global_settings").getAsJsonObject("global_variance").get(attribute).getAsDouble();
    }

    public static Map<String, Double> getTierVariance(int tier) {
        safeInit();
        return jsonObject.getAsJsonObject("tiers").getAsJsonObject(String.valueOf(tier)).getAsJsonObject("variance").entrySet().stream().collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> entry.getValue().getAsDouble()
        ));
    }

    public static Map<String, Double> getOverrideVariance(String dimension, String mob) {
        safeInit();
        return jsonObject.getAsJsonObject("dimensions").getAsJsonObject(dimension).getAsJsonObject("overrides").getAsJsonObject(mob).getAsJsonObject("variance").entrySet().stream().collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> entry.getValue().getAsDouble()
        ));
    }

    public static Map<String, Double> getGlobalOverrideVariance(String mob) {
        safeInit();
        return jsonObject.getAsJsonObject("global_overrides").getAsJsonObject(mob).getAsJsonObject("variance").entrySet().stream().collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> entry.getValue().getAsDouble()
        ));
    }

    public static Map<String, Double> getTierOverrideVariance(int tier, String mob) {
        safeInit();
        return jsonObject.getAsJsonObject("tiers").getAsJsonObject(String.valueOf(tier)).getAsJsonObject("overrides").getAsJsonObject(mob).getAsJsonObject("variance").entrySet().stream().collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> entry.getValue().getAsDouble()
        ));
    }

    public static @NotNull Boolean globalAttributeHasVariance(String attribute) {
        safeInit();
        return jsonObject.getAsJsonObject("global_settings").getAsJsonObject("global_variance").has(attribute);
    }

    public static @NotNull Boolean attributeHasDimVariance(String dimension, String attribute) {
        safeInit();
        return jsonObject.getAsJsonObject("dimensions").getAsJsonObject(dimension).getAsJsonObject("variance").has(attribute);
    }

    public static @NotNull Boolean tierAttributeHasVariance(int tier, String attribute) {
        safeInit();
        return jsonObject.getAsJsonObject("tiers").getAsJsonObject(String.valueOf(tier)).getAsJsonObject("variance").has(attribute);
    }

    public static @NotNull Boolean mobHasGlobalOverrideVariance(String mob, String attribute) {
        safeInit();
        JsonObject mobOverrides = jsonObject.getAsJsonObject("global_overrides").getAsJsonObject(mob);
        return mobOverrides != null && mobOverrides.getAsJsonObject("variance").has(attribute);
    }

    public static @NotNull Boolean overrideHasVariance(String dimension, String mob, String attribute) {
        safeInit();
        JsonObject mobOverrides = jsonObject.getAsJsonObject("dimensions").getAsJsonObject(dimension).getAsJsonObject("overrides").getAsJsonObject(mob);
        return mobOverrides != null && mobOverrides.getAsJsonObject("variance").has(attribute);
    }

    public static @NotNull Boolean tierOverrideHasVariance(int tier, String mob, String attribute) {
        safeInit();
        JsonObject mobOverrides = jsonObject.getAsJsonObject("tiers").getAsJsonObject(String.valueOf(tier)).getAsJsonObject("overrides").getAsJsonObject(mob);
        return mobOverrides != null && mobOverrides.getAsJsonObject("variance").has(attribute);
    }

    public static Set<String> getDimensions() {
        safeInit();
        return jsonObject.getAsJsonObject("dimensions").keySet();
    }

    public static boolean isJsonInitialized() {
        return jsonObject != null;
    }

    public static boolean areTiersDefined() {
        safeInit();
        return jsonObject.has("tiers");
    }

    public static boolean dimHasTiers(String dimension) {
        safeInit();
        return jsonObject.getAsJsonObject("dimensions").getAsJsonObject(dimension).has("tier");
    }

    public static @NotNull Map<String, Double> getTierAttributes(int tier) {
        safeInit();
        return jsonObject.getAsJsonObject("tiers").getAsJsonObject(String.valueOf(tier)).getAsJsonObject("attributes").entrySet().stream().collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> entry.getValue().getAsDouble()
        ));
    }

    public static @NotNull Map<Integer, Map<String, Double>> getTierOverrides() {
        safeInit();
        return jsonObject.getAsJsonObject("tiers").entrySet().stream().collect(Collectors.toMap(
                entry -> Integer.parseInt(entry.getKey()),
                entry -> entry.getValue().getAsJsonObject().entrySet().stream().collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> e.getValue().getAsDouble()
                ))
        ));
    }

    public static Map<String, Double> getGlobalOverrideAttributes(String mob) {
        safeInit();
        return jsonObject.getAsJsonObject("global_overrides").getAsJsonObject(mob).getAsJsonObject("attributes").entrySet().stream().collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> entry.getValue().getAsDouble()
        ));
    }

    public static Boolean mobHasGlobalOverrides(String mob) {
        safeInit();
        return jsonObject.getAsJsonObject("global_overrides").has(mob);
    }

    public static Boolean hasTierOverrides(int tier) {
        safeInit();
        return jsonObject.getAsJsonObject("tiers").getAsJsonObject(String.valueOf(tier)).has("overrides");
    }

    public static Boolean hasTierAttributes(int tier) {
        safeInit();
        return jsonObject.getAsJsonObject("tiers").getAsJsonObject(String.valueOf(tier)).has("attributes");
    }

    public static boolean hasGlobalOverrides() {
        safeInit();
        return jsonObject.has("global_overrides");
    }

    public static Map<String, Double> getTierAttributes(String dimension) {
        safeInit();
        return getTierAttributes(getTier(dimension));
    }

    public static boolean hasAttributes(String dimension) {
        safeInit();
        return jsonObject.getAsJsonObject("dimensions").getAsJsonObject(dimension).has("attributes");
    }

    public static Map<String, Float> getAttributes(String dimension) {
        safeInit();
        return jsonObject.getAsJsonObject("dimensions").getAsJsonObject(dimension).getAsJsonObject("attributes").entrySet().stream().collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> entry.getValue().getAsFloat()
        ));
    }

    public static Map<String, Float> getOverrides(String dimension, String mob) {
        safeInit();
        if (doesMobHaveOverrides(dimension, mob)) {
            return jsonObject.getAsJsonObject("dimensions").getAsJsonObject(dimension).getAsJsonObject("overrides").getAsJsonObject(mob).getAsJsonObject("attributes").entrySet().stream().collect(Collectors.toMap(
                    Map.Entry::getKey,
                    entry -> entry.getValue().getAsFloat()
            ));
        }
        throw new IllegalArgumentException("Mob does not have overrides");
    }

    public static Integer getTier(String dimension) {
        safeInit();
        return jsonObject.getAsJsonObject("dimensions").getAsJsonObject(dimension).getAsJsonObject("tier").getAsInt();
    }

    public static boolean hasOverrides(String dimension) {
        safeInit();
        return jsonObject.getAsJsonObject("dimensions").getAsJsonObject(dimension).has("overrides");
    }

    public static boolean doesMobHaveOverrides(String dimension, String mob) {
        safeInit();
        JsonObject overridesObject = jsonObject.getAsJsonObject("dimensions").getAsJsonObject(dimension).getAsJsonObject("overrides");
        return overridesObject != null && overridesObject.has(mob);
    }
}