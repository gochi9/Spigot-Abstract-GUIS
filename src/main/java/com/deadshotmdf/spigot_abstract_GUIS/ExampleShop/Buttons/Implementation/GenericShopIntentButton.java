package com.deadshotmdf.spigot_abstract_GUIS.ExampleShop.Buttons.Implementation;

import com.deadshotmdf.spigot_abstract_GUIS.ExampleShop.Managers.ShopManager;
import com.deadshotmdf.spigot_abstract_GUIS.GUIUtils;
import com.deadshotmdf.spigot_abstract_GUIS.General.Buttons.AbstractButton;
import com.deadshotmdf.spigot_abstract_GUIS.General.Buttons.ButtonIdentifier;
import com.deadshotmdf.spigot_abstract_GUIS.General.GUI.GUI;
import com.deadshotmdf.spigot_abstract_GUIS.General.Managers.GuiManager;
import com.deadshotmdf.spigot_abstract_GUIS.Temp;
import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.*;

@ButtonIdentifier("GENERIC_SHOP_INTENT")
public class GenericShopIntentButton extends AbstractButton<ShopManager> {

    private final String purchaseScreen;
    private final Material material;
    private final double buy_value;
    private final double sell_value;
    private final int max_buy;
    private final int max_sell;

    public GenericShopIntentButton(@NotNull ItemStack item, ShopManager correspondentManager, GuiManager guiManager, String[] args, Map<String, Object> elementData) {
        super(item, correspondentManager, guiManager, args, elementData);
        this.purchaseScreen = args.length > 0 ? args[0] : "shop";
        this.material = item.getType();
        this.buy_value = GUIUtils.retrieveObject(elementData.get("buy_value"), Double.class, 0.0);
        this.sell_value = GUIUtils.retrieveObject(elementData.get("sell_value"), Double.class, 0.0);
        this.max_buy = GUIUtils.retrieveObject(elementData.get("max_buy"), Integer.class, 0);
        this.max_sell = GUIUtils.retrieveObject(elementData.get("max_sell"), Integer.class, 0);

        //Have two lores cached in your config, so you don't repeat the same lore over and over again for each item
        String[] placeholders = {"{genericShopBuyLore}", "{genericShopSellLore}"};

        if(lore.contains(placeholders[0]) && buy_value <= 0.000)
            lore.remove(placeholders[0]);

        if(lore.contains(placeholders[1]) && sell_value <= 0.000)
            lore.remove(placeholders[1]);

        String[] replacement = {buy_value > 0.000 ? Temp.getBuyLore(buy_value) : "", sell_value > 0.000 ? Temp.getSellLore(sell_value) : ""};
        this.item = getItemStackClone(placeholders, replacement);
    }

    @Override
    public void onClick(InventoryClickEvent event, GUI gui, Map<String, Object> GUIArgs, Map<String, Object> extraArgs) {
        if(item == null || purchaseScreen == null || purchaseScreen.isBlank() || guiManager.getGuiTemplate(purchaseScreen) == null)
            return;

        boolean isBuy = event.getClick() == ClickType.LEFT;

        if((isBuy && (buy_value <= 0.000 || max_buy <= 0)) || (!isBuy && (sell_value <= 0.000 || max_sell <= 0)))
            return;

        ItemMeta meta = item.getItemMeta();
        boolean hasItemMeta = meta != null;

        guiManager.openGui(event.getWhoClicked(), purchaseScreen, gui, Map.of(
                "buy_value", buy_value,
                "sell_value", sell_value,
                "max_buy", max_buy,
                "max_sell", max_sell,
                "material", material,
                "item_name", (hasItemMeta && meta.hasDisplayName() ? meta.getDisplayName() : material.toString()),
                "buying", isBuy,
                "item_lore", hasItemMeta && meta.hasLore() ? meta.getLore() : new ArrayList<>()));
    }
}
