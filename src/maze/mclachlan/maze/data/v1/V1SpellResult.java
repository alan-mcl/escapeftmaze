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

package mclachlan.maze.data.v1;

import java.util.*;
import mclachlan.maze.data.Database;
import mclachlan.maze.stat.*;
import mclachlan.maze.stat.combat.AttackType;
import mclachlan.maze.stat.condition.ConditionEffect;
import mclachlan.maze.stat.condition.ConditionTemplate;
import mclachlan.maze.stat.magic.*;
import mclachlan.maze.util.MazeException;

/**
 *
 */
public class V1SpellResult
{
	static final String SEP = ":";
	static final String SUB_SEP = "/";
	public static Map<Class, Integer> types;

	public static final int CUSTOM = 0;
	public static final int ATTACK_WITH_WEAPON = 1;
	public static final int CHARM = 2;
	public static final int CONDITION = 3;
	public static final int DAMAGE = 4;
	public static final int HEALING = 5;
	public static final int IDENTIFY = 6;
	public static final int MIND_READ = 7;
	public static final int MIND_READ_FAILED = 8;
	public static final int RECHARGE = 9;
	public static final int REMOVE_CURSE = 10;
	public static final int SUMMONING = 11;
	public static final int THEFT = 12;
	public static final int THEFT_FAILED = 13;
	public static final int UNLOCK = 14;
	public static final int DRAIN = 15;
	public static final int CREATE_ITEM = 16;
	public static final int SINGLE_USE_SPELL = 17;
	public static final int CONDITION_REMOVAL = 18;
	public static final int DEATH = 19;
	public static final int CLOUD_SPELL = 20;
	public static final int PURIFY_AIR = 21;
	public static final int RESURRECTION = 22;
	public static final int BOOZE = 23;
	public static final int FORGET = 24;
	public static final int CONDITION_IDENTIFICATION = 25;
	public static final int LOCATE_PERSON = 26;
	public static final int REMOVE_ITEM = 27;
	public static final int CONDITION_TRANSFER = 28;

	public static final int MAX = 29;

	static
	{
		types = new HashMap<Class, Integer>();

		types.put(AttackWithWeaponSpellResult.class, ATTACK_WITH_WEAPON);
		types.put(CharmSpellResult.class, CHARM);
		types.put(ConditionSpellResult.class, CONDITION);
		types.put(DamageSpellResult.class, DAMAGE);
		types.put(HealingSpellResult.class, HEALING);
		types.put(IdentifySpellResult.class, IDENTIFY);
		types.put(MindReadSpellResult.class, MIND_READ);
		types.put(MindReadFailedSpellResult.class, MIND_READ_FAILED);
		types.put(RechargeSpellResult.class, RECHARGE);
		types.put(RemoveCurseSpellResult.class, REMOVE_CURSE);
		types.put(SummoningSpellResult.class, SUMMONING);
		types.put(TheftSpellResult.class, THEFT);
		types.put(TheftFailedSpellResult.class, THEFT_FAILED);
		types.put(UnlockSpellResult.class, UNLOCK);
		types.put(DrainSpellResult.class, DRAIN);
		types.put(ConditionRemovalSpellResult.class, CONDITION_REMOVAL);
		types.put(DeathSpellResult.class, DEATH);
		types.put(CloudSpellResult.class, CLOUD_SPELL);
		types.put(PurifyAirSpellResult.class, PURIFY_AIR);
		types.put(ResurrectionSpellResult.class, RESURRECTION);
		types.put(BoozeSpellResult.class, BOOZE);
		types.put(ForgetSpellResult.class, FORGET);
		types.put(ConditionIdentificationSpellResult.class, CONDITION_IDENTIFICATION);
		types.put(CreateItemSpellResult.class, CREATE_ITEM);
		types.put(LocatePersonSpellResult.class, LOCATE_PERSON);
		types.put(RemoveItemSpellResult.class, REMOVE_ITEM);
		types.put(SingleUseSpellSpellResult.class, SINGLE_USE_SPELL);
		types.put(ConditionTransferSpellResult.class, CONDITION_TRANSFER);
	}

