package de.jozelot.jozelotProxy.apis;

import de.jozelot.jozelotProxy.JozelotProxy;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class GroupManager {
    private final JozelotProxy plugin;
    // Map: ServerName -> GroupID
    private final Map<String, Integer> serverToGroup = new HashMap<>();
    // Map: GroupID -> DisplayName
    private final Map<Integer, String> groupNames = new HashMap<>();
    // Map: GroupID -> Identifier (z.B. 1 -> "lobby")
    private final Map<Integer, String> groupIdentifiers = new HashMap<>();

    public GroupManager(JozelotProxy plugin) {
        this.plugin = plugin;
        ensureProxyGroupExists(); // Sicherstellen, dass die Proxy-Gruppe da ist
        load();
    }

    public void load() {
        serverToGroup.clear();
        groupNames.clear();
        groupIdentifiers.clear();

        plugin.getMySQLManager().loadGroups(serverToGroup, groupNames, groupIdentifiers);
    }

    private void ensureProxyGroupExists() {
        plugin.getMySQLManager().ensureProxyGroupExists();
    }

    public int getGroupId(String serverName) {
        return serverToGroup.getOrDefault(serverName, -1);
    }

    public String getGroupName(int groupId) {
        if (groupId == 0) return "Netzwerk";
        return groupNames.getOrDefault(groupId, "Unbekannt");
    }

    public String getGroupIdentifier(int groupId) {
        if (groupId == 0) return "proxy";
        return groupIdentifiers.getOrDefault(groupId, "unknown");
    }

    public List<String> getServersInGroup(int groupId) {
        return serverToGroup.entrySet().stream()
                .filter(e -> e.getValue() == groupId)
                .map(Map.Entry::getKey)
                .toList();
    }

    public boolean isTabEnabled(int groupId) {
        return plugin.getMySQLManager().isGroupTabEnabled(groupId);
    }

    public Set<String> getAllGroupIdentifiers() {
        return Set.copyOf(groupIdentifiers.values());
    }
}