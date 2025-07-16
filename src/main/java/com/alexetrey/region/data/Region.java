package com.alexetrey.region.data;

import org.bukkit.Location;
import org.bukkit.World;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class Region {
    private final String name;
    private final World world;
    private final Location corner1;
    private final Location corner2;
    private final RegionFlags flags;
    private final long createdAt;
    private final Set<UUID> whitelist;

    public Region(String name, World world, Location corner1, Location corner2) {
        this.name = name;
        this.world = world;
        this.corner1 = corner1;
        this.corner2 = corner2;
        this.flags = new RegionFlags();
        this.createdAt = System.currentTimeMillis();
        this.whitelist = new HashSet<>();
    }

    public Region(String name, World world, Location corner1, Location corner2, Set<UUID> whitelist, RegionFlags flags, long createdAt) {
        this.name = name;
        this.world = world;
        this.corner1 = corner1;
        this.corner2 = corner2;
        this.flags = flags != null ? flags : new RegionFlags();
        this.createdAt = createdAt;
        this.whitelist = whitelist != null ? whitelist : new HashSet<>();
    }

    public boolean contains(Location location) {
        if (!location.getWorld().equals(world)) return false;
        
        int x = location.getBlockX();
        int y = location.getBlockY();
        int z = location.getBlockZ();
        
        int minX = Math.min(corner1.getBlockX(), corner2.getBlockX());
        int maxX = Math.max(corner1.getBlockX(), corner2.getBlockX());
        int minY = Math.min(corner1.getBlockY(), corner2.getBlockY());
        int maxY = Math.max(corner1.getBlockY(), corner2.getBlockY());
        int minZ = Math.min(corner1.getBlockZ(), corner2.getBlockZ());
        int maxZ = Math.max(corner1.getBlockZ(), corner2.getBlockZ());
        
        return x >= minX && x <= maxX && y >= minY && y <= maxY && z >= minZ && z <= maxZ;
    }

    public Set<UUID> getWhitelist() { return whitelist; }
    public void addToWhitelist(UUID uuid) { whitelist.add(uuid); }
    public void removeFromWhitelist(UUID uuid) { whitelist.remove(uuid); }
    public boolean isWhitelisted(UUID uuid) { return whitelist.contains(uuid); }

    public String getName() { return name; }
    public World getWorld() { return world; }
    public Location getCorner1() { return corner1; }
    public Location getCorner2() { return corner2; }
    public RegionFlags getFlags() { return flags; }
    public long getCreatedAt() { return createdAt; }
} 