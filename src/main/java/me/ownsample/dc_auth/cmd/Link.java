package me.ownsample.dc_auth.cmd;


import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import net.dv8tion.jda.api.requests.restaction.MessageCreateAction;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.sql.*;
import java.util.concurrent.atomic.AtomicBoolean;

import static me.ownsample.dc_auth.dc_auth.jda;

public class Link implements CommandExecutor {
    private final Connection con;
    private final JavaPlugin pl;
    public Link(Connection con, JavaPlugin pl){
        this.con= con;
        this.pl = pl;
    }
    @Override
    public boolean onCommand(@NotNull CommandSender sender,
                             @NotNull Command command,
                             @NotNull String label,
                             @NotNull String[] args) {
        if (sender instanceof Player player){
            try {
                Statement smt = con.createStatement();
                String sql  = "SELECT id FROM auth WHERE name = \""+player.getName()+"\";";
                ResultSet rs =  smt.executeQuery(sql);
                if (!rs.isBeforeFirst()){
                    //Empty we need link
                    if (args.length == 0){
                        player.sendRichMessage("&1Usage: /link <discord id>");
                        return true;
                    }
                    String id = args[0];
                    AtomicBoolean ret = new AtomicBoolean(false);
                    jda.retrieveUserById(id).queue(user -> {
                        if (user.isBot() || user.isSystem()){
                            pl.getLogger().warning(player.getName() +" tired to be funny and  sent a bot/system " +
                                    "account id");
                            player.sendRichMessage("&2You sent a bot/system account id");
                            ret.set(true);
                            return;
                        }
                        pl.getLogger().info("Got info " + user.getName() + " " + user.getIdLong());
                        user.openPrivateChannel().queue(dm -> {
                            EmbedBuilder eb = new EmbedBuilder();
                            eb.setTitle(pl.getConfig().getString("embed.link.title"));
                            eb.setColor(new Color(pl.getConfig().getInt("embed.color")));
                            eb.setThumbnail(pl.getConfig().getString("embed.image"));
                            eb.setDescription(String.format(pl.getConfig().getString("embed.link.message"),
                                    player.getName()));
                            //msg.delete().queueAfter(pl.getConfig().getInt("embed.delay"), TimeUnit.SECONDS);
                            MessageCreateAction l = dm.sendMessageEmbeds(eb.build()).addActionRow(
                                    Button.primary("apr", pl.getConfig().getString("embed.btn_apr")),
                                    Button.danger("rej", pl.getConfig().getString("embed.btn_rej"))
                            );
                            l.queue();
                        });
                    });
                    if (ret.get())
                        return true;
                }
                while(rs.next()) {
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
