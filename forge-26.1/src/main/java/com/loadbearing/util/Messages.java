package com.loadbearing.util;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public final class Messages {
    private Messages() {}

    public static void tell(Player player, Component message, boolean overlay) {
        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.sendSystemMessage(message, overlay);
        } else {
            player.sendSystemMessage(message);
        }
    }
}
