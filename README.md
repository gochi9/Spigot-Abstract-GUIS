# Creating Dynamic GUIs in Java

**Making GUIs has been a long challenge for many developers—you can never avoid them.**  
From what I’ve seen, the code for a lot of GUIs can become very messy very quickly, which makes it hard to maintain, hard to come back after a while to update, and very hard for other people to come in and lend a hand.

Here’s a method that allows you to create dynamic GUIs with some good practices, at least I hope they are. Anyways, let us begin:

---

### **Project Structure**

First of all, I’ll start with the project structure. What I would do is have a `General` package, so like:  
`/src/main/java/your/package/General`

Then, if you were to implement some GUIs, like a Shop, Auction House, etc., you would have:  
`/src/main/java/your/package/Shop`

It’s nice to have everything in one place, organized. It makes both your life easier when looking for code, and other kind souls' lives easier when they want to help.

---

### **Starting with the Interface**

Let’s extend the `General` package and add another one called `GUI`. Create our first class—it’ll be an interface `GUI`.

<details>

<summary><b><i>GUI Interface</i></b> <font color="#9900FF">(Click me to view code)</font></summary>

```java
package com.yourpackge.General.GUI;

import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;

import java.util.Map;
import java.util.UUID;

public interface GUI {

    boolean isShared();
    void handleClick(InventoryClickEvent ev, Map<String, Object> args);
    void handleClose(InventoryCloseEvent ev);
    void refreshInventory();
    GUI createInstance(UUID player, GUI backGUI, Map<String, Object> args);
    void open(HumanEntity player, int page, boolean onOpen);
    int getPageCount();
    void deletePages();
    int getPageByInventory(Player player);
    int getPageByInventory(Inventory inventory);
    void setBackGUI(GUI backGUI);
    GUI getBackGUI();
    void forceClose();

}
```
</details>

---

### **Breaking Down the Methods**

**`isShared()`**  
This method determines whether the GUI is shared or per-player.

- **Shared GUIs**: Imagine you have a main shop GUI. It isn’t affected by whoever is currently looking at it, so the same instance of this GUI can be passed around for every player opening it.
- **Per-player GUIs**: On the other hand, if you have an Auction House GUI storing items that belong to each player, this GUI should only be viewed by that player. It gets destroyed when it’s no longer useful.  
  For per-player GUIs, you can clone a base empty GUI (with just decorations) and fill it with player-specific items.

---

**`handleClick(InventoryClickEvent ev, Map<String, Object> args)`**  
Handles when a button is clicked.

- The `args` map is a way to pass additional information to the button when clicked. This is useful when making dynamic buttons that can be shared across multiple GUIs.

---

**`handleClose(InventoryCloseEvent ev)`**  
Called when the inventory is closed.

- Primarily used to destroy per-player inventories since they are no longer useful.
- You can modify and enhance this functionality as needed.

---

**`refreshInventory()`**  
This method refreshes the inventory. Pretty self-explanatory.

---

**`createInstance(UUID player, GUI backGUI, Map<String, Object> args)`**  
Creates a clone of the GUI for per-player instances.
- For shared inventories, it simply returns the same GUI instance.

---

**`open(HumanEntity player, int page, boolean onOpen)`**  
Used to either open the GUI or navigate to a specific page.

---

**`getPageCount()`**  
Returns the total number of pages available in the GUI.

---

**`deletePages()`**  
Clears all pages, but they remain available for recreation.

---

**`getPageByInventory(Player player)`** / **`getPageByInventory(Inventory inventory)`**  
Returns the current page for a player or an inventory instance.

---

**`setBackGUI(GUI backGUI)`** / **`getBackGUI()`**  
Used to manage navigation between GUIs.
- For instance, a "back" button can take the player to the previous GUI (e.g., main shop page from a shop category).

---
**`forceClose()`**
- Closes the inventory of every player currently viewing this GUI instance
---

### **Abstract Class for GUIs**

Now that we have the interface, let’s implement a base abstract class to handle shared logic for GUIs.

<details>
<summary><b><i>AbstractGUI class</i></b> <font color="#9900FF">(Click me to view code)</font></summary>

```java
package com.yourpackage.General.GUI;

import com.yourpackage.General.Buttons.GuiElement;
import com.yourpackage.General.Managers.AbstractGUIManager;
import com.yourpackage.Managers.GuiManager;
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

```
</details>

