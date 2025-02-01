package com.frostdev.wowidbt.event;

import com.frostdev.wowidbt.util.Async;
import com.frostdev.wowidbt.util.Getter;
import com.frostdev.wowidbt.wowidbt;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

import static com.frostdev.wowidbt.util.Logg.*;

@EventBusSubscriber(modid = "wowidbt")
public class DimEventRegister {

    public static final Map<Player, Future<?>> noFlyZoneTasks = new HashMap<>();
    private static final List<Player> creativeFlightPlayers = new ArrayList<>();

    public static List<Player> getCreativeFlightPlayers() {
        return creativeFlightPlayers;
    }

    @SubscribeEvent
    public static void onDimChange(PlayerEvent.PlayerChangedDimensionEvent event) {
        handleNoFlyZoneLogic(event.getEntity(), event.getEntity().level(), event);
    }

    @SubscribeEvent
    public static void onDimLogin(PlayerEvent.PlayerLoggedInEvent event) {
        handleNoFlyZoneLogic(event.getEntity(), event.getEntity().level(), event);
    }
    @SubscribeEvent
    public static void onDimLogout(PlayerEvent.PlayerLoggedOutEvent event){
        handleNoFlyZoneLogic(event.getEntity(), event.getEntity().level(), event);
    }
    @SubscribeEvent
    public static void onDimRespawn(PlayerEvent.PlayerRespawnEvent event){
        if (!Getter.getFlyingDisabledDims().contains(Getter.getDimName(event.getEntity().level()))){
            handleLeaveDim(event.getEntity(), event.getEntity().level(), event);
        } else {
            handleNoFlyZoneLogic(event.getEntity(), event.getEntity().level(), event);
        }

    }
    @SubscribeEvent public static void onPlayerDeath(LivingDeathEvent event){
        if (event.getEntity() instanceof Player player){
            if (noFlyZoneTasks.containsKey(player)){
                handleLeaveDim(player, player.level(), event);
            }
        }
    }

    private static void handleNoFlyZoneLogic(Player player, Level level, PlayerEvent event) {
        if (Getter.getFlyingDisabledDims().isEmpty()) {
            return;
        }
        if (Getter.getFlyingDisabledDims().contains(Getter.getDimName(level))) {
            switch (event.getClass().getSimpleName()) {
                case "PlayerChangedDimensionEvent", "PlayerLoggedInEvent": 
                    wowidbt.log(player.getName().getString() + "Joined DIM: " + Getter.getDimName(level));
                    handleJoinDim(player, level);
                    break;
                case "PlayerLoggedOutEvent":
                    wowidbt.log("Player logged out");
                    handleLeaveDim(player, level, event);
                    break;
                case "PlayerRespawnEvent":
                    wowidbt.log("Player respawned");
                    handleJoinDim(player, level);
                    break;
            }
        } else if (noFlyZoneTasks.containsKey(player)) {
            noFlyZoneTasks.get(player).cancel(true);
            noFlyZoneTasks.remove(player);
        }
    }

    private static void handleLeaveDim(Player player, Level level, Event event) {
        if (Getter.getFlyingDisabledDims().contains(Getter.getDimName(level)) && !event.getClass().getSimpleName().equals("LivingDeathEvent")) {
            return;
        }
        if (creativeFlightPlayers.contains(player)) {
            player.getAbilities().mayfly = true;
            player.onUpdateAbilities();
            creativeFlightPlayers.remove(player);
            wowidbt.log(LOG_LEFT_NFZ);
        }
        if (noFlyZoneTasks.containsKey(player)) {
            wowidbt.log("Player left DIM, cancelling task: " + noFlyZoneTasks.get(player).hashCode());
            noFlyZoneTasks.get(player).cancel(true);
            noFlyZoneTasks.remove(player);
        }
    }

    private static void handleJoinDim(Player player, Level level) {
        if (Getter.getFlyingDisabledDims().contains(Getter.getDimName(level))){
            if (player.mayFly()) {
                creativeFlightPlayers.add(player);
                wowidbt.log(LOG_ADDED_TO_FLIGHT_LIST);
            }
            player.getAbilities().mayfly = false;
            player.onUpdateAbilities();
            wowidbt.log(LOG_SET_TO_NFZ);
            if (!noFlyZoneTasks.containsKey(player)) {
                noFlyZoneTasks.put(player, Async.setInNoFlyZone(player));
            }
            wowidbt.log(LOG_SCHEDULED_TASK_SET_TO_NFZ);
        } else {
            if (noFlyZoneTasks.containsKey(player)) {
                noFlyZoneTasks.get(player).cancel(true);
                noFlyZoneTasks.remove(player);
                wowidbt.log(LOG_LEFT_NFZ);
                if (creativeFlightPlayers.contains(player)) {
                    player.getAbilities().mayfly = true;
                    player.onUpdateAbilities();
                    creativeFlightPlayers.remove(player);
                }
            }
        }
    }
}