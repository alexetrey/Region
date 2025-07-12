package com.alexetrey.region.listeners;

import com.alexetrey.region.RegionPlugin;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class WandListener implements Listener {
    private final RegionPlugin plugin;

    public WandListener(RegionPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        
        if (item == null || !plugin.getWandManager().isWand(item)) {
            return;
        }
        
        event.setCancelled(true);
        
        if (event.getAction() == Action.LEFT_CLICK_BLOCK && event.getClickedBlock() != null) {
            plugin.getWandManager().setCorner1(player, event.getClickedBlock().getLocation());
        } else if (event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getClickedBlock() != null) {
            plugin.getWandManager().setCorner2(player, event.getClickedBlock().getLocation());
        }
    }
} 