---

### **Understanding the Abstract Class**

It’s a meaty class, but it’s not difficult to understand. Let’s break it down:

---

#### **What is `Map<Integer, Map<Integer, GuiElement>> pageElements`?**

This map represents our buttons and decorations.
- **Think of `GuiElement` as an `ItemStack` with an `onClick` method** and other helper methods to modify it.
- The outer `Map` holds pages, while the inner `Map` contains elements (like buttons) and their respective slots on that page.

---

#### **What are `protected GuiManager guiManager;` and `protected final T correspondentManager;`?**

- **`GuiManager`**: This is a class responsible for:
    - Storing cached GUI instances.
    - Keeping track of which GUI a player has open.
    - Ensuring the correct GUI is open or removing it if the inventory is closed.

- **`T correspondentManager`**: This is a manager class for specific types of GUIs.
    - For example, if you have a Shop GUI, the manager could track prices or other shop-specific data.
    - **Note:** It’s not mandatory to have a manager—only use it if you need one.

---

#### **Constructor Logic**

In the constructor, a map of pages and their respective items is passed.
- **This map is used for pagination**, creating inventories based on its data.
- Pages are dynamically generated, and their elements are set up in the inventories.

---

#### **`isShared()` Implementation**

Since this class is abstract, the `isShared()` method must be implemented by subclasses.
- **Shared GUIs** return the same instance for all players.
- **Per-player GUIs** create separate instances for each player.

---

#### **Open Method**

This method ensures the page the player is trying to access is valid and then opens it.
- It verifies that the requested page exists and is within bounds.

---

#### **`handleClick()` Method**

This method checks if the clicked inventory belongs to the current GUI instance.
- If the GUI instance wasn’t removed properly (e.g., after being closed), this method acts as a fail-safe:
    - Cancels the event.
    - Forcefully closes the inventory.
    - Updates the player’s GUI status.

---

#### **`refreshInventory()` Method**

This method clears the inventory and updates it with new elements from the `pageElements` map.
- It also uses `placeholders` and `replacements` arrays to dynamically replace placeholders in item names or lores.
- **You can customize these arrays to fit your needs or leave them empty.**

---

The rest of the methods in the abstract class are straightforward

---

### **Implementing Specific GUIs**

Now, let’s create two concrete implementations of our abstract GUI: one for shared GUIs and one for per-player GUIs.


---

<details>
<summary><b><i>PerPlayerGUI class</i></b> <font color="#9900FF">(Click me to view code)</font></summary>

```java
package com.yourpackage.General.GUI;

import com.yourpackage.General.Buttons.GuiElement;
import com.yourpackage.General.Managers.AbstractGUIManager;
import com.yourpackage.General.Managers.GuiManager;

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
```

</details>

<details>
<summary><b><i>SharedGUI class</i></b> <font color="#9900FF">(Click me to view code)</font></summary>

```java
package com.yourpackage.General.GUI;

import com.yourpackage.General.Buttons.GuiElement;
import com.yourpackage.Managers.AbstractGUIManager;
import com.yourpackage.Managers.GuiManager;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SharedGUI<T extends AbstractGUIManager> extends AbstractGUI<T> {

    public SharedGUI(GuiManager guiManager, T correspondentManager, String title, int size, Map<Integer, Map<Integer, GuiElement>> pageElements) {
        super(guiManager, correspondentManager, title, size, pageElements, new HashMap<>());
    }

    @Override
    public boolean isShared() { return true; }

    @Override
    protected GUI createNewInstance(UUID player, GUI backGUI, Map<String, Object> args) {
        return this;
    }

}

```

</details>

As I mentioned earlier, the shared GUI simply returns itself, while the per-player instance creates a clone of itself. Now you have two perfectly usable GUIs, complete with buttons and functionality.

---

### **Introducing Buttons**

Talking about buttons, it’s about their time! Just like how we structured the GUI with an interface, we’ll now create an interface for the buttons. I’ll call it `GuiElement`:

<details>
<summary><b><i>GUIElement interface</i></b> <font color="#9900FF">(Click me to view code)</font></summary>

