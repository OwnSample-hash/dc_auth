package me.ownsample.dc_auth.EventHandlers;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.requests.restaction.interactions.MessageEditCallbackAction;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.concurrent.TimeUnit;

public class dc_listener implements EventListener {
    private final JavaPlugin pl;

    public dc_listener(JavaPlugin pl){
        this.pl = pl;
    }

    @Override
    public void onEvent(@NotNull GenericEvent event) {
        if (event instanceof ReadyEvent) {
            pl.getLogger().info("DC API is ready!");
        }
        if (event instanceof ButtonInteractionEvent btn_event){
            pl.getLogger().info("Got a button event: "+event.toString());
            if (btn_event.getComponentId().equals("apr")){
                EmbedBuilder eb = new EmbedBuilder();
                eb.setTitle(pl.getConfig().getString("embed.link.title"));
                eb.setColor(new Color(pl.getConfig().getInt("embed.color")));
                eb.setThumbnail(pl.getConfig().getString("embed.image"));
                eb.setDescription(pl.getConfig().getString("embed.link.accept"));
                btn_event.editMessageEmbeds(eb.build()).queue();
            } else {
                EmbedBuilder eb = new EmbedBuilder();
                eb.setTitle(pl.getConfig().getString("embed.link.title"));
                eb.setColor(new Color(pl.getConfig().getInt("embed.color")));
                eb.setThumbnail(pl.getConfig().getString("embed.image"));
                eb.setDescription(pl.getConfig().getString("embed.link.reject"));
                MessageEditCallbackAction msg = btn_event.editMessageEmbeds(eb.build());
                msg.queue();
                msg.flatMap(InteractionHook::deleteOriginal).
                        queueAfter(pl.getConfig().getInt("embed.delay"), TimeUnit.SECONDS);
            }
            //String add = "INSERT INTO auth VALUES ("+user.getIdLong()+","+player.getName()+");";
        }
    }
}
