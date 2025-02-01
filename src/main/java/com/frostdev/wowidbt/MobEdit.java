package com.frostdev.wowidbt;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;

import java.util.Random;


public class MobEdit {

        private LivingEntity livingEntity;
        private Double varianceMIN = 0.0;
        private Double varianceMAX = 0.0;
        private Boolean negativeVariance = false;
        private Boolean negatieValue = false;
        public MobEdit(Entity entity) {
            try {
                this.livingEntity = entity instanceof LivingEntity ? (LivingEntity) entity : null;
            } catch (ClassCastException e) {
                wowidbt.log("Entity is not a living entity");
            }
        }
        public void setVariance(double variance) {
            this.varianceMIN = 0.0;
            if (variance < 0) {
                variance = variance * -1;
                this.negativeVariance = true;
            }
            this.varianceMAX = variance;
        }
        public void setAttribute(String attribute, double value) {
            AttributeInstance attributeInstance = livingEntity.getAttribute(BuiltInRegistries.ATTRIBUTE.getHolderOrThrow(ResourceKey.create(BuiltInRegistries.ATTRIBUTE.key(), ResourceLocation.parse(attribute))));
            if (attributeInstance != null && livingEntity != null) {
                if (value < 0) {
                    value = value * -1;
                    this.negatieValue = true;
                }
                if (varianceMAX > 0){
                    if (new Random().nextBoolean()) {
                        varianceMIN = new Random().nextDouble(varianceMAX)/2;
                    }
                    if (varianceMAX>varianceMIN){
                        value = value + new Random().nextDouble(varianceMIN, varianceMAX);
                    }
                }
                if (negativeVariance || negatieValue) {
                    negatieValue = false;
                    negativeVariance = false;
                    value = value * -1;
                }
                attributeInstance.setBaseValue(value);
                if (attribute.contains("max_health")) {
                    livingEntity.setHealth((float) value);
                }
            } else {
                if (livingEntity != null) {
                    wowidbt.log("Attribute " + attribute + " does not exist for entity " + livingEntity.getName().getString());
                } else {
                    wowidbt.log("LivingEntity is null");
                }
            }
        }
    }