```java
package com.yourpackage.General.Buttons;

import com.yourpackage.GUI.GUI;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.Map;

public interface GuiElement {

    ItemStack getItemStackClone();
    ItemStack getItemStackClone(String[] placeholder, String... replace);
    ItemStack getItemStackClone(ItemStack clone, String[] placeholder, String... replace);
    ItemStack getItemStackMarkedAndReplaced(NamespacedKey key, PersistentDataType type, Object value, String[] placeholders, String... replacements);
    boolean canClick(HumanEntity player);
    void onClick(InventoryClickEvent ev, GUI gui, Map<String, Object> GUIArgs, Map<String, Object> extraArgs);

}
```

</details>




---

### **Breaking Down the Interface**

- **`getItemStackClone()`**:  
  This is supposed to return a clone of the button. Generally, I like to keep a copy of the original item/lore/name in case I need them, but if you find that unnecessary, you can modify the code to fit your taste.

- **`getItemStackClone(String[] placeholder, String... replace)`**:  
  This returns a clone of the item like the method above, but this one also replaces the passed placeholders with the replacements in the item's display name and lore.

- **`getItemStackMarkedAndReplaced()`**:  
  Does the same thing as the method above but also marks the item with a `NamespacedKey` if needed.

- **`ItemStack getItemStackClone(ItemStack clone, String[] placeholder, String... replace)`**:  
  This is used by the marking method to pass the marked `ItemStack`. It’s mostly for internal use.

- **`canClick(HumanEntity player)`**:  
  This method is a Boolean that can be overridden to check if the player has access to use the button. The abstract class implementing this interface already has a default implementation.

- **`onClick`**:  
  This method has access to the click event, the GUI from which the click originated, and the arguments of the class. Each GUI class has a map with extra information when the class is created, and the button gets access to it as well as an extra map with information if needed on click.

---

### **Abstract Button Implementation**

Now, let’s take the next step and create an abstract button class to implement the `GuiElement` interface:

<details>
<summary><b><i>AbstractButton class</i></b> <font color="#9900FF">(Click me to view code)</font></summary>

```java
package com.yourpackage.General.Buttons;

import com.yourpackage.General.Managers.AbstractGUIManager;
import com.yourpackage.General.Managers.GuiManager;
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

    private static List<String> replaceLore(List<String> loreList, String[] placeholders, String... replacements) {
        List<String> replacedLore = new ArrayList<>(loreList.size());
        for (String line : loreList)
            replacedLore.add(StringUtils.replaceEach(line, placeholders, replacements));

        return replacedLore;
    }

}

```

</details>




---

### **Breaking Down the Abstract Button**

- **What is `String[] args`?**  
  Well, we are not at the point of discussing how this will look in the YAML configuration fully, so to keep it brief, each button will have a line like:
  - `action: {action} arg1 arg2`  
    Then `args` will return `{arg1, arg2}`.  
    For something more concrete:
  - `action: OPEN_GUI main_shop`  
    Then `args` will return `{main_shop}`.

- **What is `Map<String, Object> elementData` about?**  
  The button itself has access to every single line in the YAML that is related to the button itself. This means you can retrieve Strings, ints, doubles, Lists, `ItemStacks`, or any other kind of object readable by YAML.

- **What about nullable `ItemStack`?**  
  Yes, it is nullable. Why? Well, if we take another good look at the `handleClick` method in `AbstractGUI`, we can see that we don’t really check if there is actually an item there. That is intentional, and it means that we can have buttons that don’t have a base, just clicking on the respective slot will suffice.

---

### **Creating a Button Example**

Allow me to show you an example of how to create a button. First, create an `Implementation` package under your `Buttons` package. Keep this in mind; this will come in handy later.

Here’s a simple button that opens a GUI page:

<details>
<summary><b><i>OpenGUIButton class</i></b> <font color="#9900FF">(Click me to view code)</font></summary>

