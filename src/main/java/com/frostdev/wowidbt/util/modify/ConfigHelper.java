package com.frostdev.wowidbt.util.modify;

import blue.endless.jankson.JsonElement;
import com.frostdev.wowidbt.wowidbt;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.UnboundedMapCodec;
import dev.lukebemish.codecextras.compat.jankson.JanksonOps;
import net.minecraft.core.Registry;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;


public final class ConfigHelper {
    public static <T> JsonElement encodeJson(Codec<T> codec, T input) {
        return codec.encodeStart(JanksonOps.COMMENTED, input).getOrThrow();
    }

    public static <T> T decodeJson(Codec<T> codec, JsonElement input) {
        return codec.decode(JanksonOps.COMMENTED, input).map(Pair::getFirst).getOrThrow();
    }

    public static <T> JsonElement encodeJson(MapCodec<T> codec, T input) {
        return encodeJson(codec.codec(), input);
    }

    public static <T> T decodeJson(MapCodec<T> codec, JsonElement input) {
        return decodeJson(codec.codec(), input);
    }

    public static <T> UnboundedMapCodec<String, T> codecStringMap(Codec<T> codec) {
        return Codec.unboundedMap(Codec.STRING, codec);
    }

    public static <R, T> void processConfigMap(Map<String, T> configValue, Registry<R> registry, BiConsumer<R, T> consumer, @Nullable Function<String, String> emptyListMessage) {
        processConfigMap(configValue, key -> resolveRegex(key, registry), consumer, emptyListMessage);
    }

    public static <R, T> void processConfigMap(Map<String, T> configValue, Function<String, List<R>> registryGetter, BiConsumer<R, T> consumer, @Nullable Function<String, String> emptyListMessage) {
        for (Map.Entry<String, T> entry : configValue.entrySet()) {
            String key = entry.getKey();
            T value = entry.getValue();
            List<R> list = registryGetter.apply(key);
            if (list.isEmpty() && emptyListMessage != null) {
                wowidbt.log(emptyListMessage.apply(key));
            }
            for (R r : list) {
                consumer.accept(r, value);
            }
        }
    }

    public static <T> List<T> resolveRegex(String regex, Registry<T> registry) {
        return registry.stream()
                .map(registry::getKey)
                .filter(Objects::nonNull)
                .filter(e -> e.toString().matches(regex))
                .map(registry::get)
                .toList();
    }
}
