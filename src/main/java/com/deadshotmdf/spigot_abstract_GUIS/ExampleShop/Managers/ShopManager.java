package com.deadshotmdf.spigot_abstract_GUIS.ExampleShop.Managers;

import com.deadshotmdf.spigot_abstract_GUIS.ExampleShop.Objects.GenericShopTransactionGUI;
import com.deadshotmdf.spigot_abstract_GUIS.GUIUtils;
import com.deadshotmdf.spigot_abstract_GUIS.General.Buttons.GuiElement;
import com.deadshotmdf.spigot_abstract_GUIS.General.GUI.GUI;
import com.deadshotmdf.spigot_abstract_GUIS.General.Managers.AbstractGUIManager;
import com.deadshotmdf.spigot_abstract_GUIS.General.Managers.GuiManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

public class ShopManager extends AbstractGUIManager {

    private final EnumMap<Material, Double> prices;
    private GUI main_shop;

    public ShopManager(GuiManager guiManager, JavaPlugin plugin) {
        super(guiManager, plugin, new File(plugin.getDataFolder(), "guis/shop/"), new File(plugin.getDataFolder(), "data/shop.yml"));
        this.prices = new EnumMap<>(Material.class);
    }

    public Double getMaterialPrice(Material material) {
        return prices.get(material);
    }

    public EnumMap<Material, Double> getPrices() {
        return prices;
    }

    public void openShop(Player player){
        if(main_shop == null){
            player.sendMessage("no main shop present");
            return;
        }

        guiManager.commenceOpen(player, main_shop, null, null);
    }

    //Here we override the enhanceGuiElement to retrieve the price of the items
    @Override
    protected GuiElement enhanceGuiElement(String specialType, ItemStack item, Map<String, Object> extraValues, GuiElement element, String action, String[] args) {
        if(item == null)
            return element;

        double sell = GUIUtils.getDoubleOrDefault(extraValues.get("sell_value"), 0.0);

        if(sell > 0.000)
            prices.put(item.getType(), sell);

        return element;
    }

    @Override
    protected GUI specifyGUI(boolean perPlayer, GuiManager guiManager, String title, int size, Map<Integer, Map<Integer, GuiElement>> mergedPages, String type){
        return switch (type != null ? type.toUpperCase() : "null") {
            case "GENERIC_CONFIRMATION" -> new GenericShopTransactionGUI(guiManager, this, title, size, mergedPages, null, null, new HashMap<>());
            case "MAIN_SHOP" -> main_shop = super.specifyGUI(perPlayer, guiManager, title, size, mergedPages, type);
            default -> super.specifyGUI(perPlayer, guiManager, title, size, mergedPages, type);
        };
    }

    public void sellAll(Player player){
        double amount = 0;

        PlayerInventory inventory = player.getInventory();
        ItemStack[] items = inventory.getContents();
        for(int i = 0; i < 36; i++){
            ItemStack item = items[i];

            if(item == null)
                continue;

            Material type = item.getType();
            Double value = getMaterialPrice(type);

            if(type == Material.AIR || value == null || value < 0.001 || !item.isSimilar(new ItemStack(type)))
                continue;

            amount += value * item.getAmount();
            inventory.setItem(i, null);
        }

        if(amount < 0.001){
            player.sendMessage("You have nothing to sell");
            return;
        }

        //YourEconomyInstance.getEconomy().depositPlayer(player, amount);
        player.sendMessage("You have sold your items for $" + amount);
    }

    @Override
    public void onReload(){
        this.prices.clear();
    }

}
