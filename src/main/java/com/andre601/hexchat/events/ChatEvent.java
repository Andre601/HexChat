package com.andre601.hexchat.events;

import com.andre601.hexchat.HexChat;
import com.andre601.hexchat.utils.ReflectionHelper;
import me.mattstudios.mfmsg.base.FormatOptions;
import me.mattstudios.mfmsg.base.Message;
import me.mattstudios.mfmsg.base.internal.Format;
import me.mattstudios.mfmsg.base.internal.MessageComponent;
import me.rayzr522.jsonmessage.JSONMessage;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ChatEvent implements Listener{
    
    private final HexChat plugin;
    
    public ChatEvent(HexChat plugin){
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onChat(AsyncPlayerChatEvent event){
        if(event.isCancelled())
            return;
        
        // If no default format exists, don't handle the event.
        if(!plugin.getFormatResolver().getFormats().containsKey("default"))
            return;
        
        Player player = event.getPlayer();

        JSONMessage format = null;
        for(String name : plugin.getFormatResolver().getFormats().keySet()){
            if(player.hasPermission("hexchat.format." + name))
                format = plugin.getFormatResolver().getFormats().get(name);
        }
        
        // When the player doesn't have any other formats available will we apply the default one.
        if(format == null)
            format = plugin.getFormatResolver().getFormats().get("default");
        
        String msg = parse(player, event.getMessage());
        
        event.setCancelled(true);

        String json = plugin.getFormatResolver().parseString(player, format.toString()).replace("%msg%", msg);

        Player[] players = event.getRecipients().toArray(new Player[0]);
        ReflectionHelper.sendPacket(ReflectionHelper.createTextPacket(json), players);
        
        if(!plugin.getConfig().getBoolean("console.log", true))
            return;
        
        String consoleFormat = plugin.getConfig().getString("console.format");
        if(consoleFormat == null)
            consoleFormat = "<%player%> %msg%";
        
        plugin.send(
                consoleFormat.replace("%player%", event.getPlayer().getName())
                             .replace("%msg%", event.getMessage())
                             .replace("%world%", event.getPlayer().getWorld().getName())
        );
    }
    
    private boolean hasPerm(Player player, String... permissions){ 
        return Arrays.stream(permissions).anyMatch(player::hasPermission);
    }
    private String parse(Player player, String msg){
        List<Format> formats = new ArrayList<>();
        
        if(player.hasPermission("hexchat.markdown.bold"))
            formats.add(Format.BOLD);
        
        if(player.hasPermission("hexchat.markdown.italic"))
            formats.add(Format.ITALIC);
        
        if(player.hasPermission("hexchat.markdown.underline"))
            formats.add(Format.UNDERLINE);
        
        if(player.hasPermission("hexchat.markdown.strikethrough"))
            formats.add(Format.STRIKETHROUGH);
        
        if(hasPerm(player, "hexchat.markdown.obfuscated", "hexchat.markdown.magic"))
            formats.add(Format.OBFUSCATED);

        if(player.hasPermission("hexchat.color.code"))
            formats.add(Format.COLOR);

        Message message = Message.create(FormatOptions.builder().with(formats.toArray(new Format[0])));
        MessageComponent component = message.parse(msg);
        
        return component.toString();
    }
}
