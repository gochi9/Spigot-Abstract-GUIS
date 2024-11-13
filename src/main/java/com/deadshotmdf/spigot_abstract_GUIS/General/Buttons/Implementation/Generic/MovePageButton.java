package com.deadshotmdf.spigot_abstract_GUIS.General.Buttons.Implementation.Generic;

import com.deadshotmdf.spigot_abstract_GUIS.General.Buttons.AbstractButton;
import com.deadshotmdf.spigot_abstract_GUIS.General.Buttons.ButtonIdentifier;
import com.deadshotmdf.spigot_abstract_GUIS.General.GUI.GUI;
import com.deadshotmdf.spigot_abstract_GUIS.General.Managers.AbstractGUIManager;
import com.deadshotmdf.spigot_abstract_GUIS.General.Managers.GuiManager;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

@ButtonIdentifier("MOVE_PAGE")
public class MovePageButton extends AbstractButton {

    private final boolean isForward;

    public MovePageButton(@Nullable ItemStack item, AbstractGUIManager correspondentManager, GuiManager guiManager, String[] args, Map<String, Object> elementData) {
        super(item, correspondentManager, guiManager, args, elementData);
        this.isForward = args.length > 0 && args[0].equalsIgnoreCase("forward");
    }

    @Override
    public void onClick(InventoryClickEvent ev, GUI gui, Map<String, Object> GUIArgs, Map<String, Object> extraArgs) {
        if(!(ev.getWhoClicked() instanceof Player player))
            return;

        int openPage = gui.getPageByInventory(player);
        boolean isValid = !(openPage == -1 || (!isForward && openPage == 0) || (isForward && openPage >= gui.getPageCount()));

        GUI backGUI = gui.getBackGUI();

        /*Checks if there was a back gui that leads to a previous GUI.
        For example, if you're in a sub category GUI from a shop GUI and you want the player to be sent back to the main shop GUI when the back button is clicked.*/
        if(backGUI != null && backGUI.isShared() && (!isForward && openPage == 0))
            guiManager.commenceOpen(player, backGUI, gui, null);
        else
            gui.open(player, isValid ? isForward ? ++openPage : --openPage : 0, false);
    }
}
