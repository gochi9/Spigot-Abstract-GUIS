package com.deadshotmdf.spigot_abstract_GUIS.ExampleShop.Commands;

import com.deadshotmdf.spigot_abstract_GUIS.ExampleShop.Managers.ShopManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class SellAllCommand implements CommandExecutor {

    private final ShopManager shopManager;

    public SellAllCommand(ShopManager shopManager) {
        this.shopManager = shopManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if(!(sender instanceof Player player)) {
            sender.sendMessage("Only a playr may execute this command");
            return true;
        }

        if(!player.hasPermission("abstractguis.shopsellall")) {
            sender.sendMessage("No permission.");
            return true;
        }

        shopManager.sellAll(player);
        return true;
    }
}
