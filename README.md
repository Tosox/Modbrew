# Modbrew

A declarative mod installer for PC games. You write a `modbrew.yaml` describing what to download and where to put it — Modbrew handles the rest.

## 🍺 How it works

Modbrew reads a `modbrew.yaml` file (the *brewfile*) authored by a modlist maintainer. It downloads each mod, extracts it, copies files to the right locations, and sets up a Mod Organizer 2 profile — all in one click.

Users never touch the YAML. Maintainers write it once; everyone brews from it.

## 📋 Requirements

- [Mod Organizer 2](https://github.com/ModOrganizer2/modorganizer) installed and launched at least once
- Modbrew placed in a `.Modbrew/` subfolder inside the MO2 directory
- A `data/modbrew.yaml` brewfile alongside the installer

## 🚀 Getting started

1. Create a `.Modbrew/` folder inside your MO2 directory (next to `ModOrganizer.exe`) and place `Modbrew.exe` and the accompanying `data/`, `locales/`, and `resources/` folders inside it.
2. Run `Modbrew.exe`.
3. Click **Brew**.

To reinstall everything from scratch, tick **Full brew** before clicking Brew.

## 📝 Writing a brewfile

The brewfile is `data/modbrew.yaml`. It is human-readable, Git-friendly, and maintained by the modlist author.

```yaml
profileName: "My Modlist"
shortcutName: "My Game — Modded"

mods:
  - id: "mod-config-menu"
    name: "Mod Configuration Menu"
    url: "https://www.moddb.com/..."
    hash: "abc123"          # optional SHA-256, validated on download
    recipe:
      - "gamedata"          # copy the gamedata/ folder from the archive

patches:
  - id: "my-patch"
    name: "Compatibility Patch"
    url: "https://github.com/..."
    recipe:
      - "bin"               # patches extract directly to the game root
      - "db"

separators:
  - id: "frameworks-sep"
    name: "Frameworks"      # creates a visual separator in MO2
```

### 🔑 Entry fields

| Field | Required | Description |
|---|---|---|
| `id` | ✓ | Unique identifier used for progress tracking and hash storage |
| `name` | ✓ | Display name shown in the installer UI |
| `url` | ✓ | Direct download URL or ModDB addon page URL |
| `hash` | — | Expected SHA-256 of the archive. Modbrew warns on mismatch and can update it automatically. |
| `recipe` | — | List of paths inside the extracted archive to copy to the destination |

### 📦 Recipe syntax

Each `recipe` entry is a path relative to the extracted archive root.

```yaml
recipe:
  - "gamedata"                  # copy the gamedata/ folder as-is
  - "MyMod-main/gamedata"       # copy a nested folder
  - "config/settings.ltx -> gamedata/config/settings.ltx"  # explicit destination
  - "bin/engine.exe -> bin/"    # copy a file into a folder
```

The `->` operator maps a source path to a custom destination. Without it, the entry is copied to the mod folder root.

## ⚙️ User configuration

`user_config.yaml` (created next to the installer on first run) controls personal preferences:

```yaml
language: en-US
logLevel: INFO
logRetentionCount: 5
archiveCleanupStrategy: KEEP_ALL   # KEEP_ALL | KEEP_LATEST_ONLY | DELETE_ALL
```

## 🗂️ Directory layout

```
ModOrganizer.exe          ← MO2 lives here
.Modbrew/
├── Modbrew.exe
├── data/
│   ├── modbrew.yaml      ← the brewfile
│   └── assets/
│       ├── icon.ico
│       └── splash.png
├── locales/
├── resources/
├── logs/
└── user_config.yaml
```

## 📄 License

Modbrew is licensed under [CC BY-NC-ND 4.0](https://creativecommons.org/licenses/by-nc-nd/4.0/) — free to use and share with attribution, but not for commercial purposes or redistribution of modified versions.
