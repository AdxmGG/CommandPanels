package me.rockyhawk.commandPanels.premium;

import me.rockyhawk.commandPanels.commandpanels;
import org.bukkit.*;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

public class commandpanelrefresher implements Listener {
    commandpanels plugin;
    public commandpanelrefresher(commandpanels pl) {
        this.plugin = pl;
    }
    private int c = 0;
    private int animatevalue = -1;
    private int animatecount = 0;
    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent e){ //Handles when Players open inventory
        //I have to convert HumanEntity to a player
        if (plugin.config.contains("config.refresh-panels")) {
            if (plugin.config.getString("config.refresh-panels").trim().equalsIgnoreCase("false")) {
                return;
            }
        }
        HumanEntity h = e.getPlayer();
        Player p;
        if (h instanceof Player) {
            p = Bukkit.getPlayer(h.getName());
        }else{
            return;
        }
        //get all panel names (not titles)
        String tag = plugin.config.getString("config.format.tag") + " ";
        YamlConfiguration cf = null;
        String panel = null;
        String panelTitle = null;
        ArrayList<String> filenames = new ArrayList<String>(Arrays.asList(plugin.panelsf.list()));
        try {
            boolean foundPanel = false;
            for (int f = 0; filenames.size() > f; f++) { //will loop through all the files in folder
                String key;
                YamlConfiguration temp;
                temp = YamlConfiguration.loadConfiguration(new File(plugin.panelsf + File.separator + filenames.get(f)));
                if(!plugin.checkPanels(temp)){
                    p.sendMessage(ChatColor.translateAlternateColorCodes('&',tag + plugin.papi(p, plugin.config.getString("config.format.error") + ": File with no Panels found or Panel with syntax error Found!")));
                    return;
                }
                for (Iterator var10 = temp.getConfigurationSection("panels").getKeys(false).iterator(); var10.hasNext();) {
                    key = (String) var10.next();
                    if(ChatColor.translateAlternateColorCodes('&',temp.getString("panels." + key + ".title")).equals(e.getView().getTitle())){
                        panel = key;
                        panelTitle = ChatColor.translateAlternateColorCodes('&',temp.getString("panels." + key + ".title"));
                        cf = YamlConfiguration.loadConfiguration(new File(plugin.panelsf + File.separator + filenames.get(f)));
                        foundPanel= true;
                        break;
                    }
                }
                if(foundPanel){
                    //this is to avoid the plugin to continue looking when it was already found
                    break;
                }
            }
        }catch(Exception fail){
            //could not fetch all panel names (probably no panels exist)
        }
        if(panel == null){
            return;
        }
        //there is already a runnable running for this player
        if(plugin.panelRunning.contains(p.getName() + ";" +  panel)){
            return;
        }
        plugin.panelRunning.add(p.getName() + ";" +  panel);
        if (plugin.config.contains("config.panel-snooper")) {
            if (plugin.config.getString("config.panel-snooper").trim().equalsIgnoreCase("true")) {
                Bukkit.getConsoleSender().sendMessage("[CommandPanels] " + p.getName() + " Opened " + panel);
            }
        }
        if(cf.contains("panels." + panel + ".animatevalue")){
            animatevalue = cf.getInt("panels." + panel + ".animatevalue");
        }
        final YamlConfiguration cfFinal = cf;
        final String fpanel = panel;
        final String fpanelTitle = panelTitle;
        ItemStack panelItemList[] = plugin.openGui(fpanel, p, cf,2, -1).getContents();
        ItemStack playerItemList[] = p.getInventory().getStorageContents();
        new BukkitRunnable(){
            @Override
            public void run() {
                //counter counts to refresh delay (in seconds) then restarts
                if(c < Double.parseDouble(plugin.config.getString("config.refresh-delay").trim())){
                    c+=1;
                }else{
                    c=0;
                }
                //refresh here
                if(p.getOpenInventory().getTitle().equals(fpanelTitle)){
                    if(c == 0) {
                        //animation counter
                        if(animatevalue != -1) {
                            if (animatecount < animatevalue) {
                                animatecount += 1;
                            } else {
                                animatecount = 0;
                            }
                        }
                        try {
                            plugin.openGui(fpanel, p, cfFinal, 0,animatecount);
                        } catch (Exception e) {
                            //error opening gui
                        }
                    }
                }else{
                    if(plugin.config.getString("config.stop-sound").trim().equalsIgnoreCase("true")){
                        try {
                            p.stopSound(Sound.valueOf(cfFinal.getString("panels." + fpanel + ".sound-on-open").toUpperCase()));
                        }catch(Exception sou){
                            //skip
                        }
                    }
                    c = 0;
                    //check to ensure players haven't duplicated items
                    try {
                        p.updateInventory();
                        for (ItemStack playerContent : p.getInventory().getStorageContents()) {
                            for (ItemStack panelContent : panelItemList) {
                                if (playerContent != null && panelContent != null) {
                                    if (!playerContent.getType().equals(Material.matchMaterial("AIR")) && !panelContent.getType().equals(Material.matchMaterial("AIR"))) {
                                        if (playerContent.equals(panelContent)) {
                                            boolean isOriginal = false;
                                            for (ItemStack playerOriginalContent : playerItemList) {
                                                if (playerOriginalContent != null && !playerOriginalContent.getType().equals(Material.matchMaterial("AIR"))) {
                                                    if (playerContent.equals(playerOriginalContent)) {
                                                        isOriginal = true;
                                                    }
                                                }
                                            }
                                            if(!isOriginal) {
                                                p.getInventory().removeItem(playerContent);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }catch(Exception e){
                        //oof
                    }
                    this.cancel();
                    if(p.getOpenInventory().getTitle().equals(fpanelTitle)) {
                        p.closeInventory();
                    }
                    plugin.panelRunning.remove(p.getName() + ";" +  fpanel);
                    if (plugin.config.contains("config.panel-snooper")) {
                        if (plugin.config.getString("config.panel-snooper").trim().equalsIgnoreCase("true")) {
                            Bukkit.getConsoleSender().sendMessage("[CommandPanels] " + p.getName() + " Closed " + fpanel);
                        }
                    }
                }
            }
        }.runTaskTimer(this.plugin, 5, 5); //20 ticks == 1 second (5 ticks = 0.25 of a second)

    }
}