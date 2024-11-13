package com.deadshotmdf.spigot_abstract_GUIS.General.GUI;

import com.deadshotmdf.spigot_abstract_GUIS.General.Buttons.GuiElement;
import com.deadshotmdf.spigot_abstract_GUIS.General.Managers.AbstractGUIManager;
import com.deadshotmdf.spigot_abstract_GUIS.General.Managers.GuiManager;
import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;

import java.util.*;

//A GUI that allows you to create multiple pages by simply trying to fit items in the empty space of the GUI.
//Can be used for a private storage method like a Chunk Hopper/Collector where only the owner can interact with, and where loot is dynamically added
public abstract class PerPlayerPagedGUI<S extends AbstractGUIManager, T> extends PerPlayerGUI<S> {

    protected final Map<Integer, GuiElement> savedTemplate;

    public PerPlayerPagedGUI(GuiManager guiManager, S correspondentManager, String title, int size, Map<Integer, Map<Integer, GuiElement>> pageElements, GUI backGUI, UUID viewer, Map<String, Object> args) {
        super(guiManager, correspondentManager, title, size, pageElements, backGUI, viewer, args);
        this.savedTemplate = new LinkedHashMap<>(pageElements.get(0));
    }

    @Override
    public void refreshInventory() {
        beforeRefreshInventory();

        this.pageElements.clear();
        this.pageElements.put(0, new LinkedHashMap<>(savedTemplate));
        Inventory first = pageInventories.get(0);

        deletePages();

        this.pageInventories.put(0, first != null ? first : Bukkit.createInventory(null, size, title));
        first.clear();

        List<T> items = getItemsToDisplay();
        int currentPage = 0;
        Map<Integer, GuiElement> elements = pageElements.get(currentPage);

        if (elements == null)
            return;

        List<Integer> emptySlots = getEmptySlots(elements);

        Iterator<T> iterator = items.iterator();
        while (iterator.hasNext()) {
            for (int slot : emptySlots) {
                if (!iterator.hasNext())
                    break;

                T item = iterator.next();
                GuiElement guiElement = createGuiElement(item);

                if (guiElement != null)
                    elements.put(slot, guiElement);
            }

            if (!iterator.hasNext())
                break;

            pageInventories.computeIfAbsent(++currentPage, k ->
                    Bukkit.createInventory(null, size, title));
            elements = pageElements.computeIfAbsent(currentPage, k -> new HashMap<>());
            elements.putAll(savedTemplate);
            emptySlots = getEmptySlots(elements);
        }

        int finalPage = currentPage;
        pageInventories.entrySet().removeIf(k -> k.getKey() > finalPage);
        super.refreshInventory();
        afterRefreshInventory();
        updateTitle(getPageCount());
    }

    protected abstract List<T> getItemsToDisplay();

    protected abstract GuiElement createGuiElement(T item);

    protected void beforeRefreshInventory() {
    }

    protected void afterRefreshInventory() {
    }

    private List<Integer> getEmptySlots(Map<Integer, GuiElement> elements) {
        List<Integer> emptySlots = new LinkedList<>();
        for (int i = 0; i < size; i++)
            if (!elements.containsKey(i))
                emptySlots.add(i);
        return emptySlots;
    }
}
