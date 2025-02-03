package com.frostdev.wowidbt.util.modify;

import blue.endless.jankson.JsonElement;
import blue.endless.jankson.JsonObject;
import blue.endless.jankson.api.SyntaxError;
import com.frostdev.wowidbt.util.config.CoreModifierConfig;
import com.frostdev.wowidbt.util.config.EntitiesConfig;
import com.frostdev.wowidbt.util.config.ItemsConfig;
import com.frostdev.wowidbt.wowidbt;
import com.mojang.serialization.MapCodec;
import net.neoforged.fml.loading.FMLPaths;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public final class ConfigBootstrap {
    private static final List<ConfigHolder<?>> CONFIGS = new ArrayList<>();

    public static void init() throws IOException, SyntaxError {
        ConfigHolder.add("items",    ItemsConfig.CODEC,    ItemsConfig.DEFAULT,    ItemsConfig::process);
        ConfigHolder.add("entities", EntitiesConfig.CODEC, EntitiesConfig.DEFAULT, EntitiesConfig::process);
        ConfigHolder.add("core",    CoreModifierConfig.CODEC, CoreModifierConfig.DEFAULT, CoreModifierConfig::process);
        Path directory = Path.of("config","wowid");
        if (!Files.exists(directory)) {
            Files.createDirectory(directory);
        }
        for (ConfigHolder<?> config : CONFIGS) {
            Path path = directory.resolve(config.name() + ".json5");
            Object value;
            if (!Files.exists(path)) {
                value = config.defaultValue();
                Files.createFile(path);
                Files.writeString(path, config.encodeJsonDefault().toJson(true, true));
            } else {
                    value = config.decodeJson(Modifier.JANKSON.fromJson(Files.readString(path), JsonObject.class));
            }
            config.process(value);
        }
    }

    private record ConfigHolder<T>(String name, MapCodec<T> codec, T defaultValue, Consumer<T> processor) {
        private static <T> void add(String name, MapCodec<T> codec, T defaultValue, Consumer<T> processor) {
            CONFIGS.add(new ConfigHolder<>(name, codec, defaultValue, processor));
        }

        public JsonElement encodeJson(T value) {
            return ConfigHelper.encodeJson(codec(), value);
        }

        public JsonElement encodeJsonDefault() {
            return encodeJson(defaultValue());
        }

        public T decodeJson(JsonElement value) {
            return ConfigHelper.decodeJson(codec(), value);
        }
        
        @SuppressWarnings("unchecked")
        public void process(Object object) {
            processor().accept((T) object);
        }
    }
}
