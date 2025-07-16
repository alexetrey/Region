package com.alexetrey.region.gui;

import com.alexetrey.region.RegionPlugin;
import com.alexetrey.region.data.Region;
import com.alexetrey.region.data.RegionFlags;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import fr.minuskube.inv.content.SlotPos;
import fr.minuskube.inv.ClickableItem;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.HashSet;
import java.util.stream.Collectors;

public class GUIManager {
    private final RegionPlugin plugin;

    public GUIManager(RegionPlugin plugin) {
        this.plugin = plugin;
    }

    public void openRegionsMenu(Player player) {
        try {
            Set<Region> allRegions = new HashSet<>(plugin.getRegionManager().getAllRegions());
            Set<Region> playerRegions = allRegions.stream().filter(r -> r.isWhitelisted(player.getUniqueId())).collect(Collectors.toSet());
            plugin.getLogger().info("Opening regions menu for player " + player.getName() + " with " + playerRegions.size() + " regions");
            
            fr.minuskube.inv.SmartInventory.builder()
                .manager(plugin.getInvManager())
                .id("regions_menu")
                .provider(new InventoryProvider() {
                    @Override
                    public void init(Player player, InventoryContents contents) {
                        plugin.getLogger().info("Initializing regions menu for player " + player.getName());
                        int row = 0;
                        int col = 0;
                        for (Region region : playerRegions) {
                            ItemStack item = createRegionItem(region);
                            contents.set(SlotPos.of(row, col), ClickableItem.of(item, e -> {
                                openRegionMenu(player, region);
                            }));
                            if (++col >= 9) {
                                col = 0;
                                row++;
                            }
                        }
                        if (playerRegions.isEmpty()) {
                            ItemStack emptyItem = new ItemStack(Material.BARRIER);
                            ItemMeta meta = emptyItem.getItemMeta();
                            if (meta != null) {
                                meta.setDisplayName("§cNo Regions");
                                meta.setLore(List.of("§7You are not whitelisted in any regions yet.", "§7Use /region wand to get started!"));
                                emptyItem.setItemMeta(meta);
                            }
                            contents.set(SlotPos.of(0, 4), ClickableItem.empty(emptyItem));
                        }
                    }
                    @Override
                    public void update(Player player, InventoryContents contents) {}
                })
                .size(6, 9)
                .title("§8Regions Menu")
                .build()
                .open(player);
        } catch (Exception e) {
            plugin.getLogger().severe("Error opening regions menu for player " + player.getName() + ": " + e.getMessage());
            e.printStackTrace();
            player.sendMessage("§cAn error occurred while opening the GUI. Please try again.");
        }
    }

    public void openRegionMenu(Player player, Region region) {
        SmartInventory.builder()
            .manager(plugin.getInvManager())
            .id("region_menu_" + region.getName())
            .provider(new InventoryProvider() {
                @Override
                public void init(Player player, InventoryContents contents) {
                    ItemStack renameItem = new ItemStack(Material.NAME_TAG);
                    ItemMeta renameMeta = renameItem.getItemMeta();
                    if (renameMeta != null) {
                        renameMeta.setDisplayName("§eRename Region");
                        renameMeta.setLore(List.of("§7Click to rename this region"));
                        renameItem.setItemMeta(renameMeta);
                    }
                    contents.set(SlotPos.of(1, 1), ClickableItem.of(renameItem, e -> {
                        player.closeInventory();
                        player.sendMessage("§eTo rename the region, use: §f/region rename " + region.getName() + " <new_name>");
                    }));

                    ItemStack whitelistItem = new ItemStack(Material.PLAYER_HEAD);
                    ItemMeta whitelistMeta = whitelistItem.getItemMeta();
                    if (whitelistMeta != null) {
                        whitelistMeta.setDisplayName("§aManage Whitelist");
                        whitelistMeta.setLore(List.of("§7Click to manage whitelist", "§7Current members: §f" + region.getWhitelist().size()));
                        whitelistItem.setItemMeta(whitelistMeta);
                    }
                    contents.set(SlotPos.of(1, 3), ClickableItem.of(whitelistItem, e -> {
                        openWhitelistMenu(player, region);
                    }));

                    ItemStack redefineItem = new ItemStack(Material.COMPASS);
                    ItemMeta redefineMeta = redefineItem.getItemMeta();
                    if (redefineMeta != null) {
                        redefineMeta.setDisplayName("§6Redefine Area");
                        redefineMeta.setLore(List.of("§7Click to redefine the region area"));
                        redefineItem.setItemMeta(redefineMeta);
                    }
                    contents.set(SlotPos.of(1, 5), ClickableItem.of(redefineItem, e -> {
                        player.closeInventory();
                        player.sendMessage("§eUse /region wand to select new corners, then use: §f/region redefine " + region.getName());
                    }));

                    ItemStack flagsItem = new ItemStack(Material.REDSTONE);
                    ItemMeta flagsMeta = flagsItem.getItemMeta();
                    if (flagsMeta != null) {
                        flagsMeta.setDisplayName("§cManage Flags");
                        flagsMeta.setLore(List.of("§7Click to manage region flags"));
                        flagsItem.setItemMeta(flagsMeta);
                    }
                    contents.set(SlotPos.of(1, 7), ClickableItem.of(flagsItem, e -> {
                        openFlagsMenu(player, region);
                    }));

                    ItemStack deleteItem = new ItemStack(Material.BARRIER);
                    ItemMeta deleteMeta = deleteItem.getItemMeta();
                    if (deleteMeta != null) {
                        deleteMeta.setDisplayName("§4Delete Region");
                        deleteMeta.setLore(List.of("§7Click to delete this region", "§cThis action cannot be undone!"));
                        deleteItem.setItemMeta(deleteMeta);
                    }
                    contents.set(SlotPos.of(2, 4), ClickableItem.of(deleteItem, e -> {
                        player.closeInventory();
                        player.sendMessage("§cTo delete the region, use: §f/region delete " + region.getName());
                    }));
                }
                @Override
                public void update(Player player, InventoryContents contents) {}
            })
            .size(3, 9)
            .title("§8Region: §f" + region.getName())
            .build()
            .open(player);
    }

