
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
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class HomeMenu implements Listener {
    public static final String TITLE = ChatColor.GREEN + "Mes Homes";
    private final SurvieHomePlugin plugin;
    private final HomeStorage storage;
    private final ChatInputs chatInputs;
    private HomeDetailMenu detailMenu;

    public HomeMenu(SurvieHomePlugin plugin, HomeStorage storage, ChatInputs chatInputs) {
        this.plugin = plugin;
        this.storage = storage;
        this.chatInputs = chatInputs;
    }

    public void setDetailMenu(HomeDetailMenu detailMenu) { this.detailMenu = detailMenu; }

    public void open(Player p) {
        Inventory inv = Bukkit.createInventory(p, 9, TITLE);
        Map<String, Location> homes = storage.load(p.getUniqueId());
        int allowed = storage.allowed(p);
        java.util.List<String> names = new java.util.ArrayList<>(homes.keySet());
        names.sort(String::compareToIgnoreCase);

        for (int i = 0; i < 8; i++) {
            ItemStack item;
            if (i < names.size()) {
                String name = names.get(i);
                item = homeItem(name);
            } else if (i < allowed) {
                item = unlockedSlotItem();
            } else {
                item = lockedSlotItem();
            }
            inv.setItem(i, item);
        }
        inv.setItem(8, infoItem(p, names.size(), allowed));
        p.openInventory(inv);
    }

    private ItemStack homeItem(String name) {
        ItemStack it = new ItemStack(Material.COMPASS);
        ItemMeta m = it.getItemMeta();
        m.setDisplayName(ChatColor.AQUA + "Home: " + ChatColor.WHITE + name);
        m.setLore(Arrays.asList(
                ChatColor.GRAY + "Clique gauche: " + ChatColor.WHITE + "Téléporter",
                ChatColor.GRAY + "Clique droit: " + ChatColor.WHITE + "Renommer/Supprimer"
        ));
        m.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        it.setItemMeta(m);
        return it;
    }

    private ItemStack unlockedSlotItem() {
        ItemStack it = new ItemStack(Material.LIME_STAINED_GLASS_PANE);
        ItemMeta m = it.getItemMeta();
        m.setDisplayName(ChatColor.GREEN + "Emplacement libre");
        m.setLore(Collections.singletonList(ChatColor.WHITE + "Clique pour créer un home"));
        it.setItemMeta(m);
        return it;
    }

    private ItemStack lockedSlotItem() {
        ItemStack it = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta m = it.getItemMeta();
        m.setDisplayName(ChatColor.DARK_GRAY + "Emplacement verrouillé");
        m.setLore(Arrays.asList(
                ChatColor.GRAY + "Débloque plus de homes :",
                ChatColor.DARK_AQUA + "- Pouvoir 3 homes : Survivants",
                ChatColor.DARK_AQUA + "- 5 homes : Maître & Suprême",
                ChatColor.DARK_AQUA + "- 8 homes : Suprême+"
        ));
        it.setItemMeta(m);
        return it;
    }

    private ItemStack infoItem(Player p, int count, int allowed) {
        ItemStack it = new ItemStack(Material.BOOK);
        ItemMeta m = it.getItemMeta();
        m.setDisplayName(ChatColor.GOLD + "Infos");
        m.setLore(Arrays.asList(
                ChatColor.WHITE + "Homes : " + count + "/" + allowed,
                ChatColor.GRAY + "Tu commences avec 1 home.",
                ChatColor.GRAY + "Commandes : /homes"
        ));
        it.setItemMeta(m);
        return it;
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (e.getView() == null) return;
        if (!TITLE.equals(e.getView().getTitle())) return;
        e.setCancelled(true);

        if (!(e.getWhoClicked() instanceof Player p)) return;
        int slot = e.getRawSlot();
        if (slot < 0 || slot > 8) return;

        Map<String, Location> homes = storage.load(p.getUniqueId());
        int allowed = storage.allowed(p);
        java.util.List<String> names = new java.util.ArrayList<>(homes.keySet());
        names.sort(String::compareToIgnoreCase);

        if (slot <= 7) {
            if (slot < names.size()) {
                String name = names.get(slot);
                if (e.isLeftClick()) {
                    Location l = homes.get(name);
                    if (l != null) {
                        p.teleport(l);
                        p.sendMessage("§aTéléportation au home §f" + name + "§a.");
                        p.closeInventory();
                    }
                } else if (e.isRightClick()) {
                    if (detailMenu != null) {
                        detailMenu.open(p, name);
                    }
                }
            } else if (slot < allowed) {
                p.closeInventory();
                chatInputs.askCreateName(p);
            } else {
                p.closeInventory();
                p.sendMessage("§cTu n'as pas débloqué cet emplacement.");
            }
        } else if (slot == 8) {
            p.closeInventory();
        }
    }

    public void reopen(Player p) { open(p); }
}
