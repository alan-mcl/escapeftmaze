/*
 * Copyright (c) 2011 Alan McLachlan
 *
 * This file is part of Escape From The Maze.
 *
 * Escape From The Maze is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package mclachlan.maze.stat;

import java.util.*;
import mclachlan.diygui.util.HashMapMutableTree;
import mclachlan.maze.data.Database;
import mclachlan.maze.data.StringUtil;
import mclachlan.maze.game.ActorEncounter;
import mclachlan.maze.game.Log;
import mclachlan.maze.game.Maze;
import mclachlan.maze.stat.combat.*;
import mclachlan.maze.stat.condition.Condition;
import mclachlan.maze.stat.condition.ConditionManager;
import mclachlan.maze.stat.magic.*;
import mclachlan.maze.stat.npc.NpcFaction;
import mclachlan.maze.stat.npc.NpcScript;
import mclachlan.maze.util.MazeException;

import static mclachlan.maze.stat.EquipableSlot.Type.*;

/**
 *
 */
public class PlayerCharacter extends UnifiedActor
{
	private int experience;
	private int kills;
	private String portrait;
	private Personality personality;

	/** how many spells this character can pick at the next level up */
	private int spellPicks;

	/** How far along with practise this character is in each modifier. */
	private Practice practice;

	/** The names of modifiers that this character has activated */
	private StatModifier activeModifiers;

	/** The keys of any removed level abilities */
	private List<String> removedLevelAbilities;

	/** A measure of behaviour: better, honourable, "good" behaviour will
	 * add to this, the opposite will deduct to this. */
	private int karma;

	/*-------------------------------------------------------------------------*/
	private static SignatureWeaponUpgradePath
		engineeringOmnigun, engineeringXbow, engineeringHandCannon;

	static
	{
		StatModifier req = new StatModifier();

		engineeringOmnigun = new SignatureWeaponUpgradePath();

		req.setModifier(Stats.Modifier.ENGINEERING, 1);
		engineeringOmnigun.addUpgrade("Omnigun Prototype", "Omnigun Mk1", req);
		req.setModifier(Stats.Modifier.ENGINEERING, 3);
		engineeringOmnigun.addUpgrade("Omnigun Mk1", "Omnigun Mk2", req);
		req.setModifier(Stats.Modifier.ENGINEERING, 6);
		engineeringOmnigun.addUpgrade("Omnigun Mk2", "Omnigun Mk3", req);
		req.setModifier(Stats.Modifier.ENGINEERING, 9);
		engineeringOmnigun.addUpgrade("Omnigun Mk3", "Omnigun Mk4", req);
		req.setModifier(Stats.Modifier.ENGINEERING, 12);
		engineeringOmnigun.addUpgrade("Omnigun Mk4", "Omnigun Mk5", req);
		req.setModifier(Stats.Modifier.ENGINEERING, 15);
		engineeringOmnigun.addUpgrade("Omnigun Mk5", "Omnigun Mk6", req);

		req = new StatModifier();

		engineeringXbow = new SignatureWeaponUpgradePath();

		req.setModifier(Stats.Modifier.ENGINEERING, 1);
		engineeringXbow.addUpgrade("Light Crossbow", "X-bow Mk1", req);
		req.setModifier(Stats.Modifier.ENGINEERING, 3);
		engineeringXbow.addUpgrade("X-bow Mk1", "X-bow Mk2", req);
		req.setModifier(Stats.Modifier.ENGINEERING, 6);
		engineeringXbow.addUpgrade("X-bow Mk2", "X-bow Mk3", req);
		req.setModifier(Stats.Modifier.ENGINEERING, 9);
		engineeringXbow.addUpgrade("X-bow Mk3", "X-bow Mk4", req);
		req.setModifier(Stats.Modifier.ENGINEERING, 12);
		engineeringXbow.addUpgrade("X-bow Mk4", "X-bow Mk5", req);
		req.setModifier(Stats.Modifier.ENGINEERING, 15);
		engineeringXbow.addUpgrade("X-bow Mk5", "X-bow Mk6", req);

		req = new StatModifier();

		engineeringHandCannon = new SignatureWeaponUpgradePath();

		req.setModifier(Stats.Modifier.ENGINEERING, 1);
		engineeringHandCannon.addUpgrade("Flintlock Pistol", "Hand Cannon Mk1", req);
		req.setModifier(Stats.Modifier.ENGINEERING, 3);
		engineeringHandCannon.addUpgrade("Hand Cannon Mk1", "Hand Cannon Mk2", req);
		req.setModifier(Stats.Modifier.ENGINEERING, 6);
		engineeringHandCannon.addUpgrade("Hand Cannon Mk2", "Hand Cannon Mk3", req);
		req.setModifier(Stats.Modifier.ENGINEERING, 9);
		engineeringHandCannon.addUpgrade("Hand Cannon Mk3", "Hand Cannon Mk4", req);
		req.setModifier(Stats.Modifier.ENGINEERING, 12);
		engineeringHandCannon.addUpgrade("Hand Cannon Mk4", "Hand Cannon Mk5", req);
		req.setModifier(Stats.Modifier.ENGINEERING, 15);
		engineeringHandCannon.addUpgrade("Hand Cannon Mk5", "Hand Cannon Mk6", req);
	}

	/*-------------------------------------------------------------------------*/

