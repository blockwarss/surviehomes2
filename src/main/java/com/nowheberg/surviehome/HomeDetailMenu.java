
package com.nowheberg.surviehome;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.Map;

public class HomeDetailMenu implements Listener {
    private static final String TITLE_PREFIX = ChatColor.AQUA + "Home ";
    private final SurvieHomePlugin plugin;
    private final HomeStorage storage;
    private final ChatInputs chatInputs;
    private final HomeMenu homeMenu;

    public HomeDetailMenu(SurvieHomePlugin plugin, HomeStorage storage, ChatInputs chatInputs, HomeMenu homeMenu) {
        this.plugin = plugin;
        this.storage = storage;
        this.chatInputs = chatInputs;
        this.homeMenu = homeMenu;
    }

    public void open(Player p, String name) {
        Inventory inv = Bukkit.createInventory(p, 9, TITLE_PREFIX + name);
        inv.setItem(2, button(Material.ENDER_PEARL, ChatColor.GREEN + "Téléporter"));
        inv.setItem(4, button(Material.NAME_TAG, ChatColor.YELLOW + "Renommer"));
        inv.setItem(6, button(Material.LAVA_BUCKET, ChatColor.RED + "Supprimer"));
        inv.setItem(8, button(Material.BARRIER, ChatColor.GRAY + "Retour"));
        p.openInventory(inv);
    }

    private ItemStack button(Material mat, String name) {
        ItemStack it = new ItemStack(mat);
        ItemMeta m = it.getItemMeta();
        m.setDisplayName(name);
        it.setItemMeta(m);
        return it;
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (e.getView() == null) return;
        String title = e.getView().getTitle();
        if (!title.startsWith(TITLE_PREFIX)) return;
        e.setCancelled(true);
        if (!(e.getWhoClicked() instanceof Player p)) return;
        String name = title.substring(TITLE_PREFIX.length());
        int slot = e.getRawSlot();

        Map<String, Location> homes = storage.load(p.getUniqueId());
        if (!homes.containsKey(name)) {
            p.closeInventory();
            p.sendMessage("§cCe home n'existe plus.");
            return;
        }

        if (slot == 2) {
            p.teleport(homes.get(name));
            p.closeInventory();
            p.sendMessage("§aTéléportation au home §f" + name + "§a.");
        } else if (slot == 4) {
            p.closeInventory();
            chatInputs.askRename(p, name);
        } else if (slot == 6) {
            storage.removeHome(p, name);
            p.sendMessage("§cHome supprimé : §f" + name);
            homeMenu.open(p);
        } else if (slot == 8) {
            homeMenu.open(p);
        }
    }
}
