package me.ownsample.dc_auth.EventHandlers;

import me.ownsample.dc_auth.dc_auth;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public record onJoin(dc_auth pl) implements Listener {

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        pl.getLogger().info("Authing user: " + event.getPlayer().getName());
        pl.frozen_players.add(event.getPlayer());
    }
}
