package com.frostdev.wowidbt.util.modify;


import blue.endless.jankson.Jankson;
import com.frostdev.wowidbt.wowidbt;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.world.item.Item;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.neoforged.neoforge.event.ModifyDefaultComponentsEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeModificationEvent;
import org.jline.reader.SyntaxError;

import java.io.IOException;
import java.util.List;
import java.util.Map;


public final class Modifier {
    public static final Jankson JANKSON = new Jankson.Builder().build();


    public static void entityAttributeModification(EntityAttributeModificationEvent event) {
        try {
            ConfigBootstrap.init();
        } catch (IOException e) {
            wowidbt.log("Error reading config file, no changes will be applied");
        } catch (SyntaxError e) {
            wowidbt.log("Malformed config file found, no changes will be applied");
        } catch (blue.endless.jankson.api.SyntaxError e) {
            throw new RuntimeException(e);
        }
    }
    
    public static void modifyDefaultComponents(ModifyDefaultComponentsEvent event) {
        for (Map.Entry<Item, List<DataComponentMap>> entry : ConfigResults.DEFAULT_COMPONENTS.entrySet()) {
            Item item = entry.getKey();
            List<DataComponentMap> list = entry.getValue();
            event.modify(item, builder -> list.stream().flatMap(DataComponentMap::stream).forEach(builder::set));
        }
    }

    public static void loadComplete(FMLLoadCompleteEvent event) {
        ConfigResults.CORE_CONFIG.run();
    }
}
