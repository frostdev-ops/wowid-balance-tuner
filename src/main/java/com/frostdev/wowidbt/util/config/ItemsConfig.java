package com.frostdev.wowidbt.util.config;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.lukebemish.codecextras.comments.CommentMapCodec;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

public record ItemsConfig(
        Map<String, DataComponentMap> defaultComponents,
        Map<String, Item> craftingRemainders
) {
    public static final MapCodec<ItemsConfig> CODEC = CommentMapCodec.of(RecordCodecBuilder.mapCodec(inst -> inst.group(
            ConfigHelper.codecStringMap(DataComponentMap.CODEC).fieldOf("default_components").forGetter(ItemsConfig::defaultComponents),
            ConfigHelper.codecStringMap(BuiltInRegistries.ITEM.byNameCodec()).fieldOf("crafting_remainders").forGetter(ItemsConfig::craftingRemainders)
    ).apply(inst, ItemsConfig::new)), Map.of(
            "default_components", """
        Define the item components here, using the item name (or a regex pattern for item names) as the key. For instance:
        ".*": {"minecraft:max_stack_size": 64}
        This example sets the stack size of all (non-damageable) items to 64. Note that this will merge with existing components, overwriting any conflicting values.
        Changes to this map require a game restart to take effect.""",
            "crafting_remainders", """
        Define crafting remainder items for other items here, using the item name (or a regex pattern for item names) as the key. For instance:
        "minecraft:.*_(soup|stew)": "minecraft:bowl"
        This example makes all Minecraft soup and stew items return a bowl when used in crafting.
        Changes to this map require a game restart to take effect."""
    ));
    public static final ItemsConfig DEFAULT = new ItemsConfig(Map.of(), Map.of());

    public void process() {
        processConfig("default_components", defaultComponents(), (item, value) -> {
            List<DataComponentMap> list = ConfigResults.DEFAULT_COMPONENTS.computeIfAbsent(item, k -> new ArrayList<>());
            list.add(value);
        });
        processConfig("crafting_remainders", craftingRemainders(), ConfigResults.CRAFTING_REMAINDERS::put);
    }

    private static <T> void processConfig(String configName, Map<String, T> configValue, BiConsumer<Item, T> consumer) {
        ConfigHelper.processConfigMap(configValue, BuiltInRegistries.ITEM, consumer, key -> "Key " + key + " for " + configName + " in items.json5 did not match any items");
    }
}