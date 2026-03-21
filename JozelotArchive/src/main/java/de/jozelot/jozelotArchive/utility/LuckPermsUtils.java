package de.jozelot.jozelotArchive.utility;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;
import org.bukkit.entity.Player;

public class LuckPermsUtils {

    public static String getPlayerRankAsString(Player player) {
        LuckPerms api = LuckPermsProvider.get();
        User user = api.getUserManager().getUser(player.getUniqueId());

        if (user == null) return "Spieler";

        String groupName = user.getPrimaryGroup();

        Group group = api.getGroupManager().getGroup(groupName);
        String displayName = (group != null && group.getDisplayName() != null)
                ? group.getDisplayName()
                : groupName;

        if (displayName == null || displayName.isEmpty()) return "Spieler";

        return displayName.substring(0, 1).toUpperCase() + displayName.substring(1).toLowerCase();
    }

}
