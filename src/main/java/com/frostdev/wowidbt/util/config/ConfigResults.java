package com.frostdev.wowidbt.util.config;

import net.minecraft.core.component.DataComponentMap;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.item.Item;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class ConfigResults {

    public static final Map<Item, List<DataComponentMap>> DEFAULT_COMPONENTS = new HashMap<>();
    public static final Map<Item, Item> CRAFTING_REMAINDERS = new HashMap<>();
    public static final Map<EntityType<? extends LivingEntity>, Map<Attribute, Float>> ATTRIBUTE_BASE_VALUES = new HashMap<>();

    public static GetRecord RECORD;

    private ConfigResults() {
        // Private constructor to prevent instantiation
    }
}