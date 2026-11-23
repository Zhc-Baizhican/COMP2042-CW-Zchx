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
- Five coin types with distinct effects: normal, gold (+15 points), silver (adds obstacles, hard mode only), blue (fearless of borders), rainbow (speed-up)
- Obstacles that the snake must avoid
- Score and run time painted on the board itself, so they stay pinned to the playfield whatever the window size
- Coin sound effect played on pickup
- Pause menu on `Escape` with Resume / Restart / Main menu, restart (`TAB`), fullscreen toggle (`+` / `-`)
- Game over and level complete panels showing the final score and the restart keys
- Main menu with logo, Start, Settings and Quit; Start opens a separate difficulty page instead of hiding the choice behind one cycling button
- Three difficulties on that page, each with a one-line description: Easy is slower, the walls stop the snake instead of killing it, coins spawn near the snake and one automatic respawn is granted (-30 points); Normal moves faster with deadly walls; Hard is the fastest and is the only mode that spawns the obstacle-adding silver coin
- Single application window: the menu, the game and the settings screen are scenes swapped inside one stage, so there is one taskbar entry and one fullscreen state
- Settings screen: sound on/off, volume slider, five snake colour presets, a day / night theme, three window sizes and rebindable keys, saved to `~/.comp2042/settings.properties` and restored on the next launch
- One fixed square window for every screen: the size setting (480 / 600 / 760 px) applies to the whole application, and the board scales its cell size to fit, so navigating never resizes the window
- Night theme recolours the menu, the board and the obstacles, so obstacles stay visible
- A run is won by reaching the difficulty's target score: Easy 100, Normal 200, Hard 250. The HUD shows progress as `Score: 45 / 200`
- Endless is the fourth choice on that page: no score target, the classic game that is won only by filling the whole board
- History screen listing finished runs newest first, each with a picture of the final board, the mode, the score, the run time, the snake length, and the hacks that were on
- A run clock that starts at 0:00 when the start prompt appears, pauses with the game, and freezes when the run ends
- Game feel: a 3-2-1 countdown after the start prompt, a coin pickup ring, a screen shake when a run ends, a low thud and a bright chime for death and badges, and a best-score line per mode on the main menu
- Badges: 15 achievements checked against every finished run, unlocked badges are marked on a dedicated screen and reported under the result of the run that earned them
- Four rarity tiers colour the badges from muted grey up to a glowing gold card; earned badges drop into the end-of-run panel one after another
- One badge is secret: its name and condition stay masked as `???` until it is earned, and locked secrets are listed after the visible ones
- Developer mode, off by default and toggled in the settings: it reveals the badge reset button, so the unlock flow can be retested without editing files
- Every control is rebindable: click a key in the settings screen and press the new one. A key can only drive one action, and clashes are reported instead of silently overwriting
- On-screen hints (main menu, pause, game over) are generated from the current bindings, so they follow a rebind
- Hack mode: a list of rule-breaking entries reachable from the difficulty page, freely combinable, saved between runs, with a preset row that clears the previous selection before applying
- Eight hacks: ghost board, instant win, Midas (gold coins only), no growth, mirror keys, turbo, slow motion and blind (the board goes dark except the head and the coin)
- *Ghost board* lets the snake leave the board and drops coins in the transparent area around it; *Win* ends the run as a victory on the first key press
- Unit tests (JUnit 5, `src/test/java`) — 68 tests covering `Game`, `Direction`, `KeyBindings`, `Hacks`, the hack presets, the run history, the achievements and the board geometry, run with `.\mvnw.cmd test`

---

## 4. Implemented but Not Working Properly

