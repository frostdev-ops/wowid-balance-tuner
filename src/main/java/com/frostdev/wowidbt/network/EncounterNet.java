package com.frostdev.wowidbt.network;

import com.frostdev.wowidbt.util.encounters.Encounter;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record EncounterNet(Encounter encounter) implements CustomPacketPayload {

    public EncounterNet {
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return null;
    }
}
