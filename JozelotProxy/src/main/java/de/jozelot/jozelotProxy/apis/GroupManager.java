package de.jozelot.jozelotProxy.apis;

import de.jozelot.jozelotProxy.JozelotProxy;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GroupManager {
    private final JozelotProxy plugin;
    private final Map<String, Integer> serverToGroup = new HashMap<>();
    private final Map<Integer, String> groupNames = new HashMap<>();

    public GroupManager(JozelotProxy plugin) {
        this.plugin = plugin;
        load();
    }

    public void load() {
        serverToGroup.clear();
        groupNames.clear();
        plugin.getMySQLManager().loadGroups(serverToGroup, groupNames);
    }

    public int getGroupId(String serverName) {
        return serverToGroup.getOrDefault(serverName, -1);
    }

    public String getGroupName(int groupId) {
        return groupNames.getOrDefault(groupId, "Unbekannt");
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
}