	/**
	 * Instantiates a new PC, explicitly specifying all fields.
	 */
	public PlayerCharacter(
		String name,
		Gender gender,
		Race race,
		CharacterClass characterClass,
		Personality personality,
		Map<String, Integer> levels,
		int experience,
		int kills,
		int karma,
		String portrait,
		Item helm,
		Item torsoArmour,
		Item legArmour,
		Item boots,
		Item gloves,
		Item miscItem1,
		Item miscItem2,
		Item bannerItem,
		Item primaryWeapon,
		Item secondaryWeapon,
		Item altPrimaryWeapon,
		Item altSecondaryWeapon,
		Inventory inventory,
		SpellBook spellBook,
		int spellPicks,
		Stats stats,
		Practice practice,
		StatModifier activeModifiers,
		List<String> removedLevelAbilities)
	{
		super(name, gender, race, characterClass, race.getBodyParts(),
			levels, stats, spellBook, inventory);


		setEquippedItem(PRIMARY_WEAPON, primaryWeapon, 0);
		setEquippedItem(PRIMARY_WEAPON, altPrimaryWeapon, 1);
		setEquippedItem(SECONDARY_WEAPON, secondaryWeapon, 0);
		setEquippedItem(SECONDARY_WEAPON, altSecondaryWeapon, 1);
		setEquippedItem(HELM, helm, 0);
		setEquippedItem(TORSO_ARMOUR, torsoArmour, 0);
		setEquippedItem(LEG_ARMOUR, legArmour, 0);
		setEquippedItem(GLOVES, gloves, 0);
		setEquippedItem(BOOTS, boots, 0);
		setEquippedItem(MISC_ITEM, miscItem1, 0);
		setEquippedItem(MISC_ITEM, miscItem2, 1);
		setEquippedItem(BANNER_ITEM, bannerItem, 0);

		this.activeModifiers = activeModifiers;
		this.experience = experience;
		this.kills = kills;
		this.karma = karma;
		this.portrait = portrait;
		this.practice = practice;
		this.spellPicks = spellPicks;
		this.personality = personality;
		this.removedLevelAbilities = removedLevelAbilities;
	}

	/*-------------------------------------------------------------------------*/

	/**
	 * Instantiates a new PC, specifying only the fields set up at character
	 * creation time.
	 */
	public PlayerCharacter(
		String name,
		Race race,
		Gender gender,
		CharacterClass characterClass,
		String portrait,
		Map<String, Integer> levels,
		int maxHitPoints,
		int maxActionPoints,
		int maxMagicPoints,
		StatModifier activeModifiers)
	{
		super(name, gender, race, characterClass, race.getBodyParts(),
			levels, new Stats(), null, new Inventory(MAX_PACK_ITEMS));

		this.portrait = portrait;
		this.activeModifiers = activeModifiers;
		this.practice = new Practice();
		this.kills = 0;
		this.karma = 0;
		this.removedLevelAbilities = new ArrayList<String>();

		getStats().setHitPoints(new CurMaxSub(maxHitPoints));
		getStats().setActionPoints(new CurMax(maxActionPoints));
		getStats().setMagicPoints(new CurMax(maxMagicPoints));

		if (Maze.getInstance() != null)
		{
			setActorGroup(Maze.getInstance().getParty());
		}
	}

	/*-------------------------------------------------------------------------*/

	/**
	 * Instantiates a new PC, a deep copy of the given PC.
	 */
	public PlayerCharacter(PlayerCharacter pc)
	{
		super(pc);

		this.activeModifiers = new StatModifier(pc.activeModifiers);
		this.experience = pc.experience;
		this.kills = pc.kills;
		this.karma = pc.karma;
		this.portrait = pc.portrait;
		this.practice = new Practice(pc.practice);
		this.spellPicks = pc.spellPicks;
		this.personality = pc.personality;
		if (pc.removedLevelAbilities == null)
		{
			this.removedLevelAbilities = new ArrayList<String>();
		}
		else
		{
			this.removedLevelAbilities = new ArrayList<String>(pc.removedLevelAbilities);
		}

		setEquippedItem(PRIMARY_WEAPON, cloneItem(pc.getEquippedItem(PRIMARY_WEAPON, 0)), 0);
		setEquippedItem(PRIMARY_WEAPON, cloneItem(pc.getEquippedItem(PRIMARY_WEAPON, 1)), 1);
		setEquippedItem(SECONDARY_WEAPON, cloneItem(pc.getEquippedItem(SECONDARY_WEAPON, 0)), 0);
		setEquippedItem(SECONDARY_WEAPON, cloneItem(pc.getEquippedItem(SECONDARY_WEAPON, 1)), 1);
		setEquippedItem(HELM, cloneItem(pc.getEquippedItem(HELM, 0)), 0);
		setEquippedItem(TORSO_ARMOUR, cloneItem(pc.getEquippedItem(TORSO_ARMOUR, 0)), 0);
		setEquippedItem(LEG_ARMOUR, cloneItem(pc.getEquippedItem(LEG_ARMOUR, 0)), 0);
		setEquippedItem(GLOVES, cloneItem(pc.getEquippedItem(GLOVES, 0)), 0);
		setEquippedItem(BOOTS, cloneItem(pc.getEquippedItem(BOOTS, 0)), 0);
		setEquippedItem(MISC_ITEM, cloneItem(pc.getEquippedItem(MISC_ITEM, 0)), 0);
		setEquippedItem(MISC_ITEM, cloneItem(pc.getEquippedItem(MISC_ITEM, 1)), 1);
		setEquippedItem(BANNER_ITEM, cloneItem(pc.getEquippedItem(BANNER_ITEM, 0)), 0);
	}

	/*-------------------------------------------------------------------------*/

	private Item cloneItem(Item item)
	{
		return item == null ? null : new Item(item);
	}

	/*-------------------------------------------------------------------------*/
	public void applyPermanentStatModifier(StatModifier m)
	{
		for (Stats.Modifier modifier : m.getModifiers().keySet())
		{
			int value = m.getModifier(modifier);

			int currentValue = this.getBaseModifier(modifier);
			this.setModifier(modifier, currentValue + value);
		}
	}

