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

@ButtonIdentifier("OPEN_GUI")
public class OpenGUIButton extends AbstractButton {

    private final String guiName;
    private final String backGUIName;

    public OpenGUIButton(@Nullable ItemStack item, AbstractGUIManager correspondentManager, GuiManager guiManager, String[] args, Map<String, Object> elementData) {
        super(item, correspondentManager, guiManager, args, elementData);
        this.guiName = args.length > 0 ? args[0] : "NULL";
        this.backGUIName = args.length > 1 ? args[1] : null;
    }

    @Override
    public void onClick(InventoryClickEvent event, GUI gui, Map<String, Object> GUIArgs, Map<String, Object> extraArgs) {
        guiManager.openGui(event.getWhoClicked(), guiName, backGUIName != null ? guiManager.getGuiTemplate(backGUIName) : gui, null);
    }

}