- **The transparent game window depends on the platform.** The game window is created with `StageStyle.TRANSPARENT`, which is what lets the desktop show through around the board and makes the ghost-board hack look like the board is floating. On a machine where per-pixel transparency is unavailable — a remote desktop session, a virtual machine, or a driver that does not offer desktop composition — the JavaFX toolkit silently falls back to an ordinary decorated window. The game still plays, but the area around the board becomes an opaque block and the window grows a title bar. The application cannot detect or force composition, so there is no fallback to switch to.
- **The ghost board only covers the primary screen.** The playfield is sized from `Screen.getPrimary().getVisualBounds()`, so on a multi-monitor setup the extra cells stop at the edge of the main display.
- **The run clock includes the 3-2-1 countdown.** The clock starts when the start prompt appears, as specified, so it also counts the 1.5 s countdown before the snake is released. A recorded run therefore cannot be much shorter than two seconds.
- **Older history entries are skipped after the format change.** `history.tsv` gained a duration column, so rows written by an earlier build have seven fields instead of eight and are dropped on load rather than migrated. The screen simply starts from an empty list.
- **The three sound effects share one clip.** The pickup blip, the death thud and the badge chime are the same `coinSound.wav` played at three playback rates. It works and needs no extra assets, but it is a compromise: a recorded effect per event would sound better.
- **Blind hides the obstacles too.** With the blind hack on, the silver coin in hard mode still litters the board with obstacles, and those obstacles are invisible under the darkness. That is a genuine handicap rather than a bug, but it can feel unfair.
- **Badges are only evaluated when a run ends.** Leaving a run through the pause menu records nothing, so a good run abandoned half way is neither scored nor eligible for a badge.

---

## 5. Features Not Implemented

