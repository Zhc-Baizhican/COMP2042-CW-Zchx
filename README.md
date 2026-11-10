# COMP2042 Coursework — Snake (JavaFX)

**Module:** COMP2042 Developing Maintainable Software
**Student:** Zhou Chenhaoxi
**Repository:** https://github.com/Zhc-Baizhican/COMP2042-CW-Zchx

A maintenance-and-extension coursework based on the provided JavaFX Snake implementation.

---

## 1. GitHub Repository

https://github.com/Zhc-Baizhican/COMP2042-CW-Zchx

> This repository is private. Access has been granted to `kooitt` for assessment.

---

## 2. Compilation Instructions

### Requirements

| Tool | Version |
|---|---|
| JDK | 17 |
| Maven | 3.9+ (a wrapper is included, no separate install needed) |
| JavaFX | 21.0.13 (downloaded automatically by Maven) |

### Run with the Maven wrapper

```powershell
cd CW2026-master
.\mvnw.cmd clean javafx:run
```

### Run from IntelliJ IDEA

1. `File → Open` and select `CW2026-master/pom.xml`, choose *Open as Project*
2. Set *Project SDK* to **JDK 17**
3. Let Maven import finish, then run `com.comp2042.Main`

### Build a runnable jar

```powershell
.\mvnw.cmd clean package
```

### Run the tests

```powershell
.\mvnw.cmd test
```

The suite uses JUnit 5 (Jupiter) and covers the board configuration, the
difficulty presets, scoring and the win condition — 12 tests in total.

---

## 3. Implemented and Working Properly

- Arrow-key controlled snake with self-collision and wall-collision detection
- Five coin types with distinct effects: normal, gold (+15 points), silver (adds obstacles), blue (fearless of borders), rainbow (speed-up)
- Obstacles that the snake must avoid
- Score HUD pill in the top bar, updated on every coin
- Coin sound effect played on pickup
- Pause (`P`) with a PAUSED overlay, restart (`TAB`), fullscreen toggle (`+` / `-`), back to main menu (`ESC`)
- Game over and level complete panels showing the final score and the restart keys
- Main menu with logo, Start, difficulty selector (Normal / Hard / Easy) and Quit
- Settings screen: sound on/off, volume slider, five snake colour presets, a day / night theme, three window sizes and rebindable keys, saved to `~/.comp2042/settings.properties` and restored on the next launch
- Night theme recolours the menu, the board and the obstacles, so obstacles stay visible
- Every control is rebindable: click a key in the settings screen and press the new one. A key can only drive one action, and clashes are reported instead of silently overwriting
- On-screen hints (main menu, pause, game over) are generated from the current bindings, so they follow a rebind
- Unit tests (JUnit 5, `src/test/java`) — 25 tests covering `Game`, `Direction` and `KeyBindings`, run with `.\mvnw.cmd test`

---

## 4. Implemented but Not Working Properly

<!-- TODO -->

---

## 5. Features Not Implemented

<!-- TODO -->

---

## 6. New Java Classes

All under `src/main/java/com/comp2042/`.

| Class | Purpose |
|---|---|
| `Launcher` | Entry point for the packaged jar; launches `Main` without inheriting `Application`, which JavaFX requires when the modules are on the class path |
| `Settings` | Shared settings model: sound, volume, snake skin, and fields reserved for the theme and key-binding screens |
| `SettingsStore` | Loads and saves `Settings` as `~/.comp2042/settings.properties`, falling back to defaults on any read error |
| `SnakeSkin` | Colour presets for the snake sprites, implemented as `ColorAdjust` effects |
| `AudioService` | Loads the pickup sound once and plays it according to the sound settings |
| `SettingsController` | Controller for the settings screen |
| `ThemeManager` | Applies the day / night theme by toggling a style class on a scene root |
| `Direction` | The four directions the snake travels, replacing raw character and key codes |
| `Action` | Every keyboard action with its display name, default key and direction |
| `KeyBindings` | The current key for each action, with reverse lookup for clash detection |

---

## 7. Modified Java Classes

