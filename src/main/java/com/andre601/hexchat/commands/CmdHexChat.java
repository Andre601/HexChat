package com.andre601.hexchat.commands;

import com.andre601.hexchat.HexChat;
import com.andre601.hexchat.utils.ReflectionHelper;
import me.mattstudios.mf.annotations.*;
import me.mattstudios.mf.base.CommandBase;
import me.rayzr522.jsonmessage.JSONMessage;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@Command("hexchat")
@Alias({"hchat", "hc"})
public class CmdHexChat extends CommandBase{
    
    private final HexChat plugin;
    
    public CmdHexChat(HexChat plugin){
        this.plugin = plugin;
    }
    
    @Default
    @Permission("hexchat.command.help")
    public void helpMsg(final CommandSender sender){
        help(sender, null);
    }
    
    @SubCommand("help")
    @Permission("hexchat.command.help")
    @Completion("#cmds")
    public void help(final CommandSender sender, String command){
        if(command == null){
            if(sender instanceof Player){
                Player player = (Player)sender;
                if(!player.hasPermission("hexchat.command.help"))
                    return;

                JSONMessage json = getPrefix(false)
                        .then(String.format("v%s", plugin.getDescription().getVersion()))
                        .color("WHITE")
                        .newline()
                        .newline()
                        .then("/hexchat help [command]")
                        .color("AQUA")
                        .tooltip(getHover(
                                "&7Displays this help menu.\n" +
                                "&7Run &b/hexchat help [command] &7to get more detailed info about a command."
                        ))
                        .suggestCommand("/hexchat help ")
                        .newline()
                        .then("/hexchat reload")
                        .color("AQUA")
                        .tooltip(getHover(
                                "&7Reloads the config file and applies any changed formats."
                        ))
                        .suggestCommand("/hexchat reload")
                        .newline()
                        .then("/hexchat formats")
                        .color("AQUA")
                        .tooltip(getHover(
                                "&7Generates a preview of all available formats.\n" +
                                "&7This is a player only command."
                        ))
                        .suggestCommand("/hexchat formats")
                        .newline()
                        .newline()
                        .then("[Spigot]")
                        .color("GOLD")
                        .tooltip(getHover(
                                "&7Visit the Resource page on SpigotMC.org for more info."
                        ))
                        .openURL("https://www.spigotmc.org/resources/80696/")
                        .then(" ")
                        .then("[GitHub]")
                        .color("WHITE")
                        .tooltip(getHover(
                                "&7Found a bug? Report it on GitHub!"
                        ))
                        .openURL("https://github.com/Andre601/HexChat");

                json.send(player);
            }else{
                plugin.sendColor(
                        "&bHexChat v%s",
                        plugin.getDescription().getVersion()
                );
                plugin.sendColor("");
                plugin.sendColor("&bhexchat help [command]");
                plugin.sendColor("&7Displays all available commands.");
                plugin.sendColor("&7Run &bhexchat help [command] &7to get more info about a specific command.");
                plugin.sendColor("");
                plugin.sendColor("&bhexchat reload");
                plugin.sendColor("&7Reloads the config and applies any changed formats.");
            }
            return;
        }
        
        if(sender instanceof Player){
            Player player = (Player)sender;
            if(!player.hasPermission("hexchat.command.help"))
                return;
            
            if(command.equalsIgnoreCase("help")){
                sendCmdInfo(
                        player,
                        "Help",
                        "Displays all available commands or gives more information about a provided one."
                );
            }else
            if(command.equalsIgnoreCase("reload")){
                sendCmdInfo(
                        player,
                        "Reload",
                        "Reloads the config and applies any changes to the formats."
                );
            }else
            if(command.equalsIgnoreCase("formats")){
                sendCmdInfo(
                        player,
                        "Formats",
                        "Generates a preview of all available formats. This is a player only command."
                );
            }else{
                JSONMessage json = getPrefix(true)
                        .then("Invalid command provided!");
                
                json.send(player);
            }
        }else{
            if(command.equalsIgnoreCase("help")){
                sendCmdInfo(
                        sender,
                        "Help",
                        "Displays all available commands or gives more information about a provided one."
                );
            }else
            if(command.equalsIgnoreCase("reload")){
                sendCmdInfo(
                        sender,
                        "Reload",
                        "Reloads the config and applies any changes to the formats."
                );
            }else
            if(command.equalsIgnoreCase("formats")){
                plugin.sendColor("&cThis command can only be executed by players!");
            }else{
                plugin.sendColor("&cInvalid command provided!");
            }
        }
    }
    
