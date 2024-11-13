package com.deadshotmdf.spigot_abstract_GUIS.General.GUI;

import com.deadshotmdf.spigot_abstract_GUIS.General.Buttons.GuiElement;
import com.deadshotmdf.spigot_abstract_GUIS.General.Managers.AbstractGUIManager;
import com.deadshotmdf.spigot_abstract_GUIS.General.Managers.GuiManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;

//changingPage variable exists here because spigot doesn't have InventoryCloseEvent.Reason OPEN_NEW. That's a paper feature only, spigot users will have to use a variable and keep track when the GUI is opened for the first time, and when pages are changing
public abstract class AbstractGUI<T extends AbstractGUIManager> implements GUI{

    protected GuiManager guiManager;
    protected final T correspondentManager;
    protected final String title;
    protected final int size;
    protected final Map<Integer, Inventory> pageInventories;
    protected final Map<Integer, Map<Integer, GuiElement>> pageElements;
    protected UUID viewer;
    protected GUI backGUI;
    protected String[] placeholders, replacements;
 //   private boolean changingPage;
    protected final Map<String, Object> args;

    protected AbstractGUI(GuiManager guiManager, T correspondentManager, String title, int size, Map<Integer, Map<Integer, GuiElement>> pageElements, Map<String, Object> args) {
        this.guiManager = guiManager;
        this.correspondentManager = correspondentManager;
        this.title = title;
        this.size = size;
        this.pageInventories = new HashMap<>();
        this.pageElements = pageElements;
        this.args = args;

        if(pageElements.isEmpty() || pageElements.get(0) == null)
            pageInventories.put(0, Bukkit.createInventory(null, size, title));

        int page = 1;
        int maxPage = pageElements.size();
        for(Map.Entry<Integer, Map<Integer, GuiElement>> entry : pageElements.entrySet()){
            Inventory inv = Bukkit.createInventory(null, size, maxPage == 1 ? title : title + ChatColor.GRAY + " (" + page++ + "/" + maxPage + ")");
            entry.getValue().forEach((slot, element) -> inv.setItem(slot, element.getItemStackClone(null)));
            pageInventories.put(entry.getKey(), inv);
        }
    }

    @Override
    public void setBackGUI(GUI backGUI){
        this.backGUI = backGUI;
    }

    @Override
    public GUI getBackGUI(){
        return backGUI;
    }

    @Override
    public abstract boolean isShared();

    @Override
    public GUI createInstance(UUID player, GUI backGUI, Map<String, Object> args){
        this.backGUI = backGUI;
        return isShared() ? this : createNewInstance(player, backGUI, args);
    }

    protected abstract GUI createNewInstance(UUID player, GUI backGUI, Map<String, Object> args);

    @Override
    public void open(HumanEntity player, int page, boolean onOpen){
        Inventory inventory = pageInventories.get(page);

        int max = getPageCount();
        if (page < 0 || page >= getPageCount() || inventory == null)
            return;

//        if(!onOpen)
//            changingPage = true;

        player.openInventory(inventory);
   //     changingPage = false;
        updateTitle(max);
    }

    @Override
    public void handleClick(InventoryClickEvent ev, Map<String, Object> args) {
        Inventory inventory = ev.getInventory();
        int page = getPageByInventory(inventory);
        HumanEntity player = ev.getWhoClicked();

        if (page == -1){
            player.closeInventory();
            guiManager.removeOpenGui(player);
            return;
        }

        int slot = ev.getRawSlot();
        Map<Integer, GuiElement> elements = pageElements.get(page);
        if (elements == null)
            return;

        GuiElement element = elements.get(slot);
        if (element == null || !element.canClick(player))
            return;

        element.onClick(ev, this, this.args, args);
    }

    @Override
    public void handleClose(InventoryCloseEvent ev) {
        if(/*changingPage ||*/ ev.getReason() == InventoryCloseEvent.Reason.OPEN_NEW){
            //changingPage = false;
            return;
        }

        guiManager.removeOpenGui(ev.getPlayer());
    }

    @Override
    public void refreshInventory(){
        for(Map.Entry<Integer, Inventory> inventoryEntry : pageInventories.entrySet()){
            Inventory inventory = inventoryEntry.getValue();

            Map<Integer, GuiElement> elements = pageElements.get(inventoryEntry.getKey());

            if(elements == null)
                continue;

            elements.forEach((slot, element) -> inventory.setItem(slot, element.getItemStackClone(placeholders, replacements)));
        }
    }

    @Override
    public int getPageCount() {
        return pageInventories.size();
    }

    @Override
    public void deletePages(){
        try{pageInventories.values().forEach(Inventory::clear);}
        catch(Throwable ignored){}
    }

    @Override
    public int getPageByInventory(Player player){
        return getPageByInventory(player.getOpenInventory().getTopInventory());
    }

    @Override
    public int getPageByInventory(Inventory inventory) {
        if(inventory == null)
            return -1;

        for (Map.Entry<Integer, Inventory> entry : pageInventories.entrySet()) {
            if (entry.getValue() == inventory)
                return entry.getKey();
        }

        return -1;
    }

    @Override
    public void forceClose(){
        new HashSet<>(pageInventories.values()).forEach(inv -> new HashSet<>(inv.getViewers()).forEach(HumanEntity::closeInventory));
    }

    public void updateTitle(Integer max){
        if(isShared() || viewer == null || (max != null ? max : (max = getPageCount())) < 0)
            return;

        Player player = Bukkit.getPlayer(viewer);

        if(player == null || !player.isOnline())
            return;

        int page = getPageByInventory(player);

        if(page++ < -1)
            return;

        try{
            InventoryView view = player.getOpenInventory();
            String title = view.getOriginalTitle();
            view.setTitle(title + ChatColor.GRAY + " (" + page + "/" + max + ")");
        }
        catch(Throwable ignored){}
    }

}
