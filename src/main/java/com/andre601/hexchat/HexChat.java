package com.andre601.hexchat;

import com.andre601.hexchat.commands.CmdHexChat;
import com.andre601.hexchat.events.ChatEvent;
import com.andre601.hexchat.utils.FormatResolver;
import me.mattstudios.mf.base.CommandManager;
import org.bukkit.ChatColor;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;

public class HexChat extends JavaPlugin{
    
    private boolean placeholderApiEnabled = false;
    private FormatResolver formatResolver;
    
    @Override
    public void onEnable(){
        saveDefaultConfig();
        PluginManager manager = getServer().getPluginManager();
        
        if(getConfig().getBoolean("console.banner", true)) 
            sendBanner();
        
        if(getConfig().get("formats.default") == null){
            sendColor("&cNo format with the name 'default' was defined in the config.yml");
            sendColor("&cA format with this name is REQUIRED!");
            sendColor("&cPlease add a format with the name 'default'.");
            sendColor("&cDisabling plugin...");
            
            manager.disablePlugin(this);
            return;
        }
        
        if(manager.isPluginEnabled("PlaceholderAPI")){
            placeholderApiEnabled = true;
            sendColor("Found PlaceholderAPI! Placeholder support enabled.");
        }
        
        formatResolver = new FormatResolver(this);
        sendColor("Loading formats...");
        formatResolver.loadFormats();
        sendColor("All formats have been loaded.");

        sendColor("Loading Events...");
        new ChatEvent(this);
        sendColor("Enabled Events.");

        sendColor("Loading command /hexchat...");
        setupCommands();
        sendColor("Command loaded.");
        
        sendColor("&aSuccessfully enabled %s v%s", getName(), getDescription().getVersion());
    }
    
    public boolean isPlaceholderApiEnabled(){
        return placeholderApiEnabled;
    }
    
    public FormatResolver getFormatResolver(){
        return formatResolver;
    }
    
    public void send(String msg, Object... args){
        ConsoleCommandSender sender = getServer().getConsoleSender();
        
        sender.sendMessage(String.format(msg, args));
    }
    
    public void sendColor(String msg, Object... args){
        send("[" + getName() + "] " + ChatColor.translateAlternateColorCodes('&', msg), args);
    }
    
    private void setupCommands(){
        CommandManager manager = new CommandManager(this);
        
        manager.getCompletionHandler().register("#cmds", input -> Arrays.asList("help", "reload", "formats"));
        
        manager.register(new CmdHexChat(this));
    }
    
    private void sendBanner(){
        sendColor("&4 _    _            _____ _           _");
        sendColor("&c| |  | |          / ____| |         | |");
        sendColor("&6| |__| | _____  _| |    | |__   __ _| |_");
        sendColor("&e|  __  |/ _ \\ \\/ / |    | '_ \\ / _` | __|");
        sendColor("&a| |  | |  __/>  <| |____| | | | (_| | |_");
        sendColor("&2|_|  |_|\\___/_/\\_\\\\_____|_| |_|\\__,_|\\__|");
        sendColor("&3 By Andre_601                     v%s", getDescription().getVersion());
    }
}
