package com.frostdev.wowidbt.util.config;

import com.frostdev.wowidbt.util.modify.ConfigHelper;
import com.frostdev.wowidbt.util.modify.ConfigResults;
import com.frostdev.wowidbt.wowidbt;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.lukebemish.codecextras.comments.CommentMapCodec;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;

import java.util.HashMap;
import java.util.Map;

public record EntitiesConfig(
        Map<String, Map<String, Float>> attributeBaseValues
) {
    public static final MapCodec<EntitiesConfig> CODEC = CommentMapCodec.of(RecordCodecBuilder.mapCodec(inst -> inst.group(
            ConfigHelper.codecStringMap(ConfigHelper.codecStringMap(Codec.FLOAT)).fieldOf("attribute_base_values").forGetter(EntitiesConfig::attributeBaseValues)
    ).apply(inst, EntitiesConfig::new)), Map.of(
            "attribute_base_values", """
            Specify entities to set their attribute values. You can use regex patterns for attribute names. For example:
                    "minecraft:.*": {"minecraft:generic.max_health": 40}
            This will set the max health of all vanilla living entities to 40.
            Changes to this map require a game restart to take effect."""
    ));
    public static final EntitiesConfig DEFAULT = new EntitiesConfig(Map.of());

    @SuppressWarnings({"unchecked"})
    public void process() {
        ConfigHelper.processConfigMap(attributeBaseValues(), BuiltInRegistries.ENTITY_TYPE, (entityType, attributes) -> {
            try {
                EntityType<? extends LivingEntity> entity = (EntityType<? extends LivingEntity>) entityType;
                Map<Attribute, Float> attributeMap = new HashMap<>();
                ConfigHelper.processConfigMap(attributes, BuiltInRegistries.ATTRIBUTE, attributeMap::put, key -> "Key " + key + " for attribute_base_values in entities.json5 did not match any attributes");
                ConfigResults.ATTRIBUTE_BASE_VALUES.put(entity, attributeMap);
            } catch (ClassCastException e) {
                wowidbt.log("Entity " + BuiltInRegistries.ENTITY_TYPE.getId(entityType) + " is not a living entity, but was a match for a regex in attribute_base_values in entities.json5");
            }
        }, key -> "Key " + key + " for attribute_base_values in entities.json5 did not match any entities");
    }
}