package com.frostdev.wowidbt;

import com.frostdev.wowidbt.event.MobEventRegister;
import com.frostdev.wowidbt.util.Async;
import com.frostdev.wowidbt.util.config.Getter;
import com.mojang.logging.LogUtils;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import org.slf4j.Logger;

import java.io.File;
import java.io.FileWriter;

import static com.frostdev.wowidbt.wowidbt.MODID;

@Mod(value = MODID, dist = Dist.DEDICATED_SERVER)
public class wowidbt
{

    public static final String MODID = "wowidbt";
    public static final Logger LOGGER = LogUtils.getLogger(); //logger
    public static void log (String message) {
        LOGGER.info(LOG_PREFIX + "{}", message);}

    private static final String LOG_PREFIX = "[WOWID Balance Tuner] ";
    public wowidbt(IEventBus bus, ModContainer modContainer){
        log("Getting Things moving....");
        bus.addListener(this::commonSetup);
        Getter.safeInit(false);
        log("Getter initialized: " + Getter.isJsonInitialized());
        NeoForge.EVENT_BUS.register(this);
    }

    private void commonSetup(final FMLCommonSetupEvent event)
    {
        log("Common Setup: Creating Combat Log");

        // Check if combatlog.json exists and archive it
        File combatLogFile = new File("logs/combatlog.json");
        if (combatLogFile.exists()) {
            File archiveFile = new File("logs/combatlog_" + System.currentTimeMillis() + ".json");
            if (combatLogFile.renameTo(archiveFile)) {
                log("Archived combatlog.json to " + archiveFile.getName());
            } else {
                log("Failed to archive combatlog.json");
            }
        }

        // Create a new empty combatlog.json file
        try (FileWriter writer = new FileWriter(combatLogFile)) {
            writer.write("{}");
            log("Created new empty combatlog.json");
        } catch (Exception e) {
            log("Failed to create new combatlog.json: " + e.getMessage());
        }
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event)
    {
        if (Getter.getDebug()){
            log("---- Debug mode enabled ----");
        }
        if (!Getter.getCreativeFlight().isEmpty()){
            for (String item : Getter.getCreativeFlight()){
                log("Creative flight item: " + item);
            }
        }
        if (!Getter.getFlyingDisabledDims().isEmpty()){
            for (String dim : Getter.getFlyingDisabledDims()){
                log("No fly zone dimension: " + dim);
            }
        }
        if (!Getter.getDimensions().isEmpty()){
            for (String dim : Getter.getDimensions()){
                log("Dimension: " + dim);
                if (Getter.dimHasTier(dim)){
                    log("Tier: " + Getter.getTier(dim));
                }
                if(Getter.hasAttributes(dim)){
                    for (String attr : Getter.getAttributes(dim).keySet()){
                        log("Attribute: " + attr);
                    }
                }
            }
        }
        if (Getter.isBlackListDefined()) {
            log("AutoBlacklist is defined");
            Getter.loadBlackList();
            log("AutoBlacklist loaded");
        }


    }
    @SubscribeEvent
    public void onServerStopping(ServerStoppingEvent event){
        if (!MobEventRegister.attributeBlacklist.isEmpty()) {
            log("Writing AutoBlacklist to file");
            Getter.writeBlackListToFile(MobEventRegister.attributeBlacklist);
        }

        // Archive the combatlog.json file
        File combatLogFile = new File("logs/combatlog.json");
        if (combatLogFile.exists()) {
            File archiveFile = new File("logs/combatlog_" + System.currentTimeMillis() + ".json");
            if (combatLogFile.renameTo(archiveFile)) {
                log("Archived combatlog.json to " + archiveFile.getName());
            } else {
                log("Failed to archive combatlog.json");
            }
        }

        Async.cancelAllTasks();
        wowidbt.log("All tasks cancelled");
        wowidbt.log("Bye bye!");
    }

}
