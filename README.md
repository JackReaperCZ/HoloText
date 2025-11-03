HoloText

Floating text (hologram) plugin for Paper/Spigot. Create, update, move, list, reset, and purge holograms. Includes a simple Java API for other plugins.

Requirements
- Server: `Paper 1.21.4` (or compatible Spigot/Paper versions)
- Permission: `holotext.admin` (defaults to op)

Installation
- Download `HoloText-1.0.0.jar` from your build output.
- Place the jar in your server `plugins/` folder.
- Start or restart the server. You should see `HoloText enabled` in the console.

Usage (Commands)
- Base command: `/holo`
- Sender-aware help: players see player-usable forms; console sees console-only forms.

- Create
  - Player: `/holo create <static:true|false> <name> <text with '|' as newline>`
  - Console: `holo create <static:true|false> <name> <x> <y> <z> <world> <text with '|' as newline>`
- Delete: `/holo delete <name>` (works for both)
- Update text and rotation: `/holo update <name> <static:true|false> <text with '|' as newline>`
- Move
  - Player: `/holo move <name>` moves to your current location
  - Console: `holo move <name> <x> <y> <z> <world>`
- List: `/holo list` shows all names
- Reset: `/holo reset` reloads `holograms.yml` and respawns entries
- Purge (tagged entity cleanup)
  - Player: `/holo purge <radius>` around your location
  - Console: `holo purge <world>` removes all tagged entities in a world

Text Formatting
- Use the `|` character to split lines in command arguments.
- Color codes supported via legacy formatting (§) if allowed by server, or by MiniMessage when using TextDisplay.

Permissions
- `holotext.admin`: required for all management commands (defaults to op).

Persistence
- Holograms are stored in `plugins/HoloText/holograms.yml`.
- Commands automatically save after create/update/move/delete.
- `reset` will reload from disk and respawn entries, clearing the in-memory map.

Management Tips
- Backups: keep a copy of `holograms.yml` before bulk changes.
- Purge safety: use player `purge <radius>` for targeted cleanup; console `purge <world>` for a full world cleanup.
- Despawn-all (without data changes): use `/holo reset` to reload configuration; or programmatically call `HologramManager#despawnAll()` if extending.

Java API (for other plugins)
- Add HoloText as a dependency in your plugin’s `plugin.yml`.
- Static methods in `cz.jackreaper.holotext.api.HoloTextAPI`:
  - `create(name, location, lines, staticRotation)`
  - `update(name, lines, staticRotation)`
  - `updateText(name, lines)`
  - `moveTo(name, location)`
  - `delete(name)`
  - `get(name)` → `Optional<Hologram>`
  - `names()` → `Set<String>`

Building From Source
- Requires Java 21 and Maven.
- Run `./build.ps1` (Windows PowerShell) or `mvn -q -DskipTests package`.
- Output jar: `target/HoloText-1.0.0.jar` and optionally copied to your server `plugins/` folder by the provided script.

Troubleshooting
- Ensure the world name exists when creating/moving from console.
- If holograms don’t appear, check for errors related to `TextDisplay` or `ArmorStand` in your server log.
- If you upgraded Paper, review deprecation warnings and recompile.

License
- MIT Licence 2025
