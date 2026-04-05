package de.jozelot.jozelotArchive.inventory.menus.navigator.locations;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import de.jozelot.jozelotArchive.JozelotArchive;
import de.jozelot.jozelotArchive.inventory.menus.InventoryType;
import de.jozelot.jozelotArchive.inventory.menus.Menu;
import de.jozelot.jozelotArchive.location.Location;
import de.jozelot.jozelotArchive.player.archivedPlayer.ArchivedPlayer;
import de.jozelot.jozelotArchive.player.user.Sound;
import de.jozelot.jozelotArchive.player.user.User;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.*;

public class LocationOverviewMenu extends Menu {

    private int currentPage;
    private int maxPage;
    private LocationSort sort;
    private long lastClickPage = 0;
    private long lastClickSort = 0;
    private Collection<Location> locations;

    private final long PAGE_COOLDOWN = 100;
    private final long SORT_COOLDOWN = 100;

    public LocationOverviewMenu(JozelotArchive plugin, Collection<Location> locations) {
        super(plugin, calculateSize(locations), plugin.getServiceManager().getConfigManager().getString("inventories.location_overview.title"));
        currentPage = 0;
        this.locations = locations;

        int locationCount = locations.size();
        this.maxPage = Math.max(0, (locationCount - 1) / 36);
    }

    private static int calculateSize(Collection<Location> locations) {
        int locationCount = locations.size();

        int neededSlots = locationCount + 18;

        int rows = (int) Math.ceil(neededSlots / 9.0);

        return Math.min(6, Math.max(3, rows)) * 9;
    }

    @Override
    public void setupItems(User user, InventoryType previousInventory) {
        var cm = plugin.getServiceManager().getConfigManager();
        int size = getInventory().getSize();

        setFiller(user, size);
        setBackButton(size - 9, user, previousInventory);
        setNavigationItems(user, size - 6, size - 5, size - 4, size - 1);
        setLocationItems(user);
    }

    private void updatePage(User user) {
        int size = getInventory().getSize();
        setCurrentArrow(user, size - 5);
        setSortItem(user, size - 1);
        setLocationItems(user);
    }

    private void setLocationItems(User rawUser) {
        int size = getInventory().getSize();

        for (int slot = 9; slot < size - 9; slot++) {
            getInventory().setItem(slot, null);
        }

        List<Location> displayList = new ArrayList<>(this.locations);
        sortLocations(displayList, rawUser.getLocationSort());

        int itemsPerPage = size - 18;
        int start = currentPage * itemsPerPage;
        int end = Math.min(start + itemsPerPage, displayList.size());

        int slot = 9;
        for (int i = start; i < end; i++) {
            Location loc = displayList.get(i);
            ItemStack item = createLocationItem(loc);

            setItem(slot++, item, (user, event) -> {
                user.openInventory(InventoryType.LOCATION_INFO, InventoryType.LOCATION_OVERVIEW, loc);
                user.playSound(Sound.PLING);
            });
        }
    }

    private void sortLocations(List<Location> locations, LocationSort sort) {
        switch (sort) {
            case NAME -> locations.sort(Comparator.comparing(Location::getName, String.CASE_INSENSITIVE_ORDER));
            case MEMBERS -> locations.sort(Comparator.comparingInt(loc -> loc.getMembers().size()));
            case TYPE -> locations.sort(Comparator.comparing(loc -> loc.getType().name()));
            case SIZE -> locations.sort(Comparator.comparingDouble(Location::getSize).reversed());
        }
    }

    private ItemStack createLocationItem(Location loc) {
        Material mat = loc.getType().getMaterial();

        ItemStack item = new ItemStack(mat);

        item.editMeta(meta -> {
            meta.displayName(mm.deserialize("<!italic><#00A4FC>" + loc.getName()));
            meta.removeItemFlags(ItemFlag.values());

            ArchivedPlayer owner = loc.getOwner();
            String ownerName = (owner != null) ? owner.getName() : "Keiner";

            List<String> lines = List.of(
                    "",
                    "<dark_gray>» <gray>Typ: <white>" + loc.getType().getName(),
                    "<dark_gray>» <gray>Besitzer: <white>" + ownerName,
                    "<dark_gray>» <gray>Mitglieder: <white>" + loc.getMembers().size(),
                    "",
                    "<#00A4FC>Klicke für weitere Informationen"
            );

            meta.lore(lines.stream()
                    .map(line -> mm.deserialize("<!italic>" + line))
                    .toList());
        });

        return item;
    }

    private void setNavigationItems(User user, int backSlot, int currentSlot, int nextSlot, int sortSlot) {
        setBackArrow(user, backSlot);
        setNextArrow(user, nextSlot);
        setCurrentArrow(user, currentSlot);
        setSortItem(user, sortSlot);
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
            if (!(lastClickPage < System.currentTimeMillis() - PAGE_COOLDOWN)) {
                //user.playSound(Sound.ERROR);
                return;
            }
            lastClickPage = System.currentTimeMillis();
            if (currentPage == 0) {
                user.playSound(Sound.ERROR);
                return;
            }
            user.playSound(Sound.PLING);
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
            if (!(lastClickPage < System.currentTimeMillis() - PAGE_COOLDOWN)) {
                //user.playSound(Sound.ERROR);
                return;
            }
            lastClickPage = System.currentTimeMillis();
            if (currentPage == maxPage) {
                user.playSound(Sound.ERROR);
                return;
            }
            user.playSound(Sound.PLING);
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

    private void setSortItem(User rawUser, int slot) {
        var cm = plugin.getServiceManager().getConfigManager();

        ItemStack item = new ItemStack(Material.PLAYER_HEAD);
        LocationSort[] sorts = LocationSort.values();

        List<String> rawLore = new ArrayList<>();
        rawLore.add("");

        for (LocationSort sort : sorts) {
            if (sort == rawUser.getLocationSort()) {
                rawLore.add("<!italic><#00A4FC>» <#00A4FC>" + sort.getName());
                continue;
            }
            rawLore.add("<!italic><dark_gray>» <white>" + sort.getName());
        }

        item.editMeta(SkullMeta.class,meta -> {
            meta.displayName(mm.deserialize(plugin.getServiceManager().getConfigManager().getString("items.sort_button.name")));
            meta.lore(rawLore.stream().map(mm::deserialize).toList());

            PlayerProfile profile = Bukkit.createProfile(UUID.randomUUID());
            profile.setProperty(new ProfileProperty("textures", rawUser.getColor().getInfoIcon()));

            meta.setPlayerProfile(profile);
        });

        setItem(slot, item, ((user, event) -> {
            if (!(lastClickSort < System.currentTimeMillis() - SORT_COOLDOWN)) {
               // user.playSound(Sound.ERROR);
                return;
            }

            user.toggleLocationSort();
            user.playSound(Sound.PLING);
            updatePage(user);
            lastClickSort = System.currentTimeMillis();
        }));
    }
}
