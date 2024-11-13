package com.deadshotmdf.spigot_abstract_GUIS.General.Buttons.Implementation.Generic;

import com.deadshotmdf.spigot_abstract_GUIS.General.Buttons.AbstractButton;
import com.deadshotmdf.spigot_abstract_GUIS.General.Buttons.ButtonIdentifier;
import com.deadshotmdf.spigot_abstract_GUIS.General.GUI.GUI;
import com.deadshotmdf.spigot_abstract_GUIS.General.Managers.AbstractGUIManager;
import com.deadshotmdf.spigot_abstract_GUIS.General.Managers.GuiManager;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

//Buttons that are used to display information and is not clickable
@ButtonIdentifier("REPLACEABLE")
public class ReplaceableButton extends AbstractButton {

    public ReplaceableButton(@Nullable ItemStack item, AbstractGUIManager correspondentManager, GuiManager guiManager, String[] args, Map<String, Object> elementData) {
        super(item, correspondentManager, guiManager, args, elementData);
    }

    public static ReplaceableButton createSimpleReplaceableButton(@Nullable ItemStack item, String name, List<String> lore) {
        if(item == null)
            return new ReplaceableButton(null, null, null, null, null);

        ItemStack i = item.clone();
        ItemMeta meta = i.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(lore);
        i.setItemMeta(meta);
        return new ReplaceableButton(i, null, null, null, null);
    }

    public static ReplaceableButton createReplaceableButton(@Nullable ItemStack item, AbstractGUIManager correspondentManager, GuiManager guiManager, String[] args, Map<String, Object> elementData) {
        return new ReplaceableButton(item, correspondentManager, guiManager, args, elementData);
    }

    @Override
    public void onClick(InventoryClickEvent event, GUI gui, Map<String, Object> GUIArgs, Map<String, Object> extraArgs) {
    }
}
