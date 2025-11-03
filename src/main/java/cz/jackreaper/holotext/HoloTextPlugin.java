package cz.jackreaper.holotext;

import cz.jackreaper.holotext.command.HoloCommand;
import cz.jackreaper.holotext.hologram.HologramManager;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Main plugin entrypoint for HoloText.
 *
 * <p>Responsibilities:
 * - Manage the lifecycle (enable/disable) of the plugin.
 * - Initialize and expose the {@link cz.jackreaper.holotext.hologram.HologramManager}.
 * - Register the {@link cz.jackreaper.holotext.command.HoloCommand} executor and tab completer.
 *
 * <p>Usage: Paper/Spigot loads this class based on {@code plugin.yml}.
 */
public class HoloTextPlugin extends JavaPlugin {
    private static HoloTextPlugin instance;
    private HologramManager hologramManager;

    @Override
    public void onEnable() {
        instance = this;
        // No config defaults: data folder creation handled by HologramManager
        this.hologramManager = new HologramManager(this);
        this.hologramManager.loadAll();

        // Register command executor and tab completer
        HoloCommand holoCommand = new HoloCommand(this.hologramManager);
        if (getCommand("holo") != null) {
            getCommand("holo").setExecutor(holoCommand);
            getCommand("holo").setTabCompleter(holoCommand);
        }
        getLogger().info("HoloText enabled. Holograms loaded: " + hologramManager.size());
    }

    @Override
    public void onDisable() {
        if (hologramManager != null) {
            hologramManager.saveAll();
        }
        getLogger().info("HoloText disabled.");
    }

    /**
     * Returns the active plugin singleton instance.
     * @return active {@link HoloTextPlugin} instance
     */
    public static HoloTextPlugin getInstance() {
        return instance;
    }

    /**
     * Exposes the {@link HologramManager} for runtime and API usage.
     * @return initialized hologram manager
     */
    public HologramManager getHologramManager() {
        return hologramManager;
    }
}