	/*-------------------------------------------------------------------------*/
	public void rollbackPermanentStatModifier(StatModifier m)
	{
		StatModifier negation = new StatModifier();
		for (Stats.Modifier mod : m.getModifiers().keySet())
		{
			negation.setModifier(mod, -m.getModifier(mod));
		}
		applyPermanentStatModifier(negation);
	}

	/*-------------------------------------------------------------------------*/
	public void applyStartingKit(StartingKit kit)
	{
		switch (getCharacterClass().getFocus())
		{
			case COMBAT:
				applyPermanentStatModifier(kit.getCombatModifiers());
				break;
			case STEALTH:
				applyPermanentStatModifier(kit.getStealthModifiers());
				break;
			case MAGIC:
				applyPermanentStatModifier(kit.getMagicModifiers());
				break;
			default: throw new MazeException("Invalid focus "+getCharacterClass().getFocus());
		}

		setPrimaryWeapon(GameSys.getInstance().createItemForStartingKit(kit.getPrimaryWeapon(), this));
		setSecondaryWeapon(GameSys.getInstance().createItemForStartingKit(kit.getSecondaryWeapon(), this));
		setHelm(GameSys.getInstance().createItemForStartingKit(kit.getHelm(), this));
		setTorsoArmour(GameSys.getInstance().createItemForStartingKit(kit.getTorsoArmour(), this));
		setLegArmour(GameSys.getInstance().createItemForStartingKit(kit.getLegArmour(), this));
		setGloves(GameSys.getInstance().createItemForStartingKit(kit.getGloves(), this));
		setBoots(GameSys.getInstance().createItemForStartingKit(kit.getBoots(), this));
		setBannerItem(GameSys.getInstance().createItemForStartingKit(kit.getBannerItem(), this));
		setMiscItem1(GameSys.getInstance().createItemForStartingKit(kit.getMiscItem1(), this));
		setMiscItem2(GameSys.getInstance().createItemForStartingKit(kit.getMiscItem2(), this));

		if (kit.getPackItems() != null)
		{
			for (int i = 0; i < kit.getPackItems().size(); i++)
			{
				Item item = GameSys.getInstance().createItemForStartingKit(kit.getPackItems().get(i), this);
				if (item != null)
				{
					getInventory().add(item);
				}
			}
		}
	}

