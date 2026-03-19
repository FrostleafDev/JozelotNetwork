package de.jozelot.jozelotArchive.storage;

import de.jozelot.jozelotArchive.JozelotArchive;

import java.io.IOException;
import java.nio.file.*;

public class FileSystemManager {

    private final JozelotArchive plugin;

    public FileSystemManager(JozelotArchive plugin) {
        this.plugin = plugin;
    }

    public void archivePlayerFiles() {
        Path sourceDir = Paths.get("world/playerdata");
        Path targetDir = Paths.get("world/playerdata_old");

        if (Files.exists(targetDir)) {
            return;
        }

        try {
            Files.createDirectories(targetDir);

            try (DirectoryStream<Path> stream = Files.newDirectoryStream(sourceDir)) {
                for (Path file : stream) {
                    Files.copy(file, targetDir.resolve(file.getFileName()));
                }
            }

            try (DirectoryStream<Path> stream = Files.newDirectoryStream(sourceDir)) {
                for (Path file : stream) {
                    Files.delete(file);
                }
            }

            plugin.getLogger().info("Playerdata erfolgreich nach 'playerdata_old' verschoben.");

        } catch (IOException e) {
            plugin.getLogger().severe("Fehler beim Archivieren: " + e.getMessage());
        }
    }
}
