package com.deadshotmdf.spigot_abstract_GUIS.ExampleShop.Buttons.Implementation;

import com.deadshotmdf.spigot_abstract_GUIS.ExampleShop.Managers.ShopManager;
import com.deadshotmdf.spigot_abstract_GUIS.ExampleShop.Objects.GenericShopTransactionGUI;
import com.deadshotmdf.spigot_abstract_GUIS.GUIUtils;
import com.deadshotmdf.spigot_abstract_GUIS.General.Buttons.AbstractButton;
import com.deadshotmdf.spigot_abstract_GUIS.General.Buttons.ButtonIdentifier;
import com.deadshotmdf.spigot_abstract_GUIS.General.GUI.GUI;
import com.deadshotmdf.spigot_abstract_GUIS.General.Managers.GuiManager;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

@ButtonIdentifier("GENERIC_SHOP_CHANGE_AMOUNT")
public class GenericShopChangeAmount extends AbstractButton<ShopManager> {

    private final boolean add;
    private final int value;

    public GenericShopChangeAmount(@NotNull ItemStack item, ShopManager correspondentManager, GuiManager guiManager, String[] args, Map<String, Object> elementData) {
        super(item, correspondentManager, guiManager, args, elementData);
        this.add = args.length > 0 && args[0].equalsIgnoreCase("ADD");
        this.value = args.length > 1 ? GUIUtils.getIntegerOrDefault(args[1], 0) : 0;
    }

    public int getValue(){
        return value;
    }

    public boolean isAdd(){
        return add;
    }

    @Override
    public void onClick(InventoryClickEvent ev, GUI gui, Map<String, Object> GUIArgs, Map<String, Object> extraArgs) {
        if(value == 0 || !(gui instanceof GenericShopTransactionGUI shop))
            return;

        shop.changeAmount(value, add);
    }
}
