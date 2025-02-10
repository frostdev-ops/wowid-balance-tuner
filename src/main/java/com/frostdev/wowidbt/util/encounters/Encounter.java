package com.frostdev.wowidbt.util.encounters;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import java.sql.Time;
import java.time.Instant;
import java.util.*;

public class Encounter {
    Player player;
    Map<Date, IncomingDamage> incomingDamage = new HashMap<>();
    Map<Date, OutgoingDamage> outgoingDamage = new HashMap<>();
    Map<Date, Float> healingReceived = new HashMap<>();
    Map<Date, String> encounterKills = new HashMap<>();
    List<LivingEntity> encounterEntities = new ArrayList<>();
    Map<Date, Map<String,Double>> entityHealing = new HashMap<>();
    UUID encounterID;
    Boolean timedOut = false;
    public Encounter(Player player){
        this.player = player;
        this.encounterID = UUID.randomUUID();

    }

    private Timer timeoutTimer;

    private void resetTimeout() {
        if (timeoutTimer != null) {
            timeoutTimer.cancel();
        }
        timeoutTimer = new Timer(true);
        timeoutTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                EncounterManager.submitEncounter(Encounter.this);
            }
        }, 30000);
    }

    public void updateDamageActivity() {
        resetTimeout();
    }
    public void addIncomingDamage(IncomingDamage damage){
        updateDamageActivity();
        incomingDamage.put(Time.from(Instant.now()), damage);
    }

    public void addOutgoingDamage(OutgoingDamage damage){
        updateDamageActivity();
        outgoingDamage.put(Time.from(Instant.now()),damage);
    }

    public void addHealingReceived(float healing){
        healingReceived.put(Time.from(Instant.now()), healing);
    }

    public void addEntityHealing(float healing, LivingEntity entity){
        entityHealing.put(Time.from(Instant.now()), Map.of(entity.getDisplayName().getString(), (double) healing));
    }


    public void addEncounterEntity(LivingEntity entity){
        encounterEntities.add(entity);
    }

    public void removeEncounterEntity(LivingEntity entity){
        updateDamageActivity();
        encounterKills.put(Time.from(Instant.now()), entity.getName().getString());

        encounterEntities.remove(entity);
        if (encounterEntities.isEmpty()){
            EncounterManager.submitEncounter(this);
        }
    }

    public boolean encounteredEntity(LivingEntity entity){
       return encounterEntities.contains(entity);

    }

    public Integer getEntitiesKilled(){
        return encounterKills.size();
    }

    public Double getTotalDamageTaken(){
        return incomingDamage.values().stream().mapToDouble(IncomingDamage::damage).sum();
    }

    public Double getTotalDamageDealt(){
        return outgoingDamage.values().stream().mapToDouble(OutgoingDamage::damage).sum();
    }

    public Float getTotalHealingReceived(){
        return healingReceived.values().stream().reduce(0f, Float::sum);
    }



    public JsonArray serialize() {
        JsonArray eventsArray = new JsonArray();

        // Combine all events into a single list
        List<Map.Entry<Date, JsonElement>> allEvents = new ArrayList<>();
        incomingDamage.forEach((date, damage) -> {
            JsonObject event = new JsonObject();
            event.addProperty("type", "incoming");
            event.addProperty("timestamp", date.getTime());
            event.add("data", damage.toJson());
            allEvents.add(new AbstractMap.SimpleEntry<>(date, event));
        });
        outgoingDamage.forEach((date, damage) -> {
            JsonObject event = new JsonObject();
            event.addProperty("type", "outgoing");
            event.addProperty("timestamp", date.getTime());
            event.add("data", damage.toJson());
            allEvents.add(new AbstractMap.SimpleEntry<>(date, event));
        });
        healingReceived.forEach((date, healing) -> {
            JsonObject event = new JsonObject();
            event.addProperty("type", "healing");
            event.addProperty("timestamp", date.getTime());
            event.addProperty("amount", healing);
            allEvents.add(new AbstractMap.SimpleEntry<>(date, event));
        });
        encounterKills.forEach((date, entity) -> {
            JsonObject event = new JsonObject();
            event.addProperty("type", "kill");
            event.addProperty("timestamp", date.getTime());
            event.addProperty("entity", entity);
            allEvents.add(new AbstractMap.SimpleEntry<>(date, event));
        });
        entityHealing.forEach((date, healingMap) -> {
            JsonObject event = new JsonObject();
            event.addProperty("type", "entityHealing");
            event.addProperty("timestamp", date.getTime());
            JsonObject data = new JsonObject();
            healingMap.forEach(data::addProperty);
            event.add("data", data);
            allEvents.add(new AbstractMap.SimpleEntry<>(date, event));
        });
        // Sort events by date
        allEvents.sort(Map.Entry.comparingByKey());

        // Add sorted events to the JsonArray
        for (Map.Entry<Date, JsonElement> entry : allEvents) {
            eventsArray.add(entry.getValue());
        }
        return eventsArray;
    }

}
