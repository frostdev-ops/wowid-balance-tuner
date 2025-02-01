package com.frostdev.wowidbt.event;

import com.frostdev.wowidbt.MobEdit;
import com.frostdev.wowidbt.util.Async;
import com.frostdev.wowidbt.util.Getter;
import com.frostdev.wowidbt.wowidbt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ambient.AmbientCreature;
import net.minecraft.world.entity.animal.frog.Frog;
import net.minecraft.world.entity.monster.MagmaCube;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

import static com.frostdev.wowidbt.util.Getter.getDimName;


@EventBusSubscriber(modid = "wowidbt")
public class MobEventRegister {

    private static final boolean debug = Getter.getDebug(); // Global debug flag

    @SubscribeEvent
    public static void onMobSpawn(EntityJoinLevelEvent event) {
        if (event.getEntity() instanceof LivingEntity && !(event.getEntity() instanceof Player) && !(event.getEntity() instanceof AmbientCreature)) {
            if (debug) wowidbt.log("EntityJoinLevelEvent triggered for entity: " + event.getEntity().getName() + "-------------------------------------------------------------");
            if (Getter.hasGlobalOverrides()){
                if(Getter.mobHasGlobalOverrides(Getter.getGenericEntityType(event.getEntity()))){
                    MobEdit editor = new MobEdit(event.getEntity());
                    try {
                        for (String attribute : Getter.getGlobalOverrideAttributes(Getter.getGenericEntityType(event.getEntity())).keySet()) {
                            if (Getter.mobHasGlobalOverrideVariance(Getter.getGenericEntityType(event.getEntity()), attribute)){
                                Double variance = Getter.getGlobalOverrideVariance(Getter.getGenericEntityType(event.getEntity())).get(attribute);
                                editor.setVariance(variance);
                            }
                            editor.setAttribute(attribute, Getter.getGlobalOverrideAttributes(Getter.getGenericEntityType(event.getEntity())).get(attribute));
                        }
                    }catch (Exception e){
                        wowidbt.log("Error setting attributes for entity: " + e.getCause());
                    }
                    Async.setHealthAsync((LivingEntity) event.getEntity()); // failsafe to ensure mobs are at full health
                    return;
                }
            }
            // Check if the dimension exists in the JSON file
            if (Getter.getDimensions().contains(getDimName(event.getLevel()))) {
                if (debug) wowidbt.log("Dimension " + getDimName(event.getLevel()) + " exists in JSON file");

                // Get generic entity type
                String mobName = Getter.getGenericEntityType(event.getEntity());
                if (debug) wowidbt.log("Generic entity type retrieved: " + mobName);

                // Check if the dimension has overrides
                if (Getter.hasOverrides(getDimName(event.getLevel()))) {
                    if (debug) wowidbt.log("Dimension " + getDimName(event.getLevel()) + " has overrides");

                    // Check if the mob has overrides
                    if (Getter.doesMobHaveOverrides(getDimName(event.getLevel()), mobName)) {
                        if (debug) wowidbt.log("Mob " + mobName + " has overrides");

                        MobEdit editor = new MobEdit(event.getEntity());
                        try {
                        for (String attribute : Getter.getOverrides(getDimName(event.getLevel()), mobName).keySet()) {
                            if (Getter.overrideHasVariance(getDimName(event.getLevel()), mobName, attribute)){
                                Double variance = Getter.getOverrideVariance(getDimName(event.getLevel()), mobName).get(attribute);
                                editor.setVariance(variance);
                            }
                            editor.setAttribute(attribute, Getter.getOverrides(getDimName(event.getLevel()), mobName).get(attribute));
                        }
                    }catch (Exception e){
                        wowidbt.log("Error setting attributes for entity: " + e.getCause());

                    }
                        Async.setHealthAsync((LivingEntity) event.getEntity()); // failsafe to ensure mobs are at full health
                        return; // return to prevent further checks
                    }
                }

                // Check if the dimension has attributes
                if (Getter.hasAttributes(getDimName(event.getLevel()))) {
                    if (debug) wowidbt.log("Dimension " + getDimName(event.getLevel()) + " has attributes");

                    MobEdit editor = new MobEdit(event.getEntity());
                    try {
                        for (String attribute : Getter.getAttributes(getDimName(event.getLevel())).keySet()) {
                        if (Getter.attributeHasDimVariance(getDimName(event.getLevel()), attribute)){
                            Double variance = Getter.getDimVariance(getDimName(event.getLevel())).get(attribute);
                            editor.setVariance(variance);
                        } else if (Getter.globalAttributeHasVariance(attribute)){
                            Double variance = Getter.getGlobalVariance(attribute);
                            editor.setVariance(variance);
                        }
                        editor.setAttribute(attribute, Getter.getAttributes(getDimName(event.getLevel())).get(attribute));
                    }
                    }catch (Exception e){
                        wowidbt.log("Error setting attributes for entity: " + e.getCause());
                    }

                } else if (Getter.areTiersDefined()) {
                    if (debug) wowidbt.log("Tiers are defined");

                    if (Getter.dimHasTiers(getDimName(event.getLevel()))) {
                        if (debug) wowidbt.log("Dimension " + getDimName(event.getLevel()) + " has tiers");

                        // Check if the tier has overrides
                        if (Getter.hasTierOverrides(Getter.getTier(getDimName(event.getLevel())))) {
                            if (debug)
                                wowidbt.log("Tier " + Getter.getTier(getDimName(event.getLevel())) + " has overrides");

                            MobEdit editor = new MobEdit(event.getEntity());
                            try {
                            for (String attribute : Getter.getTierOverrides().get(Integer.valueOf(getDimName(event.getLevel()))).keySet()) {
                                if(Getter.tierOverrideHasVariance(Getter.getTier(getDimName(event.getLevel())),Getter.getGenericEntityType(event.getEntity()) ,attribute)){
                                   Double variance = Getter.getTierOverrideVariance(Getter.getTier(getDimName(event.getLevel())), Getter.getGenericEntityType(event.getEntity())).get(attribute);
                                    editor.setVariance(variance);
                                }
                                editor.setAttribute(attribute, Getter.getTierOverrides().get(Integer.valueOf(getDimName(event.getLevel()))).get(attribute));
                            }
                        }catch (Exception e){
                            wowidbt.log("Error setting attributes for entity: " + e.getCause());
                        }
                            Async.setHealthAsync((LivingEntity) event.getEntity()); // failsafe to ensure mobs are at full health
                            return; // return to prevent further checks
                        }

                        // Check if the tier has attributes
                        if (Getter.hasTierAttributes(Getter.getTier(getDimName(event.getLevel())))) {
                            if (debug)
                                wowidbt.log("Tier " + Getter.getTier(getDimName(event.getLevel())) + " has attributes");

                            MobEdit editor = new MobEdit(event.getEntity());
                            try {
                            for (String attribute : Getter.getTierAttributes(getDimName(event.getLevel())).keySet()) {
                                if (Getter.tierAttributeHasVariance(Getter.getTier(getDimName(event.getLevel())), attribute)) {
                                    Double variance = Getter.getTierVariance(Getter.getTier(getDimName(event.getLevel()))).get(attribute);
                                    editor.setVariance(variance);
                                } else if (Getter.globalAttributeHasVariance(attribute)) {
                                    Double variance = Getter.getGlobalVariance(attribute);
                                    editor.setVariance(variance);
                                }
                                editor.setAttribute(attribute, Getter.getTierAttributes(getDimName(event.getLevel())).get(attribute));
                            }
                        }catch (Exception e){
                            wowidbt.log("Error setting attributes for entity: " + e.getCause());
                        }
                            Async.setHealthAsync((LivingEntity) event.getEntity()); // failsafe to ensure mobs are at full health
                        }
                    }
                } else {
                    if (debug) wowidbt.log("No further checks required for dimension: " + getDimName(event.getLevel()));
                    return; // return to prevent further checks
                }
            }
        }
    }

    // Special event for frogs attacking magma cubes for frog lanterns
    @SubscribeEvent
    public static void onFrogAttack(LivingIncomingDamageEvent event) {
        if (event.getSource().getEntity() instanceof Frog) {
            if (debug) wowidbt.log("Frog attack detected on entity: " + event.getEntity());

            if (event.getEntity() instanceof MagmaCube) {
                if (debug) wowidbt.log("Frog attacking MagmaCube - setting MagmaCube health to 0");
                event.getEntity().setHealth(0);
            }
        }
    }
}