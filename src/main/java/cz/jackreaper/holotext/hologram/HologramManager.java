package cz.jackreaper.holotext.hologram;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Display;
import org.bukkit.entity.TextDisplay;
import org.bukkit.plugin.Plugin;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service responsible for managing hologram lifecycle and persistence.
 *
 * <p>Backed by {@code holograms.yml} in the plugin data folder, this manager
 * provides operations to create, update, move, delete, load/save, and purge
 * hologram entities (TextDisplay/ArmorStand) from worlds.
 */
public class HologramManager {
    private final Plugin plugin;
    private final Map<String, Hologram> holograms = new HashMap<>();
    private final File dataFile;
    private YamlConfiguration data;

    /**
     * Create a manager bound to a plugin instance.
     * Ensures the data folder and {@code holograms.yml} exist.
     * @param plugin owning plugin
     */
    public HologramManager(Plugin plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "holograms.yml");
        if (!plugin.getDataFolder().exists()) plugin.getDataFolder().mkdirs();
        if (!dataFile.exists()) {
            try { dataFile.createNewFile(); } catch (IOException ignored) {}
        }
        this.data = YamlConfiguration.loadConfiguration(dataFile);
    }

    /**
     * @return number of holograms currently tracked in memory
     */
    public int size() { return holograms.size(); }

    /**
     * @return immutable set of hologram names
     */
    public Set<String> names() { return Collections.unmodifiableSet(holograms.keySet()); }

    /**
     * Look up a hologram by name.
     * @param name hologram name (case-insensitive)
     * @return optional hologram if present
     */
    public Optional<Hologram> get(String name) { return Optional.ofNullable(holograms.get(name.toLowerCase(Locale.ROOT))); }

    /**
     * Create and spawn a new hologram.
     * @param name unique name
     * @param loc world location to spawn at
     * @param lines text lines to render
     * @param staticRotation true for fixed rotation (TextDisplay), false for viewer-facing (ArmorStand)
     * @return false if a hologram with the same name already exists
     */
    public boolean create(String name, Location loc, List<String> lines, boolean staticRotation) {
        String key = name.toLowerCase(Locale.ROOT);
        if (holograms.containsKey(key)) return false;
        Hologram holo = new Hologram(name, loc.clone(), new ArrayList<>(lines), staticRotation);
        holograms.put(key, holo);
        spawnOrRefresh(holo);
        saveOne(holo);
        return true;
    }

    /**
     * Delete a hologram and remove its entities.
     * @param name name of hologram
     * @return true if a hologram was found and deleted
     */
    public boolean delete(String name) {
        String key = name.toLowerCase(Locale.ROOT);
        Hologram holo = holograms.remove(key);
        if (holo == null) return false;
        removeEntitiesFor(holo);
        data.set("holograms." + key, null);
        saveDataFile();
        return true;
    }

    /**
     * Update hologram text lines and respawn entities.
     * @param name hologram name
     * @param lines new text lines
     * @return true if updated successfully
     */
    public boolean updateText(String name, List<String> lines) {
        Hologram holo = holograms.get(name.toLowerCase(Locale.ROOT));
        if (holo == null) return false;
        holo.setLines(new ArrayList<>(lines));
        spawnOrRefresh(holo);
        saveOne(holo);
        return true;
    }

    /**
     * Update static rotation and text, then respawn entities.
     * @param name hologram name
     * @param lines new text lines
     * @param staticRotation whether rotation is fixed
     * @return true if updated successfully
     */
    public boolean updateStaticAndText(String name, List<String> lines, boolean staticRotation) {
        Hologram holo = holograms.get(name.toLowerCase(Locale.ROOT));
        if (holo == null) return false;
        holo.setLines(new ArrayList<>(lines));
        holo.setStaticRotation(staticRotation);
        spawnOrRefresh(holo);
        saveOne(holo);
        return true;
    }

    /**
     * Move a hologram to a new location and respawn entities.
     * @param name hologram name
     * @param newLoc target location
     * @return true if moved successfully
     */
    public boolean moveTo(String name, Location newLoc) {
        Hologram holo = holograms.get(name.toLowerCase(Locale.ROOT));
        if (holo == null) return false;
        Hologram moved = new Hologram(holo.getName(), newLoc.clone(), holo.getLines(), holo.isStaticRotation());
        holograms.put(name.toLowerCase(Locale.ROOT), moved);
        spawnOrRefresh(moved);
        saveOne(moved);
        return true;
    }

    /**
     * Load holograms from {@code holograms.yml} and spawn them into the world.
     * Warns and skips entries with missing worlds or required fields.
     */
    public void loadAll() {
        holograms.clear();
        if (!data.contains("holograms")) return;
        for (String key : Objects.requireNonNull(data.getConfigurationSection("holograms")).getKeys(false)) {
            String path = "holograms." + key + ".";
            String worldName = data.getString(path + "world");
            World world = worldName != null ? plugin.getServer().getWorld(worldName) : null;
            if (world == null) {
                plugin.getLogger().warning("Skipping hologram '" + key + "' due to missing world '" + worldName + "'.");
                continue;
            }
            double x = data.getDouble(path + "x");
            double y = data.getDouble(path + "y");
            double z = data.getDouble(path + "z");
            float yaw = (float) data.getDouble(path + "yaw", 0.0);
            float pitch = (float) data.getDouble(path + "pitch", 0.0);
            List<String> lines = data.getStringList(path + "lines");
            if (!data.contains(path + "static")) {
                plugin.getLogger().warning("Skipping hologram '" + key + "' because 'static' flag is missing. Update it via command to set static/dynamic.");
                continue;
            }
            boolean staticRotation = data.getBoolean(path + "static");
            Location loc = new Location(world, x, y, z, yaw, pitch);
            Hologram holo = new Hologram(key, loc, lines, staticRotation);
            holograms.put(key.toLowerCase(Locale.ROOT), holo);
            spawnOrRefresh(holo);
        }
    }

    /**
     * Persist all holograms to {@code holograms.yml}.
     */
    public void saveAll() {
        data.set("holograms", null);
        for (Hologram holo : holograms.values()) {
            saveOne(holo);
        }
        saveDataFile();
    }

    private void saveOne(Hologram holo) {
        String key = holo.getName().toLowerCase(Locale.ROOT);
        String path = "holograms." + key + ".";
        Location loc = holo.getLocation();
        data.set(path + "world", loc.getWorld().getName());
        data.set(path + "x", loc.getX());
        data.set(path + "y", loc.getY());
        data.set(path + "z", loc.getZ());
        data.set(path + "yaw", loc.getYaw());
        data.set(path + "pitch", loc.getPitch());
        data.set(path + "lines", holo.getLines());
        data.set(path + "static", holo.isStaticRotation());
        saveDataFile();
    }

    private void saveDataFile() {
        try {
            data.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save holograms.yml: " + e.getMessage());
        }
    }

    private void spawnOrRefresh(Hologram holo) {
        // Remove any existing displays for this hologram and respawn cleanly
        removeEntitiesFor(holo);

        World world = holo.getLocation().getWorld();
        if (world == null) return;

        // Spawn one invisible, marker ArmorStand per line, stacked with small spacing.
        List<String> lines = holo.getLines();
        Location base = holo.getLocation().clone();
        double spacing = 0.25; // vertical distance between lines
        // Place lines stacked UPWARD from the base location (base is bottom)
        for (int i = 0; i < lines.size(); i++) {
            String raw = lines.get(i);
            Location lineLoc = base.clone().add(0, (lines.size() - 1 - i) * spacing, 0);

            if (holo.isStaticRotation()) {
                // Use TextDisplay with FIXED billboard and set rotation to location yaw/pitch
                String colored = ChatColor.translateAlternateColorCodes('&', raw);
                world.spawn(lineLoc, TextDisplay.class, td -> {
                    td.setBillboard(Display.Billboard.FIXED);
                    td.setRotation(base.getYaw(), base.getPitch());
                    td.setText(colored);
                    td.addScoreboardTag(tagFor(holo.getName()));
                    td.setPersistent(true);
                });
            } else {
                // Default: ArmorStand nameplates which always face the viewer
                String colored = ChatColor.translateAlternateColorCodes('&', raw);
                world.spawn(lineLoc, ArmorStand.class, as -> {
                    as.setInvisible(true);
                    as.setMarker(true);
                    as.setGravity(false);
                    as.setCustomNameVisible(true);
                    as.setCustomName(colored);
                    as.addScoreboardTag(tagFor(holo.getName()));
                    as.setPersistent(true);
                });
            }
        }
    }

    private void removeEntitiesFor(Hologram holo) {
        World world = holo.getLocation().getWorld();
        if (world == null) return;
        String tag = tagFor(holo.getName());
        for (TextDisplay td : world.getEntitiesByClass(TextDisplay.class)) {
            if (td.getScoreboardTags().contains(tag)) {
                td.remove();
            }
        }
        for (ArmorStand as : world.getEntitiesByClass(ArmorStand.class)) {
            if (as.getScoreboardTags().contains(tag)) {
                as.remove();
            }
        }
    }

    private String tagFor(String name) {
        return "holotext:" + name.toLowerCase(Locale.ROOT);
    }

    /**
     * Purge all TextDisplay/ArmorStand entities across all worlds that have any tag
     * starting with the HoloText prefix, regardless of whether a matching hologram
     * exists in memory.
     * @return number of entities removed
     */
    public int purgeAllTagged() {
        int removed = 0;
        for (World world : plugin.getServer().getWorlds()) {
            for (TextDisplay td : world.getEntitiesByClass(TextDisplay.class)) {
                boolean hasHoloTag = td.getScoreboardTags().stream().anyMatch(t -> t.startsWith("holotext:"));
                if (hasHoloTag) {
                    td.remove();
                    removed++;
                }
            }
            for (ArmorStand as : world.getEntitiesByClass(ArmorStand.class)) {
                boolean hasHoloTag = as.getScoreboardTags().stream().anyMatch(t -> t.startsWith("holotext:"));
                if (hasHoloTag) {
                    as.remove();
                    removed++;
                }
            }
        }
        return removed;
    }

    /**
     * Purge all HoloText-tagged entities in the given world.
     * @param world target world
     * @return number of entities removed
     */
    public int purgeTaggedInWorld(World world) {
        int removed = 0;
        for (TextDisplay td : world.getEntitiesByClass(TextDisplay.class)) {
            boolean hasHoloTag = td.getScoreboardTags().stream().anyMatch(t -> t.startsWith("holotext:"));
            if (hasHoloTag) {
                td.remove();
                removed++;
            }
        }
        for (ArmorStand as : world.getEntitiesByClass(ArmorStand.class)) {
            boolean hasHoloTag = as.getScoreboardTags().stream().anyMatch(t -> t.startsWith("holotext:"));
            if (hasHoloTag) {
                as.remove();
                removed++;
            }
        }
        return removed;
    }

    /**
     * Purge HoloText-tagged entities within a radius around a center location.
     * @param center center location (world and coordinates)
     * @param radius radius in blocks
     * @return number of entities removed
     */
    public int purgeTaggedWithinRadius(Location center, double radius) {
        if (center == null || center.getWorld() == null || radius <= 0) return 0;
        World world = center.getWorld();
        int removed = 0;
        for (Entity e : world.getNearbyEntities(center, radius, radius, radius)) {
            boolean hasHoloTag = e.getScoreboardTags().stream().anyMatch(t -> t.startsWith("holotext:"));
            if (!hasHoloTag) continue;
            if (e instanceof TextDisplay || e instanceof ArmorStand) {
                e.remove();
                removed++;
            }
        }
        return removed;
    }

    /**
     * Despawn all hologram entities currently in the world, without altering memory or disk.
     * @return number of holograms processed
     */
    public int despawnAll() {
        int count = 0;
        for (Hologram holo : holograms.values()) {
            removeEntitiesFor(holo);
            count++;
        }
        return count;
    }

    /**
     * Reload {@code holograms.yml} from disk and spawn holograms defined therein.
     * Clears current in-memory holograms map and respawns from config.
     * @return number of holograms loaded after reset
     */
    public int resetFromConfig() {
        // First purge all tagged entities across worlds to remove orphans
        purgeAllTagged();
        // Also despawn known holograms (redundant with purge, but safe if tags changed)
        despawnAll();
        // Reload YAML from disk to pick up any external edits
        this.data = YamlConfiguration.loadConfiguration(dataFile);
        // Rebuild in-memory state and respawn
        loadAll();
        return size();
    }

    // No Kyori Adventure: keep plain strings and colorize with Bukkit ChatColor
    // Each line becomes an ArmorStand custom name, so no multi-line component needed
    // (method removed)

    /**
     * Parse a raw text argument into lines, splitting on {@code |} and trimming.
     * Empty lines are discarded.
     * @param raw raw text argument
     * @return list of non-empty, trimmed lines
     */
    public static List<String> parseTextArg(String raw) {
        // Split by '|' to allow multi-line input and drop empty lines
        if (raw == null) return Collections.emptyList();
        return Arrays.stream(raw.split("\\|"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }
}