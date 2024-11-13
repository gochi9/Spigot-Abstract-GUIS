package com.deadshotmdf.spigot_abstract_GUIS;

import com.deadshotmdf.spigot_abstract_GUIS.ExampleShop.Commands.OpenShopCommand;
import com.deadshotmdf.spigot_abstract_GUIS.ExampleShop.Commands.SellAllCommand;
import com.deadshotmdf.spigot_abstract_GUIS.ExampleShop.Managers.ShopManager;
import com.deadshotmdf.spigot_abstract_GUIS.General.Commands.ReloadCommand;
import com.deadshotmdf.spigot_abstract_GUIS.General.Listeners.GUIListener;
import com.deadshotmdf.spigot_abstract_GUIS.General.Managers.GuiManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class S_A_GUIS extends JavaPlugin {

    private GuiManager guiManager;
    private ShopManager shopManager;

    @Override
    public void onEnable() {
        GUIUtils.createButtons();

        this.guiManager = new GuiManager();
        this.shopManager = new ShopManager(guiManager, this);

        this.guiManager.reloadConfig();

        Bukkit.getPluginManager().registerEvents(new GUIListener(guiManager), this);

        this.getCommand("abstractguis").setExecutor(new ReloadCommand(guiManager));

        this.getCommand("shop").setExecutor(new OpenShopCommand(shopManager));
        this.getCommand("sellall").setExecutor(new SellAllCommand(shopManager));
    }

    @Override
    public void onDisable() {
        guiManager.saveAll();
    }
}
