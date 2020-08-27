package com.andre601.hexchat.utils;

import com.andre601.hexchat.HexChat;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FormatResolver{
    
    private final HexChat plugin;
    private final Map<String, String> formats = new LinkedHashMap<>();
    
    public FormatResolver(HexChat plugin){
        this.plugin = plugin;
    }
    
    public void loadingFormats(){
        ConfigurationSection formatsSection = plugin.getConfig().getConfigurationSection("formats");
        if(formatsSection == null)
            return;
        
        for(String formatName : formatsSection.getKeys(false)){
            StringBuilder formatBuilder = new StringBuilder();
            
            ConfigurationSection formatSection = formatsSection.getConfigurationSection(formatName);
            if(formatSection == null)
                continue;
            
            for(String key : formatSection.getKeys(false)){
                ConfigurationSection section = formatSection.getConfigurationSection(key);
                if(section == null)
                    continue;
                
                String text = section.getString("text");
                if(text == null || text.isEmpty())
                    continue;
                
                String color = section.getString("color");
                if(color != null && !color.isEmpty())
                    text = Colors.getColor(color) + escape(text);
                else
                    text = "&f" + escape(text);
                
                String hover = null;
                String click = null;
                
                boolean hasHover = false;
                boolean hasClick = false;
                
                List<String> hoverText = section.getStringList("hover");
                if(!hoverText.isEmpty()){
                    hover = "hover: " + String.join("\\n", hoverText);
                    hasHover = true;
                }
                
                String clickType = section.getString("click.type");
                if(clickType != null && !clickType.isEmpty()){
                    String value = section.getString("click.value");
                    switch(clickType.toLowerCase()){
                        case "command":
                        case "execute":
                            if(value != null && !value.isEmpty()){
                                click = "command: " + value;
                                hasClick = true;
                            }
                            break;
                        
                        case "suggest":
                            if(value != null && !value.isEmpty()){
                                click = "suggest: " + value;
                                hasClick = true;
                            }
                            break;
                        
                        case "copy":
                            if(value != null && !value.isEmpty()){
                                click = "clipboard: " + value;
                                hasClick = true;
                            }
                            break;
                        
                        case "url":
                            if(value != null && !value.isEmpty()){
                                click = "url: " + value;
                                hasClick = true;
                            }
                            break;
                    }
                }
                
                if(hasHover || hasClick){
                    formatBuilder.append("[").append(text).append("](");
                    
                    if(hasHover)
                        formatBuilder.append(hover);
                    
                    if(hasClick){
                        if(hasHover)
                            formatBuilder.append("|");
                        
                        formatBuilder.append(click);
                    }
                    
                    formatBuilder.append(")");
                    continue;
                }
                
                formatBuilder.append(text);
            }
            
            formats.put(formatName, formatBuilder.toString());
        }
    }

    public Map<String, String> getFormats(){
        return formats;
    }
    
    public String formatString(Player player, String text){
        return formatString(player, text, true);
    }
    
    public String formatString(Player player, String text, boolean escape){
        text = text.replace("%player%", escape ? escapeAll(player.getName()) : player.getName())
                   .replace("%world%", escape ? escapeAll(player.getWorld().getName()) : player.getWorld().getName());
        
        return plugin.isPlaceholderApiEnabled() ? PlaceholderAPI.setPlaceholders(player, text) : text;
    }
    
    private String escape(String text){
        return text.replace("[", "\\[")
                   .replace("]", "\\]")
                   .replace("(", "\\(")
                   .replace(")", "\\)")
                   .replace("|", "\\|");
    }
    
    private String escapeAll(String text){
        return escape(text).replace("_", "\\_")
                           .replace("__", "\\__");
    }
    
    private enum Colors{
        BLACK       (null, "&0"),
        DARK_BLUE   (null, "&1"),
        DARK_GREEN  (null, "&2"),
        DARK_AQUA   (null, "&3"),
        DARK_RED    (null, "&4"),
        DARK_PURPLE (null, "&5"),
        GOLD        (null, "&6"),
        GRAY        (null, "&7"),
        DARK_GRAY   (null, "&8"),
        BLUE        (null, "&9"),
        GREEN       (null, "&a"),
        AQUA        (null, "&b"),
        RED         (null, "&c"),
        LIGHT_PURPLE(null, "&d"),
        YELLOW      (null, "&e"),
        WHITE       (null, "&f"),
        
        HEX("#[a-fA-F0-9]{6}", null);
        
        private final Pattern pattern;
        private final String color;
        
        Colors(String pattern, String color){
            this.pattern = Pattern.compile("(" + (pattern == null ? this.name().toLowerCase() : pattern) + ")");
            this.color = color;
        }
        
        public static String getColor(String value){
            for(Colors color : values()){
                Matcher matcher = color.pattern.matcher(value.toLowerCase());
                
                if(matcher.matches())
                    return color.color == null ? "&" + matcher.group(1) : color.color;
            }
            
            return "&f";
        }
    }
}
