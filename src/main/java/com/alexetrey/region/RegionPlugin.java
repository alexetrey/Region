package com.alexetrey.region;

import org.bukkit.plugin.java.JavaPlugin;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import com.alexetrey.region.managers.RegionManager;
import com.alexetrey.region.managers.WandManager;
import com.alexetrey.region.commands.RegionCommand;
import com.alexetrey.region.listeners.RegionListener;
import com.alexetrey.region.listeners.WandListener;
import com.alexetrey.region.gui.GUIManager;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import fr.minuskube.inv.InventoryManager;
import fr.minuskube.inv.SmartInvsPlugin;

import java.sql.Connection;
import java.sql.SQLException;

public class RegionPlugin extends JavaPlugin {
    private static RegionPlugin instance;
    private HikariDataSource dataSource;
    private RegionManager regionManager;
    private WandManager wandManager;
    private GUIManager guiManager;
    private InventoryManager invManager;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        setupDatabase();
        
        invManager = new InventoryManager(this);
        invManager.init();
        
        regionManager = new RegionManager(this);
        wandManager = new WandManager(this);
        guiManager = new GUIManager(this);
        
        getCommand("region").setExecutor(new RegionCommand(this));
        getCommand("region").setTabCompleter(new RegionCommand(this));
        
        Bukkit.getPluginManager().registerEvents(new RegionListener(this), this);
        Bukkit.getPluginManager().registerEvents(new WandListener(this), this);
        
        getLogger().info("Region plugin enabled");
    }

    @Override
    public void onDisable() {
        if (dataSource != null) {
            dataSource.close();
        }
        getLogger().info("Region plugin disabled");
    }

    private void setupDatabase() {
        FileConfiguration config = getConfig();
        String storageType = config.getString("storage.type", "sqlite");
        
        if ("mysql".equalsIgnoreCase(storageType)) {
            setupMySQL(config);
        } else {
            setupSQLite();
        }
    }
    
    private void setupMySQL(FileConfiguration config) {
        String host = config.getString("mysql.host", "localhost");
        int port = config.getInt("mysql.port", 3306);
        String database = config.getString("mysql.database", "regions");
        String user = config.getString("mysql.user", "root");
        String password = config.getString("mysql.password", "");

        getLogger().info("Attempting to connect to MySQL database:");
        getLogger().info("  Host: " + host);
        getLogger().info("  Port: " + port);
        getLogger().info("  Database: " + database);
        getLogger().info("  User: " + user);

        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + database + "?useSSL=false&autoReconnect=true");
        hikariConfig.setUsername(user);
        hikariConfig.setPassword(password);
        hikariConfig.setMaximumPoolSize(10);
        hikariConfig.setPoolName("RegionHikariCP");

        try {
            dataSource = new HikariDataSource(hikariConfig);
        } catch (Exception e) {
            getLogger().severe("Failed to initialize MySQL connection pool!");
            getLogger().severe("Host: " + host + ", Port: " + port + ", Database: " + database + ", User: " + user);
            getLogger().severe("Error: " + e.getMessage());
            throw e;
        }
    }
    
    private void setupSQLite() {
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl("jdbc:sqlite:plugins/Region/regions.db");
        hikariConfig.setMaximumPoolSize(1);
        hikariConfig.setPoolName("RegionHikariCP");
        
        dataSource = new HikariDataSource(hikariConfig);
    }

    public static RegionPlugin getInstance() {
        return instance;
    }

    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public RegionManager getRegionManager() {
        return regionManager;
    }

    public WandManager getWandManager() {
        return wandManager;
    }

    public GUIManager getGuiManager() {
        return guiManager;
    }
    
    public InventoryManager getInvManager() {
        return invManager;
    }
} 