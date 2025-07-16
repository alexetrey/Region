package com.alexetrey.region.commands;

import com.alexetrey.region.RegionPlugin;
import com.alexetrey.region.data.Region;
import com.alexetrey.region.data.RegionFlags;
import com.alexetrey.region.gui.GUIManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class RegionCommand implements CommandExecutor, TabCompleter {
    private final RegionPlugin plugin;

    public RegionCommand(RegionPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cThis command can only be used by players!");
            return true;
        }

        if (args.length == 0) {
            if (!player.hasPermission("region.menu")) {
                player.sendMessage("§cYou don't have permission to use this command!");
                return true;
            }
            try {
                plugin.getGuiManager().openRegionsMenu(player);
            } catch (Exception e) {
                player.sendMessage("§cAn error occurred while opening the GUI. Please try again.");
                e.printStackTrace();
            }
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "create" -> handleCreate(player, args);
            case "wand" -> handleWand(player);
            case "add" -> handleAdd(player, args);
            case "remove" -> handleRemove(player, args);
            case "flag" -> handleFlag(player, args);
            case "list" -> handleList(player);

            case "rename" -> handleRename(player, args);
            case "redefine" -> handleRedefine(player, args);
            case "delete" -> handleDelete(player, args);
            default -> handleRegionMenu(player, args[0]);
        }

        return true;
    }

    private void handleCreate(Player player, String[] args) {
        if (!player.hasPermission("region.create")) {
            player.sendMessage("§cYou don't have permission to create regions!");
            return;
        }

        if (args.length < 2) {
            player.sendMessage("§cUsage: /region create <name>");
            return;
        }

        String name = args[1];
        if (plugin.getRegionManager().getRegion(name) != null) {
            player.sendMessage("§cA region with that name already exists!");
            return;
        }

        if (!plugin.getWandManager().validateSelection(player)) {
            return;
        }

        if (plugin.getRegionManager().createRegion(name, 
                plugin.getWandManager().getCorner1(player), 
                plugin.getWandManager().getCorner2(player))) {
            player.sendMessage("§aRegion '" + name + "' created successfully!");
            plugin.getWandManager().clearSelection(player);
        } else {
            player.sendMessage("§cFailed to create region!");
        }
    }

    private void handleWand(Player player) {
        if (!player.hasPermission("region.create")) {
            player.sendMessage("§cYou don't have permission to use the wand!");
            return;
        }

        plugin.getWandManager().giveWand(player);
    }

    private void handleAdd(Player player, String[] args) {
        if (!player.hasPermission("region.add")) {
            player.sendMessage("§cYou don't have permission to add players to whitelist!");
            return;
        }

        if (args.length < 3) {
            player.sendMessage("§cUsage: /region add <name> <username>");
            return;
        }

        String regionName = args[1];
        String username = args[2];

        Region region = plugin.getRegionManager().getRegion(regionName);
        if (region == null) {
            player.sendMessage("§cRegion '" + regionName + "' not found!");
            return;
        }

        Player target = Bukkit.getPlayer(username);
        if (target == null) {
            player.sendMessage("§cPlayer '" + username + "' not found!");
            return;
        }

        if (plugin.getRegionManager().addToWhitelist(regionName, target.getUniqueId())) {
            player.sendMessage("§aAdded " + username + " to the whitelist of region '" + regionName + "'!");
            target.sendMessage("§aYou have been added to the whitelist of region '" + regionName + "'!");
        } else {
            player.sendMessage("§cFailed to add player to whitelist!");
        }
    }

    private void handleRemove(Player player, String[] args) {
        if (!player.hasPermission("region.remove")) {
            player.sendMessage("§cYou don't have permission to remove players from whitelist!");
            return;
        }

        if (args.length < 3) {
            player.sendMessage("§cUsage: /region remove <name> <username>");
            return;
        }

        String regionName = args[1];
        String username = args[2];

        Region region = plugin.getRegionManager().getRegion(regionName);
        if (region == null) {
            player.sendMessage("§cRegion '" + regionName + "' not found!");
            return;
        }

        Player target = Bukkit.getPlayer(username);
        if (target == null) {
            player.sendMessage("§cPlayer '" + username + "' not found!");
            return;
        }

        if (plugin.getRegionManager().removeFromWhitelist(regionName, target.getUniqueId())) {
            player.sendMessage("§aRemoved " + username + " from the whitelist of region '" + regionName + "'!");
            target.sendMessage("§cYou have been removed from the whitelist of region '" + regionName + "'!");
        } else {
            player.sendMessage("§cFailed to remove player from whitelist!");
        }
    }

    private void handleFlag(Player player, String[] args) {
        if (!player.hasPermission("region.flag")) {
            player.sendMessage("§cYou don't have permission to edit region flags!");
            return;
        }

        if (args.length < 4) {
            player.sendMessage("§cUsage: /region flag <name> <flag> <state>");
            return;
        }

        String regionName = args[1];
        String flagName = args[2];
        String stateName = args[3];

        Region region = plugin.getRegionManager().getRegion(regionName);
        if (region == null) {
            player.sendMessage("§cRegion '" + regionName + "' not found!");
            return;
        }

        RegionFlags.IFlag flag = RegionFlags.Flag.fromName(flagName);
        if (flag == null) {
            player.sendMessage("§cInvalid flag! Available flags: block-break, block-place, interact, entity-damage");
            return;
        }

        RegionFlags.FlagState state;
        try {
            state = RegionFlags.FlagState.valueOf(stateName.toUpperCase());
        } catch (IllegalArgumentException e) {
            player.sendMessage("§cInvalid state! Available states: everyone, whitelist, none");
            return;
        }

        if (plugin.getRegionManager().setFlag(regionName, flag, state)) {
            player.sendMessage("§aFlag '" + flagName + "' set to '" + stateName + "' for region '" + regionName + "'!");
        } else {
            player.sendMessage("§cFailed to set flag!");
        }
    }

    private void handleList(Player player) {
        if (!player.hasPermission("region.menu")) {
            player.sendMessage("§cYou don't have permission to list regions!");
            return;
        }

        Set<Region> playerRegions = plugin.getRegionManager().getAllRegions().stream()
            .filter(r -> r.isWhitelisted(player.getUniqueId()))
            .collect(Collectors.toSet());
        if (playerRegions.isEmpty()) {
            player.sendMessage("§eYou are not whitelisted in any regions yet.");
            return;
        }

        player.sendMessage("§aYour regions:");
        for (Region region : playerRegions) {
            player.sendMessage("§7- §6" + region.getName() + " §7(Members: §f" + region.getWhitelist().size() + "§7)");
        }
    }

    private void handleRegionMenu(Player player, String regionName) {
        if (!player.hasPermission("region.menu")) {
            player.sendMessage("§cYou don't have permission to use this command!");
            return;
        }

        Region region = plugin.getRegionManager().getRegion(regionName);
        if (region == null) {
            player.sendMessage("§cRegion '" + regionName + "' not found!");
            return;
        }

        plugin.getGuiManager().openRegionMenu(player, region);
    }



    private void handleRename(Player player, String[] args) {
        if (!player.hasPermission("region.rename")) {
            player.sendMessage("§cYou don't have permission to rename regions!");
            return;
        }

        if (args.length < 3) {
            player.sendMessage("§cUsage: /region rename <old_name> <new_name>");
            return;
        }

        String oldName = args[1];
        String newName = args[2];

        Region region = plugin.getRegionManager().getRegion(oldName);
        if (region == null) {
            player.sendMessage("§cRegion '" + oldName + "' not found!");
            return;
        }

        if (plugin.getRegionManager().getRegion(newName) != null) {
            player.sendMessage("§cA region with the name '" + newName + "' already exists!");
            return;
        }

        if (plugin.getRegionManager().renameRegion(oldName, newName)) {
            player.sendMessage("§aRegion renamed from '" + oldName + "' to '" + newName + "'!");
        } else {
            player.sendMessage("§cFailed to rename region!");
        }
    }

    private void handleRedefine(Player player, String[] args) {
        if (!player.hasPermission("region.redefine")) {
            player.sendMessage("§cYou don't have permission to redefine regions!");
            return;
        }

        if (args.length < 2) {
            player.sendMessage("§cUsage: /region redefine <name>");
            return;
        }

        String regionName = args[1];
        Region region = plugin.getRegionManager().getRegion(regionName);
        if (region == null) {
            player.sendMessage("§cRegion '" + regionName + "' not found!");
            return;
        }

        if (!plugin.getWandManager().validateSelection(player)) {
            return;
        }

        if (plugin.getRegionManager().redefineRegion(regionName, 
                plugin.getWandManager().getCorner1(player), 
                plugin.getWandManager().getCorner2(player))) {
            player.sendMessage("§aRegion '" + regionName + "' redefined successfully!");
            plugin.getWandManager().clearSelection(player);
        } else {
            player.sendMessage("§cFailed to redefine region!");
        }
    }

    private void handleDelete(Player player, String[] args) {
        if (!player.hasPermission("region.delete")) {
            player.sendMessage("§cYou don't have permission to delete regions!");
            return;
        }

        if (args.length < 2) {
            player.sendMessage("§cUsage: /region delete <name>");
            return;
        }

        String regionName = args[1];
        Region region = plugin.getRegionManager().getRegion(regionName);
        if (region == null) {
            player.sendMessage("§cRegion '" + regionName + "' not found!");
            return;
        }

        if (plugin.getRegionManager().deleteRegion(regionName)) {
            player.sendMessage("§aRegion '" + regionName + "' deleted successfully!");
        } else {
            player.sendMessage("§cFailed to delete region!");
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            completions.addAll(List.of("create", "wand", "add", "remove", "flag", "list", "rename", "redefine", "delete"));
        } else if (args.length == 2) {
            String subCommand = args[0].toLowerCase();
            switch (subCommand) {
                case "add", "remove", "flag", "rename", "redefine", "delete" -> {
                    if (sender instanceof Player player) {
                        Set<Region> playerRegions = plugin.getRegionManager().getAllRegions().stream()
                            .filter(r -> r.isWhitelisted(player.getUniqueId()))
                            .collect(Collectors.toSet());
                        for (Region region : playerRegions) {
                            completions.add(region.getName());
                        }
                    }
                }
            }
        } else if (args.length == 3) {
            String subCommand = args[0].toLowerCase();
            if ("flag".equals(subCommand)) {
                completions.addAll(List.of("block-break", "block-place", "interact", "entity-damage"));
            } else if ("add".equals(subCommand) || "remove".equals(subCommand)) {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    completions.add(player.getName());
                }
            }
        } else if (args.length == 4) {
            String subCommand = args[0].toLowerCase();
            if ("flag".equals(subCommand)) {
                completions.addAll(List.of("everyone", "whitelist", "none"));
            }
        }

        return completions.stream()
                .filter(s -> s.toLowerCase().startsWith(args[args.length - 1].toLowerCase()))
                .toList();
    }
} 