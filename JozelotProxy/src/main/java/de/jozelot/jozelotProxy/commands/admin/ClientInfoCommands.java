package de.jozelot.jozelotProxy.commands.admin;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.util.ModInfo;
import de.jozelot.jozelotProxy.JozelotProxy;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class ClientInfoCommands implements SimpleCommand {

    private final JozelotProxy plugin;
    private final ProxyServer server;
    private final MiniMessage mm = MiniMessage.miniMessage();

    public ClientInfoCommands(JozelotProxy plugin) {
        this.plugin = plugin;
        this.server = plugin.getServer();
    }

    @Override
    public void execute(Invocation invocation) {
        CommandSource source = invocation.source();
        String[] args = invocation.arguments();
        String alias = invocation.alias();

        if (args.length == 0) {
            source.sendMessage(mm.deserialize(plugin.getLang().format("command-" + alias + "-usage", null)));
            return;
        }

        Optional<Player> target = server.getPlayer(args[0]);
        if (target.isEmpty()) {
            source.sendMessage(mm.deserialize(plugin.getLang().format("player-not-found", Map.of("player-name", args[0]))));
            return;
        }

        Player p = target.get();

        switch (alias.toLowerCase()) {
            case "client":
                String brand = p.getClientBrand() != null ? p.getClientBrand() : "Unbekannt";
                source.sendMessage(mm.deserialize(plugin.getLang().format("command-client-success", Map.of(
                        "player-name", p.getUsername(),
                        "brand", brand,
                        "version", p.getProtocolVersion().getName(),
                        "protocol", String.valueOf(p.getProtocolVersion().getProtocol())
                ))));
                break;

            case "version":
                source.sendMessage(mm.deserialize(plugin.getLang().format("command-version-success", Map.of(
                        "player-name", p.getUsername(),
                        "version", p.getProtocolVersion().getName(),
                        "protocol", String.valueOf(p.getProtocolVersion().getProtocol())
                ))));
                break;

            case "mods":
                Optional<ModInfo> modInfo = p.getModInfo();
                if (modInfo.isEmpty() || modInfo.get().getMods().isEmpty()) {
                    source.sendMessage(mm.deserialize(plugin.getLang().format("command-mods-none", Map.of("player-name", p.getUsername()))));
                } else {
                    String modList = modInfo.get().getMods().stream()
                            .map(mod -> mod.getId() + " (" + mod.getVersion() + ")")
                            .collect(Collectors.joining(", "));

                    source.sendMessage(mm.deserialize(plugin.getLang().format("command-mods-list", Map.of(
                            "player-name", p.getUsername(),
                            "count", String.valueOf(modInfo.get().getMods().size()),
                            "mods", modList
                    ))));
                }
                break;
        }
    }

    @Override
    public List<String> suggest(final Invocation invocation) {
        String[] args = invocation.arguments();
        if (args.length > 1) return List.of();

        String input = args.length == 1 ? args[0].toLowerCase() : "";
        List<String> suggestions = new ArrayList<>();

        server.getAllPlayers().stream()
                .map(Player::getUsername)
                .filter(name -> name.toLowerCase().startsWith(input))
                .forEach(suggestions::add);

        return suggestions;
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission("network.command." + invocation.alias());
    }
}