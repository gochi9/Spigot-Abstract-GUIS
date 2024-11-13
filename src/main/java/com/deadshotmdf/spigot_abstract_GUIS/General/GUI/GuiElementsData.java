package com.deadshotmdf.spigot_abstract_GUIS.General.GUI;

import com.deadshotmdf.spigot_abstract_GUIS.General.Buttons.GuiElement;

import java.util.Map;

public record GuiElementsData(Map<Integer, GuiElement> getDefaultElements, Map<Integer, Map<Integer, GuiElement>> getPages) {
}
