package com.andre601.hexchat.events;

import com.andre601.hexchat.HexChat;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatEvent implements Listener{
    
    private final HexChat plugin;
    Pattern colorPattern = Pattern.compile("(#[a-fA-F0-9]{6})");
    
    public ChatEvent(HexChat plugin){
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onChat(AsyncPlayerChatEvent event){
        if(event.isCancelled())
            return;
        
        Player player = event.getPlayer();
        String format = plugin.getFormatResolver().resolve(player);
        if(format == null)
            return;
        
        String msg = ChatColor.translateAlternateColorCodes('&', event.getMessage());
        Matcher matcher = colorPattern.matcher(msg);
        if(matcher.find()){
            StringBuffer buffer = new StringBuffer();
            do{
                matcher.appendReplacement(buffer, "" + ChatColor.of(matcher.group(1)));
            }while(matcher.find());
            
            matcher.appendTail(buffer);
            msg = buffer.toString();
        }
        
        event.setCancelled(true);
        
        for(Player recipient : event.getRecipients())
            recipient.sendMessage(format.replace("%msg%", msg));
        
        plugin.send("<%s> %s", event.getPlayer().getName(), event.getMessage());
    }
}
