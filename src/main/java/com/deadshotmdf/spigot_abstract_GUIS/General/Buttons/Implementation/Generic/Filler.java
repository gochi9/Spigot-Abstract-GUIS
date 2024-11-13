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

import java.util.Map;

//Items that have their tool tip hidden, meaning no information about the item is displayed.
//Meant to be used for background items, like BLACK_STAINED_GLASS_PANE
@ButtonIdentifier("FILLER")
public class Filler extends AbstractButton {

    public Filler(@Nullable ItemStack item, AbstractGUIManager correspondentManager, GuiManager guiManager, String[] args, Map<String, Object> elementData) {
        super(item, null, null, null, null);

        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setHideTooltip(true);
        item.setItemMeta(itemMeta);
    }

    //Filler items don't have any name or lore visible so there's no need to keep the parent implementation
    @Override
    public ItemStack getItemStackClone(String[] placeholder, String... replace){ return this.item.clone(); }

    @Override
    public ItemStack getItemStackClone(ItemStack clone, String[] placeholders, String... replacements){ return this.item.clone(); }

    @Override
    public void onClick(InventoryClickEvent event, GUI gui, Map<String, Object> GUIArgs, Map<String, Object> extraArgs) {
        // Do nothing, fillers are not clickable.
    }
}