	/*-------------------------------------------------------------------------*/
	public boolean isEquippableItem(Item item)
	{
		// check for WEAPON_MASTER
		if (item.isWeapon() && this.getModifier(Stats.Modifier.WEAPON_MASTER) > 0)
		{
			return true;
		}

		boolean by_gender = item.getUsableByGender().contains(this.getGender().getName());
		boolean by_class = item.getUsableByCharacterClass().contains(this.getCharacterClass().getName());
		boolean by_race = item.getUsableByRace().contains(this.getRace().getName());
		boolean by_req = meetsRequirements(item.getEquipRequirements());

		return by_gender && by_class && by_race && by_req;
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public CharacterClass.Focus getFocus()
	{
		return getCharacterClass().getFocus();
	}

	@Override
	public String getFaction()
	{
		return null;
	}

	/*-------------------------------------------------------------------------*/
	public List<Stance> getCharacterStanceOptions(
		Maze maze, Combat combat)
	{
		ArrayList<Stance> result = new ArrayList<Stance>();

		boolean alive = GameSys.getInstance().isActorAlive(this);
		boolean aware = GameSys.getInstance().isActorAware(this);
		if (!alive)
		{
			result.add(Stance.DEAD);
		}
		else if (!aware)
		{
			result.add(Stance.UNAWARE);
		}
		else if (maze.getState() == Maze.State.MOVEMENT)
		{
			if (maze.getParty().getPlayerCharacterIndex(this) < maze.getParty().getFormation())
			{
//				result.add(StringUtil.getUiLabel("aao.front.row"));
			}
			else
			{
//				result.add(StringUtil.getUiLabel("aao.back.row"));
			}

		}
		else if (maze.getState() == Maze.State.COMBAT)
		{
			if (this.getModifier(Stats.Modifier.SNAKESPEED) > 0)
			{
				result.add(Stance.SNAKESPEED);
			}

			result.add(Stance.ACT_EARLY);
			result.add(Stance.ACT_LATE);

			if (this.getModifier(Stats.Modifier.PATIENCE) > 0)
			{
				result.add(Stance.PATIENCE);
			}
		}

		return result;
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * Returns all action options for this character.
	 *
	 * @param combat
	 * 	The current combat that this character is involved in. If non null,
	 * 	all legal combat intentions will be returned. If null, all legal
	 * 	non combat action options will be returned.
	 * @return
	 * 	All current legal action options for this character
	 */
	public HashMapMutableTree<ActorActionOption> getCharacterActionOptions(
		Maze maze,
		Combat combat)
	{
		HashMapMutableTree<ActorActionOption> result =
			new HashMapMutableTree<ActorActionOption>();

		boolean alive = GameSys.getInstance().isActorAlive(this);
		boolean aware = GameSys.getInstance().isActorAware(this);
		if (!alive)
		{
			result.add(new NullOption("aao.dead"), null);
		}
		else if (!aware)
		{
			result.add(new NullOption("aao.unaware"), null);
		}
		else if (maze.getState() == Maze.State.MOVEMENT)
		{
			// always a use item option
			result.add(new UseItemOption(), null);

			// cast spell option if this character has spells
			if (this.getSpellBook() != null && this.getSpellBook().size() > 0)
			{
				result.add(new SpellOption(), null);
			}

			// always an equip option
			result.add(new EquipOption(), null);

			// special abilities
			if (this.getSpellLikeAbilities() != null)
			{
				for (SpellLikeAbility sla : this.getSpellLikeAbilities())
				{
					if (sla.isUsableDuringMovement() && sla.meetsRequirements(this))
					{
						result.add(new SpecialAbilityOption(sla), null);
					}
				}
			}
		}
		else if (maze.getState() == Maze.State.COMBAT)
		{
			boolean hasQuickWits = getModifier(Stats.Modifier.QUICK_WITS) > 0;

			boolean partyIsSurprised =
				combat.getAmbushStatus() == Combat.AmbushStatus.FOES_MAY_AMBUSH_PARTY ||
				combat.getAmbushStatus() == Combat.AmbushStatus.FOES_MAY_AMBUSH_OR_EVADE_PARTY;

			// actions in the surprise round only available with QUICK WITS
			if (!partyIsSurprised || hasQuickWits)
			{
				// Attack option if there are attackable groups
				List<ActorGroup> attackableGroups = GameSys.getInstance().getAttackableGroups(this, combat);
				if (attackableGroups != null && !attackableGroups.isEmpty())
				{
					for (AttackWith aw : getAttackWithOptions())
					{
						result.add(new AttackOption(aw), null);
					}
				}

				// There's always a Defend option
				result.add(new DefendOption(), null);

				// Cast Spell option if this actor has spells
				if (this.getSpellBook().getSpells().size() != 0)
				{
					result.add(new SpellOption(), null);
				}

				// always a use item option
				result.add(new UseItemOption(), null);

				// always an equip option
				result.add(new EquipOption(), null);

				// check for HIDE
				if (this.getModifier(Stats.Modifier.HIDE) > 0)
				{
					result.add(new HideOption(), null);
				}

				// check for DEADLY_STRIKE
				if (this.getModifier(Stats.Modifier.DEADLY_STRIKE) > 0)
				{
					result.add(
						new SpecialAbilityOption(
							new SpellLikeAbility(
								Database.getInstance().getSpell("_DEADLY_STRIKE_"),
								new ValueList(new Value(this.getLevel(), Value.SCALE.NONE)))),
						null);
				}

				// check for DISPLACER
				if (this.getModifier(Stats.Modifier.DISPLACER) > 0)
				{
					result.add(new DisplaceOption(), null);
				}

				// special abilities
				if (this.getSpellLikeAbilities() != null)
				{
					for (SpellLikeAbility sla : this.getSpellLikeAbilities())
					{
						if (sla.isUsableDuringCombat() && sla.meetsRequirements(this))
						{
							if (!result.add(new SpecialAbilityOption(sla), null))
							{
								throw new MazeException("could not add " + sla);
							}
						}
					}
				}
			}
			else
			{
				// PC is surprised
				result.add(new NullOption("aao.surprised"), null);
			}
		}
		else if (maze.getState() == Maze.State.ENCOUNTER_ACTORS)
		{
			ActorEncounter actorEncounter = maze.getCurrentActorEncounter();
			NpcFaction.Attitude attitude = actorEncounter.getEncounterAttitude();

			// Cast Spell option if this actor has spells
			if (this.getSpellBook().getSpells().size() != 0)
			{
				result.add(new SpellOption(), null);
			}

			// always a use item option
			result.add(new UseItemOption(), null);

			// special abilities
			if (this.getSpellLikeAbilities() != null)
			{
				for (SpellLikeAbility sla : this.getSpellLikeAbilities())
				{
					if (sla.isUsableDuringEncounterActors() && sla.meetsRequirements(this))
					{
						result.add(new SpecialAbilityOption(sla), null);
					}
				}
			}

			boolean canThreaten = this.getModifier(Stats.Modifier.THREATEN) > 0;
			boolean canBribe = this.getModifier(Stats.Modifier.TO_BRIBE) > 0;

			switch (attitude)
			{
				case ATTACKING:
					break;
				case AGGRESSIVE:
					result.add(new GiveOption(), null);
					if (canThreaten) result.add(new ThreatenOption(), null);
					if (canBribe) result.add(new BribeOption(), null);
					break;
				case WARY:
					result.add(new GiveOption(), null);
					if (canThreaten) result.add(new ThreatenOption(), null);
					if (canBribe) result.add(new BribeOption(), null);
					break;
				case SCARED:
					result.add(new GiveOption(), null);
					if (canThreaten) result.add(new ThreatenOption(), null);
					if (canBribe) result.add(new BribeOption(), null);
					break;
				case NEUTRAL:
					result.add(new TalkOption(), null);
					result.add(new GiveOption(), null);
					if (canThreaten) result.add(new ThreatenOption(), null);
					if (canBribe) result.add(new BribeOption(), null);

					if (getModifier(Stats.Modifier.STEAL) > 0)
					{
						result.add(new StealOption(), null);
					}

					result.add(new TradeOption(), null);
					break;
				case FRIENDLY:
					result.add(new TalkOption(), null);
					result.add(new GiveOption(), null);

					if (getModifier(Stats.Modifier.STEAL) > 0)
					{
						result.add(new StealOption(), null);
					}

					result.add(new TradeOption(), null);
					break;
				case ALLIED:
					result.add(new TalkOption(), null);
					result.add(new GiveOption(), null);

					if (getModifier(Stats.Modifier.STEAL) > 0)
					{
						result.add(new StealOption(), null);
					}

					result.add(new TradeOption(), null);
					break;
			}
		}
		else if (maze.getState() == Maze.State.ENCOUNTER_CHEST)
		{
			// Cast Spell option if this actor has spells
			if (this.getSpellBook().getSpells().size() != 0)
			{
				result.add(new SpellOption(), null);
			}

			// always a use item option
			result.add(new UseItemOption(), null);

			// special abilities
			if (this.getSpellLikeAbilities() != null)
			{
				for (SpellLikeAbility sla : this.getSpellLikeAbilities())
				{
					if (sla.isUsableDuringEncounterChestOrPortal() && sla.meetsRequirements(this))
					{
						result.add(new SpecialAbilityOption(sla), null);
					}
				}
			}

			if (getModifier(Stats.Modifier.LOCK_AND_TRAP) > 0)
			{
				result.add(new DisarmTrapOption(), null);
			}

			result.add(new OpenChestOption(), null);
		}
		else if (maze.getState() == Maze.State.ENCOUNTER_PORTAL)
		{
			// Cast Spell option if this actor has spells
			if (this.getSpellBook().getSpells().size() != 0)
			{
				result.add(new SpellOption(), null);
			}

			// always a use item option
			result.add(new UseItemOption(), null);

			// special abilities
			if (this.getSpellLikeAbilities() != null)
			{
				for (SpellLikeAbility sla : this.getSpellLikeAbilities())
				{
					if (sla.isUsableDuringEncounterChestOrPortal() && sla.meetsRequirements(this))
					{
						result.add(new SpecialAbilityOption(sla), null);
					}
				}
			}

			if (getModifier(Stats.Modifier.LOCK_AND_TRAP) > 0)
			{
				result.add(new PickLockOption(), null);
			}

			result.add(new ForceOpenOption(), null);
		}

		// set the actor for all action options
		for (ActorActionOption aao : result.getNodes())
		{
			aao.setActor(this);
		}

		return result;
	}

	/*-------------------------------------------------------------------------*/

	@Override
	public Item getEquippedItem(EquipableSlot.Type type, int ordinal)
	{
		if (this.hasEquipableSlot(type))
		{
			return super.getEquippedItem(type, ordinal);
		}
		else
		{
			return null;
		}
	}

	/*-------------------------------------------------------------------------*/

	@Override
	public void setEquippedItem(EquipableSlot.Type type, Item item, int ordinal)
	{
		if (this.hasEquipableSlot(type))
		{
			super.setEquippedItem(type, item, ordinal);
			this.updateCurseState(item, Item.CursedState.DISCOVERED);
		}
	}

	/*-------------------------------------------------------------------------*/
	public void updateCurseState(Item item, int state)
	{
		if (item != null)
		{
			item.setCursedState(state);
		}
	}

	/*-------------------------------------------------------------------------*/

	@Override
	public void inventoryItemAdded(Item item)
	{
		GameSys.getInstance().attemptManualIdentify(item, getActorGroup());
	}

	@Override
	public NpcScript getActionScript()
	{
		return null;
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public boolean addInventoryItem(Item item)
	{
		if (item.getType() == ItemTemplate.Type.MONEY)
		{
			int amount = item.applyConversionRate();
			((PlayerParty)getActorGroup()).incGold(amount);
			return true;
		}
		else if (item.getType() == ItemTemplate.Type.SUPPLIES)
		{
			int amount = item.applyConversionRate();
			((PlayerParty)getActorGroup()).incSupplies(amount);
			return true;
		}
		else
		{
			return super.addInventoryItem(item);
		}
	}

	/*-------------------------------------------------------------------------*/

	@Override
	public List<LevelAbility> getLevelAbilities()
	{
		List<LevelAbility> levelAbilities = super.getLevelAbilities();

		if (removedLevelAbilities == null || removedLevelAbilities.isEmpty())
		{
			return levelAbilities;
		}


		List<LevelAbility> result = new ArrayList<LevelAbility>();

		for (LevelAbility la : levelAbilities)
		{
			if (la.getKey() == null || la.getKey().length() == 0 ||
				!removedLevelAbilities.contains(la.getKey()))
			{
				result.add(la);
			}
		}

		return result;
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public void removeLevelAbility(Spell spell)
	{
		String key = null;

		List<LevelAbility> levelAbilities = getLevelAbilities();
		for (LevelAbility la : levelAbilities)
		{
			if (la instanceof SpecialAbilityLevelAbility &&
				la.getAbility().getSpell().equals(spell))
			{
				key = la.getKey();
			}
		}

		if (key != null)
		{
			if (removedLevelAbilities == null)
			{
				removedLevelAbilities = new ArrayList<String>();
			}
			removedLevelAbilities.add(key);
		}
		else
		{
			throw new MazeException("No such level ability: "+spell.getName());
		}
	}

	/*-------------------------------------------------------------------------*/

	public List<String> getRemovedLevelAbilities()
	{
		return removedLevelAbilities;
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public List<Item> getStealableItems()
	{
		return getInventory().getItems();
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public List<AttackWith> getAttackWithOptions()
	{
		ArrayList<AttackWith> result = new ArrayList<AttackWith>();
		if (getNaturalWeapons() != null)
		{
			result.addAll(getNaturalWeapons());
		}

		if (getPrimaryWeapon() != null)
		{
			result.add(getPrimaryWeapon());
		}
		else
		{
			result.add(GameSys.getInstance().getUnarmedWeapon(this, true));
		}

		return result;
	}

	/*-------------------------------------------------------------------------*/
	public String getPortrait()
	{
		return portrait;
	}

	public String getRightHandIcon()
	{
		return getRace().getRightHandIcon();
	}

	public String getLeftHandIcon()
	{
		return getRace().getLeftHandIcon();
	}

	public Personality getPersonality()
	{
		return personality;
	}

	public void setPersonality(Personality personality)
	{
		this.personality = personality;
	}

	/*-------------------------------------------------------------------------*/
	public Practice getPractice()
	{
		return practice;
	}

	/*-------------------------------------------------------------------------*/
	public int getBaseModifier(Stats.Modifier modifier)
	{
		return this.getStats().getModifier(modifier);
	}

	/*-------------------------------------------------------------------------*/
	public StatModifier getActiveModifiers()
	{
		return activeModifiers;
	}

	public void setActiveModifiers(StatModifier sm)
	{
		this.activeModifiers = sm;
	}

	public String getDisplayName() { return getName();
	}

	@Override
	public String getDisplayNamePlural() { return getName(); }

	public List<TypeDescriptor> getTypes()
	{
		ArrayList<TypeDescriptor> result = new ArrayList<TypeDescriptor>();
		result.add(getCharacterClass());
		result.add(getRace());
		return result;
	}

	public int getExperience()
	{
		return experience;
	}

	public int getKills()
	{
		return kills;
	}

	public int getKarma()
	{
		return karma;
	}

	/*-------------------------------------------------------------------------*/
	public ActorGroup getActorGroup()
	{
		if (Maze.getInstance() != null)
		{
			setActorGroup(Maze.getInstance().getParty());
		}
		return super.getActorGroup();
	}

	/*-------------------------------------------------------------------------*/
	public void removeCurse(int strength)
	{
		removeCurse(getPrimaryWeapon(), strength);
		removeCurse(getSecondaryWeapon(), strength);
		removeCurse(getAltPrimaryWeapon(), strength);
		removeCurse(getAltSecondaryWeapon(), strength);
		removeCurse(getHelm(), strength);
		removeCurse(getTorsoArmour(), strength);
		removeCurse(getLegArmour(), strength);
		removeCurse(getGloves(), strength);
		removeCurse(getBoots(), strength);
		removeCurse(getMiscItem1(), strength);
		removeCurse(getMiscItem2(), strength);
		removeCurse(getBannerItem(), strength);

		for (Item i : getInventory())
		{
			removeCurse(i, strength);
		}
	}

	/*-------------------------------------------------------------------------*/
	public void addAllies(List<FoeGroup> foeGroups)
	{
		Maze.getInstance().addPartyAllies(foeGroups);
		if (this.getCombatantData() != null)
		{
			this.getCombatantData().setSummonedGroup(foeGroups);
		}
	}

	/*-------------------------------------------------------------------------*/
	private void removeCurse(Item item, int strength)
	{
		if (item == null)
		{
			return;
		}

		if (item.getCurseStrengh() > 0 && item.getCurseStrengh() <= strength)
		{
			item.setCursedState(Item.CursedState.TEMPORARILY_REMOVED);
			Maze.log(Log.DEBUG, "removed curse on "+item.getName());
		}
	}

	/*-------------------------------------------------------------------------*/
	public void incExperience(int amount)
	{
		this.experience += amount;
	}

	public void incKills(int amount)
	{
		this.kills += amount;
	}

	/*-------------------------------------------------------------------------*/

	/**
	 * Swaps this characters current and alternate weapons
	 */
	public void swapWeapons()
	{
		// can't swap if wielding a cursed weapon
		if (getPrimaryWeapon() != null &&
			getPrimaryWeapon().isCursed() &&
			getPrimaryWeapon().getCursedState() != Item.CursedState.TEMPORARILY_REMOVED)
		{
			return;
		}
		if (getSecondaryWeapon() != null &&
			getSecondaryWeapon().isCursed() &&
			getSecondaryWeapon().getCursedState() != Item.CursedState.TEMPORARILY_REMOVED)
		{
			return;
		}

		Item tempPrimary = this.getPrimaryWeapon();
		Item tempSecondary = this.getSecondaryWeapon();

		setPrimaryWeapon(this.getAltPrimaryWeapon());
		setSecondaryWeapon(this.getAltSecondaryWeapon());

		this.setAltPrimaryWeapon(tempPrimary);
		this.setAltSecondaryWeapon(tempSecondary);

		// set the cursed states on the new wielded weapons
		if (getPrimaryWeapon() != null)
		{
			getPrimaryWeapon().setCursedState(Item.CursedState.DISCOVERED);
		}
		if (getSecondaryWeapon() != null)
		{
			getSecondaryWeapon().setCursedState(Item.CursedState.DISCOVERED);
		}
	}

	/*-------------------------------------------------------------------------*/
	public int getSpellPicks()
	{
		return this.spellPicks;
	}

	/*-------------------------------------------------------------------------*/
	public void incSpellPicks(int amount)
	{
		this.spellPicks += amount;
	}

	/*-------------------------------------------------------------------------*/
	public List<Spell> getSpellsThatCanBeLearned()
	{
		return this.getSpellBook().getSpellsThatCanBeLearned(this);
	}

	/*-------------------------------------------------------------------------*/
	public int getNextLevel()
	{
		return this.getCharacterClass().getExperienceTable().getNextLevelUp(this.getLevel());
	}

	/*-------------------------------------------------------------------------*/
	public int getLastLevel()
	{
		return this.getCharacterClass().getExperienceTable().getLastLevelUp(this.getLevel());
	}

	/*-------------------------------------------------------------------------*/

	/**
	 * @return true if this character has earned enough experience for a level up,
	 *         but hasn't taken it yet.
	 */
	public boolean isLevelUpPending()
	{
		return this.getNextLevel() <= this.experience;
	}

	/*-------------------------------------------------------------------------*/

	/**
	 * @return a list of modifiers that can be unlocked at this level
	 */
	public List<Stats.Modifier> getUnlockableModifiers()
	{
		List<Stats.Modifier> result = new ArrayList<Stats.Modifier>();

		for (Stats.Modifier modifier : Stats.regularModifiers)
		{
			if (!isActiveModifier(modifier)
				&& getCharacterClass().getUnlockModifiers().getModifier(modifier) > 0
				&& getCharacterClass().getUnlockModifiers().getModifier(modifier) <= this.getCurrentClassLevel())
			{
				result.add(modifier);
			}
		}

		return result;
	}

	/*-------------------------------------------------------------------------*/

	/**
	 * @return the attribute modifiers that can still be raised
	 */
	public List<Stats.Modifier> getRaisableAttributes()
	{
		List<Stats.Modifier> result = new ArrayList<Stats.Modifier>();

		for (Stats.Modifier modifier : Stats.attributeModifiers)
		{
			if (getBaseModifier(modifier) < getRace().getAttributeCeilings().getModifier(modifier))
			{
				result.add(modifier);
			}
		}

		return result;
	}

	/*-------------------------------------------------------------------------*/
	public boolean isActiveModifier(Stats.Modifier modifier)
	{
		return (activeModifiers.getModifier(modifier) > 0);
	}

	/*-------------------------------------------------------------------------*/
	public List<MagicSys.SpellBook> getUnlockableSpellLevels()
	{
		List<MagicSys.SpellBook> result = new ArrayList<MagicSys.SpellBook>();

		// todo????
		/*if (this.getCharacterClass().getAvailableSpellBooks() != null)
		{
			for (StartingSpellBook ssb : this.getCharacterClass().getAvailableSpellBooks())
			{
				int limit = this.getSpellBook().getLimit(ssb.getSpellBook());
				if (limit != -1 && limit < MagicSys.MAX_SPELL_LEVEL &&
					this.getCurrentClassLevel() >= ssb.getLevelOffset())
				{
					result.add(ssb.getSpellBook());
				}
			}
		}*/

		return result;
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * @return The names of all classes that this character is eligible to
	 * 	switch to. Excludes the current class.
	 */
	public List<String> getEligibleClasses()
	{
		List<String> result = new ArrayList<String>();
		List<String> temp;

		temp = new ArrayList<String>(Database.getInstance().getCharacterClassList());

		for (String s : temp)
		{
			CharacterClass c = Database.getInstance().getCharacterClass(s);

			if (!c.getName().equals(this.getCharacterClass().getName()) &&
				this.meetsRequirements(c.getStartingModifiers(), false, false))
			{
				result.add(s);
			}
		}

		return result;
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * @return The names of all classes that this character is ineligible to
	 * 	switch to. Excludes the current class.
	 */
	public List<String> getIneligibleClasses()
	{
		List<String> result = new ArrayList<String>();
		List<String> temp;

		temp = new ArrayList<String>(Database.getInstance().getCharacterClassList());

		result.addAll(temp);
		result.removeAll(getEligibleClasses());
		result.remove(getCharacterClass().getName());

		return result;
	}

	/*-------------------------------------------------------------------------*/
	public List<String> getEligibleSignatureWeapons()
	{
		if (getModifier(Stats.Modifier.SIGNATURE_WEAPON_ENGINEERING) > 0)
		{
			List<String> result = new ArrayList<String>();

			List<Item> items = getAllItems();
			for (Item item : items)
			{
				if ((engineeringOmnigun.getUpgrade(item.getName(), this) != null) ||
					(engineeringXbow.getUpgrade(item.getName(), this) != null) ||
					(engineeringHandCannon.getUpgrade(item.getName(), this) != null))
				{
					result.add(item.getName());
				}
			}

			return result;
		}
		else
		{
			return new ArrayList<String>();
		}
	}

	/*-------------------------------------------------------------------------*/
	public List<String> getEligibleModifierUpgrades()
	{
		List<String> result = new ArrayList<String>();

		if (getModifier(Stats.Modifier.MODIFIER_SELECTION_FAVOURED_ENEMY) > 0)
		{
			List<String> canBeUpgraded = new ArrayList<String>();
			List<Stats.Modifier> favouredEnemyModifiers = new ArrayList<Stats.Modifier>(Stats.favouredEnemies);

			int alreadyUpgraded = 0;
			for (Stats.Modifier m : favouredEnemyModifiers)
			{
				if (getBaseModifier(m) > 0)
				{
					alreadyUpgraded++;
				}
				else
				{
					canBeUpgraded.add(StringUtil.getModifierName(m));
				}
			}

			if (alreadyUpgraded < getModifier(Stats.Modifier.MODIFIER_SELECTION_FAVOURED_ENEMY))
			{
				result.addAll(canBeUpgraded);
			}
		}

		return result;
	}

	/*-------------------------------------------------------------------------*/
	public void upgradeSignatureWeapon(String fromItem)
	{
		Item item = getItem(fromItem);

		if (item == null)
		{
			throw new MazeException("Player does not possess ["+fromItem+"]");
		}

		String toItem = engineeringOmnigun.getUpgrade(item.getName(), this);
		if (toItem == null)
		{
			toItem = engineeringXbow.getUpgrade(item.getName(), this);
		}
		if (toItem == null)
		{
			toItem = engineeringHandCannon.getUpgrade(item.getName(), this);
		}

		if (toItem == null)
		{
			throw new MazeException("No upgrade path for ["+fromItem+"]");
		}

		ItemTemplate t = Database.getInstance().getItemTemplate(toItem);
		Item newItem = t.create();
		GameSys.getInstance().attemptManualIdentify(newItem, getActorGroup());
		this.replaceItem(item, newItem);
	}

	/*-------------------------------------------------------------------------*/
	public void downgradeSignatureWeapon(String fromItem)
	{
		String toItem = engineeringOmnigun.getUpgrade(fromItem, this);
		if (toItem == null)
		{
			toItem = engineeringXbow.getUpgrade(fromItem, this);
		}

		Item newItem = getItem(toItem);

		if (newItem == null)
		{
			throw new MazeException("Player does not possess ["+toItem+"]");
		}

		ItemTemplate t = Database.getInstance().getItemTemplate(fromItem);
		Item oldItem = t.create();
		GameSys.getInstance().attemptManualIdentify(oldItem, getActorGroup());
		this.replaceItem(newItem, oldItem);
		return;
	}

	/*-------------------------------------------------------------------------*/
	public void incLevel(int value)
	{
		int cur = getLevels().get(this.getCharacterClass().getName());
		getLevels().put(this.getCharacterClass().getName(), cur + value);
	}

	/*-------------------------------------------------------------------------*/
	public void unlockModifier(Stats.Modifier modifier)
	{
		if (isActiveModifier(modifier))
		{
			throw new MazeException("Modifier is already active: [" + modifier + "]");
		}
		activeModifiers.setModifier(modifier, 1);
	}

	/*-------------------------------------------------------------------------*/
	public void unlockSpellLevel(MagicSys.SpellBook book)
	{
		/*int curLimit = this.getSpellBook().getLimit(book);
		this.getSpellBook().setLimit(book, curLimit + 1);*/
	}

	/*-------------------------------------------------------------------------*/
	public void lockModifier(Stats.Modifier modifier)
	{
		if (!isActiveModifier(modifier))
		{
			throw new MazeException("Modifier is NOT active: [" + modifier + "]");
		}
		activeModifiers.setModifier(modifier, 0);
	}

	/*-------------------------------------------------------------------------*/
	public void lockSpellLevel(MagicSys.SpellBook book)
	{
		/*int curLimit = this.getSpellBook().getLimit(book);
		this.getSpellBook().setLimit(book, curLimit - 1);*/
	}

	/*-------------------------------------------------------------------------*/
	public void incModifier(Stats.Modifier mod, int amount)
	{
		this.getStats().incModifier(mod, amount);
	}

	/*-------------------------------------------------------------------------*/
	public void applyClassChange(CharacterClass c)
	{
		this.setCharacterClass(c);
		this.getLevels().put(c.getName(), 0);
	}

	/*-------------------------------------------------------------------------*/
	public void rollbackClassChange(CharacterClass oldClass,
		CharacterClass newClass)
	{
		this.getLevels().remove(newClass.getName());
		this.setCharacterClass(oldClass);
	}

	/*-------------------------------------------------------------------------*/
	public void setExperience(int experience)
	{
		this.experience = experience;
	}

	public void setKills(int kills)
	{
		this.kills = kills;
	}

	public void setKarma(int karma)
	{
		this.karma = karma;
	}

	public void setPortrait(String portrait)
	{
		this.portrait = portrait;
	}

	public void setPractice(Practice practice)
	{
		this.practice = practice;
	}

	public void setSpellPicks(int spellPicks)
	{
		this.spellPicks = spellPicks;
	}

	/*-------------------------------------------------------------------------*/

	@Override
	public String toString()
	{
		final StringBuilder sb = new StringBuilder();
		sb.append("PlayerCharacter");
		sb.append("{name='").append(getName()).append('\'');
		sb.append(", '").append(getGender().getName()).append('\'');
		sb.append(", '").append(getRace().getName()).append('\'');
		sb.append(", levels=").append(getLevels());
		sb.append('}');
		return sb.toString();
	}

	/*-------------------------------------------------------------------------*/
	public String modifyPersonalitySpeech(String speechKey, String text)
	{
		// Add the modifiers of all conditions on this character
		for (Condition c : ConditionManager.getInstance().getConditions(this))
		{
			text = c.modifyPersonalitySpeech(speechKey, text, personality);
		}

		return text;
	}

	/*-------------------------------------------------------------------------*/
	public StatModifier getBaseModifiers()
	{
		return getStats().getModifiers();
	}

	/*-------------------------------------------------------------------------*/
	public static class EquipableSlots
	{
		public static final int PRIMARY_WEAPON = 0;
		public static final int SECONDARY_WEAPON = 1;
		public static final int HELM = 2;
		public static final int TORSO_ARMOUR = 3;
		public static final int LEG_ARMOUR = 4;
		public static final int GLOVES = 5;
		public static final int BOOTS = 6;
		public static final int BANNER_ITEM = 7;
		public static final int MISC_ITEM_1 = 8;
		public static final int MISC_ITEM_2 = 9;

		public static final int NUMBER_OF_SLOTS = 10;

		private static Map<Integer, String> desc = new HashMap<Integer, String>();

		static Map<String, Integer> types = new HashMap<String, Integer>();

		static
		{
			for (int i = 0; i < NUMBER_OF_SLOTS; i++)
			{
				types.put(describe(i), i);
			}
			desc.put(PRIMARY_WEAPON, "Primary Weapon");
			desc.put(SECONDARY_WEAPON, "Secondary Weapon");
			desc.put(HELM, "Helm");
			desc.put(TORSO_ARMOUR, "Torso Armour");
			desc.put(LEG_ARMOUR, "Leg Armour");
			desc.put(GLOVES, "Gloves");
			desc.put(BOOTS, "Boots");
			desc.put(BANNER_ITEM, "Banner Item");
			desc.put(MISC_ITEM_1, "Misc Item");
			desc.put(MISC_ITEM_2, "Misc Item");
		}

		public static int valueOf(String s)
		{
			if (types.containsKey(s))
			{
				return types.get(s);
			}
			else
			{
				throw new MazeException("Invalid type [" + s + "]");
			}
		}

		public static String describe(int slot)
		{
			return desc.get(slot);
		}
	}

	/*-------------------------------------------------------------------------*/
	public static class BodyParts
	{
		public static final String HEAD = "head";
		public static final String TORSO = "torso";
		public static final String LEG = "leg";
		public static final String HAND = "hand";
		public static final String FOOT = "foot";
	}
}
