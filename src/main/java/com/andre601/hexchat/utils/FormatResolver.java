package com.andre601.hexchat.utils;

import com.andre601.hexchat.HexChat;
import me.clip.placeholderapi.PlaceholderAPI;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FormatResolver{
    
    private final HexChat plugin;
    
    private final Pattern colorPattern = Pattern.compile("(?<!\\\\)(#[a-fA-F0-9]{6})");
    
    public FormatResolver(HexChat plugin){
        this.plugin = plugin;
    }
    
    public String resolve(Player player){
        String format = null;
        
        ConfigurationSection formats = plugin.getConfig().getConfigurationSection("formats");
        if(formats == null)
            return null;
        
        for(String formatKey : formats.getKeys(false)){
            if(!player.hasPermission("hexchat.format." + formatKey) && !formatKey.equals("default"))
                continue;
            
            StringBuilder builder = new StringBuilder();
            ConfigurationSection formatSection = plugin.getConfig().getConfigurationSection("formats." + formatKey);
            if(formatSection == null)
                continue;
            
            for(String sectionKey : formatSection.getKeys(false)){
                ConfigurationSection section = formatSection.getConfigurationSection(sectionKey);
                if(section == null)
                    continue;
                
                if(section.get("text") == null)
                    continue;
    
                String text = section.getString("text");
                if(text == null || text.isEmpty())
                    continue;
                
                ChatColor color = ChatColor.WHITE;
                if(section.get("color") != null){
                    String value = section.getString("color");
                    if(value == null || value.isEmpty()){
                        builder.append(color).append(parseString(player, text));
                        continue;
                    }
                    
                    Matcher matcher = colorPattern.matcher(value);
                    if(matcher.matches()){
                        color = ChatColor.of(matcher.group(1));
                    }else{
                        try{
                            color = ChatColor.of(value);
                        }catch(IllegalArgumentException ignored){}
                    }
                }
                builder.append(color).append(parseString(player, text));
                
                format = builder.toString();
            }
        }
        
        return format;
    }
    
    private String parseString(Player player, String text){
        text = text.replace("%player%", player.getName())
                   .replace("%world%", player.getWorld().getName());
        
        text = plugin.isPlaceholderApiEnabled() ? PlaceholderAPI.setPlaceholders(player, text) : text;
        
        Matcher matcher = colorPattern.matcher(text);
        if(matcher.find())
            text = text.replaceAll(colorPattern.pattern(), "" + ChatColor.of(matcher.group(1)));
        
        return ChatColor.translateAlternateColorCodes('&', text);
    }
}
