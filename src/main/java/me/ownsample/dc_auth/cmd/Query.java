package me.ownsample.dc_auth.cmd;

import me.ownsample.dc_auth.dc_auth;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public record Query(dc_auth pl) implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender,
                             @NotNull Command command,
                             @NotNull String label,
                             @NotNull String[] args) {
        try {
            Statement smt = pl.con.createStatement();
            String sql = "SELECT * FROM auth;";
            ResultSet rs = smt.executeQuery(sql);
            if (!rs.isBeforeFirst()){
                sender.sendPlainMessage("There are no records to display!");
            }
            while (rs.next()){
                long id = rs.getLong("id");
                String name = rs.getString("name");
                sender.sendPlainMessage("ID: "  + id + "    Name: " + name);
            }
            rs.close();
            smt.close();
        } catch (SQLException e) {
            pl.getLogger().severe("SQLException");
            pl.getLogger().severe(e.getMessage());
        }
        return true;
    }
}
