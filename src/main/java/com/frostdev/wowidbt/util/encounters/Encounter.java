package com.frostdev.wowidbt.util.encounters;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Encounter {
    Player player;
    Map<Instant, IncomingDamage> incomingDamage = new ConcurrentHashMap<>();
    Map<Instant, OutgoingDamage> outgoingDamage = new ConcurrentHashMap<>();
    Map<Instant, Float> healingReceived = new ConcurrentHashMap<>();
    Map<Instant, String> encounterKills = new ConcurrentHashMap<>();
    List<LivingEntity> encounterEntities = Collections.synchronizedList(new ArrayList<>());
    Map<Instant, Map<String, Double>> entityHealing = new ConcurrentHashMap<>();
    private float prevHeal = 0F;
    UUID encounterID;
    private Timer timeoutTimer;

    public Encounter(Player player) {
        this.player = player;
        this.encounterID = UUID.randomUUID();
    }

    private void resetTimeout(long delay) {
        if (timeoutTimer != null) {
            timeoutTimer.cancel();
        }
        timeoutTimer = new Timer(true);
        timeoutTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                EncounterManager.submitEncounter(Encounter.this);
            }
        }, delay);
    }

    public void updateDamageActivity() {
        resetTimeout(30000L);
    }

    public void addIncomingDamage(IncomingDamage damage) {
        updateDamageActivity();
        incomingDamage.put(Instant.now(), damage);
    }

    public void addOutgoingDamage(OutgoingDamage damage) {
        updateDamageActivity();
        outgoingDamage.put(Instant.now(), damage);
    }

    public void addHealingReceived(float healing) {
        if (healing == prevHeal) {
            return;
        }
        prevHeal = healing;
        healingReceived.put(Instant.now(), healing);
    }

    public void addEntityHealing(float healing, LivingEntity entity) {
        entityHealing.put(Instant.now(), Map.of(entity.getDisplayName().getString(), (double) healing));
    }

    public void addEncounterEntity(LivingEntity entity) {
        encounterEntities.add(entity);
    }

    public void removeEncounterEntity(LivingEntity entity) {
        updateDamageActivity();
        encounterKills.put(Instant.now(), entity.getName().getString());
        encounterEntities.remove(entity);
        if (encounterEntities.isEmpty()) {
            resetTimeout(1000L);
        }
    }

    public boolean encounteredEntity(LivingEntity entity) {
        return encounterEntities.contains(entity);
    }

    public int getEntitiesKilled() {
        return encounterKills.size();
    }

    public double getTotalDamageTaken() {
        return incomingDamage.values().stream().mapToDouble(IncomingDamage::damage).sum();
    }

    public double getTotalDamageDealt() {
        return outgoingDamage.values().stream().mapToDouble(OutgoingDamage::damage).sum();
    }

    public float getTotalHealingReceived() {
        return healingReceived.values().stream().reduce(0f, Float::sum);
    }

    public long getEncounterDurationInSeconds() {
        if (incomingDamage.isEmpty() && outgoingDamage.isEmpty() && healingReceived.isEmpty() && encounterKills.isEmpty() && entityHealing.isEmpty()) {
            return 0L;
        }
        Instant firstEvent = Collections.min(Arrays.asList(
                incomingDamage.keySet().stream().min(Instant::compareTo).orElse(Instant.now()),
                outgoingDamage.keySet().stream().min(Instant::compareTo).orElse(Instant.now()),
                healingReceived.keySet().stream().min(Instant::compareTo).orElse(Instant.now()),
                encounterKills.keySet().stream().min(Instant::compareTo).orElse(Instant.now()),
                entityHealing.keySet().stream().min(Instant::compareTo).orElse(Instant.now())
        ));
        Instant lastEvent = Collections.max(Arrays.asList(
                incomingDamage.keySet().stream().max(Instant::compareTo).orElse(Instant.now()),
                outgoingDamage.keySet().stream().max(Instant::compareTo).orElse(Instant.now()),
                healingReceived.keySet().stream().max(Instant::compareTo).orElse(Instant.now()),
                encounterKills.keySet().stream().max(Instant::compareTo).orElse(Instant.now()),
                entityHealing.keySet().stream().max(Instant::compareTo).orElse(Instant.now())
        ));
        return lastEvent.getEpochSecond() - firstEvent.getEpochSecond();
    }

    public double getEncounterDPS() {
        return getTotalDamageDealt() / getEncounterDurationInSeconds();
    }

    public JsonArray serialize() {
        JsonArray eventsArray = new JsonArray();
        List<Map.Entry<Instant, JsonElement>> allEvents = new ArrayList<>();

        incomingDamage.forEach((instant, damage) -> {
            JsonObject event = new JsonObject();
            event.addProperty("type", "incoming");
            event.addProperty("timestamp", instant.toEpochMilli());
            event.add("data", damage.toJson());
            allEvents.add(new AbstractMap.SimpleEntry<>(instant, event));
        });
        outgoingDamage.forEach((instant, damage) -> {
            JsonObject event = new JsonObject();
            event.addProperty("type", "outgoing");
            event.addProperty("timestamp", instant.toEpochMilli());
            event.add("data", damage.toJson());
            allEvents.add(new AbstractMap.SimpleEntry<>(instant, event));
        });
        healingReceived.forEach((instant, healing) -> {
            JsonObject event = new JsonObject();
            event.addProperty("type", "healing");
            event.addProperty("timestamp", instant.toEpochMilli());
            event.addProperty("amount", healing);
            allEvents.add(new AbstractMap.SimpleEntry<>(instant, event));
        });
        encounterKills.forEach((instant, entity) -> {
            JsonObject event = new JsonObject();
            event.addProperty("type", "kill");
            event.addProperty("timestamp", instant.toEpochMilli());
            event.addProperty("entity", entity);
            allEvents.add(new AbstractMap.SimpleEntry<>(instant, event));
        });
        entityHealing.forEach((instant, healingMap) -> {
            JsonObject event = new JsonObject();
            event.addProperty("type", "entityHealing");
            event.addProperty("timestamp", instant.toEpochMilli());
            JsonObject data = new JsonObject();
            healingMap.forEach(data::addProperty);
            event.add("data", data);
            allEvents.add(new AbstractMap.SimpleEntry<>(instant, event));
        });

        allEvents.sort(Map.Entry.comparingByKey());
        allEvents.forEach(entry -> eventsArray.add(entry.getValue()));

        return eventsArray;
    }
}