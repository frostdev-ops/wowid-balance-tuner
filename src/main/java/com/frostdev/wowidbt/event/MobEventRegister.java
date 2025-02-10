package com.frostdev.wowidbt.event;

import com.frostdev.wowidbt.MobEdit;
import com.frostdev.wowidbt.util.Async;
import com.frostdev.wowidbt.util.config.Getter;
import com.frostdev.wowidbt.wowidbt;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ambient.AmbientCreature;
import net.minecraft.world.entity.animal.frog.Frog;
import net.minecraft.world.entity.monster.MagmaCube;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.frostdev.wowidbt.util.config.Getter.getDimName;
@EventBusSubscriber
public class MobEventRegister {

    private static boolean isDebug() {
        return Getter.getDebug();
    }

    public static Map<String, List<String>> attributeBlacklist = new HashMap<>();

    @SubscribeEvent
    public static void onMobSpawn(EntityJoinLevelEvent event) {
        Entity entity = event.getEntity();
        if (!(entity instanceof LivingEntity) || entity instanceof Player || entity instanceof AmbientCreature) {
            return;
        }
        boolean debug = isDebug();

        if (debug) {
            wowidbt.log("EntityJoinLevelEvent triggered for entity: " + entity.getName() + "-------------------------------------------------------------");
        }

        if (handleGlobalOverrides(entity)) return;

        String dimensionName = getDimName(event.getLevel());
        if (!Getter.getDimensions().contains(dimensionName)) {
            return;
        }

        if (debug) {
            wowidbt.log("Dimension " + dimensionName + " exists in JSON file");
        }

        String mobName = Getter.getGenericEntityType(entity);
        if (debug) {
            wowidbt.log("Generic entity type retrieved: " + mobName);
        }

        if (handleDimensionOverrides(entity, dimensionName, mobName)) return;
        if (handleDimensionAttributes(entity, dimensionName)) return;
        if (handleTierAttributes(entity, dimensionName)) return;

        if (debug) {
            wowidbt.log("No further checks required for dimension: " + dimensionName);
        }
    }

    private static boolean handleGlobalOverrides(Entity entity) {
        boolean debug = isDebug();
        if (debug) {
            wowidbt.log("handleGlobalOverrides called for entity: " + entity.getName());
        }
        if (!Getter.hasGlobalOverrides() || !Getter.mobHasGlobalOverrides(Getter.getGenericEntityType(entity))) {
            return false;
        }

        MobEdit editor = new MobEdit(entity);
        try {
            for (String attribute : Getter.getGlobalOverrideAttributes(Getter.getGenericEntityType(entity)).keySet()) {
                if (isAttributeBlacklisted(entity, attribute)) continue;

                if (Getter.mobHasGlobalOverrideVariance(Getter.getGenericEntityType(entity), attribute)) {
                    editor.setVariance(Getter.getGlobalOverrideVariance(Getter.getGenericEntityType(entity)).get(attribute));
                }
                editor.setAttribute(attribute, Getter.getGlobalOverrideAttributes(Getter.getGenericEntityType(entity)).get(attribute));
            }
        } catch (Exception e) {
            wowidbt.log("Error setting attributes for entity: " + e.getCause());
        }
        Async.setHealthAsync((LivingEntity) entity);
        return true;
    }

    private static boolean handleDimensionOverrides(Entity entity, String dimensionName, String mobName) {
        boolean debug = isDebug();
        if (debug) {
            wowidbt.log("handleDimensionOverrides called for entity: " + entity.getName() + " in dimension: " + dimensionName);
        }
        if (!Getter.hasOverrides(dimensionName) || !Getter.doesMobHaveOverrides(dimensionName, mobName)) {
            return false;
        }

        MobEdit editor = new MobEdit(entity);
        try {
            for (String attribute : Getter.getOverrides(dimensionName, mobName).keySet()) {
                if (isAttributeBlacklisted(entity, attribute)) continue;

                if (Getter.overrideHasVariance(dimensionName, mobName, attribute)) {
                    editor.setVariance(Getter.getOverrideVariance(dimensionName, mobName).get(attribute));
                }
                editor.setAttribute(attribute, Getter.getOverrides(dimensionName, mobName).get(attribute));
            }
        } catch (Exception e) {
            wowidbt.log("Error setting attributes for entity: " + e.getCause());
        }
        Async.setHealthAsync((LivingEntity) entity);
        return true;
    }

