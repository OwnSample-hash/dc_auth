package me.ownsample.dc_auth;

import me.ownsample.dc_auth.EventHandlers.dc_listener;
import me.ownsample.dc_auth.EventHandlers.onJoin;
import me.ownsample.dc_auth.cmd.Delete;
import me.ownsample.dc_auth.cmd.Link;
import me.ownsample.dc_auth.cmd.Query;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;
import java.sql.*;
public class dc_auth extends JavaPlugin  {

    public static JDA jda;
    public List<String> login_q = new ArrayList<String>();
    public Map<Long, String> link_q = new HashMap<Long, String>();
    public Connection con;

    public Timer timer = new Timer();
    @Override
    public void onEnable() {
        saveDefaultConfig();
        jda = JDABuilder.createDefault(getConfig().getString("token"))
                .addEventListeners(new dc_listener(this))
                .build();
        try {
            con = DriverManager.getConnection(Objects.requireNonNull(getConfig().getString("jdbc")));
            Statement stm = con.createStatement();
            String sql = "CREATE TABLE IF NOT EXISTS auth (" +
                    "    id BIGINT NOT NULL PRIMARY KEY," +
                    "    name VARCHAR(255) NOT NULL" +
                    ");";
            stm.executeUpdate(sql);
            stm.close();
        } catch (SQLException e) {
            getLogger().severe("SQLException");
            getLogger().severe(e.getMessage());
        }
        getCommand("link").setExecutor(new Link(this));
        getCommand("query").setExecutor(new Query(this));
        getCommand("delete").setExecutor(new Delete(this));
        getServer().getPluginManager().registerEvents(new onJoin(this), this);
        getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
    }

    @Override
    public void onDisable(){
        if (con != null){
            try {
                con.close();
            } catch (SQLException e) {
                getLogger().severe("SQLException");
                getLogger().severe(e.getMessage());
            }
        }
    }
}