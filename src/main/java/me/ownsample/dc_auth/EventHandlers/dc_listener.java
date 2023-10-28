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
        for (Map.Entry<Long, String> e:pl.link_q.entrySet()) {
            if (e.getKey() == btn_event.getUser().getIdLong()){
                pl.getLogger().info(e.getKey().toString());
                pl.getLogger().info(e.getValue());
                String sql = "INSERT INTO auth VALUES ("+ e.getKey() + ",\"" + e.getValue()+"\")";
                pl.getLogger().info(sql);
                Statement stm = pl.con.createStatement();
                stm.executeUpdate(sql);
                stm.close();
            } else{
                pl.getLogger().severe(String.format("FAILED TO FIND ID: %d in link_q",
                        btn_event.getUser().getIdLong()));
            }
        }
    }

    @Override
    public void onEvent(@NotNull GenericEvent event) {
        if (event instanceof ReadyEvent) {
            pl.getLogger().info("DC API is ready!");
        }
        if (event instanceof ButtonInteractionEvent btn_event){
            try {
                if (btn_event.getComponentId().equals("apr"))
                    onBtnEventHelper(btn_event, "embed.link.accept");
                else
                    onBtnEventHelper(btn_event, "embed.link.reject");
            } catch (SQLException e) {
                pl.getLogger().severe("SQLException");
                pl.getLogger().severe(e.getMessage());
            }
        }
    }
}