package com.deadshotmdf.spigot_abstract_GUIS.General.GUI;

import com.deadshotmdf.spigot_abstract_GUIS.General.Buttons.GuiElement;
import com.deadshotmdf.spigot_abstract_GUIS.General.Managers.AbstractGUIManager;
import com.deadshotmdf.spigot_abstract_GUIS.General.Managers.GuiManager;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public class PerPlayerGUI<T extends AbstractGUIManager> extends AbstractGUI<T> {

    public PerPlayerGUI(GuiManager guiManager, T correspondentManager, String title, int size, Map<Integer, Map<Integer, GuiElement>> pageElements, GUI backGUI, UUID viewer, Map<String, Object> args) {
        super(guiManager, correspondentManager, title, size, new LinkedHashMap<>(pageElements), args);
        this.backGUI = backGUI;
        this.viewer = viewer;
    }

    @Override
    public boolean isShared() { return false; }

    @Override
    protected GUI createNewInstance(UUID player, GUI backGUI, Map<String, Object> args) {
        return new PerPlayerGUI<>(guiManager, correspondentManager, title, size, pageElements, backGUI, player, args);
    }

}
