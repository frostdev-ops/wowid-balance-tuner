package com.frostdev.wowidbt.network;

import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class Sync {

    public static void  register(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar  registrar = event.registrar("1");
    }

}
