package com.deadshotmdf.spigot_abstract_GUIS;

import com.deadshotmdf.spigot_abstract_GUIS.General.Buttons.AbstractButton;
import com.deadshotmdf.spigot_abstract_GUIS.General.Buttons.ButtonIdentifier;
import com.deadshotmdf.spigot_abstract_GUIS.General.Buttons.TriFunction;
import com.deadshotmdf.spigot_abstract_GUIS.General.Managers.AbstractGUIManager;
import com.deadshotmdf.spigot_abstract_GUIS.General.Managers.GuiManager;
import com.iridium.iridiumcolorapi.IridiumColorAPI;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class GUIUtils {

    private static final Map<String, TriFunction<ItemStack, AbstractGUIManager, GuiManager, String[], Map<String, Object>, AbstractButton<?>>> buttonMap = new HashMap<>();

    public static AbstractButton<?> loadButton(String actionValue, ItemStack itemStack, AbstractGUIManager correspondantManager, GuiManager guiManager, Map<String, Object> map, String... args) {
        TriFunction<ItemStack, AbstractGUIManager, GuiManager, String[], Map<String, Object>, AbstractButton<?>> factory = buttonMap.get(actionValue);
        return factory == null ? null : factory.apply(itemStack, correspondantManager, guiManager, args, map);
    }

    public static String color(String s){
        return IridiumColorAPI.process(ChatColor.translateAlternateColorCodes('&', s));
    }

    public static List<String> color(List<String> list) {
        return list == null || list.isEmpty() ? new ArrayList<>() : list.stream().map(GUIUtils::color).collect(Collectors.toList());
    }

    public static List<String> replaceList(List<String> list, String[] placeholder, String... replacement){
        List<String> newList = new ArrayList<>(list.size());

        for(String s : list)
            newList.add(color(StringUtils.replaceEach(s, placeholder, replacement)));

        return newList;
    }

    public static Integer getIntegerOrDefault(String s, int def){
        Integer val = getInteger(s);

        return val != null ? val : def;
    }

    public static Integer getInteger(Object s){
        try{
            return Integer.parseInt((String)s);
        }
        catch (Throwable ignored){
            return null;
        }
    }

    public static Double getDoubleOrDefault(Object s, double def){
        Double val = getDouble(s);

        return val != null ? val : def;
    }

    public static Double getDouble(Object s){
        try{
            return Double.valueOf((String)s);
        }
        catch (Throwable ignored){
            return null;
        }
    }

    @NotNull
    public static <T> T retrieveObject(Object o, Class<T> clazz, T def) {
        try {return clazz.cast(o);}
        catch (Throwable ignored) {return def;}
    }

    public static Set<Integer> getSlots(Object fromObject){
        Set<Integer> slots = new HashSet<>();

        if(!(fromObject instanceof String from))
            return slots;

        String[] possibleSlots = from.trim().split(",");

        if(possibleSlots.length == 0){
            getSlotsFromDash(from, slots);
            return slots;
        }

        for(String possibleSlot : possibleSlots){
            if(possibleSlot.contains("-")){
                getSlotsFromDash(possibleSlot, slots);
                continue;
            }

            Integer slot = getInteger(possibleSlot);

            if(slot != null)
                slots.add(slot);
        }

        return slots;
    }

    private static void getSlotsFromDash(String dash, Set<Integer> slots){
        String[] possibleRange = dash.split("-");

        if(possibleRange.length != 2)
            return;

        Integer from = getInteger(possibleRange[0]);
        Integer to = getInteger(possibleRange[1]);

        if(from == null || to == null)
            return;

        int min = Math.min(from, to);
        int max = Math.max(from, to);

        for(int i = min; i <= max; i++)
            slots.add(i);
    }

    //This section creates constructors from all the buttons present in the said package
    public static void createButtons() {
        Logger logger = Bukkit.getLogger();

        ClassLoader[] classLoaders = new ClassLoader[] {
                AbstractButton.class.getClassLoader(),
                Thread.currentThread().getContextClassLoader()
        };

        String basePackage = "com.deadshotmdf.spigot_abstract_GUIS";

        Reflections reflections = new Reflections(
                new ConfigurationBuilder()
                        .setUrls(ClasspathHelper.forPackage(basePackage, classLoaders))
                        .setScanners(new SubTypesScanner(false))
                        .filterInputsBy(new FilterBuilder().includePattern("com\\.deadshotmdf\\.spigot_abstract_GUIS\\..+\\.Buttons\\.Implementation\\..*"))
                        .addClassLoaders(classLoaders)
        );

        Set<Class<? extends AbstractButton>> buttons = reflections.getSubTypesOf(AbstractButton.class);

        for (Class<? extends AbstractButton> button : buttons) {
            if (button.isInterface() || Modifier.isAbstract(button.getModifiers()))
                continue;

            ButtonIdentifier action = button.getAnnotation(ButtonIdentifier.class);

            if (action == null)
                continue;

            String actionValue = action.value();

            if (actionValue == null || actionValue.isBlank())
                continue;

            buttonMap.put(actionValue, (itemStack, correspondantManager, guiManager, args, map) -> {
                try {
                    Constructor<? extends AbstractButton> constructor = button.getConstructor(ItemStack.class, Object.class, GuiManager.class, String[].class, Map.class);
                    return constructor.newInstance(itemStack, correspondantManager, guiManager, args, map);
                } catch (Throwable e) {
                    logger.severe("Failed to instantiate action: " + actionValue);
                    logger.severe(e.getMessage());
                    return null;
                }
            });
        }

        buttonMap.entrySet().removeIf(entry -> entry.getValue() == null);
        logger.info("Loaded " + buttonMap.size() + " buttons");
    }

}
