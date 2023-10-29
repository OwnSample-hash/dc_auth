package me.ownsample.dc_auth.cmd;

import me.ownsample.dc_auth.dc_auth;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.sql.Statement;

public record Delete(dc_auth pl) implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender,
                             @NotNull Command command,
                             @NotNull String label,
                             @NotNull String[] args) {
        if (args.length > 2) {
            sender.sendPlainMessage("Usage: /delete <id:name> <value>"+args.length);
            return true;
        }
        String method = args[0];
        String value = args[1];
        String sql = "DELETE FROM auth WHERE %s = %s;";
        if (method.equals("id"))
            sql = String.format(sql, method, value);
        else
            sql = String.format(sql, method, '"'+value+'"');

        try {
            Statement smt = pl.con.createStatement();
            sender.sendPlainMessage("Deleted value: "+value+" with method: "+method);
            smt.executeUpdate(sql);
            smt.close();
        } catch (SQLException e) {
            pl.getLogger().severe("SQLException");
            pl.getLogger().severe(e.getMessage());
        }
        return true;
    }
}