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
import mclachlan.maze.data.Database;
import mclachlan.maze.data.StringUtil;
import mclachlan.maze.game.*;
import mclachlan.maze.map.Portal;
import mclachlan.maze.map.Tile;
import mclachlan.maze.map.Trap;
import mclachlan.maze.map.script.LockOrTrap;
import mclachlan.maze.map.script.LoseExperienceEvent;
import mclachlan.maze.stat.combat.*;
import mclachlan.maze.stat.combat.event.AttackEvent;
import mclachlan.maze.stat.combat.event.StrikeEvent;
import mclachlan.maze.stat.condition.Condition;
import mclachlan.maze.stat.magic.*;
import mclachlan.maze.stat.npc.Npc;
import mclachlan.maze.stat.npc.NpcFaction;
import mclachlan.maze.util.MazeException;

import static mclachlan.maze.stat.ItemTemplate.Type;

public class GameSys
{
	private static Dice stealth2d2 = new Dice(2, 2, 0);
	private static Dice stealth4d2 = new Dice(4, 2, 0);

	private static StatModifier modEncumbrance = new StatModifier();
	private static StatModifier heavyEncumbrance = new StatModifier();
	private static StatModifier insaneEncumbrance = new StatModifier();

	private static Random r = new Random();

	static
	{
		modEncumbrance.setModifier(Stats.Modifier.INITIATIVE, -2);
		modEncumbrance.setModifier(Stats.Modifier.SNEAKING, -2);

		heavyEncumbrance.setModifier(Stats.Modifier.INITIATIVE, -5);
		heavyEncumbrance.setModifier(Stats.Modifier.ATTACK, -5);
		heavyEncumbrance.setModifier(Stats.Modifier.DEFENCE, -5);
		heavyEncumbrance.setModifier(Stats.Modifier.SKILL, -2);
		heavyEncumbrance.setModifier(Stats.Modifier.SNEAKING, -4);
		heavyEncumbrance.setModifier(Stats.Modifier.LIGHTNING_STRIKE_AXE, -1);
		heavyEncumbrance.setModifier(Stats.Modifier.LIGHTNING_STRIKE_DAGGER, -1);
		heavyEncumbrance.setModifier(Stats.Modifier.LIGHTNING_STRIKE_MACE, -1);
		heavyEncumbrance.setModifier(Stats.Modifier.LIGHTNING_STRIKE_SPEAR, -1);
		heavyEncumbrance.setModifier(Stats.Modifier.LIGHTNING_STRIKE_STAFF, -1);
		heavyEncumbrance.setModifier(Stats.Modifier.LIGHTNING_STRIKE_SWORD, -1);
		heavyEncumbrance.setModifier(Stats.Modifier.LIGHTNING_STRIKE_UNARMED, -1);
		heavyEncumbrance.setModifier(Stats.Modifier.ARROW_CUTTING, -20);
		heavyEncumbrance.setModifier(Stats.Modifier.AMBUSHER, -1);
		heavyEncumbrance.setModifier(Stats.Modifier.DODGE, -20);
		heavyEncumbrance.setModifier(Stats.Modifier.PARRY, -20);

		for (Stats.Modifier mod : heavyEncumbrance.getModifiers().keySet())
		{
			insaneEncumbrance.setModifier(mod, heavyEncumbrance.getModifier(mod)*4);
		}
	}

	/*-------------------------------------------------------------------------*/
	public static GameSys getInstance()
	{
		return Maze.getInstance().getGameSys();
	}

	/*-------------------------------------------------------------------------*/
	public Combat.AmbushStatus determineAmbushStatus(PlayerParty party, List<FoeGroup> foes)
	{
		Combat.AmbushStatus result;

		// determine surprise
		int partyValue = GameSys.getInstance().getStealthValue(
			Maze.getInstance().getCurrentTile(), party);
		int foesValue = GameSys.getInstance().getStealthValue(
			Maze.getInstance().getCurrentTile(), foes);

		partyValue += Dice.d10.roll();
		foesValue += Dice.d10.roll();

		if (partyValue > foesValue+20)
		{
			// check and see if there are any "cannot evade" foes
			boolean cannotEvade = false;
			for (FoeGroup fg : foes)
			{
				for(Foe f : fg.getFoes())
				{
					cannotEvade |= f.cannotBeEvaded();
				}
			}
			if (cannotEvade)
			{
				result = Combat.AmbushStatus.PARTY_MAY_AMBUSH_FOES;
			}
			else
			{
				result = Combat.AmbushStatus.PARTY_MAY_AMBUSH_OR_EVADE_FOES;
			}
		}
		else if (partyValue > foesValue+10)
		{
			result = Combat.AmbushStatus.PARTY_MAY_AMBUSH_FOES;
		}
		else if (foesValue > partyValue+20)
		{
			result = Combat.AmbushStatus.FOES_MAY_AMBUSH_OR_EVADE_PARTY;
		}
		else if (foesValue > partyValue+10)
		{
			result = Combat.AmbushStatus.FOES_MAY_AMBUSH_PARTY;
		}
		else
		{
			result = Combat.AmbushStatus.NONE;
		}

		return result;
	}

	/*-------------------------------------------------------------------------*/
	public NpcFaction.Attitude calcAttitudeChange(
		NpcFaction.Attitude current,
		NpcFaction.AttitudeChange change)
	{
		switch (change)
		{
			case BETTER:
				switch (current)
				{
					case ATTACKING:
						return NpcFaction.Attitude.AGGRESSIVE;
					case AGGRESSIVE:
						return NpcFaction.Attitude.WARY;
					case WARY:
						return NpcFaction.Attitude.NEUTRAL;
					case SCARED:
						return NpcFaction.Attitude.WARY;
					case NEUTRAL:
						return NpcFaction.Attitude.FRIENDLY;
					case FRIENDLY:
						// scripted events only can move it to ALLIED
						return current;
					case ALLIED:
						// no better
						return current;
				}
				break;
			case WORSE:
				switch (current)
				{
					case ATTACKING:
						// no worse
						return current;
					case AGGRESSIVE:
						return NpcFaction.Attitude.ATTACKING;
					case WARY:
						// todo: transition to AGGRESSIVE or SCARED based on party level?
						return NpcFaction.Attitude.AGGRESSIVE;
					case SCARED:
						// todo: flee instead?
						return NpcFaction.Attitude.ATTACKING;
					case NEUTRAL:
						return NpcFaction.Attitude.AGGRESSIVE;
					case FRIENDLY:
						return NpcFaction.Attitude.NEUTRAL;
					case ALLIED:
						return NpcFaction.Attitude.FRIENDLY;
				}
				break;
			default:
				throw new MazeException(""+change);
		}

		return current;
	}

	/*-------------------------------------------------------------------------*/
	public Item createItemForStartingKit(String name, PlayerCharacter pc)
	{
		if (name == null)
		{
			return null;
		}

		ItemTemplate template = Database.getInstance().getItemTemplate(name);
		Item result;
		if (template.getType() == ItemTemplate.Type.AMMUNITION)
		{
			result = template.create(Math.min(50, template.getMaxItemsPerStack()));
		}
		else if (template.getType() == ItemTemplate.Type.THROWN_WEAPON)
		{
			result = template.create(Math.min(20, template.getMaxItemsPerStack()));
		}
		else if (template.getType() == ItemTemplate.Type.BOMB ||
			template.getType() == ItemTemplate.Type.POTION ||
			template.getType() == ItemTemplate.Type.POWDER ||
			template.getType() == ItemTemplate.Type.FOOD)
		{
			result = template.create(Math.min(5, template.getMaxItemsPerStack()));
		}
		else
		{
			result = template.create();
		}

		result.setIdentificationState(Item.IdentificationState.IDENTIFIED);
		result.setCursedState(Item.CursedState.DISCOVERED);

		// check and see whether this player can use this item
		if (pc.isEquippableItem(result))
		{
			return result;
		}
		else
		{
			return null;
		}
	}

	/*-------------------------------------------------------------------------*/
	public AttackType getAttackType(AttackWith attackWith)
	{
		String[] attackTypes = attackWith.getAttackTypes();

		if (attackTypes == null)
		{
			return AttackType.NULL_ATTACK_TYPE;
		}

		AttackType attackType = Database.getInstance().getAttackType(
			attackTypes[nextInt(attackTypes.length)]);

/*		boolean isBackstabCapable = attackWith.isBackstabCapable();
		boolean isSnipeCapable = attackWith.isSnipeCapable();
		if (isBackstabCapable || isSnipeCapable)
		{
			int characterSkill = attackAction.getActor().getModifier(attackType.getName());

			int stealthSkill;
			if (isBackstabCapable)
			{
				stealthSkill = attackAction.getActor().getModifier(Stats.Modifiers.BACKSTAB);
			}
			else if (isSnipeCapable)
			{
				stealthSkill = attackAction.getActor().getModifier(Stats.Modifiers.SNIPE);
			}
			else
			{
				throw new MazeException("FOOBAR: "+attackWith);
			}

			if (stealthSkill >= characterSkill)
			{
				int ambushCost = getInstance().getBackstabSnipeCost(
					attackAction.getActor(), attackAction.getDefender());

				if (ambushCost <= attackAction.getActor().getActionPoints().getCurrent())
				{
					if (isBackstabCapable)
					{
						attackType = Database.getInstance().getAttackType(Stats.Modifiers.BACKSTAB);
						attackAction.setDamageType(attackWith.getDefaultDamageType());
					}
					else if (isSnipeCapable)
					{
						attackType = Database.getInstance().getAttackType(Stats.Modifiers.SNIPE);
						MagicSys.SpellEffectType defaultDamageType = attackWith.getDefaultDamageType();

						// special case to get snipe damage type to be ammo default damage type
						if (attackAction.getActor() instanceof PlayerCharacter)
						{
							PlayerCharacter pc = (PlayerCharacter)attackAction.getActor();

							Item weapon = pc.getPrimaryWeapon();
							if (weapon == attackWith)
							{
								Item secondaryWeapon = pc.getSecondaryWeapon();
								if (weapon.getAmmoRequired() != null &&
									secondaryWeapon != null &&
									weapon.getAmmoRequired().contains(secondaryWeapon.isAmmoType()))
								{
									defaultDamageType = secondaryWeapon.getDefaultDamageType();
								}
							}
						}

						attackAction.setDamageType(defaultDamageType);
					}
					else
					{
						throw new MazeException("FOOBAR: "+attackWith);
					}
				}
			}
		}*/

		return attackType;
	}

	/*-------------------------------------------------------------------------*/

