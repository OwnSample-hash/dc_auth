package me.ownsample.dc_auth.EventHandlers;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import me.ownsample.dc_auth.dc_auth;
import static me.ownsample.dc_auth.dc_auth.jda;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.awt.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public record onJoin(dc_auth pl) implements Listener {

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        pl.getLogger().info("Authing user: " + event.getPlayer().getName());
        pl.timer.schedule(new TimerTask() {
            @Override
            public void run() {
                ByteArrayDataOutput out = ByteStreams.newDataOutput();
                out.writeUTF("KickPlayer");
                out.writeUTF(event.getPlayer().getName());
                out.writeUTF("Login timed out!");
                event.getPlayer().sendPluginMessage(pl, "BungeeCord", out.toByteArray());
            }
        }, pl.getConfig().getInt("embed.delay.no_resp") * 1000);
        pl.login_q.add(event.getPlayer().getName());
        try {
            Statement smt = pl.con.createStatement();
            String sql = "SELECT id FROM auth WHERE name = \""+event.getPlayer().getName()+"\";";
            ResultSet rs = smt.executeQuery(sql);
            if (!rs.isBeforeFirst()){
                pl.getLogger().severe("Failed to look up name: "+event.getPlayer().getName());
                event.getPlayer().sendPlainMessage("Use: \"/link <dc id>\" to link your account..");
                rs.close();
                smt.close();
                return;
            }
            while (rs.next()){
                long id = rs.getLong("id");
                jda.retrieveUserById(id).queue(user -> {
                    if (user.isBot() || user.isSystem()) {
                        pl.getLogger().warning(event.getPlayer().getName() + " tired to be funny and sent a " +
                                "bot/system account id");
                        event.getPlayer().sendRichMessage("You sent a bot/system account id");
                        return;
                    }
                    event.getPlayer().sendRichMessage("Approve the login request on dc!");
                    user.openPrivateChannel().queue(dm -> {
                        EmbedBuilder eb = new EmbedBuilder();
                        eb.setTitle(pl.getConfig().getString("embed.login.title"));
                        eb.setColor(new Color(pl.getConfig().getInt("embed.color")));
                        eb.setThumbnail(pl.getConfig().getString("embed.image"));
                        eb.setDescription(String.format(pl.getConfig().getString("embed.login.message"),
                                event.getPlayer().getName(), pl.getConfig().getInt("embed.delay.no_resp")));
                        MessageCreateAction l = dm.sendMessageEmbeds(eb.build()).addActionRow(
                                Button.primary("apr-login", pl.getConfig().getString("embed.btn_apr")),
                                Button.danger("rej-login", pl.getConfig().getString("embed.btn_rej"))
                        );
                        l.queue();
                        l.flatMap(Message::delete).queueAfter(pl.getConfig().getInt("embed.delay.no_resp"),
                                TimeUnit.SECONDS);
                    });
                });
            }
            rs.close();
            smt.close();
        } catch (SQLException e) {
            pl.getLogger().severe("SQLException");
            pl.getLogger().severe(e.getMessage());
        }
    }
}
