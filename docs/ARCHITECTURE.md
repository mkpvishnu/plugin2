# PropHunt Plugin - Complete Architecture Document

**Version:** 1.0.0
**Minecraft Version:** 1.21+
**Platform:** Spigot/Paper
**Players:** 6-20 players per game

---

## Table of Contents

1. [Overview](#overview)
2. [Game Flow](#game-flow)
3. [Core Mechanics](#core-mechanics)
4. [Technical Architecture](#technical-architecture)
5. [Class Specifications](#class-specifications)
6. [Data Models](#data-models)
7. [Event System](#event-system)
8. [Configuration](#configuration)

---

## Overview

PropHunt is a hide-and-seek style minigame where players are divided into two teams:

- **Props (Hiders):** Disguise as blocks/objects and hide in the arena
- **Hunters (Seekers):** Find and eliminate all props before time runs out

### Win Conditions

| Team | Win Condition |
|------|---------------|
| Props | At least 1 prop survives until timer ends |
| Hunters | Eliminate all props before timer ends |
| Hunters | Props win if all hunters die from penalties |

---

## Game Flow

### State Machine

```
┌─────────────────────────────────────────────────────────────────────┐
│                          GAME STATES                                 │
├─────────────────────────────────────────────────────────────────────┤
│                                                                     │
│   DISABLED ──▶ WAITING ──▶ STARTING ──▶ HIDING ──▶ HUNTING ──▶ ENDING
│                   │                                              │
│                   │◀────────────── RESTART ◀────────────────────│
│                                                                     │
└─────────────────────────────────────────────────────────────────────┘
```

### State Descriptions

#### 1. DISABLED
- Arena is not active
- No players can join
- Used for maintenance/setup

#### 2. WAITING
- **Location:** Lobby area (separate from arena)
- **Duration:** Until minimum players reached
- **Player Actions:**
  - Join/leave freely
  - View arena info
  - Check stats
- **Display:**
  - Bossbar: "Waiting for players... (5/6)"
  - Periodic actionbar tips

#### 3. STARTING
- **Duration:** 10 seconds countdown
- **Sequence:**
  1. Countdown begins (10 seconds)
  2. Players teleported to arena (random spawn points)
  3. Teams assigned (hidden from players initially)
  4. Team reveal via Title: "You are a PROP!" / "You are a HUNTER!"
  5. Hunters receive BLINDNESS + SLOWNESS + teleported to hunter cage
  6. Transition to HIDING phase

#### 4. HIDING
- **Duration:** 45 seconds (configurable)
- **Props:**
  - Spawn scattered across arena (invisible initially)
  - Can move freely (ghost mode - semi-transparent)
  - Must select a prop disguise
  - Must find hiding spot and LOCK in place
  - 5-second spawn protection (cannot be damaged)
- **Hunters:**
  - Trapped in hunter cage/spawn area
  - Blinded with heavy fog effect
  - Can hear distant prop movement sounds
  - Receive hunter kit items
  - Can strategize in chat
- **Display:**
  - Bossbar: "Hiding Phase - 0:45"
  - Countdown sounds at 10, 5, 3, 2, 1

#### 5. HUNTING
- **Duration:** 5 minutes (configurable)
- **Phases:**
  - **Early Game (5:00 - 3:00):** Normal gameplay
  - **Mid Game (3:00 - 1:00):** Taunt frequency increases
  - **Late Game (1:00 - 0:30):** Arena border warning
  - **Final Stand (0:30 - 0:00):** Props get subtle glow, maximum tension
- **Props:**
  - Stay hidden (locked mode)
  - Can reposition (risky - ghost mode visible)
  - Forced taunt every 30 seconds
  - If hit: revealed, can run, try to re-hide
- **Hunters:**
  - Search for props
  - Use detection sword to check blocks
  - Wrong hits cause self-damage
  - Coordinate with teammates
- **Transitions:**
  - All props dead → ENDING (Hunters win)
  - All hunters dead → ENDING (Props win)
  - Timer expires → ENDING (Props win)

#### 6. ENDING
- **Duration:** 10 seconds
- **Sequence:**
  1. Game freeze (no more damage)
  2. Announce winner with Title
  3. Display scoreboard
  4. Announce MVP
  5. Fireworks/effects for winners
  6. Teleport all players to lobby
  7. Reset arena
  8. Transition to WAITING

---

## Core Mechanics

### Team System

#### Team Composition
```
Total Players    Props (70%)    Hunters (30%)
─────────────    ───────────    ─────────────
     6               4              2
     8               6              2
    10               7              3
    12               8              4
    15              10              5
    20              14              6
```

#### Team Assignment
- Random assignment by default
- Optional: Rotation mode (previous hunters become props)
- Configurable ratio (default 70/30)

---

### Prop Disguise System

#### Technical Implementation
Using Minecraft 1.21 **BlockDisplay** entities:
1. Player becomes invisible (potion effect)
2. BlockDisplay entity spawned at player location
3. Entity follows player position
4. Entity rotation controlled by player

#### Prop Sizes & Health

| Size | Health | Examples | Hiding Difficulty |
|------|--------|----------|-------------------|
| Small | 8 HP (4 hearts) | Button, flower, torch, candle | Hard to spot |
| Medium | 14 HP (7 hearts) | Flower pot, lantern, skull, cake | Moderate |
| Large | 20 HP (10 hearts) | Barrel, chest, furnace, cauldron | Easy to spot |

#### Available Props
- Scanned automatically from arena blocks
- Only blocks that EXIST in the arena can be used as props
- Prevents obvious fakes (no diamond blocks in a wooden house)

#### Prop Selection GUI
```
┌─────────────────────────────────────────────────┐
│           Select Your Disguise                   │
├─────────────────────────────────────────────────┤
│  [SMALL]      [MEDIUM]       [LARGE]            │
│   4 HP         7 HP          10 HP              │
├─────────────────────────────────────────────────┤
│ ┌───┐ ┌───┐ ┌───┐ ┌───┐ ┌───┐ ┌───┐ ┌───┐     │
│ │ A │ │ B │ │ C │ │ D │ │ E │ │ F │ │ G │ ... │
│ └───┘ └───┘ └───┘ └───┘ └───┘ └───┘ └───┘     │
│  Barrel Chest  Pot  Flower ...                  │
└─────────────────────────────────────────────────┘
```

---

### Prop Movement System

#### Movement Modes

**1. GHOST MODE (Moving)**
```
Activated: When player moves (WASD)
Visual: Semi-transparent prop (50% opacity)
Sound: Soft footstep sounds audible to hunters
Risk: VISIBLE to hunters while moving
Use: Finding a hiding spot, repositioning
```

**2. LOCKED MODE (Hidden)**
```
Activated: Press SHIFT (sneak) to toggle
Visual: Fully opaque, solid-looking prop
Sound: Silent
Protection: Blends in with environment
Use: Actual hiding - this is the core gameplay
```

**3. ROTATING (While Locked)**
```
Activated: A/D keys or mouse movement while locked
Function: Rotate prop to match environment
Use: Face the correct direction to blend in
```

#### Movement Restrictions
- Cannot sprint (would be too obvious)
- Cannot jump while locked
- Swimming reveals you (floating props suspicious)
- Large props cannot fit through 1-block gaps

---

### Combat System

#### Hunter Attack Mechanics
```
HUNTER HITS BLOCK:
    │
    ├── [REAL BLOCK]
    │   ├── "Miss!" sound effect
    │   ├── Hunter takes 1 heart damage
    │   ├── 1.5 second attack cooldown
    │   └── Block particles (feedback)
    │
    └── [PROP PLAYER]
        ├── "Hit!" sound effect
        ├── Prop takes damage (weapon damage)
        ├── Prop is REVEALED (glowing effect, 3 seconds)
        ├── Prop auto-unlocks (can run)
        ├── Prop gets Speed I (2 seconds) for escape chance
        └── Hunter heals 1 heart (reward)
```

#### Prop Health & Damage
- Props have health based on size
- Hunters deal standard sword damage
- Props do NOT fight back (no weapons)
- Props rely on hiding and running

#### Chase Sequence
```
PROP HIT:
    │
    ├── Revealed (glowing) for 3 seconds
    ├── Speed boost for escape
    ├── Can run and try to re-hide
    │
    ├── If survives 10 seconds without being hit:
    │   └── Glow fades, can lock again
    │
    └── If killed:
        ├── Death effect (prop shatters)
        ├── Becomes spectator
        └── Points calculated
```

#### Hunter Death
- Hunters can die from wrong-hit penalties
- If ALL hunters die → Props win immediately
- Dead hunters become spectators

---

### Taunt System

#### Forced Taunts
Props MUST make sounds periodically to prevent indefinite camping:

```
TAUNT SCHEDULE:
├── Normal (5:00 - 2:00): Every 30 seconds
├── Intensified (2:00 - 1:00): Every 20 seconds
└── Final (1:00 - 0:00): Every 15 seconds

TAUNT SOUNDS (Random):
├── Animal: Chicken, Pig, Cow, Cat, Wolf
├── Block: Chest open, Door creak, Anvil, Note block
└── Ambient: Burp, Pop, Ding
```

#### Voluntary Taunts
- Props can taunt manually for BONUS POINTS
- Shows confidence, adds fun
- Cooldown: 30 seconds between voluntary taunts

---

### Late Game Mechanics

#### Dynamic Difficulty Scaling

**At 2:00 Remaining:**
```
├── Announcement: "The hunt intensifies!"
├── Taunt frequency: 30s → 20s
├── Hunters get slight speed boost
└── Tension music starts (optional)
```

**At 1:00 Remaining:**
```
├── Announcement: "Final minute!"
├── Arena border warning (particles at edge)
├── Taunt frequency: 20s → 15s
└── Props get nervous heartbeat sound
```

**At 0:30 Remaining:**
```
├── Announcement: "Last stand!"
├── Remaining props get subtle permanent glow
├── Survival bonus points start accumulating faster
└── Maximum intensity
```

---

### Scoring System

#### Prop Scoring
| Action | Points |
|--------|--------|
| Survive full game | +100 |
| Per 30 seconds survived | +10 |
| Voluntary taunt | +15 |
| Last prop standing bonus | +50 |
| Escape after being hit | +20 |

#### Hunter Scoring
| Action | Points |
|--------|--------|
| Find prop (first hit) | +25 |
| Kill prop | +50 |
| First blood | +25 |
| Find all props (team) | +100 (split) |

#### Penalties
| Action | Points |
|--------|--------|
| Wrong hit (hunter) | -5 |
| AFK warning (prop) | -10 |

---

### Spectator System

#### On Death
- Player enters Minecraft spectator mode
- Can fly around arena freely
- Camera follows action
- Cannot interact with game

#### Spectator Restrictions
- **Chat:** Dead players have separate chat (cannot leak info)
- **Visibility:** Can see all players (including disguised props)
- **Interference:** Cannot affect gameplay in any way

---

### Anti-Exploit Systems

#### AFK Detection
```
IF prop has no input for 45 seconds:
├── Warning message
├── Forced taunt
├── After 60 seconds: Start taking 1 HP damage every 10 seconds
└── After 90 seconds: Kick from game
```

#### Spawn Protection
- Props have 5-second immunity when game starts
- Prevents hunters from memorizing spawn locations

#### Stuck Detection
- If prop changes to large size in small space:
  - Check for valid space
  - If stuck: Force change to smaller prop
  - If still stuck: Teleport to nearest valid position

---

## Technical Architecture

### Package Structure

```
com.example.prophunt/
│
├── PropHuntPlugin.java              # Main plugin class
│
├── game/
│   ├── GameManager.java             # Manages all game instances
│   ├── Game.java                    # Individual game instance
│   ├── GameState.java               # Enum: WAITING, STARTING, etc.
│   ├── GameSettings.java            # Per-game configuration
│   ├── GameScoreboard.java          # Scoreboard management
│   └── GameTimer.java               # Timer utilities
│
├── arena/
│   ├── ArenaManager.java            # CRUD operations for arenas
│   ├── Arena.java                   # Arena data model
│   ├── ArenaRegion.java             # Cuboid region definition
│   ├── ArenaScanner.java            # Scan arena for valid props
│   └── ArenaSigns.java              # Join signs (optional)
│
├── team/
│   ├── Team.java                    # Enum: PROPS, HUNTERS, SPECTATOR
│   ├── TeamManager.java             # Team assignment logic
│   └── TeamBalancer.java            # Balance teams fairly
│
├── player/
│   ├── GamePlayer.java              # Base player wrapper
│   ├── PropPlayer.java              # Prop-specific data/methods
│   ├── HunterPlayer.java            # Hunter-specific data/methods
│   ├── PlayerManager.java           # Track all game players
│   └── PlayerStats.java             # Statistics tracking
│
├── disguise/
│   ├── DisguiseManager.java         # Handle all disguises
│   ├── PropDisguise.java            # Individual disguise instance
│   ├── PropType.java                # Prop metadata (block, size, health)
│   └── PropRegistry.java            # Available props per arena
│
├── mechanics/
│   ├── TauntManager.java            # Forced/voluntary taunts
│   ├── CombatManager.java           # Hit detection, damage
│   ├── MovementManager.java         # Ghost/lock modes
│   └── LateGameManager.java         # Dynamic difficulty
│
├── commands/
│   ├── PropHuntCommand.java         # Main command handler
│   ├── AdminCommands.java           # Setup commands
│   ├── PlayerCommands.java          # Gameplay commands
│   └── CommandTabCompleter.java     # Tab completion
│
├── gui/
│   ├── GUIManager.java              # GUI utilities
│   ├── PropSelectorGUI.java         # Choose prop disguise
│   ├── ArenaSetupGUI.java           # Admin arena setup
│   └── StatsGUI.java                # Player statistics view
│
├── listeners/
│   ├── GameListener.java            # Game state events
│   ├── PlayerConnectionListener.java # Join/quit handling
│   ├── PlayerInteractionListener.java# Hit detection
│   ├── PlayerMovementListener.java  # Movement modes
│   ├── InventoryListener.java       # GUI clicks
│   └── ProtectionListener.java      # Block protection
│
├── storage/
│   ├── StorageManager.java          # Storage abstraction
│   ├── YAMLStorage.java             # YAML file storage
│   ├── SQLiteStorage.java           # SQLite database
│   └── DataConverter.java           # Migration utilities
│
├── util/
│   ├── MessageUtil.java             # Formatted messages
│   ├── LocationUtil.java            # Location helpers
│   ├── ItemBuilder.java             # Item creation utility
│   ├── SoundUtil.java               # Sound effects
│   └── ParticleUtil.java            # Particle effects
│
└── api/
    ├── PropHuntAPI.java             # Public API for other plugins
    └── events/                      # Custom events
        ├── GameStartEvent.java
        ├── GameEndEvent.java
        ├── PropFoundEvent.java
        └── PropKilledEvent.java
```

---

## Class Specifications

### Core Classes

#### PropHuntPlugin.java
```java
public class PropHuntPlugin extends JavaPlugin {
    // Singleton instance
    private static PropHuntPlugin instance;

    // Managers
    private GameManager gameManager;
    private ArenaManager arenaManager;
    private PlayerManager playerManager;
    private DisguiseManager disguiseManager;
    private StorageManager storageManager;
    private MessageUtil messageUtil;

    // Lifecycle
    void onEnable();
    void onDisable();

    // Getters for all managers
}
```

#### Game.java
```java
public class Game {
    private final Arena arena;
    private GameState state;
    private GameSettings settings;
    private final Map<UUID, GamePlayer> players;
    private final TeamManager teamManager;
    private GameTimer timer;

    // State management
    void setState(GameState state);
    void start();
    void stop(Team winner);
    void reset();

    // Player management
    void addPlayer(Player player);
    void removePlayer(Player player);
    void eliminatePlayer(GamePlayer player);

    // Team access
    List<PropPlayer> getProps();
    List<HunterPlayer> getHunters();
    List<GamePlayer> getSpectators();

    // Game info
    int getAlivePropsCount();
    int getAliveHuntersCount();
    int getTimeRemaining();
}
```

#### Arena.java
```java
public class Arena {
    private final String name;
    private final ArenaRegion region;        // Play area
    private final ArenaRegion lobbyRegion;   // Lobby area
    private final ArenaRegion hunterCage;    // Hunter waiting area

    private Location lobbySpawn;
    private List<Location> propSpawns;
    private List<Location> hunterSpawns;

    private final PropRegistry propRegistry;  // Valid props for this arena
    private GameSettings defaultSettings;

    private boolean enabled;

    // Region checks
    boolean contains(Location loc);
    boolean isInLobby(Location loc);
    boolean isInHunterCage(Location loc);

    // Management
    void scanForProps();                      // Auto-detect valid props
    void setEnabled(boolean enabled);
}
```

#### PropDisguise.java
```java
public class PropDisguise {
    private final PropPlayer player;
    private PropType propType;
    private BlockDisplay displayEntity;      // The visual block

    private boolean locked;
    private float rotation;

    // Creation/destruction
    void apply();
    void remove();

    // State
    void setLocked(boolean locked);
    void setRotation(float rotation);
    void setPropType(PropType type);

    // Visual updates
    void updatePosition();
    void setGlowing(boolean glowing);
    void setTransparency(float alpha);       // For ghost mode
}
```

---

## Data Models

### Arena Configuration (YAML)
```yaml
# arenas/castle.yml
name: "Castle"
enabled: true

region:
  world: "world"
  pos1: { x: 100, y: 64, z: 100 }
  pos2: { x: 200, y: 100, z: 200 }

lobby:
  spawn: { x: 150, y: 65, z: 95, yaw: 0, pitch: 0 }
  region:
    pos1: { x: 145, y: 64, z: 90 }
    pos2: { x: 155, y: 70, z: 99 }

hunter-cage:
  region:
    pos1: { x: 148, y: 64, z: 150 }
    pos2: { x: 152, y: 68, z: 154 }

spawns:
  props:
    - { x: 120, y: 65, z: 120 }
    - { x: 130, y: 65, z: 140 }
    - { x: 180, y: 70, z: 160 }
  hunters:
    - { x: 150, y: 65, z: 152 }

settings:
  min-players: 6
  max-players: 16
  hiding-time: 45
  hunting-time: 300

props:
  # Auto-scanned or manually defined
  - BARREL
  - CHEST
  - CRAFTING_TABLE
  - FLOWER_POT
  - LANTERN
```

### Player Statistics (SQLite Schema)
```sql
CREATE TABLE player_stats (
    uuid TEXT PRIMARY KEY,
    username TEXT NOT NULL,

    -- General
    games_played INTEGER DEFAULT 0,
    wins INTEGER DEFAULT 0,
    losses INTEGER DEFAULT 0,

    -- As Prop
    games_as_prop INTEGER DEFAULT 0,
    survivals INTEGER DEFAULT 0,
    times_found INTEGER DEFAULT 0,
    times_escaped INTEGER DEFAULT 0,
    total_survival_time INTEGER DEFAULT 0,
    taunts_performed INTEGER DEFAULT 0,

    -- As Hunter
    games_as_hunter INTEGER DEFAULT 0,
    props_found INTEGER DEFAULT 0,
    props_killed INTEGER DEFAULT 0,
    wrong_hits INTEGER DEFAULT 0,

    -- Scoring
    total_points INTEGER DEFAULT 0,
    highest_game_score INTEGER DEFAULT 0,

    -- Meta
    first_played TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_played TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_points ON player_stats(total_points DESC);
CREATE INDEX idx_wins ON player_stats(wins DESC);
```

---

## Event System

### Custom Events

```java
// Called when a game starts
public class GameStartEvent extends Event {
    private final Game game;
    private final List<PropPlayer> props;
    private final List<HunterPlayer> hunters;
}

// Called when a game ends
public class GameEndEvent extends Event {
    private final Game game;
    private final Team winner;
    private final EndReason reason;  // PROPS_WIN, HUNTERS_WIN, CANCELLED
}

// Called when a prop is first hit (found)
public class PropFoundEvent extends Event implements Cancellable {
    private final PropPlayer prop;
    private final HunterPlayer hunter;
}

// Called when a prop is killed
public class PropKilledEvent extends Event {
    private final PropPlayer prop;
    private final HunterPlayer killer;
}

// Called when a prop changes disguise
public class PropDisguiseEvent extends Event implements Cancellable {
    private final PropPlayer prop;
    private final PropType oldType;
    private final PropType newType;
}
```

---

## Configuration

### Main Config (config.yml)
```yaml
# PropHunt Configuration

# General
prefix: "&8[&bPropHunt&8]"
language: "en_US"

# Game Settings (defaults, can be overridden per arena)
game:
  min-players: 6
  max-players: 20

  team-ratio:
    props-percentage: 70

  timers:
    lobby-countdown: 10        # Seconds before game starts
    hiding-phase: 45           # Seconds for props to hide
    hunting-phase: 300         # Seconds for hunting (5 min)
    ending-phase: 10           # Seconds to show results

  spawn-protection: 5          # Seconds of immunity for props

# Prop Settings
props:
  health:
    small: 8                   # 4 hearts
    medium: 14                 # 7 hearts
    large: 20                  # 10 hearts

  change-cooldown: 45          # Seconds between prop changes
  max-changes: 3               # Maximum prop changes per game

  movement:
    ghost-opacity: 0.5         # Transparency when moving (0-1)
    footstep-volume: 0.3       # Volume of footsteps when moving

# Hunter Settings
hunters:
  wrong-hit-damage: 2          # Hearts lost for wrong guess
  attack-cooldown: 1.5         # Seconds between attacks
  reward-heal: 2               # Hearts healed on successful hit

  starting-kit:
    sword: IRON_SWORD

# Taunt Settings
taunts:
  enabled: true
  intervals:
    normal: 30                 # Every 30 seconds normally
    intensified: 20            # Every 20 seconds (under 2 min)
    final: 15                  # Every 15 seconds (under 1 min)

  sounds:
    - ENTITY_CHICKEN_AMBIENT
    - ENTITY_PIG_AMBIENT
    - ENTITY_COW_AMBIENT
    - ENTITY_CAT_AMBIENT
    - BLOCK_CHEST_OPEN
    - BLOCK_WOODEN_DOOR_OPEN
    - ENTITY_PLAYER_BURP
    - ENTITY_EXPERIENCE_ORB_PICKUP

# Late Game
late-game:
  intensify-at: 120            # Seconds remaining to intensify
  final-stand-at: 30           # Seconds remaining for final stand
  prop-glow-in-final: true     # Props glow in final 30 seconds

# AFK Protection
afk:
  warning-time: 45             # Seconds before AFK warning
  damage-time: 60              # Seconds before AFK damage starts
  kick-time: 90                # Seconds before AFK kick
  damage-amount: 2             # Hearts damage per 10 seconds

# Scoring
scoring:
  props:
    survive-game: 100
    per-30-seconds: 10
    voluntary-taunt: 15
    last-standing: 50
    escape-after-hit: 20
  hunters:
    find-prop: 25
    kill-prop: 50
    first-blood: 25
    find-all-bonus: 100

# Storage
storage:
  type: SQLITE                 # YAML, SQLITE, MYSQL
  mysql:
    host: localhost
    port: 3306
    database: prophunt
    username: root
    password: ""

# Effects
effects:
  sounds: true
  particles: true
  titles: true
  actionbar: true
  bossbar: true

# Debug
debug: false
```

### Messages Config (messages.yml)
```yaml
# PropHunt Messages

general:
  prefix: "&8[&bPropHunt&8] "
  no-permission: "&cYou don't have permission to do that!"
  player-only: "&cThis command can only be used by players!"
  unknown-command: "&cUnknown command. Use /prophunt help"

game:
  join:
    success: "&aYou joined the game in arena &e{arena}&a!"
    already-in-game: "&cYou are already in a game!"
    game-full: "&cThis game is full!"
    game-in-progress: "&cThe game has already started!"
  leave:
    success: "&aYou left the game."
    not-in-game: "&cYou are not in a game!"

  start:
    countdown: "&eGame starting in &c{seconds} &eseconds!"
    team-reveal-prop: "&a&lYOU ARE A PROP!\n&7Hide from the hunters!"
    team-reveal-hunter: "&c&lYOU ARE A HUNTER!\n&7Find all the props!"
    hiding-phase: "&e&lHIDING PHASE\n&7Props have {seconds} seconds to hide!"
    hunting-phase: "&c&lHUNT BEGINS!\n&7Find the props!"

  end:
    props-win: "&a&lPROPS WIN!\n&7{count} prop(s) survived!"
    hunters-win: "&c&lHUNTERS WIN!\n&7All props eliminated!"
    draw: "&e&lDRAW!"

props:
  select-prompt: "&eRight-click the &bNether Star &eto select your disguise!"
  selected: "&aYou are now disguised as: &e{prop}"
  locked: "&7You are now &aLOCKED&7. Blend in!"
  unlocked: "&7You are now &eUNLOCKED&7. Find a hiding spot!"
  cooldown: "&cYou must wait &e{seconds}s &cbefore changing props!"
  no-changes-left: "&cYou have no prop changes remaining!"
  forced-taunt: "&c&oYou made a sound!"

  found: "&cYou've been found! RUN!"
  escaped: "&aYou escaped! Find a new hiding spot!"
  eliminated: "&c&lELIMINATED!\n&7You survived for {time}."

hunters:
  released: "&c&lHUNTERS RELEASED!\n&7Find the props!"
  hit-prop: "&aYou found a prop! Chase them down!"
  hit-wrong: "&cThat was a real block! &7-1 heart"
  killed-prop: "&a&lKILL! &7You eliminated a prop!"
  first-blood: "&6&lFIRST BLOOD! &e{hunter} &7found the first prop!"

late-game:
  intensify: "&e&lThe hunt intensifies!"
  final-minute: "&c&lFINAL MINUTE!"
  last-stand: "&4&lLAST STAND!"

spectator:
  now-spectating: "&7You are now spectating."
  tip: "&7Fly around to watch the game!"

scoreboard:
  title: "&b&lPROPHUNT"
  lines:
    - "&7{date}"
    - ""
    - "&fTime: &a{time}"
    - "&fPhase: &e{phase}"
    - ""
    - "&fProps: &a{props_alive}/{props_total}"
    - "&fHunters: &c{hunters_alive}/{hunters_total}"
    - ""
    - "&fYour Role: &b{role}"
    - "&fPoints: &e{points}"
```

---

## Dependency Diagram

```
┌─────────────────────────────────────────────────────────────┐
│                      PropHuntPlugin                         │
│                      (Main Entry Point)                     │
└─────────────────────────┬───────────────────────────────────┘
                          │
        ┌─────────────────┼─────────────────┐
        ▼                 ▼                 ▼
┌───────────────┐ ┌───────────────┐ ┌───────────────┐
│ GameManager   │ │ ArenaManager  │ │ StorageManager│
│               │ │               │ │               │
│ - Games map   │ │ - Arenas map  │ │ - Stats       │
│ - Create game │ │ - Load/Save   │ │ - Arenas      │
│ - End game    │ │ - Scan props  │ │ - Settings    │
└───────┬───────┘ └───────┬───────┘ └───────────────┘
        │                 │
        ▼                 ▼
┌─────────────────────────────────────┐
│               Game                   │
│                                     │
│  ┌─────────────┐  ┌──────────────┐  │
│  │TeamManager  │  │PlayerManager │  │
│  │             │  │              │  │
│  │- Props      │  │- GamePlayers │  │
│  │- Hunters    │  │- Add/Remove  │  │
│  │- Spectators │  │- Get by UUID │  │
│  └─────────────┘  └──────────────┘  │
│                                     │
│  ┌─────────────────────────────┐   │
│  │      DisguiseManager        │   │
│  │                             │   │
│  │  - PropDisguise instances   │   │
│  │  - Apply/Remove disguises   │   │
│  │  - Update positions         │   │
│  └─────────────────────────────┘   │
│                                     │
│  ┌──────────────┐ ┌─────────────┐  │
│  │ TauntManager │ │CombatManager│  │
│  │              │ │             │  │
│  │- Force taunts│ │- Hit detect │  │
│  │- Scheduling  │ │- Damage     │  │
│  └──────────────┘ └─────────────┘  │
└─────────────────────────────────────┘
```

---

## Performance Considerations

### Entity Management
- Limit BlockDisplay entities (one per prop player)
- Remove entities on player disconnect
- Efficient position updates (every tick during movement, pause when locked)

### Tick Optimization
- Taunt scheduler: Check every second, not every tick
- Position updates: Only when player moves
- Glow updates: Event-driven, not polled

### Memory Management
- Clean up Game objects after ending
- Clear player data on disconnect
- Use weak references where appropriate

---

## Future Expansion (V2+)

### Planned Features
- Multiple arenas with queue system
- Spectator betting system
- Custom prop textures (resource pack)
- Achievement system
- Seasonal events
- Map voting
- Replay system

### API for Extensions
- Custom prop types
- Custom abilities
- Custom scoring rules
- Integration with other minigame plugins

---

*Document Version: 1.0*
*Last Updated: 2024*
