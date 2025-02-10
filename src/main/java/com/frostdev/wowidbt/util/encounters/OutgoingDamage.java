package com.frostdev.wowidbt.util.encounters;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public record OutgoingDamage(double damage, DamageSource type, LivingEntity target, double targetHealth, Player source) {
    public JsonElement toJson(){
        JsonObject json = new JsonObject();
        json.addProperty("damage", damage);
        json.addProperty("type", type.toString());
        json.addProperty("target", target.getDisplayName().getString());
        json.addProperty("target_health", targetHealth);
        json.addProperty("source", source.getDisplayName().getString());
        return json;
    }
}



