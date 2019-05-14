package com.firesoftitan.play.titansql;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class UpdateChecker extends BukkitRunnable{
    private String url = "https://api.spigotmc.org/legacy/update.php?resource=";
    private String download ="https://www.spigotmc.org/resources/";
    private String name = "";
    private String projectID = "";
    private Plugin plugin = null;
    private String verions = "";
    private boolean update = false;
    public UpdateChecker(Plugin plugin, String projectID)
    {
        this.plugin = plugin;
        this.verions = this.plugin.getDescription().getVersion();
        this.name = this.plugin.getDescription().getName();
        this.projectID = projectID;
    }
    public boolean isUpdateAvailable()
    {
        return update;
    }
    @Override
    public void run() {
        URL url = null;
        try {
            url = new URL(this.url + this.projectID);
        } catch (MalformedURLException e) {

        }
        URLConnection conn = null;
        try {
            conn = url.openConnection();
        } catch (IOException e) {

        }
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line = br.readLine();
            if(line.equals(this.verions)) {
                System.out.println("[" + this.name + "] " + ChatColor.GREEN + "No Updates aviable");
                update = false;
            }else {
                if ( line.split("\\.").length < 3)
                {
                    line = line + ".???.???.???";
                }
                String[] subVersion = line.split("\\.");
                String forServer = subVersion[2];
                String server_Version = Bukkit.getVersion().split("MC: ")[1].replace("\\.", "_");
                if (!server_Version.equals(forServer)) {
                    System.out.println(ChatColor.RED + "============================================");
                    System.out.println(ChatColor.WHITE + "[" + this.name + "] " + ChatColor.RED + "This version of Minecraft is no loner supported.");
                    System.out.println(ChatColor.WHITE + "[" + this.name + "] " + ChatColor.RED + "Supported version: " + forServer.replace("_", "\\."));
                }
                System.out.println(ChatColor.GREEN + "============================================");
                System.out.println(ChatColor.WHITE + "[" + this.name + "] " + ChatColor.GREEN + "There is an update! Download it at ");
                System.out.println(ChatColor.WHITE + "[" + this.name + "] " + ChatColor.BLUE + this.download + this.name + "." + this.projectID);
                System.out.println(ChatColor.GREEN + "============================================");
                update = true;

            }
        } catch (IOException e) {


        }
    }
    public void messageUpdate(Player player)
    {
        if (update)
        {
            new BukkitRunnable()
            {

                @Override
                public void run() {
                    player.sendMessage(ChatColor.GREEN +"•••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••");
                    player.sendMessage(ChatColor.WHITE +  "[" + name + "] " + ChatColor.GREEN + "There is an update! Download it at " );
                    player.sendMessage(ChatColor.WHITE +  "[" + name + "] " + ChatColor.BLUE + download + name + "." + projectID);
                    player.sendMessage(ChatColor.GREEN +"•••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••");
                }
            }.runTaskLater(this.plugin, 10*20);

        }
    }
}
