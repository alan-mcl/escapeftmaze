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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.util.*;
import mclachlan.maze.data.Database;
import mclachlan.maze.game.MazeScript;
import mclachlan.maze.stat.*;
import mclachlan.maze.stat.magic.Spell;
import mclachlan.maze.stat.magic.SpellEffect;

/**
 *
 */
public class V1ItemTemplate
{
	private static final String SEP = ",";

	/*-------------------------------------------------------------------------*/
	static V1List<ItemTemplate.AmmoType> ammoTypeList = new V1List<ItemTemplate.AmmoType>()
	{
		public String typeToString(ItemTemplate.AmmoType ammoType)
		{
			return ammoType.toString();
		}

		public ItemTemplate.AmmoType typeFromString(String s)
		{
			return ItemTemplate.AmmoType.valueOf(s);
		}
	};

	/*-------------------------------------------------------------------------*/
	static V1PercentageTable<ItemEnchantment> enchantmentTable
		= new V1PercentageTable<ItemEnchantment>()
	{
		public ItemEnchantment typeFromString(String s)
		{
			return V1ItemEnchantment.fromString(s);
		}

		public String typeToString(ItemEnchantment ie)
		{
			return V1ItemEnchantment.toString(ie);
		}
	};

	/*-------------------------------------------------------------------------*/
	public static Map<String, ItemTemplate> load(BufferedReader reader) throws Exception
	{
		Map <String, ItemTemplate> result = new HashMap<String, ItemTemplate>();
		while (true)
		{
			Properties p = V1Utils.getProperties(reader);
			if (p.isEmpty())
			{
				break;
			}
			ItemTemplate g = fromProperties(p);
			result.put(g.getName(), g);
		}

		return result;
	}

	/*-------------------------------------------------------------------------*/
	public static void save(BufferedWriter writer, Map<String, ItemTemplate> map) throws Exception
	{
		for (String name : map.keySet())
		{
			ItemTemplate g = map.get(name);
			writer.write(toProperties(g));
			writer.write("@");
			writer.newLine();
		}
	}