    @SubCommand("reload")
    @Permission("hexchat.command.reload")
    public void reload(final CommandSender sender){
        if(sender instanceof Player){
            Player player = (Player)sender;
            if(!player.hasPermission("hexchat.command.reload"))
                return;
            
            sendMsg(player, "Reloading config file...", "gray");
            plugin.reloadConfig();
            
            if(plugin.getConfig().get("formats.default") == null){
                sendMsg(player, "Aborting reload! No format called 'default' available!", "red");
                return;
            }
            
            sendMsg(player, "Updating formats...", "gray");
            plugin.getFormatResolver().getFormats().clear();
            plugin.getFormatResolver().loadFormats();
            
            sendMsg(player, "Reload complete!", "green");
        }else{
            plugin.sendColor("Reloading config file...");
            plugin.reloadConfig();
            
            if(plugin.getConfig().get("formats.default") == null){
                plugin.sendColor("&cAborting reload! No format called 'default' available!");
                return;
            }
            
            plugin.sendColor("Updating formats...");
            plugin.getFormatResolver().getFormats().clear();
            plugin.getFormatResolver().loadFormats();
            plugin.sendColor("&aReload complete!");
        }
    }
    
    @SubCommand("formats")
    @Permission("hexchat.command.formats")
    public void formats(final Player player){
        if(!player.hasPermission("hexchat.command.formats"))
            return;
        
        JSONMessage title = getPrefix(true)
                .then("Loading formats...")
                .color("WHITE");
        
        title.send(player);
        
        for(String format : plugin.getFormatResolver().getFormats().keySet()){
            JSONMessage json = plugin.getFormatResolver().getFormats().get(format);
            String result = plugin.getFormatResolver().parseString(player, json.toString()).replace("%msg%", "My message");

            ReflectionHelper.sendPacket(ReflectionHelper.createTextPacket(result), player);
        }
    }
    
    private JSONMessage getHover(String text){
        return JSONMessage.create(ChatColor.translateAlternateColorCodes('&', text));
    }
    
    private void sendMsg(Player player, String msg, String color){
        JSONMessage json = getPrefix(true)
                .then(msg)
                .color(color);
        
        json.send(player);
    }
    
    private void sendCmdInfo(CommandSender sender, String command, String desc){
        if(sender instanceof Player){
            JSONMessage json = getPrefix(false)
                    .then("Command Info: ")
                    .color("WHITE")
                    .then(command)
                    .color("WHITE")
                    .newline()
                    .newline()
                    .then("Description:")
                    .color("AQUA")
                    .newline()
                    .then(desc)
                    .color("WHITE")
                    .newline()
                    .newline()
                    .then("Permission: ")
                    .color("AQUA")
                    .then("hexchat.command." + command.toLowerCase())
                    .color("WHITE");

            json.send((Player)sender);
        }else{
            plugin.sendColor("&bHexChat &fCommand Info: %s", command);
            plugin.sendColor("");
            plugin.sendColor("&bDescription:");
            plugin.sendColor("&f%s", desc);
            plugin.sendColor("");
            plugin.sendColor("&bPermission: &f%shexchat.command.", command.toLowerCase());
        }
    }
    
    private JSONMessage getPrefix(boolean brackets){
        if(!brackets)
            return JSONMessage
                    .create("H")
                    .color("#7CFF0A")
                    .then("e")
                    .color("#68FF30")
                    .then("x")
                    .color("#55FF56")
                    .then("C")
                    .color("#3EFF84")
                    .then("h")
                    .color("#2CFFA")
                    .then("a")
                    .color("#18FFD1")
                    .then("t")
                    .color("#05FFF6")
                    .then(" ");
        
        return JSONMessage
                .create("[")
                .color("#95A5A6")
                .then("H")
                .color("#7CFF0A")
                .then("e")
                .color("#68FF30")
                .then("x")
                .color("#55FF56")
                .then("C")
                .color("#3EFF84")
                .then("h")
                .color("#2CFFA")
                .then("a")
                .color("#18FFD1")
                .then("t")
                .color("#05FFF6")
                .then("]")
                .color("#95A5A6")
                .then(" ");
    }
}
