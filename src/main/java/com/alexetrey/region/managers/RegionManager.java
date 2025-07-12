package com.alexetrey.region.managers;

import com.alexetrey.region.RegionPlugin;
import com.alexetrey.region.data.Region;
import com.alexetrey.region.data.RegionFlags;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.sql.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class RegionManager {
    private final RegionPlugin plugin;
    private final Map<String, Region> regions;
    private final Map<UUID, Set<String>> playerRegions;

    public RegionManager(RegionPlugin plugin) {
        this.plugin = plugin;
        this.regions = new ConcurrentHashMap<>();
        this.playerRegions = new ConcurrentHashMap<>();
        createTables();
        loadRegions();
    }

    private void createTables() {
        try (Connection conn = plugin.getConnection();
             Statement stmt = conn.createStatement()) {
            
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS regions (
                    name VARCHAR(64) PRIMARY KEY,
                    owner VARCHAR(36) NOT NULL,
                    world VARCHAR(64) NOT NULL,
                    corner1_x INT NOT NULL,
                    corner1_y INT NOT NULL,
                    corner1_z INT NOT NULL,
                    corner2_x INT NOT NULL,
                    corner2_y INT NOT NULL,
                    corner2_z INT NOT NULL,
                    created_at BIGINT NOT NULL
                )
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS region_whitelist (
                    region_name VARCHAR(64) NOT NULL,
                    player_uuid VARCHAR(36) NOT NULL,
                    PRIMARY KEY (region_name, player_uuid),
                    FOREIGN KEY (region_name) REFERENCES regions(name) ON DELETE CASCADE
                )
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS region_flags (
                    region_name VARCHAR(64) NOT NULL,
                    flag_name VARCHAR(32) NOT NULL,
                    flag_state VARCHAR(16) NOT NULL,
                    PRIMARY KEY (region_name, flag_name),
                    FOREIGN KEY (region_name) REFERENCES regions(name) ON DELETE CASCADE
                )
            """);

        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to create database tables: " + e.getMessage());
        }
    }

    private void loadRegions() {
        try (Connection conn = plugin.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM regions")) {
            
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String name = rs.getString("name");
                UUID owner = UUID.fromString(rs.getString("owner"));
                World world = plugin.getServer().getWorld(rs.getString("world"));
                
                if (world == null) continue;
                
                Location corner1 = new Location(world, 
                    rs.getInt("corner1_x"), 
                    rs.getInt("corner1_y"), 
                    rs.getInt("corner1_z"));
                Location corner2 = new Location(world, 
                    rs.getInt("corner2_x"), 
                    rs.getInt("corner2_y"), 
                    rs.getInt("corner2_z"));
                
                Set<UUID> whitelist = loadWhitelist(name);
                RegionFlags flags = loadFlags(name);
                long createdAt = rs.getLong("created_at");
                
                Region region = new Region(name, owner, world, corner1, corner2, whitelist, flags, createdAt);
                regions.put(name, region);
                
                playerRegions.computeIfAbsent(owner, k -> new HashSet<>()).add(name);
            }
            
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to load regions: " + e.getMessage());
        }
    }

    private Set<UUID> loadWhitelist(String regionName) {
        Set<UUID> whitelist = new HashSet<>();
        try (Connection conn = plugin.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT player_uuid FROM region_whitelist WHERE region_name = ?")) {
            
            stmt.setString(1, regionName);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                whitelist.add(UUID.fromString(rs.getString("player_uuid")));
            }
            
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to load whitelist for region " + regionName + ": " + e.getMessage());
        }
        return whitelist;
    }

    private RegionFlags loadFlags(String regionName) {
        RegionFlags flags = new RegionFlags();
        try (Connection conn = plugin.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT flag_name, flag_state FROM region_flags WHERE region_name = ?")) {
            
            stmt.setString(1, regionName);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String flagName = rs.getString("flag_name");
                String flagState = rs.getString("flag_state");
                RegionFlags.IFlag flag = RegionFlags.Flag.fromName(flagName);
                if (flag != null) {
                    flags.setFlag(flag, RegionFlags.FlagState.valueOf(flagState));
                }
            }
            
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to load flags for region " + regionName + ": " + e.getMessage());
        }
        return flags;
    }

    public boolean createRegion(String name, UUID owner, Location corner1, Location corner2) {
        if (regions.containsKey(name)) return false;
        
        Region region = new Region(name, owner, corner1.getWorld(), corner1, corner2);
        
        try (Connection conn = plugin.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "INSERT INTO regions (name, owner, world, corner1_x, corner1_y, corner1_z, corner2_x, corner2_y, corner2_z, created_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)")) {
            
            stmt.setString(1, name);
            stmt.setString(2, owner.toString());
            stmt.setString(3, corner1.getWorld().getName());
            stmt.setInt(4, corner1.getBlockX());
            stmt.setInt(5, corner1.getBlockY());
            stmt.setInt(6, corner1.getBlockZ());
            stmt.setInt(7, corner2.getBlockX());
            stmt.setInt(8, corner2.getBlockY());
            stmt.setInt(9, corner2.getBlockZ());
            stmt.setLong(10, region.getCreatedAt());
            
            stmt.executeUpdate();
            
            regions.put(name, region);
            playerRegions.computeIfAbsent(owner, k -> new HashSet<>()).add(name);
            
            saveFlags(name, region.getFlags());
            
            if (plugin.getConfig().getBoolean("features.show-particles", true)) {
                showRegionParticles(region);
            }
            
            return true;
            
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to create region " + name + ": " + e.getMessage());
            return false;
        }
    }

    public boolean deleteRegion(String name) {
        Region region = regions.get(name);
        if (region == null) return false;
        
        try (Connection conn = plugin.getConnection();
             PreparedStatement stmt = conn.prepareStatement("DELETE FROM regions WHERE name = ?")) {
            
            stmt.setString(1, name);
            stmt.executeUpdate();
            
            regions.remove(name);
            playerRegions.get(region.getOwner()).remove(name);
            
            return true;
            
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to delete region " + name + ": " + e.getMessage());
            return false;
        }
    }

    public boolean addToWhitelist(String regionName, UUID playerId) {
        Region region = regions.get(regionName);
        if (region == null) return false;
        
        try (Connection conn = plugin.getConnection();
             PreparedStatement stmt = conn.prepareStatement("INSERT INTO region_whitelist (region_name, player_uuid) VALUES (?, ?)")) {
            
            stmt.setString(1, regionName);
            stmt.setString(2, playerId.toString());
            stmt.executeUpdate();
            
            region.addToWhitelist(playerId);
            return true;
            
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to add player to whitelist: " + e.getMessage());
            return false;
        }
    }

    public boolean removeFromWhitelist(String regionName, UUID playerId) {
        Region region = regions.get(regionName);
        if (region == null) return false;
        
        try (Connection conn = plugin.getConnection();
             PreparedStatement stmt = conn.prepareStatement("DELETE FROM region_whitelist WHERE region_name = ? AND player_uuid = ?")) {
            
            stmt.setString(1, regionName);
            stmt.setString(2, playerId.toString());
            stmt.executeUpdate();
            
            region.removeFromWhitelist(playerId);
            return true;
            
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to remove player from whitelist: " + e.getMessage());
            return false;
        }
    }

    private void saveFlags(String regionName, RegionFlags flags) {
        try (Connection conn = plugin.getConnection()) {
            try (PreparedStatement deleteStmt = conn.prepareStatement("DELETE FROM region_flags WHERE region_name = ?")) {
                deleteStmt.setString(1, regionName);
                deleteStmt.executeUpdate();
            }
            
            try (PreparedStatement insertStmt = conn.prepareStatement(
                "INSERT INTO region_flags (region_name, flag_name, flag_state) VALUES (?, ?, ?)")) {
                
                for (Map.Entry<RegionFlags.IFlag, RegionFlags.FlagState> entry : flags.getAllFlags().entrySet()) {
                    insertStmt.setString(1, regionName);
                    insertStmt.setString(2, entry.getKey().getName());
                    insertStmt.setString(3, entry.getValue().name());
                    insertStmt.executeUpdate();
                }
            }
            
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to save flags for region " + regionName + ": " + e.getMessage());
        }
    }

    private void showRegionParticles(Region region) {
        Location corner1 = region.getCorner1();
        Location corner2 = region.getCorner2();
        
        int minX = Math.min(corner1.getBlockX(), corner2.getBlockX());
        int maxX = Math.max(corner1.getBlockX(), corner2.getBlockX());
        int minY = Math.min(corner1.getBlockY(), corner2.getBlockY());
        int maxY = Math.max(corner1.getBlockY(), corner2.getBlockY());
        int minZ = Math.min(corner1.getBlockZ(), corner2.getBlockZ());
        int maxZ = Math.max(corner1.getBlockZ(), corner2.getBlockZ());
        
        try {
            for (int x = minX; x <= maxX; x += Math.max(1, (maxX - minX) / 10)) {
                for (int y = minY; y <= maxY; y += Math.max(1, (maxY - minY) / 10)) {
                    for (int z = minZ; z <= maxZ; z += Math.max(1, (maxZ - minZ) / 10)) {
                        if (x == minX || x == maxX || y == minY || y == maxY || z == minZ || z == maxZ) {
                            Location particleLoc = new Location(corner1.getWorld(), x, y, z);
                            corner1.getWorld().spawnParticle(org.bukkit.Particle.END_ROD, particleLoc, 1, 0, 0, 0, 0);
                        }
                    }
                }
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to show region particles: " + e.getMessage());
        }
    }

    public boolean setFlag(String regionName, RegionFlags.IFlag flag, RegionFlags.FlagState state) {
        Region region = regions.get(regionName);
        if (region == null) return false;
        
        region.getFlags().setFlag(flag, state);
        saveFlags(regionName, region.getFlags());
        return true;
    }

    public Region getRegion(String name) {
        return regions.get(name);
    }

    public Region getRegionAt(Location location) {
        return regions.values().stream()
                .filter(region -> region.contains(location))
                .findFirst()
                .orElse(null);
    }

    public Set<Region> getPlayerRegions(UUID playerId) {
        Set<String> regionNames = playerRegions.get(playerId);
        if (regionNames == null) return new HashSet<>();
        
        Set<Region> playerRegions = new HashSet<>();
        for (String name : regionNames) {
            Region region = regions.get(name);
            if (region != null) {
                playerRegions.add(region);
            }
        }
        return playerRegions;
    }

    public boolean hasPermission(Player player, Location location, RegionFlags.IFlag flag) {
        Region region = getRegionAt(location);
        if (region == null) return true;
        
        if (player.hasPermission("region.bypass")) return true;
        
        boolean isWhitelisted = region.isOwner(player.getUniqueId()) || region.isWhitelisted(player.getUniqueId());
        return region.getFlags().isAllowed(flag, isWhitelisted);
    }

    public boolean renameRegion(String oldName, String newName) {
        Region region = regions.get(oldName);
        if (region == null) return false;
        
        try (Connection conn = plugin.getConnection()) {
            try (PreparedStatement stmt = conn.prepareStatement("UPDATE regions SET name = ? WHERE name = ?")) {
                stmt.setString(1, newName);
                stmt.setString(2, oldName);
                stmt.executeUpdate();
            }
            
            try (PreparedStatement stmt = conn.prepareStatement("UPDATE region_whitelist SET region_name = ? WHERE region_name = ?")) {
                stmt.setString(1, newName);
                stmt.setString(2, oldName);
                stmt.executeUpdate();
            }
            
            try (PreparedStatement stmt = conn.prepareStatement("UPDATE region_flags SET region_name = ? WHERE region_name = ?")) {
                stmt.setString(1, newName);
                stmt.setString(2, oldName);
                stmt.executeUpdate();
            }
            
            regions.remove(oldName);
            regions.put(newName, region);
            
            Set<String> playerRegionNames = playerRegions.get(region.getOwner());
            if (playerRegionNames != null) {
                playerRegionNames.remove(oldName);
                playerRegionNames.add(newName);
            }
            
            return true;
            
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to rename region " + oldName + " to " + newName + ": " + e.getMessage());
            return false;
        }
    }

    public boolean redefineRegion(String regionName, Location newCorner1, Location newCorner2) {
        Region oldRegion = regions.get(regionName);
        if (oldRegion == null) return false;
        
        try (Connection conn = plugin.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "UPDATE regions SET corner1_x = ?, corner1_y = ?, corner1_z = ?, corner2_x = ?, corner2_y = ?, corner2_z = ? WHERE name = ?")) {
            
            stmt.setInt(1, newCorner1.getBlockX());
            stmt.setInt(2, newCorner1.getBlockY());
            stmt.setInt(3, newCorner1.getBlockZ());
            stmt.setInt(4, newCorner2.getBlockX());
            stmt.setInt(5, newCorner2.getBlockY());
            stmt.setInt(6, newCorner2.getBlockZ());
            stmt.setString(7, regionName);
            
            stmt.executeUpdate();
            
            Region newRegion = new Region(regionName, oldRegion.getOwner(), newCorner1.getWorld(), 
                newCorner1, newCorner2, oldRegion.getWhitelist(), oldRegion.getFlags(), oldRegion.getCreatedAt());
            
            regions.put(regionName, newRegion);
            
            if (plugin.getConfig().getBoolean("features.show-particles", true)) {
                showRegionParticles(newRegion);
            }
            
            return true;
            
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to redefine region " + regionName + ": " + e.getMessage());
            return false;
        }
    }
} 