package com.frostdev.wowidbt;

import com.frostdev.wowidbt.event.MobEventRegister;
import com.frostdev.wowidbt.util.config.Getter;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MobEdit {

    private final LivingEntity livingEntity;
    private double varianceMAX = 0.0;
    private boolean negativeVariance = false;
    private boolean negativeValue = false;

    public MobEdit(Entity entity) {
        if (entity instanceof LivingEntity) {
            this.livingEntity = (LivingEntity) entity;
        } else {
            throw new IllegalArgumentException("Entity is not a living entity");
        }
    }

    public void setVariance(double variance) {
        if (variance < 0) {
            variance = -variance;
            this.negativeVariance = true;
        }
        this.varianceMAX = variance;
    }

    public void setAttribute(String attribute, double value) {
        try {
            AttributeInstance attributeInstance = livingEntity.getAttribute(BuiltInRegistries.ATTRIBUTE.getHolderOrThrow(
                    ResourceKey.create(BuiltInRegistries.ATTRIBUTE.key(), ResourceLocation.parse(attribute))));

            if (attributeInstance != null) {
                if (value < 0) {
                    value = -value;
                    this.negativeValue = true;
                }

                if (varianceMAX > 0) {
                    double varianceMIN = new Random().nextDouble(varianceMAX) / 2;
                    value += new Random().nextDouble(varianceMIN, varianceMAX);
                }

                if (negativeVariance || negativeValue) {
                    value = -value;
                    negativeVariance = false;
                    negativeValue = false;
                }

                attributeInstance.setBaseValue(value);
                if(Getter.getDebug()){
                    wowidbt.log("Attribute " + attribute + " set to " + value + " for entity " + livingEntity.getType());
                }

                if (attribute.contains("max_health")) {
                    livingEntity.setHealth((float) value);
                }
            } else {
                if (Getter.getDebug()){
                    wowidbt.log("Attribute " + attribute + " not found for entity " + livingEntity.getType() + " adding to blacklist. If you see a message again for this entity and attribute without clearing the blacklist file, something is horribly wrong. Please report it to the mod author.");
                }
                attributeBlacklistAdd(attribute);
        }

        } catch (Exception e) {
            if (Getter.getDebug()){
                wowidbt.log("Attribute " + attribute + " not found for entity " + livingEntity.getType() + " adding to blacklist. If you see a message again for this entity and attribute without clearing the blacklist file, something is horribly wrong. Please report it to the mod author.");
            }
            attributeBlacklistAdd(attribute);
        }
    }

    private void attributeBlacklistAdd(String attribute) {
        if (MobEventRegister.attributeBlacklist.containsKey(livingEntity.getType().toString())) {
            MobEventRegister.attributeBlacklist.get(livingEntity.getType().toString()).add(attribute);
        } else {
            List<String> attributes = new ArrayList<>();
            attributes.add(attribute);
            MobEventRegister.attributeBlacklist.put(livingEntity.getType().toString(), attributes);
        }
    }
}