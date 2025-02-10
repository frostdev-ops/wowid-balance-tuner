package com.frostdev.wowidbt.util.config;


import blue.endless.jankson.Jankson;
import com.frostdev.wowidbt.util.tier.Tier;
import com.frostdev.wowidbt.util.tier.TierRegistry;
import com.frostdev.wowidbt.util.tier.TierSet;
import com.frostdev.wowidbt.wowidbt;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.neoforged.neoforge.event.ModifyDefaultComponentsEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeModificationEvent;
import org.jline.reader.SyntaxError;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.frostdev.wowidbt.wowidbt.log;

@Mod(wowidbt.MODID)
public final class Modifier {
    public Modifier(IEventBus bus, ModContainer modContainer) {
        log("Getting Things moving....");
        bus.addListener(this::commonSetup);
        log("Common setup done");
        bus.addListener(EventPriority.LOWEST, EntityAttributeModificationEvent.class, Modifier::entityAttributeModification);
        log("Entity Attribute Modification done");
        bus.addListener(EventPriority.LOWEST, ModifyDefaultComponentsEvent.class,     Modifier::modifyDefaultComponents);
        log("Modify Default Components done");
        bus.addListener(EventPriority.LOWEST, FMLLoadCompleteEvent.class,             Modifier::loadComplete);
        log("Load Complete");
    }
    public static final Jankson JANKSON = new Jankson.Builder().build();

    private void commonSetup(final FMLCommonSetupEvent event)
    {

    }
    public static void entityAttributeModification(EntityAttributeModificationEvent event) {
        try {
            ConfigBootstrap.init();
        } catch (IOException e) {
            log("Error reading config file, no changes will be applied");
        } catch (SyntaxError e) {
            log("Malformed config file found, no changes will be applied");
        } catch (blue.endless.jankson.api.SyntaxError e) {
            throw new RuntimeException(e);
        }
    }
    private static final List<Item> weapons = new ArrayList<>();
    public static List<Item> getWeapons() {
        if (weapons.isEmpty()){
            for (Tier tier : TierRegistry.getTierList()) {
                for (TierSet set : tier.getSets()) {
                    weapons.addAll(set.getItems());
                }
            }
        }
        return weapons;
    }
    
    public static void modifyDefaultComponents(ModifyDefaultComponentsEvent event) {
        for (Map.Entry<Item, List<DataComponentMap>> entry : ConfigResults.DEFAULT_COMPONENTS.entrySet()) {
            Item item = entry.getKey();
            List<DataComponentMap> list = entry.getValue();
            event.modify(item, builder -> list.stream().flatMap(DataComponentMap::stream).forEach(builder::set));
        }
    }

    public static void loadComplete(FMLLoadCompleteEvent event) {
        ConfigResults.RECORD.run();
    }
}
