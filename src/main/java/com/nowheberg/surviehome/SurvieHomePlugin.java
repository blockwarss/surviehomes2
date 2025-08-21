
package com.nowheberg.surviehome;

import org.bukkit.plugin.java.JavaPlugin;

public class SurvieHomePlugin extends JavaPlugin {
    private static SurvieHomePlugin instance;
    private HomeStorage storage;
    private ChatInputs chatInputs;
    private HomeMenu homeMenu;
    private HomeDetailMenu detailMenu;

    @Override
    public void onEnable() {
        instance = this;

        storage = new HomeStorage(this);
        chatInputs = new ChatInputs(this, storage);
        homeMenu = new HomeMenu(this, storage, chatInputs);
        detailMenu = new HomeDetailMenu(this, storage, chatInputs, homeMenu);
        homeMenu.setDetailMenu(detailMenu);

        if (getCommand("homes") != null) {
            getCommand("homes").setExecutor(new HomesCommand(homeMenu));
        }

        getServer().getPluginManager().registerEvents(homeMenu, this);
        getServer().getPluginManager().registerEvents(detailMenu, this);
        getServer().getPluginManager().registerEvents(chatInputs, this);
        getLogger().info("SurvieHome activé.");
    }

    @Override
    public void onDisable() {
        getLogger().info("SurvieHome désactivé.");
    }

    public static SurvieHomePlugin get() { return instance; }
}
