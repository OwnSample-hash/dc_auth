package me.ownsample.dc_auth.cmd;

import me.ownsample.dc_auth.dc_auth;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import net.dv8tion.jda.api.requests.restaction.MessageCreateAction;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.sql.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static me.ownsample.dc_auth.dc_auth.jda;

public record Link(dc_auth pl) implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender,
                             @NotNull Command command,
                             @NotNull String label,
                             @NotNull String[] args) {
        if (sender instanceof Player player) {
            try {
                Statement smt = pl.con.createStatement();
                String sql = "SELECT id FROM auth WHERE name = \"" + player.getName() + "\";";
                ResultSet rs = smt.executeQuery(sql);
                if (!rs.isBeforeFirst()) {
                    //Empty we need link
                    if (args.length == 0) {
                        player.sendRichMessage("Usage: /link <discord id>");
                        return true;
                    }
                    String id = args[0];
                    AtomicBoolean ret = new AtomicBoolean(false);
                    jda.retrieveUserById(id).queue(user -> {
                        if (user.isBot() || user.isSystem()) {
                            pl.getLogger().warning(player.getName() + " tired to be funny and  sent a bot/system " +
                                    "account id");
                            player.sendRichMessage("You sent a bot/system account id");
                            ret.set(true);
                            return;
                        }
                        player.sendRichMessage("Linking... check dc!");
                        user.openPrivateChannel().queue(dm -> {
                            EmbedBuilder eb = new EmbedBuilder();
                            eb.setTitle(pl.getConfig().getString("embed.link.title"));
                            eb.setColor(new Color(pl.getConfig().getInt("embed.color")));
                            eb.setThumbnail(pl.getConfig().getString("embed.image"));
                            eb.setDescription(String.format(pl.getConfig().getString("embed.link.message"),
                                    player.getName(), pl.getConfig().getInt("embed.delay.no_resp")));
                            //msg.delete().queueAfter(pl.getConfig().getInt("embed.delay"), TimeUnit.SECONDS);
                            MessageCreateAction l = dm.sendMessageEmbeds(eb.build()).addActionRow(
                                    Button.primary("apr", pl.getConfig().getString("embed.btn_apr")),
                                    Button.danger("rej", pl.getConfig().getString("embed.btn_rej"))
                            );
                            l.queue();
                            l.flatMap(Message::delete).queueAfter(pl.getConfig().getInt("embed.delay.no_resp"),
                                    TimeUnit.SECONDS);
                            pl.link_q.put(user.getIdLong(), player.getName());
                        });
                    });
                    if (ret.get())
                        return true;
                }
                while (rs.next()) {
                    //long id = rs.getLong("id");
                    player.sendRichMessage("You are already have a discord account link setup!");
                }
                rs.close();
                smt.close();
            } catch (SQLException e) {
                pl.getLogger().severe("SQLException");
                pl.getLogger().severe(e.getMessage());
            }
        }
        return true;
    }
}
