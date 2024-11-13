package com.deadshotmdf.spigot_abstract_GUIS.General.Listeners;

import com.deadshotmdf.spigot_abstract_GUIS.General.GUI.GUI;
import com.deadshotmdf.spigot_abstract_GUIS.General.Managers.GuiManager;
import com.deadshotmdf.spigot_abstract_GUIS.General.Objects.TypeAction;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryCreativeEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;

public class GUIListener implements Listener {

    private final GuiManager guiManager;

    public GUIListener(GuiManager guiManager) {
        this.guiManager = guiManager;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onCreative(InventoryCreativeEvent ev){
        handle((Player) ev.getWhoClicked(), ev, TypeAction.NOT_SUPPORTED);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onDrag(InventoryDragEvent ev){
        handle((Player) ev.getWhoClicked(), ev, TypeAction.NOT_SUPPORTED);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onClick(InventoryClickEvent ev) {
        handle((Player) ev.getWhoClicked(), ev, TypeAction.CLICK);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onClose(InventoryCloseEvent ev) {
        handle((Player) ev.getPlayer(), ev, TypeAction.CLOSE);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onQuit(PlayerQuitEvent ev) {
        handle(ev.getPlayer(), ev, TypeAction.LEAVE);
    }

    private void handle(Player player, Event ev, TypeAction action){
        GUI gui = guiManager.getOpenGui(player.getUniqueId());

        if(gui == null)
            return;

        if(ev instanceof Cancellable cancellable)
            cancellable.setCancelled(true);

        switch(action){
            case CLICK:
                gui.handleClick((InventoryClickEvent) ev, new HashMap<>());
                break;
            case CLOSE:
                gui.handleClose((InventoryCloseEvent) ev);
                break;
            case LEAVE:
                guiManager.removeOpenGui(player);
                break;
        }
    }

}

