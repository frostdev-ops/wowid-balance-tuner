package com.frostdev.wowidbt.util.encounters;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public record IncomingDamage(double damage, DamageSource type, LivingEntity source, double currentHealth, Player target) {


    public JsonElement toJson(){
        JsonObject json = new JsonObject();
        json.addProperty("damage", damage);
        json.addProperty("type", type.toString());
        json.addProperty("source", source.getDisplayName().getString());
        json.addProperty("current_health", currentHealth);
        json.addProperty("target", target.getDisplayName().getString());
        return json;
    }
}