    public void openWhitelistMenu(Player player, Region region) {
        Set<UUID> whitelist = region.getWhitelist();
        SmartInventory.builder()
            .manager(plugin.getInvManager())
            .id("whitelist_menu_" + region.getName())
            .provider(new InventoryProvider() {
                @Override
                public void init(Player player, InventoryContents contents) {
                    int row = 0;
                    int col = 0;
                    for (UUID uuid : whitelist) {
                        String name = Bukkit.getOfflinePlayer(uuid).getName();
                        if (name != null) {
                            ItemStack item = new ItemStack(Material.PLAYER_HEAD);
                            ItemMeta meta = item.getItemMeta();
                            if (meta != null) {
                                meta.setDisplayName("§f" + name);
                                meta.setLore(List.of("§7Click to remove from whitelist"));
                                item.setItemMeta(meta);
                            }
                            final String playerName = name;
                            contents.set(SlotPos.of(row, col), ClickableItem.of(item, e -> {
                                region.removeFromWhitelist(uuid);
                                player.sendMessage("§aRemoved §f" + playerName + " §afrom whitelist");
                                openWhitelistMenu(player, region);
                            }));
                            if (++col >= 9) {
                                col = 0;
                                row++;
                            }
                        }
                    }
                    if (whitelist.isEmpty()) {
                        ItemStack emptyItem = new ItemStack(Material.BARRIER);
                        ItemMeta meta = emptyItem.getItemMeta();
                        if (meta != null) {
                            meta.setDisplayName("§cNo Whitelisted Players");
                            meta.setLore(List.of("§7No players are whitelisted in this region."));
                            emptyItem.setItemMeta(meta);
                        }
                        contents.set(SlotPos.of(0, 4), ClickableItem.empty(emptyItem));
                    }
                }
                @Override
                public void update(Player player, InventoryContents contents) {}
            })
            .size(6, 9)
            .title("§8Whitelist: §f" + region.getName())
            .build()
            .open(player);
    }

    public void openFlagsMenu(Player player, Region region) {
        RegionFlags flags = region.getFlags();
        SmartInventory.builder()
            .manager(plugin.getInvManager())
            .id("flags_menu_" + region.getName())
            .provider(new InventoryProvider() {
                @Override
                public void init(Player player, InventoryContents contents) {
                    int slot = 0;
                    for (RegionFlags.IFlag flag : RegionFlags.getAllAvailableFlags()) {
                        RegionFlags.FlagState state = flags.getFlag(flag);
                        Material material = getFlagMaterial(flag);
                        ItemStack item = new ItemStack(material);
                        ItemMeta meta = item.getItemMeta();
                        if (meta != null) {
                            meta.setDisplayName("§f" + flag.getName().replace("-", " ").replace("_", " "));
                            meta.setLore(List.of(
                                "§7Current: " + getStateColor(state) + state.name(),
                                "§7Click to cycle through states"
                            ));
                            item.setItemMeta(meta);
                        }
                        final RegionFlags.IFlag currentFlag = flag;
                        contents.set(SlotPos.of(slot / 9, slot % 9), ClickableItem.of(item, e -> {
                            RegionFlags.FlagState newState = cycleFlagState(state);
                            flags.setFlag(currentFlag, newState);
                            player.sendMessage("§aSet §f" + currentFlag.getName().replace("-", " ").replace("_", " ") + " §ato §f" + newState.name());
                            openFlagsMenu(player, region);
                        }));
                        slot++;
                    }
                }
                @Override
                public void update(Player player, InventoryContents contents) {}
            })
            .size(6, 9)
            .title("§8Flags: §f" + region.getName())
            .build()
            .open(player);
    }

    private ItemStack createRegionItem(Region region) {
        ItemStack item = new ItemStack(Material.GOLD_BLOCK);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§6" + region.getName());
            meta.setLore(List.of(
                "§7Members: §f" + region.getWhitelist().size(),
                "§7Click to manage"
            ));
            item.setItemMeta(meta);
        }
        return item;
    }

    private Material getFlagMaterial(RegionFlags.IFlag flag) {
        String flagName = flag.getName().toLowerCase();
        switch (flagName) {
            case "block-break": return Material.DIAMOND_PICKAXE;
            case "block-place": return Material.BRICKS;
            case "interact": return Material.LEVER;
            case "entity-damage": return Material.DIAMOND_SWORD;
            default: return Material.BARRIER;
        }
    }

    private RegionFlags.FlagState cycleFlagState(RegionFlags.FlagState current) {
        switch (current) {
            case EVERYONE: return RegionFlags.FlagState.WHITELIST;
            case WHITELIST: return RegionFlags.FlagState.NONE;
            case NONE: return RegionFlags.FlagState.EVERYONE;
            default: return RegionFlags.FlagState.WHITELIST;
        }
    }

    private String getStateColor(RegionFlags.FlagState state) {
        switch (state) {
            case EVERYONE: return "§a";
            case WHITELIST: return "§e";
            case NONE: return "§c";
            default: return "§f";
        }
    }
} 