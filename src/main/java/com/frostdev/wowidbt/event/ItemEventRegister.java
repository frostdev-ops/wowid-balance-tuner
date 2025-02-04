
package com.frostdev.wowidbt.event;

import com.frostdev.wowidbt.util.Async;
import com.frostdev.wowidbt.util.Getter;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingEquipmentChangeEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Future;

import static net.neoforged.neoforge.common.NeoForgeMod.CREATIVE_FLIGHT;

@EventBusSubscriber(modid = "wowidbt")
public class ItemEventRegister {
    private static final Map<Player, Future<?>> creativeFlightPlayers = new HashMap<>();

    @SubscribeEvent
    public static void onItemRegister(LivingEquipmentChangeEvent event) {
        if (event.getEntity() instanceof Player player) {
            boolean hasCreativeFlightItem = Getter.getCreativeFlight().contains(event.getTo().getItem().toString());
            boolean hadCreativeFlightItem = Getter.getCreativeFlight().contains(event.getFrom().getItem().toString());

            if (hasCreativeFlightItem || hadCreativeFlightItem) {
                handleItemFlightLogic(player, hasCreativeFlightItem);
            }
        }
    }




    private static void handleItemFlightLogic(Player player, boolean flyEnabled) {
        if (!flyEnabled && creativeFlightPlayers.containsKey(player)) {
            flyEnabled = player.getInventory().armor.stream()
                    .map(ItemStack::getItem)
                    .map(Object::toString)
                    .anyMatch(Getter.getCreativeFlight()::contains);

            if (!flyEnabled) {
                creativeFlightPlayers.get(player).cancel(true);
                creativeFlightPlayers.remove(player);
            }
        }

        if (DimEventRegister.noFlyZoneTasks.containsKey(player)) {
            disableFlight(player);
        } else {
            if (flyEnabled) {
                enableFlight(player);
            } else {
                disableFlight(player);
            }
        }
    }

    private static void enableFlight(Player player) {
        creativeFlightPlayers.put(player, Async.setToFly(player));
        Objects.requireNonNull(player.getAttributes().getInstance(CREATIVE_FLIGHT)).setBaseValue(1.0);
        player.onUpdateAbilities();
    }

    private static void disableFlight(Player player) {
        Objects.requireNonNull(player.getAttributes().getInstance(CREATIVE_FLIGHT)).setBaseValue(0.0);
        player.onUpdateAbilities();
        if (creativeFlightPlayers.containsKey(player)) {
            creativeFlightPlayers.get(player).cancel(true);
            creativeFlightPlayers.remove(player);
        }
    }
}