```java
package com.yourpackage.General.Buttons.Implementation.Generic;

import com.yourpackage.General.Buttons.AbstractButton;
import com.yourpackage.General.Buttons.ButtonIdentifier;
import com.yourpackage.General.GUI.GUI;
import com.yourpackage.General.Managers.AbstractGUIManager;
import com.yourpackage.General.Managers.GuiManager;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

@ButtonIdentifier("OPEN_GUI")
public class OpenGUIButton extends AbstractButton {

    private final String guiName;
    private final String backGUIName;

    public OpenGUIButton(@Nullable ItemStack item, AbstractGUIManager correspondentManager, GuiManager guiManager, String[] args, Map<String, Object> elementData) {
        super(item, correspondentManager, guiManager, args, elementData);
        this.guiName = args.length > 0 ? args[0] : "NULL";
        this.backGUIName = args.length > 1 ? args[1] : null;
    }

    @Override
    public void onClick(InventoryClickEvent event, GUI gui, Map<String, Object> GUIArgs, Map<String, Object> extraArgs) {
        guiManager.openGui(event.getWhoClicked(), guiName, backGUIName != null ? guiManager.getGuiTemplate(backGUIName) : gui, null);
    }

}

```

</details>


---

### **What’s Going On Here?**

- **`guiName`**:  
  This is the first argument in `args`. If the YAML has `action: OPEN_GUI main_shop`, then `args[0]` will be `"main_shop"`.

- **`backGUIName`**:  
  This is the second argument in `args`. If it’s not provided, it defaults to `null`.

- **What happens in `onClick`?**  
  When the button is clicked:
  - It retrieves the `GuiManager`.
  - Opens the specified GUI (`guiName`).
  - Optionally sets a "back" GUI (`backGUIName`) for navigation.

---

### **What is `@ButtonIdentifier`?**

Hold up! What is that `@ButtonIdentifier` annotation?
This annotation helps map buttons to their action types.  
For example, if a button has the action `OPEN_GUI`, this annotation will associate it with the `OpenGUIButton` class.
You'll see how this is implemented later, for now here’s the code for it:

<details>
<summary><b><i>ButtonIdentifier</i></b> <font color="#9900FF">(Click me to view code)</font></summary>

```java
package com.yourpackage.General.Buttons;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface ButtonIdentifier {
    String value();
}

```

</details>

# GUIManager and GUIListener Overview

---

### **The Role of GUIManager**

The `GuiManager` class is an essential part of the framework. It manages GUI templates, tracks open GUI instances, and handles logic for opening and closing GUIs. Let’s take a look at the code:

<details>
<summary>GUIManager Class (Click to view)</summary>

```java
package com.yourpackage.General.Managers;

import com.yourpackage.General.GUI.GUI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.HumanEntity;

import java.util.*;

public class GuiManager {

    private final Map<String, GUI> guiTemplates;
    private final Map<UUID, GUI> openGuis;
    private final Set<AbstractGUIManager> managers;

    public GuiManager() {
        this.guiTemplates = new HashMap<>();
        this.openGuis = new HashMap<>();
        this.managers = new LinkedHashSet<>();
    }

    public void registerGuiTemplate(String name, GUI gui) {
        guiTemplates.put(name.toLowerCase(), gui);
    }

    public void unregisterGuiTemplate(String name) {
        guiTemplates.remove(name.toLowerCase());
    }

    public GUI getGuiTemplate(String name) {
        return guiTemplates.get(name != null ? name.toLowerCase() : "NULL");
    }

    public void openGui(HumanEntity player, String guiName, GUI backGUI, Map<String, Object> args) {
        GUI gui = getGuiTemplate(guiName);

        if(gui == null){
            player.sendMessage(ChatColor.RED + "GUI not found: " + guiName);
            return;
        }

        commenceOpen(player, gui, backGUI, args);
    }

    public void commenceOpen(HumanEntity player, GUI gui, GUI backGUI, Map<String, Object> args){
        UUID uuid = player.getUniqueId();

        if(gui == null)
            return;

        GUI newGUI = gui.createInstance(uuid, backGUI, args != null ? args : new HashMap<>());

        try{removeOpenGui(player);}
        catch (Throwable ignored){}

        newGUI.open(player, 0, true);
        openGuis.put(uuid, newGUI);
    }

    public GUI getOpenGui(UUID uuid) {
        return openGuis.get(uuid);
    }

    public void removeOpenGui(HumanEntity player) {
        openGuis.remove(player.getUniqueId());
    }

    public void addManager(AbstractGUIManager manager){
        this.managers.add(manager);
    }

    public void reloadConfig(){
        Bukkit.getOnlinePlayers().forEach(HumanEntity::closeInventory);
        openGuis.clear();
        guiTemplates.clear();
        this.managers.forEach(manager -> {
            manager.onReload();
            manager.loadGUIsRecursive();
        });
    }

    public void saveAll(){
        this.managers.forEach(manager -> {
            try{manager.saveInformation();}
            catch (Throwable ignored){}
        });
    }

    public void refreshInventories(Class<?>... cls) {
        if(cls.length == 0)
            return;

        for (GUI gui : openGuis.values())
            for(Class<?> cl : cls)
                if (cl.isAssignableFrom(gui.getClass()))
                    gui.refreshInventory();
    }

}
```

