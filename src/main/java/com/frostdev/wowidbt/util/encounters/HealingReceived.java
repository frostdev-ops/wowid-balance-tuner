package com.frostdev.wowidbt.util.encounters;

import net.minecraft.world.entity.player.Player;

public record HealingReceived(double healing, String source, Player target) {
}
