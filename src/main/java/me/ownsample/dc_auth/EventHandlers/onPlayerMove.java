package me.ownsample.dc_auth.EventHandlers;

import me.ownsample.dc_auth.dc_auth;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public record onPlayerMove(dc_auth pl) implements Listener {

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerMoveEvent(PlayerMoveEvent event) {
        if (pl.frozen_players.contains(event.getPlayer()))
            event.setCancelled(true);
    }
}
