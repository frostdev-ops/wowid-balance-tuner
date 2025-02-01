package com.frostdev.wowidbt.event;

import com.frostdev.wowidbt.util.Async;
import com.frostdev.wowidbt.util.Getter;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

import java.util.*;
import java.util.concurrent.Future;

import static net.neoforged.neoforge.common.NeoForgeMod.CREATIVE_FLIGHT;


@EventBusSubscriber(modid = "wowidbt")
public class DimEventRegister {

    public static final Map<Player, Future<?>> noFlyZoneTasks = new HashMap<>();
    private static final List<Player> creativeFlightPlayers = new ArrayList<>();


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
                    handleJoinDim(player, level);
                    break;
                case "PlayerLoggedOutEvent":
                    handleLeaveDim(player, level, event);
                    break;
                case "PlayerRespawnEvent":
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
            Objects.requireNonNull(player.getAttributes().getInstance(CREATIVE_FLIGHT)).setBaseValue(1.0);
            player.onUpdateAbilities();
            creativeFlightPlayers.remove(player);
        }
        if (noFlyZoneTasks.containsKey(player)) {
            noFlyZoneTasks.get(player).cancel(true);
            noFlyZoneTasks.remove(player);
        }
    }

    private static void handleJoinDim(Player player, Level level) {
        if (Getter.getFlyingDisabledDims().contains(Getter.getDimName(level))){
            if (player.mayFly()) {
                creativeFlightPlayers.add(player);
            }
            Objects.requireNonNull(player.getAttributes().getInstance(CREATIVE_FLIGHT)).setBaseValue(1.0);
            player.onUpdateAbilities();
            if (!noFlyZoneTasks.containsKey(player)) {
                noFlyZoneTasks.put(player, Async.setInNoFlyZone(player));
            }
        } else {
            if (noFlyZoneTasks.containsKey(player)) {
                noFlyZoneTasks.get(player).cancel(true);
                noFlyZoneTasks.remove(player);
                if (creativeFlightPlayers.contains(player)) {
                    Objects.requireNonNull(player.getAttributes().getInstance(CREATIVE_FLIGHT)).setBaseValue(1.0);
                    player.onUpdateAbilities();
                    creativeFlightPlayers.remove(player);
                }
            }
        }
    }
}