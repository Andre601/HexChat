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

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatEvent implements Listener{
    
    private final HexChat plugin;
    
    private final Pattern hexColorPattern = Pattern.compile("(#([a-fA-F0-9]{6}))");
    
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
        
        // When the player doesn't have any other formats available will we apply the default one.
        if(format == null)
            format = plugin.getFormatResolver().getFormats().get("default");
        
        String msg = event.getMessage();
        if(hasPerm(player, "hexchat.color.all", "hexchat.color.code"))
            msg = ChatColor.translateAlternateColorCodes('&', msg);

        Matcher hexColorMatcher = hexColorPattern.matcher(msg);
        if(hexColorMatcher.find()){
            StringBuffer buffer = new StringBuffer();
            do{
                if(hasPerm(player, "hexchat.color.all", "hexchat.color.hex", "hexchat.color.hex." + hexColorMatcher.group(2)))
                    hexColorMatcher.appendReplacement(buffer, "" + ChatColor.of(hexColorMatcher.group(1)));
            }while(hexColorMatcher.find());
            
            hexColorMatcher.appendTail(buffer);
            msg = buffer.toString();
        }
        
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
}