	/**
	 * @return the damage type of the given AttackWith
	 */
	public MagicSys.SpellEffectType getAttackWithDamageType(UnifiedActor source, AttackWith attackWith)
	{
		if (attackWith.isRanged())
		{
			// ranged weapon uses the default damage type of the ammo
			Item secondaryWeapon = source.getSecondaryWeapon();
			if (attackWith.getAmmoRequired() != null &&
				secondaryWeapon != null &&
				attackWith.getAmmoRequired().contains(secondaryWeapon.isAmmoType()))
			{
				return secondaryWeapon.getDefaultDamageType();
			}
		}

		// melee weapon just uses its default damage type
		return attackWith.getDefaultDamageType();
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * @return
	 * 	A random integer between 0 and max-1 inclusive
	 */
	public int nextInt(int max)
	{
		return r.nextInt(max);
	}

	/*-------------------------------------------------------------------------*/
	public int calcHitPercent(
		StrikeEvent event)
	{
		int result;

		UnifiedActor attacker = event.getAttacker();
		UnifiedActor defender = event.getDefender();

		Maze.log(Log.DEBUG, " Calc hit % for "+attacker.getName()+" vs "+defender.getName());

		int lvlDiff = attacker.getLevel() - defender.getLevel();
		Maze.log(Log.DEBUG, "lvlDiff="+lvlDiff);

		switch (lvlDiff)
		{
			case -5: result = 1; break;
			case -4: result = 6; break;
			case -3: result = 26; break;
			case -2: result = 38; break;
			case -1: result = 46; break;
			case 0: result = 50; break;
			case 1: result = 54; break;
			case 2: result = 62; break;
			case 3: result = 74; break;
			case 4: result = 94; break;
			case 5: result = 99; break;
			default:
				if (lvlDiff > 0)
				{
					result = 100;
				}
				else
				{
					result = 0;
				}
		}

		// defender and attacker modifiers
		result += calcAttackerToHitModifier(event);
		result -= calcDefenderToHitModifier(event);

		// pin it to range 1%..99%
		result = Math.min(99, result);
		result = Math.max(1, result);

		Maze.log(Log.DEBUG, "result="+result);

		return result;
	}
	
	/*-------------------------------------------------------------------------*/
	private int calcAttackerToHitModifier(
		StrikeEvent event)
	{
		UnifiedActor attacker = event.getAttacker();

		AttackWith attackWith = event.getAttackWith();

		// todo: this is going to be a problem
		Stats.Modifier requiredSkill = event.getAttackType().getAttackModifier();
		int currentWeaponSkill = attacker.getModifier(requiredSkill);

		int result = 0;
		result += attacker.getModifier(Stats.Modifier.SKILL);
		result += currentWeaponSkill;
		result += attacker.getModifier(Stats.Modifier.ATTACK);
		result += event.getAttackType().getModifiers().getModifier(Stats.Modifier.ATTACK);
		result += event.getBodyPart().getModifiers().getModifier(Stats.Modifier.ATTACK);
		result += attackWith.getToHit();

		boolean isRanged = attackWith instanceof Item && ((Item)attackWith).getType() == Type.RANGED_WEAPON ||
			attackWith.isRanged() && attackWith.getMaxRange() == ItemTemplate.WeaponRange.LONG;

		// deadly aim bonus
		if (isRanged && attacker.getModifier(Stats.Modifier.DEADLY_AIM) > 0)
		{
			result += (10 * attacker.getModifier(Stats.Modifier.DEADLY_AIM));
		}

		// master archer bonus
		if (isRanged && attacker.getModifier(Stats.Modifier.MASTER_ARCHER) > 0)
		{
			// master archer bonus with bows
			result += attacker.getLevel();
		}

		// +10% per level of favoured enemy
		result += (10*getFavouredEnemyBonus(attacker, event.getDefender()));

		Maze.log(Log.DEBUG, attacker.getName()+" attacker modifier is "+result);

		// practise modifiers as required
		practice(attacker, requiredSkill, 1);
		if (attackWith instanceof Item)
		{
			Item aw = (Item)attackWith;
			if (Stats.Modifier.MARTIAL_ARTS.equals(aw.getDiscipline()))
			{
				if (attacker.getModifier(Stats.Modifier.MARTIAL_ARTS) > 0)
				{
					practice(attacker, Stats.Modifier.MARTIAL_ARTS, 1);
				}
			}

			if (attacker.getModifier(Stats.Modifier.KENDO) > 1 &&
				Stats.Modifier.KENDO.equals(aw.getDiscipline()))
			{
				practice(attacker, Stats.Modifier.KENDO, 1);
			}
		}

		return result;
	}
	
	/*-------------------------------------------------------------------------*/
	private int calcDefenderToHitModifier(StrikeEvent event)
	{
		UnifiedActor defender = event.getDefender();

		int result =
			defender.getModifier(Stats.Modifier.SKILL)
			+ defender.getModifier(Stats.Modifier.DEFENCE)
			+ event.getBodyPart().getModifiers().getModifier(Stats.Modifier.DEFENCE);

		// immobile actors get their defence zeroed and no skill bonus
		if (!isActorImmobile(defender))
		{
			result += defender.getModifier(Stats.Modifier.SKILL);
		}
		else
		{
			result -= defender.getBaseModifier(Stats.Modifier.DEFENCE);
		}

		Maze.log(Log.DEBUG, defender.getName()+" defender modifier is "+result);

		return result;
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * Calculates an actor's Initiative value
	 */ 
	public int calcInitiative(UnifiedActor actor)
	{
		Maze.log(Log.DEBUG, "calculating initiative for "+actor.getName());
		
		int actorAgility = actor.getModifier(Stats.Modifier.SKILL);
		int actorInitiative = actor.getModifier(Stats.Modifier.INITIATIVE);
		int diceRoll = Dice.d6.roll();

		int result = diceRoll + actorAgility + actorInitiative;
		Maze.log(Log.DEBUG, actor.getName()+" intiative is "+result);
		return result;
	}
	
	/*-------------------------------------------------------------------------*/
	/**
	 * Calculates an attacks damage.  Can return a negative number, which should
	 * be handled by the caller.
	 */ 
	public DamagePacket calcDamage(StrikeEvent event)
	{
		Maze.log(Log.DEBUG, "calculating damage");

		UnifiedActor defender = event.getDefender();
		if (defender.getModifier(Stats.Modifier.IMMUNE_TO_DAMAGE) > 0)
		{
			Maze.log(Log.DEBUG, "defender is immune to damage!");
			return new DamagePacket(0,1);
		}

		UnifiedActor attacker = event.getAttacker();
		AttackWith attackWith = event.getAttackWith();
		Dice diceDamage = attackWith.getDamage();
		Dice ammoDamage = null;
		BodyPart bodyPart = event.getBodyPart();
		Item armour = defender.getArmour(bodyPart);
		int armourSoak = 0;

		Item ammo = isAttackRangedWithAmmo(attacker, attackWith);
		if (ammo != null)
		{
			ammoDamage = ammo.getDamage();
		}

		// damage prevention from armour
		if (armour != null && hitArmour(event, armour.getDamagePreventionChance()))
		{
			armourSoak += armour.getDamagePrevention();
		}

		// damage prevention from body part
		if (hitArmour(event, bodyPart.getDamagePreventionChance()))
		{
			armourSoak += bodyPart.getDamagePrevention();
		}
		
		// damage prevention from a shield
		armourSoak += calcShieldDamagePrevention(event);

		int diceDamageRoll = diceDamage.roll();
		int ammoDamageRoll = (ammoDamage != null) ? ammoDamage.roll() : 0;
		int bpDamageMod = event.getBodyPart().getModifiers().getModifier(Stats.Modifier.DAMAGE);
		int eventDamageMod = attacker.getModifier(Stats.Modifier.DAMAGE);
		int brawn = attacker.getModifier(Stats.Modifier.BRAWN);

		// +2 damage per level of FAVOURED ENEMY
		int favouredEnemy = 2*getFavouredEnemyBonus(attacker, defender);

		Maze.log(Log.DEBUG, "diceDamage = [" + diceDamage + "]");
		Maze.log(Log.DEBUG, "ammoDamage = [" + ammoDamage + "]");
		Maze.log(Log.DEBUG, "diceDamageRoll = [" + diceDamageRoll + "]");
		Maze.log(Log.DEBUG, "ammoDamageRoll = [" + ammoDamageRoll + "]");
		Maze.log(Log.DEBUG, "bpDamageMod = [" + bpDamageMod + "]");
		Maze.log(Log.DEBUG, "eventDamageMod = [" + eventDamageMod + "]");
		Maze.log(Log.DEBUG, "armourSoak = [" + armourSoak + "]");
		Maze.log(Log.DEBUG, "favouredEnemy = [" + favouredEnemy + "]");

		int damage = diceDamageRoll
			+ ammoDamageRoll
			+ brawn/2
			+ bpDamageMod
			+ eventDamageMod
			+ favouredEnemy
			- armourSoak;

		// add up the various "double damage" clauses
		int damageMultiplier = 1;
		damageMultiplier += bodyPart.getModifiers().getModifier(Stats.Modifier.DAMAGE_MULTIPLIER);
		damageMultiplier += attacker.getModifier(Stats.Modifier.DAMAGE_MULTIPLIER);
		damageMultiplier += event.getAttackType().getModifiers().getModifier(Stats.Modifier.DAMAGE_MULTIPLIER);
		if (armour != null)
		{
			damageMultiplier += armour.getModifiers().getModifier(Stats.Modifier.DAMAGE_MULTIPLIER);
		}

		if (defender.getTypes().contains(attackWith.slaysFoeType()))
		{
			// double damage time
			damageMultiplier++;
		}
		
		Maze.log(Log.DEBUG, "damageMultiplier is "+damageMultiplier);
		Maze.log(Log.DEBUG, "damage is "+damage);

		return new DamagePacket(damage, damageMultiplier);
	}

	/*-------------------------------------------------------------------------*/

	/**
	 * @return
	 * 	If the attacker is using a missile weapon with ammunition, this method
	 * 	returns the Item representing that ammunition. Otherwise it returns
	 * 	null.
	 */
	private Item isAttackRangedWithAmmo(
		UnifiedActor attacker,
		AttackWith attackWith)
	{
		if (attackWith instanceof Item &&
			attackWith.isRanged() &&
			attacker instanceof PlayerCharacter)
		{
			Item secondaryWeapon = ((PlayerCharacter)attacker).getSecondaryWeapon();
			if (secondaryWeapon != null &&
				secondaryWeapon instanceof Item &&
				attackWith.getAmmoRequired() != null &&
				attackWith.getAmmoRequired().contains(secondaryWeapon.isAmmoType()))
			{
				return secondaryWeapon;
			}
		}
		return null;
	}

	/*-------------------------------------------------------------------------*/

	/**
	 * @return
	 * 	the resultant spell of any wild magic at play
	 */
	public SpellAction applyWildMagic(
		Combat combat, UnifiedActor source, Spell spell, int castingLevel, SpellTarget target)
	{
		Value v = spell.getWildMagicValue();

		if (v != null)
		{
			//
			// Wild magic results work as follows:
			//  - 10 different Spells in the table
			//  - a calculated wild magic value for the spell that should be a number from 0..9
			//  - there is a 5& chance of giving value +2 and a 5% chance of giving value -2
			//  - there is a 10% chance of giving the value +1, and a 10% value of -1
			//  - a Spell is then looked up and replaces the original spell
			//

			int value = v.compute(source, castingLevel);

			int roll = Dice.d100.roll();
			if (roll <= 5)
			{
				value -= 2;
			}
			else if (roll <= 15)
			{
				value -= 1;
			}
			else if (roll <= 25)
			{
				value += 1;
			}
			else if (roll <= 35)
			{
				value += 2;
			}

			value = Math.min(9, value);
			value = Math.max(0, value);

			String s = spell.getWildMagicTable()[value];

			Spell newSpell = Database.getInstance().getSpell(s);
			ActorGroup party = Maze.getInstance().getParty();

			int targetType = newSpell.getTargetType();

			if (spell.getTargetType() != targetType)
			{
				//
				// reassign the target type appropriately
				//
				switch(targetType)
				{
					case MagicSys.SpellTargetType.ALLY:
						// choose a random ally
						List<UnifiedActor> allies;
						if (combat != null)
						{
							allies = combat.getAllAlliesOf(source);
						}
						else
						{
							allies = party.getActors();
						}
						Dice d = new Dice(1, allies.size(), -1);
						target = allies.get(d.roll());
						break;

					case MagicSys.SpellTargetType.CASTER:
						target = source;
						break;

					case MagicSys.SpellTargetType.ITEM:
					case MagicSys.SpellTargetType.LOCK_OR_TRAP:
					case MagicSys.SpellTargetType.NPC:
					case MagicSys.SpellTargetType.TILE:
						target = null;
						break;

					case MagicSys.SpellTargetType.ALL_FOES:
					case MagicSys.SpellTargetType.CLOUD_ALL_GROUPS:
						target = null;
						break;

					case MagicSys.SpellTargetType.PARTY:
						if (combat != null)
						{
							target = combat.getActorGroup(source);
						}
						else
						{
							target = party;
						}
						break;

					case MagicSys.SpellTargetType.PARTY_BUT_NOT_CASTER:
						target = SpellTargetUtils.getActorGroupWithoutCaster(source);
						break;

					case MagicSys.SpellTargetType.FOE:
						List<UnifiedActor> enemies;
						if (combat != null)
						{
							enemies = combat.getAllFoesOf(source);
						}
						else
						{
							enemies = new ArrayList<UnifiedActor>();
							enemies.add(new NullActor());
						}
						d = new Dice(1, enemies.size(), -1);
						target = enemies.get(d.roll());
						break;

					case MagicSys.SpellTargetType.FOE_GROUP:
					case MagicSys.SpellTargetType.CLOUD_ONE_GROUP:
						List<ActorGroup> groups;
						if (combat != null)
						{
							groups = combat.getFoesOf(source);
						}
						else
						{
							groups = new ArrayList<ActorGroup>();
						}
						d = new Dice(1, groups.size(), -1);
						target = groups.get(d.roll());
						break;

					default: throw new MazeException("Invalid target type: "+
						spell.getTargetType());
				}
			}

			return new SpellAction(target, newSpell, castingLevel, source);
		}
		else
		{
			// no wild magic
			return null;
		}
	}

	/*-------------------------------------------------------------------------*/
	public List<AttackSpellEffects> getAttackSpellEffects(StrikeEvent attackEvent)
	{
		List<AttackSpellEffects> result = new ArrayList<AttackSpellEffects>();
		UnifiedActor attacker = attackEvent.getAttacker();
		AttackWith attackWith = attackEvent.getAttackWith();

		//
		// basic spell effects from the attackWith
		//
		if (attackWith != null)
		{
			GroupOfPossibilities<SpellEffect> gop = attackWith.getSpellEffects();
			if (gop != null)
			{
				result.add(new AttackSpellEffects(
					gop.getRandom(),
					attackWith.getSpellEffectLevel(),
					attackWith.getSpellEffectLevel()));
			}
		}

		//
		// Ammunition adds it's own spell effects, if it is used
		//
		Item ammo = isAttackRangedWithAmmo(
			attacker,
			attackEvent.getAttackWith());

		if (ammo != null)
		{
			GroupOfPossibilities<SpellEffect> gop = ammo.getSpellEffects();
			if (gop != null)
			{
				result.add(
					new AttackSpellEffects(
						gop.getRandom(),
						ammo.getSpellEffectLevel(),
						ammo.getSpellEffectLevel()));
			}
		}

		//
		// add spell effects for the XXX_TOUCH modifiers
		//
		if (!attackWith.isRanged())
		{
			GroupOfPossibilities<SpellEffect> touchEffects = new GroupOfPossibilities<SpellEffect>();
			addTouchEffect(attacker, Stats.Modifier.TOUCH_BLIND, "TOUCH_BLIND", touchEffects);
			addTouchEffect(attacker, Stats.Modifier.TOUCH_DISEASE, "TOUCH_DISEASE", touchEffects);
			addTouchEffect(attacker, Stats.Modifier.TOUCH_FEAR, "TOUCH_FEAR", touchEffects);
			addTouchEffect(attacker, Stats.Modifier.TOUCH_HEX, "TOUCH_HEX", touchEffects);
			addTouchEffect(attacker, Stats.Modifier.TOUCH_INSANE, "TOUCH_INSANE", touchEffects);
			addTouchEffect(attacker, Stats.Modifier.TOUCH_IRRITATE, "TOUCH_IRRITATE", touchEffects);
			addTouchEffect(attacker, Stats.Modifier.TOUCH_NAUSEA, "TOUCH_NAUSEA", touchEffects);
			addTouchEffect(attacker, Stats.Modifier.TOUCH_PARALYSE, "TOUCH_PARALYSE", touchEffects);
			addTouchEffect(attacker, Stats.Modifier.TOUCH_SILENCE, "TOUCH_SILENCE", touchEffects);
			addTouchEffect(attacker, Stats.Modifier.TOUCH_SLEEP, "TOUCH_SLEEP", touchEffects);
			addTouchEffect(attacker, Stats.Modifier.TOUCH_STONE	, "TOUCH_STONE", touchEffects);
			addTouchEffect(attacker, Stats.Modifier.TOUCH_WEB, "TOUCH_WEB", touchEffects);
			addTouchEffect(attacker, Stats.Modifier.TOUCH_POISON, "TOUCH_POISON", touchEffects);

			result.add(
				new AttackSpellEffects(
					touchEffects.getRandom(),
					attacker.getLevel(),
					1));
		}


		return result;
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * Determines whether or not an attack hits the given armour.
	 */ 
	public boolean hitArmour(StrikeEvent event, int basePercent)
	{
		Maze.log(Log.DEBUG, "determining armour impact");
		
		basePercent -= event.getAttackWith().getToPenetrate();
		basePercent -= event.getAttacker().getModifier(Stats.Modifier.BRAWN);
		basePercent -= event.getAttacker().getModifier(Stats.Modifier.TO_PENETRATE);
		basePercent += event.getDefender().getModifier(Stats.Modifier.VS_PENETRATE);
		
		Maze.log(Log.DEBUG, "basePercent is "+basePercent);
		
		return (Dice.d100.roll() <= basePercent);
	}
	
	/*-------------------------------------------------------------------------*/
	/**
	 * @return if the defender is using a shield and the attack hits it: the amount
	 * 	of damage to prevent
	 */
	public int calcShieldDamagePrevention(StrikeEvent event)
	{
		UnifiedActor defender = event.getDefender();

		Maze.log(Log.DEBUG, "determining shield impact for "+defender.getName());
		
		if (defender instanceof PlayerCharacter)
		{
			PlayerCharacter pc = (PlayerCharacter)defender;
			
			if (pc.getSecondaryWeapon() != null && pc.getSecondaryWeapon().isShield())
			{
				int basePercent = pc.getSecondaryWeapon().getDamagePreventionChance();
				// Effect of Chivalry: + to shield defence chance
				basePercent += defender.getModifier(Stats.Modifier.CHIVALRY);
				
				if (hitArmour(event, basePercent))
				{
					int result = pc.getSecondaryWeapon().getDamagePreventionChance();
					practice(pc, Stats.Modifier.CHIVALRY, 1);
					Maze.log(Log.DEBUG, "shield damage prevention is "+result);
					return result;
				}
			}
		}
		
		Maze.log(Log.DEBUG, "no shield damage prevention");
		return 0;		
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * @param defender
	 * 	The actor being target
	 * @param attacker
	 * 	The actor doing the targetting
	 * @param type
	 * 	The type of effect, a constant from {@link MagicSys.SpellEffectType}
	 * @return
	 * 	The modifier to resistance to the given type of effect.
	 */
	public int getResistance(UnifiedActor defender, UnifiedActor attacker, MagicSys.SpellEffectType type)
	{
		Stats.Modifier modifier = type.getResistanceModifier();

		// minus attacker POWER to defender resistance
		int power = attacker.getModifier(Stats.Modifier.POWER);

		// minus 2x attacker POWER CAST
		power += (attacker.getModifier(Stats.Modifier.POWER_CAST)*2);
		if (attacker.getModifier(Stats.Modifier.POWER_CAST) > 0)
		{
			practice(attacker, Stats.Modifier.POWER_CAST, 1);
		}

		// minus 5x attacker FAVOURED ENEMY
		power += (5*getFavouredEnemyBonus(attacker, defender));

		// return defender modifier minus all the stuff, minimum 0
		int defenderModifier = (modifier==null) ? 0 : defender.getModifier(modifier);
		return Math.max(defenderModifier -power, 0);
	}
	
	/*-------------------------------------------------------------------------*/
	/**
	 * Attempt a saving throw.
	 * 
	 * @return
	 * 	True if the actor saves successfully
	 */ 
	public boolean savingThrow(
		UnifiedActor source,
		UnifiedActor target,
		MagicSys.SpellEffectType type,
		MagicSys.SpellEffectSubType subType,
		int spellLevel,
		int castingLevel,
		int saveAdjustment)
	{
		Maze.log(Log.DEBUG, target.getName()+" attempts saving throw for ["+
			type+"/"+subType+"] coming from ["+source.getName()+"]");
		Maze.log(Log.DEBUG, "spellLevel = [" + spellLevel + "]");
		Maze.log(Log.DEBUG, "castingLevel = [" + castingLevel + "]");
		Maze.log(Log.DEBUG, "saveAdjustment = [" + saveAdjustment + "]");
		
		if (isActorImmuneToSpellEffect(target, subType))
		{
			Maze.log(Log.DEBUG, target.getName()+" is immune to this type of attack");
			return true;
		}

		int resistance = getResistance(target, source, type);

		resistance -= saveAdjustment;
		resistance -= castingLevel;

		Maze.log(Log.DEBUG, "resistance = [" + resistance + "]");

		return Dice.d100.roll() <= resistance;
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * @return
	 * 	A % chance of spell failure.
	 */
	public int getSpellFailureChance(UnifiedActor caster, Spell spell, int castingLevel)
	{
		Maze.log(Log.DEBUG, "determining spell failure chance for "+
			caster.getName()+"["+spell.getName()+"] ["+castingLevel+"]");
		
		if (caster instanceof DummyCaster)
		{
			// dummy caster never fails
			return 0;
		}
			
		Stats.Modifier primaryModifier = spell.getPrimaryModifier();

		if (primaryModifier == null)
		{
			// no modifier required, this spell always succeeds
			Maze.log(Log.DEBUG, "no primary modifier required"); 
			return 0;
		}

		int difficulty = (spell.getLevel()*2) + (castingLevel*2);
		Maze.log(Log.DEBUG, "difficulty = [" + difficulty + "]");
		
		int casterTotal = caster.getLevel()/2;
		
		Stats.Modifier secondaryModifier = spell.getSecondaryModifier();
		if (secondaryModifier != null)
		{
			casterTotal += caster.getModifier(primaryModifier)/2;
			casterTotal += caster.getModifier(secondaryModifier)/2;
		}
		else
		{
			casterTotal += caster.getModifier(primaryModifier);
		}
		Maze.log(Log.DEBUG, "casterTotal = [" + casterTotal + "]");

		int result;
		if (difficulty > casterTotal)
		{
			result = 15*(difficulty-casterTotal);
		}
		else
		{
			result = 0;
		}

		int brains = caster.getModifier(Stats.Modifier.BRAINS);
		if (brains >= 0)
		{
			result -= brains;
		}
		else
		{
			result += brains*10;
		}

		Maze.log(Log.DEBUG, "Spell failure chance for ["+caster.getName()+
			"] casting ["+spell.getName()+"] is ["+result+"]");

		return result;
	}

	/*-------------------------------------------------------------------------*/

	/**
	 * @return
	 * 	In the case of a scaling level item invoked spell, calculate the
	 * 	effective casting level based on the users skills
	 */
	public int getItemUseCastingLevel(UnifiedActor user, Item item)
	{
		StatModifier req = item.getUseRequirements();

		Maze.log(Log.DEBUG, "determining item use casting level for "+
			user.getName()+"["+item.getName()+"]");

		int result = 0;

		if (user instanceof DummyCaster)
		{
			Maze.log(Log.DEBUG, "DummyCaster gets a 7");
			result = 7;
		}
		else if (req == null)
		{
			Maze.log(Log.DEBUG, "null req");
			result = 7;
		}
		else if (!user.meetsRequirements(req))
		{
			Maze.log(Log.DEBUG, "user fails to meet requirements");
			result = 1;
		}
		else
		{
			// A user modifier of 5+req maximises the level (to 7)
			// Anything less is added up linearly.
			// Calculate these requirements separately and average them

			int count = 0;
			float sum = 0F;

			for (Stats.Modifier mod : req.getModifiers().keySet())
			{
				int reqMod = req.getModifier(mod);
				int userMod = user.getModifier(mod);

				float max = 5+reqMod;

				if (userMod >= max)
				{
					sum += 1F;
					count++;
				}
				else
				{
					float perc = userMod/max;
					sum += perc;
					count++;
				}
			}

			if (count == 0)
			{
				result = 7;
			}
			else
			{
				float avePerc = sum/count;
				result = Math.round(7*avePerc);
			}
		}

		// random factor
		if (Dice.d100.roll() < 10)
		{
			result += Dice.d5.roll() - 3;
		}
		else
		{
			result += Dice.d3.roll() - 2;
		}

		// at least 1
		result = Math.max(result, 1);
		
		Maze.log(Log.DEBUG, "Item user casting level for ["+user.getName()+
			"] with ["+item.getName()+"] is ["+result+"]");

		return result;

	}

	/*-------------------------------------------------------------------------*/

	/**
	 * @return true if the item invocation can backfire
	 */
	public boolean canBackfire(Item item)
	{
		return item.getCharges() == null ||
			(item.getCharges() != null && item.getCharges().getCurrent() > 0);
	}

	/*-------------------------------------------------------------------------*/

	/**
	 * @return an array of the foe groups that can be attacked by the given actor.
	 * 	Returns an empty list if there are no possible attacks
	 */
	public List<ActorGroup> getAttackableGroups(UnifiedActor attacker, Combat combat)
	{
		List<ActorGroup> result = new ArrayList<ActorGroup>();

		for (ActorGroup ag : combat.getFoesOf(attacker))
		{
			int range = combat.getEngagementRange(
				attacker,
				ag);

			for (AttackWith aw : attacker.getAttackWithOptions())
			{
				int minRange = aw.getMinRange();
				int maxRange = aw.getMaxRange();

				if (range >= minRange && range <= maxRange)
				{
					result.add(ag);
				}
			}
		}

		return result;
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * @return
	 * 	A % chance of item use failure.
	 */
	public int getItemUseFailureChance(
		UnifiedActor user,
		Item item)
	{
		Spell spell = item.getInvokedSpell();
		StatModifier useRequirements = item.getUseRequirements();

		Maze.log(Log.DEBUG, "determining item use failure chance for " +
			user.getName() + "[" + spell.getName() + "] [" +useRequirements+ "]");

		int result = 0;

		if (user instanceof DummyCaster)
		{
			Maze.log(Log.DEBUG, "DummyCaster never fails");
			result = 0;
		}
		else if (useRequirements == null)
		{
			Maze.log(Log.DEBUG, "null useRequirements");
			result = 100;
		}
		else if (!user.meetsRequirements(useRequirements))
		{
			Maze.log(Log.DEBUG, "user fails to meets requirements");
			result = 100;
		}
		else if (item.getChargesType() == ItemTemplate.ChargesType.CHARGES_NON_FATAL &&
			item.getCharges().getCurrent() == 0)
		{
			Maze.log(Log.DEBUG, "item is out of non-fatal charges");
			result = 100;
		}
		else
		{
			// A user modifier of 5+req minimises the chance of failure (to 1%)
			// Anything less is added up linearly.
			// Calculate these requirements separately and average them

			int count=0, sum=0;

			for (Stats.Modifier mod : useRequirements.getModifiers().keySet())
			{
				int reqMod = useRequirements.getModifier(mod);
				int userMod = user.getModifier(mod);

				int max = 5+reqMod;

				if (userMod >= max)
				{
					// no addition to the sum
					count++;
				}
				else
				{
					int failurePerc = 100-userMod*100/max;
					count++;
					sum += failurePerc;
				}
			}

			if (count == 0)
			{
				result = 0;
			}
			else
			{
				result = sum/count;
			}
		}

		result = Math.max(result, 1);

		Maze.log(Log.DEBUG, "Item user failure chance for ["+user.getName()+
			"] with ["+spell.getName()+"] is ["+result+"]");

		return result;
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * @return
	 * 	The Action point cost for the defender to dodge an attack.
	 */
	public int getDodgeCostForAttack(UnifiedActor attacker, UnifiedActor defender)
	{
		Maze.log(Log.DEBUG, "determining dodge cost for attack by "+attacker.getName()+" on "+defender.getName());
		
		int result;

		if (isActorImmobile(defender))
		{
			result = Integer.MAX_VALUE;
		}
		else
		{
			result = stealth2d2.roll();
			result += attacker.getModifier(Stats.Modifier.VS_DODGE);
		}

		Maze.log(Log.DEBUG, defender.getName()+" dodge cost is "+result);

		return result;
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * @return
	 * 	The Action point cost for the defender to dodge an attack.
	 */
	public int getDodgeCostForAmbush(UnifiedActor attacker, UnifiedActor defender)
	{
		Maze.log(Log.DEBUG, "determining dodge cost for ambush by "+attacker.getName()+" on "+defender.getName());
		
		int result;
		if (isActorImmobile(defender))
		{
			result = Integer.MAX_VALUE;
		}
		else
		{
			result = stealth4d2.roll();
			result += attacker.getModifier(Stats.Modifier.VS_DODGE);
		}

		Maze.log(Log.DEBUG, defender.getName()+" dodge cost is "+result);

		return result;
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * @return
	 * 	The Action point cost for the attacker to ambush the defender.
	 */
	public int getBackstabSnipeCost(UnifiedActor attacker, UnifiedActor defender)
	{
		Maze.log(Log.DEBUG, "determining ambush cost for ambush by "+attacker.getName()+
			" on "+defender.getName());
			
		int result = 3;
		result += defender.getModifier(Stats.Modifier.VS_AMBUSH);

		Maze.log(Log.DEBUG, attacker.getName()+" ambush cost is "+result);

		return result;
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * @return
	 * 	The % chance to hide of the given actor
	 */
	public int getHideChance(UnifiedActor actor, List<UnifiedActor> allFoes, List<UnifiedActor> allAllies)
	{
		Maze.log(Log.DEBUG, "determining hide chance for "+actor.getName());
			
		int result = 0;

		// 5% for every potential Action point. // todo: rejig now that these are action points?
		result += actor.getActionPoints().getMaximum() * 5;

		// ...to a max of 80%
		result = Math.min(result, 80);

		// plus the actors sneaking modifier
		result += actor.getModifier(Stats.Modifier.SNEAKING);

		// plus the actors terrain type stealth modifier
		Tile t = Maze.getInstance().getCurrentTile();
		Stats.Modifier stealthModifierRequired = t.getStealthModifierRequired();
		result += actor.getModifier(stealthModifierRequired);

		// plus one for every ally with fewer action points than this one
		// Easier to hide in a crowd ;) todo: rejig now that these are action points?
		for (UnifiedActor ally : allAllies)
		{
			// do not check for awake/conscious.  Presumably foes are keeping their
			// eyes on all allies even if they are non-combatant
			if (ally.getActionPoints().getCurrent() <
				actor.getActionPoints().getCurrent()
				&& ally.getHitPoints().getCurrent() > 0)
			{
				result++;
			}
		}

		// minus one for every foe (more eyes watching you)
		// ... and minus each foes vsHide modifier
		for (UnifiedActor foe : allFoes)
		{
			if (isActorAware(actor))
			{
				// -1 for every foe
				result--;
				result -= foe.getModifier(Stats.Modifier.VS_HIDE);
			}
		}

		Maze.log(Log.DEBUG, actor.getName()+" hide chance is "+result);
		practice(actor, stealthModifierRequired, 1);

		return result;
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * @param primary
	 * 	set to true if attacks must be calculated for the characters primary
	 * 	weapon, false for the secondary weapon.
	 * @return
	 * 	An array describing the melee or ranged attacks of the given character.
	 * 	the length of the array is the number of attacks, and each element is
	 * 	the number of strikes for that attack.
	 */
	public int getNrAttacks(UnifiedActor actor, boolean primary)
	{
		Item weapon;
		if (primary)
		{
			weapon = actor.getPrimaryWeapon();
		}
		else
		{
			weapon = actor.getSecondaryWeapon();
		}
		if (weapon == null)
		{
			weapon = this.getUnarmedWeapon(actor, primary);
		}
		int bonusAttacks = calcBonusAttacks(actor, primary, weapon);

		int result = 0;

		switch (actor.getFocus())
		{
			case COMBAT:
				result = 1 + actor.getLevel()/10;
				break;
			case STEALTH:
				result = 1 + actor.getLevel()/15;
				break;
			case MAGIC:
				result = 1 + actor.getLevel()/20;
				break;
			default:
				throw new MazeException(""+ actor.getCharacterClass().getFocus());
		}

		result += bonusAttacks;

		return result;
	}

	/*-------------------------------------------------------------------------*/
	protected int calcBonusAttacks(UnifiedActor actor, boolean primary, Item weapon)
	{
		int result = actor.getModifier(Stats.Modifier.BONUS_ATTACKS);

		//
		// SKILL +15 grants a bonus attack
		//
		if (actor.getModifier(Stats.Modifier.SKILL) >= 15)
		{
			result++;
		}

		//
		// KENDO +5 grants an additional attack on the primary weapon, if it is
		// a KENDO weapon
		//
		if (actor.getModifier(Stats.Modifier.KENDO) > 5 &&
			Stats.Modifier.KENDO.equals(weapon.getDiscipline()) &&
			primary)
		{
			result++;
		}

		//
		// MELEE MASTER grants an additional attack for each foe group beyond the
		// second on primary weapon, if the character is armed with a melee weapon
		//
		if (Maze.getInstance().getCurrentCombat() != null)
		{
			if (actor.getModifier(Stats.Modifier.MELEE_MASTER) > 0 &&
				isActorArmedWithMeleeWeapon(actor))
			{
				int nrFoeGroups =
					Maze.getInstance().getCurrentCombat().getNrOfEnemyGroups(actor);
				if (nrFoeGroups > 2)
				{
					result += nrFoeGroups-2;
				}
			}
		}

		result += weapon.getBonusAttacks();

		return Math.max(1, result);
	}

	/*-------------------------------------------------------------------------*/
	public int getNrStrikes(
		UnifiedActor attacker,
		UnifiedActor defender,
		AttackType attackType,
		AttackWith attackWith)
	{
		int result = 1 + attacker.getModifier(Stats.Modifier.BONUS_STRIKES);

		//
		// up to a max of 3 strikes
		// todo: more here about bonus strikes and so on
		//

		// todo: broken mapping
		int mod = attacker.getModifier(attackType.getAttackModifier());
		result += mod / 9;

		result = Math.min(result, 3);

		if (attackWith instanceof Item)
		{
			result += ((Item)attackWith).getBonusStrikes();
		}

		return Math.max(1, result);
	}

	/*-------------------------------------------------------------------------*/
	public boolean isLightningStrike(UnifiedActor attacker, AttackWith attackWith)
	{
		Stats.Modifier lightningStrikeModifier;

		if (attackWith instanceof Item)
		{
			switch (((Item)(attackWith)).getSubType())
			{
				case ItemTemplate.WeaponSubType.AXE: lightningStrikeModifier = Stats.Modifier.LIGHTNING_STRIKE_AXE; break;
				case ItemTemplate.WeaponSubType.DAGGER: lightningStrikeModifier = Stats.Modifier.LIGHTNING_STRIKE_DAGGER; break;
				case ItemTemplate.WeaponSubType.MACE: lightningStrikeModifier = Stats.Modifier.LIGHTNING_STRIKE_MACE; break;
				case ItemTemplate.WeaponSubType.MARTIAL_ARTS: lightningStrikeModifier = Stats.Modifier.LIGHTNING_STRIKE_UNARMED; break;
				case ItemTemplate.WeaponSubType.POLEARM: lightningStrikeModifier = Stats.Modifier.LIGHTNING_STRIKE_SPEAR; break;
				case ItemTemplate.WeaponSubType.STAFF: lightningStrikeModifier = Stats.Modifier.LIGHTNING_STRIKE_STAFF; break;
				case ItemTemplate.WeaponSubType.SWORD: lightningStrikeModifier = Stats.Modifier.LIGHTNING_STRIKE_SWORD; break;
				default: lightningStrikeModifier = null;
			}
		}
		else
		{
			// for foe attacks, use the UNARMED modifier.  It's a hack, but
			// an easy way of providing for the occasional lightning strike foe.
			lightningStrikeModifier = Stats.Modifier.TIRELESS_UNARMED;
		}

		if (attacker.getModifier(lightningStrikeModifier) <= 0)
		{
			// early exit;
			return false;
		}

		//
		// build up a chance out of 1000
		//

		// base 2.5%
		int chance = 25;

		// add various modifiers
		chance += attacker.getModifier(Stats.Modifier.SKILL);
		chance += attacker.getModifier(Stats.Modifier.INITIATIVE);

		// subtract 1 for every 10kg encumbrance
		if (attacker instanceof PlayerCharacter)
		{
			int carryingKg = ((PlayerCharacter)attacker).getCarrying() / 1000;
			chance -= (carryingKg / 10);
		}

		// subtract 1 for every 1 kg weapon weight (yes, again)
		if (attackWith instanceof Item)
		{
			int weightKg = ((Item)(attackWith)).getWeight() / 1000;
			chance -= weightKg;
		}

		return Dice.d1000.roll() <= chance;
	}

	/*-------------------------------------------------------------------------*/
	public int getLightningStrikeNrStrikes()
	{
		return Dice.d3.roll() +1;
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * @return
	 * 	The carrying capacity in grams of the given actor.
	 */
	public int getCarryingCapacity(UnifiedActor pc)
	{
		// base 50 kg
		int result = 50000;

		// 1 kg per level
		// 2 kg per brawn
		// 0.05 kg per thieving
		// 0.1 kg per stealth:wilderness
		// 0.1 kg per stealth:wasteland

		result += pc.getLevel() * 1000;
		result += pc.getModifier(Stats.Modifier.BRAWN, false) * 2000;
		result += pc.getModifier(Stats.Modifier.THIEVING, false) * 50;
		result += pc.getModifier(Stats.Modifier.WILDERNESS_LORE, false) * 100;
		result += pc.getModifier(Stats.Modifier.SURVIVAL, false) * 100;
		
		// modify the result by a % specified by the ccPenalty modifier
		double p = pc.getModifier(Stats.Modifier.CC_PENALTY, false)/100.0;
		if (p != 0)
		{
			result += (result*p);
		}

		return result;
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * @return
	 * 	The total number of practice points required for the given PC to
	 * 	increase the given base modifier by 1.
	 */
	public int getPracticePointsRequired(PlayerCharacter pc, Stats.Modifier modifier)
	{
		int baseModifier = pc.getBaseModifier(modifier);

		if (baseModifier < 0)
		{
			// constant for negative modifiers
			return 75;
		}
		else
		{
			// 0..10 is 100 points, and so on
			int result = ((baseModifier / 10) + 1) * 100;

			if (result > Byte.MAX_VALUE)
			{
				// todo: this is a bug, max values should scale up.
				// alas, the V1StatModifier implementation does not allow for this
				result = Byte.MAX_VALUE;
			}

			return result;
		}
	}

	/*-------------------------------------------------------------------------*/
	public void practice(UnifiedActor actor, Stats.Modifier modifier, int amount)
	{
		TurnCache.getInstance().practice(actor, modifier, amount);
	}

	/*-------------------------------------------------------------------------*/
	public void practiseAtEndOfTurn(UnifiedActor actor, Stats.Modifier modifier, int amount)
	{
		if (!(actor instanceof PlayerCharacter))
		{
			// only the PCs need practice
			return;
		}

		PlayerCharacter pc = (PlayerCharacter)actor;

		if (!pc.isActiveModifier(modifier))
		{
			// can't practice an inactive modifier
			return;
		}

		Practice practice = pc.getPractice();

		int required = getPracticePointsRequired(pc, modifier);
		int current = practice.getPracticePoints(modifier);
		int newValue = current+amount;

		if (newValue >= required)
		{
			// inc the modifier
			pc.setModifier(modifier, pc.getModifier(modifier)+1);
			practice.setPracticePoints(modifier, newValue-required);
			Maze.log(Log.DEBUG, pc.getName()+" increases "+modifier+"!");
		}
		else
		{
			// inc the practice
			practice.setPracticePoints(modifier, newValue);
			Maze.log(Log.DEBUG, pc.getName()+" practices "+modifier);
		}
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * @return
	 * 	An array of int constants from {@link Trap.InspectionResult}
	 * 	corresponding to each tool, or <code>null</code> if the trap has been
	 * 	sprung.
	 */
	public int[] inspectTrap(UnifiedActor actor, Trap trap)
	{
		int[] result = new int[8];
		int[] trapDifficulty;
		BitSet trapRequired;
		if (trap == null)
		{
			// untrapped chest
			trapDifficulty = new int[]{2,2,2,2,2,2,2,2};
			trapRequired = new BitSet();
		}
		else
		{
			trapDifficulty = trap.getDifficulty();
			trapRequired = trap.getRequired();
		}

		for (int i = 0; i < result.length; i++)
		{
			int playerScore = actor.getModifier(Stats.Modifier.THIEVING)
				+ actor.getModifier(Stats.Modifier.LOCK_AND_TRAP)
				+ Dice.d20.roll();

			int trapScore = trapDifficulty[i] + Dice.d20.roll();

			practice(actor, Stats.Modifier.LOCK_AND_TRAP, 1);

			if (playerScore >= trapScore)
			{
				result[i] = (trapRequired.get(i)) ?
					Trap.InspectionResult.PRESENT : Trap.InspectionResult.NOT_PRESENT;
			}
			else if (playerScore >= trapScore-10)
			{
				result[i] = Trap.InspectionResult.UNKNOWN;
			}
			else if (playerScore >= trapScore-15)
			{
				// it lies!
				result[i] = (trapRequired.get(i)) ?
					Trap.InspectionResult.NOT_PRESENT : Trap.InspectionResult.PRESENT;
			}
			else
			{
				// trap is sprung
				return null;
			}
		}

		return result;
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * @return
	 * 	An int constant from {@link Trap.DisarmResult} indicating what happens
	 * 	when the given actor tries to disarm the trap using the given tool.
	 */
	public int disarm(UnifiedActor actor, Trap trap, int tool)
	{
		if (trap == null)
		{
			return Trap.DisarmResult.SPRING_TRAP;
		}
		
		if (!trap.getRequired().get(tool))
		{
			// this tool isn't required: the trap is sprung!
			return Trap.DisarmResult.SPRING_TRAP;
		}

		int playerScore = actor.getModifier(Stats.Modifier.THIEVING)
			+ actor.getModifier(Stats.Modifier.LOCK_AND_TRAP)
			+ Dice.d20.roll();

		int trapScore = trap.getDifficulty()[tool] + Dice.d20.roll();

		practice(actor, Stats.Modifier.LOCK_AND_TRAP, 1);

		if (playerScore >= trapScore)
		{
			return Trap.DisarmResult.DISARMED;
		}
		else if (playerScore >= trapScore-10)
		{
			return Trap.DisarmResult.NOTHING;
		}
		else
		{
			return Trap.DisarmResult.SPRING_TRAP;
		}
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * @return
	 * 	An int constant from {@link Trap.DisarmResult} indicating what happens
	 * 	when the given actor tries to pick the lock using the given tool.
	 */
	public int pickLock(UnifiedActor actor, LockOrTrap lockOrTrap, int tool)
	{
		if (!lockOrTrap.getPickLockToolsRequired().get(tool))
		{
			// this tool isn't required: the trap is sprung!
			return Trap.DisarmResult.SPRING_TRAP;
		}

		if (!lockOrTrap.canManualPick())
		{
			// can't pick this one. fake it a bit
			if (Dice.d2.roll() == 1)
			{
				return Trap.DisarmResult.NOTHING;
			}
			else
			{
				return Trap.DisarmResult.SPRING_TRAP;
			}
		}

		int playerScore = actor.getModifier(Stats.Modifier.THIEVING)
			+ actor.getModifier(Stats.Modifier.LOCK_AND_TRAP)
			+ Dice.d20.roll();

		int trapScore = lockOrTrap.getPickLockDifficulty()[tool] + Dice.d20.roll();

		practice(actor, Stats.Modifier.LOCK_AND_TRAP, 1);

		if (playerScore >= trapScore)
		{
			return Trap.DisarmResult.DISARMED;
		}
		else if (playerScore >= trapScore-10)
		{
			return Trap.DisarmResult.NOTHING;
		}
		else
		{
			return Trap.DisarmResult.SPRING_TRAP;
		}
	}

	/*-------------------------------------------------------------------------*/
	public int disarmWithSpell(UnifiedActor caster, int castingLevel, Value spellModifier, Trap trap, int tool)
	{
		if (!trap.getRequired().get(tool))
		{
			// this tool isn't required: the trap is sprung!
			return Trap.DisarmResult.SPRING_TRAP;
		}

		int playerScore = spellModifier.compute(caster, castingLevel) +Dice.d20.roll();
		int trapScore = trap.getDifficulty()[tool] + Dice.d20.roll();

		if (playerScore >= trapScore)
		{
			return Trap.DisarmResult.DISARMED;
		}
		else if (playerScore >= trapScore-10)
		{
			return Trap.DisarmResult.NOTHING;
		}
		else
		{
			return Trap.DisarmResult.SPRING_TRAP;
		}
	}

	/*-------------------------------------------------------------------------*/
	public int pickLockWithSpell(
		UnifiedActor caster,
		int castingLevel,
		Value spellModifier,
		LockOrTrap lockOrTrap,
		int tool)
	{
		if (!lockOrTrap.getPickLockToolsRequired().get(tool))
		{
			// this tool isn't required: the trap is sprung!
			return Trap.DisarmResult.SPRING_TRAP;
		}

		if (!lockOrTrap.canSpellPick())
		{
			// can't spell-pick this one. fake it a bit
			if (Dice.d2.roll() == 1)
			{
				return Trap.DisarmResult.NOTHING;
			}
			else
			{
				return Trap.DisarmResult.SPRING_TRAP;
			}
		}

		int playerScore = spellModifier.compute(caster, castingLevel) +Dice.d20.roll();
		int trapScore = lockOrTrap.getPickLockDifficulty()[tool] + Dice.d20.roll();

		if (playerScore >= trapScore)
		{
			return Trap.DisarmResult.DISARMED;
		}
		else if (playerScore >= trapScore-10)
		{
			return Trap.DisarmResult.NOTHING;
		}
		else
		{
			return Trap.DisarmResult.SPRING_TRAP;
		}
	}

	/*-------------------------------------------------------------------------*/
	public void castSpellOnPartyOutsideCombat(Spell spell, int casterLevel, int castingLevel)
	{
		Maze maze = Maze.getInstance();
		DummyCaster dummyCaster = new DummyCaster(spell, casterLevel, castingLevel);

		SpellTarget target = null;
		switch (spell.getTargetType())
		{
			case MagicSys.SpellTargetType.PARTY:
			case MagicSys.SpellTargetType.FOE_GROUP:
			case MagicSys.SpellTargetType.ALL_FOES:
			case MagicSys.SpellTargetType.CLOUD_ONE_GROUP:
			case MagicSys.SpellTargetType.CLOUD_ALL_GROUPS:
			case MagicSys.SpellTargetType.PARTY_BUT_NOT_CASTER:
				target = maze.getParty();
				break;
			case MagicSys.SpellTargetType.FOE:
			case MagicSys.SpellTargetType.CASTER:
			case MagicSys.SpellTargetType.ALLY:
				target = maze.getParty().getRandomPlayerCharacter();
				break;
			case MagicSys.SpellTargetType.TILE:
				target = dummyCaster;
				break;
			default:
				target = maze.getParty(); // what the hell, let's try it anyway
		}

		SpellIntention intention = new SpellIntention(target, spell, castingLevel);

		resolveActorActionIntention(maze, dummyCaster, intention);
	}

	/*-------------------------------------------------------------------------*/
	public void resolveActorActionIntention(Maze maze, UnifiedActor caster,
		ActorActionIntention intention)
	{
		List<CombatAction> combatActions = caster.getCombatActions(intention);

		for (CombatAction action : combatActions)
		{
			maze.appendEvents(ActorActionResolver.resolveAction(action, null));
		}
	}

	/*-------------------------------------------------------------------------*/
	public void castPartySpellOutsideCombat(
		Spell spell,
		PlayerCharacter caster,
		int castingLevel,
		SpellTarget target)
	{
		processPlayerCharacterIntentionOutsideCombat(
			new SpellIntention(target, spell, castingLevel), caster);
	}

	/*-------------------------------------------------------------------------*/
	public void useItemOutsideCombat(
		Item item,
		PlayerCharacter user,
		SpellTarget target)
	{
		processPlayerCharacterIntentionOutsideCombat(
			new UseItemIntention(item, target), user);
	}

	/*-------------------------------------------------------------------------*/
	public void processPlayerCharacterIntentionOutsideCombat(
		ActorActionIntention intention,
		PlayerCharacter pc)
	{
		Maze maze = Maze.getInstance();
		List<CombatAction> combatActions = pc.getCombatActions(intention);

		for (CombatAction action : combatActions)
		{
			maze.appendEvents(ActorActionResolver.resolveAction(action, null));
		}
	}

	/*-------------------------------------------------------------------------*/
	public void castSpellOnNpc(
		final Spell spell,
		final PlayerCharacter caster,
		final int castingLevel,
		final Npc npc)
	{
		Maze.log(Log.DEBUG, "PC ["+caster.getName()+
			"] casting ["+spell.getName()+" ("+castingLevel+
			")] on NPC ["+npc.getName()+"]");

		processPlayerCharacterIntentionOutsideCombat(
			new SpellIntention(npc, spell, castingLevel), caster);
	}

	/*-------------------------------------------------------------------------*/
	public void useItemOnNpc(
		Item item,
		PlayerCharacter caster,
		Npc npc)
	{
		processPlayerCharacterIntentionOutsideCombat(
			new UseItemIntention(item, npc), caster);
	}

	/*-------------------------------------------------------------------------*/
	public int getItemCost(Item item, int costMultiplier)
	{
		int result = item.getBaseCost();

		// for enchanted items, add the enchantment cost modifier
		if (item.getEnchantment() != null)
		{
			result += item.getEnchantment().getCostModifier();
		}

		// for charged items, add 10% per charge above 1, or halve if empty.
		if (item.getInvokedSpell() != null)
		{
			CurMax charges = item.getCharges();
			if (charges == null || charges.getCurrent() == 0)
			{
				result /= 2;
			}
			else
			{
				int inc = item.getBaseCost()/10;
				result += (charges.getCurrent()-1)*inc;
			}
		}

		// apply the multiplier
		result += (result*costMultiplier/100);

		// stacked?
		if (item.isStackable())
		{
			result *= item.getStack().getCurrent();
		}

		return result;
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * @param actor
	 * 	The actor doing the threatening
	 * @param target
	 * 	The actor being threatened
	 * @return
	 * 	the difference in totals
	 */
	public int threatenNpc(UnifiedActor actor, UnifiedActor target)
	{
		int targetTotal =
			target.getLevel()*10
//			+ target.getResistThreats() todo: make a modifier
			+ Dice.d10.roll();

		int actorTotal = 0;
		actorTotal += actor.getLevel();
		actorTotal += actor.getModifier(Stats.Modifier.BRAWN);
		actorTotal += actor.getModifier(Stats.Modifier.SKILL);
		actorTotal += actor.getModifier(Stats.Modifier.THREATEN);
		actorTotal += Dice.d10.roll();

		return actorTotal-targetTotal;
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * @param npc
	 * 	The npc being bribed
	 * @param pc
	 * 	The PC doing the bribing
	 * @param amount
	 * 	The amount of gold pieces changing hands
	 * @return
	 * 	the difference in totals
	 */
	public int bribeNpc(Foe npc, PlayerCharacter pc, int amount)
	{
		int npcTotal =
			npc.getLevel()*10
			+ npc.getResistBribes()
			+ Dice.d10.roll();

		int partyTotal = 0;
		partyTotal += amount/10;
		partyTotal += pc.getModifier(Stats.Modifier.TO_BRIBE);
		partyTotal += Dice.d10.roll();

		return partyTotal-npcTotal;
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * @param npc
	 * 	The NPC being ripped off
	 * @param pc
	 * 	The pc doing the stealing
	 * @param item
	 * 	The item in question (may be GoldPieces)
	 * @return
	 * 	a constant from {@link Npc.TheftResult}
	 */
	public int stealItem(Foe npc, PlayerCharacter pc, Item item)
	{
		Maze.log(Log.DEBUG, "theft attempt of ["+item.getName()+"] from " +
			"["+npc.getName()+"] by ["+pc.getName()+"]");

		boolean npcIsNeutral = npc.getAttitude().equals(NpcFaction.Attitude.NEUTRAL);

		int npcTotal =
			npc.getLevel() +
			npc.getResistSteal() +
			npc.getTheftCounter() +
			Dice.d10.roll();

		if (npc.getTheftCounter() > 10)
		{
			npcTotal += 99;
		}
		Maze.log(Log.DEBUG, "theft counter = [" + npc.getTheftCounter() + "]");
		Maze.log(Log.DEBUG, "npcTotal = [" + npcTotal + "]");

		int pcTotal =
			pc.getLevel() +
			pc.getModifier(Stats.Modifier.THIEVING) +
			pc.getModifier(Stats.Modifier.STEAL) +
			Dice.d10.roll();
		Maze.log(Log.DEBUG, "pcTotal = [" + pcTotal + "]");

		int requiredDifference = npcIsNeutral ? 10 : 5;
		Maze.log(Log.DEBUG, "requiredDiff = [" + requiredDifference + "]");

		// practise STEAL
		practice(pc, Stats.Modifier.STEAL, 1);

		if (pcTotal >= npcTotal+requiredDifference)
		{
			Maze.log(Log.DEBUG, "result = [SUCCESS]");
			return Npc.TheftResult.SUCCESS;
		}
		else if (pcTotal >= npcTotal)
		{
			Maze.log(Log.DEBUG, "result = [FAILED_UNDETECTED]");
			return Npc.TheftResult.FAILED_UNDETECTED;
		}
		else
		{
			if (pc.getModifier(Stats.Modifier.MASTER_THIEF) > 0)
			{
				// master thief is never detected
				Maze.log(Log.DEBUG, "result = [FAILED_UNDETECTED - Master Thief]");
				return Npc.TheftResult.FAILED_UNDETECTED;
			}
			else
			{
				Maze.log(Log.DEBUG, "result = [FAILED_DETECTED]");
				return Npc.TheftResult.FAILED_DETECTED;
			}
		}
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * Call when a dishonourable action occurs.
	 *
	 * @return
	 * 	List of events to process
	 */
	public List<MazeEvent> processDishonourableAction(PlayerParty party)
	{
		List<MazeEvent> result = new ArrayList<MazeEvent>();

		for (PlayerCharacter pc : party.getPlayerCharacters())
		{
			if (pc.getModifier(Stats.Modifier.CODE_OF_HONOUR) > 0)
			{
				int amount = 50*pc.getLevel();
				result.add(new LoseExperienceEvent(amount, pc));
			}
		}

		return result;
	}

	/*-------------------------------------------------------------------------*/

	/**
	 * Call when a honourable action occurs.
	 *
	 * @return
	 * 	List of events to process
	 */
	public List<MazeEvent> processHonourableAction(PlayerParty party)
	{
		List<MazeEvent> result = new ArrayList<MazeEvent>();

		for (PlayerCharacter pc : party.getPlayerCharacters())
		{
			if (pc.getModifier(Stats.Modifier.CODE_OF_DISHONOUR) > 0)
			{
				int amount = 50*pc.getLevel();
				result.add(new LoseExperienceEvent(amount, pc));
			}
		}

		return result;
	}

	/*-------------------------------------------------------------------------*/
	public int getAmountOfGoldStolen(Foe npc, PlayerCharacter pc)
	{
		Maze.log(Log.DEBUG, "calculating amount of gold stolen from " +
			"["+npc.getName()+"] by ["+pc.getName()+"]");

		int percent = Dice.d10.roll();
		Maze.log(Log.DEBUG, "percent = [" + percent + "]");

		int amount = npc.getMaxPurchasePrice() *percent /100
			+ pc.getModifier(Stats.Modifier.THIEVING)
			+ pc.getModifier(Stats.Modifier.STEAL)
			+ Dice.d10.roll();

		Maze.log(Log.DEBUG, "amount = [" + amount + "]");

		if (pc.getModifier(Stats.Modifier.EXTRA_GOLD) > 0)
		{
			amount += (amount*pc.getModifier(Stats.Modifier.EXTRA_GOLD)/100);
		}

		amount = Math.min(amount, pc.getLevel()*250);

		Maze.log(Log.DEBUG, "final amount = [" + amount + "]");

		return amount;
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * @return
	 * 	null if there is nothing to steal
	 */
	public Item getRandomItemToSteal(Foe npc)
	{
		Maze.log(Log.DEBUG, "determining random item to steal from ["+npc.getName()+"]");

		boolean canStealGold = npc.getMaxPurchasePrice() > 0;
		Maze.log(Log.DEBUG, "canStealGold = [" + canStealGold + "]");

		List<Item> stealableItems = npc.getStealableItems();

		if (canStealGold && (Dice.d100.roll() <= 30 ||
			stealableItems == null || stealableItems.size() == 0))
		{
			// 30% chance of gold
			Maze.log(Log.DEBUG, "gold will be stolen");
			return new GoldPieces(npc.getMaxPurchasePrice());
		}
		else
		{
			if (stealableItems != null && stealableItems.size() > 0)
			{
				int max = stealableItems.size();
				Dice d = new Dice(0, max-1, 0);
				Item item = stealableItems.get(d.roll());
				Maze.log(Log.DEBUG, "item = [" + item.getName() + "]");
				return item;
			}
			else
			{
				return null;
			}
		}
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * @return
	 * 	a constant from {@link Portal.ForceResult}
	 */
	public int forcePortal(PlayerCharacter pc, LockOrTrap lockOrTrap)
	{
		if (pc.getHitPoints().getCurrent() <= lockOrTrap.getHitPointCostToForceLock())
		{
			return Portal.ForceResult.FAILED_NO_DAMAGE;
		}
		else if (!lockOrTrap.canForceOpen())
		{
			return Portal.ForceResult.FAILED_DAMAGE;
		}

		int forceChance = 50
			+ pc.getModifier(Stats.Modifier.BRAWN)
			- lockOrTrap.getResistForceOpen();

		forceChance = Math.min(99, forceChance);

		if (Dice.d100.roll() <= forceChance)
		{
			return Portal.ForceResult.SUCCESS;
		}
		else
		{
			return Portal.ForceResult.FAILED_DAMAGE;
		}
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * The given party attempts to manually identify the given item.  This will
	 * generally be attempted by the highest level PC, although the ARTIFACTS
	 * modifier plays a role too.
	 */
	public void attemptManualIdentify(Item item, ActorGroup party)
	{
		if (party == null || item.getIdentificationState() == Item.IdentificationState.IDENTIFIED)
		{
			return;
		}

		int partyTotal = Integer.MIN_VALUE;
		UnifiedActor assayer = null;

		for (UnifiedActor pc : party.getActors())
		{
			int playerTotal =
				pc.getLevel() +
				pc.getModifier(Stats.Modifier.ARTIFACTS);

			if (playerTotal > partyTotal)
			{
				partyTotal = playerTotal;
				assayer = pc;
			}
			else if (playerTotal == partyTotal &&
				assayer != null &&
				!assayer.isActiveModifier(Stats.Modifier.ARTIFACTS) &&
				pc.isActiveModifier(Stats.Modifier.ARTIFACTS))
			{
				// give preference to the first character able to practice the skill
				assayer = pc;
			}
		}

		if (partyTotal >= item.getIdentificationDifficulty())
		{
			// only practice on a successful identification, to prevent easy
			// power training.
			practice(assayer, Stats.Modifier.ARTIFACTS, 1);
			item.setIdentificationState(Item.IdentificationState.IDENTIFIED);
		}
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * @return
	 * 	The stealth value for the group on the given tile.  This is based on
	 * 	the lowest stealth modifier in the party for the given terrain.
	 */
	public int getStealthValue(Tile currentTile, ActorGroup group)
	{
		Stats.Modifier mod = currentTile.getStealthModifierRequired();

		// is there a more efficient way to do this?

		int lowest = Integer.MAX_VALUE;
		UnifiedActor lowestActor = null;

		// find the lowest stealth modifier for this terrain
		for (UnifiedActor a : group.getActors())
		{
			int temp = a.getModifier(mod);
			if (temp < lowest)
			{
				lowest = temp;
				lowestActor = a;
			}
		}

		// adjust for the rest of the party
		int groupTotal = lowest;
		for (UnifiedActor pc : group.getActors())
		{
			if (pc == lowestActor)
			{
				continue;
			}

			// +1 for each character with a higher modifier
			// -1 for each character with an equal modifier
			int temp = pc.getModifier(mod);
			if (temp > lowest)
			{
				groupTotal++;
			}
			else
			{
				groupTotal--;
			}
		}

		return groupTotal;
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * @return
	 * 	a stealth value for the given grouping of actors.
	 */
	public int getStealthValue(Tile currentTile, List<FoeGroup> groups)
	{
		int lowest = Integer.MAX_VALUE;

		for (ActorGroup ag : groups)
		{
			int stealthValue = getStealthValue(currentTile, ag);
			if (stealthValue < lowest)
			{
				lowest = stealthValue;
			}
		}

		return lowest;
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * @return
	 * 	A leader for the given bunch of foes
	 */
	public Foe getLeader(List<FoeGroup> groups)
	{
		Foe bestSoFar = groups.get(0).getFoes().get(0);

		for (FoeGroup ag : groups)
		{
			for (Foe actor : ag.getFoes())
			{
				// give preference to NPCs, then legendary foes, then the highest
				// level regular actor

//				if (actor instanceof Npc) todo
//				{
//					bestSoFar = actor;
//				}
				/*else */if (actor.getTypes().contains(Foe.Type.LEGENDARY) &&
					!bestSoFar.getTypes().contains(Foe.Type.LEGENDARY))
				{
					bestSoFar = actor;
				}
				else if (actor.getLevel() > bestSoFar.getLevel())
				{
					bestSoFar = actor;
				}
			}
		}

		return bestSoFar;
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * The party attempts to manually identify the foes.
	 */
	public void attemptManualIdentification(List<FoeGroup> foes, PlayerParty party, int combatRound)
	{
		if (party == null)
		{
			return;
		}
		// go try all the foes
		for (FoeGroup fg : foes)
		{
			if (!fg.getFoes().isEmpty())
			{
				Foe representativeFoe = fg.getFoes().get(0);
				PlayerCharacter bestAtMythology = getMythologist(party, representativeFoe);

				// it gets easier each combat round
				int partyTotal = getMythologyToIdentify(bestAtMythology, representativeFoe)
					+ combatRound;

				// assume all foes in a group are the same, only try the first one
				if (representativeFoe.getIdentificationState() == Item.IdentificationState.UNIDENTIFIED)
				{
					if (representativeFoe.getIdentificationDifficulty() <= partyTotal)
					{
						for (Foe ff : fg.getFoes())
						{
							ff.setIdentificationState(Item.IdentificationState.IDENTIFIED);
						}
						practice(bestAtMythology, Stats.Modifier.MYTHOLOGY, 1);
					}
				}
			}
		}
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * @return
	 * 	Return the player character responsible for identifying foes.
	 */
	public PlayerCharacter getMythologist(PlayerParty party, Foe foe)
	{
		// determine the party total
		int temp = Integer.MIN_VALUE;
		PlayerCharacter bestAtMythology = null;

		for (PlayerCharacter pc : party.getPlayerCharacters())
		{
			int pcTotal = getMythologyToIdentify(pc, foe);

			if (pcTotal > temp)
			{
				temp = pcTotal;
				bestAtMythology = pc;
			}
			else if (pcTotal == temp &&
				bestAtMythology != null &&
				!bestAtMythology.isActiveModifier(Stats.Modifier.MYTHOLOGY) &&
				pc.isActiveModifier(Stats.Modifier.MYTHOLOGY))
			{
				// give preference to the character who can practice the skill
				bestAtMythology = pc;
			}
		}
		return bestAtMythology;
	}

	/*-------------------------------------------------------------------------*/
	public int getMythologyToIdentify(PlayerCharacter pc, Foe foe)
	{
		int pcTotal = pc.getLevel() + pc.getModifier(Stats.Modifier.MYTHOLOGY);

		// +2 per level of FAVOURED ENEMY
		int favouredEnemy = getFavouredEnemyBonus(pc, foe);
		if (favouredEnemy > 0)
		{
			pcTotal += (favouredEnemy * 2);
		}
		return pcTotal;
	}

	/*-------------------------------------------------------------------------*/

	/**
	 * @return
	 * 	the value of the applicable FAVOURED ENEMY modifier if the defender
	 * 	is a favoured enemy of the attacker, otherwise 0
	 */
	private int getFavouredEnemyBonus(UnifiedActor attacker,
		UnifiedActor defender)
	{
		if (defender.getTypes() != null)
		{
			for (TypeDescriptor td : defender.getTypes())
			{
				if (td.getFavouredEnemyModifier() != null)
				{
					int modifier = attacker.getModifier(td.getFavouredEnemyModifier());
					if (modifier > 0)
					{
						return modifier;
					}
				}
			}
		}

		return 0;
	}

	/*-------------------------------------------------------------------------*/
	public int getHitPointsToRegeneratePerTurn(
		UnifiedActor actor,
		long turnNr,
		boolean resting,
		ActorGroup group)
	{
		// find the best entertainer
		int entertainer=0;
		if (resting && group != null)
		{
			for (UnifiedActor a : group.getActors())
			{
				if (a.getModifier(Stats.Modifier.ENTERTAINER) > entertainer)
				{
					entertainer = a.getModifier(Stats.Modifier.ENTERTAINER);
				}
			}
		}

		int hpRegenModifier = actor.getModifier(Stats.Modifier.HIT_POINT_REGEN);

		// at this point, the gamesys just gives a +1 for an entertainer present
		if (entertainer > 0)
		{
			entertainer = 1;
		}

		int hpRegenRate = 1 + hpRegenModifier + entertainer;
		int hpRegenTurns = (resting&&hpRegenRate>0) ? 10 : 20;

		return getRegenResult(hpRegenRate, hpRegenTurns, turnNr);
	}

	/*-------------------------------------------------------------------------*/

	/**
	 * @return
	 * 	The total resources to regen over an entire period of rest
	 */
	public int getResourcesToRegenerateWhileResting(
		UnifiedActor actor,
		CurMax resource,
		ActorGroup group,
		Tile tile)
	{
		// find the best entertainer
		int entertainer=0;
		if (group != null)
		{
			for (UnifiedActor a : group.getActors())
			{
				if (a.getModifier(Stats.Modifier.ENTERTAINER) > entertainer)
				{
					entertainer = a.getModifier(Stats.Modifier.ENTERTAINER);
				}
			}
		}

		// base resting percentage
		double regenPerc = getRestingRegenPercentage(tile);

		// add the Entertainer modifier to the regen %
		if (entertainer > 0)
		{
			regenPerc = regenPerc + (entertainer/100D);
		}

		// reduce if there is a food shortage
		int neededToRest = getSuppliesNeededToRest(actor);
		int consumedWhileResting = getSuppliesConsumedWhileResting(actor, (PlayerParty)group);

		if (neededToRest > consumedWhileResting)
		{
			regenPerc = regenPerc *consumedWhileResting /neededToRest;
		}

		// regen at least 1 hp
		int max = resource.getMaximum();
		return Math.max(1, (int)(max * regenPerc));
	}

	/*-------------------------------------------------------------------------*/
	protected double getRestingRegenPercentage(Tile tile)
	{
		double regenPerc;

		switch (tile.getRestingEfficiency())
		{
			case POOR:
				regenPerc = .2D;
				break;
			case AVERAGE:
				regenPerc = .4D;
				break;
			case GOOD:
				regenPerc = .6D;
				break;
			case EXCELLENT:
				regenPerc = 1D;
				break;
			default: throw new MazeException(tile.getRestingEfficiency().toString());
		}
		return regenPerc;
	}

	/*-------------------------------------------------------------------------*/
	public int getRestingDangerPercentage(Tile tile)
	{
		switch (tile.getRestingDanger())
		{
			case NONE: return 0;
			case LOW: return 10;
			case MEDIUM: return 25;
			case HIGH: return 50;
			case EXTREME: return 100;
			default: throw new MazeException(tile.getRestingDanger().toString());
		}
	}

	/*-------------------------------------------------------------------------*/
	public int getMagicPointsToRegeneratePerTurn(
		UnifiedActor actor,
		long turnNr,
		boolean resting,
		ActorGroup group)
	{
		// find the best entertainer
		int entertainer=0;
		if (resting && group != null)
		{
			for (UnifiedActor a : group.getActors())
			{
				if (a.getModifier(Stats.Modifier.ENTERTAINER) > entertainer)
				{
					entertainer = a.getModifier(Stats.Modifier.ENTERTAINER);
				}
			}
		}

		int magicRegenModifier = actor.getModifier(Stats.Modifier.MAGIC_POINT_REGEN);

		// at this point, the gamesys just gives a +1 for an entertainer present
		if (entertainer > 0)
		{
			entertainer = 1;
		}

		int magicRegenRate = 1 + magicRegenModifier + entertainer;
		

		int magicRegenTurns = (resting&&magicRegenRate>0) ? 10 : 20;

		return getRegenResult(magicRegenRate, magicRegenTurns, turnNr);
	}

	/*-------------------------------------------------------------------------*/
	public int getActionPointsToRegenerateInCombat(UnifiedActor actor,
		long turnNr)
	{
		int stealthRegenRate = actor.getModifier(Stats.Modifier.ACTION_POINT_REGEN);
		int stealthRegenTurns = 10;

		return getRegenResult(stealthRegenRate, stealthRegenTurns, turnNr);
	}

	/*-------------------------------------------------------------------------*/
	public int getActionPointsToRegenerateWhileMoving(UnifiedActor actor,
		Tile tile)
	{
		if (isActorImmobile(actor))
		{
			return 0;
		}
		
		Stats.Modifier mod = tile.getStealthModifierRequired();
		int deficit = actor.getActionPoints().getMaximum() - actor.getActionPoints().getCurrent();

		int modifier = actor.getModifier(mod);
		return Math.max(deficit / 2 + modifier, modifier);
	}

	/*-------------------------------------------------------------------------*/
	protected int getRegenResult(int regenRate, int regenTurns, long turnNr)
	{
		if (regenRate == 0)
		{
			return 0;
		}

		int result = 0;

		// work in the positive and convert back later
		int sign = (regenRate<0) ? -1 : 1;
		regenRate = Math.abs(regenRate);

		// collect the minimum each turn
		result += regenRate/regenTurns;

		// evenly distribute the remainder
		int leftOver = regenRate%regenTurns;
		int offset=0;

		while (leftOver > 0)
		{
			int toAssign = leftOver;
			if (leftOver > regenTurns/2)
			{
				toAssign = regenTurns/2;
			}

			int interval = regenTurns/toAssign;
			int turnInSequence = (int)(turnNr%regenTurns - offset);

			if (turnInSequence%interval == 0)
			{
				result++;
			}

			leftOver -= toAssign;
			offset++;
		}

		result *= sign;

		return result;
	}

	/*-------------------------------------------------------------------------*/
	public int getFatigueToRegenInCombat(UnifiedActor actor)
	{
		int hpRegen = actor.getModifier(Stats.Modifier.HIT_POINT_REGEN);
		int stamRegen = actor.getModifier(Stats.Modifier.STAMINA_REGEN);
		return Math.max(4, hpRegen) + stamRegen;
	}

	/*-------------------------------------------------------------------------*/
	public int getFatigueToRegenWhileMoving(UnifiedActor actor, boolean resting)
	{
		int hpRegen = actor.getModifier(Stats.Modifier.HIT_POINT_REGEN);
		int stamRegen = actor.getModifier(Stats.Modifier.STAMINA_REGEN);

		int result = Math.max(1, hpRegen) + stamRegen;

		if (resting)
		{
			result *= 2;
		}

		return result;
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * @param coward
	 * 	The actor trying to run away
	 * @param nrFoes
	 * 	The number of foes faced
	 * @return
	 * 	tru if the actor runs away successfully
	 */
	public boolean attemptToRunAway(UnifiedActor coward, int nrFoes)
	{
		//
		// The probabilities are set differently depending on whether a PC or a foe
		// is trying to run away.  A single PC successfully running away gets the
		// whole group away, while a single foe can desert and leave the rest
		// there.
		//

		Maze.log(Log.DEBUG, coward.getName()+" tries to run away");

		int base = (coward instanceof PlayerCharacter) ? 50 : 80;
		int mod = coward.getModifier(Stats.Modifier.TO_RUN_AWAY);

		Maze.log(Log.DEBUG, "success chance is "+base+" + "+mod+" - "+nrFoes);

		base = base + mod - nrFoes;

		return base >= Dice.d100.roll();
	}

	/*-------------------------------------------------------------------------*/

	/**
	 * Party attempts to flee
	 *
	 * @return success or failure
	 */
	public boolean attemptToRunAway(PlayerParty party, List<ActorGroup> actors)
	{
		Maze.log(Log.DEBUG, "Party tries to run away");

		int base = 80;
		int mod = party.getTotalModifier(Stats.Modifier.TO_RUN_AWAY);
		int nrFoes = 0;
		for (ActorGroup ag : actors)
		{
			nrFoes += ag.numActive();
		}

		return attemptToRunAway(party, nrFoes);
	}

	/*-------------------------------------------------------------------------*/

	/**
	 * Party attempts to flee
	 *
	 * @return success or failure
	 */
	public boolean attemptToRunAway(PlayerParty party, int nrFoes)
	{
		Maze.log(Log.DEBUG, "Party tries to run away");

		int base = 80;
		int mod = party.getTotalModifier(Stats.Modifier.TO_RUN_AWAY);

		Maze.log(Log.DEBUG, "success chance is "+base+" + "+mod+" - "+nrFoes);

		base = base + mod - nrFoes;

		return Dice.d100.roll() <= base;
	}

	/*-------------------------------------------------------------------------*/
	public void useItemCharge(Item item, UnifiedActor actor)
	{
		switch (item.getChargesType())
		{
			case CHARGES_INFINITE:
				break;
			case CHARGES_FATAL:
				CurMax charges = item.getCharges();
				if (charges != null)
				{
					charges.decCurrent(1);
				}

				if (charges == null || item.getCharges().getCurrent() == 0)
				{
					actor.removeItem(item, false);
				}
				break;
			case CHARGES_NON_FATAL:
				if (item.getCharges().getCurrent() > 0)
				{
					item.getCharges().decCurrent(1);
				}
				break;
			default:
				throw new MazeException("Invalid charges type: "+item.getChargesType());
		}
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * The given party attempts to hide outside of combat.
	 */
	public void partyHidesOutOfCombat(PlayerParty party, Tile tile)
	{
		for (UnifiedActor pc : party.getActors())
		{
			int amount = this.getActionPointsToRegenerateWhileMoving((PlayerCharacter)pc, tile);
			pc.getActionPoints().incCurrent(amount);
		}
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * Generates the unarmed weapon for this character
	 */
	public Item getUnarmedWeapon(UnifiedActor actor, boolean isPrimary)
	{
		Dice damage = null;
		int toHit = 0;
		int toPenetrate = 0;
		int toCritical = 0;
		int toInitiative = 0;
		String[] attackTypes = null;
		GroupOfPossibilities<SpellEffect> spellEffects = null;
		int bonusAttacks = 0;
		int bonusStrikes = 0;
		
		// debatable if it should be character level or class level.  Leaving it
		// at class level provides some protection against Ninjas turned Cultists
		// being too powerful
		int spellEffectLevel = actor.getCurrentClassLevel();

		int martialArts = actor.getModifier(Stats.Modifier.MARTIAL_ARTS);
		int brawn = actor.getModifier(Stats.Modifier.BRAWN);
		if (martialArts <= 0)
		{
			damage = new Dice(1, Math.max(brawn, 1), 0);
			toPenetrate = -2;
			attackTypes = new String[]{"punch"};
			bonusAttacks = 0;
			bonusStrikes = -3;
			spellEffects = new GroupOfPossibilities<SpellEffect>();
			SpellEffect ko = Database.getInstance().getSpellEffect("MARTIAL_ARTS_KO");
			spellEffects.add(ko, brawn/2);
		}
		else
		{
			damage = new Dice(1, Math.max(brawn, 1), actor.getLevel()/3);
			toPenetrate = martialArts;
			toInitiative = martialArts/3;
			attackTypes = new String[]{"punch", "kick"};
			toCritical = actor.getLevel();

			// todo: PARALYSE?
			spellEffects = new GroupOfPossibilities<SpellEffect>();
			SpellEffect ko = Database.getInstance().getSpellEffect("MARTIAL_ARTS_KO");
			spellEffects.add(ko, Math.max(martialArts*2, brawn/2));
		}
		
		ItemTemplate result = new ItemTemplate(
			"unarmed",
			"unarmed",
			"unarmed",
			ItemTemplate.Type.SHORT_WEAPON,
			ItemTemplate.WeaponSubType.MARTIAL_ARTS,
			"unarmed weapon",
			StatModifier.NULL_STAT_MODIFIER,
			"item/defaultitem",
			new BitSet(),
			0,
			1,
			0,
			null,
			spellEffectLevel, 
			Dice.d1,
			ItemTemplate.ChargesType.CHARGES_INFINITE,
			null,
			null,
			null,
			false,
			0,
			0,
			0,
			StatModifier.NULL_STAT_MODIFIER,
			StatModifier.NULL_STAT_MODIFIER,
			Database.getInstance().getScript("generic weapon swish"),
			damage,
			MagicSys.SpellEffectType.BLUDGEONING,
			attackTypes,
			false,
			false,
			false,
			false,
			false,
			toHit,
			toPenetrate,
			toCritical,
			toInitiative,
			ItemTemplate.WeaponRange.MELEE,
			ItemTemplate.WeaponRange.MELEE,
			null,
			spellEffects,
			bonusAttacks,
			bonusStrikes,
			Stats.Modifier.MARTIAL_ARTS,
			null,
			null,
			0,
			0,
			0,
			ItemTemplate.EnchantmentCalculation.STRAIGHT,
			null,
			null,
			0F);

		return new Item(result)
		{
			public String getDisplayName()
			{
				return StringUtil.getGamesysString("unarmed.weapon");
			}
		};
	}

	/*-------------------------------------------------------------------------*/
	private void addTouchEffect(
		UnifiedActor actor,
		Stats.Modifier mod,
		String spellEffectName,
		GroupOfPossibilities<SpellEffect> spellEffects)
	{
		if (actor.getModifier(mod) > 0)
		{
			SpellEffect se = Database.getInstance().getSpellEffect(spellEffectName);
			spellEffects.add(se, actor.getModifier(mod));
		}
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * @return
	 * 	true if the given actor is afflicted by a condition the renders it
	 * 	immobile
	 */
	public boolean isActorImmobile(UnifiedActor actor)
	{
		for (Condition c : actor.getConditions())
		{
			if (c.getEffect().isImmobile(actor, c))
			{
				return true;
			}
		}

		return false;
	}

	/*-------------------------------------------------------------------------*/
	public boolean isActorAware(UnifiedActor actor)
	{
		if (!isActorAlive(actor))
		{
			return false;
		}

		if (!actor.isConscious())
		{
			return false;
		}

		for (Condition c : actor.getConditions())
		{
			if (!c.getEffect().isAware(actor, c))
			{
				return false;
			}
		}

		return true;
	}

	/*-------------------------------------------------------------------------*/
	public boolean isActorAlive(UnifiedActor actor)
	{
		return actor.getHitPoints().getCurrent() > 0;
	}

	/*-------------------------------------------------------------------------*/
	public boolean isActorAttackable(UnifiedActor actor)
	{
		if (!isActorAlive(actor))
		{
			return false;
		}

		for (Condition c : actor.getConditions())
		{
			if (!c.getEffect().canBeAttacked(actor, c))
			{
				return false;
			}
		}

		return true;
	}
	
	/*-------------------------------------------------------------------------*/
	public boolean isActorBlinkedOut(UnifiedActor actor)
	{
		if (!isActorAlive(actor))
		{
			return false;
		}
		
		for (Condition c : actor.getConditions())
		{
			if (c.getEffect().isBlinkedOut(actor, c))
			{
				return true;
			}
		}
		
		return false;
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * @param type
	 * 	A constant from {@link MagicSys.SpellEffectType}
	 * @return
	 * 	True if the given character is immune to this type of energy
	 */
	public boolean isActorImmuneToSpellEffect(UnifiedActor actor,
		MagicSys.SpellEffectSubType type)
	{
		Stats.Modifier modifier;

		modifier = getImmunityModifier(type);

		if (modifier == null)
		{
			return false;
		}

		return actor.getModifier(modifier) > 0;
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * @return
	 * 	the name of the modifier that defines if the given character is
	 * 	immune to this type of energy
	 */
	Stats.Modifier getImmunityModifier(MagicSys.SpellEffectSubType type)
	{
		return MagicSys.SpellEffectSubType.getImmunityModifier(type);
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * @return
	 * 	True if the given actor is immune to the given condition
	 */
	public boolean isActorImmuneToCondition(UnifiedActor actor, Condition c)
	{
		Stats.Modifier modifier = c.getEffect().getImmunityModifier();

		return modifier != null && actor.getModifier(modifier) > 0;
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * @return
	 * 	True if the given tile is immune to the given spell effect type
	 */
	public boolean isTileImmuneToCondition(Tile tile,
		MagicSys.SpellEffectSubType spellEffect)
	{
		Stats.Modifier modifier = getImmunityModifier(spellEffect);
		return tile.getStatModifier().getModifier(modifier) > 0;
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * @return
	 * 	true if the given actor should be asked for combat intentions
	 */
	public boolean askActorForCombatIntentions(UnifiedActor actor)
	{
		if (actor.getHitPoints().getCurrent() <= 0 || isActorImmobile(actor))
		{
			return false;
		}

		for (Condition c : actor.getConditions())
		{
			if (!c.getEffect().askForCombatIntentions(actor, c))
			{
				return false;
			}
		}

		return true;
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * Checks the conditions on this actor prior to the system processing
	 * it's combat intention. Some condition types have substitute combat
	 * intentions that are returned instead.
	 */
	public ActorActionIntention checkConditions(
		UnifiedActor actor,
		ActorActionIntention intention,
		Combat combat)
	{
		ActorActionIntention result = intention;

		List<Condition> conditions = actor.getConditions();
		if (conditions != null && intention != null)
		{
			Maze.log(Log.DEBUG, "Checking if conditions on ["+actor.getName() + "] will modify ["+intention+"]");

			List<Condition> list = new ArrayList<Condition>(conditions);
			for (Condition c : list)
			{
				ActorActionIntention actorActionIntention =
					c.getEffect().checkIntention(actor, combat, intention, c);
				Maze.log(Log.DEBUG, "["+c.getName() + "] returns ["+ actorActionIntention +"]");
				if (actorActionIntention != null)
				{
					result = actorActionIntention;
					result.setActor(intention.getActor());
				}
			}
		}

		return result;
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * Checks the conditions on this actor prior to it being allowed to take a
	 * combat action.  Some condition types have substitute actions that are
	 * returned instead.
	 */
	public CombatAction checkConditions(UnifiedActor actor, CombatAction action)
	{
		CombatAction result = action;
		
		// todo:
		// this doesn't really cater for weird combinations of immobility
		// and non-presence and so forth.
		// One way to get around this might be to priority-sort the conditions on
		// an actor and then take the first replaced action.
		// Another might be clever checking in the ConditionEffect implementations.
		// Defer until after condition effects are completely implemented.

		List<Condition> conditions = actor.getConditions();
		if (conditions != null)
		{
			List<Condition> list = new ArrayList<Condition>(conditions);
			for (Condition c : list)
			{
				CombatAction combatAction = c.getEffect().checkAction(actor, action, c);
				if (combatAction != null)
				{
					result = combatAction;
					result.setActor(action.getActor());
				}
			}
		}
		
		return result;
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * @return
	 * 	True if the given actor breaks free of the given web condition.
	 */
	public boolean actorBreaksFreeOfWeb(UnifiedActor actor, Condition condition)
	{
		int difficulty = condition.getStrength() + condition.getCastingLevel() + Dice.d4.roll();
		int actorTotal = actor.getModifier(Stats.Modifier.BRAWN) + Dice.d4.roll();

		return actorTotal > difficulty;
	}

	/*-------------------------------------------------------------------------*/
	public boolean actorGoesBeserk(UnifiedActor actor)
	{
		int basePercent = actor.getModifier(Stats.Modifier.BERSERKER);

		if (basePercent <= 0)
		{
			return false;
		}

		if (!actor.isConscious() || actor.getHitPoints().getCurrent() <= 0)
		{
			return false;
		}

		// more chance when faced with more foes
		int foeFactor = 0;
		Combat combat = Maze.getInstance().getCurrentCombat();
		if (combat != null)
		{
			List<UnifiedActor> foes = combat.getAllFoesOf(actor);
			if (foes != null)
			{
				foeFactor = foes.size();
			}
		}

		// up to 25% more chance when near death
		int woundFactor = (int)(25 * (1 - actor.getHitPoints().getRatio()));

		int chance = basePercent + foeFactor + woundFactor;

		return Dice.d100.roll() <= chance;
	}
	
	/*-------------------------------------------------------------------------*/
	/**
	 * @return
	 * 	True if the given actor cheats death
	 */ 
	public boolean actorCheatsDeath(UnifiedActor actor)
	{
		if (actor.getModifier(Stats.Modifier.CHEAT_DEATH) <= 0)
		{
			return false;
		}
		
		// base 10% chance, + "by strength of spirit"
		int chance = 10
			+ actor.getLevel()
			+ actor.getModifier(Stats.Modifier.BRAWN)
			+ actor.getModifier(Stats.Modifier.POWER)
			+ actor.getMagicPoints().getCurrent()
			+ (actor.getHitPoints().getMaximum()/10);
		
		// max 80%
		chance = Math.min(80, chance);
		
		return Dice.d100.roll() <= chance;
	}
	
	/*-------------------------------------------------------------------------*/
	/**
	 * Perform the various state changes on an actor who cheats death
	 */ 
	public void cheatDeath(UnifiedActor actor)
	{
		// reset HP to 20%-50%, with equal fatigue
		Dice d = new Dice(3, 10, 17);
		int percentHp = d.roll();
		CurMaxSub hp = actor.getHitPoints();
		int newHp = hp.getMaximum() * percentHp / 100;
		hp.setCurrent(newHp);
		hp.setSub(newHp);
		
		// drain stealth and magic
		actor.getActionPoints().setCurrent(0);
		actor.getMagicPoints().setCurrent(0);
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * @return
	 * 	True if this is a CRITICAL HIT INSTAKILL!
	 */ 
	public boolean isCriticalHit(UnifiedActor attacker, UnifiedActor defender, AttackWith attackWith)
	{
		if (attackWith == null)
		{
			// no critical possible
			return false;
		}
		
		int percent;
		if (attackWith.getToCritical() > 0)
		{
			percent = attackWith.getToCritical();
		}
		else
		{
			// this represents the penalty for attacking with a non-KIA weapon
			percent = -5;
		}

		if (defender instanceof Foe && ((Foe)defender).isImmuneToCriticals())
		{
			// no critical possible
			return false;
		}

		Stats.Modifier modifier = Stats.Modifier.MELEE_CRITICALS;

		if (attackWith instanceof Item)
		{
			Item item = (Item)attackWith;
			switch (item.getType())
			{
				case Type.SHORT_WEAPON:
				case Type.EXTENDED_WEAPON:
					modifier = Stats.Modifier.MELEE_CRITICALS;
					percent += attacker.getModifier(modifier);
					break;
				case Type.THROWN_WEAPON:
					modifier = Stats.Modifier.THROWN_CRITICALS;
					percent += attacker.getModifier(modifier);
					break;
				case Type.RANGED_WEAPON:
					modifier = Stats.Modifier.RANGED_CRITICALS;
					percent += attacker.getModifier(modifier);
					break;
				default: // no op
			}
		}
		else
		{
			// no critical possible
			return false;
		}
		
		if (percent < 1)
		{
			// no critical possible
			return false;
		}

		boolean result = Dice.d100.roll() <= percent;

		if (result)
		{
			practice(attacker, modifier, 1);
		}

		return result;
	}

	/*-------------------------------------------------------------------------*/
	public boolean isDeadlyStrike(UnifiedActor attacker, UnifiedActor defender, AttackWith attackWith)
	{
		if (defender instanceof Foe && ((Foe)defender).isImmuneToCriticals())
		{
			// no deadly strike possible
			return false;
		}

		if (attacker.getModifier(Stats.Modifier.DEADLY_STRIKE) <= 0)
		{
			// no deadly strike possible
			return false;
		}

		int percent = attacker.getModifier(Stats.Modifier.DEADLY_STRIKE);

		// modify slightly
		if (attackWith instanceof Item)
		{
			Item item = (Item)attackWith;
			switch (item.getType())
			{
				case Type.SHORT_WEAPON:
				case Type.EXTENDED_WEAPON:
					percent += (attacker.getModifier(Stats.Modifier.MELEE_CRITICALS)/2);
					break;
				case Type.THROWN_WEAPON:
					return false;
				case Type.RANGED_WEAPON:
					return false;
				default: // no op
			}
		}

		return Dice.d100.roll() <= percent;		
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * @return
	 * 	True if the defender successfully saves vs the critical
	 */ 
	public boolean saveVsCritical(UnifiedActor defender, UnifiedActor attacker)
	{
		return this.savingThrow(
			attacker,
			defender,
			MagicSys.SpellEffectType.NONE, // todo: fix this
			MagicSys.SpellEffectSubType.NORMAL_DAMAGE,
			0,
			0,
			0);
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * @return
	 * 	True if the attack is dodged
	 */
	public boolean isAttackDodged(UnifiedActor attacker, UnifiedActor defender, AttackWith attackWith)
	{
		if (defender.getModifier(Stats.Modifier.DODGE) <= 0)
		{
			return false;
		}

		int percent = defender.getModifier(Stats.Modifier.DODGE);

		percent -= attacker.getModifier(Stats.Modifier.VS_DODGE);

		return Dice.d100.roll() <= percent;
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * @return
	 * 	True if the attack is dodged
	 */
	public boolean isAttackParried(UnifiedActor attacker, UnifiedActor defender, AttackWith attackWith)
	{
		if (defender.getModifier(Stats.Modifier.PARRY) <= 0)
		{
			return false;
		}

		// PCs only parry with melee weapons
		if (defender instanceof PlayerCharacter)
		{
			if (!isActorArmedWithMeleeWeapon((PlayerCharacter)defender))
			{
				return false;
			}
		}

		// can only parry melee attacks
		if (attackWith.isRanged())
		{
			return false;
		}

		int percent = defender.getModifier(Stats.Modifier.PARRY);

		return Dice.d100.roll() <= percent;
	}

	/*-------------------------------------------------------------------------*/
	private boolean isActorArmedWithMeleeWeapon(UnifiedActor actor)
	{
		return actor.getPrimaryWeapon() != null &&
			(actor.getPrimaryWeapon().getType() == ItemTemplate.Type.SHORT_WEAPON ||
				actor.getPrimaryWeapon().getType() == ItemTemplate.Type.EXTENDED_WEAPON);
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * @return
	 * 	True if this attack is deflected
	 */ 
	public boolean isAttackDeflected(UnifiedActor attacker, UnifiedActor defender, AttackWith attackWith)
	{
		if (attackWith.isRanged())
		{
			// Arrow cutting property
			if (defender.getModifier(Stats.Modifier.ARROW_CUTTING) > 0)
			{
				if (Dice.d100.roll() <= defender.getModifier(Stats.Modifier.ARROW_CUTTING))
				{
					// projectile is deflected
					return true;
				}
			}
		}
		
		// todo: conditions the deflect stuff
		// todo: deflecting melee attacks
		
		// no luck for the defender
		return false;
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * Applies to the given modifiers any adjustments that an actor gets from
	 * surprising a foe.
	 */ 
	public StatModifier getSurpriseModifiers(UnifiedActor attacker)
	{
		int ambusher = attacker.getModifier(Stats.Modifier.AMBUSHER);
		if (ambusher > 0)
		{
			StatModifier modifier = new StatModifier();
			modifier.setModifier(Stats.Modifier.ATTACK, ambusher);
			modifier.setModifier(Stats.Modifier.BACKSTAB, ambusher);
			modifier.setModifier(Stats.Modifier.SNIPE, ambusher);
			modifier.setModifier(Stats.Modifier.DAMAGE, ambusher);
			modifier.setModifier(Stats.Modifier.TO_PENETRATE, ambusher);
			modifier.setModifier(Stats.Modifier.MELEE_CRITICALS, ambusher);
			modifier.setModifier(Stats.Modifier.THROWN_CRITICALS, ambusher);
			modifier.setModifier(Stats.Modifier.RANGED_CRITICALS, ambusher);
			return modifier;
		}
		
		return new StatModifier();
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * Apply any fatigue effects from the given attack
	 */
	public void fatigueFromAttack(AttackEvent event)
	{
		Stats.Modifier tirelessModifier;

		AttackWith attackWith = event.getAttackWith();
		if (attackWith instanceof Item)
		{
			switch (((Item)(attackWith)).getSubType())
			{
				case ItemTemplate.WeaponSubType.AXE: tirelessModifier = Stats.Modifier.TIRELESS_AXE; break;
				case ItemTemplate.WeaponSubType.BOW: tirelessModifier = Stats.Modifier.TIRELESS_BOW; break;
				case ItemTemplate.WeaponSubType.DAGGER: tirelessModifier = Stats.Modifier.TIRELESS_DAGGER; break;
				case ItemTemplate.WeaponSubType.MACE: tirelessModifier = Stats.Modifier.TIRELESS_MACE; break;
				case ItemTemplate.WeaponSubType.MARTIAL_ARTS: tirelessModifier = Stats.Modifier.TIRELESS_UNARMED; break;
				case ItemTemplate.WeaponSubType.POLEARM: tirelessModifier = Stats.Modifier.TIRELESS_SPEAR; break;
				case ItemTemplate.WeaponSubType.STAFF: tirelessModifier = Stats.Modifier.TIRELESS_STAFF; break;
				case ItemTemplate.WeaponSubType.SWORD: tirelessModifier = Stats.Modifier.TIRELESS_SWORD; break;
				case ItemTemplate.WeaponSubType.THROWN: tirelessModifier = Stats.Modifier.TIRELESS_THROWN; break;
				default: tirelessModifier = null;
			}
		}
		else
		{
			// for foe attacks, use the UNARMED tireless modifier.  It's a hack, but
			// an easy way of providing for the occasional tireless foe.
			tirelessModifier = Stats.Modifier.TIRELESS_UNARMED;
		}
		
		// only deduct fatigue if the attacker is not tireless with this weapon type 
		if (tirelessModifier == null || event.getAttacker().getModifier(tirelessModifier) <= 0)
		{
			CurMaxSub hp = event.getAttacker().getHitPoints();
			hp.incSub(1);
		}
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * Set any penalty modifiers on the action as a result of the player wielding
	 * two weapons.
	 */
	public void setDualWeaponPenalties(
		CombatAction action, 
		UnifiedActor actor,
		boolean isPrimaryWeapon)
	{
		//
		// Dual weapons skill can alleviate the penalties but not remove them
		//
		
		int base = isPrimaryWeapon ? -5 : -10;
		int modifier = actor.getModifier(Stats.Modifier.DUAL_WEAPONS);

		if (actor.isActiveModifier(Stats.Modifier.DUAL_WEAPONS))
		{
			practice(actor, Stats.Modifier.DUAL_WEAPONS, 1);
		}

		action.setModifier(Stats.Modifier.ATTACK, Math.min(0, base+modifier));
	}
	
	/*-------------------------------------------------------------------------*/
	/**
	 * @return the price in GP to create a character in one of the in-game guilds.
	 */
	public int getCreateCharacterCost()
	{
		return 20000;
	}
	
	/*-------------------------------------------------------------------------*/
	/**
	 * @return the price in GP to recruite a character from on of the in-game guilds.
	 */
	public int getRecruitCharacterCost()
	{
		return 500;
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * @return the nr of points that can be put into modifiers on character creation.
	 */
	public int getAssignableModifiersOnCharacterCreation()
	{
		return 5;
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * @return the max nr of points that can be put into a single modifier on
	 * level up or at character creation.
	 */
	public int getMaxAssignableToAModifierOnLevelUp()
	{
		return 2;
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * @return the number of points required to be spend to increase the
	 * given modifier by 1 for this player character.
	 */
	public int getModifierIncreaseCost(Stats.Modifier modifier, PlayerCharacter pc, int modifierValue)
	{
		StatModifier attributeCeilings = pc.getRace().getAttributeCeilings();
		int ceiling = attributeCeilings.getModifier(modifier);

		// magic dead races always pay 2 to increase power
		if (pc.getRace().isMagicDead() && Stats.Modifier.POWER.equals(modifier))
		{
			return 2;
		}

		// a zero ceiling indicates a default ceiling of 10
		if (ceiling == 0)
		{
			ceiling = 10;
		}

		/*if (modifierValue < ceiling)
		{
			return 1;
		}
		else
		{
			return 2;
		}*/

		return 1 + (modifierValue/ceiling);
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * @return true if a new item gets an enchantment
	 */
	public boolean applyEnchantmentToNewItem(ItemTemplate template)
	{
		if (template.enchantmentScheme == null)
		{
			return false;
		}

		int chance = template.enchantmentChance;
		int clvl = 0;
		if (Maze.getInstance() != null && Maze.getInstance().getParty() != null)
		{
			clvl = Maze.getInstance().getParty().getPartyLevel();
		}

		switch (template.enchantmentCalculation)
		{
			case PARTY_LEVEL:
				chance *= clvl;
				break;
		}

		return Dice.d100.roll() <= chance;
	}

	/*-------------------------------------------------------------------------*/
	public int getRandomEncounterChance(Tile t)
	{
		DifficultyLevel dl = Maze.getInstance().getDifficultyLevel();
		return dl.getRandomEncounterChance(t);
	}

	/*-------------------------------------------------------------------------*/
	public StatModifier getModifierForCarryingCapacity(
		UnifiedActor pc)
	{
		int cur = pc.getCarrying();
		int max = getCarryingCapacity(pc);

		if (cur <= max * .5)
		{
			return StatModifier.NULL_STAT_MODIFIER;
		}
		else if (cur <= max * .75)
		{
			return modEncumbrance;
		}
		else if (cur <= max)
		{
			return heavyEncumbrance;
		}
		else
		{
			return insaneEncumbrance;
		}
	}

	/*-------------------------------------------------------------------------*/
	public ModifierValue modifyModifierForSpecialAbility(
		UnifiedActor actor,
		Stats.Modifier modifier)
	{
		if (Stats.Modifier.HIT_POINT_REGEN.equals(modifier))
		{
			if (actor.getModifier(Stats.Modifier.ARCANE_BLOOD) > 0)
			{
				return new ModifierValue(
					StringUtil.getModifierName(Stats.Modifier.ARCANE_BLOOD),
					actor.getModifier(Stats.Modifier.RED_MAGIC_GEN));
			}
		}
		else if (Stats.Modifier.MAGIC_POINT_REGEN.equals(modifier))
		{
			if (actor.getModifier(Stats.Modifier.FEY_AFFINITY) > 0)
			{
				return new ModifierValue(
					StringUtil.getModifierName(Stats.Modifier.FEY_AFFINITY),
					actor.getModifier(Stats.Modifier.GOLD_MAGIC_GEN));
			}
		}
		else if (Stats.Modifier.BONUS_ATTACKS.equals(modifier))
		{
			if (actor.getModifier(Stats.Modifier.KI_FURY) > 0)
			{
				int green = actor.getModifier(Stats.Modifier.GREEN_MAGIC_GEN);

				if (green >= 4)
				{
					return new ModifierValue(
						StringUtil.getModifierName(Stats.Modifier.KI_FURY), 1);
				}
			}
		}
		else if (Stats.Modifier.DEFENCE.equals(modifier))
		{
			if (actor.getModifier(Stats.Modifier.DIVINE_PROTECTION) > 0)
			{
				return new ModifierValue(
					StringUtil.getModifierName(Stats.Modifier.DIVINE_PROTECTION),
					actor.getModifier(Stats.Modifier.WHITE_MAGIC_GEN));
			}
		}
		else if (Stats.Modifier.MELEE_CRITICALS.equals(modifier))
		{
			if (actor.getModifier(Stats.Modifier.SHADOW_MASTER) > 0)
			{
				return new ModifierValue(
					StringUtil.getModifierName(Stats.Modifier.SHADOW_MASTER),
					actor.getModifier(Stats.Modifier.BLACK_MAGIC_GEN));
			}
		}
		else if (Stats.Modifier.THROWN_CRITICALS.equals(modifier))
		{
			if (actor.getModifier(Stats.Modifier.SHADOW_MASTER) > 0)
			{
				return new ModifierValue(
					StringUtil.getModifierName(Stats.Modifier.SHADOW_MASTER),
					actor.getModifier(Stats.Modifier.BLACK_MAGIC_GEN));
			}
		}
		else if (Stats.Modifier.RANGED_CRITICALS.equals(modifier))
		{
			if (actor.getModifier(Stats.Modifier.SHADOW_MASTER) > 0)
			{
				return new ModifierValue(
					StringUtil.getModifierName(Stats.Modifier.SHADOW_MASTER),
					actor.getModifier(Stats.Modifier.BLACK_MAGIC_GEN));
			}
		}
		else if (Stats.Modifier.OBFUSCATION.equals(modifier))
		{
			if (actor.getModifier(Stats.Modifier.CHARMED_DESTINY) > 0)
			{
				return new ModifierValue(
									StringUtil.getModifierName(Stats.Modifier.CHARMED_DESTINY),
					Math.max(actor.getModifier(Stats.Modifier.PURPLE_MAGIC_GEN) - 1, 0));
			}
		}
		else if (Stats.Modifier.TO_RUN_AWAY.equals(modifier))
		{
			if (actor.getModifier(Stats.Modifier.CHARMED_DESTINY) > 0)
			{
				return new ModifierValue(
					StringUtil.getModifierName(Stats.Modifier.CHARMED_DESTINY),
					actor.getModifier(Stats.Modifier.PURPLE_MAGIC_GEN));
			}
		}
		else if (Stats.Modifier.RESIST_ENERGY.equals(modifier))
		{
			if (actor.getModifier(Stats.Modifier.CHARMED_DESTINY) > 0)
			{
				return new ModifierValue(
					StringUtil.getModifierName(Stats.Modifier.CHARMED_DESTINY),
					actor.getModifier(Stats.Modifier.PURPLE_MAGIC_GEN));
			}
		}
		else if (Stats.Modifier.INITIATIVE.equals(modifier))
		{
			if (actor.getModifier(Stats.Modifier.CHANNELLING) > 0)
			{
				return new ModifierValue(
					StringUtil.getModifierName(Stats.Modifier.CHANNELLING),
					actor.getModifier(Stats.Modifier.BLUE_MAGIC_GEN)/2);
			}
		}
		else if (Stats.Modifier.POWER_CAST.equals(modifier))
		{
			if (actor.getModifier(Stats.Modifier.POWER_OF_DARKNESS) > 0)
			{
				return new ModifierValue(
					StringUtil.getModifierName(Stats.Modifier.POWER_OF_DARKNESS),
					actor.getModifier(Stats.Modifier.BLACK_MAGIC_GEN)*5);
			}
		}
		else if (Stats.Modifier.PARRY.equals(modifier))
		{
			if (actor.getModifier(Stats.Modifier.SWORD_PARRY) > 0 &&
				actor.getPrimaryWeapon() != null &&
				actor.getPrimaryWeapon().getSubType() == ItemTemplate.WeaponSubType.SWORD)
			{
				return new ModifierValue(
					StringUtil.getModifierName(Stats.Modifier.SWORD_PARRY),
					actor.getModifier(Stats.Modifier.PARRY) +
					actor.getModifier(Stats.Modifier.SWORD_PARRY));
			}
			else if (actor.getModifier(Stats.Modifier.AXE_PARRY) > 0 &&
				actor.getPrimaryWeapon() != null &&
				actor.getPrimaryWeapon().getSubType() == ItemTemplate.WeaponSubType.AXE)
			{
				return new ModifierValue(
					StringUtil.getModifierName(Stats.Modifier.AXE_PARRY),
					actor.getModifier(Stats.Modifier.PARRY) +
						actor.getModifier(Stats.Modifier.AXE_PARRY));
			}
			else if (actor.getModifier(Stats.Modifier.MACE_PARRY) > 0 &&
				actor.getPrimaryWeapon() != null &&
				actor.getPrimaryWeapon().getSubType() == ItemTemplate.WeaponSubType.MACE)
			{
				return new ModifierValue(
					StringUtil.getModifierName(Stats.Modifier.MACE_PARRY),
					actor.getModifier(Stats.Modifier.PARRY) +
						actor.getModifier(Stats.Modifier.MACE_PARRY));
			}
			else if (actor.getModifier(Stats.Modifier.POLEARM_PARRY) > 0 &&
				actor.getPrimaryWeapon() != null &&
				actor.getPrimaryWeapon().getSubType() == ItemTemplate.WeaponSubType.POLEARM)
			{
				return new ModifierValue(
					StringUtil.getModifierName(Stats.Modifier.POLEARM_PARRY),
					actor.getModifier(Stats.Modifier.PARRY) +
						actor.getModifier(Stats.Modifier.POLEARM_PARRY));
			}
			else if (actor.getModifier(Stats.Modifier.STAFF_PARRY) > 0 &&
				actor.getPrimaryWeapon() != null &&
				actor.getPrimaryWeapon().getSubType() == ItemTemplate.WeaponSubType.STAFF)
			{
				return new ModifierValue(
					StringUtil.getModifierName(Stats.Modifier.STAFF_PARRY),
					actor.getModifier(Stats.Modifier.PARRY) +
						actor.getModifier(Stats.Modifier.STAFF_PARRY));
			}
		}

		return null;
	}

	/*-------------------------------------------------------------------------*/

	/**
	 * @return
	 * 	The number of units of supplies to be consumed when the given
	 * 	character rests.
	 */
	public int getSuppliesNeededToRest(UnifiedActor actor)
	{
		if (actor.getModifier(Stats.Modifier.LARGE_SIZE) > 0)
		{
			return 3;
		}
		else
		{
			return 2;
		}
	}

	/*-------------------------------------------------------------------------*/

	/**
	 * @return
	 * 	The units of supplies consumed by this PC when resting
	 */
	public int getSuppliesConsumedWhileResting(UnifiedActor actor,
		PlayerParty group)
	{
		int needed = getSuppliesNeededToRest(actor);

		int totalNeeded = 0;
		for (PlayerCharacter p : group.getPlayerCharacters())
		{
			totalNeeded += getSuppliesNeededToRest(actor);
		}

		if (totalNeeded < group.getSupplies())
		{
			// no food shortage
			return needed;
		}

		// otherwise, work out the rationing
		// front characters get the leftovers

		int index = group.getPlayerCharacterIndex((PlayerCharacter)actor);
		int minPerPerson = group.getSupplies() / group.getActors().size();
		int remainder = group.getSupplies() % group.getActors().size();

		if (index < remainder)
		{
			return minPerPerson + 1;
		}
		else
		{
			return minPerPerson;
		}
	}

	/*-------------------------------------------------------------------------*/

	/**
	 * @return Any attidude change from giving an item to some encountered actors.
	 */
	public NpcFaction.AttitudeChange giveItemToActors(ActorEncounter actorEncounter, Item item)
	{
		Foe leader = actorEncounter.getLeader();

		if (item.getBaseCost() > (leader.getLevel()*500))
		{
			return NpcFaction.AttitudeChange.BETTER;
		}
		else if (item.getBaseCost() < leader.getLevel()*50)
		{
			return NpcFaction.AttitudeChange.WORSE;
		}
		else
		{
			return NpcFaction.AttitudeChange.NO_CHANGE;
		}
	}

	/*-------------------------------------------------------------------------*/

	/**
	 * @return
	 * 	true if this actor can pay the cost of casting this spell
	 */
	public boolean canPaySpellCost(Spell spell, int castingLevel,
		UnifiedActor caster)
	{
		int hpCost = MagicSys.getInstance().getPointCost(
			spell.getHitPointCost(), castingLevel, caster);
		int apCost = MagicSys.getInstance().getPointCost(
			spell.getActionPointCost(), castingLevel, caster);
		int mpCost = MagicSys.getInstance().getPointCost(
			spell.getMagicPointCost(), castingLevel, caster);

		if (hpCost > caster.getHitPoints().getCurrent())
		{
			Maze.log(Log.DEBUG, "insufficient HP points: "+hpCost+" > "+caster.getHitPoints().getCurrent());
			return false;
		}

		if (apCost > caster.getActionPoints().getCurrent())
		{
			Maze.log(Log.DEBUG, "insufficient AP points: "+apCost+" > "+caster.getActionPoints().getCurrent());
			return false;
		}

		if (mpCost > caster.getMagicPoints().getCurrent())
		{
			Maze.log(Log.DEBUG, "insufficient MP points: "+mpCost+" > "+caster.getMagicPoints().getCurrent());
			return false;
		}

		return true;
	}

	/*-------------------------------------------------------------------------*/

	/**
	 * @return
	 * 	The actor (if any) who spots the given hidden stash. If no one spots
	 * 	it, return null.
	 */
	public PlayerCharacter scoutingSpotsStash(Maze maze, int spotDifficulty)
	{
		PlayerParty party = maze.getParty();

		for (PlayerCharacter pc : party.getPlayerCharacters())
		{
			if (isActorAware(pc) && pc.getModifier(Stats.Modifier.SCOUTING) >= spotDifficulty)
			{
				practice(pc, Stats.Modifier.SCOUTING, 1);
				return pc;
			}
		}

		return null;
	}

	/*-------------------------------------------------------------------------*/

	/**
	 * @return
	 * 	The actor (if any) who finds the given hidden stash. If no one finds
	 * 	it, return null.
	 */
	public PlayerCharacter scoutingFindsStash(Maze maze, int findDifficulty)
	{
		PlayerParty party = maze.getParty();

		for (PlayerCharacter pc : party.getPlayerCharacters())
		{
			if (isActorAware(pc) && pc.getModifier(Stats.Modifier.SCOUTING) >= findDifficulty)
			{
				practice(pc, Stats.Modifier.SCOUTING, 1);
				return pc;
			}
		}

		return null;
	}

	/*-------------------------------------------------------------------------*/

	/**
	 * @return
	 * 	The fatigue point cost of every move on a water tile.
	 */
	public int getSwimmingFatigueCost(UnifiedActor a)
	{
		if (a.getModifier(Stats.Modifier.AMPHIBIOUS) > 0)
		{
			return 0;
		}
		else if (a.getModifier(Stats.Modifier.STRONG_SWIMMER) > 0)
		{
			return 5;
		}
		else
		{
			return 10;
		}
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * @return
	 * 	true if the given character can wield two-handed items of the given
	 * 	type with one hand
	 */
	public boolean oneHandWieldApplies(PlayerCharacter pc, Item item)
	{
		if (item == null)
		{
			return false;
		}

		return
			pc.getModifier(Stats.Modifier.SWORD_1H_WIELD) > 0
				&& item.getWeaponType() == ItemTemplate.WeaponSubType.SWORD ||
			pc.getModifier(Stats.Modifier.AXE_1H_WIELD) > 0
				&& item.getWeaponType() == ItemTemplate.WeaponSubType.AXE ||
			pc.getModifier(Stats.Modifier.MACE_1H_WIELD) > 0
				&& item.getWeaponType() == ItemTemplate.WeaponSubType.MACE ||
			pc.getModifier(Stats.Modifier.POLEARM_1H_WIELD) > 0
				&& item.getWeaponType() == ItemTemplate.WeaponSubType.POLEARM ||
			pc.getModifier(Stats.Modifier.STAFF_1H_WIELD) > 0
				&& item.getWeaponType() == ItemTemplate.WeaponSubType.STAFF;

	}

	/*-------------------------------------------------------------------------*/
	public static class DummyCaster extends AbstractActor
	{
		FoeGroup actorGroup;
		Spell spell;
		int casterLevel, castingLevel;
		String name;

		/*----------------------------------------------------------------------*/
		public DummyCaster(Spell spell, int casterLevel, int castingLevel)
		{
			this.name = spell.getDescription();
			this.casterLevel = casterLevel;
			this.castingLevel = castingLevel;
			this.spell = spell;
		}

		public String getName()
		{
			return name;
		}

		public ActorGroup getActorGroup()
		{
			return actorGroup;
		}

		public int getLevel()
		{
			return casterLevel;
		}

		public int getModifier(Stats.Modifier modifier)
		{
			return 0;
		}

		public CurMax getMagicPoints()
		{
			return new CurMax(Integer.MAX_VALUE);
		}

		public List<CombatAction> getCombatActions(
			ActorActionIntention actionIntention)
		{
			if (!(actionIntention instanceof SpellIntention))
			{
				throw new MazeException("Invalid intention "+actionIntention);
			}

			List<CombatAction> result = new ArrayList<CombatAction>();
			SpellAction spellAction =
				new SpellAction(((SpellIntention)actionIntention).getTarget(), spell, castingLevel);
			spellAction.setActor(this);
			result.add(spellAction);
			return result;
		}
	}
}