	/*-------------------------------------------------------------------------*/
	private static String toProperties(ItemTemplate obj)
	{
		StringBuilder b = new StringBuilder();

		b.append("name=");
		b.append(obj.getName());
		b.append(V1Utils.NEWLINE);

		if (obj.getClass() != ItemTemplate.class)
		{
			// custom impl
			b.append("impl=");
			b.append(obj.getClass().getName());
			b.append(V1Utils.NEWLINE);
		}
		else
		{
			b.append("pluralName=");
			b.append(obj.getPluralName());
			b.append(V1Utils.NEWLINE);

			b.append("unidentifiedName=");
			b.append(obj.getUnidentifiedName());
			b.append(V1Utils.NEWLINE);

			b.append("type=");
			b.append(obj.getType());
			b.append(V1Utils.NEWLINE);

			b.append("subType=");
			b.append(obj.getSubtype());
			b.append(V1Utils.NEWLINE);

			b.append("description=");
			b.append(V1Utils.escapeNewlines(obj.getDescription()));
			b.append(V1Utils.NEWLINE);

			b.append("image=");
			b.append(obj.getImage());
			b.append(V1Utils.NEWLINE);

			b.append("modifiers=");
			b.append(V1StatModifier.toString(obj.getModifiers()));
			b.append(V1Utils.NEWLINE);

			b.append("equipableSlots=");
			b.append(V1BitSet.toString(obj.getEquipableSlots()));
			b.append(V1Utils.NEWLINE);

			b.append("weight=");
			b.append(obj.getWeight());
			b.append(V1Utils.NEWLINE);

			b.append("usableByCharacterClass=");
			Set<String> heroic = obj.getUsableByCharacterClass();
			b.append(heroic==null?"":V1Utils.stringList.toString(new ArrayList<String>(heroic)));
			b.append(V1Utils.NEWLINE);

			b.append("usableByRace=");
			Set<String> race = obj.getUsableByRace();
			b.append(race==null?"":V1Utils.stringList.toString(new ArrayList<String>(race)));
			b.append(V1Utils.NEWLINE);

			b.append("usableByGender=");
			Set<String> gender = obj.getUsableByGender();
			b.append(gender==null?"":V1Utils.stringList.toString(new ArrayList<String>(gender)));
			b.append(V1Utils.NEWLINE);

			b.append("questItem=");
			b.append(obj.isQuestItem());
			b.append(V1Utils.NEWLINE);

			b.append("curseStrength=");
			b.append(obj.getCurseStrength());
			b.append(V1Utils.NEWLINE);

			b.append("maxItemsPerStack=");
			b.append(obj.getMaxItemsPerStack());
			b.append(V1Utils.NEWLINE);

			b.append("baseCost=");
			b.append(obj.getBaseCost());
			b.append(V1Utils.NEWLINE);

			b.append("invokedSpell=");
			b.append(obj.getInvokedSpell()==null?"":obj.getInvokedSpell().getName());
			b.append(V1Utils.NEWLINE);

			b.append("invokedSpellLevel=");
			b.append(obj.getInvokedSpellLevel());
			b.append(V1Utils.NEWLINE);

			b.append("charges=");
			b.append(V1Dice.toString(obj.getCharges()));
			b.append(V1Utils.NEWLINE);

			b.append("chargesType=");
			b.append(obj.getChargesType());
			b.append(V1Utils.NEWLINE);

			b.append("identificationDifficulty=");
			b.append(obj.getIdentificationDifficulty());
			b.append(V1Utils.NEWLINE);

			b.append("rechargeDifficulty=");
			b.append(obj.getRechargeDifficulty());
			b.append(V1Utils.NEWLINE);

			b.append("equipRequirements=");
			b.append(V1StatModifier.toString(obj.getEquipRequirements()));
			b.append(V1Utils.NEWLINE);

			b.append("useRequirements=");
			b.append(V1StatModifier.toString(obj.getUseRequirements()));
			b.append(V1Utils.NEWLINE);

			b.append("attackScript=");
			b.append(obj.getAttackScript()==null?"":obj.getAttackScript().getName());
			b.append(V1Utils.NEWLINE);

			b.append("damage=");
			b.append(V1Dice.toString(obj.getDamage()));
			b.append(V1Utils.NEWLINE);

			b.append("defaultDamageType=");
			b.append(obj.getDefaultDamageType());
			b.append(V1Utils.NEWLINE);

			b.append("attackTypes=");
			b.append(V1Utils.toStringStrings(obj.getAttackTypes(), SEP));
			b.append(V1Utils.NEWLINE);

			b.append("twoHanded=");
			b.append(obj.isTwoHanded());
			b.append(V1Utils.NEWLINE);

			b.append("isRanged=");
			b.append(obj.isRanged());
			b.append(V1Utils.NEWLINE);
			
			b.append("isReturning=");
			b.append(obj.isReturning());
			b.append(V1Utils.NEWLINE);

			b.append("isBackstabCapable=");
			b.append(obj.isBackstabCapable());
			b.append(V1Utils.NEWLINE);

			b.append("isSnipeCapable=");
			b.append(obj.isSnipeCapable());
			b.append(V1Utils.NEWLINE);

			b.append("toHit=");
			b.append(obj.getToHit());
			b.append(V1Utils.NEWLINE);

			b.append("toPenetrate=");
			b.append(obj.getToPenetrate());
			b.append(V1Utils.NEWLINE);
			
			b.append("toCritical=");
			b.append(obj.getToCritical());
			b.append(V1Utils.NEWLINE);

			b.append("toInitiative=");
			b.append(obj.getToInitiative());
			b.append(V1Utils.NEWLINE);

			b.append("minRange=");
			b.append(obj.getMinRange());
			b.append(V1Utils.NEWLINE);

			b.append("maxRange=");
			b.append(obj.getMaxRange());
			b.append(V1Utils.NEWLINE);

			b.append("ammo=");
			b.append(ammoTypeList.toString(obj.getAmmo()));
			b.append(V1Utils.NEWLINE);

			b.append("spellEffects=");
			b.append(V1FoeAttack.spellEffects.toString(obj.getSpellEffects()));
			b.append(V1Utils.NEWLINE);

			b.append("bonusAttacks=");
			b.append(obj.getBonusAttacks());
			b.append(V1Utils.NEWLINE);

			b.append("bonusStrikes=");
			b.append(obj.getBonusStrikes());
			b.append(V1Utils.NEWLINE);

			b.append("discipline=");
			b.append(obj.getDiscipline()==null?"":obj.getDiscipline());
			b.append(V1Utils.NEWLINE);

			b.append("slaysFoeType=");
			b.append(obj.getSlaysFoeType()==null?"":obj.getSlaysFoeType());
			b.append(V1Utils.NEWLINE);

			b.append("ammoType=");
			b.append(obj.getAmmoType()==null?"":obj.getAmmoType().toString());
			b.append(V1Utils.NEWLINE);

			b.append("damagePrevention=");
			b.append(obj.getDamagePrevention());
			b.append(V1Utils.NEWLINE);

			b.append("damagePreventionChance=");
			b.append(obj.getDamagePreventionChance());
			b.append(V1Utils.NEWLINE);

			b.append("enchantmentChance=");
			b.append(obj.getEnchantmentChance());
			b.append(V1Utils.NEWLINE);

			b.append("enchantmentCalculation=");
			b.append(obj.getEnchantmentCalculation().toString());
			b.append(V1Utils.NEWLINE);

			b.append("enchantmentScheme=");
			b.append(obj.getEnchantmentScheme()==null?"":obj.getEnchantmentScheme());
			b.append(V1Utils.NEWLINE);

			b.append("disassemblyLootTable=");
			b.append(obj.getDisassemblyLootTable()==null?"":obj.getDisassemblyLootTable());
			b.append(V1Utils.NEWLINE);

		}

		return b.toString();
	}

