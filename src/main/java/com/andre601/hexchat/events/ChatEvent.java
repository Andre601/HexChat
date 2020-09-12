package com.andre601.hexchat.events;

import com.andre601.hexchat.HexChat;
import me.mattstudios.mfmsg.base.Message;
import me.mattstudios.mfmsg.base.internal.MessageComponent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.Map;
import java.util.regex.Pattern;

public class ChatEvent implements Listener{
    
    private final HexChat plugin;
    
    private final Pattern chatColor = Pattern.compile("(?i)&[0-9A-FK-OR]");
    
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
        
        String format = null;
        for(Map.Entry<String, String> formatMap : plugin.getFormatResolver().getFormats().entrySet()){
            if(player.hasPermission("hexchat.format." + formatMap.getKey()))
                format = formatMap.getValue();
        }
        
        if(format == null)
            format = plugin.getFormatResolver().getFormats().get("default");
        
        String msg = event.getMessage();
        
        if(!player.hasPermission("hexchat.markdown.bold"))
            msg = escape(msg, "**");
        if(!player.hasPermission("hexchat.markdown.italic"))
            msg = escape(msg, "*", "_");
        if(!player.hasPermission("hexchat.markdown.magic") && !player.hasPermission("hexchat.markdown.obfuscated"))
            msg = escape(msg, "||");
        if(!player.hasPermission("hexchat.markdown.strikethrough"))
            msg = escape(msg, "~~");
        if(!player.hasPermission("hexchat.markdown.underline"))
            msg = escape(msg, "__");
        
        if(!player.hasPermission("hexchat.color.code"))
            msg = chatColor.matcher(msg).replaceAll("");
        
        format = format.replace("%msg%", msg);
        
        final MessageComponent component = Message.create()
                .parse(plugin.getFormatResolver().formatString(event.getPlayer(), format));


        for(Player recipient : event.getRecipients())
            component.sendMessage(recipient);
        
        // Clear recipients so that the original message won't be send
        event.getRecipients().clear();
    }
    
    private String escape(String msg, String... replaces){
        for(String replace : replaces)
            msg = msg.replace(replace, "\\" + replace);
        
        return msg;
    }
}