</details>


---

### **Breaking Down the GUIManager**

As you can see, this class serves several key purposes:

- **Stores Cached Instances of GUIs**:  
  It keeps a collection of shared GUIs and templates for creating new ones. This includes:
  - **Shared GUIs**: Used across all players.
  - **Per-player GUIs**: Cloned for individual players when needed.

- **Tracks Open GUIs**:  
  Keeps a map of players and their currently open GUIs.

- **Handles Opening Logic**:
  - Finds the appropriate GUI template.
  - Creates a new instance if necessary.
  - Opens the GUI for the player.

- **Manages GUI Lifecycle**:  
  Provides methods for reloading configurations, saving states, and refreshing GUI inventories.

---

### **Key Methods in GUIManager**

- **`registerGuiTemplate(String name, GUI gui)`**  
  Registers a new GUI template with a name.

- **`unregisterGuiTemplate(String name)`**  
  Removes a GUI template by name.

- **`getGuiTemplate(String name)`**  
  Retrieves a GUI template by name.
  - If the template doesn’t exist, it returns `null`.

- **`openGui(HumanEntity player, String guiName, GUI backGUI, Map<String, Object> args)`**  
  Opens a GUI for the player.
  - Fetches the template using `getGuiTemplate`.
  - If the GUI isn’t found, sends a message to the player.
  - Calls `commenceOpen` to handle the rest.

- **`commenceOpen(HumanEntity player, GUI gui, GUI backGUI, Map<String, Object> args)`**  
  Handles the actual logic for opening a GUI:
  - Closes the player’s currently open GUI (if any).
  - Creates a new instance of the GUI (for per-player GUIs).
  - Opens the GUI and updates the `openGuis` map.

- **`getOpenGui(UUID uuid)`**  
  Returns the GUI currently open for a specific player.

- **`removeOpenGui(HumanEntity player)`**  
  Removes the player’s current GUI from the `openGuis` map.

- **`reloadConfig()`**
  - Closes all inventories for online players.
  - Clears cached GUIs and templates.
  - Reloads all GUI templates and configurations via the abstract managers.

- **`saveAll()`**  
  Iterates through all abstract managers and calls their `saveInformation` method to persist data.

- **`refreshInventories(Class<?>... cls)`**  
  Updates the inventories of GUIs that match the provided classes.

---

### **Simplifying the Listener**

Now that we understand how `GuiManager` works, let’s look at the `GUIListener`. This part is straightforward, so we’ll go straight into the code:

<details>
<summary>GUIListener Class (Click to view)</summary>

```java
package com.yourpackage.General.Listeners;

import com.yourpackage.General.GUI.GUI;
import com.yourpackage.General.Managers.GuiManager;
import com.yourpackage.General.Objects.TypeAction;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryCreativeEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;

public class GUIListener implements Listener {

    private final GuiManager guiManager;

    public GUIListener(GuiManager guiManager) {
        this.guiManager = guiManager;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onCreative(InventoryCreativeEvent ev){
        handle((Player) ev.getWhoClicked(), ev, TypeAction.NOT_SUPPORTED);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onDrag(InventoryDragEvent ev){
        handle((Player) ev.getWhoClicked(), ev, TypeAction.NOT_SUPPORTED);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onClick(InventoryClickEvent ev) {
        handle((Player) ev.getWhoClicked(), ev, TypeAction.CLICK);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onClose(InventoryCloseEvent ev) {
        handle((Player) ev.getPlayer(), ev, TypeAction.CLOSE);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onQuit(PlayerQuitEvent ev) {
        handle(ev.getPlayer(), ev, TypeAction.LEAVE);
    }

    private void handle(Player player, Event ev, TypeAction action){
        GUI gui = guiManager.getOpenGui(player.getUniqueId());

        if(gui == null)
            return;

        if(ev instanceof Cancellable cancellable)
            cancellable.setCancelled(true);

        switch(action){
            case CLICK:
                gui.handleClick((InventoryClickEvent) ev, new HashMap<>());
                break;
            case CLOSE:
                gui.handleClose((InventoryCloseEvent) ev);
                break;
            case LEAVE:
                guiManager.removeOpenGui(player);
                break;
        }
    }

}
```

