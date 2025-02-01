package com.frostdev.wowidbt;

import com.frostdev.wowidbt.util.Async;
import com.frostdev.wowidbt.util.Getter;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartingEvent;

@Mod(wowidbt.MODID)
public class wowidbt
{

    public static final String MODID = "wowidbt";
    public static final Logger LOGGER = LogUtils.getLogger(); //logger
    public static void log (String message) {
        LOGGER.info(LOG_PREFIX + "{}", message);}

    private static final String LOG_PREFIX = "[WOWID Balance Tuner] ";
    public wowidbt(IEventBus modEventBus, ModContainer modContainer)
    {
        modEventBus.addListener(this::commonSetup);
        Getter.safeInit(true);
        log("Getter initialized: " + Getter.isJsonInitialized());
        NeoForge.EVENT_BUS.register(this);
    }

    private void commonSetup(final FMLCommonSetupEvent event)
    {
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event)
    {
        //GetRegistered.mobs();
    }
    @SubscribeEvent
    public void registerCommands(RegisterCommandsEvent event)
    {
        //event.register(new Command());
    }
    @SubscribeEvent
    public void onServerStopping(ServerStoppingEvent event){
        Async.cancelAllTasks();
        wowidbt.log("All tasks cancelled");
        wowidbt.log("Bye bye!");
    }

}
