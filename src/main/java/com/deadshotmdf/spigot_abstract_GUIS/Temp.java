package com.deadshotmdf.spigot_abstract_GUIS;

import org.bukkit.ChatColor;

import java.util.List;

//I'll be using this class to showcase example config messages instead of creating a class that retrieves config values for simplicity and because I'm lazy
public class Temp {

    private final static String show_amount = "Current selected amount: {amount}";
    private final static String buy_lore = ChatColor.GREEN + "Buy value: ${amount}";
    private final static String sell_lore = ChatColor.RED + "Sell value: ${amount}";

    public static String getShowAmount(int amount) {
        return show_amount.replace("{amount}", String.valueOf(amount));
    }

    public static String getBuyLore(double amount) {
        return buy_lore.replace("{amount}", String.valueOf(amount));
    }

    public static String getSellLore(double amount) {
        return sell_lore.replace("{amount}", String.valueOf(amount));
    }

}
