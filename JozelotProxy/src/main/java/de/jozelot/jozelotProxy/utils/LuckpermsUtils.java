package de.jozelot.jozelotProxy.utils;

import com.velocitypowered.api.proxy.Player;
import de.jozelot.jozelotProxy.JozelotProxy;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.NodeType;

public class LuckpermsUtils {

    private final JozelotProxy plugin;

    public LuckpermsUtils(JozelotProxy plugin) {
        this.plugin = plugin;
    }

    public String getPlayerPrefix(Player player) {
        if (plugin.getLuckPerms() == null) return "";

        User user = plugin.getLuckPerms().getUserManager().getUser(player.getUniqueId());
        if (user == null) return "";

        String prefix = user.getCachedData().getMetaData().getPrefix();
        return (prefix != null) ? prefix : "";
    }


    public int getWeight(Player player) {
        if (plugin.getLuckPerms() == null) return 0;

        User user = plugin.getLuckPerms().getUserManager().getUser(player.getUniqueId());
        if (user == null) return 0;

        return user.getNodes().stream()
                .filter(NodeType.INHERITANCE::matches)
                .map(NodeType.INHERITANCE::cast)
                .map(node -> plugin.getLuckPerms().getGroupManager().getGroup(node.getGroupName()))
                .filter(java.util.Objects::nonNull)
                .mapToInt(group -> group.getWeight().orElse(0))
                .max()
                .orElse(0);
    }
}
