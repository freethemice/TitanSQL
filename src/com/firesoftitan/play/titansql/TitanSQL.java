package com.firesoftitan.play.titansql;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import javax.xml.bind.DatatypeConverter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class TitanSQL extends JavaPlugin {

    private Connection connection;
    public static TitanSQL instance;
    private String titan_mysql_host;
    private String titan_mysql_port;
    private String titan_mysql_database;
    private String titan_mysql_username;
    private String titan_mysql_password;
    private File configFile;
    private FileConfiguration config;
    public TitanSQL()
    {

    }
    public void onEnable(){
        instance = this;
        this.configFile = new File("plugins/" + instance.getDescription().getName().replace(" ", "_") + "/config.yml");
        this.config = YamlConfiguration.loadConfiguration(this.configFile);

        if (!this.config.contains("mysql.host"))
        {
            this.config.set("mysql.host", "Host_Adrress_Here");
        }
        if (!this.config.contains("mysql.port"))
        {
            this.config.set("mysql.port", "3306");
        }
        if (!this.config.contains("mysql.database"))
        {
            this.config.set("mysql.database", "Database_Name_Here");
        }
        if (!this.config.contains("mysql.username"))
        {
            this.config.set("mysql.username", "Username_Name_Here");
        }
        if (!this.config.contains("mysql.password"))
        {
            this.config.set("mysql.password", "Password_Name_Here");
        }
        try {
            this.config.save(configFile);
        } catch (IOException e) {
            e.printStackTrace();
        }

        titan_mysql_host = this.config.getString("mysql.host");
        titan_mysql_port = this.config.getString("mysql.port");
        titan_mysql_database = this.config.getString("mysql.database");
        titan_mysql_username = this.config.getString("mysql.username");
        titan_mysql_password = this.config.getString("mysql.password");

        connection = Initialize();

        Bukkit.getScheduler().runTaskTimer(this, new Runnable() {
            @Override
            public void run() {
                try (PreparedStatement ps = connection.prepareStatement("/* ping */ SELECT 1")) {
                    ps.executeQuery();
                } catch (SQLException ex) {
                    disconnect();

                    connection = Initialize();
                }
            }
        }, 3600 * 20L, 3600 * 20L);
        System.out.println("[TitanSQL]: Initialized and Enabled.");

    }
    private Connection Initialize() {
        try {
            if (connection != null && !connection.isClosed()) {
                return connection;
            }

            Class.forName("com.mysql.jdbc.Driver");

            String connectUrl = "jdbc:mysql://" + titan_mysql_host + ":" + titan_mysql_port + "/" + titan_mysql_database + "?autoReconnect=true&useSSL=false";

            connection = DriverManager.getConnection(connectUrl, titan_mysql_username, titan_mysql_password);

            return connection;
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }

    public Connection getConnection() {
        return connection;
    }

    public void disconnect() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public void setDatabase(String mysql_database) {
        this.titan_mysql_database = mysql_database;
    }
    public void setHost(String mysql_host) {
        this.titan_mysql_host = mysql_host;
    }
    public void setPort(String mysql_port) {
        this.titan_mysql_port = mysql_port;
    }
    public void setUsername(String mysql_username) {
        this.titan_mysql_username = mysql_username;
    }
    public void setPassword(String mysql_password) {
        this.titan_mysql_password = mysql_password;
    }

    /**
     * Encodes an {@link ItemStack} in a Base64 String
     * @param itemStack {@link ItemStack} to encode
     * @return Base64 encoded String
     */
    public static String encode(ItemStack itemStack) {
        YamlConfiguration config = new YamlConfiguration();
        config.set("i", itemStack);
        return DatatypeConverter.printBase64Binary(config.saveToString().getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Encodes an {@link Location} in a Base64 String
     * @param location {@link Location} to encode
     * @return Base64 encoded String
     */
    public static String encode(Location location) {
        YamlConfiguration config = new YamlConfiguration();
        config.set("i.x", location.getX());
        config.set("i.y", location.getY());
        config.set("i.z", location.getZ());
        config.set("i.pitch", location.getPitch() + "");
        config.set("i.yaw", location.getYaw() + "");
        if (location.getWorld() == null)
        {
            config.set("i.world", "worldmain");
        }
        else {
            config.set("i.world", location.getWorld().getName());
        }
        return DatatypeConverter.printBase64Binary(config.saveToString().getBytes(StandardCharsets.UTF_8));
    }
    /**
     * Encodes an {@link List<Integer>} in a Base64 String
     * Encodes an {@link List<String>} in a Base64 String
     * Encodes an {@link List<ItemStack>} in a Base64 String
     * @param list {@link List} to encode
     * @return Base64 encoded String
     */
    public static String encode(List list) {
        YamlConfiguration config = new YamlConfiguration();
        if (list.size() > 0)
        {
            if (list.get(0) instanceof  ItemStack)
            {
                int i = 0;
                for (ItemStack is: (List<ItemStack>)list)
                {
                    config.set("i" + i, is);
                    i++;
                }
                return DatatypeConverter.printBase64Binary(config.saveToString().getBytes(StandardCharsets.UTF_8));
            }
        }
        config.set("i", list);
        return DatatypeConverter.printBase64Binary(config.saveToString().getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Decodes an {@link ItemStack} from a Base64 String
     * @param string Base64 encoded String to decode
     * @return Decoded {@link ItemStack}
     */
    public static ItemStack decodeItemStack(String string) {
        YamlConfiguration config = new YamlConfiguration();
        try {
            config.loadFromString(new String(DatatypeConverter.parseBase64Binary(string), StandardCharsets.UTF_8));
        } catch (IllegalArgumentException | InvalidConfigurationException e) {
            e.printStackTrace();
            return null;
        }
        return config.getItemStack("i", null);
    }


    /**
     * Decodes an {@link ItemStack} from a Base64 String
     * @param string Base64 encoded String to decode
     * @return Decoded {@link ItemStack}
     */
    public static List<ItemStack> decodeItemList(String string) {
        YamlConfiguration config = new YamlConfiguration();
        try {
            config.loadFromString(new String(DatatypeConverter.parseBase64Binary(string), StandardCharsets.UTF_8));
        } catch (IllegalArgumentException | InvalidConfigurationException e) {
            e.printStackTrace();
            return null;
        }
        List<ItemStack> tmp = new ArrayList<ItemStack>();
        for (String key: config.getKeys(false))
        {
            ItemStack itsub = config.getItemStack(key);
            tmp.add(itsub);
        }

        return tmp;
    }

    /**
     * Decodes an {@link ItemStack} from a Base64 String
     * @param string Base64 encoded String to decode
     * @return Decoded {@link ItemStack}
     */
    public static List<String> decodeStringList(String string) {
        YamlConfiguration config = new YamlConfiguration();
        try {
            config.loadFromString(new String(DatatypeConverter.parseBase64Binary(string), StandardCharsets.UTF_8));
        } catch (IllegalArgumentException | InvalidConfigurationException e) {
            e.printStackTrace();
            return null;
        }
        List<String> tmp = (List<String>) config.getList("i", new ArrayList<String>());
        return tmp;
    }
    /**
     * Decodes an {@link ItemStack} from a Base64 String
     * @param string Base64 encoded String to decode
     * @return Decoded {@link ItemStack}
     */
    public static List<Integer> decodeIntList(String string) {
        YamlConfiguration config = new YamlConfiguration();
        try {
            config.loadFromString(new String(DatatypeConverter.parseBase64Binary(string), StandardCharsets.UTF_8));
        } catch (IllegalArgumentException | InvalidConfigurationException e) {
            e.printStackTrace();
            return null;
        }
        List<Integer> tmp = (List<Integer>) config.getList("i", new ArrayList<Integer>());
        return tmp;
    }

    /**
     * Decodes an {@link Location} from a Base64 String
     * @param string Base64 encoded String to decode
     * @return Decoded {@link Location}
     */
    public static Location decodeLocation(String string) {
        YamlConfiguration config = new YamlConfiguration();
        try {
            config.loadFromString(new String(DatatypeConverter.parseBase64Binary(string), StandardCharsets.UTF_8));
        } catch (IllegalArgumentException | InvalidConfigurationException e) {
            e.printStackTrace();
            return null;
        }
        double x = config.getDouble("i.x");
        double y = config.getDouble("i.y");
        double z = config.getDouble("i.z");
        float pitch =  Float.valueOf(config.getString("i.pitch"));
        float yaw = Float.valueOf(config.getString("i.yaw"));
        String worldname = config.getString("i.world");
        World world = Bukkit.getWorld(worldname);
        Location location = new Location(world, x, y, z, yaw, pitch);
        return  location.clone();
    }
}
