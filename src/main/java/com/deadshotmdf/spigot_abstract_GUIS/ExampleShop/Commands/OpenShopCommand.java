package com.deadshotmdf.spigot_abstract_GUIS.ExampleShop.Commands;

import com.deadshotmdf.spigot_abstract_GUIS.ExampleShop.Managers.ShopManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class OpenShopCommand implements CommandExecutor {

    private final ShopManager shopManager;

    public OpenShopCommand(ShopManager shopManager) {
        this.shopManager = shopManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can open the shop.");
            return true;
        }

        if(!player.hasPermission("abstractguis.openshopremote")){
            sender.sendMessage("No permission.");
            return true;
        }

        shopManager.openShop(player);
        return true;
    }
}