- **World-shaping hacks** — shrinking board, rotating board, portals, a coin magnet and an autopilot. Each one moves coordinates or spawns entities while the run is in progress, so it touches the drawing, the collision checks and the coin placement at the same time. With roughly five working days left before the documentation deadline I chose to keep eight hacks finished and tested instead of adding two half-working ones.
- **Named leaderboards.** The best score per mode is shown on the main menu, but there is no name entry, no per-achievement ranking and no shared table, because a meaningful ranking needs a server or a synchronised file that the coursework does not provide.
- **Replaying a finished run.** The history keeps a picture of the final board; replaying the whole run would mean recording every direction change and re-running the simulation, which is a feature on its own.
- **Gamepad or touch input.** Every control is rebindable to a key, but the input layer only understands `KeyEvent`; supporting a controller or touch would need a second input path.
- **Localisation.** All user-facing text is English and lives in the FXML files and the controllers, so adding a language would mean externalising every string.

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
| `PlayMenuController` | Controller for the difficulty page, which also builds and starts the game |
| `Hack` | One rule-breaking entry, with its name and description |
| `Hacks` | Which entries are switched on; persisted with the other settings |
| `HackPreset` | Ready-made combinations that clear the previous selection before applying |
| `HackMenuController` | Controller for the hack list and its preset row |
| `RunRecord` | One finished run: time, mode, score, length, duration, hacks and picture |
| `RunHistory` | Persists the records to `~/.comp2042/history/history.tsv` and caps the list |
| `HistoryController` | Controller for the history screen |
| `RunSummary` | The facts about a finished run that the achievement rules read |
| `Achievement` | One badge, with its title, description, rarity tier and unlock rule |
| `AchievementTier` | Common, rare, epic and legendary, each with the style class that colours it |
| `Achievements` | Which badges are unlocked; persisted to `~/.comp2042/achievements.txt` |
| `AchievementController` | Controller for the badge screen |
| `ThemeManager` | Applies the day / night theme by toggling a style class on a scene root |
| `Direction` | The four directions the snake travels, replacing raw character and key codes |
| `Action` | Every keyboard action with its display name, default key and direction |
| `KeyBindings` | The current key for each action, with reverse lookup for clash detection |
| `BoardGeometry` | Where the twenty-cell board sits inside a playfield that may be a whole screen; the one place that knows the board rectangle |

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
| `Action.java` | `PAUSE` (on `P`) removed; `BACK_TO_MENU` replaced by `PAUSE_MENU` on `Escape` | Pause had to open a menu with Resume / Restart / Main menu, so one key replaced the old pair of "pause" and "back" |
| `Direction.java` | Added `mirrored()` | The mirror-keys hack needs one place that knows left and right are each other's opposite |
| `PlayMenuController.java` | Fourth difficulty (Endless); a Hacks button; scene sizing for the ghost board; the initial coin's region; Escape back to the menu | The page became the entry point for both the modes and the hacks |
| `GameController.java` | Pause-menu actions wired to the view; a run clock and a first-key idle measurement; run recording (picture, history, badges); guards so a finished run stops; coin spawn regions; a speed multiplier for the turbo and slow-motion hacks; the 3-2-1 countdown | The end of a run used to leave the timeline running, and the hacks needed a single place to scale the animation rate |
| `GameView.java` | The score and the run time are painted on the canvas; the status overlay fades in and then pops; badge cards animate in one by one; shake, pickup ring and countdown; board capture for the history; bounds-checked turn lookups | A `Label` positioned by the layout drifted away from the board once the playfield and the window had different sizes, and the turn array threw as soon as the snake could leave the grid |
| `Snake.java` | In easy mode a wall stops the snake instead of wrapping it, with a `wallBlocked` flag for the arrows; the ghost-board hack removes the border rule; the no-growth hack keeps the tail moving on a pickup | Wrapping made easy mode feel like a different game, and the hacks needed the rules to live where they are checked |
| `Coin.java` | An `Area` enum (`ANY` / `BOARD` / `OUTSIDE`) filters the free cells a coin may occupy | The ghost board alternates between coins on the board and coins in the transparent area |
| `GoldCoin` / `SilverCoin` / `BlueCoin` / `RainbowCoin` | Constructors take the spawn area | They have to pass the region through to `Coin` |
| `Settings.java` / `SettingsStore.java` | A `developerMode` flag, saved as `developer.mode` | The badge reset is a testing tool and should not be on screen by default |
| `Settings.fxml` / `SettingsController.java` | A "Dev mode" toggle | The switch for the flag above |
| `MainMenuController.java` / `MainMenu.fxml` | History and Badges buttons in one row; a best-score line under the title | Three destinations had to fit in a 480 px window without pushing the menu off screen; the best scores are aggregated from the history rather than stored twice |
| `ViewManager.java` | Screens for hacks, history and badges; the board fills the window instead of leaving a margin; the game runs on a transparent stage | The margin only made sense while the window was transparent and became a dead border where it was not |
| `styling.css` | The night-theme background is scoped to the menu panes; badge tier colours; badge cards; the best-score label | A generic `.dark` background painted over the game scene, and the badge screen needed four visual tiers |
| `GameTest.java` / `KeyBindingsTest.java` / `HacksTest.java` | Cases for the endless target, the win condition, the renamed pause action, the presets, the tiers and the mirror direction | Each new rule needed a test that pins it down |

---

## 8. Unexpected Problems