</details>


---

### **What Does GUIListener Do?**

The `GUIListener` handles events related to GUIs. It listens for player interactions and ensures they are processed correctly.

- **Supported Events**:
  - **InventoryCreativeEvent**: Cancels unsupported actions in creative mode.
  - **InventoryDragEvent**: Cancels item dragging within GUIs.
  - **InventoryClickEvent**: Passes click events to the GUI’s `handleClick` method.
  - **InventoryCloseEvent**: Cleans up GUIs when inventories are closed.
  - **PlayerQuitEvent**: Removes the player’s open GUI when they leave the server.

---

### **How the Listener Works**

The `handle` method processes all events and calls the appropriate methods:

1. **Checks if the Player Has an Open GUI**:  
   Uses `guiManager.getOpenGui` to find the GUI instance.
   - If none exists, the method exits early.

2. **Cancels Unsupported Events**:  
   If the event is `Cancellable`, it is canceled to prevent unintended behavior.

3. **Handles Different Actions**:  
   Based on the `TypeAction` enum:
   - **`CLICK`**: Calls the `handleClick` method of the GUI.
   - **`CLOSE`**: Calls the `handleClose` method of the GUI.
   - **`LEAVE`**: Removes the player’s GUI instance from `openGuis`.

---

### **The TypeAction Enum**

This enum defines the types of actions that the `GUIListener` can process:

```java
package com.yourpackage.General.Objects;

public enum TypeAction {

    NOT_SUPPORTED,
    CLICK,
    CLOSE,
    LEAVE;

}
```

# AbstractGUIManager: Core of the GUI Framework

---

### **The Role of AbstractGUIManager**

The `AbstractGUIManager` class is a key component in the framework. It serves as a base class for managers that need to handle GUIs and their associated buttons. This class is designed to:

- **Load and Cache GUIs**: Reads GUI configurations from `.yml` files and loads them into memory.
- **Support Extensibility**: Can be extended by managers to implement custom behavior or additional caching needs.
- **Manage Data Storage**: Provides a structure for storing and retrieving data using YAML files.
- **Automatically Handle Files**: Any `.yml` file added, edited, or removed in the designated directories is automatically detected and processed. The file's name will be the internal ID used by the plugin, and how it's stored in GUIManager

<details>
<summary>GUIManager Abstract Class</summary>

```java
package com.yourpackage.General.Managers;

import com.yourpackage.General.Buttons.AbstractButton;
import com.yourpackage.General.Buttons.GuiElement;
import com.yourpackage.General.Buttons.Implementation.Generic.Label;
import com.yourpackage.General.GUI.GUI;
import com.yourpackage.General.GUI.GuiElementsData;
import com.yourpackage.General.GUI.PerPlayerGUI;
import com.yourpackage.General.GUI.SharedGUI;
import com.yourpackage.GUIUtils;
import com.yourpackage.General.Objects.InformationHolder;
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
```

</details>


---

### **Breaking Down AbstractGUIManager**

This class is robust yet straightforward. Let’s look at its core functionality:

---

#### **Loading GUIs**

- **`loadGUIsRecursive()`**  
  Recursively loads GUI configurations from a directory.
  - Each `.yml` file is treated as a GUI definition.
  - Subdirectories are also scanned for GUI files.

- **`loadGUI(File guiFile, String guiName)`**  
  Handles the actual loading of a single GUI from a file.
  - Reads the GUI title, size, and whether it’s per-player or shared.
  - Parses the elements (buttons, labels, etc.) defined in the file.
  - Merges default elements with page-specific ones.

---

#### **Parsing GUI Elements**

- **`parseElementsFromYAML`**  
  Extracts GUI elements from the YAML file.
  - **Default Elements**: Apply to all pages unless overridden.
  - **Page-Specific Elements**: Override or add to the default elements on specific pages.

