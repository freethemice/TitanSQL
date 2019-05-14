package com.firesoftitan.play.titansql;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import javax.xml.bind.DatatypeConverter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class TitanSQL extends JavaPlugin {

    //private Connection connection;
    private HashMap<String, Database> connections;
    public static TitanSQL instance;
    public static String titan_mysql_host;
    public static String titan_mysql_port;
    public static String titan_mysql_database;
    public static String titan_mysql_username;
    public static String titan_mysql_password;
    public static boolean titan_mysql_enabled;
    public static HashMap<Integer, BukkitRunnable> tasksSaver = new HashMap<Integer, BukkitRunnable>();
    private Timer saver = new Timer();
    private Long lastSaved = Long.valueOf(0);
    private File configFile;
    private FileConfiguration config;
    private static boolean disabled = false;
    private List<TimerTask> running = new ArrayList<TimerTask>();
    private List<Table> allTables = new ArrayList<Table>();
    public TitanRunnable saverCheck;
    private HashMap<String, PreparedStatementHolder> preparedStatementBulkData = new HashMap<String, PreparedStatementHolder>();
    private UpdateChecker updateChecker;
    public TitanSQL()
    {

    }
    public void addPreparedStatement(String Type, PreparedStatement preparedStatement)
    {
        if (!preparedStatementBulkData.containsKey(Type)) {
            PreparedStatementHolder tmp = new PreparedStatementHolder(preparedStatement);
            preparedStatementBulkData.put(Type, tmp);
        }
    }
    public PreparedStatement getPreparedStatement(String type)
    {
        PreparedStatementHolder tmp = preparedStatementBulkData.get(type);
        if (tmp == null) return null;
        return tmp.getPreparedStatement();
    }
    public PreparedStatementHolder getPreparedStatementHolder(String type)
    {
        PreparedStatementHolder tmp = preparedStatementBulkData.get(type);
        if (tmp == null) return null;
        return tmp;
    }
    public void removePreparedStatement(String type)
    {
        preparedStatementBulkData.remove(type);
    }
    public void addTable(Table table)
    {
        allTables.add(table);
    }
    public void addBulkCount(String type)
    {
        PreparedStatementHolder tmp = preparedStatementBulkData.get(type);
        if (tmp == null) return;
        tmp.addCount();
    }

    public int getBuilkDataCount(String type) {
        PreparedStatementHolder tmp = preparedStatementBulkData.get(type);
        if (tmp == null) return 0;
        return tmp.getCount();
    }
    public void sendDataBulk(String type, boolean thread)
    {
        if (!preparedStatementBulkData.containsKey(type)) return;
        final String trace =  TitanSQL.getTrace();
        final String simpletrace =  TitanSQL.getSimpleTrace();
        final String pluginName = TitanSQL.getPlugin();

        final PreparedStatementHolder tmpST = preparedStatementBulkData.get(type);
        if (tmpST.getCount() <= 0) return;
        preparedStatementBulkData.remove(type);
        TimerTask adding = new TimerTask() {
            @Override
            public void run() {
                System.out.println("[TitanSQL]: Sending data to MySql, queued (max:" + TitanSQL.instance.getQueued_size() + "): " + tmpST.getCount());
                try {
                    tmpST.getPreparedStatement().executeBatch();
                } catch (SQLException e) {
                    try {
                        e.printStackTrace();
                        System.out.println("[TitanSQL]: batch failed, adding to next que.");
                        addPreparedStatement(type, getDatebase().getConnection().prepareStatement(type));
                    } catch (SQLException e1) {
                        e.printStackTrace();
                        System.out.println("----------------------------------");
                        e1.printStackTrace();
                        System.out.println("----------------------------------");
                    }
                } finally {
                    running.remove(this);
                    close(tmpST.getPreparedStatement());
                    if (running.size() > 0) {
                        saver.schedule(running.get(0), 10);
                    }
                }
            }
        };
        if (thread)
        {
            running.add(adding);
            //saver.schedule(adding, 10);
        }
        else
        {
            adding.run();
        }
        /*
        TitanRunnable tmpY = new TitanRunnable(pluginName, simpletrace) {

            @Override
            public void run() {
                System.out.println("[TitanSQL]: Sending data to MySql, queued: " + tmpST.getCount() + "/" + TitanSQL.instance.getQueued_size());
                try {
                    //System.out.println(tmpST.getPreparedStatement().toString());
                    tmpST.getPreparedStatement().executeBatch();
                } catch (SQLException e) {
                    e.printStackTrace();
                    System.out.println("----------------------------------");
                    System.out.println(trace);
                    System.out.println("----------------------------------");
                } finally {
                    close(tmpST.getPreparedStatement());
                }
            }
        };
        if (thread) {
            tmpY.runTask(TitanSQL.instance);
            TitanSQL.instance.tasksSaver.put(tmpY.getTaskId(), tmpY);
        }
        else
        {
            tmpY.run();
        }*/
    }
    public void forceSaveAll()
    {
        if (saverCheck != null)
        {
            saverCheck.run();
        }
    }
    private void close(PreparedStatement ps) {
        close(ps, null);
    }
    private void close(PreparedStatement ps, ResultSet rs) {
        try {
            if (ps != null)
                ps.close();
            if (rs != null)
                rs.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    private void setSaverCheck()
    {
        System.out.println("[TitanSQL]: setting up threads");
        final String trace =  TitanSQL.getTrace();
        final String simpletrace =  TitanSQL.getSimpleTrace();
        saverCheck = new TitanRunnable("TitanSQL", trace) {
            @Override
            public void run() {


                Database myData = connections.get("default");
                myData.ping();
                if (!myData.isConnected())
                {
                    return;
                }
                try {
                    Set<String> keys = preparedStatementBulkData.keySet();
                    List<String> keyss = new ArrayList<>(keys);
                    List<String> deletes = new ArrayList<String>();
                    System.out.println("[TitanSQL]: Checking for data that needs to be sent: " + keyss.size());
                    for(String key: keyss) {
                        if (key.startsWith("DELETE FROM "))
                        {
                            deletes.add(key);
                        }
                        else {
                            sendDataBulk(key, true);
                        }
                    }
                    System.out.println("[TitanSQL]: Checking for data that needs to be deleted: " + deletes.size());
                    for(String key: deletes) {
                        sendDataBulk(key, true);
                    }
                    lastSaved = System.currentTimeMillis();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                finally {
                    if (running.size() > 0) {
                        System.out.println("[TitanSQL]: sending...");
                        saver.schedule(running.get(0), 10);
                    }
                    else
                    {
                        System.out.println("[TitanSQL]: Done.");
                    }
                }
            }
        };
        int savetime = 5*60*20;
        saverCheck.runTaskTimer(TitanSQL.instance, savetime, savetime);
    }
    public Long getLastSaved()
    {
        return lastSaved;
    }
    public static boolean isDisabled() {
        return disabled;
    }
    public static String getPlugin() {
        StackTraceElement[] AllCalls = Thread.currentThread().getStackTrace();
        Plugin[] AllPlugins  = Bukkit.getPluginManager().getPlugins();
        for (int i = 3; i < AllCalls.length; i++)
        {
            StackTraceElement callingFrame = AllCalls[i];
            String splitter[] = callingFrame.toString().split("\\.");
            for(String name: splitter)
            {
                for (Plugin p: AllPlugins)
                {
                    if (p.getName().equalsIgnoreCase(name))
                    {
                        return p.getName();
                    }
                }
            }
        }
        return "Unknown";
    }
    public int getQueued_size() {
        return 1000;
    }

    public static String getSimpleTrace() {
        StackTraceElement[] AllCalls = Thread.currentThread().getStackTrace();
        String calls = "";
        for (int i = 3; i < AllCalls.length; i++)
        {
            StackTraceElement callingFrame = AllCalls[i];
            return  callingFrame + "-->\n";
        }
        return "No Trace???";
    }
    public static String getTrace() {
        StackTraceElement[] AllCalls = Thread.currentThread().getStackTrace();
        String calls = "";
        for (int i = 2; i < AllCalls.length; i++)
        {
            StackTraceElement callingFrame = AllCalls[i];
            calls  = calls + callingFrame + "-->\n";
        }
        return calls;
    }
    public void onDisable()
    {
        disabled =true;
        saverCheck.run();
        long timepass = System.currentTimeMillis();
        int sizeleft = printThreadCounts();
        long killTimer = System.currentTimeMillis() + 300000;
        while (sizeleft > 0 || running.size() > 0)
        {
            if (System.currentTimeMillis() >= killTimer)
            {
                System.out.println("Killing Threads...");
                break;
            }
            if (System.currentTimeMillis() - timepass > 15000)
            {
                timepass = System.currentTimeMillis();
                sizeleft = printThreadCounts();
                sizeleft += running.size();
                int SecondsLeft = (int) ((killTimer - System.currentTimeMillis()) / 1000);
                System.out.println("Killing Threads in: " +SecondsLeft + " Seconds");
            }
        }
        killThreadCounts();
        saverCheck.cancel();
        saverCheck = null;
    }

    private void killThreadCounts() {
        List<BukkitTask> tmpTask = Bukkit.getScheduler().getPendingTasks();
        for(TimerTask timerTask: running)
        {
            timerTask.cancel();
        }
        for(BukkitTask BT: tmpTask)
        {
            if (BT.getOwner().getName().equals(this.getName())) {
                if (!BT.isCancelled()) {
                    BT.cancel();
                }
            }
        }
    }

    private int printThreadCounts() {
        List<BukkitTask> tmpTask = Bukkit.getScheduler().getPendingTasks();
        List<BukkitRunnable> waitingCount = new ArrayList<BukkitRunnable>();
        List<BukkitTask> waitingCountNoPlugin = new ArrayList<BukkitTask>();
        for(BukkitTask BT: tmpTask) {
            if (!BT.isCancelled()) {
                if (BT.getOwner().getName().equals(this.getName())) {

                    if (tasksSaver.containsKey(BT.getTaskId())) {
                        waitingCount.add(tasksSaver.get(BT.getTaskId()));
                    }
                } else {
                    waitingCountNoPlugin.add(BT);
                }
            }
        }
        int size = running.size() + waitingCount.size();
        System.out.println("[TitanSQL]: There are threads being saved: " + size + " (+" + waitingCountNoPlugin.size() + ")");
        for (BukkitRunnable key: waitingCount)
        {
            if (key instanceof  TitanRunnable) {
                TitanRunnable keyT = (TitanRunnable)key;
                String running = Bukkit.getScheduler().isCurrentlyRunning(keyT.getTaskId()) +"";
                String qued = Bukkit.getScheduler().isQueued(keyT.getTaskId()) +"";
                System.out.println("----------------------- [Running:" + running + ", Queued:" + qued + "] "  + keyT.plugin + " (" + keyT.time + ") -----------------------\n" + keyT.trace);
                if (!Bukkit.getScheduler().isCurrentlyRunning(keyT.getTaskId()))
                {
                    System.out.println("[TitanSQL]: Running...");
                    keyT.run();
                    keyT.cancel();
                }
            }
        }

        /*for (BukkitTask key: waitingCountNoPlugin)
        {
            ((CraftTask)key).run();
            System.out.println(key.getOwner().getName());
        }*/
        System.out.println("[TitanSQL]: End List");

        return  waitingCount.size();
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
        if (!this.config.contains("mysql.enabled"))
        {
            this.config.set("mysql.enabled", false);
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
        titan_mysql_enabled = this.config.getBoolean("mysql.enabled");
        if (!titan_mysql_enabled)
        {
            System.out.println("[TitanSQL]: I'm disabled, enable me in the config file.");
            return;
        }

        connections = new HashMap<String, Database>();

        Database myDefault = new Database(titan_mysql_database,true);
        connections.put("default", myDefault);
        connections.put(titan_mysql_database, myDefault);


        Bukkit.getScheduler().runTaskTimer(this, new Runnable() {
            @Override
            public void run() {
                for (String key: connections.keySet())
                {
                    if (!key.equals("defualt"))
                    {
                        connections.get(key).ping();
                    }
                }
            }
        }, 3600 * 20L, 3600 * 20L);

        setSaverCheck();
        System.out.println("[TitanSQL]: Initialized and Enabled.");

        updateChecker = new UpdateChecker(this, "65264");
        updateChecker.runTaskLater(this, 3*20);

    }


    public Database getDatebase() {

        return connections.get("default");
    }
    public Database getDatebase(String mysql_database) {

        if (connections.containsKey(mysql_database)) {
            return connections.get(mysql_database);
        }
        else {
            return  null;
        }
    }
    public void addDatabase(String mysql_database, boolean KeepAlive)
    {
        Database mydatabase = new Database(mysql_database,KeepAlive);
        connections.put(mysql_database, mydatabase);
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
