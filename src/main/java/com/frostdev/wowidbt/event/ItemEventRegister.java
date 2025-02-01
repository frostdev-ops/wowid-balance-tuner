package com.frostdev.wowidbt.event;

import com.frostdev.wowidbt.util.Async;
import com.frostdev.wowidbt.util.Getter;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingEquipmentChangeEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;


@EventBusSubscriber(modid = "wowidbt")
public class ItemEventRegister {
    private static final Map<Player, Future<?>> creativeFlightPlayers = new HashMap<>();

    @SubscribeEvent
    public static void onItemRegister(LivingEquipmentChangeEvent event) {
        if (event.getEntity() instanceof Player player) {
            if (Getter.getCreativeFlight().contains(event.getTo().getItem().toString())){
                handleItemFlightLogic(player, true);
            }else if (Getter.getCreativeFlight().contains(event.getFrom().getItem().toString())){
                handleItemFlightLogic(player, false);
            }
        }
    }


    private static void handleItemFlightLogic(Player player, boolean flyEnabled) {
        if (!flyEnabled) {
            if (creativeFlightPlayers.containsKey(player)) {
                flyEnabled = player.getInventory().armor.stream()
                        .map(ItemStack::getItem)
                        .map(Object::toString)
                        .anyMatch(Getter.getCreativeFlight()::contains);
                if (!flyEnabled) {
                    creativeFlightPlayers.get(player).cancel(true);
                    creativeFlightPlayers.remove(player);
                }
            }
        }
        // If player is in no-fly zone,cancel the task
         if (DimEventRegister.noFlyZoneTasks.containsKey(player)) {
            player.getAbilities().mayfly = false;
            player.onUpdateAbilities();
            if (creativeFlightPlayers.containsKey(player)) {
                creativeFlightPlayers.get(player).cancel(true);
                creativeFlightPlayers.remove(player);
            }
            
        } else {
            for (ItemStack item : player.getInventory().armor.stream().toList()) {
                // If player has creative flight item, set to fly
                if (Getter.getCreativeFlight().contains(item.getItem().toString())) {
                    creativeFlightPlayers.put(player, Async.setToFly(player));
                    flyEnabled = true;
                    break;
                }
            }
             player.getAbilities().mayfly = flyEnabled;
             player.onUpdateAbilities();
        }
        
    }
}
