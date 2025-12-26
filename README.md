# PropHunt

A feature-rich Prop Hunt minigame plugin for Minecraft 1.21+ (Spigot/Paper).

Transform into blocks and hide from hunters, or hunt down disguised players before time runs out!

---

## Features

- **Full Prop Hunt Experience** - Classic hide-and-seek gameplay with block disguises
- **Smart Disguise System** - Props use BlockDisplay entities for smooth, accurate disguises
- **Balanced Gameplay** - Prop sizes affect health, wrong hits damage hunters
- **Dynamic Late Game** - Tension increases as time runs down
- **Multi-Arena Support** - Run multiple games simultaneously
- **Automatic Prop Detection** - Scans arenas to determine valid disguises
- **Full Statistics** - Track wins, kills, survival time, and more
- **Highly Configurable** - Customize timers, health, scoring, and more

---

## Requirements

- **Minecraft Server:** Spigot or Paper 1.21+
- **Java:** 21 or higher
- **Players:** 6-20 players per game (configurable)

### Optional Dependencies
- **WorldEdit** - Easier arena region selection
- **PlaceholderAPI** - Placeholders for scoreboards/chat
- **Vault** - Economy rewards (optional)

---

## Quick Start

### Installation

1. Download `PropHunt.jar` from [Releases](#)
2. Place in your server's `plugins/` folder
3. Restart the server
4. Configure in `plugins/PropHunt/config.yml`

### Create Your First Arena

```bash
# 1. Create arena
/ph create myarena

# 2. Select arena region (requires WorldEdit or use coordinates)
/ph setregion arena

# 3. Set lobby spawn (stand at location)
/ph setspawn lobby

# 4. Define hunter waiting cage
/ph setregion huntercage

# 5. Add spawn points (repeat at different locations)
/ph setspawn prop
/ph setspawn hunter

# 6. Scan for valid props
/ph scan myarena

# 7. Enable arena
/ph enable myarena
```

### Play!

```bash
/ph join myarena    # Join a game
/ph leave           # Leave the game
```

---

## How to Play

### For Props (Hiders)

1. **Select Your Disguise** - Right-click the Nether Star to open prop selection
2. **Find a Hiding Spot** - Move around (you'll be semi-transparent while moving)
3. **Lock In Place** - Press SHIFT to become solid and blend in
4. **Stay Hidden** - Don't move! Movement makes you visible
5. **Survive** - Last until the timer ends to win

**Tips:**
- Smaller props have less health but are harder to spot
- You can rotate while locked to face the right direction
- Every 30 seconds you'll make a sound (forced taunt) - hunters will hear it!
- If found, RUN! You get a speed boost to escape

### For Hunters (Seekers)

1. **Wait** - You start in a cage while props hide
2. **Hunt** - When released, search for props
3. **Attack Suspicious Blocks** - Hit blocks you think are players
4. **Beware** - Hitting real blocks damages YOU
5. **Eliminate All Props** - Find them all before time runs out

**Tips:**
- Listen for taunt sounds - they reveal prop locations
- Moving props are visible (semi-transparent)
- Coordinate with your team
- Don't spam-click everything - you'll kill yourself!

---

## Game Phases

| Phase | Duration | Description |
|-------|----------|-------------|
| **Waiting** | Until min players | Players gather in lobby |
| **Starting** | 10 seconds | Teams assigned, teleport to arena |
| **Hiding** | 45 seconds | Props hide, hunters are caged/blinded |
| **Hunting** | 5 minutes | Main gameplay - hunters seek props |
| **Ending** | 10 seconds | Results shown, players return to lobby |

---

## Commands

### Player Commands

| Command | Description |
|---------|-------------|
| `/ph join [arena]` | Join a game |
| `/ph leave` | Leave current game |
| `/ph stats [player]` | View statistics |
| `/ph list` | List available arenas |
| `/ph top [category]` | View leaderboard |
| `/ph help` | Show help |

### Admin Commands

| Command | Description |
|---------|-------------|
| `/ph create <name>` | Create new arena |
| `/ph delete <arena>` | Delete arena |
| `/ph setup <arena>` | Enter setup mode |
| `/ph setregion <type>` | Set arena/lobby/cage region |
| `/ph setspawn <team>` | Set spawn point |
| `/ph scan [arena]` | Auto-detect valid props |
| `/ph enable <arena>` | Enable arena |
| `/ph disable <arena>` | Disable arena |
| `/ph forcestart <arena>` | Force start game |
| `/ph forcestop <arena>` | Force stop game |
| `/ph reload` | Reload configuration |

See [COMMANDS.md](docs/COMMANDS.md) for complete command reference.

---

## Configuration

### config.yml

```yaml
# Game Settings
game:
  min-players: 6
  max-players: 20
  team-ratio:
    props-percentage: 70

  timers:
    hiding-phase: 45      # Seconds for props to hide
    hunting-phase: 300    # Seconds for hunting (5 min)

# Prop Settings
props:
  health:
    small: 8              # 4 hearts
    medium: 14            # 7 hearts
    large: 20             # 10 hearts
  change-cooldown: 45     # Seconds between prop changes

# Hunter Settings
hunters:
  wrong-hit-damage: 2     # Hearts lost for hitting real blocks
  attack-cooldown: 1.5    # Seconds between attacks

# Taunt Settings
taunts:
  enabled: true
  intervals:
    normal: 30            # Seconds between forced taunts
```

See full configuration options in `config.yml` after first run.

---

## Permissions

| Permission | Description | Default |
|------------|-------------|---------|
| `prophunt.play` | Basic gameplay | Everyone |
| `prophunt.admin` | Admin commands | OP |
| `prophunt.bypass.full` | Join full games | OP |

---

## Arena Setup Guide

### Requirements for a Valid Arena

- [ ] Arena region defined (play area boundaries)
- [ ] Lobby spawn point set
- [ ] Hunter cage/waiting area defined
- [ ] At least 1 prop spawn point
- [ ] At least 1 hunter spawn point
- [ ] At least 5 valid prop types

### Tips for Good Arenas

1. **Size Matters** - Not too big (hunters can't find anyone) or small (too easy)
2. **Varied Environment** - Mix of block types for prop variety
3. **Multiple Hiding Spots** - Nooks, crannies, furniture
4. **Clear Paths** - Hunters need to be able to move around
5. **Good Lighting** - Dark arenas favor props too much
6. **Fitting Props** - Use `/ph scan` to auto-detect blocks in your build

### Recommended Arena Sizes

| Players | Size (blocks) |
|---------|---------------|
| 6-8 | 30x30 to 40x40 |
| 8-12 | 40x40 to 50x50 |
| 12-16 | 50x50 to 60x60 |
| 16-20 | 60x60 to 80x80 |

---

## Scoring System

### Props

| Action | Points |
|--------|--------|
| Survive full game | +100 |
| Per 30 seconds survived | +10 |
| Voluntary taunt | +15 |
| Last prop standing | +50 |
| Escape after being hit | +20 |

### Hunters

| Action | Points |
|--------|--------|
| Find prop (first hit) | +25 |
| Kill prop | +50 |
| First blood | +25 |

---

## FAQ

**Q: Can props fight back?**
A: No, props cannot attack. Your only defense is hiding and running.

**Q: What happens if I disconnect?**
A: You're eliminated from the game. Rejoin to play the next round.

**Q: Can I use custom blocks as props?**
A: Props are limited to blocks that exist in the arena. Use `/ph scan` to detect them.

**Q: How do I change my prop after selecting?**
A: Press the prop selector item again, but you have limited changes and a cooldown.

**Q: Why do I keep making sounds as a prop?**
A: Forced taunts prevent indefinite camping. They're part of the game balance!

**Q: Can hunters see prop nametags?**
A: No, nametags are hidden for disguised props.

---

## Troubleshooting

### Props Not Spawning Correctly

1. Check that BlockDisplay entities are enabled in server
2. Ensure arena has valid prop spawn points
3. Run `/ph debug entities` to check for issues

### Game Won't Start

1. Verify minimum player count
2. Check arena is enabled: `/ph list`
3. Ensure all setup requirements met: `/ph setup <arena>`

### Players Can't Join

1. Check permissions: `prophunt.play`
2. Verify arena isn't full or disabled
3. Check for error messages in console

---

## Support

- **Issues:** [GitHub Issues](#)
- **Discord:** [Join our Discord](#)
- **Documentation:** [Full Docs](docs/)

---

## Contributing

Contributions welcome! Please read our contributing guidelines before submitting PRs.

---

## License

This project is licensed under the MIT License - see [LICENSE](LICENSE) for details.

---

## Credits

- Inspired by the classic Garry's Mod Prop Hunt
- Built for the Minecraft community

---

*Happy Hunting! (or Hiding!)*
