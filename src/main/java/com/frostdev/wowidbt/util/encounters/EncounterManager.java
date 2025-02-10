package com.frostdev.wowidbt.util.encounters;

import com.google.gson.*;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class EncounterManager {

    private static final Map<Player, Encounter> encounters = new ConcurrentHashMap<>();
    private static final Map<Player, JsonArray> encounterData = new ConcurrentHashMap<>();

    public static void addEncounter(Player player, Encounter encounter) {
        encounters.put(player, encounter);
    }

    public static void removeEncounter(Player player) {
        if (encounters.containsKey(player)) {
            submitEncounter(encounters.get(player));
        }
    }

    public static boolean hasEncounter(Player player) {
        return encounters.containsKey(player);
    }

    public static Encounter getEncounter(Player player) {
        return encounters.get(player);
    }

    public static boolean hasEncounter(LivingEntity entity) {
        return encounters.values().stream().anyMatch(encounter -> encounter.encounteredEntity(entity));
    }

    public static List<Encounter> getEncounter(LivingEntity entity) {
        List<Encounter> foundEncounters = new ArrayList<>();
        encounters.values().forEach(encounter -> {
            if (encounter.encounteredEntity(entity)) {
                foundEncounters.add(encounter);
            }
        });
        return foundEncounters;
    }

    public static void submitEncounter(Encounter encounter) {
        Runnable submit = () -> {
            encounterData.put(encounter.player, encounter.serialize());
            encounters.remove(encounter.player);
            File file = new File("logs/combatlog.json");
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            JsonObject root;

            // Load existing file contents or create a new JsonObject
            try (FileReader reader = new FileReader(file)) {
                root = file.exists() ? JsonParser.parseReader(reader).getAsJsonObject() : new JsonObject();
            } catch (IOException e) {
                root = new JsonObject();
            }

            String playerKey = encounter.player.getStringUUID();
            JsonArray encounterArray = encounter.serialize();
            JsonObject playerObject = root.has(playerKey) ? root.getAsJsonObject(playerKey) : new JsonObject();
            root.add(playerKey, playerObject);
            playerObject.add(encounter.encounterID.toString(), encounterArray);

            // Write the updated root object back to the file
            try (FileWriter writer = new FileWriter(file)) {
                gson.toJson(root, writer);
            } catch (IOException e) {
                e.printStackTrace();
            }

            String summary = String.format(
                    "Encounter Summary: Total Damage Taken: %,.1f\nTotal Damage Done: %,.1f\nEntities Killed: %,d\nTotal Healed: %,.1f\nDuration: %,d seconds",
                    encounter.getTotalDamageTaken(),
                    encounter.getTotalDamageDealt(),
                    encounter.getEntitiesKilled(),
                    encounter.getTotalHealingReceived(),
                    encounter.getEncounterDurationInSeconds()
            );
            encounter.player.sendSystemMessage(Component.literal(summary).withStyle(ChatFormatting.BOLD));
            encounter.player.sendSystemMessage(Component.literal("DPS: ").withStyle(ChatFormatting.GREEN).withStyle(ChatFormatting.BOLD).append(String.format("%,.1f", encounter.getEncounterDPS())).withStyle(ChatFormatting.RED).withStyle(ChatFormatting.BOLD));
        };

        submit.run();
    }
}