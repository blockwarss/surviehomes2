
package com.nowheberg.surviehome;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class HomeStorage {
    private final SurvieHomePlugin plugin;
    private final File homesFolder;

    public HomeStorage(SurvieHomePlugin plugin) {
        this.plugin = plugin;
        this.homesFolder = new File(plugin.getDataFolder(), "homes");
        if (!homesFolder.exists()) homesFolder.mkdirs();
    }

    public int allowed(Player p) {
        if (p.hasPermission("surviehome.8")) return 8;
        if (p.hasPermission("surviehome.5")) return 5;
        if (p.hasPermission("surviehome.1")) return 1;
        return 1; // défaut
    }

    public Map<String, Location> load(UUID uuid) {
        File f = file(uuid);
        FileConfiguration c = YamlConfiguration.loadConfiguration(f);
        Map<String, Location> map = new LinkedHashMap<>();
        if (c.isConfigurationSection("homes")) {
            for (String key : c.getConfigurationSection("homes").getKeys(false)) {
                String path = "homes." + key + ".";
                String worldName = c.getString(path + "world");
                World w = Bukkit.getWorld(worldName);
                if (w == null) continue;
                double x = c.getDouble(path + "x");
                double y = c.getDouble(path + "y");
                double z = c.getDouble(path + "z");
                float yaw = (float) c.getDouble(path + "yaw");
                float pitch = (float) c.getDouble(path + "pitch");
                map.put(key, new Location(w, x, y, z, yaw, pitch));
            }
        }
        return map;
    }

    public void save(UUID uuid, Map<String, Location> homes) {
        File f = file(uuid);
        FileConfiguration c = new YamlConfiguration();
        for (Map.Entry<String, Location> e : homes.entrySet()) {
            String path = "homes." + e.getKey() + ".";
            Location l = e.getValue();
            c.set(path + "world", l.getWorld().getName());
            c.set(path + "x", l.getX());
            c.set(path + "y", l.getY());
            c.set(path + "z", l.getZ());
            c.set(path + "yaw", l.getYaw());
            c.set(path + "pitch", l.getPitch());
        }
        try {
            c.save(f);
        } catch (IOException ex) {
            plugin.getLogger().warning("Impossible d'enregistrer les homes: " + ex.getMessage());
        }
    }

    public boolean addHome(Player p, String name, Location loc) {
        name = sanitize(name);
        Map<String, Location> homes = load(p.getUniqueId());
        if (homes.containsKey(name)) return false;
        if (homes.size() >= allowed(p)) return false;
        homes.put(name, loc.clone());
        save(p.getUniqueId(), homes);
        return true;
    }

    public boolean removeHome(Player p, String name) {
        Map<String, Location> homes = load(p.getUniqueId());
        if (homes.remove(name) != null) {
            save(p.getUniqueId(), homes);
            return true;
        }
        return false;
    }

    public boolean renameHome(Player p, String oldName, String newName) {
        newName = sanitize(newName);
        if (newName.isBlank()) return false;
        Map<String, Location> homes = load(p.getUniqueId());
        if (!homes.containsKey(oldName) || homes.containsKey(newName)) return false;
        Location l = homes.remove(oldName);
        homes.put(newName, l);
        save(p.getUniqueId(), homes);
        return true;
    }

    private File file(UUID uuid) { return new File(homesFolder, uuid + ".yml"); }

    private String sanitize(String s) {
        s = s.trim();
        if (s.length() > 16) s = s.substring(0, 16);
        return s.replaceAll("[^A-Za-z0-9_\-]", "_");
    }
}