	/*-------------------------------------------------------------------------*/
	static V1GroupOfPossibilties<String> spellEffects =
		new V1GroupOfPossibilties<String>(";",",")
	{
		public String typeFromString(String s)
		{
			return s;
		}

		public String typeToString(String spellEffect)
		{
			return spellEffect;
		}
	};

	
	/*-------------------------------------------------------------------------*/
	static V1List<ConditionEffect> conditionEffects = new V1List<ConditionEffect>()
	{
		public String typeToString(ConditionEffect conditionEffect)
		{
			return conditionEffect.getName();
		}

		public ConditionEffect typeFromString(String s)
		{
			return Database.getInstance().getConditionEffect(s);
		}
	};

	/*-------------------------------------------------------------------------*/
	public static String toString(SpellResult sr)
	{
		if (sr == null)
		{
			return "";
		}

		StringBuilder s = new StringBuilder();

		int type;
		if (types.containsKey(sr.getClass()))
		{
			type = types.get(sr.getClass());
		}
		else
		{
			type = CUSTOM;
		}
		s.append(type);
		s.append(SEP);

		if (sr.getFoeType() != null)
		{
			s.append(sr.getFoeType().getName());
		}
		s.append(SEP);

		if (sr.getFocusAffinity() != null)
		{
			s.append(sr.getFocusAffinity().name());
		}
		s.append(SEP);

		switch (type)
		{
			case CUSTOM:
				s.append(sr.getClass().getName());
				break;
			case ATTACK_WITH_WEAPON:
				AttackWithWeaponSpellResult awwsr = (AttackWithWeaponSpellResult)sr;
				s.append(V1StatModifier.toString(awwsr.getModifiers()));
				s.append(SEP);
				s.append(V1Value.toString(awwsr.getNrStrikes()));
				s.append(SEP);
				s.append(awwsr.getDamageType() == null ? "" : awwsr.getDamageType().toString());
				s.append(SEP);
				s.append(awwsr.getAttackScript()==null?"":awwsr.getAttackScript());
				s.append(SEP);
				s.append(awwsr.getAttackType()==null?"":awwsr.getAttackType().getName());
				s.append(SEP);
				s.append(Boolean.toString(awwsr.isRequiresBackstabWeapon()));
				s.append(SEP);
				s.append(Boolean.toString(awwsr.isRequiresSnipeWeapon()));
				s.append(SEP);
				s.append(Integer.toString(awwsr.getRequiredWeaponType()));
				s.append(SEP);
				s.append(Boolean.toString(awwsr.isConsumesWeapon()));
				s.append(SEP);
				s.append(spellEffects.toString(awwsr.getSpellEffects()));
				break;
			case CHARM:
				s.append(V1Value.toString(((CharmSpellResult)sr).getValue()));
				break;
			case CONDITION:
				s.append(((ConditionSpellResult)sr).getConditionTemplate().getName());
				break;
			case DAMAGE:
				DamageSpellResult dsr = (DamageSpellResult)sr;
				s.append(V1Value.toString(dsr.getHitPointDamage()));
				s.append(SEP);
				s.append(V1Value.toString(dsr.getFatigueDamage()));
				s.append(SEP);
				s.append(V1Value.toString(dsr.getActionPointDamage()));
				s.append(SEP);
				s.append(V1Value.toString(dsr.getMagicPointDamage()));
				s.append(SEP);
				s.append(dsr.getMultiplier());
				s.append(SEP);
				s.append(dsr.isTransferToCaster());
				break;
			case HEALING:
				s.append(V1Value.toString(((HealingSpellResult)sr).getHitPointHealing()));
				s.append(SEP);
				s.append(V1Value.toString(((HealingSpellResult)sr).getStaminaHealing()));
				s.append(SEP);
				s.append(V1Value.toString(((HealingSpellResult)sr).getActionPointHealing()));
				s.append(SEP);
				s.append(V1Value.toString(((HealingSpellResult)sr).getMagicPointHealing()));
				break;
			case IDENTIFY:
				IdentifySpellResult isr = (IdentifySpellResult)sr;
				s.append(V1Value.toString(isr.getValue()));
				s.append(SEP);
				s.append(isr.isRevealCurses());
				break;
			case MIND_READ:
				s.append(V1Value.toString(((MindReadSpellResult)sr).getValue()));
				break;
			case MIND_READ_FAILED:
				s.append(V1Value.toString(((MindReadFailedSpellResult)sr).getValue()));
				break;
			case RECHARGE:
				s.append(V1Value.toString(((RechargeSpellResult)sr).getValue()));
				break;
			case REMOVE_CURSE:
				s.append(V1Value.toString(((RemoveCurseSpellResult)sr).getValue()));
				break;
			case SUMMONING:
				SummoningSpellResult ssr = (SummoningSpellResult)sr;
				s.append(V1Utils.toStringStrings(ssr.getEncounterTable(), SUB_SEP));
				s.append(SEP);
				s.append(V1Value.toString(ssr.getStrength()));
				break;
			case THEFT:
				s.append(V1Value.toString(((TheftSpellResult)sr).getValue()));
				break;
			case THEFT_FAILED:
				s.append(V1Value.toString(((TheftFailedSpellResult)sr).getValue()));
				break;
			case UNLOCK:
				s.append(V1Value.toString(((UnlockSpellResult)sr).getValue()));
				break;
			case DRAIN:
				DrainSpellResult drainSpellResult = (DrainSpellResult)sr;
				s.append(V1Value.toString(drainSpellResult.getDrain()));
				s.append(SEP);
				s.append(drainSpellResult.getModifier());
				break;
			case CONDITION_REMOVAL:
				ConditionRemovalSpellResult crsr = (ConditionRemovalSpellResult)sr;
				s.append(V1Value.toString(crsr.getStrength()));
				s.append(SEP);
				s.append(conditionEffects.toString(crsr.getEffects()));
				break;
			case CONDITION_TRANSFER:
				ConditionTransferSpellResult ctsr = (ConditionTransferSpellResult)sr;
				s.append(ctsr.isDeliver());
				s.append(SEP);
				s.append(conditionEffects.toString(ctsr.getEffects()));
				break;
			case DEATH:
				DeathSpellResult d = (DeathSpellResult)sr;
				break;
			case CLOUD_SPELL:
				CloudSpellResult csr = (CloudSpellResult)sr;
				s.append(V1Value.toString(csr.getDuration()));
				s.append(SEP);
				s.append(V1Value.toString(csr.getStrength()));
				s.append(SEP);
				s.append(csr.getIcon());
				s.append(SEP);
				s.append(csr.getSpell());
				break;
			case PURIFY_AIR:
				PurifyAirSpellResult pasr = (PurifyAirSpellResult)sr;
				s.append(V1Value.toString(pasr.getStrength()));
				break;
			case RESURRECTION:
				ResurrectionSpellResult rsr = (ResurrectionSpellResult)sr;
				break;
			case BOOZE:
				BoozeSpellResult bsr = (BoozeSpellResult)sr;
				break;
			case FORGET:
				ForgetSpellResult fsr = (ForgetSpellResult)sr;
				s.append(V1Value.toString(fsr.getStrength()));
				break;
			case CONDITION_IDENTIFICATION:
				ConditionIdentificationSpellResult cisr = (ConditionIdentificationSpellResult)sr;
				s.append(V1Value.toString(cisr.getStrength()));
				s.append(SEP);
				s.append(cisr.isCanIdentifyConditionStrength());
				break;
			case CREATE_ITEM:
				CreateItemSpellResult createItemSpellResult = (CreateItemSpellResult)sr;
				s.append(createItemSpellResult.getLootTable());
				s.append(SEP);
				s.append(createItemSpellResult.isEquipItems());
				break;
			case LOCATE_PERSON:
				LocatePersonSpellResult locatePersonSpellResult = (LocatePersonSpellResult)sr;
				s.append(V1Value.toString(locatePersonSpellResult.getValue()));
				break;
			case REMOVE_ITEM:
				RemoveItemSpellResult removeItemSpellResult = (RemoveItemSpellResult)sr;
				s.append(removeItemSpellResult.getItemName());
				break;
			case SINGLE_USE_SPELL:
				SingleUseSpellSpellResult susr = (SingleUseSpellSpellResult)sr;
				break;

			default: throw new MazeException("Invalid type: "+type+" ["+sr+"]");
		}

		return s.toString();
	}