    private static boolean handleDimensionAttributes(Entity entity, String dimensionName) {
        boolean debug = isDebug();
        if (debug) {
            wowidbt.log("handleDimensionAttributes called for entity: " + entity.getName() + " in dimension: " + dimensionName);
        }
        if (!Getter.hasAttributes(dimensionName)) {
            return false;
        }

        MobEdit editor = new MobEdit(entity);
        try {
            for (String attribute : Getter.getAttributes(dimensionName).keySet()) {
                if (isAttributeBlacklisted(entity, attribute)) continue;

                if (Getter.attributeHasDimVariance(dimensionName, attribute)) {
                    editor.setVariance(Getter.getDimVariance(dimensionName).get(attribute));
                } else if (Getter.globalAttributeHasVariance(attribute)) {
                    editor.setVariance(Getter.getGlobalVariance(attribute));
                }
                editor.setAttribute(attribute, Getter.getAttributes(dimensionName).get(attribute));
            }
        } catch (Exception e) {
            wowidbt.log("Error setting attributes for entity: " + e.getCause());
        }
        Async.setHealthAsync((LivingEntity) entity);
        return true;
    }

    private static boolean handleTierAttributes(Entity entity, String dimensionName) {
        boolean debug = isDebug();
        if (debug) {
            wowidbt.log("handleTierAttributes called for entity: " + entity.getName() + " in dimension: " + dimensionName);
        }
        if (!Getter.areTiersDefined() || !Getter.dimHasTier(dimensionName)) {
            if (debug) {
            wowidbt.log("Tiers not defined for dimension: " + dimensionName);
            }
            return false;
        }

        int tier = Getter.getTier(dimensionName);
        if (Getter.hasTierOverrides(tier)) {
            if (debug) {
            wowidbt.log("Tier overrides found for tier: " + tier);
            }
            MobEdit editor = new MobEdit(entity);
            try {
                for (String attribute : Getter.getTierOverrides().get(tier).keySet()) {
                    if (isAttributeBlacklisted(entity, attribute)) continue;

                    if (Getter.tierOverrideHasVariance(tier, Getter.getGenericEntityType(entity), attribute)) {
                        editor.setVariance(Getter.getTierOverrideVariance(tier, Getter.getGenericEntityType(entity)).get(attribute));
                    }
                    editor.setAttribute(attribute, Getter.getTierOverrides().get(tier).get(attribute));
                }
            } catch (Exception e) {
                wowidbt.log("Error setting attributes for entity: " + e.getCause());
            }
            Async.setHealthAsync((LivingEntity) entity);
            return true;
        }

        if (Getter.hasTierAttributes(tier)) {
            if (debug) {
            wowidbt.log("Tier attributes found for tier: " + tier);
            }
            MobEdit editor = new MobEdit(entity);
            try {
                for (String attribute : Getter.getTierAttributes(dimensionName).keySet()) {
                    if (isAttributeBlacklisted(entity, attribute)){
                        continue;
                    }
                    if(debug)wowidbt.log("Checking for variance for attribute: " + attribute + " in tier: " + tier);
                    if (Getter.tierAttributeHasVariance(tier, attribute)) {
                        if (debug) wowidbt.log("Variance found for attribute: " + attribute + " in tier: " + tier);
                        editor.setVariance(Getter.getTierVariance(tier).get(attribute));
                    } else if (Getter.globalAttributeHasVariance(attribute)) {
                        wowidbt.log("Variance found for attribute: " + attribute + " in global");
                        editor.setVariance(Getter.getGlobalVariance(attribute));
                    }
                    if (debug) wowidbt.log("Setting attribute: " + attribute + " to: " + Getter.getTierAttributes(dimensionName).get(attribute));
                    editor.setAttribute(attribute, Getter.getTierAttributes(dimensionName).get(attribute));
                }
            } catch (Exception e) {
                wowidbt.log("Error setting attributes for entity: " + e);
            }
            Async.setHealthAsync((LivingEntity) entity);
            return true;
        }

        return false;
    }

    private static boolean isAttributeBlacklisted(Entity entity, String attribute) {
        boolean debug = isDebug();
        if (debug) {
            wowidbt.log("isAttributeBlacklisted called for entity: " + entity.getType().toString() + " with attribute: " + attribute);
        }
        if (attributeBlacklist.containsKey(entity.getType().toString())) {
            if (attributeBlacklist.get(entity.getType().toString()).contains(attribute)) {
                if (debug) {
                    wowidbt.log("Attribute: " + attribute + " is blacklisted for entity: " + entity.getType().toString());
                }
                return true;
            }
        }
        return false;
    }

    @SubscribeEvent
    public static void onFrogAttack(LivingIncomingDamageEvent event) {
        if (event.getSource().getEntity() instanceof Frog && event.getEntity() instanceof MagmaCube) {
            boolean debug = isDebug();
            if (debug) {
                wowidbt.log("Frog attacking MagmaCube - setting MagmaCube health to 0");
            }
            event.getEntity().setHealth(0);
        }
    }
}