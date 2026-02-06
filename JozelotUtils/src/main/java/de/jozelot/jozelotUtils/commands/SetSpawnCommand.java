package de.jozelot.jozelotUtils.commands;

import de.jozelot.jozelotUtils.JozelotUtils;
import de.jozelot.jozelotUtils.storage.ConfigManager;
import de.jozelot.jozelotUtils.storage.LangManager;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SetSpawnCommand extends Command {

    private final JozelotUtils plugin;
    private final ConfigManager config;
    private final LangManager lang;
    private final MiniMessage mm = MiniMessage.miniMessage();

    public SetSpawnCommand(JozelotUtils plugin) {
        super("setspawn", "Setze den Spawn", "/setspawn [x y z] [yaw pitch]", new ArrayList<>());
        this.setPermission("network.utils.command.setspawn");
        this.plugin = plugin;
        this.config = plugin.getConfigManager();
        this.lang = plugin.getLang();
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(mm.deserialize(lang.format("only-player", null)));
            return true;
        }

        if (!config.isSpawnCommand()) {
            sender.sendMessage(mm.deserialize(lang.format("blocked-command", Map.of("command", "/setspawn"))));
            if (sender instanceof Player) {
                String soundPath = lang.get("sounds.error");
                if (!soundPath.isEmpty()) {
                    try {
                        Sound successSound = Sound.sound(
                                Key.key(soundPath),
                                Sound.Source.UI,
                                1.0f,
                                1.0f
                        );
                        sender.playSound(successSound, Sound.Emitter.self());
                    } catch (Exception e) {
                    }
                }
            }
            return true;
        }

        if (!player.hasPermission(this.getPermission())) {
            sender.sendMessage(mm.deserialize(lang.format("no-permission", null)));
            if (sender instanceof Player) {
                String soundPath = lang.get("sounds.error");
                if (!soundPath.isEmpty()) {
                    try {
                        Sound successSound = Sound.sound(
                                Key.key(soundPath),
                                Sound.Source.UI,
                                1.0f,
                                1.0f
                        );
                        sender.playSound(successSound, Sound.Emitter.self());
                    } catch (Exception e) {
                    }
                }
            }
            return true;
        }

        Location newSpawn;

        if (args.length == 0) {
            newSpawn = player.getLocation();
        }
        else if (args.length == 3 || args.length == 5) {
            try {
                double x = Double.parseDouble(args[0]);
                double y = Double.parseDouble(args[1]);
                double z = Double.parseDouble(args[2]);
                float yaw = 0;
                float pitch = 0;

                if (args.length == 5) {
                    yaw = Float.parseFloat(args[3]);
                    pitch = Float.parseFloat(args[4]);
                }

                newSpawn = new Location(player.getWorld(), x, y, z, yaw, pitch);
            } catch (NumberFormatException e) {
                player.sendMessage(mm.deserialize(lang.format("command-setspawn-error", null)));
                if (sender instanceof Player) {
                    String soundPath = lang.get("sounds.error");
                    if (!soundPath.isEmpty()) {
                        try {
                            Sound successSound = Sound.sound(
                                    Key.key(soundPath),
                                    Sound.Source.UI,
                                    1.0f,
                                    1.0f
                            );
                            sender.playSound(successSound, Sound.Emitter.self());
                        } catch (Exception es) {
                        }
                    }
                }
                return true;
            }
        } else {
            player.sendMessage(mm.deserialize(lang.format("command-setspawn-usage", null)));
            if (sender instanceof Player) {
                String soundPath = lang.get("sounds.error");
                if (!soundPath.isEmpty()) {
                    try {
                        Sound successSound = Sound.sound(
                                Key.key(soundPath),
                                Sound.Source.UI,
                                1.0f,
                                1.0f
                        );
                        sender.playSound(successSound, Sound.Emitter.self());
                    } catch (Exception e) {
                    }
                }
            }
            return true;
        }

        config.setSpawnLocation(newSpawn);
        player.sendMessage(mm.deserialize(lang.format("command-setspawn-success", Map.of(
                "x", String.format("%.2f", newSpawn.getX()),
                "y", String.format("%.2f", newSpawn.getY()),
                "z", String.format("%.2f", newSpawn.getZ())
        ))));

        if (sender instanceof Player) {
            String soundPath = lang.get("sounds.success");
            if (!soundPath.isEmpty()) {
                try {
                    Sound successSound = Sound.sound(
                            Key.key(soundPath),
                            Sound.Source.UI,
                            1.0f,
                            1.0f
                    );
                    sender.playSound(successSound, Sound.Emitter.self());
                } catch (Exception e) {
                }
            }
        }

        return true;
    }

    @Override
    public @NotNull List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args) throws IllegalArgumentException {
        if (!(sender instanceof Player player)) return new ArrayList<>();

        List<String> list = new ArrayList<>();
        if (args.length == 1) list.add(String.valueOf(player.getLocation().getBlockX()));
        if (args.length == 2) list.add(String.valueOf(player.getLocation().getBlockY()));
        if (args.length == 3) list.add(String.valueOf(player.getLocation().getBlockX()));

        return list;
    }
}