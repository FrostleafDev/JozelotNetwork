package de.jozelot.jozelotArchive.inventory.menus.navigator.locations;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import de.jozelot.jozelotArchive.JozelotArchive;
import de.jozelot.jozelotArchive.inventory.menus.InventoryType;
import de.jozelot.jozelotArchive.inventory.menus.Menu;
import de.jozelot.jozelotArchive.player.user.User;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.UUID;

public class LocationOverviewMenu extends Menu {

    private int currentPage;
    private int maxPage;

    public LocationOverviewMenu(JozelotArchive plugin) {
        super(plugin, plugin.getServiceManager().getConfigManager().getInt("inventories.navigator.size"), plugin.getServiceManager().getConfigManager().getString("inventories.navigator.title"));
        currentPage = 0;
        maxPage = 5;
    }

    @Override
    public void setupItems(User user, InventoryType previousInventory) {
        var cm = plugin.getServiceManager().getConfigManager();
        int size = getInventory().getSize();

        setFiller(user, size);
        setBackButton(size - 9, user, previousInventory);
        updatePage(user);
    }

    private void updatePage(User user) {
        int size = getInventory().getSize();
        setNavigationItems(user, size - 6, size - 5, size - 4);
    }

    private void setNavigationItems(User user, int backSlot, int currentSlot, int nextSlot) {
        setBackArrow(user, backSlot);
        setNextArrow(user, nextSlot);
        setCurrentArrow(user, currentSlot);
    }

    private void setBackArrow(User rawUser, int slot) {
        var cm = plugin.getServiceManager().getConfigManager();

        ItemStack item = new ItemStack(Material.PLAYER_HEAD);

        item.editMeta(SkullMeta.class, meta -> {
            meta.displayName(mm.deserialize(plugin.getServiceManager().getConfigManager().getString("items.previous_page.name")));

            PlayerProfile profile = Bukkit.createProfile(UUID.randomUUID());
            profile.setProperty(new ProfileProperty("textures", rawUser.getColor().getPageBack()));

            meta.setPlayerProfile(profile);
        });

        setItem(slot, item, ((user, event) -> {
            if (currentPage == 0) {
                user.playSound("error");
                return;
            }
            user.playSound("pling");
            currentPage -= 1;
            updatePage(user);
        }));
    }

    private void setNextArrow(User rawUser, int slot) {
        var cm = plugin.getServiceManager().getConfigManager();

        ItemStack item = new ItemStack(Material.PLAYER_HEAD);

        item.editMeta(SkullMeta.class,meta -> {
            meta.displayName(mm.deserialize(plugin.getServiceManager().getConfigManager().getString("items.next_page.name")));

            PlayerProfile profile = Bukkit.createProfile(UUID.randomUUID());
            profile.setProperty(new ProfileProperty("textures", rawUser.getColor().getPageNext()));

            meta.setPlayerProfile(profile);
        });

        setItem(slot, item, ((user, event) -> {
            if (currentPage == maxPage) {
                user.playSound("error");
                return;
            }
            user.playSound("pling");
            currentPage += 1;
            updatePage(user);
        }));
    }

    private void setCurrentArrow(User rawUser, int slot) {
        var cm = plugin.getServiceManager().getConfigManager();

        ItemStack item = new ItemStack(Material.PLAYER_HEAD);

        item.editMeta(SkullMeta.class,meta -> {
            meta.displayName(mm.deserialize(plugin.getServiceManager().getConfigManager().getString("items.current_page.name") + (currentPage + 1)));

            PlayerProfile profile = Bukkit.createProfile(UUID.randomUUID());
            profile.setProperty(new ProfileProperty("textures", rawUser.getColor().getInfoIcon()));

            meta.setPlayerProfile(profile);
        });

        setItem(slot, item, null);
    }
}
