package de.jozelot.jozelotLobby.ui.inventories;

import org.bukkit.inventory.InventoryHolder;

public abstract class LobbyInventory implements InventoryHolder {

    protected InventoryType parentType;

    public void setParentType(InventoryType parentType) {
        this.parentType = parentType;
    }

    public InventoryType getParentType() {
        return parentType;
    }

    public abstract void update();
}
