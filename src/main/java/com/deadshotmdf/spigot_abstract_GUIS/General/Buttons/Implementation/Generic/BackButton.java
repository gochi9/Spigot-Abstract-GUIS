package com.deadshotmdf.spigot_abstract_GUIS.General.Buttons.Implementation.Generic;

import com.deadshotmdf.spigot_abstract_GUIS.General.Buttons.AbstractButton;
import com.deadshotmdf.spigot_abstract_GUIS.General.Buttons.ButtonIdentifier;
import com.deadshotmdf.spigot_abstract_GUIS.General.GUI.GUI;
import com.deadshotmdf.spigot_abstract_GUIS.General.Managers.AbstractGUIManager;
import com.deadshotmdf.spigot_abstract_GUIS.General.Managers.GuiManager;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

@ButtonIdentifier("BACK")
public class BackButton extends AbstractButton {

    public BackButton(@Nullable ItemStack item, AbstractGUIManager correspondentManager, GuiManager guiManager, String[] args, Map<String, Object> elementData) {
        super(item, correspondentManager, guiManager, args, elementData);
    }

    @Override
    public void onClick(InventoryClickEvent ev, GUI gui, Map<String, Object> GUIArgs, Map<String, Object> extraArgs) {
        if(gui.getBackGUI() != null)
            guiManager.commenceOpen(ev.getWhoClicked(), gui.getBackGUI(), gui.getBackGUI().getBackGUI(), null);
        else
            guiManager.removeOpenGui(ev.getWhoClicked());
    }
}
