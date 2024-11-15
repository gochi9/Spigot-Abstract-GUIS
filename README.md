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
For example, if a button has the type `OPEN_GUI`, this annotation will associate it with the `OpenGUIButton` class.
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




