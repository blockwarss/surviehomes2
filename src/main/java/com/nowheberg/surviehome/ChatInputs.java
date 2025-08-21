
package com.nowheberg.surviehome;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ChatInputs implements Listener {
    private enum Mode { CREATE, RENAME }

    private static class Pending {
        Mode mode; String refName; long expiresAt;
        Pending(Mode m, String refName, long expiresAt) { this.mode = m; this.refName = refName; this.expiresAt = expiresAt; }
    }

    private final SurvieHomePlugin plugin;
    private final HomeStorage storage;
    private final Map<UUID, Pending> wait = new ConcurrentHashMap<>();

    public ChatInputs(SurvieHomePlugin plugin, HomeStorage storage) {
        this.plugin = plugin;
        this.storage = storage;
    }

    public void askCreateName(Player p) {
        wait.put(p.getUniqueId(), new Pending(Mode.CREATE, null, System.currentTimeMillis() + 30000));
        p.sendMessage("§eEntre le nom du nouveau home dans le chat (30s). §7Écris §ccancel §7pour annuler.");
    }

    public void askRename(Player p, String oldName) {
        wait.put(p.getUniqueId(), new Pending(Mode.RENAME, oldName, System.currentTimeMillis() + 30000));
        p.sendMessage("§eEntre le nouveau nom pour §f" + oldName + " §e(30s). §7Écris §ccancel §7pour annuler.");
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent e) {
        Player p = e.getPlayer();
        Pending pending = wait.get(p.getUniqueId());
        if (pending == null) return;
        e.setCancelled(true);

        if (System.currentTimeMillis() > pending.expiresAt) {
            wait.remove(p.getUniqueId());
            p.sendMessage("§cTemps écoulé.");
            return;
        }
        String msg = e.getMessage().trim();
        if (msg.equalsIgnoreCase("cancel")) {
            wait.remove(p.getUniqueId());
            p.sendMessage("§7Annulé.");
            return;
        }

        if (pending.mode == Mode.CREATE) {
            if (storage.addHome(p, msg, p.getLocation().clone())) {
                wait.remove(p.getUniqueId());
                p.sendMessage("§aHome créé : §f" + msg);
            } else {
                p.sendMessage("§cImpossible de créer ce home. Nom pris ou limite atteinte.");
            }
        } else if (pending.mode == Mode.RENAME) {
            if (storage.renameHome(p, pending.refName, msg)) {
                wait.remove(p.getUniqueId());
                p.sendMessage("§aHome renommé en §f" + msg);
            } else {
                p.sendMessage("§cImpossible de renommer. Nom pris ou introuvable.");
            }
        }
    }
}
