package com.andre601.hexchat.events;

import com.andre601.hexchat.HexChat;
import com.andre601.hexchat.utils.ReflectionHelper;
import me.rayzr522.jsonmessage.JSONMessage;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

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
        
        Player player = event.getPlayer();

        JSONMessage format = null;
        for(String name : plugin.getFormatResolver().getFormats().keySet()){
            if(player.hasPermission("hexchat.format." + name))
                format = plugin.getFormatResolver().getFormats().get(name);
        }
        
        if(format == null)
            return;
        
        String msg = event.getMessage();
        if(player.hasPermission("hexchat.color.code") || player.hasPermission("hexchat.color.all"))
            msg = ChatColor.translateAlternateColorCodes('&', msg);
        
        event.setCancelled(true);

        String json = plugin.getFormatResolver().parseString(player, format.toString()).replace("%msg%", msg);

        Player[] players = event.getRecipients().toArray(new Player[0]);
        ReflectionHelper.sendPacket(ReflectionHelper.createTextPacket(json), players);
        
        plugin.send("<%s> %s", event.getPlayer().getName(), event.getMessage());
    }
}
