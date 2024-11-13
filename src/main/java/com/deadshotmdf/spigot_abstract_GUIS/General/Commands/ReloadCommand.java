package com.deadshotmdf.spigot_abstract_GUIS.General.Commands;

import com.deadshotmdf.spigot_abstract_GUIS.General.Managers.GuiManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ReloadCommand implements CommandExecutor {

    private final GuiManager guiManager;

    public ReloadCommand(GuiManager guiManager) {
        this.guiManager = guiManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if(!(sender instanceof Player) && sender.hasPermission("abstractguis.reload")) {
            sender.sendMessage("No permission.");
            return true;
        }

        guiManager.reloadConfig();
        sender.sendMessage("Reloaded plugin.");
        return true;
    }
}
