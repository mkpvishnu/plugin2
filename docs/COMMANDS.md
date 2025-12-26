# PropHunt Plugin - Commands Reference

Complete reference for all player and administrator commands.

---

## Table of Contents

1. [Command Overview](#command-overview)
2. [Player Commands](#player-commands)
3. [Admin Commands](#admin-commands)
4. [Arena Setup Commands](#arena-setup-commands)
5. [Permissions](#permissions)
6. [Command Examples](#command-examples)

---

## Command Overview

### Base Command
All PropHunt commands use the base command `/prophunt` with aliases `/ph` and `/hunt`.

```
/prophunt <subcommand> [arguments]
/ph <subcommand> [arguments]
/hunt <subcommand> [arguments]
```

### Command Summary Table

| Command | Description | Permission |
|---------|-------------|------------|
| `/ph join` | Join a game | `prophunt.play` |
| `/ph leave` | Leave current game | `prophunt.play` |
| `/ph stats` | View statistics | `prophunt.play` |
| `/ph list` | List available arenas | `prophunt.play` |
| `/ph top` | View leaderboard | `prophunt.play` |
| `/ph help` | Show help | `prophunt.play` |
| `/ph create` | Create new arena | `prophunt.admin` |
| `/ph delete` | Delete an arena | `prophunt.admin` |
| `/ph setup` | Arena setup mode | `prophunt.admin` |
| `/ph setspawn` | Set spawn points | `prophunt.admin` |
| `/ph setlobby` | Set lobby spawn | `prophunt.admin` |
| `/ph addprop` | Add valid prop | `prophunt.admin` |
| `/ph scan` | Scan arena for props | `prophunt.admin` |
| `/ph enable` | Enable arena | `prophunt.admin` |
| `/ph disable` | Disable arena | `prophunt.admin` |
| `/ph forcestart` | Force start game | `prophunt.admin` |
| `/ph forcestop` | Force stop game | `prophunt.admin` |
| `/ph reload` | Reload configuration | `prophunt.admin` |
| `/ph setconfig` | Modify settings | `prophunt.admin` |

---

## Player Commands

### /prophunt join
Join a PropHunt game.

**Usage:**
```
/prophunt join [arena]
/ph join [arena]
```

**Arguments:**
| Argument | Required | Description |
|----------|----------|-------------|
| `arena` | No | Arena name. If omitted, joins first available arena |

**Behavior:**
- Teleports player to arena lobby
- Adds player to waiting queue
- Shows current player count
- Fails if player is already in a game
- Fails if arena is full or disabled

**Examples:**
```
/ph join              # Join any available arena
/ph join castle       # Join the "castle" arena specifically
```

**Response Messages:**
```
Success: "You joined the game in arena Castle! (5/12 players)"
Error:   "You are already in a game!"
Error:   "Arena 'castle' is full!"
Error:   "Arena 'castle' does not exist!"
Error:   "No available arenas found!"
```

---

### /prophunt leave
Leave the current game.

**Usage:**
```
/prophunt leave
/ph leave
```

**Behavior:**
- Removes player from game
- Teleports player to server spawn (or configured exit location)
- If game is in progress, player becomes eliminated
- Adjusts team counts accordingly
- May trigger game end if not enough players remain

**Response Messages:**
```
Success: "You left the game."
Error:   "You are not in a game!"
```

---

### /prophunt stats
View player statistics.

**Usage:**
```
/prophunt stats [player]
/ph stats [player]
```

**Arguments:**
| Argument | Required | Description |
|----------|----------|-------------|
| `player` | No | Player name to view. If omitted, shows own stats |

**Behavior:**
- Opens GUI with statistics OR displays in chat
- Shows games played, wins, kills, etc.
- Can view other players' stats

**Output Format (Chat):**
```
========== Stats for PlayerName ==========
Games Played: 150
Wins: 87 (58%)
Losses: 63

-- As Prop --
Games: 95 | Survivals: 52 | Times Found: 43
Best Survival Time: 4:32
Taunts Performed: 234

-- As Hunter --
Games: 55 | Props Found: 127 | Props Killed: 98
Wrong Hits: 312
Accuracy: 29%

Total Points: 15,420
==========================================
```

---

### /prophunt list
List all available arenas.

**Usage:**
```
/prophunt list
/ph list
```

**Output Format:**
```
========== PropHunt Arenas ==========

 Castle [WAITING] - 5/12 players
   Click to join!

 Village [IN GAME] - 10/16 players
   Hunting phase - 2:34 remaining

 Factory [DISABLED]
   Currently unavailable

=====================================
```

**Behavior:**
- Shows all arenas with status
- Clickable in chat to join (if available)
- Shows player counts and game phase

---

### /prophunt top
View the leaderboard.

**Usage:**
```
/prophunt top [category] [page]
/ph top [category] [page]
```

**Arguments:**
| Argument | Required | Description |
|----------|----------|-------------|
| `category` | No | Leaderboard type: `wins`, `points`, `kills`, `survivals` |
| `page` | No | Page number (10 entries per page) |

**Categories:**
- `wins` - Most game wins (default)
- `points` - Highest total points
- `kills` - Most props killed (as hunter)
- `survivals` - Most survivals (as prop)

**Output Format:**
```
======= PropHunt Leaderboard (Wins) =======
#1  PlayerOne     - 156 wins
#2  PlayerTwo     - 143 wins
#3  PlayerThree   - 128 wins
#4  PlayerFour    - 115 wins
#5  PlayerFive    - 102 wins
#6  PlayerSix     - 98 wins
#7  PlayerSeven   - 87 wins
#8  PlayerEight   - 76 wins
#9  PlayerNine    - 65 wins
#10 PlayerTen     - 54 wins

Your Rank: #23 (45 wins)
Page 1/5 - /ph top wins 2 for next page
==========================================
```

---

### /prophunt help
Display help information.

**Usage:**
```
/prophunt help [command]
/ph help [command]
/ph ?
```

**Arguments:**
| Argument | Required | Description |
|----------|----------|-------------|
| `command` | No | Specific command to get detailed help |

**General Help Output:**
```
========== PropHunt Help ==========
/ph join [arena]  - Join a game
/ph leave         - Leave current game
/ph stats [player]- View statistics
/ph list          - List arenas
/ph top [type]    - Leaderboard
/ph help [cmd]    - This help

Use /ph help <command> for details
===================================
```

---

## In-Game Commands (During Gameplay)

These commands are available only during an active game:

### /prophunt taunt
Perform a voluntary taunt (props only).

**Usage:**
```
/prophunt taunt
/ph taunt
```

**Behavior:**
- Only works for prop players during hunting phase
- Plays random taunt sound
- Awards bonus points (+15)
- Has cooldown (30 seconds)

**Response Messages:**
```
Success: "You taunted! +15 points"
Error:   "You must be a prop to taunt!"
Error:   "Taunt on cooldown! (15s remaining)"
Error:   "You can only taunt during the hunting phase!"
```

---

### /prophunt prop
Open prop selection GUI (props only).

**Usage:**
```
/prophunt prop
/ph prop
```

**Behavior:**
- Opens prop selection GUI
- Only works during hiding phase or if changes remain
- Shows available props for current arena

---

## Admin Commands

### /prophunt create
Create a new arena.

**Usage:**
```
/prophunt create <name>
/ph create <name>
```

**Arguments:**
| Argument | Required | Description |
|----------|----------|-------------|
| `name` | Yes | Unique arena identifier (alphanumeric, no spaces) |

**Behavior:**
- Creates new arena with given name
- Arena starts in DISABLED state
- Puts admin in setup mode for this arena
- Must complete setup before enabling

**Examples:**
```
/ph create castle
/ph create medieval_town
/ph create factory1
```

**Response Messages:**
```
Success: "Arena 'castle' created! Now in setup mode."
         "Use /ph setup to configure the arena."
Error:   "Arena 'castle' already exists!"
Error:   "Invalid name! Use only letters, numbers, and underscores."
```

---

### /prophunt delete
Delete an arena.

**Usage:**
```
/prophunt delete <arena>
/ph delete <arena>
```

**Arguments:**
| Argument | Required | Description |
|----------|----------|-------------|
| `arena` | Yes | Arena name to delete |

**Behavior:**
- Requires confirmation (run twice or add `-confirm`)
- Kicks all players from arena if occupied
- Removes arena configuration file
- Cannot be undone

**Examples:**
```
/ph delete castle           # First time - asks for confirmation
/ph delete castle -confirm  # Immediate delete
```

**Response Messages:**
```
Warning: "Are you sure? Run '/ph delete castle -confirm' to confirm."
Success: "Arena 'castle' has been deleted."
Error:   "Arena 'castle' does not exist!"
```

---

### /prophunt setup
Enter/exit arena setup mode.

**Usage:**
```
/prophunt setup <arena>
/prophunt setup done
/ph setup <arena>
```

**Arguments:**
| Argument | Required | Description |
|----------|----------|-------------|
| `arena` | Yes | Arena to configure |

**Setup Mode Features:**
- Visual indicators for current regions
- Streamlined commands
- Real-time feedback
- Cannot leave setup mode until minimum requirements met

**Setup Checklist (shown to admin):**
```
========== Arena Setup: castle ==========
[X] Arena region defined
[X] Lobby spawn set
[ ] Lobby region defined (optional)
[X] Hunter cage region defined
[X] At least 1 prop spawn set
[X] At least 1 hunter spawn set
[X] At least 5 valid props defined
[ ] Test game completed (optional)

Use /ph setup done when ready.
=========================================
```

---

### /prophunt setregion
Define arena regions using WorldEdit-style selection or wand.

**Usage:**
```
/prophunt setregion <type>
/ph setregion <type>
```

**Arguments:**
| Argument | Required | Description |
|----------|----------|-------------|
| `type` | Yes | Region type: `arena`, `lobby`, `huntercage` |

**Methods:**
1. **With WorldEdit:** Uses current WorldEdit selection
2. **With Wand:** Left-click pos1, right-click pos2 with setup wand
3. **Manual:** `/ph setregion arena <x1> <y1> <z1> <x2> <y2> <z2>`

**Examples:**
```
/ph setregion arena           # Uses WorldEdit selection
/ph setregion lobby           # Uses WorldEdit selection
/ph setregion huntercage      # Uses WorldEdit selection
/ph setregion arena 100 64 100 200 100 200  # Manual coordinates
```

---

### /prophunt setspawn
Set spawn points.

**Usage:**
```
/prophunt setspawn <team> [index]
/ph setspawn <team> [index]
```

**Arguments:**
| Argument | Required | Description |
|----------|----------|-------------|
| `team` | Yes | Team: `prop`, `hunter`, `lobby` |
| `index` | No | Spawn index (for multiple spawns). Omit to add new. |

**Behavior:**
- Uses current player position and facing direction
- Multiple spawns can be set (players spawn randomly at one)
- At least 1 spawn required per team

**Examples:**
```
/ph setspawn lobby       # Set single lobby spawn
/ph setspawn prop        # Add a prop spawn point
/ph setspawn prop        # Add another prop spawn point
/ph setspawn hunter      # Add a hunter spawn point
/ph setspawn prop 1      # Override first prop spawn
```

**Response Messages:**
```
Success: "Prop spawn #3 set at your location."
Success: "Hunter spawn #1 set at your location."
Info:    "Arena now has 5 prop spawns and 2 hunter spawns."
```

---

### /prophunt delspawn
Delete a spawn point.

**Usage:**
```
/prophunt delspawn <team> <index>
/ph delspawn <team> <index>
```

**Examples:**
```
/ph delspawn prop 3      # Delete third prop spawn
/ph delspawn hunter 2    # Delete second hunter spawn
```

---

### /prophunt scan
Automatically scan arena for valid props.

**Usage:**
```
/prophunt scan [arena]
/ph scan [arena]
```

**Behavior:**
- Scans all blocks within arena region
- Identifies suitable prop blocks
- Categorizes by size (small/medium/large)
- Saves to arena configuration

**Output:**
```
========== Prop Scan Results: castle ==========
Scanning... Done!

Found 47 unique block types:

SMALL (15):
  Button, Torch, Candle, Flower, Dead Bush,
  Pressure Plate, Tripwire Hook, Lever, ...

MEDIUM (18):
  Flower Pot, Lantern, Skull, Cake,
  Brewing Stand, Enchanting Table, ...

LARGE (14):
  Barrel, Chest, Furnace, Crafting Table,
  Cauldron, Composter, Lectern, ...

Total: 47 props registered for arena 'castle'
===============================================
```

---

### /prophunt addprop
Manually add a prop type to arena.

**Usage:**
```
/prophunt addprop <arena> <block> [size]
/ph addprop <arena> <block> [size]
```

**Arguments:**
| Argument | Required | Description |
|----------|----------|-------------|
| `arena` | Yes | Arena name |
| `block` | Yes | Block material name |
| `size` | No | Size category: `small`, `medium`, `large` (auto-detected if omitted) |

**Examples:**
```
/ph addprop castle BARREL large
/ph addprop castle FLOWER_POT medium
/ph addprop castle TORCH small
/ph addprop castle DIAMOND_BLOCK       # Auto-detect size
```

---

### /prophunt removeprop
Remove a prop type from arena.

**Usage:**
```
/prophunt removeprop <arena> <block>
/ph removeprop <arena> <block>
```

**Examples:**
```
/ph removeprop castle DIAMOND_BLOCK    # Remove (too obvious!)
```

---

### /prophunt enable
Enable an arena for play.

**Usage:**
```
/prophunt enable <arena>
/ph enable <arena>
```

**Behavior:**
- Validates arena configuration is complete
- Enables arena for players to join
- Starts accepting players in lobby

**Validation Checks:**
- Arena region defined
- At least 1 prop spawn
- At least 1 hunter spawn
- At least 5 valid props
- Hunter cage region defined

**Response Messages:**
```
Success: "Arena 'castle' is now enabled!"
Error:   "Cannot enable arena. Missing: lobby spawn, hunter cage region"
```

---

### /prophunt disable
Disable an arena.

**Usage:**
```
/prophunt disable <arena>
/ph disable <arena>
```

**Behavior:**
- Stops new players from joining
- If game in progress: Force ends game
- Teleports all players out

---

### /prophunt forcestart
Force start a game (bypass minimum player requirement).

**Usage:**
```
/prophunt forcestart <arena>
/ph forcestart <arena>
```

**Behavior:**
- Starts game even with fewer than minimum players
- Useful for testing
- Warning shown to admin

---

### /prophunt forcestop
Force stop a running game.

**Usage:**
```
/prophunt forcestop <arena> [winner]
/ph forcestop <arena> [winner]
```

**Arguments:**
| Argument | Required | Description |
|----------|----------|-------------|
| `arena` | Yes | Arena name |
| `winner` | No | Force winner: `props`, `hunters`, `draw` |

**Behavior:**
- Immediately ends game
- If winner specified, awards points accordingly
- If no winner specified, treated as cancelled (no points)

---

### /prophunt reload
Reload plugin configuration.

**Usage:**
```
/prophunt reload [what]
/ph reload [what]
```

**Arguments:**
| Argument | Required | Description |
|----------|----------|-------------|
| `what` | No | What to reload: `config`, `messages`, `arenas`, `all` |

**Examples:**
```
/ph reload            # Reload everything
/ph reload config     # Just config.yml
/ph reload messages   # Just messages.yml
/ph reload arenas     # Just arena configs
```

**Response Messages:**
```
Success: "Configuration reloaded successfully!"
Warning: "Configuration reloaded with 2 warnings. Check console."
```

---

### /prophunt setconfig
Modify arena settings in-game.

**Usage:**
```
/prophunt setconfig <arena> <setting> <value>
/ph setconfig <arena> <setting> <value>
```

**Available Settings:**
| Setting | Type | Description |
|---------|------|-------------|
| `min-players` | Integer | Minimum players to start |
| `max-players` | Integer | Maximum players allowed |
| `hiding-time` | Integer | Hiding phase duration (seconds) |
| `hunting-time` | Integer | Hunting phase duration (seconds) |
| `prop-ratio` | Integer | Percentage of players as props |

**Examples:**
```
/ph setconfig castle min-players 4
/ph setconfig castle max-players 20
/ph setconfig castle hiding-time 60
/ph setconfig castle hunting-time 360
/ph setconfig castle prop-ratio 75
```

---

### /prophunt debug
Debug commands for troubleshooting.

**Usage:**
```
/prophunt debug <subcommand>
/ph debug <subcommand>
```

**Subcommands:**
| Subcommand | Description |
|------------|-------------|
| `info <arena>` | Show detailed arena info |
| `players` | Show all players in games |
| `entities` | List all PropHunt entities |
| `cleanup` | Force cleanup orphaned entities |
| `test <arena>` | Run arena validation tests |

---

## Permissions

### Permission Nodes

```yaml
permissions:
  prophunt.*:
    description: All PropHunt permissions
    default: op
    children:
      prophunt.play: true
      prophunt.admin: true
      prophunt.bypass: true

  prophunt.play:
    description: Basic gameplay permissions
    default: true
    children:
      prophunt.join: true
      prophunt.leave: true
      prophunt.stats: true
      prophunt.stats.others: true
      prophunt.list: true
      prophunt.top: true
      prophunt.help: true

  prophunt.admin:
    description: Administrative permissions
    default: op
    children:
      prophunt.create: true
      prophunt.delete: true
      prophunt.setup: true
      prophunt.setspawn: true
      prophunt.setregion: true
      prophunt.scan: true
      prophunt.addprop: true
      prophunt.removeprop: true
      prophunt.enable: true
      prophunt.disable: true
      prophunt.forcestart: true
      prophunt.forcestop: true
      prophunt.reload: true
      prophunt.setconfig: true
      prophunt.debug: true

  prophunt.bypass:
    description: Bypass restrictions
    default: op
    children:
      prophunt.bypass.full: true      # Join full games
      prophunt.bypass.cooldown: true  # No command cooldowns
```

### Default Permission Setup

For typical server setup, add to your permissions plugin:

```yaml
# Example for LuckPerms
groups:
  default:
    permissions:
      - prophunt.play

  moderator:
    permissions:
      - prophunt.forcestart
      - prophunt.forcestop
      - prophunt.bypass.full

  admin:
    permissions:
      - prophunt.admin
      - prophunt.bypass
```

---

## Command Examples

### Complete Arena Setup Workflow

```bash
# 1. Create the arena
/ph create castle

# 2. Define arena region (with WorldEdit)
//pos1                          # Stand at corner 1
//pos2                          # Stand at corner 2
/ph setregion arena

# 3. Define lobby area
//pos1                          # Lobby corner 1
//pos2                          # Lobby corner 2
/ph setregion lobby
/ph setspawn lobby              # Set lobby spawn

# 4. Define hunter cage
//pos1                          # Cage corner 1
//pos2                          # Cage corner 2
/ph setregion huntercage

# 5. Set spawn points
# Walk to various locations and run:
/ph setspawn prop               # Repeat 5+ times at different spots
/ph setspawn hunter             # Repeat 2+ times

# 6. Scan for valid props
/ph scan castle

# 7. Enable the arena
/ph enable castle

# 8. Test the arena
/ph forcestart castle
```

### Quick Admin Commands

```bash
# Check arena status
/ph list

# See who's playing
/ph debug players

# Quick fix if something breaks
/ph forcestop castle
/ph debug cleanup
/ph enable castle

# Reload after config changes
/ph reload config
```

### Player Commands in Chat

```bash
# Join a game
/ph join

# Check stats
/ph stats

# View leaderboard
/ph top wins

# Leave game
/ph leave
```

---

## Tab Completion

All commands support intelligent tab completion:

```
/ph j<TAB>           → join
/ph join <TAB>       → [arena1, arena2, arena3]
/ph setspawn <TAB>   → [prop, hunter, lobby]
/ph setconfig <TAB>  → [arena names]
/ph setconfig castle <TAB> → [min-players, max-players, hiding-time, ...]
```

---

## Error Handling

### Common Errors and Solutions

| Error Message | Cause | Solution |
|--------------|-------|----------|
| "You are already in a game!" | Player in another game | Use `/ph leave` first |
| "Arena does not exist!" | Typo in arena name | Check `/ph list` |
| "Arena is full!" | Max players reached | Wait or join different arena |
| "Not enough players!" | Below minimum | Wait for more players |
| "Arena is disabled!" | Admin disabled arena | Contact admin |
| "Missing setup requirements!" | Arena not fully configured | Complete setup |

---

*Document Version: 1.0*
*Last Updated: 2024*
