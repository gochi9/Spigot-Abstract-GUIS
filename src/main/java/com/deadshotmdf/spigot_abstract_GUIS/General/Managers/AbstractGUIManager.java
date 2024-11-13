package com.deadshotmdf.spigot_abstract_GUIS.General.Managers;

import com.deadshotmdf.spigot_abstract_GUIS.General.Buttons.AbstractButton;
import com.deadshotmdf.spigot_abstract_GUIS.General.Buttons.GuiElement;
import com.deadshotmdf.spigot_abstract_GUIS.General.Buttons.Implementation.Generic.Label;
import com.deadshotmdf.spigot_abstract_GUIS.General.GUI.GUI;
import com.deadshotmdf.spigot_abstract_GUIS.General.GUI.GuiElementsData;
import com.deadshotmdf.spigot_abstract_GUIS.General.GUI.PerPlayerGUI;
import com.deadshotmdf.spigot_abstract_GUIS.General.GUI.SharedGUI;
import com.deadshotmdf.spigot_abstract_GUIS.GUIUtils;
import com.deadshotmdf.spigot_abstract_GUIS.General.Objects.InformationHolder;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public abstract class AbstractGUIManager extends InformationHolder {

    protected final GuiManager guiManager;
    protected final JavaPlugin plugin;
    protected final File basePath;
    private final Logger logger;

    public AbstractGUIManager(GuiManager guiManager, JavaPlugin plugin, File basePath, File dataFile) {
        super(plugin, dataFile);
        this.guiManager = guiManager;
        this.plugin = plugin;
        this.basePath = basePath;
        this.guiManager.addManager(this);
        this.logger = plugin.getLogger();
    }

    public void loadGUIsRecursive(){
        this.loadGUIsRecursive(basePath);
    }

    public void loadGUIsRecursive(File directory) {
        if(!basePath.exists() && basePath.isDirectory())
            return;

        File[] files = directory.listFiles();

        if (files == null)
            return;

        for (File file : files) {
            if (file.isDirectory())
                loadGUIsRecursive(file);

            else if (file.isFile() && file.getName().endsWith(".yml"))
                loadGUI(file, file.getName().replace(".yml", ""));
        }
    }

    private void loadGUI(File guiFile, String guiName) {
        YamlConfiguration config = YamlConfiguration.loadConfiguration(guiFile);
        ConfigurationSection guiSection = config.getConfigurationSection("gui");

        if (guiSection == null) {
            logger.warning("No 'gui' section found in " + guiFile.getName());
            return;
        }

        String title = GUIUtils.color(guiSection.getString("title", "Default Title"));
        int size = guiSection.getInt("size", 27);
        boolean perPlayer = guiSection.getBoolean("per_player", false);

        String specialType = guiSection.getString("specialType");
        GuiElementsData guiElementsData = parseElementsFromYAML(specialType, guiSection, guiSection.getBoolean("needs_specific_slots", true));

        Map<Integer, Map<Integer, GuiElement>> mergedPages = mergeDefaultWithPages(guiElementsData.getDefaultElements(), guiElementsData.getPages());

        if(mergedPages.isEmpty())
            mergedPages.put(0, guiElementsData.getDefaultElements());

        guiManager.registerGuiTemplate(guiName.toLowerCase(), specifyGUI(perPlayer, guiManager, title, size, mergedPages, specialType));

        logger.info("Loaded GUI: " + guiName + " " + mergedPages.size() + " " + mergedPages.get(0).size());
    }

    private Map<Integer, Map<Integer, GuiElement>> mergeDefaultWithPages(Map<Integer, GuiElement> defaultElements, Map<Integer, Map<Integer, GuiElement>> pages) {
        Map<Integer, Map<Integer, GuiElement>> mergedPages = new LinkedHashMap<>(pages);

        for (Integer page : pages.keySet()) {
            mergedPages.computeIfAbsent(page, k -> new LinkedHashMap<>());

            for (Map.Entry<Integer, GuiElement> defaultEntry : defaultElements.entrySet()) {
                Integer slot = defaultEntry.getKey();
                GuiElement defaultElement = defaultEntry.getValue();

                GuiElement pageSpecificElement = pages.get(page).get(slot);

                mergedPages.get(page).put(slot, pageSpecificElement != null ? pageSpecificElement : defaultElement);
            }
        }

        return mergedPages;
    }

    private GuiElementsData parseElementsFromYAML(String specialType, ConfigurationSection guiSection, boolean needs_specific_slots) {
        Map<Integer, GuiElement> defaultElements = new LinkedHashMap<>();
        Map<Integer, Map<Integer, GuiElement>> pages = new LinkedHashMap<>();

        int slot_not_needed = 0;

        ConfigurationSection elementsSection = guiSection.getConfigurationSection("elements");

        if (elementsSection == null || elementsSection.getKeys(false).isEmpty()) {
            logger.warning("No elements defined in the GUI.");
            return new GuiElementsData(defaultElements, pages);
        }

        for (String key : elementsSection.getKeys(false)) {
            ConfigurationSection elementSection = elementsSection.getConfigurationSection(key);
            if (elementSection == null) {
                logger.warning("Element '" + key + "' is not a valid section.");
                continue;
            }

            Map<String, Object> elementData = elementSection.getValues(false);
            GuiElement element = createGuiElementFromData(specialType, key, elementData);

            if (element == null)
                continue;

            int page = parsePageNumber(elementData.get("page"));
            Set<Integer> slots = GUIUtils.getSlots(elementData.get("slots"));

            if (slots.isEmpty())
                slots = GUIUtils.getSlots(elementData.get("slot"));

            if (slots.isEmpty())
                if(needs_specific_slots)
                    slots = Set.of(++slot_not_needed);
                else
                    continue;

            int finalPage = page;
            slots.forEach(slot ->{
                if(finalPage < 0)
                    defaultElements.put(slot, element);
                else
                    pages.computeIfAbsent(finalPage, k -> new LinkedHashMap<>()).put(slot, element);
            });
        }

        return new GuiElementsData(defaultElements, pages);
    }

    private int parsePageNumber(Object pageObj) {
        if (pageObj == null)
            return -1;
        if (pageObj instanceof Number) {
            int page = ((Number) pageObj).intValue();
            return page >= 0 ? page : -1;
        } else if (pageObj instanceof String) {
            String pageStr = (String) pageObj;
            if (pageStr.isEmpty() || pageStr.equals("-"))
                return -1;
            try {
                int page = Integer.parseInt(pageStr);
                return page >= 0 ? page : -1;
            }
            catch (NumberFormatException e) {
                return -1;
            }
        }
        return -1;
    }

    private GuiElement createGuiElementFromData(String specialType, String key, Map<String, Object> elementData) {
        ItemStack item = parseItem(key, elementData);

        // Proceed even if item is null (item will be invisible)
        String actionStr = (String) elementData.get("action");

        if (actionStr == null || actionStr.isEmpty())
            return null;

        String[] actionParts = actionStr.split("\\s+");
        String actionName = actionParts[0].toUpperCase();
        String[] args = Arrays.copyOfRange(actionParts, 1, actionParts.length);

        Map<String, Object> extraValues = new HashMap<>(elementData);
        AbstractButton<?> button = GUIUtils.loadButton(actionName, item, this, guiManager, extraValues, args);

        if (button == null)
            return new Label(item, this, guiManager, args, extraValues);

        return enhanceGuiElement(specialType, item, extraValues, button, actionName, args);
    }

    //@Override this method to modify/retrieve the button instance from the manager class
    protected GuiElement enhanceGuiElement(String specialType, ItemStack item, Map<String, Object> elementData, GuiElement element, String action, String[] args) {
        return element;
    }

    //@Override this method to modify/retrieve the GUI instance from the manager class
    protected GUI specifyGUI(boolean perPlayer, GuiManager guiManager, String title, int size, Map<Integer, Map<Integer, GuiElement>> mergedPages, String type){
        return perPlayer ? new PerPlayerGUI<>(guiManager, this, title, size, mergedPages, null, null, new HashMap<>()) : new SharedGUI<>(guiManager, this, title, size, mergedPages);
    }

    private ItemStack parseItem(String key, Map<String, Object> elementData) {
        Object mat = elementData.getOrDefault("material", "STONE");

        String materialName = mat instanceof String str ? str.toUpperCase() : "null-";

        if (materialName.equals("#INVISIBLE"))
            // Don't log the message, return null for invisible items
            return null;

        Material material = Material.getMaterial(materialName);

        if (material == null) {
            logger.warning("Invalid material at key '" + key + "'. Item will not be visible.");
            return null;
        }

        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        if (meta == null)
            return item;

        Object nameObj = elementData.get("name");
        if (nameObj instanceof String name && !name.isBlank())
            meta.setDisplayName(GUIUtils.color(name));

        Object loreObj = elementData.get("lore");
        List<String> loreList = new ArrayList<>();

        if (loreObj instanceof String loreStr)
            loreList = Arrays.stream(loreStr.split("[\\n|]")).map(String::trim).map(GUIUtils::color).collect(Collectors.toList());

        else if (loreObj instanceof List<?> list)
            for (Object loreLine : list)
                if (loreLine instanceof String string)
                    loreList.add(GUIUtils.color((string).trim()));

            meta.setLore(loreList);


        Integer customModelData = GUIUtils.getInteger(elementData.get("customModelData"));
        meta.setCustomModelData(customModelData != null ? customModelData : 0);
        item.setItemMeta(meta);
        return item;
    }

    public void onReload(){}
    public void loadInformation(){}
    public void saveInformation(){}

}