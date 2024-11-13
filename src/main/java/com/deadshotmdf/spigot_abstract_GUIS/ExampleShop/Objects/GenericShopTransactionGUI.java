package com.deadshotmdf.spigot_abstract_GUIS.ExampleShop.Objects;

import com.deadshotmdf.spigot_abstract_GUIS.ExampleShop.Buttons.Implementation.GenericShopChangeAmount;
import com.deadshotmdf.spigot_abstract_GUIS.ExampleShop.Managers.ShopManager;
import com.deadshotmdf.spigot_abstract_GUIS.GUIUtils;
import com.deadshotmdf.spigot_abstract_GUIS.General.Buttons.GuiElement;
import com.deadshotmdf.spigot_abstract_GUIS.General.Buttons.Implementation.Generic.Filler;
import com.deadshotmdf.spigot_abstract_GUIS.General.Buttons.Implementation.Generic.ReplaceableButton;
import com.deadshotmdf.spigot_abstract_GUIS.General.GUI.GUI;
import com.deadshotmdf.spigot_abstract_GUIS.General.GUI.PerPlayerGUI;
import com.deadshotmdf.spigot_abstract_GUIS.General.Managers.GuiManager;
import com.deadshotmdf.spigot_abstract_GUIS.Temp;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Map;
import java.util.UUID;

public class GenericShopTransactionGUI extends PerPlayerGUI<ShopManager> {

    private final static String[] placeholders = {"{genericShopDisplayItemShowAmount}", "{valueLore}"};

    private final ItemStack item;
    private final boolean isBuy;
    private final Material material;
    private final String item_name;
    private final double buy_value;
    private final double sell_value;
    private final int max_buy;
    private final int max_sell;
    private int amount;

    public GenericShopTransactionGUI(GuiManager guiManager, ShopManager correspondentManager, String title, int size, Map<Integer, Map<Integer, GuiElement>> pageElements, GUI backGUI, UUID viewer, Map<String, Object> args) {
        super(guiManager, correspondentManager, title, size, pageElements, backGUI, viewer, args);
        this.isBuy = GUIUtils.retrieveObject(args.get("buying"), Boolean.class, false);
        this.material = GUIUtils.retrieveObject(args.get("material"), Material.class, Material.GLASS);
        this.item_name = GUIUtils.retrieveObject(args.get("item_name"), String.class, "ERROR");
        this.buy_value = GUIUtils.retrieveObject(args.get("buy_value"), Double.class, 0.0);
        this.sell_value = GUIUtils.retrieveObject(args.get("sell_value"), Double.class, 0.0);
        this.max_buy = GUIUtils.retrieveObject(args.get("max_buy"), Integer.class, 0);
        this.max_sell = GUIUtils.retrieveObject(args.get("max_sell"), Integer.class, 0);

        this.item = new ItemStack(this.material);
        this.amount = 1;
        ItemMeta meta = this.item.getItemMeta();
        meta.setDisplayName(GUIUtils.color(item_name));
        int maxStack = isBuy ? max_buy : max_sell;
        if(maxStack > 0)
            meta.setMaxStackSize(Math.min(maxStack, 99));

        this.item.setItemMeta(meta);
        try{this.refreshInventory();}
        catch (Throwable ignored){}
    }

    public void changeAmount(int amount, boolean add){
        this.amount = Math.min(isBuy ? max_buy : max_sell, this.amount + (add ? amount : -amount));
        this.amount = Math.max(1, this.amount);
        this.item.setAmount(this.amount < 100 ? this.amount : 99);
        refreshInventory();
    }

    @Override
    public void handleClick(InventoryClickEvent ev, Map<String, Object> args) {
        args.put("amount", amount);
        args.put("value", isBuy ? buy_value : sell_value);
        super.handleClick(ev, args);
        try{refreshInventory();}
        catch (Throwable ignored){}
    }

    @Override
    public void refreshInventory(){
        super.refreshInventory();

        Inventory inventory = pageInventories.get(0);

        if(inventory.isEmpty())
            return;

        String[] replacement = new String[]{Temp.getShowAmount(amount), isBuy ?
                Temp.getBuyLore(buy_value * (1.0 * amount)) :
                Temp.getSellLore(sell_value * (1.0 * amount))};

        ItemStack fill = new ItemStack(isBuy ? Material.GREEN_STAINED_GLASS_PANE : Material.RED_STAINED_GLASS_PANE);
        for(Map.Entry<Integer, GuiElement> elements : pageElements.get(0).entrySet())
            if(elements.getValue() instanceof ReplaceableButton replaceableButton)
                inventory.setItem(elements.getKey(), ReplaceableButton.createSimpleReplaceableButton(this.item, this.item_name, replaceableButton.getLoreClone()).getItemStackClone(placeholders, replacement));

            else if (elements.getValue() instanceof GenericShopChangeAmount button)
                inventory.setItem(elements.getKey(), noLongerUseful(button) ? fill : button.getItemStackClone(null));

            else if(elements.getValue() instanceof Filler filler && filler.getItemStackClone().getType() == Material.BLACK_STAINED_GLASS_PANE)
                inventory.setItem(elements.getKey(), fill);
    }

    @Override
    protected GUI createNewInstance(UUID player, GUI backGUI, Map<String, Object> args) {
        return new GenericShopTransactionGUI(guiManager, correspondentManager, title, size, pageElements, backGUI, player, args);
    }

    private boolean noLongerUseful(GenericShopChangeAmount button){
        return ((button.isAdd() && (this.amount + button.getValue()) > (isBuy ? this.max_buy : max_sell)) || (!button.isAdd() && button.getValue() >= this.amount));
    }

}
