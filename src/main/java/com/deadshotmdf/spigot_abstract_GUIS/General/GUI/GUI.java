package com.deadshotmdf.spigot_abstract_GUIS.General.GUI;

import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;

import java.util.Map;
import java.util.UUID;

public interface GUI {

    boolean isShared();
    void handleClick(InventoryClickEvent ev, Map<String, Object> args);
    void handleClose(InventoryCloseEvent ev);
    void refreshInventory();
    GUI createInstance(UUID player, GUI backGUI, Map<String, Object> args);
    void open(HumanEntity player, int page, boolean onOpen);
    int getPageCount();
    void deletePages();
    int getPageByInventory(Player player);
    int getPageByInventory(Inventory inventory);
    void setBackGUI(GUI backGUI);
    GUI getBackGUI();
    void forceClose();

}