	/*-------------------------------------------------------------------------*/
	static ItemTemplate fromProperties(Properties p) throws Exception
	{
		if (p.getProperty("impl") != null)
		{
			// custom ItemTemplate impl
			Class clazz = Class.forName(p.getProperty("impl"));
			return (ItemTemplate)clazz.newInstance();
		}
		else
		{
			String name = p.getProperty("name");
			String pluralName = p.getProperty("pluralName");
			String unidentifiedName = p.getProperty("unidentifiedName");
			int type = Integer.parseInt(p.getProperty("type"));
			int subType = Integer.parseInt(p.getProperty("subType"));
			String description = V1Utils.replaceNewlines(p.getProperty("description"));
			String image = p.getProperty("image");
			StatModifier modifiers = V1StatModifier.fromString(p.getProperty("modifiers"));
			BitSet equipableSlots = V1BitSet.fromString(p.getProperty("equipableSlots"));
			int weight = Integer.parseInt(p.getProperty("weight"));
			List<String> classes = V1Utils.stringList.fromString(p.getProperty("usableByCharacterClass"));

			Set<String> usableByCharacterClass = null;

			if (classes != null)
			{
				usableByCharacterClass = new HashSet<String>(classes);
			}

			List<String> race = V1Utils.stringList.fromString(p.getProperty("usableByRace"));
			Set<String> usableByRace = race==null?null:new HashSet<String>(race);
			List<String> gender = V1Utils.stringList.fromString(p.getProperty("usableByGender"));
			Set<String> usableByGender = gender==null?null:new HashSet<String>(gender);
			boolean questItem = Boolean.valueOf(p.getProperty("questItem"));
			int curseStrength = Integer.parseInt(p.getProperty("curseStrength"));
			int maxItemsPerStack = Integer.parseInt(p.getProperty("maxItemsPerStack"));
			int baseCost = Integer.parseInt(p.getProperty("baseCost"));
			String spellName = p.getProperty("invokedSpell");
			Spell invokedSpell = spellName.equals("")?null:Database.getInstance().getSpell(spellName);
			int invokedSpellLevel = Integer.parseInt(p.getProperty("invokedSpellLevel"));
			Dice charges = V1Dice.fromString(p.getProperty("charges"));
			ItemTemplate.ChargesType chargesType = ItemTemplate.ChargesType.valueOf(p.getProperty("chargesType"));
			int identificationDifficulty = Integer.parseInt(p.getProperty("identificationDifficulty"));
			int rechargeDifficulty = Integer.parseInt(p.getProperty("rechargeDifficulty"));
			StatModifier equipRequirements = V1StatModifier.fromString(p.getProperty("equipRequirements"));
			StatModifier useRequirements = V1StatModifier.fromString(p.getProperty("useRequirements"));
			String attackScriptName = p.getProperty("attackScript");
			MazeScript attackScript = attackScriptName.equals("")?null:Database.getInstance().getScript(attackScriptName);
			Dice damage = V1Dice.fromString(p.getProperty("damage"));
			int defaultDamageType = Integer.parseInt(p.getProperty("defaultDamageType"));
			String[] attackTypes = V1Utils.fromStringStrings(p.getProperty("attackTypes"), SEP);
			boolean twoHanded = Boolean.valueOf(p.getProperty("twoHanded"));
			boolean isRanged = Boolean.valueOf(p.getProperty("isRanged"));
			boolean isReturning = Boolean.valueOf(p.getProperty("isReturning"));
			boolean isBackstabCapable = Boolean.valueOf(p.getProperty("isBackstabCapable"));
			boolean isSnipeCapable = Boolean.valueOf(p.getProperty("isSnipeCapable"));
			int toHit = Integer.parseInt(p.getProperty("toHit"));
			int toCritical = Integer.parseInt(p.getProperty("toCritical"));
			int toPenetrate = Integer.parseInt(p.getProperty("toPenetrate"));
			int toInitiative = Integer.parseInt(p.getProperty("toInitiative"));
			int minRange = Integer.parseInt(p.getProperty("minRange"));
			int maxRange = Integer.parseInt(p.getProperty("maxRange"));
			List<ItemTemplate.AmmoType> ammo = ammoTypeList.fromString(p.getProperty("ammo"));
			GroupOfPossibilities<SpellEffect> spellEffects = V1FoeAttack.spellEffects.fromString(p.getProperty("spellEffects"));
			int bonusAttacks = Integer.parseInt(p.getProperty("bonusAttacks"));
			int bonusStrikes = Integer.parseInt(p.getProperty("bonusStrikes"));
			String discipline = p.getProperty("discipline").equals("")?null:p.getProperty("discipline");
			String s = p.getProperty("slaysFoeType");
			String slaysFoeType = "".equals(s)?null:s;
			String ammoTypeName = p.getProperty("ammoType");
			ItemTemplate.AmmoType ammoType = ammoTypeName.equals("")?null:ItemTemplate.AmmoType.valueOf(ammoTypeName);
			int damagePrevention = Integer.parseInt(p.getProperty("damagePrevention"));
			int damagePreventionChance = Integer.parseInt(p.getProperty("damagePreventionChance"));
			int enchantmentChance = Integer.parseInt(p.getProperty("enchantmentChance"));
			ItemTemplate.EnchantmentCalculation enchantmentCalc =
				ItemTemplate.EnchantmentCalculation.valueOf(p.getProperty("enchantmentCalculation"));
			String enchantmentScheme = "".equals(p.getProperty("enchantmentScheme")) ?null: p.getProperty("enchantmentScheme");
			String disassemblyLootTable = "".equals(p.getProperty("disassemblyLootTable")) ?null: p.getProperty("disassemblyLootTable");


			return new ItemTemplate(
				name,
				pluralName,
				unidentifiedName,
				type,
				subType,
				description,
				modifiers,
				image,
				equipableSlots,
				weight,
				maxItemsPerStack,
				baseCost,
				invokedSpell,
				invokedSpellLevel,
				charges,
				chargesType,
				usableByCharacterClass,
				usableByRace,
				usableByGender,
				questItem,
				curseStrength,
				identificationDifficulty,
				rechargeDifficulty,
				equipRequirements,
				useRequirements,
				attackScript,
				damage,
				defaultDamageType,
				attackTypes,
				twoHanded,
				isRanged,
				isReturning,
				isBackstabCapable,
				isSnipeCapable,
				toHit,
				toPenetrate,
				toCritical,
				toInitiative,
				minRange,
				maxRange,
				ammo,
				spellEffects,
				bonusAttacks,
				bonusStrikes,
				discipline,
				slaysFoeType,
				ammoType,
				damagePrevention,
				damagePreventionChance,
				enchantmentChance,
				enchantmentCalc,
				enchantmentScheme,
				disassemblyLootTable);
		}
	}
}
