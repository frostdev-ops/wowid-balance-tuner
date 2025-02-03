package com.frostdev.wowidbt.util;

import com.frostdev.wowidbt.event.DimEventRegister;
import com.frostdev.wowidbt.wowidbt;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.AirBlock;
import net.minecraft.world.level.block.Block;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static com.frostdev.wowidbt.util.Getter.*;
import static net.neoforged.neoforge.common.NeoForgeMod.CREATIVE_FLIGHT;

public class Async {

    public static Future<?> setInNoFlyZone(Player player) {
        return getExecutor().scheduleAtFixedRate(whileInDim(player), 0, 2, java.util.concurrent.TimeUnit.SECONDS);
    }

    public static Future<?> setToFly(Player player) {
        return getExecutor().scheduleAtFixedRate(whileEquipped(player), 0, 5, java.util.concurrent.TimeUnit.SECONDS);
    }

    public static void setHealthAsync(LivingEntity entity) {
        setHealthAfterSpawn(entity);
    }


    private static void setHealthAfterSpawn(LivingEntity entity) {
        getExecutor().schedule(() -> {
            if (entity.getHealth() != entity.getMaxHealth()) {
                entity.setHealth(entity.getMaxHealth());
            }
        }, 1, java.util.concurrent.TimeUnit.SECONDS);
    }
    private static Runnable whileInDim(Player player) {
        return () ->{
            wowidbt.log("Start of no fly zone tick for player " + player.getName());
            if (player.level().getBlockState(player.blockPosition().below()).getBlock().getName().toString().contains("void")){
                return;
            }
            wowidbt.log("Player Vertical Speed: " + Getter.getEntitySpeed(player).get(Direction.Axis.Y).toString());
            if (Getter.getEntitySpeed(player).get(Direction.Axis.Y) < 0.0 && !player.isFallFlying()) {
                return;
            }
            Objects.requireNonNull(player.getAttributes().getInstance(CREATIVE_FLIGHT)).setBaseValue(0.0);
            player.stopFallFlying();
            player.onUpdateAbilities();
            BlockPos pos = player.blockPosition();
            Block block = player.level().getBlockState(pos).getBlock();
            for (int i = 1; i <= 5; i++) {
                Block testBlock = player.level().getBlockState(pos.below(i)).getBlock();
                if (!(testBlock instanceof AirBlock)) {
                    return;
                }
            }


            while (block instanceof AirBlock){
                pos = pos.below(1);
                block = player.level().getBlockState(pos).getBlock();
                wowidbt.log(block.getName() + " at " + pos.getY());
                if (!(block instanceof AirBlock)){
                    player.teleportTo(pos.above().getX(), pos.above().getY(), pos.above().getZ());
                    player.displayClientMessage(net.minecraft.network.chat.Component.literal("Flying is not allowed here!").withColor(11141120), true); // Send player message
                    break;
                }
            }
        };
    }


    private static Runnable whileEquipped(Player player) {
        return () -> {
            if (DimEventRegister.noFlyZoneTasks.containsKey(player)) {
                return;
            }
            if (player.getInventory().armor.stream().anyMatch(item -> Getter.getCreativeFlight().contains(item.getItem().toString()))) {
                Objects.requireNonNull(player.getAttributes().getInstance(CREATIVE_FLIGHT)).setBaseValue(1.0);
                player.onUpdateAbilities();
            }
        };
    }
    public static void SafeInitAsync() {
        safeInit();
    }
    private static void safeInit() {
        getAsyncExecutor().execute(() -> {
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
        });
    }


    public static void cancelAllTasks() {
        getExecutor().shutdown();
        getAsyncExecutor().shutdown();
    }

    private static java.util.concurrent.ScheduledExecutorService getExecutor() {
        return Executors.newSingleThreadScheduledExecutor();
    }

    private static ExecutorService getAsyncExecutor() {
        return Executors.newVirtualThreadPerTaskExecutor();
    }
}
