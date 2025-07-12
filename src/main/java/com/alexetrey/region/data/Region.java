package com.alexetrey.region.data;

import org.bukkit.Location;
import org.bukkit.World;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class Region {
    private final String name;
    private final UUID owner;
    private final World world;
    private final Location corner1;
    private final Location corner2;
    private final Set<UUID> whitelist;
    private final RegionFlags flags;
    private final long createdAt;

    public Region(String name, UUID owner, World world, Location corner1, Location corner2) {
        this.name = name;
        this.owner = owner;
        this.world = world;
        this.corner1 = corner1;
        this.corner2 = corner2;
        this.whitelist = new HashSet<>();
        this.flags = new RegionFlags();
        this.createdAt = System.currentTimeMillis();
    }

    public Region(String name, UUID owner, World world, Location corner1, Location corner2, 
                  Set<UUID> whitelist, RegionFlags flags, long createdAt) {
        this.name = name;
        this.owner = owner;
        this.world = world;
        this.corner1 = corner1;
        this.corner2 = corner2;
        this.whitelist = whitelist != null ? whitelist : new HashSet<>();
        this.flags = flags != null ? flags : new RegionFlags();
        this.createdAt = createdAt;
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

    public boolean isOwner(UUID playerId) {
        return owner.equals(playerId);
    }

    public boolean isWhitelisted(UUID playerId) {
        return whitelist.contains(playerId);
    }

    public void addToWhitelist(UUID playerId) {
        whitelist.add(playerId);
    }

    public void removeFromWhitelist(UUID playerId) {
        whitelist.remove(playerId);
    }

    public String getName() { return name; }
    public UUID getOwner() { return owner; }
    public World getWorld() { return world; }
    public Location getCorner1() { return corner1; }
    public Location getCorner2() { return corner2; }
    public Set<UUID> getWhitelist() { return new HashSet<>(whitelist); }
    public RegionFlags getFlags() { return flags; }
    public long getCreatedAt() { return createdAt; }
} 