package com.deadshotmdf.spigot_abstract_GUIS.General.Buttons;

import com.deadshotmdf.spigot_abstract_GUIS.General.GUI.GUI;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.Map;

public interface GuiElement {

    ItemStack getItemStackClone();
    ItemStack getItemStackClone(String[] placeholder, String... replace);
    ItemStack getItemStackClone(ItemStack clone, String[] placeholder, String... replace);
    ItemStack getItemStackMarkedAndReplaced(NamespacedKey key, PersistentDataType type, Object value, String[] placeholders, String... replacements);
    boolean canClick(HumanEntity player);
    void onClick(InventoryClickEvent ev, GUI gui, Map<String, Object> GUIArgs, Map<String, Object> extraArgs);

}
