package me.ownsample.dc_auth.EventHandlers;

import me.ownsample.dc_auth.dc_auth;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.requests.restaction.interactions.MessageEditCallbackAction;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class dc_listener implements EventListener {
    private final dc_auth pl;

    public dc_listener(dc_auth pl){
        this.pl = pl;
    }

    private void onBtnEventHelper(ButtonInteractionEvent btn_event, String desc_id) throws SQLException {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle(pl.getConfig().getString("embed.link.title"));
        eb.setColor(new Color(pl.getConfig().getInt("embed.color")));
        eb.setThumbnail(pl.getConfig().getString("embed.image"));
        eb.setDescription(pl.getConfig().getString(desc_id));
        MessageEditCallbackAction msg = btn_event.editMessageEmbeds(eb.build());
        msg.queue();
        Message msg_ = btn_event.getMessage();
        msg_.delete().queueAfter(pl.getConfig().getInt("embed.delay.resp"), TimeUnit.SECONDS);

    }

    @Override
    public void onEvent(@NotNull GenericEvent event) {
        if (event instanceof ReadyEvent) {
            pl.getLogger().info("DC API is ready!");
        }
        if (event instanceof ButtonInteractionEvent btn_event){
            try {
                if (btn_event.getComponentId().equals("apr")) {
                    onBtnEventHelper(btn_event, "embed.link.accept");
                    for (Map.Entry<Long, String> e : pl.link_q.entrySet()) {
                        if (e.getKey() == btn_event.getUser().getIdLong()) {
                            String sql = "INSERT INTO auth VALUES (" + e.getKey() + ",\"" + e.getValue() + "\")";
                            Statement stm = pl.con.createStatement();
                            stm.executeUpdate(sql);
                            stm.close();
                            Player player = Bukkit.getPlayer(e.getValue());
                            if (player == null)
                                pl.getLogger().warning("Failed to notify player("+e.getValue()+") about successful" +
                                        " linking");
                            player.sendPlainMessage("Account linking has been successful!");
                        } else {
                            pl.getLogger().severe(String.format("FAILED TO FIND ID: %d in link_q",
                                    btn_event.getUser().getIdLong()));
                        }
                    }
                } else {
                    onBtnEventHelper(btn_event, "embed.link.reject");
                    for (Map.Entry<Long, String> e : pl.link_q.entrySet()) {
                        if (e.getKey() == btn_event.getUser().getIdLong()) {
                            Player player = Bukkit.getPlayer(e.getValue());
                            if (player == null)
                                pl.getLogger().warning("Failed to notify player("+e.getValue()+") about rejected " +
                                        "linking");
                            player.sendPlainMessage("Account linking has been rejected!");
                        }
                    }
                }
            } catch (SQLException e) {
                pl.getLogger().severe("SQLException");
                pl.getLogger().severe(e.getMessage());
            }
        }
    }
}