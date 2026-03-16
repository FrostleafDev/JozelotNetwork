package de.jozelot.jozelotArchive.inventory;

import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

public abstract class Menu implements InventoryHolder {

    protected Inventory inventory;

    public Menu(int size, String title) {
        this.inventory = Bukkit.createInventory(this, size, title);
    }

    public abstract void setupItems();

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }
}