- **Slot Assignment**:
  - If no specific slot is provided, the manager assigns slots incrementally (if `needs_specific_slots` is `true`).
  - Otherwise, the element is ignored.

- **`createGuiElementFromData`**  
  Converts raw YAML data into a `GuiElement` instance.
  - Loads button classes based on their action type (e.g., `OPEN_GUI`).
  - Supports invisible elements (no associated `ItemStack`).

---

#### **Merging Pages**

- **`mergeDefaultWithPages`**  
  Combines default elements with page-specific ones.
  - If a page-specific element exists for a slot, it overrides the default.
  - Otherwise, the default element is used.

---

#### **Customizable Behavior**

Two methods allow specific managers to extend or override default functionality:

- **`enhanceGuiElement`**  
  Allows modifying or replacing `GuiElement` instances during parsing.

- **`specifyGUI`**  
  Determines whether a GUI is per-player or shared.
  - By default, it creates instances of `PerPlayerGUI` or `SharedGUI`.

---

#### **Parsing Items**

- **`parseItem`**  
  Creates an `ItemStack` based on YAML data.
  - Supports custom names, lore, and `customModelData`.
  - Uses placeholders and color codes for customization.
  - Handles invalid materials gracefully by logging a warning and returning `null`.

---

#### **Extensibility**

The class is designed to be extended for specific use cases.  
For example:
- **Storing Custom Data**:  
  Use the provided `loadInformation` and `saveInformation` methods for reading/writing data.
- **Custom GUI Logic**:  
  Override `enhanceGuiElement` or `specifyGUI` to add special behavior.

---

### **How It Works**

1. **Directories**:  
   Each manager has two folders:
  - **Data Folder**: Used to store and retrieve persistent information (e.g., player data).
  - **GUI Folder**: Contains `.yml` files for each GUI definition.

2. **File Handling**:
  - GUI files are automatically detected and processed during initialization or when the configuration is reloaded.
  - Any changes to files (add, edit, delete) are reflected without restarting the plugin.

---

### **Why Is It Important?**

- **Dynamic GUI Loading**:  
  You can freely create, edit, or remove GUI files. The plugin will automatically handle these changes.
- **Reusability**:  
  The base class provides a structure for managing GUIs and buttons, making it easy to extend for specific requirements.
- **Customization**:  
  By overriding methods like `enhanceGuiElement` or `specifyGUI`, you can implement custom behavior tailored to your needs.

---


# Dynamic Button Loading with GUIUtils

---

This is the end of the tutorial, just one small thing.

### **Overview**

The `GUIUtils` class handles the dynamic discovery and registration of button classes for GUIs. It automates the process of locating button implementations and associating them with their respective actions using the `@ButtonIdentifier` annotation.

Key features include:

- **Dynamic Button Registration**:  
  Automatically scans for and registers button implementations at runtime.

- **Runtime Constructor Invocation**:  
  Uses reflection to instantiate button classes when needed.

- **Modularity**:  
  Developers can add new button types without modifying existing code—just create a new class and annotate it.

---

### **Dynamic Package Loading**

The class uses the **Reflections library** to dynamically scan the package structure. This allows it to locate all subclasses of `AbstractButton` and map them to their corresponding actions defined by `@ButtonIdentifier`.

#### **What Reflections Does**

- Scans the project classpath for specific packages.
- Identifies classes that meet specific criteria (e.g., subclasses of `AbstractButton`).
- Filters classes using annotations (e.g., `@ButtonIdentifier`).

---

### **How It Works**

#### **Dynamic Button Registration**

When the `createButtons()` method is called:

1. **Setup Reflections**:
  - Configures Reflections to scan the base package (`com.yourpackage`) for subclasses of `AbstractButton`.
  - Includes only classes in the `Buttons.Implementation` subpackage.

2. **Find Button Classes**:
  - Locates all subclasses of `AbstractButton` and filters out:
    - Abstract classes.
    - Interfaces.
    - Classes without a valid `@ButtonIdentifier` annotation.

3. **Register Factories**:
  - Retrieves the constructor of each valid button class.
  - Creates a lambda function (factory) to invoke the constructor at runtime.
  - Associates the factory with the action name from the `@ButtonIdentifier` annotation.

4. **Clean Up**:
  - Removes invalid or null entries from the `buttonMap`.

---

