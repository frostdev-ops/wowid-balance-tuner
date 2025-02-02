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
    public static String getDimName(LevelAccessor world) {
        String dimensionName = "";
        dimensionName = "" + (world instanceof Level _lvl ? _lvl.dimension() : (world instanceof WorldGenLevel _wgl ? _wgl.getLevel().dimension() : Level.OVERWORLD));
        return dimensionName.substring(34, (int) ((dimensionName).length() - 1));
    }

    public static String getGenericEntityType(Entity entity) {
        if (entity == null) {
            return "null";
        }
        return getGeneric(entity);
    }
    private static String getGeneric(Entity entity) {
        String generic = entity.getType().toString().replaceFirst("\\.", "");
        generic = generic.replaceFirst("entity", "");
        generic = generic.replaceFirst("\\.", ":");
        return generic;
    }
    public static Map<Direction.Axis, Double> getEntitySpeed(Entity entity) {
        Map<Direction.Axis, Double> speed = new HashMap<>();
        speed.put(Direction.Axis.X, entity.getDeltaMovement().x);
        speed.put(Direction.Axis.Y, entity.getDeltaMovement().y);
        speed.put(Direction.Axis.Z, entity.getDeltaMovement().z);
        return speed;
    }

    static JsonObject jsonObject;
    static final String CONFIG_FILE_PATH = "config/wowid/wowidbt_mobs.json";

    // Initialize the JSON object from a file
    static void initializeJson() {
        File file = new File(CONFIG_FILE_PATH);
        if (!file.exists()) {
            file.getParentFile().mkdirs();
        }
        try (FileReader reader = new FileReader(CONFIG_FILE_PATH)) {
            jsonObject = JsonParser.parseReader(reader).getAsJsonObject();
            if(getDebug()){wowidbt.log("config file loaded!");}
        } catch (Exception e) {
            if(getDebug()){wowidbt.log("Error reading JSON file");}
        }
    }
    // Helper method to ensure JSON is initialized safely
    public static void safeInit()  {
        Async.SafeInitAsync();
    }
    public static void safeInit(boolean nonAsync){
        if (jsonObject == null) {
            if(getDebug()){wowidbt.log("config file not loaded...");}
            initializeJson();
        }else {
            File file = new File(CONFIG_FILE_PATH);
            if (!file.exists()) {
                file.getParentFile().mkdirs();
            }else {
                try {
                    FileReader reader = new FileReader(CONFIG_FILE_PATH);
                    JsonObject check = JsonParser.parseReader(reader).getAsJsonObject();
                    if (check.hashCode() != jsonObject.hashCode()) {
                        if(getDebug()){wowidbt.log("Config file has been modified, reloading");}
                        initializeJson();
                    }
                } catch (IOException e) {
                    if(getDebug()){wowidbt.log("Error reading JSON file");}
                }
            }
        }
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
        if (!jsonObject.getAsJsonObject("global_settings").has("variance")) {
            return false;
        }
        return jsonObject.getAsJsonObject("global_settings").getAsJsonObject("global_variance").has(attribute);
    }
    public static @NotNull Boolean attributeHasDimVariance(String dimension, String attribute) {
        safeInit();
        if (!jsonObject.getAsJsonObject("dimensions").getAsJsonObject(dimension).has("variance")) {
            return false;
        }
        return jsonObject.getAsJsonObject("dimensions").getAsJsonObject(dimension).getAsJsonObject("variance").has(attribute);
    }
    public static @NotNull Boolean tierAttributeHasVariance(int tier, String attribute) {
        safeInit();
        if (!jsonObject.getAsJsonObject("tiers").getAsJsonObject(String.valueOf(tier)).has("variance")) {
            return false;
        }
        return jsonObject.getAsJsonObject("tiers").getAsJsonObject(String.valueOf(tier)).getAsJsonObject("variance").has(attribute);
    }
    /*
    Overrides
     */
    public static @NotNull Boolean mobHasGlobalOverrideVariance(String mob, String attribute) {
        safeInit();
        if (jsonObject.getAsJsonObject("global_overrides").has(mob)) {
            return jsonObject.getAsJsonObject("global_overrides").getAsJsonObject(mob).getAsJsonObject("variance").has(attribute);
        }else {
            return false;
        }
    }
    public static @NotNull Boolean overrideHasVariance(String dimension, String mob, String attribute) {
        safeInit();
        if (jsonObject.getAsJsonObject("dimensions").getAsJsonObject(dimension).getAsJsonObject("overrides").has(mob)) {
            if (!jsonObject.getAsJsonObject("dimensions").getAsJsonObject(dimension).getAsJsonObject("overrides").getAsJsonObject(mob).has("variance")) {
                return false;
        }else{
                return jsonObject.getAsJsonObject("dimensions").getAsJsonObject(dimension).getAsJsonObject("overrides").getAsJsonObject(mob).getAsJsonObject("variance").has(attribute);
            }

    }
            return false;
    }
    public static @NotNull Boolean tierOverrideHasVariance(int tier, String mob, String attribute) {
        safeInit();
        if (jsonObject.getAsJsonObject("tiers").getAsJsonObject(String.valueOf(tier)).getAsJsonObject("overrides").has(mob)) {
            if (!jsonObject.getAsJsonObject("tiers").getAsJsonObject(String.valueOf(tier)).getAsJsonObject("overrides").getAsJsonObject(mob).has("variance")) {
                return false;
            }else{
                return jsonObject.getAsJsonObject("tiers").getAsJsonObject(String.valueOf(tier)).getAsJsonObject("overrides").getAsJsonObject(mob).getAsJsonObject("variance").has(attribute);
            }
        }
        return false;
    }

    

    public static Set<String> getDimensions() {
        safeInit();
        return jsonObject.get("dimensions").getAsJsonObject().keySet(); // Simplified
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
        Map<String, Double> attributes = new HashMap<>();
        jsonObject.getAsJsonObject("tiers").getAsJsonObject(String.valueOf(tier)).getAsJsonObject("attributes")
                .entrySet()
                .forEach(entry -> attributes.put(entry.getKey(), entry.getValue().getAsDouble()));
        return attributes;
    }
    public static @NotNull Map<Integer, Map<String, Double>> getTierOverrides() {
        safeInit();
        Map<Integer, Map<String, Double>> tiers = new HashMap<>();
        JsonObject tiersObject = jsonObject.getAsJsonObject("tiers");
        for (String key : tiersObject.keySet()) {
            tiers.put(
                    Integer.parseInt(key),
                    tiersObject.getAsJsonObject(key)
                            .entrySet()
                            .stream()
                            .collect(Collectors.toMap(
                                    Map.Entry::getKey,
                                    entry -> entry.getValue().getAsDouble()
                            ))
            );
        }
        return tiers;
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
        return jsonObject.getAsJsonObject("tiers").get(String.valueOf(tier)).getAsJsonObject().has("overrides");
    }
    public static Boolean hasTierAttributes(int tier) {
        safeInit();
        return jsonObject.getAsJsonObject("tiers").get(String.valueOf(tier)).getAsJsonObject().has("attributes");
    }
    public static boolean hasGlobalOverrides() {
        safeInit();
        return jsonObject.has("global_overrides");
    }

    public static Map<String, Double> getTierAttributes(String dimension) {
        safeInit();
        int tier = getTier(dimension);
        return getTierAttributes(tier);
    }

    public static boolean hasAttributes(String dimension) {
        safeInit();
        return jsonObject.getAsJsonObject("dimensions").get(dimension).getAsJsonObject().has("attributes");
    }

    public static Map<String, Float> getAttributes(String dimension) {
        safeInit();
        return jsonObject.getAsJsonObject("dimensions")
                .getAsJsonObject(dimension)
                .getAsJsonObject("attributes")
                .entrySet()
                .stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().getAsFloat()
                ));
    }

    public static Map<String, Float> getOverrides(String dimension, String mob) {
        safeInit();
        if (doesMobHaveOverrides(dimension, mob)) {
            return jsonObject.getAsJsonObject("dimensions")
                    .getAsJsonObject(dimension)
                    .getAsJsonObject("overrides")
                    .getAsJsonObject(mob)
                    .getAsJsonObject("attributes")
                    .entrySet()
                    .stream()
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            entry -> entry.getValue().getAsFloat()
                    ));
        }
        throw new IllegalArgumentException("Mob does not have overrides");
    }
    
    public static Integer getTier(String dimension) {
        safeInit();
        return jsonObject.getAsJsonObject("dimensions").getAsJsonObject().get(dimension).getAsJsonObject().get("tier").getAsInt();
    }

    public static boolean hasOverrides(String dimension) {
        safeInit();
        return jsonObject.getAsJsonObject("dimensions").getAsJsonObject(dimension).has("overrides");
    }

    public static boolean doesMobHaveOverrides(String dimension, String mob) {
        safeInit();
        JsonObject overridesObject = jsonObject.getAsJsonObject("dimensions")
                .getAsJsonObject(dimension)
                .getAsJsonObject("overrides");
        return overridesObject != null && overridesObject.has(mob);
    }
}