- **The game window disappeared when the menu closed.** The game was first shown on a second stage created with `initOwner(mainStage)`. An owned window is hidden together with its owner, so `stage.hide()` — called right after showing the game — took the game window with it and clicking Play looked like it did nothing. Removing the owner relationship and showing the game before hiding the menu fixed it.
- **A white frame around the board.** JavaFX adds the style class `root` to every scene root, and the stylesheet painted `.root` white. Inside the game window that rule covered the transparent margin around the board, so the board appeared to sit in a white frame. A `.root.gameScene` rule now keeps the game scene transparent.
- **The status overlay used to be a rectangle over the whole window.** The overlay was stretched to the canvas, which in ghost-board mode is the size of the screen, so the darkened "block" was much larger than the board. It is now sized and positioned on the board rectangle.
- **Array index out of bounds once the snake could leave the board.** `GameView` indexed a 20×20 `turnArray` with the snake's coordinates to remember where the body turned. As soon as the ghost board let the head leave that grid, the indices went negative and every frame threw, which froze the picture. The array is now allocated from the game's own dimensions and read through bounds-checked helpers.
- **The snake kept moving after the game was won.** The win check ran *after* the score was increased and then returned early, so the coin was never replaced, the timeline was never stopped, and a finished run could still be steered around and fed the same coin for more points. `step()` now stops the timeline and the run on both "dead" and "won", and the clock is frozen at the same moment.
- **A win through the Win hack was never recorded.** That path returned before the recording code, so no history entry and no badge was produced — which is why the waiting badge could never fire. The win path now records the run and scores the badges before showing the result.
- **Java would not let an enum constant read a constant declared below it.** The secret badge's threshold first lived in a static field under the enum constants, which is an illegal forward reference. The value is now written into the rule itself, with a comment saying why.
- **A removed action left a stale entry in the settings file.** When `PAUSE` was replaced by `PAUSE_MENU`, an old `key.PAUSE=P` line stayed in `settings.properties`. Unknown action names are ignored on load, so the file is harmless, but it is worth knowing about when reading a saved configuration.
- **The Maven wrapper could not download its Maven distribution in this environment.** The pinned `apache-maven-3-bin.zip` URL returned 404 and the shell could not negotiate TLS, so the build was verified with `javac` plus the JUnit console launcher against a local Maven repository. The project itself builds normally with `.\mvnw.cmd` on a machine with network access.

---

## 9. Design Patterns

The codebase uses a small number of patterns deliberately; they are listed here because they are spread over several classes.

| Pattern | Where | What it buys |
|---|---|---|
| Strategy (as an enum of lambdas) | `Hack`, `Achievement` | Each entry carries its own rule as a `Predicate<RunSummary>`, so adding a badge or a hack is one enum constant instead of another branch inside the game loop |
| Singleton | `Settings`, `Hacks`, `Achievements`, `RunHistory` | One process-wide store per concern; each loads itself once and is reachable from the screens that need it |
| Observer | `SettingsController`, `HackMenuController` (property listeners) | A control writes its own change into the store and saves, so no screen has to poll another |
| Model–View–Controller | every `*.fxml` with its `*Controller` | The layout stays in FXML, the wiring in the controller, and the state in the model classes |
| Template method | `GameController.step()` | The per-tick skeleton (move, check death, eat, redraw, update status) is fixed, while the difficulty and the hacks change what each step does |
| Factory-style selection | `GameController.newCoin()` | One place decides which `Coin` subclass the next pickup is, instead of the coin knowing about the game |
| Value object | `RunRecord`, `RunSummary`, `Point`-based grid coordinates | Immutable data handed between the game, the history and the badge rules |
| Facade | `ViewManager`, `AudioService` | One entry point for screen switching and one for sound, so no controller has to know how a stage or a `MediaPlayer` is built |

---

## Development Log

| Tag | Meaning |
|---|---|
| `v0.0-baseline` | Original code as provided |
| `v0.2-ui` | Main menu rebuilt with a logo, difficulty selector and Quit button |
| `v0.3-hud` | In-game HUD pill, pause overlay and game over panel |
| `v0.4-hacks` | Hack menu with the ghost board, instant win and preset row, start prompt, pause menu, score drawn on the board |
| `v0.5-endless` | Endless mode that is won by filling the board, a run clock, and a history screen with a picture of every finished run |
| `v0.6-badges` | 15 achievements across four rarity tiers, evaluated at the end of every run, with a badge screen, pop-up badge cards, a masked secret badge and a developer mode that reveals the reset |
| `v0.7-feel` | Six more hacks, two extra presets, and game feel: countdown, pickup ring, screen shake, death and badge sounds, best scores on the menu |
| `v0.8-cleanup` | Dead `drawGrid` parameters, the duplicated renderer and the three copies of the board constant collapsed into `BoardGeometry` |
