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

---

## 3. Implemented and Working Properly

<!-- TODO: fill in as features are completed -->

---

## 4. Implemented but Not Working Properly

<!-- TODO -->

---

## 5. Features Not Implemented

<!-- TODO -->

---

## 6. New Java Classes

<!-- TODO: list every class added, with purpose and location -->

---

## 7. Modified Java Classes

| File | Change | Reason |
|---|---|---|
| `pom.xml` | `maven.compiler.release` 21 → 17; JavaFX 25.0.1 → 21.0.13 | The original values cannot be built with the installed JDK 17, and JavaFX 25 requires JDK 23 or newer |
| `Coin.java` | Spawn position now comes from a precomputed list of free cells | The original retry loop was unbounded and could freeze the game once the board was nearly full |
| `Obstacle.java` | Placement restricted to columns `[0, width - 1]`; free-cell list with shuffle replaces the retry loop; hard cap of 50 kept | The original `nextInt(x) + 1` could place an obstacle one cell outside the board, and the retry loop could spin forever |
| `GameController.java` | Coin selection rewritten as a `switch` with distinct branches; coin sound wired up; unused field and import removed | `ran == 1` was tested twice, so the rainbow coin was unreachable; the silver coin required an empty board and never appeared; the imported `MediaPlayer` was never used, so no sound was ever played |

---

## 8. Unexpected Problems

<!-- TODO: obstacles hit so far, and how they were resolved -->

---

## Development Log

| Tag | Meaning |
|---|---|
| `v0.0-baseline` | Original code as provided |
| `v0.1-run` | All known bugs fixed, build and tests pass |
| `v0.2-structured` | Package layout and decoupling complete |
| `v0.3-levels` | Level system and gameplay additions |
| `v1.0-submission` | Final submission state |
