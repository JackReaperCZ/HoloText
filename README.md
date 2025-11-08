<p align="center">
  <img src="icon.png" width="400" alt="HoloText icon" />
</p>

# HoloText

Floating text (hologram) plugin for Paper/Spigot. Create, update, move, list, reset, and purge holograms. Ships with a small Java API so other plugins can manage holograms easily.

> Current version: `1.0.1` • Requires `Java 21` • Tested on `Paper 1.21.4`

## Features
- Simple commands to create, update, move, list, reset, and purge holograms
- Player- and console-aware command variants
- Text lines with `|` separators; supports legacy color codes (§)
- Persistent storage in `plugins/HoloText/holograms.yml`
- Small API for integrations (`HoloTextAPI`)

## Requirements
- Server: `Paper 1.21.4` (or compatible Spigot/Paper versions)
- Permission: `holotext.admin` (defaults to `op`)

## Installation
- Download `HoloText-1.0.1.jar` from your build output or releases.
- Place the jar in your server `plugins/` folder.
- Start or restart the server; the console will log `HoloText enabled` and the count of loaded holograms.

## Usage
- Base command: `/holo`
- Sender-aware help: players see player-usable forms; console sees console-only forms.

### Commands
- Create
  - Player: `/holo create <static:true|false> <name> <text with '|' as newline>`
  - Console: `holo create <static:true|false> <name> <x> <y> <z> <world> <text with '|' as newline>`
- Delete: `/holo delete <name>`
- Update text & rotation: `/holo update <name> <static:true|false> <text with '|' as newline>`
- Move
  - Player: `/holo move <name>` (moves to your current location)
  - Console: `holo move <name> <x> <y> <z> <world>`
- List: `/holo list`
- Reset: `/holo reset` (reloads `holograms.yml` and respawns entries)
- Purge (cleanup tagged entities)
  - Player: `/holo purge <radius>` (around your location)
  - Console: `holo purge <world>` (entire world)

### Tips
- Use the `|` character to split lines in command arguments.
- Prefer player `purge <radius>` for targeted cleanup; reserve console `purge <world>` for full-world cleanup.
- Back up `plugins/HoloText/holograms.yml` before bulk changes.

## Java API
Add HoloText as a dependency in your plugin’s `plugin.yml`, then call these static methods:

```java
import cz.jackreaper.holotext.api.HoloTextAPI;
import org.bukkit.Location;

// Create a hologram
boolean created = HoloTextAPI.create("welcome", someLocation, List.of("Hello", "World"), true);

// Update text / rotation
boolean updated = HoloTextAPI.update("welcome", List.of("New", "Text"), false);

// Move / delete
HoloTextAPI.moveTo("welcome", anotherLocation);
HoloTextAPI.delete("welcome");

// Query
HoloTextAPI.get("welcome").ifPresent(holo -> {
    // do something with the hologram
});

// List names
Set<String> names = HoloTextAPI.names();
```

## Persistence
- Holograms are stored in `plugins/HoloText/holograms.yml`.
- Commands automatically save after create/update/move/delete.
- `reset` will reload from disk and respawn entries, clearing the in-memory map.

## Building From Source
- Requires `Java 21` and `Maven`.
- Windows PowerShell: run `./build.ps1`.
- Maven: run `mvn -q -DskipTests package`.
- Output jar: `target/HoloText-1.0.1.jar` (the build script also copies it to your server `plugins/` folder).

## Troubleshooting
- Ensure the world name exists when creating/moving from console.
- If holograms don’t appear, check for errors related to `TextDisplay` or `ArmorStand` in your server log.
- If you upgraded Paper, review deprecation warnings and recompile.

## License
- MIT Licence 2025
