package com.frostdev.wowidbt.util.tier;

import java.util.ArrayList;
import java.util.List;

public class TierRegistry {
    private TierRegistry() {
        // Prevent instantiation
    }

    private static final Tier TIER = new Tier("Common", TierType.COMMON);

    private static final List<Tier> TIER_LIST = new ArrayList<>();

    public static Tier register(Tier tier) {
        // Register the tier
        if (!TIER_LIST.contains(tier)) {
            TIER_LIST.add(tier);
        }
        // loopback the tier
        return tier;
    }


    public static void unregister(Tier tier) {
        // Unregister the tier
        TIER_LIST.remove(tier);
    }

    public static List<Tier> getTierList() {
        return TIER_LIST;
    }

    public static Tier getTierByName(String name) {
        // Get the tier by name
        for (Tier tier : TIER_LIST) {
            if (tier.getName().equals(name)) {
                return tier;
            }
        }
        return TIER;
    }

    @Override
    public String toString() {
        return "TierRegistry{" +
                "TIER_LIST=" + TIER_LIST +
                '}';
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }
}
