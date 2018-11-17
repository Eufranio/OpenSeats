package io.github.eufranio.openseats;

import com.google.inject.Inject;
import io.github.eufranio.openseats.config.Config;
import io.github.eufranio.openseats.config.MainConfig;
import io.github.eufranio.openseats.config.SitConfig;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.GameReloadEvent;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.plugin.Plugin;

import java.io.File;
import java.util.UUID;

@Plugin(
        id = "openseats",
        name = "OpenSeats",
        description = "This plugin allows users to sit on certain blocks",
        authors = {
                "Eufranio"
        }
)
public class OpenSeats {

    @Inject
    private Logger logger;

    @Inject
    @ConfigDir(sharedRoot = false)
    private File configDir;

    private Config<MainConfig> config;
    public Config<SitConfig> data;

    @Listener
    public void onServerStart(GameStartedServerEvent event) {
        this.config = new Config<>(MainConfig.class, "OpenSeats.conf", this.configDir);
        this.data = new Config<>(SitConfig.class, "SitConfig.conf", this.configDir);
        Sponge.getEventManager().registerListeners(this, new SeatListeners());
    }

    @Listener
    public void onReload(GameReloadEvent e) {
        this.config.reload();
        this.data.reload();
    }

    public MainConfig getConfig() {
        return this.config.get();
    }

    public SitConfig getData() {
        return this.data.get();
    }

    public static OpenSeats getInstance() {
        return (OpenSeats) Sponge.getPluginManager().getPlugin("openseats")
                .get()
                .getInstance()
                .get();
    }

}
