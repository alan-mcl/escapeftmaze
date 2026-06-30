# The Maze RPG System - Rule Reference

> A tabletop-style rulebook for the game system underlying *Escape From The Maze*.
> The rules below are the system the game engine actually implements; they are
> written here as if for a human referee and players. For the data structures that
> store this content, see [data_dictionary.md](data_dictionary.md); for the engine
> that runs it, see [architecture.md](architecture.md).

---

## Contents

1. [Core Mechanics](#1-core-mechanics)
2. [Characters: Attributes & Resources](#2-characters-attributes--resources)
3. [Skills (Modifiers)](#3-skills-modifiers)
4. [Character Creation](#4-character-creation)
5. [Races, Classes & Genders](#5-races-classes--genders)
6. [Advancement: Experience, Levels & Practice](#6-advancement-experience-levels--practice)
7. [Combat](#7-combat)
8. [Magic](#8-magic)
9. [Conditions & Status Effects](#9-conditions--status-effects)
10. [Equipment](#10-equipment)
11. [Exploration, Time & Survival](#11-exploration-time--survival)
12. [Locks, Traps & Skulduggery](#12-locks-traps--skulduggery)
13. [Encounters, NPCs & Trade](#13-encounters-npcs--trade)
14. [Difficulty Modes](#14-difficulty-modes)
15. [Quick Reference Tables](#15-quick-reference-tables)

---

## 1. Core Mechanics

The Maze is a party game: you control a band of up to **six** adventurers exploring
a first-person maze of zones, fighting in **phased, turn-based combat**.

### Dice

Dice are written in standard notation: `NdM+K` means roll `N` dice of `M` sides and
add `K` (for example `3d6+2`). A die roll is never less than **0** after modifiers.

### The percentile test

Almost every uncertain action is resolved by a **d100 (percentile) test**:

> Roll d100. The action **succeeds if the roll is less than or equal to the target
> number** (a percentage chance).

To-hit, criticals, dodge, parry, riposte, saving throws, resistances, hiding,
fleeing, stealing and lock-picking all use this test. Where two parties oppose each
other, each typically rolls a die (often d10 or d20) and adds the relevant skills;
the higher total wins.

### Turns and time

Time passes in **turns**. There are **300 turns per day**. Every step you take (even
walking into a wall) costs one turn, during which conditions tick, wounds slowly
mend, and the world acts. Combat is fought in **rounds**, which are a finer division
of a single turn.

---

## 2. Characters: Attributes & Resources

### The six attributes

Every character is defined by six core **attributes**. They rarely change and form
the foundation of everything else.

| Attribute | Governs |
|-----------|---------|
| **Brawn** | Physical power and toughness. Adds to melee damage (+Brawn/2), helps force doors and tear free of webs, and powers armour penetration. |
| **Skill** | Weapon proficiency and coordination. Feeds attack, defence and initiative. At **Skill 15+** you gain a bonus attack. |
| **Thieving** | Skulduggery: the basis of lock-picking, trap work, and theft. |
| **Sneaking** | Stealth and skulking. Drives hiding and ambush; degraded by heavy loads. |
| **Brains** | Intelligence. Reduces spell failure and **adds its value to Magic Points gained at each level**. |
| **Power** | Raw magical force. Strengthens your spells against enemy resistance and your own resistance to magic. |

Attributes are capped per race by **attribute ceilings** (typically 10, varying by
race). Magic-dead races pay double to raise Power.

### The three resources

| Resource | Pool | Notes |
|----------|------|-------|
| **Hit Points (HP)** | Life and stamina | Tracked as current / maximum / **sub** (fatigue threshold). |
| **Action Points (AP)** | Stamina for special exertion | Spent by dodges, spells, special abilities, and ambush attacks. |
| **Magic Points (MP)** | Spell fuel | Spent to cast spells. |

**Fatigue and consciousness.** Hit Points carry a hidden **sub** value representing
accumulated fatigue. You are **conscious only while current HP is greater than your
sub value**. Swinging a weapon raises fatigue (+1 sub per attack unless the weapon
is "tireless" for you); defending or doing nothing in combat lowers it.

- **Knocked out:** if fatigue catches up to your wounds (sub ≥ current, but HP still
  above 0), you fall unconscious (the *Fatigue KO* condition). Each turn you have a
  **50% chance** to come round once rested enough.
- **Death:** when current HP drops to **0 or below**, you die - unless a special
  ability intervenes (see *Die Hard* and *Cheat Death*, [§7](#7-combat)).

---

## 3. Skills (Modifiers)

Beyond the six attributes, characters have a broad list of trainable **skills**
(internally, all attributes, skills, statistics and traits are "modifiers"). The
player-trainable skills divide into three disciplines.

### Combat skills

Each weapon uses a particular attack motion; your skill in that motion improves your
chance to hit with it.

| Skill | Used by |
|-------|---------|
| **Swing** | Wide slashing weapons (axes, scimitars, maces) |
| **Thrust** | Spears, staves, two-handed swords |
| **Cut** | Sword edge strokes |
| **Lunge** | Sword points, daggers |
| **Bash** | Blunt weapons |
| **Punch** | Unarmed strikes with the hand/arm |
| **Kick** | Unarmed strikes with the foot/leg |
| **Throw** | Thrown weapons |
| **Shoot** | Bows and crossbows |
| **Fire** | Modern projectile/energy weapons |
| **Dual Weapons** | Fighting with a weapon in each hand |
| **Chivalry** | The Chivalry weapon discipline (broadswords, lances) |
| **Kendo** | The Kendo discipline (katana, wakizashi) |

### Stealth skills

| Skill | Use |
|-------|-----|
| **Streetwise** | Stealth in cities and buildings |
| **Dungeoneer** | Stealth in caves and tunnels |
| **Wilderness Lore** | Stealth in forests, plains, hills |
| **Survival** | Stealth in deserts, tundra, hostile wastes |
| **Backstab** | Melee strikes from hiding |
| **Snipe** | Missile strikes from hiding |
| **Locks and Traps** | Picking locks, disarming traps |
| **Steal** | Pickpocketing and shoplifting |
| **Martial Arts** | Unarmed combat and martial weapons |
| **Melee / Thrown / Ranged Criticals** | Chance to land instant-kill critical hits with that weapon class |
| **Scouting** | Finding hidden items and secret doors |

The four terrain stealth skills (Streetwise, Dungeoneer, Wilderness Lore, Survival)
each apply in their own kind of terrain - this matters for ambush and hiding.

### Magic skills

Spell-casting depends on the **components** a spell uses and on lore skills.

| Skill | Use |
|-------|-----|
| **Chant** | Chanted invocations |
| **Rhyme** | Verse components |
| **Gesture** | Hand signs |
| **Posture** | Postures and ritual dance |
| **Thought** | Purely mental casting |
| **Herbal** | Herbal reagents; brewing potions |
| **Alchemic** | Mineral reagents; alchemy |
| **Artifacts** | Identifying and using magic items |
| **Mythology** | Identifying monsters |
| **Smithcraft (Craft)** | Forging magical arms and armour |
| **Power Cast** | Pushing spells through enemy resistance |
| **Engineering** | Building and using gadgets |
| **Music** | Magical instruments and scores |

### Statistics and properties (derived)

Many further "modifiers" are **derived statistics** you do not raise directly -
**Initiative, Attack, Defence, Damage**, the **To/Vs Penetrate** values, resistances,
spell-casting levels, regeneration rates - and a large body of **properties**:
immunities, parry/riposte chances, favoured-enemy bonuses, "tireless" weapon traits,
and so on. These come from your race, your class level abilities, your equipment, and
active conditions rather than from point spending.

---

## 4. Character Creation

A new adventurer is built through a short wizard. (In the game's fiction, characters
are recruited at a guild for **20,000 gold**.)

1. **Race & Gender.** Pick a race and a gender it permits. Some races are locked
   until unlocked in configuration.
2. **Class.** Choose from the classes your race and gender allow. Class sets your
   focus (Combat, Stealth, or Magic), your base resources, starting skills, which
   skills are **active**, and your spell-book access.
3. **Starting Kit.** Choose a starting equipment kit for your class (a few races,
   such as Dryad and Faerie, supply their own kits that override the class kit). Only
   the kit bonuses matching your class focus apply.
4. **Spells.** If your class can cast at level 1, pick your starting spells with the
   spell picks your class grants.
5. **Personal details.** Choose a personality (flavour and speech only), a portrait,
   and a name.

Your starting numbers come from the combination of **class** base values, **race**
percentages and modifiers, your **kit**, your level-1 **class abilities**, and any
**starting spells** - there is no free point-buy of skills at creation in play.

### Suggested party balance

A well-formed party of six is expected to field at least: **two** combat characters,
**one** stealth character, **one** magic user (not magic-dead), **one** healer (a
spell that heals), **one** character with Locks and Traps active, and no two
characters of the same class.

---

## 5. Races, Classes & Genders

### Genders

**Male**, **Female**, and **None**. In the default campaign genders carry no stat
differences, but two spell traditions are gender-locked (Red is male-only, Purple is
female-only).

### Races

Fourteen races are defined. Percentages are the fraction of a class's base HP/AP/MP
the race receives.

| Race | HP% | AP% | MP% | Genders | Hallmarks |
|------|-----|-----|-----|---------|-----------|
| Human | 80 | 50 | 70 | F, M | Versatile all-rounder |
| Elf | 40 | 70 | 90 | F, M | Immune to disease & possession |
| Dwarf | 90 | 60 | 50 | F, M | Damage resistant |
| Goblin | 60 | 80 | 60 | F, M | Initiative bonus |
| Hynobi | 60 | 60 | 80 | F, M | Fire immune; Fire Breath ability |
| Mirka | 90 | 70 | 40 | F, M | Cold immune; berserk rage |
| Mata | 100 | 100 | 0 | None | **Magic-dead** machine; many immunities |
| Triton | 70 | 50 | 80 | F, M | Cold/paralysis resist; amphibious |
| Naga | 70 | 50 | 80 | F, M, None | Poison resist; Poison Spit; limited armour |
| Neotroll | 100 | 40 | 60 | F, M | Enhanced regeneration |
| Pasht | 60 | 90 | 50 | F, M | *Locked*; dodges melee |
| Gnome | 60 | 70 | 70 | F, M | *Locked*; extra party gold |
| Faerie | 20 | 80 | 100 | F, M | Own Gold-magic source; tiny; fast MP regen |
| Dryad | 20 | 90 | 90 | F, M | Plant resistances; Green-magic source; own kits |

Races also grant one-time **starting modifiers**, always-on **constant modifiers**,
party-wide **banner modifiers**, and their **attribute ceilings**. Magic-dead races
(Mata) cannot take a magic-focus class.

### Classes

Twenty-six classes are available, grouped by focus. All use the standard ("unity")
experience table and grant 3 assignable skill points per level.

- **Combat focus (10):** Amazon, Berserker, Blackguard, Hero, Monk, Paladin, Samurai,
  Shaman, Skald, Sohei.
- **Stealth focus (9):** Burglar, Courtesan, Exorcist, Gadgeteer, Ninja, Ranger,
  Shugenja, Troubadour, Warlock.
- **Magic focus (7):** Acolyte, Druid, Elemental, Enchanter, Priest, Sorcerer, Witch.

Your **focus** affects how fast you gain attacks with level (combat fastest), your
resource dice, and which starting-kit bonuses apply. Each class grants a cumulative
list of **level abilities** (skills, special abilities, and spell picks) as it rises,
up to class level **20**.

### Personalities

Seventeen personalities (Cunning, Faithful, Grunt, Gung Ho, Killer, Mystic, Peer,
Pro, Sage, Scholar, Sensitive, Team Player, The Silent Type, Valiant, Veteran,
Wayfarer, Wit) colour a character's voice and barks. They have no mechanical effect
on stats.

---

## 6. Advancement: Experience, Levels & Practice

### Experience and leveling

Characters earn **experience (XP)** from kills and deeds; a character's focus can add
a percentage bonus to XP earned. When XP reaches the next threshold, that character
may **level up** (manually, while exploring).

Standard XP thresholds (the "unity" table):

| Level | Total XP | Level | Total XP |
|-------|----------|-------|----------|
| 2 | 800 | 12 | 96,800 |
| 3 | 2,600 | 13 | 119,600 |
| 4 | 5,600 | 14 | 145,600 |
| 5 | 10,000 | 15 | 175,000 |
| 6 | 16,000 | 16 | 208,000 |
| 7 | 23,800 | 17 | 244,800 |
| 8 | 33,600 | 18 | 285,600 |
| 9 | 45,600 | 19 | 330,600 |
| 10 | 60,000 | 20 | 380,000 |
| 11 | 77,000 | each level after | +120,000 |

### What a level grants

Automatically on level-up: a roll of your class **HP**, **AP** and **MP** dice (MP
also adds your current **Brains**), plus any automatic class modifiers and new spell
picks.

You then **choose one** bonus from a menu, which may include:

- +3 Hit Points, +3 Action Points, or +3 Magic Points
- +1 to an attribute (if below its racial ceiling)
- +2 assignable skill points
- +1 spell pick
- Unlock a new skill (from your class's unlockable list)
- Revitalise (full HP/AP/MP and clear most conditions)
- Change class, or upgrade a signature weapon (when eligible)

Finally you **spend assignable points** (3 per level plus any bonus) on your
**active** skills, at most **+2 to any single skill** per level. The cost to raise a
skill by 1 is `1 + (current value / ceiling)`, so high skills cost more (magic-dead
races pay double for Power).

### Karma

Characters track **karma**, a moral/reputation score starting at 0. Honourable deeds
raise it by 1; dishonourable deeds (threats, bribes, theft) lower it by 1.
Characters with a **Code of Honour** lose XP and karma when the party acts
dishonourably; karma also gates certain divine magic outcomes.

### Practice

Skills also improve through **use**. Using an active skill in play earns practice
points toward it; when you accumulate enough, the skill rises by 1 (carryover
applies). Only player characters practice, and only **active** skills.

Practice points needed for the next +1:

| Current skill value | Points needed |
|--------------------|---------------|
| below 0 | 75 |
| 0-9 | 100 |
| 10-19 | 200 |
| 20-29 | 300 |
| n (general) | `((value / 10) + 1) x 100` |

---

## 7. Combat

Combat is fought in **rounds**. Each round: every combatant declares an **intention**,
intentions become ordered **actions**, and the actions resolve one at a time.

### Initiative and turn order

At the start of each round, every living combatant rolls initiative:

> **Initiative = 1d6 + Skill + Initiative modifier**

(The Initiative modifier folds in encumbrance penalties, weapon balance, haste/slow,
and so on.) Actions are then sorted first by **stance priority**, then by initiative.

**Stances** (chosen before combat) set how early you act:

| Stance | Priority | Note |
|--------|----------|------|
| Snakespeed | 12 | First action only; later actions drop to Act Early |
| Act Early | 8 | Default |
| Act Late | 4 | |
| Patience | 2 | Requires the Patience trait |
| Unaware | 1 | Unconscious / surprised |
| Dead | 0 | |

When a character makes several attacks in a round, each successive attack suffers a
**-5 initiative** penalty, so volleys spread across the round.

### Attacking: the to-hit roll

The chance to hit starts from the **level difference** between attacker and defender:

| Atk level - Def level | Base hit % | | Atk level - Def level | Base hit % |
|---|---|---|---|---|
| -5 or worse | ~1% | | +1 | 54% |
| -4 | 6% | | +2 | 62% |
| -3 | 26% | | +3 | 74% |
| -2 | 38% | | +4 | 94% |
| -1 | 46% | | +5 | 99% |
| 0 | 50% | | +6 or better | ~100% |

Then add the attacker's bonuses and subtract the defender's:

> **Hit% = base(level diff) + Attacker bonuses - Defender bonuses** (clamped 1-99)

- **Attacker bonuses:** Skill, the relevant weapon skill (Cut, Swing, Backstab,
  etc.), Attack, the weapon's to-hit, plus extras (ranged Deadly Aim x10, Master
  Archer, favoured-enemy x10, etc.).
- **Defender bonuses:** Skill, Defence and body-part defence (and Skill again if able
  to move; an immobile defender loses their Defence entirely).

Roll d100 against the result. Even a "hit" may be turned aside by the defender's
reactions, checked in order: **surprise riposte/parry**, **dodge** (costs 1-2 AP),
**arrow cutting/catching** (vs missiles), **parry**, then **riposte**.

### Damage

A landed blow rolls damage:

> Damage = weapon roll + ammo roll + (Brawn / 2) + body-part & Attack damage mods 
>          + favoured-enemy bonus - armour soak, then **x damage multiplier**.

- **Armour soak:** for each of worn armour, the struck body part, and any shield,
  roll d100 against that layer's prevention chance (reduced by the weapon's
  penetration, the attacker's Brawn and To Penetrate). On success the layer subtracts
  its prevention value. Shields add Chivalry to the block and may trigger a **shield
  bash** counter.
- **Multipliers** stack from the body part, attacker, attack type, and "bane/slayer"
  weapons against a hated foe type. **Backstab and snipe attack types double damage.**
- **Resistances:** the defender then reduces damage by their percentage resistance to
  the damage's type; **immunity** negates it entirely.

### Critical hits

After damage, an attack may be a **critical** (instant kill): roll d100 against the
weapon's critical chance plus the relevant Criticals skill. The target may resist
with a saving throw vs criticals; failing it sets their HP to 0. A **Finisher**
auto-criticals a helpless foe.

### Actions and Action Points

In a round a combatant may **Attack, Defend, Hide, Cast a Spell, use a Special
Ability, Use an Item, Equip, Run Away**, or attempt skulduggery (disarm, pick, force,
open chest) or social actions against NPCs. Conditions (fear, possession, silence...)
may override your choice.

- Ordinary weapon **attacks cost no AP** - the real limits are fatigue and initiative.
- **Backstab/Snipe** cost `3 + the target's Vs Ambush` AP.
- **Spells and special abilities** cost AP (and MP/HP) per the spell.
- **Dodging** costs 1 AP (with Acrobatics) or 2. A successful **Hide** refills AP to
  maximum. **Defending / doing nothing** recovers fatigue.

### Number of attacks and strikes

**Attacks per round** start at `1 + level/divisor` (divisor 10 for combat focus, 15
stealth, 20 magic), plus bonuses: +1 at Skill 15+, +1 for a Kendo weapon at Kendo 5+,
weapon bonus attacks, and more. **Dual-wielding** adds off-hand attacks (half your
primary count, minimum 1) at a to-hit penalty (-5 primary / -10 secondary, offset by
the Dual Weapons skill). Each attack may land several **strikes** (`1 + bonus +
weaponSkill/9`, capped at 3), each rolled separately.

### Engagement range

Foes fight in **groups** at ranges from melee (front groups) out to long range.
Front-row characters reach the nearer groups; back-row reach is one step shorter.
Your weapon's minimum/maximum range must match the target's range band.

### Ambush, surprise & fleeing

Before combat, each side's **stealth value** (lowest terrain-stealth skill in the
group, adjusted for numbers) plus 1d10 is compared:

- Lead by **20+**: that side may **ambush or evade**.
- Lead by **10+**: that side may **ambush**.
- Otherwise: normal combat.

The ambushing side gains a surprise round with bonuses (Ambusher value added to
Attack, Backstab, Snipe, Damage, Penetrate and Criticals).

**Fleeing.** An individual flees on `d100 <= base + To Run Away - number of
opponents` (base 50 for heroes, 80 for foes). The whole party flees on
`d100 <= 75 + total To Run Away - living foes`.

### Surviving death

- **Die Hard:** the first time you would drop to 0 HP in a round, you cling to **1
  HP** until the round ends.
- **Cheat Death:** roll `d100 <= min(80, 10 + level + Brawn + Power + current MP +
  maxHP/10)`; on success you snap back to 20-50% HP with AP and MP emptied.

---

## 8. Magic

### The seven colours

Magic is divided into seven **colours**, each a tradition with its own spell book:

| Colour | Flavour | Restriction |
|--------|---------|-------------|
| **Red** | Runes, wind, fire, sun; curses and evocations | Male only |
| **Black** | Curses, terror, necromancy, illusion | - |
| **Purple** | Earth, sea, moon; charms and illusions | Female only |
| **Gold** | Faerie enchantment, binding, protection | - |
| **White** | Holiness, healing, blessing | - |
| **Green** | Plants, animals, natural forces; healing & shape-changing | - |
| **Blue** | The four elements; potent and dangerous | - |

Spells also belong to one of seven **schools** (Blessing, Curse, Conjuration,
Evocation, Illusion, Transmutation, Beguilement).

### Casting cost and casting level

A spell may cost any mix of **HP, AP and MP**, paid when it is cast. You also choose a
**casting level** from **1 to 7**. Any part of a spell marked to "scale with casting
level" is multiplied by it - so casting higher costs more (often MP = base x level)
but hits harder, lasts longer, and is harder to resist. You can only raise the
casting level while you can still afford the next level's cost.

### Generating and spending magic

To cast a colour's spell you must have enough of that **colour's magic present** -
your generation modifier for that colour (Red through Blue). This comes from your
race, class, equipment and banners, plus the **current tile's** ambient magic (capped
at 13 per colour) and any conditions. A spell lists **requirements to cast** (minimum
colour magic, e.g. "2 Red") and **requirements to learn**. Casters with access to a
colour can also draw on the party's **magic circle** to boost Power Cast.

### Components and failure

Each spell names a **primary** (and sometimes **secondary**) casting modifier - one
of Chant, Gesture, Thought, Herbal, and so on. These determine **spell failure**:

> difficulty = (spell level + casting level) x 2
> caster total = level/2 + primary skill (or the average of primary & secondary)
> failure% = 15 x (difficulty - caster total), reduced by Brains
> (negative Brains makes it much worse)

If the failure roll lands, the spell either **fizzles** (lost, no effect) or
**backfires** (re-targets against your own side). Spells with no primary modifier
never fail.

### Wild magic

Some spells are **wild**: a d100 roll shifts an index (low rolls shift down, high
rolls up) into a 10-entry table, and you actually cast whatever spell that index
names - re-targeted as needed.

### Targeting

Spell target types include: **Caster**, **Ally**, **Party**, **Party but not
Caster**, **Foe**, **Foe Group**, **All Foes**, **Tile**, **Lock or Trap**, **NPC**,
**Item**, and persistent **Clouds** (one group or all groups). Spells are also gated
by when they may be cast: any time, combat only, non-combat only, NPC only, locks &
traps only, or inventory only.

### Effects, saves and resistance

A spell carries one or more **effects**, each with a damage/effect **type** (Fire,
Water, Earth, Air, Mental, Energy, Bludgeoning, Piercing, Slashing) and a **subtype**
(Heat, Cold, Poison, Disease, Curse, Acid, Lightning, Psychic, ...). Each type maps
to a resistance; each subtype to a possible immunity.

Against an **unwilling** target, roll a **saving throw**:

> resistance = max(0, target's resistance - attacker Power - 2x Power Cast - 5x
> favoured-enemy)
> then subtract the spell's save adjustment and the casting level
> **Save succeeds if d100 <= resistance** (a save usually means the lesser/"saved"
> result, or no effect at all for resistant saves)

Higher casting levels lower the target's effective resistance (harder to save).
Willing targets (your own buffs) never roll. **Projectile** spells must first land a
hit (using Power Cast) before effects apply.

Effects produce **results**: direct **damage** (optionally drained to the caster),
**healing**, applying or removing **conditions**, **summoning** monsters, **instant
death**, **resurrection**, **charm**, item creation/identification/recharge,
unlocking, theft, mind-reading, and many campaign-specific outcomes (see the
data dictionary for the full result catalogue).

### Learning spells

Spells are learned with **spell picks** granted by class level abilities. You can
learn a spell of a colour if your **casting ability** in that colour (e.g. Green
Magic 3 lets you learn Green spells up to level 3) is high enough, you meet its learn
requirements, and you do not already know it. Spellbook items can teach spells too.
Magic-dead races cannot learn spells and cannot take magic classes.

---

## 9. Conditions & Status Effects

A **condition** is a temporary state on a character, monster, or even a map tile. Each
has a **duration**, a **strength**, and may deal HP/AP/MP/stamina damage each turn or
apply stat modifiers while active.

### How conditions end

- **Duration expires:** counts down each turn (strength may also wane).
- **Chance each turn:** some end on a per-turn random check.
- **Never:** some (petrification, possession) do not end on their own.
- **Shed Blights / cures:** afflictions (curse, poison, disease) can be thrown off by
  the *Shed Blights* trait or removed by spells; *Revitalise* clears most conditions
  (but not stone, possession, invisibility, gaseous form, or swallow).

A new condition only replaces an existing one of the same kind if it is **stronger**;
poison, disease and hex may **stack**. **Immunity** to a condition blocks it outright.
Afflictions on a character start **unidentified** unless the character has
Self-Awareness.

### Common conditions

| Condition | Effect |
|-----------|--------|
| **KO / Fatigue KO** | Unconscious, helpless, takes no actions |
| **Fear** | -5 Attack/Defence; may cower, freeze, or flee |
| **Sleep** | Helpless; a hit wakes you (chance = damage x10%, max 99%) |
| **Poison** | Per-turn HP damage; may stack |
| **Disease** | Lingers indefinitely; can spawn further ailments; slow self-cure |
| **Paralyse** | Helpless, as KO |
| **Blind** | -15 Attack/Defence and stumbling, unless Blind Fighting |
| **Web** | Stuck until you tear free (Brawn + d4 vs strength + casting level + d4) |
| **Stone** | Petrified: helpless, but hugely resistant; does not end naturally |
| **Possession** | Turned against your allies |
| **Invisible** | +Sneaking/+flee, untargetable until you act or are hit |
| **Blink / Swallow** | Removed from play (blinking out, or swallowed whole) |
| **Haste / Slow** | Initiative and action changes |
| **Nausea / Irritate / Insane** | Escalating penalties and erratic behaviour |
| **Silence** | Cannot cast (spells are silenced) |
| **Hex** | Stacking debuff |
| **Berserk / Bloodthirsty** | Forced aggression with combat bonuses |
| **Gaseous Form** | +Defence, immune to physical, vulnerable to air; cannot attack |
| **Regeneration / Bless / Mage Armour** | Beneficial auras (defined by template) |

Many beneficial states (bless, regeneration, mage armour) are "untyped" conditions
whose entire effect is a bundle of stat modifiers.

---

## 10. Equipment

### Item types

Items fall into these categories: short / extended / thrown / ranged **weapons**,
**ammunition**, **shield**, **torso / leg / head armour**, **gloves**, **boots**,
**misc** and **banner** equipment, **misc magic**, **potion**, **bomb**, **powder**,
**spellbook**, **scroll**, **food**, **drink**, **key**, **writing**, **gadget**,
**musical instrument**, **money**, and **supplies**.

### Equipment slots

Each character has **ten** equipment slots: primary weapon, secondary weapon, helm,
torso armour, leg armour, gloves, boots, a banner item, and two misc items. Items
declare which slots they fit, and may carry class/race/gender or stat requirements to
equip or use.

### Weapons

Weapons carry damage dice and a damage type, the attack motions they use, to-hit,
penetration, critical and initiative modifiers, and a range band (melee, extended,
thrown, long). They may be **two-handed**, **returning**, **backstab-** or
**snipe-capable**, carry bonus attacks/strikes, slay a particular foe type, or apply
spell effects on hit. **Wielding combos** reward specific primary+secondary pairings
(dual swords, spear+shield, sword+pistol) with extra bonuses. Monsters use **natural
weapons** (claws, bites) that work like weapons but are innate.

### Armour, shields and ammunition

Armour and shields provide a **damage-prevention** amount and a **chance** to apply
it. Ranged weapons consume **ammunition** of a matching type (arrow, bolt, stone,
shot, dart, etc.); some thrown weapons return to hand.

### Charges, curses and identification

- **Charges:** wands, potions and the like have charges. Spent items may be
  destroyed (fatal), remain empty (non-fatal), or be infinite. Charges can be
  restored by recharge magic if you beat the item's recharge difficulty.
- **Curses:** cursed items reveal themselves when equipped and **cannot be removed**
  until the curse is lifted.
- **Identification:** unidentified items show only a vague name. The best party member
  rolls `level + Artifacts` against the item's identification difficulty.
- **Invocation:** many items cast a built-in spell when used.

### Crafting

With the right skills you can **craft** items (combine two ingredients into a result)
or **disassemble** items with a disassembly table into components.

---

## 11. Exploration, Time & Survival

### Movement and time

You explore a grid of tiles in first person. **Each step costs one turn**, and there
are **300 turns per day**. Walking into a wall still costs the turn.

### Terrain

Every tile has a **terrain type** that determines which stealth skill applies there:

| Terrain | Stealth skill |
|---------|---------------|
| Urban | Streetwise |
| Dungeon | Dungeoneer |
| Wilderness | Wilderness Lore |
| Wasteland | Survival |

Tiles may also carry ambient magic, a random-encounter chance, and resting danger
and efficiency ratings.

### Party resources

The party shares pools of **gold** and **supplies**. Money and supply items convert
into these pools automatically when picked up.

### Resting

Resting is a **100-turn camp** that restores HP, AP and MP - at a price.

- **Supplies:** each character needs `2 + Supply Consumption` supplies. If the party
  is short, rations are split.
- **Danger:** at intervals during the rest, roll `d100 <= resting danger` for the
  tile (None 0%, Low 10%, Medium 25%, High 50%, Extreme 100%). On a hit, you are
  ambushed by the tile's encounter table. *Guard Duty* halves this chance.
- **Recovery:** a tile's **resting efficiency** sets how much you recover - Poor 20%,
  Average 40%, Good 60%, Excellent 100% of each pool over the full rest. Short
  rations reduce recovery proportionally; an *Entertainer* improves it.

Outside of resting, characters slowly regenerate based on their regeneration rates.

### Encumbrance

Carrying capacity is roughly `50,000g + level x1,000 + Brawn x2,000 + Thieving x50 +
Wilderness Lore x100 + Survival x100`. Heavier loads bring penalties:

| Load | Effect |
|------|--------|
| up to 50% | none |
| 51-75% | minor (-2 Initiative, -2 Sneaking) |
| 76-100% | heavy (-5 Initiative, -5 Attack/Defence, big stealth & reaction penalties) |
| over 100% | crushing (four times the heavy penalties) |

### Identifying monsters

In combat, the best party member can identify foes: `level + Mythology + 2x
favoured-enemy`, improving each round, against the foe's identification difficulty.
Success identifies the whole group.

---

## 12. Locks, Traps & Skulduggery

### Tools

Locks and traps are worked with eight **tools**: chisel, crowbar, drill, hammer,
jackknife, lockpick, skeleton key, and tension wrench. Each lock or trap has a
difficulty per tool and a set of tools that actually form its mechanism.

### Inspecting and disarming

The core skulduggery contest is:

> **Your score = Thieving + Locks and Traps + d20**
> **Opposing score = difficulty(tool) + d20**

| Result | Condition |
|--------|-----------|
| Success (disarm/unlock) | your score >= opposing score |
| Nothing happens (safe) | your score >= opposing score - 10 |
| The trap springs | otherwise, or if you use the wrong tool |

Trap **inspection** uses the same contest and can report the truth, an "unknown", a
**false** reading, or spring the trap if you fail badly. Spell-based picking and
disarming substitute the spell's modifier for Thieving + Locks and Traps.

### Forcing doors

You can try to **force** a portal by brute strength:

> chance = min(99, 50 + Brawn - the door's resistance)

You must have enough HP to pay the fatigue cost; success forces the door (taking the
fatigue), failure may hurt you. Doors may instead need a **key** (sometimes consumed).

---

## 13. Encounters, NPCs & Trade

### Random encounters

While moving (or resting), if no NPC stands on the tile, roll against the tile's
encounter chance (per-mille while moving, percentile while resting); on a hit, foes
are drawn from the tile's encounter table.

### NPC attitudes

NPCs belong to **factions** with an attitude toward the party, on a ladder from
hostile to friendly:

> Attacking - Aggressive - Wary - Scared - Neutral - Friendly - Allied

Actions shift a faction's attitude up or down the ladder, affecting all its members.

### Social actions

| Action | Contest (you win on the higher total) | Notes |
|--------|----------------------------------------|-------|
| **Threaten** | level + Brawn + Skill + Threaten + d10 vs targetLevel x10 + d10 | Dishonourable; *Terrifying Reputation* auto-succeeds vs non-fearless |
| **Bribe** | gold/10 + To Bribe + d10 vs npcLevel x10 + resist + d10 | Dishonourable; gold spent first |
| **Steal** | level + Thieving + Steal + d10 vs level + resist + theftCount + d10 | Margin of 5 (hostile) or 10 (neutral); *Master Thief* never caught |
| **Give** | gift value vs leader level | Generous gifts improve attitude; trivial ones worsen it |

A successful steal grabs gold (about `d10% of stealable gold + Thieving + Steal +
d10`, capped at `level x250`) or a random item.

### Trade

NPCs **sell** to you at a markup and **buy** from you at a discount, both expressed as
percentages of an item's base cost (adjusted for enchantment and charges). The
**Barter Expert** trait improves both rates on big margins, and *Terrifying
Reputation* lowers prices when you buy.

---

## 14. Difficulty Modes

| Mode (in-game name) | Effect |
|---------------------|--------|
| **Normal** (Adventurer) | Baseline. Default mode. Loot x10. |
| **Easy** (Storyteller) | Foes weaker: -1 HP/AP/MP, -1 Attack/Defence, -5% resistances. Loot x15. |
| **Hard** (Dungeoneer) | Foes tougher: +1 HP/AP/MP, +1 Attack/Defence, +5% resistances. Loot x5. |
| **Heroic** (Heroic) | Foes scale with their level (bonuses to resources, accuracy, resistances, plus traits like Cheat Death at high level); +2 encounter chance. Loot x5. |

---

## 15. Quick Reference Tables

### Core rolls

| Action | Roll | Succeeds when |
|--------|------|---------------|
| Generic action | d100 vs target% | roll <= target |
| Initiative | 1d6 + Skill + Initiative | higher acts first (after stance) |
| To hit | d100 vs Hit% | roll <= Hit% (then defender reactions) |
| Saving throw | d100 vs adjusted resistance | roll <= resistance |
| Flee (solo) | d100 vs 50/80 + To Run Away - foes | roll <= chance |
| Skulduggery | Thieving + L&T + d20 vs difficulty + d20 | yours >= theirs (±10 partial) |
| Force door | d100 vs min(99, 50 + Brawn - resist) | roll <= chance |

### Key constants

| Constant | Value |
|----------|-------|
| Party size | up to 6 |
| Turns per day | 300 |
| Rest length | 100 turns |
| Max casting level | 7 |
| Max spell colours / schools | 7 / 7 |
| Tile magic cap per colour | 13 |
| Max class level (abilities) | 20 |
| Assignable points per level | 3 (+bonuses), max +2 per skill |
| Bonus attack at | Skill 15+ |
| Brawn damage bonus | + Brawn / 2 |
| Equipment slots | 10 |

---

*This reference describes the rules as implemented by the game engine. For the exact
formulas, source files and data structures behind each rule, consult
[architecture.md](architecture.md) and [data_dictionary.md](data_dictionary.md).*
