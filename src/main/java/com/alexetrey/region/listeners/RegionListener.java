package com.alexetrey.region.listeners;

import com.alexetrey.region.RegionPlugin;
import com.alexetrey.region.data.Region;
import com.alexetrey.region.data.RegionFlags;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;

public class RegionListener implements Listener {
    private final RegionPlugin plugin;

    public RegionListener(RegionPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Location location = event.getBlock().getLocation();
        
        if (!plugin.getRegionManager().hasPermission(player, location, RegionFlags.Flag.BLOCK_BREAK)) {
            event.setCancelled(true);
            player.sendMessage("§cYou cannot break blocks in this region!");
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        Location location = event.getBlock().getLocation();
        
        if (!plugin.getRegionManager().hasPermission(player, location, RegionFlags.Flag.BLOCK_PLACE)) {
            event.setCancelled(true);
            player.sendMessage("§cYou cannot place blocks in this region!");
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Location location = event.getClickedBlock() != null ? event.getClickedBlock().getLocation() : player.getLocation();
        
        if (!plugin.getRegionManager().hasPermission(player, location, RegionFlags.Flag.INTERACT)) {
            event.setCancelled(true);
            player.sendMessage("§cYou cannot interact with blocks in this region!");
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player player)) return;
        
        Location location = event.getEntity().getLocation();
        
        if (!plugin.getRegionManager().hasPermission(player, location, RegionFlags.Flag.ENTITY_DAMAGE)) {
            event.setCancelled(true);
            player.sendMessage("§cYou cannot damage entities in this region!");
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Location from = event.getFrom();
        Location to = event.getTo();
        
        if (to == null) return;
        
        Region fromRegion = plugin.getRegionManager().getRegionAt(from);
        Region toRegion = plugin.getRegionManager().getRegionAt(to);
        
        if (plugin.getConfig().getBoolean("features.show-entry-title", true)) {
            if (fromRegion == null && toRegion != null) {
                player.sendTitle("§a" + toRegion.getName(), "§7Protected Region", 10, 40, 10);
            } else if (fromRegion != null && toRegion == null) {
                player.sendTitle("§cLeft " + fromRegion.getName(), "§7No longer protected", 10, 40, 10);
            }
        }
    }
} 