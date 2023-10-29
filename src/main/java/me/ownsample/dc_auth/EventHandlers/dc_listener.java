package me.ownsample.dc_auth.EventHandlers;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import me.ownsample.dc_auth.dc_auth;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.api.requests.restaction.interactions.MessageEditCallbackAction;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class dc_listener implements EventListener {
    private final dc_auth pl;

    public dc_listener(dc_auth pl){
        this.pl = pl;
    }

    private void onBtnEventHelper(ButtonInteractionEvent btn_event, String desc_id, String type) {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle(pl.getConfig().getString("embed."+type+".title"));
        eb.setColor(new Color(pl.getConfig().getInt("embed.color")));
        eb.setThumbnail(pl.getConfig().getString("embed.image"));
        eb.setDescription(pl.getConfig().getString("embed."+type+"."+desc_id));
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
            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            try {
                if (btn_event.getComponentId().equals("apr-link")) {
                    onBtnEventHelper(btn_event, "accept", "link");
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
                            out.writeUTF("Connect");
                            out.writeUTF("main");
                            player.sendPluginMessage(pl, "BungeeCord", out.toByteArray());
                        } else {
                            pl.getLogger().severe(String.format("FAILED TO FIND ID: %d in link_q",
                                    btn_event.getUser().getIdLong()));
                        }
                    }
                } else if (btn_event.getComponentId().equals("rej-link")){
                    onBtnEventHelper(btn_event, "reject", "link");
                    for (Map.Entry<Long, String> e : pl.link_q.entrySet()) {
                        if (e.getKey() == btn_event.getUser().getIdLong()) {
                            Player player = Bukkit.getPlayer(e.getValue());
                            if (player == null)
                                pl.getLogger().warning("Failed to notify player("+e.getValue()+") about rejected " +
                                        "linking");
                            player.sendPlainMessage("Account linking has been rejected!");
                            out.writeUTF("KickPlayer");
                            out.writeUTF(player.getName());
                            out.writeUTF("Link rejected!");
                            player.sendPluginMessage(pl, "BungeeCord", out.toByteArray());
                        }
                    }
                } else if (btn_event.getComponentId().equals("apr-login")){
                    onBtnEventHelper(btn_event, "accepted", "login");
                    Statement smt = pl.con.createStatement();
                    String sql = "SELECT name FROM auth WHERE id = "+btn_event.getUser().getIdLong()+";";
                    ResultSet rs = smt.executeQuery(sql);
                    if (!rs.isBeforeFirst())
                        pl.getLogger().severe("Empty query: " + sql);
                    while (rs.next()){
                        String name = rs.getString("name");
                        Player player = Bukkit.getPlayer(name);
                        if (player == null)
                            pl.getLogger().warning("Player("+name+") logged in!");
                        player.sendPlainMessage("Logged in!");
                        out.writeUTF("Connect");
                        out.writeUTF("main");
                        player.sendPluginMessage(pl, "BungeeCord", out.toByteArray());
                    }
                    rs.close();
                    smt.close();
                }else if (btn_event.getComponentId().equals("rej-login")){
                    onBtnEventHelper(btn_event, "reject", "login");
                    Statement smt = pl.con.createStatement();
                    String sql = "SELECT name FROM auth WHERE id = "+btn_event.getUser().getIdLong()+";";
                    ResultSet rs = smt.executeQuery(sql);
                    while (rs.next()) {
                        String name = rs.getString("name");
                        Player player = Bukkit.getPlayer(name);
                        if (player == null)
                            pl.getLogger().warning("Player("+name+") login rejected in!");
                        player.sendPlainMessage("Login rejected!");
                        out.writeUTF("KickPlayer");
                        out.writeUTF(player.getName());
                        out.writeUTF("Login rejected!");
                        player.sendPluginMessage(pl, "BungeeCord", out.toByteArray());
                    }
                    rs.close();
                    smt.close();
                }
            } catch (SQLException e) {
                pl.getLogger().severe("SQLException");
                pl.getLogger().severe(e.getMessage());
            }
        }
    }
}