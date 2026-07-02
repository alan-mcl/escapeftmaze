# Magic System Reference

> Detailed reference for the seven-colour magic system in *Escape From The Maze*.
> For broader RPG rules (combat, advancement, conditions), see
> [rpg_system.md](rpg_system.md) §8; for persisted spell fields and serialisation,
> see [data_dictionary.md](data_dictionary.md).

---

## Contents

1. [System overview](#1-system-overview)
2. [The seven colours](#2-the-seven-colours)
3. [Spell compendium](#3-spell-compendium)

---

## 1. System overview

Magic in the Maze is organised around **seven colours**, each a distinct tradition
with its own spell book. Player characters learn spells from these books as they
advance; the same colour framework also governs foe spell-casting, item effects,
class abilities, and ambient magic on map tiles.

### Spell books and schools

Every spell belongs to exactly one **colour** (spell book) and one **school**:

| School | Typical role |
|--------|--------------|
| **Blessing** | Healing, protection, restoration |
| **Curse** | Afflictions, debilitation, necromancy |
| **Conjuration** | Summoning allies, walls, terrain effects |
| **Evocation** | Direct damage, elemental blasts |
| **Illusion** | Deception, stealth, sensory chaos |
| **Transmutation** | Physical transformation, buffs, terrain enchantment |
| **Beguilement** | Charm, mind control, social manipulation |

Some spells are shared across multiple books (for example *Charm*, *Haste*, or
*Soul Shield*); a character can only learn a shared spell from a book their class
grants access to.

### Casting costs and casting level

When a spell is cast, the caster pays any combination of **HP**, **AP**, and **MP**
defined on the spell. The caster also chooses a **casting level** from **1 to 7**
(`MagicSys.MAX_CASTING_LEVEL`). Any spell cost or effect marked to scale with
casting level is multiplied accordingly — higher casting levels cost more but hit
harder, last longer, and are harder to resist. The caster can only raise the level
while they can still afford the next step's cost.

### Colour magic present

To cast a spell, the caster must have enough **colour magic present** for each
colour listed in the spell's requirements. Present magic comes from:

- the caster's **generation** modifiers for each colour (Red through Blue);
- race, class, equipment, banners, and active conditions;
- the **current tile's ambient magic** (capped at 13 per colour);
- the party's **magic circle** for that colour (casters with book access can draw
  on the circle to boost Power Cast).

Requirements are expressed as pairs like "4 White" — the caster needs at least
that much present magic of the named colour at the moment of casting.

### Components and spell failure

Each spell names a **primary** (and sometimes **secondary**) casting modifier —
Chant, Gesture, Thought, Posture, Rhyme, Herbal, Alchemic, and so on. These feed
the spell failure check:

> difficulty = (spell level + casting level) × 2  
> caster total = level/2 + primary skill (or average of primary & secondary)  
> failure% = 15 × (difficulty − caster total), reduced by Brains  
> (negative Brains makes failure much worse)

On failure the spell either **fizzles** (lost, no effect) or **backfires**
(re-targeted against the caster's own side). Spells with no primary modifier
never fail.

**Brains** also adds to Magic Points gained at each level; **Power** strengthens
spells against enemy resistance and improves the caster's own magical resistance.

### Saving throws and resistance

Against an **unwilling** target, each effect rolls a saving throw:

> resistance = max(0, target resistance − attacker Power − 2× Power Cast − 5× favoured-enemy)  
> then subtract the spell's save adjustment and the casting level  
> **Save succeeds if d100 ≤ resistance**

A successful save usually applies a lesser result or no effect. Willing targets
(allied buffs) never roll. **Projectile** spells must first land a hit (using
Power Cast) before effects apply.

Effect types (Fire, Water, Earth, Air, Mental, Energy, and physical damage types)
map to resistances; subtypes (Heat, Cold, Poison, Disease, Curse, Acid, Lightning,
Psychic) map to possible immunities.

### Targeting and usability

Spell target types include: **Caster**, **Ally**, **Party**, **Party but not
Caster**, **Foe**, **Foe Group**, **All Foes**, **Tile**, **Lock or Trap**,
**NPC**, **Item**, and persistent **Clouds** (one group or all groups).

Spells are also gated by **when** they may be cast: any time, combat only,
non-combat only, NPC only, locks & traps only, or inventory screen only.

### Wild magic

Some spells are **wild**: a d100 roll shifts an index into a 10-entry table, and
the caster actually casts whichever spell that index names — re-targeted as needed.

### Learning spells

Characters learn spells with **spell picks** granted by class level abilities. To
learn a spell:

1. the character's **casting ability** in that colour must be at least the spell's
   level (e.g. Green Magic 3 allows Green spells up to level 3);
2. the character must meet the spell's **requirements to learn**;
3. the spell must appear in a book the character has access to;
4. the character must not already know it.

Spellbook items can also teach spells. **Magic-dead** races cannot learn spells or
take magic-focus classes.

Two traditions are **gender-locked**: **Red Magic** (male only) and **Purple Magic**
(female only). All other books are open to any gender permitted by race and class.

### Tile enchantment spells

Each colour includes a level-1 **ground enchantment** that permanently raises
ambient magic on the current tile: *Wave Of Heat* (Red), *Unholy Ground* (Black),
*Full Moon* (Purple), *Subtle Glamour* (Gold), *Holy Ground* (White),
*Verdant Growth* (Green), and *Crucible* (Blue).

---

## 2. The seven colours

The flavour text below is taken from the player spell book definitions in campaign
data (`data/default/db/playerspellbooks.json`).

### Red Magic — the right-handed path

> The right-handed path of magic, only available to male characters. This is the
> magic of runes, wind, fire and sun. Direct and powerful, it employs strong
> curses and the most dangerous evocations of any school.

Red casters are battle mages: force bolts, fireballs, lightning, and the dreaded
**Power Word** series that cripples or kills even when partially resisted. The
tradition peaks in runic evocations (*Rune Of Destruction*, *Rune Of Death*) and
transmutations that turn the caster into a tireless melee engine (*Mage Strength*,
*Majesty*). Red shares some destructive overlap with Black and Blue but favours
direct, masculine evocation over subtlety.

**Restriction:** male characters only.

### Black Magic — curses and the Pit

> The mastery of curses, terror and necromancy. Black magic users have access to
> strong illusions and evocations, and the strongest curses available to any school.

Black is the school of affliction: poison, disease, paralysis, fear, and instant
death curses stack with illusionary **Aspect** forms that disturb or terrify foes
struck in melee. Mid-level magic opens social manipulation (*Leverage*) and
spectral assassination (*Backstab*). At the apex, *Gates Of Hell* and *Vampirism*
embody the tradition's necromantic hunger.

**Restriction:** none (any gender).

### Purple Magic — the left-handed path

> The left-handed path of magic, available only to female characters. This is the
> magic of the earth and sea and moon, deep and subtle. Users can employ a variety
> of strong charms, illusions and curses.

Purple blends witchcraft, fey glamour, and lunar power. Witches heal and curse in
equal measure, conceal allies with *Blur* and *Mass Blur*, beguile with *Fey Voice*
and *Mortal Allure*, and unleash prismatic chaos (*Prismic Missile*, *Prismic
Chaos*). The tradition's signature banes — *Left Handed Bane* and *Right Handed
Bane* (Red) — mirror the gender-locked paths. *Unleash Power* and *Cloak Of Magic*
transform the caster into a formidable combatant wrapped in pure enchantment.

**Restriction:** female characters only.

### Gold Magic — the faerie plane

> Gold magic flows from the mystical plane of Faerie. This is the magic of
> enchantment, binding and protection. Its practitioners use the most potent
> blessings and the strongest charms and illusions of any school, but little else.

Gold excels at beguilement, theft, and illusion: invisibility, sleep, mesmerism,
NPC manipulation (*Beguilement*, *Mindread*, *Confidence Trick*), and battlefield
control (*Stasis*, *Pandemonium*). Offensive evocation is sparse; instead, shadow
warriors, phantasms, and prismatic bolts supplement a toolkit built around
outmanoeuvring and out-charming the enemy. Faerie fire and subtle glamour mark
targets and places touched by the fey.

**Restriction:** none (any gender).

### White Magic — holiness and healing

> The divine magic of holiness, healing and blessings. Other than the most potent
> healing and blessings of any school, it also offers strong transmutations and
> charms.

White is the primary healing tradition: light wounds through restoration and
resurrection, condition cures, party wards (*Armourplate*, *Magic Screen*,
*Soul Shield*), and undead/planar destruction (*Zap Undead*, *Astral Gate*). Divine
transmutations (*Superman*, *Divinity*, *Fervour*) and conjurations (*Summon Angel*,
*Falling Stars*) support both support and holy wrath roles.

**Restriction:** none (any gender).

### Green Magic — nature's power

> Power over plants, animals and natural forces. These casters can employ a variety
> of healing, blessing and shape changing spells, and have the strongest conjuration
> options.

Green druids summon the living world: vermin, plants, treants, wyrms, and nature
elementals. Entangling foliage, insect swarms, and blizzards control the
battlefield while healing and revitalisation sustain the party. *Force Of Nature*
caps the tradition by transforming the caster into an unstoppable avatar of the
wild. Green shares some elemental overlap with Blue but roots its power in biology
and terrain rather than raw elemental planes.

**Restriction:** none (any gender).

### Blue Magic — the four elements

> Blue magic users draw power from the four elements of earth, air, fire and water.
> They can employ extremely dangerous evocations, conjurations, and transmutations.

Blue is the elemental devastator: frost, fire, lightning, stone, wind, lava, and
vacuum. Conjured mephits, elementals, and at the pinnacle a **dragon** supplement
direct evocation. Protective magic (*Resist Elements*, *Element Shield*,
*Stoneskin*) helps the party survive their own cataclysmic spells. Several Blue
spells overlap with Red or Green lists at similar levels, reflecting shared
elemental phenomena accessed through different traditions.

**Restriction:** none (any gender).

---

## 3. Spell compendium

The tables below list every spell in the default campaign **player spell books**
(`playerspellbooks.json`), grouped by colour and spell level (1–7). Summaries are
taken from spell descriptions in `spells.json`. Several spells appear in more than
one book; each listing reflects the book from which a character of that tradition
would learn it.

Spells defined in `spells.json` but **not** in a player spell book — monster
abilities, item-granted effects, class kits, trap payloads, and bard instrument
variants — are omitted here.

### Red Magic spell list

#### Level 1

| Spell | School | Summary |
|-------|--------|---------|
| Bolt Of Force | Evocation | An invisible bolt of force strikes a single foe. |
| Bolt Of Static | Evocation | A crackling ball of electical energy speeds towards one of your foes. |
| Fear | Curse | Brings down a curse of fear on a whole group of foes. |
| Mage Shield | Evocation | A shield-shaped protective force field defends a single character. |
| Mental Attack | Beguilement | The opponents mind is directly attacked. |
| Wave Of Heat | Evocation | Enchants this tile to provide extra red magic. |

#### Level 2

| Spell | School | Summary |
|-------|--------|---------|
| Bolt Of Acid | Evocation | Casts a deadly bolt of corrosive acid at a single foe. |
| Bolt Of Fire | Evocation | Evokes a scorching bolt of flame towards one of your foes. |
| Identify | Transmutation | Attempts to identify a single item. |
| Mage Missiles | Evocation | Evokes a number of tiny magical missiles that each speed off towards a different enemy. |
| Sonic Boom | Evocation | Unleashes a thunderous sonic attack on all your foes, leaving them injured, scared,  and even unconscious. |
| Wall Of Force | Conjuration | Conjures an invisible wall of force around the party that deflects many incoming projectiles. |

#### Level 3

| Spell | School | Summary |
|-------|--------|---------|
| Ball Of Fire | Evocation | Consumes a group of your foes in a giant ball of fire. |
| Bolt Of Magma | Evocation | Lobs globe of bubbling magma at one of your foes. |
| Cone Of Cold | Beguilement | A cone of extreme cold sears a group of your enemies. |
| Knock Knock | Illusion | Attempts to open a lock or disarm a trap. |
| Mage Armour | Conjuration | Encases you in an multi-faceted invisible force field, reducing damage. |
| Mage Blades | Transmutation | Enchants the weapons of the whole party, imbuing them with added potence. |

#### Level 4

| Spell | School | Summary |
|-------|--------|---------|
| Ball Of Ice | Evocation | Batters your foes with an icy exploding ball. |
| Bomb Of Acid | Evocation | Coats a group of your foes with corrosive acid. |
| Invisible Stalker | Conjuration | Summons an invisible stalker to attack your foes. |
| Mage Strength | Transmutation | Transforms the sorcerer into a deadly, tireless melee combat machine. |
| Melt Armour | Transmutation | Softens up the armour of your foes. |
| Mental Blast | Beguilement | Flays one opponents mind, causing damage and insanity. |

#### Level 5

| Spell | School | Summary |
|-------|--------|---------|
| Bolt Of Lightning | Evocation | Unleashes a devastating lightning bolt on your foes. |
| Dehydrate | Transmutation | Inflicts heavy damage on a single foe. |
| Power Word Blind | Transmutation | Blinds one foe, causing damage even if resisted. |
| Power Word Mute | Curse | Silences one foe, causing damage even if resisted. |
| Power Word Stun | Beguilement | Stuns one foe, causing damage even if resisted. |
| Recharge | Evocation | Recharges one item. |

#### Level 6

| Spell | School | Summary |
|-------|--------|---------|
| Boiling Blood | Transmutation | The blood of one of your foes boils. |
| Power Word Kill | Curse | Kills one group of foes, causing damage even if resisted. |
| Power Word Stone | Transmutation | Petrifies one group of foes, causing damage even if resisted. |
| Rune Of Destruction | Evocation | You draw the deadly Rune of Destruction, glowing in the air before your foes... |
| Summon Demon | Conjuration | Summons a demon from the nether levels of hell and bins it to fight for you. |

#### Level 7

| Spell | School | Summary |
|-------|--------|---------|
| Concussion | Evocation | One foe is hit with a bolt of deadly concussive force. |
| Majesty | Transmutation | You are cloaked in might and majesty. |
| Nuclear Blast | Evocation | A fearsome fiery blast damages all your foes. |
| Right Handed Bane | Curse | You cast a deadly Right Handed curse at one of your foes. |
| Rune Of Death | Transmutation | You draw the infernal Rune of Death before your terrified foes... |

### Black Magic spell list

#### Level 1

| Spell | School | Summary |
|-------|--------|---------|
| Acid Splash | Evocation | Projects a globe of corrosive acid at a single target. |
| Fear | Curse | Brings down a curse of fear on a whole group of foes. |
| Minor Wound | Curse | This curse inflicts a minor wound on one opponent. |
| Paralysis | Curse | Casts a curse of paralysis at a single foe. |
| Poison | Curse | Inflicts a curse of poison on a single foe. |
| Unholy Ground | Transmutation | Enchants this tile to provide extra black magic. |

#### Level 2

| Spell | School | Summary |
|-------|--------|---------|
| Acid Spray | Beguilement | Sprays gobs of nasty acid all over a group of foes. |
| Confusion | Curse | Brings down a curse of confusion on a group of foes. |
| Darkness | Illusion | Veils the party in darkness, instantly replenishing the stealth points of all members. |
| Disease | Curse | Curses a single foe with a horrible, long lasting disease. |
| Theft | Illusion | This spell attempts to magically steal an item from an NPC. |
| Weakness | Curse | A curse of weakness and physical frailty descends on your foes. |

#### Level 3

| Spell | School | Summary |
|-------|--------|---------|
| Backstab | Evocation | A ghostly blade manifests and buries itself in one of your opponents. |
| Death | Curse | Flings a curse of instant death at one of your foes. |
| Gruesome Aspect | Illusion | You are cloaked in a digusting and hideous illusion. Enemies that you hit in combat will be deeply disturbed. |
| Leverage | Beguilement | Attempts to adjust the attitude of an NPC to be more favourable towards you. |
| Pain | Curse | Inflicts a curse of unbearable pain on your foes. |
| Silence | Curse | Inflicts your foes with a curse of silence, hindering their spell casting. |

#### Level 4

| Spell | School | Summary |
|-------|--------|---------|
| Deadly Poison | Curse | A fearsome curse of withering, death and plague. |
| Fearsome Aspect | Illusion | You are cloaked in a fearsome and deadly illusion. Enemies that you hit in combat will be badly affected. |
| Melt Armour | Transmutation | Softens up the armour of your foes. |
| Petrify | Beguilement | Attempts to turn one foe to stone. |
| Poison Gas | Evocation | Shrouds a group of your foes in a poisonous cloud. |
| Spooks | Illusion | Conjures illusory phantoms to terrify all of your foes. |

#### Level 5

| Spell | School | Summary |
|-------|--------|---------|
| Acid Cloud | Evocation | Coats a group of your foes with corrosive acid. |
| Demonic Aspect | Illusion | You are cloaked in a terrible demonic illusion. Enemies that you hit in combat will be badly affected. |
| Hex | Curse | Curses a group of your foes. |
| Lifesteal | Curse | Damages one foe, and transfers the life energy to heal the caster. |
| Power Of The Pit | Curse | Invokes the power of the Pit. |
| Wither Plants | Transmutation | Withers and damages all plant foes. |

#### Level 6

| Spell | School | Summary |
|-------|--------|---------|
| Deadly Air | Transmutation | Your magic turns the air around all your foes poisonous. |
| Decay | Transmutation | Damages your enemies, especially plants. |
| Draining Cloud | Evocation | A cursed stamina-draining cloud descends on your foes. |
| Ruin | Curse | A deadly curse of ruin on your foes. |
| Summon Demon | Conjuration | Summons a demon from the nether levels of hell and bins it to fight for you. |

#### Level 7

| Spell | School | Summary |
|-------|--------|---------|
| Curse | Curse | A deadly curse on a group of foes. |
| Death Cloud | Evocation | A devouring cloud of death descends on a group of your foes. |
| Death Wish | Beguilement | Attempts to kill every foe. |
| Gates Of Hell | Conjuration | You throw open the gates of Hell and unleash it's denizens on the face of the earth... |
| Vampirism | Curse | Drains the life force from a whole group of foes and transfers it to you. |

### Purple Magic spell list

#### Level 1

| Spell | School | Summary |
|-------|--------|---------|
| Bane | Curse | Casts a curse on a whole group of foes that lowers their defence. |
| Blur | Illusion | Partly conceals one of your allies, improving defence and stealth point regeneration. |
| Charm | Beguilement | Attempts to charm an NPC. |
| Full Moon | Transmutation | Enchants this tile to provide extra purple magic. |
| Heal Light Wounds | Blessing | Minor healing for one character. |
| Witchbolt | Illusion | Flings a bolt of icy cold shadow matter at a single foe. |

#### Level 2

| Spell | School | Summary |
|-------|--------|---------|
| Cure Lesser Condition | Blessing | Attempts to cure one of your allies of minor conditions like fear, blindness, nausea, irritation and sleep. |
| Jinx | Curse | Brings down a baleful curse of damage and discomfort on a single foe. |
| Shrill Sound | Illusion | High pitched waves of soudn assault one group of your foes. |
| Skullduggery | Blessing | Helps with picking locks and disarming traps |
| Slow | Curse | Reduces the speed effected foes. |
| Watchbells | Beguilement | Attempts to wake up all sleeping party members. |

#### Level 3

| Spell | School | Summary |
|-------|--------|---------|
| Cure Paralysis | Blessing | Attempts to cure one of your allies of paralysis. |
| Cure Poison | Blessing | Attempts to cure one of your allies of poison. |
| Dazzling Lights | Illusion | An impressive multicoloured spray of brilliant bolts of light streams out over your foes. All kinds of effects are possible. |
| Fey Voice | Beguilement | You speak with the Voice of the faerie folk. Your unfortunate target will be rendered helpless. |
| Mass Blur | Illusion | Partly conceals members of your party, improving defence and stealth point regeneration. |
| Phantom Guardian | Illusion | A phantom watchdog guards the party while you rest, alerting you at once if attacked. |

#### Level 4

| Spell | School | Summary |
|-------|--------|---------|
| Fey Bolt | Beguilement | Fey bolt. |
| Haste | Transmutation | Speeds up the entire party. |
| Mortal Allure | Beguilement | Captivates all fey and outsiders in the area with your delectable mortal allure. |
| Remove Curse | Blessing | Attempts to remove the curse from a single item. |
| Soul Shield | Blessing | Shields the party from curses, illusions, beguilements, and sundry psychic attacks. |
| Twinkling Cape | Illusion | Cloaks you in a twinkling fearie cape that provides all manner of defensive benefits. |

#### Level 5

| Spell | School | Summary |
|-------|--------|---------|
| Hex | Curse | Curses a group of your foes. |
| Melt Into Mist | Illusion | Replenished party stealth and partly conceals the party for a few rounds. |
| Prismic Chaos | Illusion | A deadly multicoloured spray of brilliant bolts of light streams out over all of your foes. All kinds of effects are possible. |
| Prismic Missile | Illusion | A deadly multicoloured spray of brilliant bolts of light streams out over your foes. All kinds of effects are possible. |
| Remedy | Blessing | Heals and removes minor conditions from the whole party. |
| Sane Mind | Blessing | Removes insanity, possession and fear conditions. |
| Unleash Power | Beguilement | Transforms the witch into a fearsome and skillful melee fighter. |

#### Level 6

| Spell | School | Summary |
|-------|--------|---------|
| Cloud of Faeries | Conjuration | Summons a cloud of hundreds of tiny faeries to assault your foes. |
| Cure Stone | Blessing | Returns life to an ally who has been petrified. |
| Might To Magic | Beguilement | A single foe is drained of life and magical energy, and your magical energy is possibly recharged |
| Starlight | Illusion | Your party is covered in a starry blanket of Gold Magic, concealing them, enhancing resistances and magic power restoration. |
| Telekinesis | Evocation | You're done with subtlety, and feeling angry and emotional. This spell unleashes your raw power on your hapless foes, battering them into submission. |

#### Level 7

| Spell | School | Summary |
|-------|--------|---------|
| Binding | Beguilement | This spell attempts to possess a whole group of your foes, cursing them if that fails. |
| Cloak Of Magic | Illusion | Covers you in a shimmering enchanted cape of pure magic. |
| Curse | Curse | A deadly curse on a group of foes. |
| Left Handed Bane | Curse | You cast a deadly Left Handed curse at one of your foes. |

### Gold Magic spell list

#### Level 1

| Spell | School | Summary |
|-------|--------|---------|
| Charm | Beguilement | Attempts to charm an NPC. |
| Colour Spray | Illusion | Sprays out a startling fan of light an energy to blind, stun or even damage nearby foes. |
| Faerie Fire | Illusion | The targets of this spell are outlined in pale glowing light of an eerie pastel shade. This drains their stealth points for the duration of the spell. |
| Sleep | Beguilement | Attempts to send a whole group of foes to sleep. |
| Stamina | Blessing | Reduces the fatigue of one of your allies. |
| Subtle Glamour | Transmutation | Enchants this tile to provide extra gold magic. |
| Tag | Beguilement | Each party member is enchanted with the ability to immobilise any foe that they touch. |

#### Level 2

| Spell | School | Summary |
|-------|--------|---------|
| Identify | Transmutation | Attempts to identify a single item. |
| Invisibility | Illusion | Turns one of your allies invisible. |
| Mesmerism | Beguilement | Thie spell attempts to mesmerise a whole group of foes into immobility. |
| Obfuscation | Illusion | Covers the party with a nondescript illusion, boosting stealth point regeneration and stealth in urban and dungeon environments. |
| Skullduggery | Blessing | Helps with picking locks and disarming traps |
| Theft | Illusion | This spell attempts to magically steal an item from an NPC. |

#### Level 3

| Spell | School | Summary |
|-------|--------|---------|
| Beguilement | Beguilement | Attempts a powerful charm on an NPC. |
| Blink | Illusion | The caster will randomly blink in and out of existence. |
| Knock Knock | Illusion | Attempts to open a lock or disarm a trap. |
| Mindread | Beguilement | Attempts to read the mind of an NPC. |
| Mirror Image | Illusion | Illusory mirror images of the caster spring up, distracting your foes. |
| Silence | Curse | Inflicts your foes with a curse of silence, hindering their spell casting. |

#### Level 4

| Spell | School | Summary |
|-------|--------|---------|
| Courage | Beguilement | Removes fear from the party and enchants them to resist psychic attacks |
| Goldfingers | Illusion | Charms and NPC and steals an item at the same time. |
| Haste | Transmutation | Speeds up the entire party. |
| Soul Shield | Blessing | Shields the party from curses, illusions, beguilements, and sundry psychic attacks. |
| Spooks | Illusion | Conjures illusory phantoms to terrify all of your foes. |

#### Level 5

| Spell | School | Summary |
|-------|--------|---------|
| Diplomacy | Beguilement | Enchants the party so that NPCs that you encounter react more favourably. |
| Prismic Chaos | Illusion | A deadly multicoloured spray of brilliant bolts of light streams out over all of your foes. All kinds of effects are possible. |
| Prismic Missile | Illusion | A deadly multicoloured spray of brilliant bolts of light streams out over your foes. All kinds of effects are possible. |
| Sane Mind | Blessing | Removes insanity, possession and fear conditions. |
| Shadow Warriors | Illusion | Conjures a troupe of shadow warriors to fight for you. |
| Turncoat | Beguilement | Beguiles one foe into fighting on your side. |
| Vanishing | Illusion | Turns the entire party invisible. |

#### Level 6

| Spell | School | Summary |
|-------|--------|---------|
| Confidence Trick | Beguilement | Magically defrauds one unfortunate NPC in every way possible. |
| Cure Stone | Blessing | Returns life to an ally who has been petrified. |
| Pandemonium | Illusion | Unleashes a pandemonium of sensory chaos on your foes. |
| Phantasm | Illusion | Summons a phantasm to fight for you. |
| Stasis | Beguilement | Freezes all your foes into immobility. |

#### Level 7

| Spell | School | Summary |
|-------|--------|---------|
| Anti-Magic | Curse | Handicaps the magical ability of a group of your foes. |
| Cerebral Hemorrhage | Beguilement | Causes the brain of one of your foes to explode. |
| Heroism | Blessing | Enhances the strength, speed and skill of one of your allies. |
| Mind Flay | Beguilement | Flays the minds of every foe. |

### White Magic spell list

#### Level 1

| Spell | School | Summary |
|-------|--------|---------|
| Bless Party | Blessing | Blessed defence for the party |
| Charm | Beguilement | Attempts to charm an NPC. |
| Heal Light Wounds | Blessing | Minor healing for one character. |
| Holy Ground | Transmutation | Enchants this tile to provide extra white magic. |
| Stamina | Blessing | Reduces the fatigue of one of your allies. |
| Striking | Transmutation | Imbues the casters attacks with extra damage and armour penetration. |

#### Level 2

| Spell | School | Summary |
|-------|--------|---------|
| Cure Lesser Condition | Blessing | Attempts to cure one of your allies of minor conditions like fear, blindness, nausea, irritation and sleep. |
| Daylight | Conjuration | Bathes the area around one group of foes in the bright wholesome light of day, scorching undead and hindering the stealth of any type of foe. |
| Enchanted Blade | Transmutation | Enchants the whole party with increased martial prowess. |
| Guardian Angel | Blessing | Blesses a party member with a guardian angel that prevents damage. |
| Healing Factor | Transmutation | Enchants a single party member with a boosted hit point regeneration rate. |
| Identify | Transmutation | Attempts to identify a single item. |

#### Level 3

| Spell | School | Summary |
|-------|--------|---------|
| Armourplate | Transmutation | Wards your party with increased defence and protection from physical attacks. |
| Cure Paralysis | Blessing | Attempts to cure one of your allies of paralysis. |
| Cure Poison | Blessing | Attempts to cure one of your allies of poison. |
| Heal Moderate Wounds | Blessing | Moderate healing for one character. |
| Magic Screen | Blessing | Wards your party with increased protection against magical attacks. |
| Rest All | Blessing | Replenishes the stamina of the whole party. |

#### Level 4

| Spell | School | Summary |
|-------|--------|---------|
| Cure Disease | Blessing | Cures disease in one of your allies. |
| Element Shield | Transmutation | Shields the entire party from the effects of ice, fire, acid and lightning. |
| Rally | Beguilement | Restores party stamina and bestows on them a blessing of steadfastness and resistance to adversity. |
| Remove Curse | Blessing | Attempts to remove the curse from a single item. |
| Soul Shield | Blessing | Shields the party from curses, illusions, beguilements, and sundry psychic attacks. |
| Superman | Transmutation | Increases the attributes of an ally to super human levels. |

#### Level 5

| Spell | School | Summary |
|-------|--------|---------|
| Astral Gate | Evocation | Instantly attempts to slay any out-planar creatures that you are fighting, damaging them even if they survive. |
| Heal All | Blessing | Heals the whole party. |
| Heal Serious Wounds | Blessing | Serious healing for one character. |
| Purify Air | Transmutation | Reduces the strength of cloud spell afflicting the party. |
| Sane Mind | Blessing | Removes insanity, possession and fear conditions. |
| Zap Undead | Evocation | Instantly attempts to slay any undead creatures that you are fighting, damaging them even if they survive. |

#### Level 6

| Spell | School | Summary |
|-------|--------|---------|
| Cure All | Blessing | Attempts to remove baneful conditions from the whole party. |
| Cure Stone | Blessing | Returns life to an ally who has been petrified. |
| Faith | Evocation | Fortified the party's saves and resistances. |
| Resurrection | Blessing | Brings life to one of your dead allies. |
| Summon Angel | Conjuration | Summons a warrior angel to fight for you. |

#### Level 7

| Spell | School | Summary |
|-------|--------|---------|
| Divinity | Blessing | Imbues you with the strength of your beliefs. |
| Falling Stars | Conjuration | Conjures a hail of falling stars on your foes. |
| Fervour | Evocation | Inspires belief and ferocity in your party. |
| Renewal | Blessing | Heals and cures one of your allies. |
| Restoration | Blessing | Heals one ally greatly. |

### Green Magic spell list

#### Level 1

| Spell | School | Summary |
|-------|--------|---------|
| Barkskin | Transmutation | This Gold Magic toughens the skin of one character. |
| Dire Wasps | Conjuration | Conjures a swarm of deadly wasps to attack one foe. |
| Heal Light Wounds | Blessing | Minor healing for one character. |
| Insect Swarm | Conjuration | Conjures a swarm of buzzing insects to distract a group of foes. |
| Stamina | Blessing | Reduces the fatigue of one of your allies. |
| Verdant Growth | Transmutation | Enchants this tile to provide extra green magic. |

#### Level 2

| Spell | School | Summary |
|-------|--------|---------|
| Call Vermin | Conjuration | Summons a number of small critters to fight for you. |
| Chameleon | Transmutation | Imbues the party with a camouflaging Gold Magic, boosting stealth point regeneration and stealth in wilderness and wasteland environments. |
| Cure Lesser Condition | Blessing | Attempts to cure one of your allies of minor conditions like fear, blindness, nausea, irritation and sleep. |
| Disease | Curse | Curses a single foe with a horrible, long lasting disease. |
| Flame Blade | Evocation | Evokes a flaming blade to wound one opponent. |
| Spit Venom | Transmutation | The druid spits a deadly stream of venom at a single foe. |

#### Level 3

| Spell | School | Summary |
|-------|--------|---------|
| Call Plants | Conjuration | Summons a group of ambulatory plants to fight for the caster. |
| Cure Poison | Blessing | Attempts to cure one of your allies of poison. |
| Entangle | Conjuration | Explosive growth of foliage surrounding your foes deals damage and may immobilise them. |
| Icicle | Evocation | Evokes a deadly icicle missile that wounds one of your foes. |
| Revitalise | Blessing | Replenishes the health of the entire party. |
| Thorns | Transmutation | Thorny growths sprout from all party members, injuring foes that attack them. |

#### Level 4

| Spell | School | Summary |
|-------|--------|---------|
| Blades Of Fire | Evocation | Enchants the weapons of the entire party with extra flaming damage. |
| Call Treant | Conjuration | Calls a single treant to fight for you. |
| Cure Disease | Blessing | Cures disease in one of your allies. |
| Entwine | Conjuration | Explosive growth of foliage surrounding your foes deals damage and may immobilise them. |
| Insect Plague | Conjuration | Conjures a swarm of deadly insects to distract and injure all your foes. |
| Sunbeam | Evocation | A dazzling beam of sunlight withers all undead foes in its path. |

#### Level 5

| Spell | School | Summary |
|-------|--------|---------|
| Bolt Of Lightning | Evocation | Unleashes a devastating lightning bolt on your foes. |
| Call Wyrm | Conjuration | Summons a mighty wyrm to fight for you. |
| Curing | Blessing | Heals and removes conditions from one ally. |
| Dehydrate | Transmutation | Inflicts heavy damage on a single foe. |
| Purify Air | Transmutation | Reduces the strength of cloud spell afflicting the party. |
| Wither Plants | Transmutation | Withers and damages all plant foes. |

#### Level 6

| Spell | School | Summary |
|-------|--------|---------|
| Call Elemental | Conjuration | Calls a fearsome nature elemental to fight by your side. |
| Decay | Transmutation | Damages your enemies, especially plants. |
| Firestorm | Evocation | A storm of fire surrounds your enemies, dealing damage every round. |
| Forest Vengeance | Conjuration | The very forest comes alive and entangles all your foes. |
| Resurrection | Blessing | Brings life to one of your dead allies. |

#### Level 7

| Spell | School | Summary |
|-------|--------|---------|
| Blizzard | Evocation | A deadly blizzard assails your foes. |
| Call Wyrms | Conjuration | Summons a group of wyrms to fight for you. |
| Falling Stars | Conjuration | Conjures a hail of falling stars on your foes. |
| Force Of Nature | Transmutation | Transforms you into an unstoppable force of nature. |
| Renewal | Blessing | Heals and cures one of your allies. |

### Blue Magic spell list

#### Level 1

| Spell | School | Summary |
|-------|--------|---------|
| Burning Hands | Evocation | A sheet of flame leaps from the casters hands, searing a group of foes. |
| Crucible | Transmutation | Enchants this tile to provide extra blue magic. |
| Frostbite | Evocation | Frost forms as the temperature drops around a single foe. |
| Mephit | Conjuration | Summons an imp from one of the elemental planes to aid you in combat. |
| Paralysis | Curse | Casts a curse of paralysis at a single foe. |
| Resist Elements | Transmutation | Increases the targets resistance to fire, cold, lightning and acid. |

#### Level 2

| Spell | School | Summary |
|-------|--------|---------|
| Blinding Flash | Evocation | A dazzling flash of light with a chance to blind a group of foes. |
| Cure Lesser Condition | Blessing | Attempts to cure one of your allies of minor conditions like fear, blindness, nausea, irritation and sleep. |
| Flare | Evocation | Flings a scorching ball of flame at a single opponent. |
| Hailstorm | Evocation | Brings down a rain of freezing hail on one unlucky group of foes. |
| Razor Cloak | Transmutation | Imbues the recipient with damage reflection. |
| Web | Conjuration | Conjures a mess of tough sticky threads to immobilise a group of foes. |

#### Level 3

| Spell | School | Summary |
|-------|--------|---------|
| Cure Paralysis | Blessing | Attempts to cure one of your allies of paralysis. |
| Freeze | Transmutation | Freezes one whole group of your enemies into paralysed immobility. |
| Mephit Swarm | Conjuration | Summons from the elemental planes a swarm of mischievous imps to plague your enemies. |
| Noxious Fumes | Evocation | Bombs your foes with a thick, stinking cloud of corrosive gas. Damage and disgust result. |
| Silence | Curse | Inflicts your foes with a curse of silence, hindering their spell casting. |
| Whipping Rocks | Evocation | A hail of jagged stones pelts your foes. Unsurprisingly, damage results. |

#### Level 4

| Spell | School | Summary |
|-------|--------|---------|
| Crush | Evocation | Crushes one foe beneath a giant boulder. |
| Cure Disease | Blessing | Cures disease in one of your allies. |
| Element Shield | Transmutation | Shields the entire party from the effects of ice, fire, acid and lightning. |
| Elemental | Conjuration | Summons an elemental to fight for you. |
| Haste | Transmutation | Speeds up the entire party. |
| Whirlwind | Evocation | Evokes a deadly and damaging gust for howling wind. |

#### Level 5

| Spell | School | Summary |
|-------|--------|---------|
| Deep Freeze | Evocation | Inflicts a large amount of cold damage to a single foe. |
| Freeze All | Transmutation | Freezes all of your enemies into paralysed immobility. |
| Lava Burst | Evocation | Sprays deadly lava all over a group of your foes. |
| Mephit Army | Conjuration | You a lord of the mephits, legendary amongst them. At your call they pour gleefully from the elemental plane in vast numbers, besetting your hapless foes from all sides with their barbs and spells. |
| Purify Air | Transmutation | Reduces the strength of cloud spell afflicting the party. |
| Stoneskin | Transmutation | Hardens the skin of one target, reducing damage. |

#### Level 6

| Spell | School | Summary |
|-------|--------|---------|
| Elemental Swarm | Conjuration | Summons a swarm of elementals to fight for you. |
| Firestorm | Evocation | A storm of fire surrounds your enemies, dealing damage every round. |
| Hurricane | Evocation | Evokes a raging hurricane that blasts all your foes. |
| Quicksand | Transmutation | Liquifies the very ground and attempts to swallow your foes. |
| Tsunami | Evocation | A giant wave of water batters your foes. |

#### Level 7

| Spell | School | Summary |
|-------|--------|---------|
| Asphixiation | Transmutation | Transforms the air around your foes to vacuum, killing them instantly. |
| Dragon | Conjuration | Summons a dragon to fight for you. |
| Earthquake | Conjuration | The earth under your foes heaves and shakes, causing damage and fear. |
| Lightning Cloud | Evocation | A cloud of lightning engulfs one group of your foes. |
| Nuclear Blast | Evocation | A fearsome fiery blast damages all your foes. |
