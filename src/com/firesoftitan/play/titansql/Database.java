package com.firesoftitan.play.titansql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class Database {
    private Connection connection;
    private boolean keepAlive;
    private String mysql_database;
    private boolean connected = false;
    public Database(String mysql_database, boolean KeepAlive)
    {
        this.mysql_database = mysql_database;
        this.keepAlive = KeepAlive;
        this.setConnection();
    }
    private void setConnection()
    {
        connection = Initialize();
        long timeFist = System.currentTimeMillis();
        long time = System.currentTimeMillis();
        long lastCount = 0;
        while (connection==null)
        {
            long timePassed = System.currentTimeMillis() - time;
            timePassed = timePassed / 1000; //seconds
            if (lastCount != timePassed) {
                if (timePassed > -1) {
                    System.out.println("[TitanSQL]: Trying to reconnect again in " + (10 - timePassed));
                    lastCount = timePassed;
                }
            }
            if (timePassed > 9) {
                connection = Initialize();
                lastCount = 0;
                time = System.currentTimeMillis();
            }
        }
        connected = true;
    }
    private Connection Initialize() {
        try {
            System.out.println("[TitanSQL]: Connecting to MySQL...");
            Class.forName("com.mysql.jdbc.Driver");

            String connectUrl = "jdbc:mysql://" + TitanSQL.titan_mysql_host + ":" + TitanSQL.titan_mysql_port + "/" + mysql_database + "?autoReconnect=true&useSSL=false";

            Connection connection = DriverManager.getConnection(connectUrl, TitanSQL.titan_mysql_username, TitanSQL.titan_mysql_password);

            System.out.println("[TitanSQL]: Connection establish...");
            return connection;
        } catch (Exception ex) {
            //ex.printStackTrace();
            System.out.println("[TitanSQL]: Failed to connect to MySQL, tried 3 times. Trying again in 10 Seconds");
        }

        return null;
    }

    public boolean isConnected() {
        return connected;
    }

    public Connection getConnection() {
        return connection;
    }

    public void ping()
    {
        if (keepAlive) {
            try  {
                PreparedStatement ps = connection.prepareStatement("/* ping */ SELECT 1");
                ps.executeQuery();
            } catch (Exception ex) {
                connected = false;
                //disconnect();
                connection = Initialize();
                if (connection != null)
                    connected = true;
            }
        }
    }
    public void disconnect() {
        connected = false;
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
