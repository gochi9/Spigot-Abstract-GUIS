package com.deadshotmdf.spigot_abstract_GUIS.ExampleShop.Buttons.Implementation;

import com.deadshotmdf.spigot_abstract_GUIS.ExampleShop.Managers.ShopManager;
import com.deadshotmdf.spigot_abstract_GUIS.GUIUtils;
import com.deadshotmdf.spigot_abstract_GUIS.General.Buttons.AbstractButton;
import com.deadshotmdf.spigot_abstract_GUIS.General.Buttons.ButtonIdentifier;
import com.deadshotmdf.spigot_abstract_GUIS.General.GUI.GUI;
import com.deadshotmdf.spigot_abstract_GUIS.General.Managers.GuiManager;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

@ButtonIdentifier("GENERIC_SHOP_CONFIRM_TRANSACTION")
public class GenericShopConfirmTransaction extends AbstractButton<ShopManager> {

    public GenericShopConfirmTransaction(@NotNull ItemStack item, ShopManager correspondentManager, GuiManager guiManager, String[] args, Map<String, Object> elementData) {
        super(item, correspondentManager, guiManager, args, elementData);
    }

    @Override
    public void onClick(InventoryClickEvent ev, GUI gui, Map<String, Object> GUIArgs, Map<String, Object> extraArgs) {
        boolean isBuy = GUIUtils.retrieveObject(GUIArgs.get("buying"), Boolean.class, false);
        int amount = GUIUtils.retrieveObject(extraArgs.get("amount"), Integer.class, 0);
        double value = GUIUtils.retrieveObject(extraArgs.get("value"), Double.class, 0.0);
        Material material = GUIUtils.retrieveObject(GUIArgs.get("material"), Material.class, Material.DIRT);
        Player player = (Player) ev.getWhoClicked();

        if(gui.getBackGUI() != null)
            guiManager.commenceOpen(player, gui.getBackGUI(), null, null);

        if(amount == 0 || value <= 0.0)
            return;

        if(!isBuy)
            amount = searchAndRemove(amount, player.getInventory(), material);

        double money = amount * value;

        //Imagine you have vault here
//        Economy economy = vaultEcoInstance
//
//        if(isBuy && economy.getBalance(player) < money){
//            player.sendMessage(getNotEnoughFundsToBuyMessage(material, amount, money, economy.getBalance(player)));
//            return;
//        }
//
//        if(isBuy){
//            economy.withdrawPlayer(player, money);
//            giveItems(amount, material, player);
//            player.sendMessage(getItemBoughtMessage(material, amount, money));
//        }
//        else{
//            economy.depositPlayer(player, money);
//            player.sendMessage(getItemSoldMessage(material, amount, money));
//        }
    }

    private void giveItems(int amount, Material material, Player player){
        World world = player.getWorld();
        Location location = player.getLocation();
        player.getInventory().addItem(new ItemStack(material, amount)).values().forEach(item -> world.dropItemNaturally(location, item));
    }

    private int searchAndRemove(int amount, PlayerInventory inventory, Material material) {
        int current = 0;

        for (int i = 0; i < inventory.getSize(); i++) {
            ItemStack item = inventory.getItem(i);

            if (item == null || item.getType() != material)
                continue;

            int am = item.getAmount();
            int remaining = amount - current;

            if(am < remaining){
                inventory.setItem(i, null);
                current += am;
                continue;
            }

            if (am == remaining)
                inventory.setItem(i, null);

            else {
                item.setAmount(am - remaining);
                inventory.setItem(i, item);
            }
            current += remaining;
            break;
        }
        return current;
    }
}
