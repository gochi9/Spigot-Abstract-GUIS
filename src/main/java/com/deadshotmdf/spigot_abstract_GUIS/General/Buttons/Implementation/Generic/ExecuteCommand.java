package com.deadshotmdf.spigot_abstract_GUIS.General.Buttons.Implementation.Generic;

import com.deadshotmdf.spigot_abstract_GUIS.General.Buttons.AbstractButton;
import com.deadshotmdf.spigot_abstract_GUIS.General.Buttons.ButtonIdentifier;
import com.deadshotmdf.spigot_abstract_GUIS.General.GUI.GUI;
import com.deadshotmdf.spigot_abstract_GUIS.General.Managers.AbstractGUIManager;
import com.deadshotmdf.spigot_abstract_GUIS.General.Managers.GuiManager;
import org.bukkit.Bukkit;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.StringJoiner;

@ButtonIdentifier("EXECUTE_COMMAND")
public class ExecuteCommand extends AbstractButton {

    private final String command;

    public ExecuteCommand(@Nullable ItemStack item, AbstractGUIManager correspondentManager, GuiManager guiManager, String[] args, Map<String, Object> elementData) {
        super(item, correspondentManager, guiManager, args, elementData);

        StringJoiner joiner = new StringJoiner(" ");
        for (String arg : args) joiner.add(arg);

        this.command = joiner.toString();
    }

    @Override
    public void onClick(InventoryClickEvent ev, GUI gui, Map<String, Object> GUIArgs, Map<String, Object> extraArgs) {
        if(command == null || command.isBlank())
            return;

        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replace("{player}", ev.getWhoClicked().getName()));
    }

}