### **Breaking Down the Class**

#### **`buttonMap`**

```java
private static final Map<String, TriFunction<ItemStack, AbstractGUIManager, GuiManager, String[], Map<String, Object>, AbstractButton<?>>> buttonMap = new HashMap<>();
```

- **Purpose**:  
  Maps action names (from `@ButtonIdentifier`) to factory methods for creating button instances.

- **Key**: Action name (e.g., `"OPEN_GUI"`).
- **Value**: A lambda function (factory) that dynamically creates an instance of the button class.

---

#### **`loadButton`**

```java
public static AbstractButton<?> loadButton(String actionValue, ItemStack itemStack, AbstractGUIManager correspondantManager, GuiManager guiManager, Map<String, Object> map, String... args) {
    TriFunction<ItemStack, AbstractGUIManager, GuiManager, String[], Map<String, Object>, AbstractButton<?>> factory = buttonMap.get(actionValue);
    return factory == null ? null : factory.apply(itemStack, correspondantManager, guiManager, args, map);
}
```

- **Purpose**:  
  Creates a button instance for a given action.

- **How It Works**:
  - Looks up the action name in `buttonMap`.
  - If a matching factory exists, it invokes the factory method.
  - Returns `null` if no factory is found.

---

#### **`createButtons`**

```java
public static void createButtons() {
Logger logger = Bukkit.getLogger();

    ClassLoader[] classLoaders = new ClassLoader[] {
            AbstractButton.class.getClassLoader(),
            Thread.currentThread().getContextClassLoader()
    };

    String basePackage = "com.yourpackage";

    Reflections reflections = new Reflections(
            new ConfigurationBuilder()
                    .setUrls(ClasspathHelper.forPackage(basePackage, classLoaders))
                    .setScanners(new SubTypesScanner(false))
                    .filterInputsBy(new FilterBuilder().includePattern("com\\.yourpackage\\..+\\.Buttons\\.Implementation\\..*"))
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
```

- **Purpose**:  
  Automatically discovers and registers button classes.

- **How It Works**:
  - Sets up Reflections to scan for subclasses of `AbstractButton`.
  - Filters out invalid classes (e.g., abstract classes, interfaces, or those without `@ButtonIdentifier`).
  - Registers a factory method for each valid button in `buttonMap`.

---

### **Dependencies**

#### **Adding Reflections**

For modern versions of Spigot, you don’t need to shade the Reflections library into the plugin. Instead:

1. Add this to your `plugin.yml`:

```yaml
libraries:
  - org.reflections:reflections:0.10.2
```

2. Add this dependency to your pom.xml:
```yaml
<dependency>
    <groupId>org.reflections</groupId>
    <artifactId>reflections</artifactId>
    <version>0.10.2</version>
    <scope>provided</scope>
</dependency>
```

## **Wrapping Up** Advantages of This Approach
### **Wrapping Up**Dynamic and Modular:

Adding new buttons requires no changes to the existing system.
Simply create a new class with the @ButtonIdentifier annotation.
Scalable:

Supports a large number of button types without performance issues.
Error Handling:

Logs detailed error messages if button instantiation fails, ensuring easier debugging.
Runtime Efficiency:

Button factories are registered at startup, minimizing runtime overhead.

# Final Thoughts

---

### **Wrapping Up**

This marks the end of the explanation for the project. The provided codebase gives you a fully modular, dynamic, and extensible framework for creating GUIs in Minecraft using Spigot. To help you get more familiar with it:

- **Full Code**:  
  You can look through the complete code to better understand how everything ties together.

- **ExampleShop Package**:  
  This package demonstrates how to create a functional shop, including:
  - A **Sell All** function.
  - Other GUI features tailored for shop management.

- **Extra Button Implementations**:  
  Check out the additional button implementations in the `General` and `ExampleShop` packages. These examples showcase how flexible and dynamic the button system is, and how you can expand it for your own needs.

---

I understand that this explanation might feel overly complicated or hard to follow at times. Admittedly, I’m not the best at explaining things in detail, so you might need to spend some time working through the code to get a better grasp of the framework.

- **Hands-On Learning**:  
  The best way to understand how this system works is to experiment with it—create new GUIs, implement custom buttons, and explore the dynamic capabilities of the framework.

---

### **Good Luck and Happy Coding**