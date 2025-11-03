package cz.jackreaper.holotext.hologram;

import org.bukkit.Location;

import java.util.List;

/**
 * A hologram definition consisting of a name, location, text lines,
 * and a static rotation flag.
 *
 * <p>The location is treated as immutable for identity; updates use
 * {@link cz.jackreaper.holotext.hologram.HologramManager#moveTo(String, org.bukkit.Location)}
 * to persist and re-spawn entities. Text lines support color codes and are
 * rendered using TextDisplay and ArmorStand entities.
 */
public class Hologram {
    private final String name;
    private final Location location;
    private List<String> lines;
    private boolean staticRotation;

    /**
     * Create a hologram model.
     * @param name unique hologram name (case-insensitive key)
     * @param location world coordinates (yaw/pitch optional)
     * @param lines text lines; {@code &} color codes supported
     * @param staticRotation true to keep display rotation fixed
     */
    public Hologram(String name, Location location, List<String> lines, boolean staticRotation) {
        this.name = name;
        this.location = location;
        this.lines = lines;
        this.staticRotation = staticRotation;
    }

    /**
     * @return configured hologram name
     */
    public String getName() {
        return name;
    }

    /**
     * @return world location for the hologram
     */
    public Location getLocation() {
        return location;
    }

    /**
     * @return current text lines for this hologram
     */
    public List<String> getLines() {
        return lines;
    }

    /**
     * Replace text lines.
     * @param lines new lines to render
     */
    public void setLines(List<String> lines) {
        this.lines = lines;
    }

    /**
     * @return whether display rotation is fixed
     */
    public boolean isStaticRotation() {
        return staticRotation;
    }

    /**
     * Update static rotation.
     * @param staticRotation true for fixed rotation, false for dynamic
     */
    public void setStaticRotation(boolean staticRotation) {
        this.staticRotation = staticRotation;
    }
}