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
import mclachlan.maze.game.Maze;
import mclachlan.maze.game.MazeScript;
import mclachlan.maze.stat.combat.*;
import mclachlan.maze.stat.condition.Condition;
import mclachlan.maze.stat.condition.ConditionManager;
import mclachlan.maze.stat.magic.MagicSys;
import mclachlan.maze.stat.magic.Spell;
import mclachlan.maze.stat.magic.SpellBook;
import mclachlan.maze.stat.magic.Value;
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

	/*-------------------------------------------------------------------------*/
	private static SignatureWeaponUpgradePath
		engineeringOmnigun, engineeringXbow, engineeringHandCannon;

	static
	{
		StatModifier req = new StatModifier();

		engineeringOmnigun = new SignatureWeaponUpgradePath();

		req.setModifier(Stats.Modifiers.ENGINEERING, 1);
		engineeringOmnigun.addUpgrade("Omnigun Prototype", "Omnigun Mk1", req);
		req.setModifier(Stats.Modifiers.ENGINEERING, 3);
		engineeringOmnigun.addUpgrade("Omnigun Mk1", "Omnigun Mk2", req);
		req.setModifier(Stats.Modifiers.ENGINEERING, 6);
		engineeringOmnigun.addUpgrade("Omnigun Mk2", "Omnigun Mk3", req);
		req.setModifier(Stats.Modifiers.ENGINEERING, 9);
		engineeringOmnigun.addUpgrade("Omnigun Mk3", "Omnigun Mk4", req);
		req.setModifier(Stats.Modifiers.ENGINEERING, 12);
		engineeringOmnigun.addUpgrade("Omnigun Mk4", "Omnigun Mk5", req);
		req.setModifier(Stats.Modifiers.ENGINEERING, 15);
		engineeringOmnigun.addUpgrade("Omnigun Mk5", "Omnigun Mk6", req);

		req = new StatModifier();

		engineeringXbow = new SignatureWeaponUpgradePath();

		req.setModifier(Stats.Modifiers.ENGINEERING, 1);
		engineeringXbow.addUpgrade("Light Crossbow", "X-bow Mk1", req);
		req.setModifier(Stats.Modifiers.ENGINEERING, 3);
		engineeringXbow.addUpgrade("X-bow Mk1", "X-bow Mk2", req);
		req.setModifier(Stats.Modifiers.ENGINEERING, 6);
		engineeringXbow.addUpgrade("X-bow Mk2", "X-bow Mk3", req);
		req.setModifier(Stats.Modifiers.ENGINEERING, 9);
		engineeringXbow.addUpgrade("X-bow Mk3", "X-bow Mk4", req);
		req.setModifier(Stats.Modifiers.ENGINEERING, 12);
		engineeringXbow.addUpgrade("X-bow Mk4", "X-bow Mk5", req);
		req.setModifier(Stats.Modifiers.ENGINEERING, 15);
		engineeringXbow.addUpgrade("X-bow Mk5", "X-bow Mk6", req);

		req = new StatModifier();

		engineeringHandCannon = new SignatureWeaponUpgradePath();

		req.setModifier(Stats.Modifiers.ENGINEERING, 1);
		engineeringHandCannon.addUpgrade("Flintlock Pistol", "Hand Cannon Mk1", req);
		req.setModifier(Stats.Modifiers.ENGINEERING, 3);
		engineeringHandCannon.addUpgrade("Hand Cannon Mk1", "Hand Cannon Mk2", req);
		req.setModifier(Stats.Modifiers.ENGINEERING, 6);
		engineeringHandCannon.addUpgrade("Hand Cannon Mk2", "Hand Cannon Mk3", req);
		req.setModifier(Stats.Modifiers.ENGINEERING, 9);
		engineeringHandCannon.addUpgrade("Hand Cannon Mk3", "Hand Cannon Mk4", req);
		req.setModifier(Stats.Modifiers.ENGINEERING, 12);
		engineeringHandCannon.addUpgrade("Hand Cannon Mk4", "Hand Cannon Mk5", req);
		req.setModifier(Stats.Modifiers.ENGINEERING, 15);
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
		StatModifier activeModifiers)
	{
		super(name, gender, race, characterClass, race.getBodyParts(),
			race.getNaturalWeapons(), levels, stats, spellBook, inventory);

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
		this.portrait = portrait;
		this.practice = practice;
		this.spellPicks = spellPicks;
		this.personality = personality;
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
			race.getNaturalWeapons(), levels,
			new Stats(), null, new Inventory(MAX_PACK_ITEMS));

		this.portrait = portrait;
		this.activeModifiers = activeModifiers;

		this.practice = new Practice();

		this.kills = 0;

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
		this.portrait = pc.portrait;
		this.practice = new Practice(pc.practice);
		this.spellPicks = pc.spellPicks;
		this.personality = pc.personality;

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
		for (String modifier : m.getModifiers().keySet())
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
		for (String mod : m.getModifiers().keySet())
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
		boolean by_gender = item.getUsableByGender().contains(this.getGender().getName());
		boolean by_class =
			item.getUsableByCharacterClass().contains(this.getCharacterClass().getName());
		boolean by_race = item.getUsableByRace().contains(this.getRace().getName());
		boolean by_req = meetsRequirements(item.getEquipRequirements());

		return by_gender && by_class && by_race && by_req;
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public List<SpellLikeAbility> getSpellLikeAbilities()
	{
		List<SpellLikeAbility> result = new ArrayList<SpellLikeAbility>();

		// race abilities
		if (getRace().getSpecialAbility() != null)
		{
			result.add(new SpellLikeAbility(
				getRace().getSpecialAbility(),
				new Value(getLevel(), Value.SCALE.NONE)));
		}

		// class abilities
		List<LevelAbility> abilities = getLevelAbilities();

		for (LevelAbility la : abilities)
		{
			if (la instanceof SpecialAbilityLevelAbility)
			{
				result.add(((SpecialAbilityLevelAbility)la).getAbility());
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
	public HashMapMutableTree<ActorActionOption> getCharacterActionOptions(Combat combat)
	{
		HashMapMutableTree<ActorActionOption> result =
			new HashMapMutableTree<ActorActionOption>();

		boolean alive = GameSys.getInstance().isActorAlive(this);
		boolean aware = GameSys.getInstance().isActorAware(this);
		if (combat == null)
		{
			if (alive && aware)
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
		}
		else
		{
			if (alive && aware)
			{
				// Attack option if there are attackable groups
				List<ActorGroup> attackableGroups = GameSys.getInstance().getAttackableGroups(this, combat);
				if (attackableGroups != null && !attackableGroups.isEmpty())
				{
					result.add(new AttackOption(), null);
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

				// special abilities
				if (this.getSpellLikeAbilities() != null)
				{
					for (SpellLikeAbility sla : this.getSpellLikeAbilities())
					{
						if (sla.isUsableDuringCombat() && sla.meetsRequirements(this))
						{
							result.add(new SpecialAbilityOption(sla), null);
						}
					}
				}
			}
			else
			{
				// actor cannot do anything
				result.add(ActorActionOption.INTEND_NOTHING, null);
			}
		}

		// set the actor for all action options
		for (ActorActionOption aao : result.getNodes())
		{
			aao.setActor(this);
		}

		return result;
	}

	/*-------------------------------------------------------------------------*/
	public List<CombatAction> getCombatActions(ActorActionIntention intention)
	{
		List<CombatAction> result = new ArrayList<CombatAction>();

		if (getHitPoints().getCurrent() <= 0)
		{
			return result;
		}
		else if (intention instanceof AttackIntention)
		{
			AttackIntention atkInt = (AttackIntention)intention;

			ActorGroup targetGroup = atkInt.getActorGroup();

			boolean canAttackWithPrimary =
				getPrimaryWeapon() == null || (getPrimaryWeapon() != null && getPrimaryWeapon().isWeapon());

			boolean canAttackWithSecondary =
				getSecondaryWeapon() != null && getSecondaryWeapon().isWeapon()
					&& (getPrimaryWeapon().getAmmoRequired() != null && !getPrimaryWeapon().getAmmoRequired().contains(getSecondaryWeapon().isAmmoType()))
				||
				getSecondaryWeapon() == null && getModifier(Stats.Modifiers.MARTIAL_ARTS) > 0;

			Item weapon;
			if (getPrimaryWeapon() != null)
			{
				weapon = getPrimaryWeapon();
			}
			else
			{
				weapon = GameSys.getInstance().getUnarmedWeapon(this, true);
			}

			// primary weapon
			if (canAttackWithPrimary)
			{
				// basic attack with primary weapon, no modifiers
				int nrAttacks = GameSys.getInstance().getNrAttacks(this, true);

				if (weapon.getAmmoRequired() == null
					|| weapon.getAmmoRequired().contains(ItemTemplate.AmmoType.SELF)
					|| getSecondaryWeapon() != null &&
					weapon.getAmmoRequired().contains(getSecondaryWeapon().isAmmoType()))
				{
					MazeScript missileScript;
					if (weapon.isRanged())
					{
						missileScript = getSecondaryWeapon().getAttackScript();
					}
					else
					{
						missileScript = weapon.getAttackScript();
					}

					for (int i = 0; i < nrAttacks; i++)
					{
						// ammo requirements ok.  Attack
						int defaultDamageType = weapon.getDefaultDamageType();
						if (weapon.getAmmoRequired() != null &&
							getSecondaryWeapon() != null &&
							weapon.getAmmoRequired().contains((getSecondaryWeapon()).isAmmoType()))
						{
							defaultDamageType = getSecondaryWeapon().getDefaultDamageType();
						}

						AttackAction action = new AttackAction(
							targetGroup,
							weapon,
							-1,
							missileScript,
							true,
							GameSys.getInstance().isLightningStrike(this, weapon),
							defaultDamageType);
						action.setModifier(Stats.Modifiers.INITIATIVE, -5 * i + weapon.getToInitiative());
						if (canAttackWithSecondary && getSecondaryWeapon() != null)
						{
							GameSys.getInstance().setDualWeaponPenalties(action, this, true);
						}
						result.add(action);
					}
				}
				else
				{
					// cannot attack
					result.add(new DefendAction());
					//todo: return from here?
				}
			}

			if (canAttackWithSecondary)
			{
				Item attackWith;
				if (getSecondaryWeapon() != null)
				{
					attackWith = getSecondaryWeapon();
				}
				else
				{
					attackWith = weapon;
				}

				// basic attack with secondary weapon:
				// -5 intiative
				// -5 to hit
				int nrAttacks = GameSys.getInstance().getNrAttacks(this, false);

				for (int i = 0; i < nrAttacks; i++)
				{
					AttackAction secAction = new AttackAction(
						targetGroup,
						attackWith,
						-1,
						attackWith.getAttackScript(),
						true,
						false,
						attackWith.getDefaultDamageType());
					secAction.setModifier(Stats.Modifiers.INITIATIVE, -5 * (i + 1) + weapon.getToInitiative());
					if (getSecondaryWeapon() != null)
					{
						// dual weapon penalties do not apply to unarmed combat
						GameSys.getInstance().setDualWeaponPenalties(secAction, this, false);
					}
					result.add(secAction);
				}
			}
		}
		else if (intention instanceof DefendIntention)
		{
			result.add(new DefendAction());
		}
		else if (intention instanceof HideIntention)
		{
			result.add(new HideAction());
		}
		else if (intention instanceof SpellIntention)
		{
			SpellIntention si = (SpellIntention)intention;

			result.add(new SpellAction(si.getTarget(),
				si.getSpell(),
				si.getCastingLevel()));
		}
		else if (intention instanceof SpecialAbilityIntention)
		{
			SpecialAbilityIntention si = (SpecialAbilityIntention)intention;

			result.add(new SpecialAbilityAction(
				si.getSpell().getDescription(),
				si.getTarget(),
				si.getSpell(),
				si.getCastingLevel()));
		}
		else if (intention instanceof UseItemIntention)
		{
			UseItemIntention ui = (UseItemIntention)intention;

			Item item = ui.getItem();

			result.add(new UseItemAction(item,
				ui.getTarget()));
		}
		else if (intention instanceof EquipIntention)
		{
			result.add(new EquipAction());
		}
		else if (intention instanceof RunAwayIntention)
		{
			result.add(new RunAwayAction());
		}
		else
		{
			throw new MazeException("Unrecognised combat intention: " + intention);
		}

		return result;
	}

	/*-------------------------------------------------------------------------*/

	//
	// These methods here mostly for backwards compatibility of the interface.
	//

	public Item getPrimaryWeapon()
	{
		return getEquippedItem(PRIMARY_WEAPON, 0);
	}

	public void setPrimaryWeapon(Item primaryWeapon)
	{
		if (this.hasEquipableSlot(PRIMARY_WEAPON))
		{
			setEquippedItem(PRIMARY_WEAPON, primaryWeapon, 0);
			this.updateCurseState(primaryWeapon, Item.CursedState.DISCOVERED);
		}
	}

	public Item getSecondaryWeapon()
	{
		return getEquippedItem(SECONDARY_WEAPON, 0);
	}

	public void setSecondaryWeapon(Item secondaryWeapon)
	{
		if (this.hasEquipableSlot(SECONDARY_WEAPON))
		{
			setEquippedItem(SECONDARY_WEAPON, secondaryWeapon, 0);
			updateCurseState(secondaryWeapon, Item.CursedState.DISCOVERED);
		}
	}

	public Item getAltPrimaryWeapon()
	{
		return getEquippedItem(PRIMARY_WEAPON, 1);
	}

	public void setAltPrimaryWeapon(Item altPrimaryWeapon)
	{
		if (this.hasEquipableSlot(PRIMARY_WEAPON, 1))
		{
			// doesn't set the curse state
			setEquippedItem(PRIMARY_WEAPON, altPrimaryWeapon, 1);
		}
	}

	public Item getAltSecondaryWeapon()
	{
		return getEquippedItem(SECONDARY_WEAPON, 1);
	}

	public void setAltSecondaryWeapon(Item altSecondaryWeapon)
	{
		if (this.hasEquipableSlot(SECONDARY_WEAPON, 1))
		{
			// doesn't set the curse state
			setEquippedItem(SECONDARY_WEAPON, altSecondaryWeapon, 1);
		}
	}

	public Item getBannerItem()
	{
		return getEquippedItem(BANNER_ITEM, 0);
	}

	public void setBannerItem(Item bannerItem)
	{
		if (this.hasEquipableSlot(BANNER_ITEM))
		{
			setEquippedItem(BANNER_ITEM, bannerItem, 0);
			updateCurseState(bannerItem, Item.CursedState.DISCOVERED);
		}
	}

	public Item getMiscItem1()
	{
		return getEquippedItem(MISC_ITEM, 0);
	}

	public Item getMiscItem2()
	{
		return getEquippedItem(MISC_ITEM, 1);
	}

	public void setMiscItem1(Item miscItem1)
	{
		if (this.hasEquipableSlot(MISC_ITEM))
		{
			setEquippedItem(MISC_ITEM, miscItem1, 0);
			updateCurseState(miscItem1, Item.CursedState.DISCOVERED);
		}
	}

	public void setMiscItem2(Item miscItem2)
	{
		if (this.hasEquipableSlot(MISC_ITEM, 1))
		{
			setEquippedItem(MISC_ITEM, miscItem2, 1);
			updateCurseState(miscItem2, Item.CursedState.DISCOVERED);
		}
	}

	public Item getBoots()
	{
		return getEquippedItem(BOOTS, 0);
	}

	public Item getGloves()
	{
		return getEquippedItem(GLOVES, 0);
	}

	public Item getHelm()
	{
		return getEquippedItem(HELM, 0);
	}

	public Item getLegArmour()
	{
		return getEquippedItem(LEG_ARMOUR, 0);
	}

	public Item getTorsoArmour()
	{
		return getEquippedItem(TORSO_ARMOUR, 0);
	}

	public void setBoots(Item boots)
	{
		if (this.hasEquipableSlot(BOOTS))
		{
			setEquippedItem(BOOTS, boots, 0);
			updateCurseState(boots, Item.CursedState.DISCOVERED);
		}
	}

	public void setGloves(Item gloves)
	{
		if (this.hasEquipableSlot(GLOVES))
		{
			setEquippedItem(GLOVES, gloves, 0);
			updateCurseState(gloves, Item.CursedState.DISCOVERED);
		}
	}

	public void setHelm(Item helm)
	{
		if (this.hasEquipableSlot(HELM))
		{
			setEquippedItem(HELM, helm, 0);
			updateCurseState(helm, Item.CursedState.DISCOVERED);
		}
	}

	public void setLegArmour(Item legArmour)
	{
		if (this.hasEquipableSlot(LEG_ARMOUR))
		{
			setEquippedItem(LEG_ARMOUR, legArmour, 0);
			updateCurseState(legArmour, Item.CursedState.DISCOVERED);
		}
	}

	public void setTorsoArmour(Item torsoArmour)
	{
		if (this.hasEquipableSlot(TORSO_ARMOUR))
		{
			setEquippedItem(TORSO_ARMOUR, torsoArmour, 0);
			updateCurseState(torsoArmour, Item.CursedState.DISCOVERED);
		}
	}

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
	public int getBaseModifier(String modifier)
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

	public String getDisplayName()
	{
		return getName();
	}

	/**
	 * a PCs type is his or her character class
	 */
	public String getType()
	{
		return this.getCharacterClass().getName();
	}

	public int getExperience()
	{
		return experience;
	}

	public int getKills()
	{
		return kills;
	}

	public void removeItem(Item item, boolean removeWholeStack)
	{
		if (item.getStack().getCurrent() > 1 && !removeWholeStack)
		{
			item.getStack().decCurrent(1);
			if (item.getStack().getCurrent() > 0)
			{
				// reset the charges to represent a new item on top of the stack
				item.getCharges().setCurrentToMax();
				return;
			}
		}

		// otherwise we actually have to find the damn thing
		if (item == getPrimaryWeapon())
		{
			setPrimaryWeapon(null);
		}
		else if (item == getAltPrimaryWeapon())
		{
			setAltPrimaryWeapon(null);
		}
		else if (item == getSecondaryWeapon())
		{
			setSecondaryWeapon(null);
		}
		else if (item == getAltSecondaryWeapon())
		{
			setAltPrimaryWeapon(null);
		}
		else if (item == getHelm())
		{
			setHelm(null);
		}
		else if (item == getTorsoArmour())
		{
			setTorsoArmour(null);
		}
		else if (item == getGloves())
		{
			setGloves(null);
		}
		else if (item == getLegArmour())
		{
			setLegArmour(null);
		}
		else if (item == getBoots())
		{
			setBoots(null);
		}
		else if (item == getBannerItem())
		{
			setBannerItem(null);
		}
		else if (item == getMiscItem1())
		{
			setMiscItem1(null);
		}
		else if (item == getMiscItem2())
		{
			setMiscItem2(null);
		}
		else if (getInventory().contains(item))
		{
			getInventory().remove(item);
		}
	}

	/*-------------------------------------------------------------------------*/
	public void replaceItem(Item fromItem, Item toItem)
	{
		if (fromItem == getPrimaryWeapon())
		{
			setPrimaryWeapon(toItem);
		}
		else if (fromItem == getAltPrimaryWeapon())
		{
			setAltPrimaryWeapon(toItem);
		}
		else if (fromItem == getSecondaryWeapon())
		{
			setSecondaryWeapon(toItem);
		}
		else if (fromItem == getAltSecondaryWeapon())
		{
			setAltPrimaryWeapon(toItem);
		}
		else if (fromItem == getHelm())
		{
			setHelm(toItem);
		}
		else if (fromItem == getTorsoArmour())
		{
			setTorsoArmour(toItem);
		}
		else if (fromItem == getGloves())
		{
			setGloves(toItem);
		}
		else if (fromItem == getLegArmour())
		{
			setLegArmour(toItem);
		}
		else if (fromItem == getBoots())
		{
			setBoots(toItem);
		}
		else if (fromItem == getBannerItem())
		{
			setBannerItem(toItem);
		}
		else if (fromItem == getMiscItem1())
		{
			setMiscItem1(toItem);
		}
		else if (fromItem == getMiscItem2())
		{
			setMiscItem2(toItem);
		}
		else if (getInventory().contains(fromItem))
		{
			getInventory().add(toItem, getInventory().indexOf(fromItem));
		}
	}

	public void removeItem(String itemName, boolean removeWholeStack)
	{
		Item item = getItem(itemName);

		if (item != null)
		{
			removeItem(item, removeWholeStack);
		}
	}

	private Item getItem(String itemName)
	{
		for (Item item : getInventory())
		{
			if (item != null && item.getName().equals(itemName))
			{
				return item;
			}
		}

		if (getPrimaryWeapon() != null && getPrimaryWeapon().getName().equals(itemName))
		{
			return getPrimaryWeapon();
		}
		if (getAltPrimaryWeapon() != null && getAltPrimaryWeapon().getName().equals(itemName))
		{
			return getAltPrimaryWeapon();
		}
		if (getSecondaryWeapon() != null && getSecondaryWeapon().getName().equals(itemName))
		{
			return getSecondaryWeapon();
		}
		if (getAltSecondaryWeapon() != null && getAltSecondaryWeapon().getName().equals(itemName))
		{
			return getAltSecondaryWeapon();
		}
		if (getHelm() != null && getHelm().getName().equals(itemName))
		{
			return getHelm();
		}
		if (getTorsoArmour() != null && getTorsoArmour().getName().equals(itemName))
		{
			return getTorsoArmour();
		}
		if (getLegArmour() != null && getLegArmour().getName().equals(itemName))
		{
			return getLegArmour();
		}
		if (getGloves() != null && getGloves().getName().equals(itemName))
		{
			return getGloves();
		}
		if (getBoots() != null && getBoots().getName().equals(itemName))
		{
			return getBoots();
		}
		if (getMiscItem1() != null && getMiscItem1().getName().equals(itemName))
		{
			return getMiscItem1();
		}
		if (getMiscItem2() != null && getMiscItem2().getName().equals(itemName))
		{
			return getMiscItem2();
		}
		if (getBannerItem() != null && getBannerItem().getName().equals(itemName))
		{
			return getBannerItem();
		}

		return null;
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
	public List<String> getUnlockableModifiers()
	{
		List<String> result = new ArrayList<String>();

		for (String modifier : Stats.regularModifiers)
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
	public List<String> getRaisableAttributes()
	{
		List<String> result = new ArrayList<String>();

		for (String modifier : Stats.attributeModifiers)
		{
			if (getBaseModifier(modifier) < getRace().getAttributeCeilings().getModifier(modifier))
			{
				result.add(modifier);
			}
		}

		return result;
	}

	/*-------------------------------------------------------------------------*/
	public boolean isActiveModifier(String modifier)
	{
		return (activeModifiers.getModifier(modifier) > 0);
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public List<AttackWith> getAttackWithOptions()
	{
		ArrayList<AttackWith> result = new ArrayList<AttackWith>();
		if (getPrimaryWeapon() != null)
		{
			result.add(getPrimaryWeapon());
		}
		else
		{
			result.add(GameSys.getInstance().getUnarmedWeapon(this, true));
		}

		if (getSecondaryWeapon() != null)
		{
			result.add(getSecondaryWeapon());
		}
		return result;
	}

	/*-------------------------------------------------------------------------*/
	public List<MagicSys.SpellBook> getUnlockableSpellLevels()
	{
		List<MagicSys.SpellBook> result = new ArrayList<MagicSys.SpellBook>();

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
		if (getModifier(Stats.Modifiers.SIGNATURE_WEAPON_ENGINEERING) > 0)
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
	public void unlockModifier(String modifier)
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
	public void lockModifier(String modifier)
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
	public void incModifier(String mod, int amount)
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
