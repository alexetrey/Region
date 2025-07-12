package com.alexetrey.region.managers;

import com.alexetrey.region.RegionPlugin;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class WandManager {
    private final RegionPlugin plugin;
    private final Map<UUID, Location> corner1;
    private final Map<UUID, Location> corner2;

    public WandManager(RegionPlugin plugin) {
        this.plugin = plugin;
        this.corner1 = new HashMap<>();
        this.corner2 = new HashMap<>();
    }

    public void giveWand(Player player) {
        ItemStack wand = new ItemStack(Material.STICK);
        ItemMeta meta = wand.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§6§lRegion Selection Wand");
            meta.setLore(java.util.Arrays.asList(
                "§7Left-click to set corner 1",
                "§7Right-click to set corner 2",
                "§7Use /region create <name> to create region"
            ));
            wand.setItemMeta(meta);
        }
        
        player.getInventory().addItem(wand);
        player.sendMessage("§aYou received a region selection wand!");
    }

    public void setCorner1(Player player, Location location) {
        corner1.put(player.getUniqueId(), location);
        player.sendMessage("§aCorner 1 set at: §f" + formatLocation(location));
        
        if (corner2.containsKey(player.getUniqueId())) {
            player.sendMessage("§aBoth corners selected! Use /region create <name> to create the region.");
        }
    }

    public void setCorner2(Player player, Location location) {
        corner2.put(player.getUniqueId(), location);
        player.sendMessage("§aCorner 2 set at: §f" + formatLocation(location));
        
        if (corner1.containsKey(player.getUniqueId())) {
            player.sendMessage("§aBoth corners selected! Use /region create <name> to create the region.");
        }
    }

    public Location getCorner1(Player player) {
        return corner1.get(player.getUniqueId());
    }

    public Location getCorner2(Player player) {
        return corner2.get(player.getUniqueId());
    }

    public boolean hasBothCorners(Player player) {
        return corner1.containsKey(player.getUniqueId()) && corner2.containsKey(player.getUniqueId());
    }

    public void clearSelection(Player player) {
        corner1.remove(player.getUniqueId());
        corner2.remove(player.getUniqueId());
        player.sendMessage("§cRegion selection cleared.");
    }

    public boolean validateSelection(Player player) {
        Location c1 = getCorner1(player);
        Location c2 = getCorner2(player);
        
        if (c1 == null || c2 == null) {
            player.sendMessage("§cYou need to select both corners first!");
            return false;
        }
        
        if (!c1.getWorld().equals(c2.getWorld())) {
            player.sendMessage("§cBoth corners must be in the same world!");
            return false;
        }
        
        return true;
    }

    private String formatLocation(Location location) {
        return String.format("%s (%d, %d, %d)", 
            location.getWorld().getName(),
            location.getBlockX(),
            location.getBlockY(),
            location.getBlockZ()
        );
    }

    public boolean isWand(ItemStack item) {
        if (item == null || item.getType() != Material.STICK) return false;
        
        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) return false;
        
        return meta.getDisplayName().equals("§6§lRegion Selection Wand");
    }
} 