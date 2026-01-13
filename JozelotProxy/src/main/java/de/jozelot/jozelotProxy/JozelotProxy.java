package de.jozelot.jozelotProxy;

import com.google.inject.Inject;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.plugin.Plugin;
import org.slf4j.Logger;

@Plugin(id = "jozelotproxy", name = "JozelotProxy", version = "1.0.0", description = "Velocity Proxy Plugin für jozelot.de - Bereitgestellt von jozelot_", url = "https://jozelot.de", authors = {"jozelot_"})
public class JozelotProxy {

    @Inject
    private Logger logger;

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
    }
}
