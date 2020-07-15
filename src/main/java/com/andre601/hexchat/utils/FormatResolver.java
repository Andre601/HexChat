package com.andre601.hexchat.utils;

import com.andre601.hexchat.HexChat;
import me.clip.placeholderapi.PlaceholderAPI;
import me.rayzr522.jsonmessage.JSONMessage;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FormatResolver{
    
    private final HexChat plugin;
    private final Map<String, JSONMessage> formats = new LinkedHashMap<>();
    
    private final Pattern colorPattern = Pattern.compile("(?<!\\\\)(#[a-fA-F0-9]{6})");
    
    public FormatResolver(HexChat plugin){
        this.plugin = plugin;
    }
    
    public void loadFormats(){
        ConfigurationSection formatsSection = plugin.getConfig().getConfigurationSection("formats");
        if(formatsSection == null)
            return;
        
        for(String formatKey : formatsSection.getKeys(false)){
            JSONMessage json = JSONMessage.create();
            ConfigurationSection formatSection = formatsSection.getConfigurationSection(formatKey);
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
                
                json.then(text);
                
                String color = "white";
                if(section.get("color") != null){
                    String value = section.getString("color");
                    if(value == null || value.isEmpty()){
                        json.color(color);
                        continue;
                    }
                    
                    Matcher matcher = colorPattern.matcher(value);
                    if(matcher.matches()){
                        color = matcher.group(1);
                    }
                }
                json.color(color);
                
                if(section.get("hover") != null){
                    if(section.get("hover.type") != null){
                        String type = section.getString("hover.type");
                        if(type != null && !type.isEmpty()){
                            switch(type.toLowerCase()){
                                case "text":
                                    List<String> values = section.getStringList("hover.value");
                                    if(!values.isEmpty())
                                        json.tooltip(formatList(values));
                                    break;
                                
                                case "achievement":
                                case "advancement":
                                    String value = section.getString("hover.value");
                                    if(value != null && !value.isEmpty())
                                        json.achievement(value);
                                    break;
                            }
                        }
                    }
                }
                
                if(section.get("click") != null){
                    if(section.get("click.type") != null){
                        String type = section.getString("click.type");
                        if(type != null && !type.isEmpty()){
                            switch(type.toLowerCase()){
                                case "execute":
                                    String cmd = section.getString("click.value");
                                    if(cmd != null && !cmd.isEmpty())
                                        json.runCommand(cmd);
                                    break;
                                
                                case "suggest":
                                    String suggestion = section.getString("click.value");
                                    if(suggestion != null && !suggestion.isEmpty())
                                        json.suggestCommand(suggestion);
                                    break;
                                
                                case "copy":
                                    String copy = section.getString("click.value");
                                    if(copy != null && !copy.isEmpty())
                                        json.copyText(copy);
                                    break;
                            }
                        }
                    }
                }
                
                formats.put(formatKey, json);
            }
        }
    }

    public Map<String, JSONMessage> getFormats(){
        return formats;
    }

    private String formatList(List<String> list){
        return formatString(String.join("\n", list));
    }
    
    private String formatString(String text){
        return ChatColor.translateAlternateColorCodes('&', text);
    }
    
    public String parseString(Player player, String text){
        text = text.replace("%player%", player.getName())
                   .replace("%world%", player.getWorld().getName());
        
        text = plugin.isPlaceholderApiEnabled() ? PlaceholderAPI.setPlaceholders(player, text) : text;
        
        return ChatColor.translateAlternateColorCodes('&', text);
    }
}
