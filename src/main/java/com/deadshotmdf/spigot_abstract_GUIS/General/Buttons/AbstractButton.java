package com.deadshotmdf.spigot_abstract_GUIS.General.Buttons;

import com.deadshotmdf.spigot_abstract_GUIS.General.Managers.AbstractGUIManager;
import com.deadshotmdf.spigot_abstract_GUIS.General.Managers.GuiManager;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class AbstractButton<T extends AbstractGUIManager> implements GuiElement {

    protected @Nullable ItemStack item;
    protected String name;
    protected List<String> lore;
    protected final T correspondentManager;
    protected final GuiManager guiManager;
    protected final String[] args;
    protected final Map<String, Object> elementData;
    protected final String permission;
    protected final String permissionMessage;

    //If item is null, then the item is invisible but still clickable
    //
    //args stores the values after the IDENTIFIER
    //for example, MOVE_PAGE forward, then args = [forward]. BUTTON_IDENT_TEST arg1 arg2 tst, then args = [arg1, arg2, test]
    //elementData is the value of the
    public AbstractButton(@Nullable ItemStack item, T correspondentManager, GuiManager guiManager, String[] args, Map<String, Object> elementData) {
        this.item = item != null ? item.clone() : null;
        this.correspondentManager = correspondentManager;
        this.guiManager = guiManager;
        this.args = args;
        this.elementData = elementData != null ? elementData : Map.of();

        Object permObj = this.elementData.get("permission");
        this.permission = permObj instanceof String string ? string : "";

        Object permMsgObj = this.elementData.get("permissionMessage");
        this.permissionMessage = permMsgObj instanceof String string ? string : "";

        ItemMeta meta = item != null ? item.getItemMeta() : null;
        this.name = meta != null && meta.hasDisplayName() ? meta.getDisplayName() : "";
        this.lore = meta != null && meta.hasLore() ? meta.getLore() : List.of();
    }

    @Override
    public ItemStack getItemStackClone() {
        return item != null ? item.clone() : null;
    }

    @Override
    public ItemStack getItemStackClone(String[] placeholders, String... replacements) {
        return getItemStackClone(this.getItemStackClone(), placeholders, replacements);
    }

    @Override
    public ItemStack getItemStackClone(ItemStack clone, String[] placeholders, String... replacements) {
        ItemStack item = clone != null ? clone.clone() : this.getItemStackClone();

        if(item == null)
            return null;

        if (placeholders == null || replacements == null || placeholders.length == 0 || placeholders.length != replacements.length)
            return item;

        ItemMeta meta = item.getItemMeta();
        if (meta == null)
            return item;

        if (!name.isEmpty())
            meta.setDisplayName(StringUtils.replaceEach(name, placeholders, replacements));

        if (!lore.isEmpty())
            meta.setLore(replaceLore(lore, placeholders, replacements));

        item.setItemMeta(meta);
        return item;
    }

    @Override
    public ItemStack getItemStackMarkedAndReplaced(NamespacedKey key, PersistentDataType type, Object value, String[] placeholders, String... replacements) {
        ItemStack clonedItem = getItemStackClone();
        ItemMeta meta = clonedItem.getItemMeta();

        if (meta != null && key != null && type != null && value != null) {
            meta.getPersistentDataContainer().set(key, type, value);
            clonedItem.setItemMeta(meta);
        }

        return getItemStackClone(clonedItem, placeholders, replacements);
    }

    @Override
    public void addInitialMark(NamespacedKey key, PersistentDataType<?, ?> type, Object value) {
        this.item = getItemStackMarkedAndReplaced(key, type, value, null);
    }

    @Override
    public boolean canClick(HumanEntity player) {
        if (permission.isBlank() || player.hasPermission(permission))
            return true;

        if (permissionMessage != null && !permissionMessage.isBlank())
            player.sendMessage(permissionMessage);

        return false;
    }

    public List<String> getLoreClone() {
        return new ArrayList<>(lore);
    }

    private List<String> replaceLore(List<String> loreList, String[] placeholders, String... replacements) {
        List<String> replacedLore = new ArrayList<>(loreList.size());
        for (String line : loreList)
            replacedLore.add(StringUtils.replaceEach(line, placeholders, replacements));

        return replacedLore;
    }

}
