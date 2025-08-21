
package com.nowheberg.surviehome;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class HomesCommand implements CommandExecutor {
    private final HomeMenu menu;

    public HomesCommand(HomeMenu menu) { this.menu = menu; }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player p)) {
            sender.sendMessage("§cCommande joueur uniquement.");
            return true;
        }
        menu.open(p);
        return true;
    }
}