	/*-------------------------------------------------------------------------*/
	public static SpellResult fromString(String s)
	{
		if (s.equals(""))
		{
			return null;
		}
		return getSpellResult(s.split(SEP, -1));
	}

	/*-------------------------------------------------------------------------*/
	static SpellResult getSpellResult(String[] strs)
	{
		int i=0;
		int type = Integer.parseInt(strs[i++]);
		String s = strs[i++];
		TypeDescriptor foeType = "".equals(s)?null:new TypeDescriptorImpl(s);
		s = strs[i++];
		CharacterClass.Focus affinity = "".equals(s)?null: CharacterClass.Focus.valueOf(s);

		SpellResult result;

		switch (type)
		{
			case CUSTOM:
				try
				{
					Class clazz = Class.forName(strs[i++]);
					result = (SpellResult)clazz.newInstance();
				}
				catch (Exception e)
				{
					throw new MazeException(e);
				}
				break;
			case ATTACK_WITH_WEAPON:
				StatModifier modifiers = V1StatModifier.fromString(strs[i++]);
				ValueList nrStrikes = V1Value.fromString(strs[i++]);
				String damType = strs[i++];
				MagicSys.SpellEffectType damageType = "".equals(damType)?null:MagicSys.SpellEffectType.valueOf(damType);

				String scriptName = strs[i++];
				String attackScript = "".equals(scriptName)?null:scriptName;

				String attackTypeName = strs[i++];
				AttackType attackType = "".equals(attackTypeName)?null:Database.getInstance().getAttackType(attackTypeName);

				boolean requiresBackstabWeapon = Boolean.valueOf(strs[i++]);
				boolean requiresSnipeWeapon = Boolean.valueOf(strs[i++]);

				int requiredWeaponType = Integer.parseInt(strs[i++]);

				boolean consumesItem = Boolean.valueOf(strs[i++]);

				GroupOfPossibilities<String> se =
					spellEffects.fromString(strs[i++]);

				result = new AttackWithWeaponSpellResult(
					nrStrikes,
					modifiers,
					attackType,
					damageType,
					attackScript,
					requiresBackstabWeapon,
					requiresSnipeWeapon,
					consumesItem,
					requiredWeaponType,
					se);
				break;
			case CHARM:
				ValueList v = V1Value.fromString(strs[i++]);
				result = new CharmSpellResult(v);
				break;
			case CONDITION:
				ConditionTemplate ct = Database.getInstance().getConditionTemplate(strs[i++]);
				result = new ConditionSpellResult(ct);
				break;
			case DAMAGE:
				ValueList hp = V1Value.fromString(strs[i++]);
				ValueList fat = V1Value.fromString(strs[i++]);
				ValueList sp = V1Value.fromString(strs[i++]);
				ValueList mp = V1Value.fromString(strs[i++]);
				float multiplier = Float.parseFloat(strs[i++]);
				boolean transferToCaster = Boolean.valueOf(strs[i++]);
				result = new DamageSpellResult(hp, fat, sp, mp, multiplier, transferToCaster);
				break;
			case HEALING:
				hp = V1Value.fromString(strs[i++]);
				fat = V1Value.fromString(strs[i++]);
				sp = V1Value.fromString(strs[i++]);
				mp = V1Value.fromString(strs[i++]);
				result = new HealingSpellResult(hp, fat, sp, mp);
				break;
			case IDENTIFY:
				v = V1Value.fromString(strs[i++]);
				boolean revealCurses = Boolean.valueOf(strs[i++]);
				result = new IdentifySpellResult(v, revealCurses);
				break;
			case MIND_READ:
				v = V1Value.fromString(strs[i++]);
				result = new MindReadSpellResult(v);
				break;
			case MIND_READ_FAILED:
				v = V1Value.fromString(strs[i++]);
				result = new MindReadFailedSpellResult(v);
				break;
			case RECHARGE:
				v = V1Value.fromString(strs[i++]);
				result = new RechargeSpellResult(v);
				break;
			case REMOVE_CURSE:
				v = V1Value.fromString(strs[i++]);
				result = new RemoveCurseSpellResult(v);
				break;
			case SUMMONING:
				String[] table = V1Utils.fromStringStrings(strs[i++], SUB_SEP);
				v = V1Value.fromString(strs[i++]);
				result = new SummoningSpellResult(table, v);
				break;
			case THEFT:
				v = V1Value.fromString(strs[i++]);
				result = new TheftSpellResult(v);
				break;
			case THEFT_FAILED:
				v = V1Value.fromString(strs[i++]);
				result = new TheftFailedSpellResult(v);
				break;
			case UNLOCK:
				v = V1Value.fromString(strs[i++]);
				result = new UnlockSpellResult(v);
				break;
			case DRAIN:
				v = V1Value.fromString(strs[i++]);
				result = new DrainSpellResult(v, Stats.Modifier.valueOf(strs[i++]));
				break;
			case CONDITION_REMOVAL:
				v = V1Value.fromString(strs[i++]);
				List<ConditionEffect> effects = conditionEffects.fromString(strs[i++]);
				result = new ConditionRemovalSpellResult(effects, v);
				break;
			case CONDITION_TRANSFER:
				boolean deliver = Boolean.valueOf(strs[i++]);
				List<ConditionEffect> effectsList = conditionEffects.fromString(strs[i++]);
				result = new ConditionTransferSpellResult(effectsList, deliver);
				break;
			case DEATH:
				result = new DeathSpellResult();
				break;
			case CLOUD_SPELL:
				result = new CloudSpellResult(
					V1Value.fromString(strs[i++]),
					V1Value.fromString(strs[i++]),
					strs[i++],
					strs[i++]);
				break;
			case PURIFY_AIR:
				result = new PurifyAirSpellResult(V1Value.fromString(strs[i++]));
				break;
			case RESURRECTION:
				result = new ResurrectionSpellResult();
				break;
			case BOOZE:
				result = new BoozeSpellResult();
				break;
			case FORGET:
				result = new ForgetSpellResult(V1Value.fromString(strs[i++]));
				break;
			case CONDITION_IDENTIFICATION:
				v = V1Value.fromString(strs[i++]);
				boolean b = Boolean.valueOf(strs[i++]);
				result = new ConditionIdentificationSpellResult(v, b);
				break;
			case CREATE_ITEM:
				String lootTable = strs[i++];
				boolean equip = Boolean.valueOf(strs[i++]);
				result = new CreateItemSpellResult(lootTable, equip);
				break;
			case LOCATE_PERSON:
				v = V1Value.fromString(strs[i++]);
				result = new LocatePersonSpellResult(v);
				break;
			case REMOVE_ITEM:
				result = new RemoveItemSpellResult(strs[i++]);
				break;
			case SINGLE_USE_SPELL:
				result = new SingleUseSpellSpellResult();
				break;

			default: throw new MazeException(
				"Cannot parse spell result ["+V1Utils.toStringStrings(strs, ",")+"]");
		}

		result.setFoeType(foeType);
		result.setFocusAffinity(affinity);

		return result;
	}
}
