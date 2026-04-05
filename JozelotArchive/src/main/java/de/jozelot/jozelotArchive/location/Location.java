package de.jozelot.jozelotArchive.location;

import de.jozelot.jozelotArchive.player.archivedPlayer.ArchivedPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

public class Location {

    private LocationType type;
    private String name;
    private String description;
    private ArchivedPlayer owner;
    private Set<ArchivedPlayer> members;
    private LocationArea area;

    private int id;

    protected Location(int id, @NotNull LocationType type, @NotNull String name, String description, ArchivedPlayer owner, Set<ArchivedPlayer> members, @NotNull LocationArea area) {
        this.id = id;
        this.type = type;
        this.name = name;
        this.description = description;
        this.owner = owner;
        this.area = area;

        this.members = (members == null) ? new HashSet<>() : new HashSet<>(members);
    }

    public LocationType getType() {
        return type;
    }

    public void setType(LocationType type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ArchivedPlayer getOwner() {
        return owner;
    }

    public void setOwner(ArchivedPlayer owner) {
        this.owner = owner;
    }

    public Set<ArchivedPlayer> getMembers() {
        return members;
    }

    public boolean addMember(ArchivedPlayer member) {
        return members.add(member);
    }

    public boolean removeMember(ArchivedPlayer member) {
        return members.remove(member);
    }

    public boolean isMember(ArchivedPlayer member) {
        return members.contains(member);
    }

    public LocationArea getArea() {
        return area;
    }

    public void setArea(LocationArea area) {
        this.area = area;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {this.id = id; }

    public double getSize() {
        if (area == null) return 0;
        return area.getVolume();
    }
}
