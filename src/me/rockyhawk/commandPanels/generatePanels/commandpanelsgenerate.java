package me.rockyhawk.commandPanels.generatePanels;

import me.rockyhawk.commandPanels.commandpanels;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;


public class commandpanelsgenerate implements CommandExecutor {
    commandpanels plugin;
    public commandpanelsgenerate(commandpanels pl) { this.plugin = pl; }

    @EventHandler
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        String tag = plugin.config.getString("config.format.tag") + " ";
        if(!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',tag + ChatColor.RED + "Please execute command as a Player!"));
            return true;
        }
        Player p = (Player) sender;
        if (label.equalsIgnoreCase("cpg") || label.equalsIgnoreCase("commandpanelgenerate") || label.equalsIgnoreCase("cpanelg")) {
            if (p.hasPermission("commandpanel.generate")) {
                if (args.length == 1) {
                    //command /cpg
                    try {
                        if (Integer.parseInt(args[0]) >= 1 && Integer.parseInt(args[0]) <= 6) {
                            Inventory i = Bukkit.createInventory((InventoryHolder) null, Integer.parseInt(args[0]) * 9, "Generate New Panel");
                            p.openInventory(i);
                        } else {
                            p.sendMessage(ChatColor.translateAlternateColorCodes('&', tag + ChatColor.RED + "Please use integer from 1-6."));
                        }
                    }catch(Exception exc){
                        p.sendMessage(ChatColor.translateAlternateColorCodes('&', tag + ChatColor.RED + "Please use integer from 1-6."));
                    }
                }else{
                    p.sendMessage(ChatColor.translateAlternateColorCodes('&',tag + ChatColor.RED + "Usage: /cpg [rows]"));
                }
                return true;
            }else{
                p.sendMessage(ChatColor.translateAlternateColorCodes('&',tag + plugin.config.getString("config.format.perms")));
                return true;
            }
        }
        p.sendMessage(ChatColor.translateAlternateColorCodes('&',tag + ChatColor.RED + "Usage: /cpg [rows]"));
        return true;
    }
}