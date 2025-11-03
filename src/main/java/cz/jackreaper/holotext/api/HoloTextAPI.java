package cz.jackreaper.holotext.api;

import cz.jackreaper.holotext.HoloTextPlugin;
import cz.jackreaper.holotext.hologram.Hologram;
import cz.jackreaper.holotext.hologram.HologramManager;
import org.bukkit.Location;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Public API for interacting with HoloText holograms from other plugins.
 *
 * <p>Add HoloText as a dependency in {@code plugin.yml} and call these
 * static methods to create, query, move, update, and delete holograms.
 * All operations delegate to the running plugin's {@link HologramManager}.
 */
public final class HoloTextAPI {
    private HoloTextAPI() {}

    private static HologramManager manager() {
        return HoloTextPlugin.getInstance().getHologramManager();
    }

    // Removed non-boolean overload: callers must specify staticRotation explicitly

    /**
     * Delete a hologram by name.
     * @param name hologram name
     * @return true if the hologram existed and was deleted
     */
    public static boolean delete(String name) {
        return manager().delete(name);
    }

    /**
     * Update text lines for an existing hologram.
     * @param name hologram name
     * @param lines new text lines
     * @return true if update succeeded
     */
    public static boolean updateText(String name, List<String> lines) {
        return manager().updateText(name, lines);
    }

    /**
     * Move a hologram to a new location.
     * @param name hologram name
     * @param location target world location
     * @return true if move succeeded
     */
    public static boolean moveTo(String name, Location location) {
        return manager().moveTo(name, location);
    }

    /**
     * Fetch a hologram by name.
     * @param name hologram name
     * @return optional hologram if present
     */
    public static Optional<Hologram> get(String name) {
        return manager().get(name);
    }

    /**
     * List names of all holograms.
     * @return set of hologram names
     */
    public static Set<String> names() {
        return manager().names();
    }

    /**
     * Create and spawn a new hologram.
     * @param name unique name
     * @param location spawn location
     * @param lines text lines (use {@code |} as newline when parsing yourself)
     * @param staticRotation true for fixed rotation, false for viewer-facing
     * @return false if a hologram with the same name already exists
     */
    public static boolean create(String name, Location location, List<String> lines, boolean staticRotation) {
        return manager().create(name, location, lines, staticRotation);
    }

    /**
     * Update both text and rotation flag for a hologram.
     * @param name hologram name
     * @param lines new text lines
     * @param staticRotation rotation mode
     * @return true if update succeeded
     */
    public static boolean update(String name, List<String> lines, boolean staticRotation) {
        return manager().updateStaticAndText(name, lines, staticRotation);
    }
}