| File | Change | Reason |
|---|---|---|
| `pom.xml` | `maven.compiler.release` 21 → 17; JavaFX 25.0.1 → 21.0.13 | The original values cannot be built with the installed JDK 17, and JavaFX 25 requires JDK 23 or newer |
| `Coin.java` | Spawn position now comes from a precomputed list of free cells | The original retry loop was unbounded and could freeze the game once the board was nearly full |
| `Obstacle.java` | Placement restricted to columns `[0, width - 1]`; free-cell list with shuffle replaces the retry loop; hard cap of 50 kept | The original `nextInt(x) + 1` could place an obstacle one cell outside the board, and the retry loop could spin forever |
| `GameController.java` | Coin selection rewritten as a `switch` with distinct branches; coin sound wired up; unused field and import removed; dead scoring helper deleted; redraw condition simplified; obstacle shrink now counts only newly added cells | `ran == 1` was tested twice, so the rainbow coin was unreachable; the silver coin required an empty board and never appeared; the imported `MediaPlayer` was never used, so no sound was ever played; `calculateBonusScore` and `unusedObstacleCount` were never called |
| `Game.java` | Difficulty handled by a `switch` with a `default` branch; duplicated `powerUp` assignment removed; win check uses `>=`; `decMaxScore` now subtracts the value it is given | An unrecognised difficulty left `speed` at 0, which froze the animation; the win check could be skipped, and the previous `num - 1` arithmetic double-counted obstacles |
| `GameView.java` | Fullscreen scale computed per axis and applied once; `fullScale * fullScale` corrected in head and body drawing; the two status labels were replaced by a single overlay with a title and a detail line; the stylesheet is now attached once at scene level | Sprite sizes were squared when scaled up, and the width scale was computed from the screen height; the old labels gave no restart hint and loaded the stylesheet three times by relative path |
| `MainMenuController.java` | Added difficulty cycling, a Quit handler and a `@FXML` reference to the difficulty button; the chosen difficulty is passed to the game | The difficulty was a hard-coded constant, there was no way to quit, and the layout no longer uses the removed status labels |
| `styling.css` | Deduplicated the button rules into `.menuButton` / `.primaryButton`, added hover and pressed states, restyled the score label as a HUD pill, added the status overlay rules and the settings-screen rules | The original file repeated the same four button properties twice and had no hover feedback; the in-game text had no backing panel and clashed with inline styles in `GameView` |
| `Snake.java` | Applies the chosen `SnakeSkin` to all four sprites; deleted the unused `headColor` / `bodyColor` fields and their getters | Those colour fields were never read by anything, because the snake is drawn from images |
| `Main.java` | Loads the saved settings before building the view manager | Settings are needed before the first snake is created |
| `ViewManager.java` | Adds the settings window, modal on the main menu | The main menu needed a way to reach the settings screen |
| `MainMenu.fxml` / `MainMenuController.java` | Added a Settings button and its handler | New entry point for the settings screen |
| `GameController.java` | Replaced the inline `MediaPlayer` and its loader with `AudioService` | Sound, volume and mute are now owned by one class instead of the game loop |
| `Game.java` | Board colour and cell size now come from the settings; `WINDOW_SCALES` maps the three window sizes onto the cell size | The background and the board size were fixed values, so the theme and window settings had nothing to change |
| `Obstacle.java` | Obstacle colour depends on the theme | A black obstacle would be invisible on the night background |
| `MainMenuController.java` | Applies the theme to the game scene | The game window would otherwise stay light while the menu turned dark |
| `Settings.fxml` / `SettingsController.java` | Added the night-mode toggle and the three window-size buttons; theme changes repaint the settings window and the menu immediately | New controls for the theme and display settings |
| `GameTest.java` | Resets the settings before each test and covers window scaling and the night board colour | `Settings` is a process-wide singleton, so tests had to start from a known state |
| `GameController.java` | Input is resolved through `Action` / `KeyBindings` instead of comparing `KeyCode` values; direction is tracked as a `Direction`; the eight hard-coded key checks collapsed into one dispatch method | Keys had to be rebindable, and the old `valKey` helper compared keys rather than directions, so it could not survive a remap |
| `GameView.java` | `turnDir` takes the previous and current `Direction` instead of reading two static `KeyCode` fields | The corner sprite depended on which physical key was pressed, which breaks as soon as a key is rebound |
| `SettingsStore.java` | Loads and saves one `key.<ACTION>` entry per action | Key bindings have to survive a restart like the other settings |
| `SettingsController.java` / `Settings.fxml` | Added the controls list: one row per action, click to capture a new key, with clash detection and an Esc cancel | The UI for rebinding, and the reason the window grew to 640 px |

---

## 8. Unexpected Problems

<!-- TODO: obstacles hit so far, and how they were resolved -->

---

## Development Log

| Tag | Meaning |
|---|---|
| `v0.0-baseline` | Original code as provided |
| `v0.2-ui` | Main menu rebuilt with a logo, difficulty selector and Quit button |
| `v0.3-hud` | In-game HUD pill, pause overlay and game over panel |
