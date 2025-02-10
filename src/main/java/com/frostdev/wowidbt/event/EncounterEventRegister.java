package com.frostdev.wowidbt.event;

import com.frostdev.wowidbt.util.encounters.Encounter;
import com.frostdev.wowidbt.util.encounters.EncounterManager;
import com.frostdev.wowidbt.util.encounters.IncomingDamage;
import com.frostdev.wowidbt.util.encounters.OutgoingDamage;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingHealEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import org.jetbrains.annotations.NotNull;

@EventBusSubscriber
public class EncounterEventRegister {
    @SubscribeEvent
    public static void onPlayerDamage(@NotNull LivingIncomingDamageEvent event) {
        if (event.getSource().getEntity() instanceof Player) {
            handleOutgoingDamage(event);
        }else if (event.getEntity() instanceof  Player){
            handleIncomingDamage(event);
        }
    }
    @SubscribeEvent
    public static void onEntityHeal(@NotNull LivingHealEvent healEvent) {
            if (healEvent.getEntity() instanceof LivingEntity livingEntity) {
                if (livingEntity instanceof Player && EncounterManager.hasEncounter((Player) livingEntity)) {
                    handlePlayerHealing(healEvent);
                } else if (EncounterManager.hasEncounter(livingEntity)) {
                    handleEntityHealing(healEvent);
                }
            }
    }

    @SubscribeEvent
    public static void onEntityDeath(@NotNull LivingDeathEvent event) {
        if (event.getEntity() instanceof Player) {
            handlePlayerDeath(event);
        } else if (event.getEntity() instanceof LivingEntity) {
            handleEntityDeath(event);
        }
    }

    private static void handleOutgoingDamage(LivingIncomingDamageEvent event){
        try {
            if (event.getSource().getEntity() instanceof Player player){
                if (EncounterManager.hasEncounter(player) ){
                    if (EncounterManager.getEncounter(player).encounteredEntity(event.getEntity())){
                        EncounterManager.getEncounter(player).addOutgoingDamage(new OutgoingDamage(event.getAmount(), event.getSource(), event.getEntity(), player.getHealth(), player));
                    }else {
                        EncounterManager.getEncounter(player).addEncounterEntity(event.getEntity());
                        EncounterManager.getEncounter(player).addOutgoingDamage(new OutgoingDamage(event.getAmount(), event.getSource(), event.getEntity(), player.getHealth(), player));
                    }
                }else {
                    EncounterManager.addEncounter(player, new Encounter(player) {{
                        addOutgoingDamage(new OutgoingDamage(event.getAmount(), event.getSource(), event.getEntity(), player.getHealth(), player));
                    }});
                }
            }
        }catch (Exception e){
            return;
        }


    }

    private static void handleIncomingDamage(@NotNull LivingIncomingDamageEvent event){
        Player player = (Player) event.getEntity();
        double damage = event.getAmount();
        double health = player.getHealth();
        if (EncounterManager.hasEncounter(player)){
            if (EncounterManager.getEncounter(player).encounteredEntity((LivingEntity) event.getSource().getEntity())){
                EncounterManager.getEncounter(player).addIncomingDamage(new IncomingDamage(damage, event.getSource(), (LivingEntity) event.getSource().getEntity(), health, player));
            }else {
                EncounterManager.getEncounter(player).addEncounterEntity((LivingEntity) event.getSource().getEntity());
                EncounterManager.getEncounter(player).addIncomingDamage(new IncomingDamage(damage, event.getSource(), (LivingEntity) event.getSource().getEntity(), health,  player));
            }
        }else {
            EncounterManager.addEncounter(player, new Encounter(player) {{
                addIncomingDamage(new IncomingDamage(damage, event.getSource(), (LivingEntity) event.getSource().getEntity(), health, player));
            }});
        }
    }

    private static void handlePlayerHealing(@NotNull LivingHealEvent event){
        Player player = (Player) event.getEntity();
        if (EncounterManager.hasEncounter(player)){
            EncounterManager.getEncounter(player).addHealingReceived(event.getAmount());
        }else {
            EncounterManager.addEncounter(player, new Encounter(player) {{
                addHealingReceived(event.getAmount());
            }});
        }
    }

    private static void handleEntityHealing(@NotNull LivingHealEvent event){
        LivingEntity entity = (LivingEntity) event.getEntity();
        if (EncounterManager.hasEncounter(entity)){
            for (Encounter encounter : EncounterManager.getEncounter(entity)){
                encounter.addEntityHealing(event.getAmount(),entity);
            }
        }
    }

    private static void handlePlayerDeath(@NotNull LivingDeathEvent event){
        Player player = (Player) event.getEntity();
        if (EncounterManager.hasEncounter(player)){
            EncounterManager.submitEncounter(EncounterManager.getEncounter(player));
        }
    }

    private static void handleEntityDeath(@NotNull LivingDeathEvent event){
        LivingEntity entity = (LivingEntity) event.getEntity();
        if (EncounterManager.hasEncounter(entity)){
            for (Encounter encounter : EncounterManager.getEncounter(entity)){
                encounter.removeEncounterEntity(entity);
            }
        }
    }

}
