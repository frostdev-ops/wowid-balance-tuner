package com.frostdev.wowidbt.util.encounters;

import com.google.gson.*;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EncounterManager {

    private static final Map<Player, Encounter> encounters = new HashMap<>();
    private static final Map<Player, JsonArray> encounterData = new HashMap<>();

    public static void addEncounter(Player player, Encounter encounter){
        encounters.put(player, encounter);
    }

    public static void removeEncounter(Player player){
        if (encounters.containsKey(player)){
            submitEncounter(encounters.get(player));
        }
    }


    public static boolean hasEncounter(Player player){
        return encounters.containsKey(player);
    }

    public static Encounter getEncounter(Player player){
        return encounters.get(player);
    }

    public static boolean hasEncounter(LivingEntity entity){
        for (Encounter encounter : encounters.values()){
            if (encounter.encounteredEntity(entity)){
                return true;
            }
        }
        return false;
    }

    public static List<Encounter> getEncounter(LivingEntity entity){
        List<Encounter> foundEncounters = new ArrayList<>();
        for (Encounter encounter : encounters.values()){
            if (encounter.encounteredEntity(entity)){
                foundEncounters.add(encounter);
            }
        }
        return foundEncounters;
    }

    public static void submitEncounter(Encounter encounter){
        Runnable submit = () -> {
            encounterData.put(encounter.player, encounter.serialize());
            encounters.remove(encounter.player);
            File file = new File("logs/combatlog.json");
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            JsonObject root;

            // Load existing file contents or create a new JsonObject
            if (file.exists()) {
                try (FileReader reader = new FileReader(file)) {
                    root = JsonParser.parseReader(reader).getAsJsonObject();
                } catch (Exception e) {
                    root = new JsonObject();
                }
            } else {
                root = new JsonObject();
                file.getParentFile().mkdirs();
            }

            String playerKey = encounter.player.getDisplayName().getString();
            JsonArray encounterArray = encounter.serialize();
            JsonObject playerObject;

            // If the player's key exists, get its JsonObject; otherwise create a new one
            if (root.has(playerKey)) {
                playerObject = root.getAsJsonObject(playerKey);
            } else {
                playerObject = new JsonObject();
                root.add(playerKey, playerObject);
            }

            // Add the encounter array under the encounterID as a key
            playerObject.add(encounter.encounterID.toString(), encounterArray);

            // Write the updated root object back to the file
            try (FileWriter writer = new FileWriter(file)) {
                gson.toJson(root, writer);
            } catch (Exception e) {
                e.printStackTrace();
            }
            String summary = String.format(
                "Encounter Summary: Total Damage Taken: %,.1f, Total Damage Done: %,.1f, Entities Killed: %,d, Total Healed: %,.1f, Duration: %,d seconds",
                encounter.getTotalDamageTaken(),
                encounter.getTotalDamageDealt(),
                encounter.getEntitiesKilled(),
                encounter.getTotalHealingReceived(),
                encounter.getEncounterDurationInSeconds()
            );
            encounter.player.sendSystemMessage(Component.literal(summary).withStyle(ChatFormatting.BOLD));
            encounter.player.sendSystemMessage(Component.literal("DPS: ").withStyle(ChatFormatting.GREEN).append(String.format("%,.1f", encounter.getEncounterDPS())).withStyle(ChatFormatting.RED));
        };

        submit.run();
    }





}
