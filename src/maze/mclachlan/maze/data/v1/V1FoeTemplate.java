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
import mclachlan.maze.data.MazeTexture;
import mclachlan.maze.game.MazeScript;
import mclachlan.maze.map.LootTable;
import mclachlan.maze.stat.*;
import mclachlan.maze.stat.magic.SpellBook;
import mclachlan.maze.stat.npc.NpcFaction;
import mclachlan.maze.util.MazeException;

/**
 *
 */
public class V1FoeTemplate
{
	static V1PercentageTable<String> playerBodyParts = new V1PercentageTable<String>()
	{
		@Override
		public String typeFromString(String s)
		{
			return s;
		}

		@Override
		public String typeToString(String s)
		{
			return s;
		}
	};

	static V1List<SpellLikeAbility> spellLikeAbilities = new V1List<SpellLikeAbility>()
	{
		@Override
		public String typeToString(SpellLikeAbility spellLikeAbility)
		{
			return V1SpellLikeAbility.toString(spellLikeAbility);
		}

		@Override
		public SpellLikeAbility typeFromString(String s)
		{
			return V1SpellLikeAbility.fromString(s);
		}
	};

	static V1List<FoeType> foeTypes = new V1List<FoeType>()
	{
		@Override
		public String typeToString(FoeType foeType)
		{
			return foeType.getName();
		}

		@Override
		public FoeType typeFromString(String s)
		{
			FoeType foeType = Database.getInstance().getFoeTypes().get(s);
			if (foeType == null)
			{
				throw new MazeException("Missing foe type ["+s+"]");
			}
			return foeType;
		}
	};

	/*-------------------------------------------------------------------------*/
	public static Map<String, FoeTemplate> load(BufferedReader reader)
	{
		try
		{
			Map <String, FoeTemplate> result = new HashMap<>();
			while (true)
			{
				Properties p = V1Utils.getProperties(reader);
				if (p.isEmpty())
				{
					break;
				}
				FoeTemplate g = fromProperties(p);
				result.put(g.getName(), g);
			}

			return result;
		}
		catch (Exception e)
		{
			throw new MazeException(e);
		}
	}

	/*-------------------------------------------------------------------------*/
	public static void save(BufferedWriter writer, Map<String, FoeTemplate> map) throws Exception
	{
		for (String name : map.keySet())
		{
			FoeTemplate g = map.get(name);
			writer.write(toProperties(g));
			writer.write("@");
			writer.newLine();
		}
	}

	/*-------------------------------------------------------------------------*/
	private static String toProperties(FoeTemplate obj)
	{
		StringBuilder b = new StringBuilder();

		b.append("name=");
		b.append(obj.getName());
		b.append(V1Utils.NEWLINE);

		if (obj.getClass() != FoeTemplate.class)
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

			b.append("unidentifiedPluralName=");
			b.append(obj.getUnidentifiedPluralName());
			b.append(V1Utils.NEWLINE);

			b.append("race=");
			b.append(obj.getRace()==null?"":obj.getRace().getName());
			b.append(V1Utils.NEWLINE);

			b.append("class=");
			b.append(obj.getCharacterClass()==null?"":obj.getCharacterClass().getName());
			b.append(V1Utils.NEWLINE);

			b.append("types=");
			b.append(foeTypes.toString(obj.getTypes()));
			b.append(V1Utils.NEWLINE);

			b.append("hitPointsRange=");
			b.append(V1Dice.toString(obj.getHitPointsRange()));
			b.append(V1Utils.NEWLINE);

			b.append("actionPointsRange=");
			b.append(V1Dice.toString(obj.getActionPointsRange()));
			b.append(V1Utils.NEWLINE);

			b.append("magicPointsRange=");
			b.append(V1Dice.toString(obj.getMagicPointsRange()));
			b.append(V1Utils.NEWLINE);

			b.append("levelRange=");
			b.append(V1Dice.toString(obj.getLevelRange()));
			b.append(V1Utils.NEWLINE);

			b.append("experience=");
			b.append(obj.getExperience());
			b.append(V1Utils.NEWLINE);

			b.append("stats=");
			b.append(V1StatModifier.toString(obj.getStats()));
			b.append(V1Utils.NEWLINE);

			b.append("bodyParts=");
			b.append(V1Race.percTable.toString(obj.getBodyParts()));
			b.append(V1Utils.NEWLINE);

			b.append("playerBodyParts=");
			b.append(playerBodyParts.toString(obj.getPlayerBodyParts()));
			b.append(V1Utils.NEWLINE);

			b.append("baseTexture=");
			b.append(obj.getBaseTexture().getName());
			b.append(V1Utils.NEWLINE);

			b.append("meleeAttackTexture=");
			b.append(obj.getMeleeAttackTexture().getName());
			b.append(V1Utils.NEWLINE);

			b.append("rangedAttackTexture=");
			b.append(obj.getRangedAttackTexture().getName());
			b.append(V1Utils.NEWLINE);

			b.append("castSpellTexture=");
			b.append(obj.getCastSpellTexture().getName());
			b.append(V1Utils.NEWLINE);

			b.append("specialAbilityTexture=");
			b.append(obj.getSpecialAbilityTexture().getName());
			b.append(V1Utils.NEWLINE);

			b.append("loot=");
			b.append(obj.getLoot().getName());
			b.append(V1Utils.NEWLINE);

			b.append("evasionBehaviour=");
			b.append(obj.getEvasionBehaviour());
			b.append(V1Utils.NEWLINE);

			b.append("cannotBeEvaded=");
			b.append(obj.cannotBeEvaded());
			b.append(V1Utils.NEWLINE);

			b.append("identificationDifficulty=");
			b.append(obj.getIdentificationDifficulty());
			b.append(V1Utils.NEWLINE);

			b.append("foeGroupBannerModifiers=");
			b.append(V1StatModifier.toString(obj.getFoeGroupBannerModifiers()));
			b.append(V1Utils.NEWLINE);

			b.append("allFoesBannerModifiers=");
			b.append(V1StatModifier.toString(obj.getAllFoesBannerModifiers()));
			b.append(V1Utils.NEWLINE);

			b.append("fleeChance=");
			b.append(obj.getFleeChance());
			b.append(V1Utils.NEWLINE);

			b.append("stealthBehaviour=");
			b.append(obj.getStealthBehaviour());
			b.append(V1Utils.NEWLINE);

			b.append("faction=");
			String faction = obj.getFaction()==null || obj.getFaction().equals("") ?
				"" : obj.getFaction();
			b.append(faction);
			b.append(V1Utils.NEWLINE);

			b.append("isNpc=");
			b.append(obj.isNpc());
			b.append(V1Utils.NEWLINE);
			
			b.append("appearanceScript=");
			b.append(obj.getAppearanceScript()==null?"":obj.getAppearanceScript().getName());
			b.append(V1Utils.NEWLINE);

			b.append("deathScript=");
			b.append(obj.getDeathScript()==null?"":obj.getDeathScript().getName());
			b.append(V1Utils.NEWLINE);

			b.append("naturalWeapons=");
			b.append(V1Utils.stringList.toString(obj.getNaturalWeapons()));
			b.append(V1Utils.NEWLINE);

			b.append("spellBook=");
			b.append(V1SpellBook.toString(obj.getSpellBook()));
			b.append(V1Utils.NEWLINE);

			b.append("spellLikeAbilities=");
			b.append(spellLikeAbilities.toString(obj.getSpellLikeAbilities()));
			b.append(V1Utils.NEWLINE);

			b.append("focus=");
			b.append(obj.getFocus());
			b.append(V1Utils.NEWLINE);

			b.append("attitude=");
			b.append(obj.getDefaultAttitude());
			b.append(V1Utils.NEWLINE);
		}

		return b.toString();
	}

	/*-------------------------------------------------------------------------*/
	static FoeTemplate fromProperties(Properties p) throws Exception
	{
		if (p.getProperty("impl") != null)
		{
			// custom FoeTemplate impl
			Class clazz = Class.forName(p.getProperty("impl"));
			return (FoeTemplate)clazz.newInstance();
		}
		else
		{
			String name = p.getProperty("name");
			String pluralName = p.getProperty("pluralName");
			String unidentifiedName = p.getProperty("unidentifiedName");
			String unidentifiedPluralName = p.getProperty("unidentifiedPluralName");
			List<FoeType> types = foeTypes.fromString(p.getProperty("types"));
			Race race = null;
			if (!"".equals(p.getProperty("race")))
			{
				race = Database.getInstance().getRace(p.getProperty("race"));
			}
			CharacterClass characterClass = null;
			if (!"".equals(p.getProperty("class")))
			{
				characterClass = Database.getInstance().getCharacterClass(p.getProperty("class"));
			}
			Dice hitPointsRange = V1Dice.fromString(p.getProperty("hitPointsRange"));
			Dice action = V1Dice.fromString(p.getProperty("actionPointsRange"));
			Dice magicPointsRange = V1Dice.fromString(p.getProperty("magicPointsRange"));
			Dice levelRange = V1Dice.fromString(p.getProperty("levelRange"));
			int experience = Integer.parseInt(p.getProperty("experience"));
			StatModifier stats = V1StatModifier.fromString(p.getProperty("stats"));
			PercentageTable<BodyPart> bodyParts = V1Race.percTable.fromString(p.getProperty("bodyParts"));
			PercentageTable<String> playerBodyParts = V1FoeTemplate.playerBodyParts.fromString(p.getProperty("playerBodyParts"));
			MazeTexture baseTexture = Database.getInstance().getMazeTexture(p.getProperty("baseTexture"));
			MazeTexture meleeAttackTexture = Database.getInstance().getMazeTexture(p.getProperty("meleeAttackTexture"));
			MazeTexture rangedAttackTexture = Database.getInstance().getMazeTexture(p.getProperty("rangedAttackTexture"));
			MazeTexture castSpellTexture = Database.getInstance().getMazeTexture(p.getProperty("castSpellTexture"));
			MazeTexture specialAbilityTexture = Database.getInstance().getMazeTexture(p.getProperty("specialAbilityTexture"));
			LootTable loot = Database.getInstance().getLootTable(p.getProperty("loot"));
			int evasionBehaviour = Integer.parseInt(p.getProperty("evasionBehaviour"));
			boolean cannotBeEvaded = Boolean.valueOf(p.getProperty("cannotBeEvaded"));
			int identificationDifficulty = Integer.parseInt(p.getProperty("identificationDifficulty"));
			StatModifier foeGroupBannerModifiers = V1StatModifier.fromString(p.getProperty("foeGroupBannerModifiers"));
			StatModifier allFoesBannerModifiers = V1StatModifier.fromString(p.getProperty("allFoesBannerModifiers"));
			int fleeChance = Integer.parseInt(p.getProperty("fleeChance"));
			int stealthBehaviour = Integer.parseInt(p.getProperty("stealthBehaviour"));
			String faction = p.getProperty("faction").equals("")?null:p.getProperty("faction");
			boolean isNpc = Boolean.valueOf(p.getProperty("isNpc"));
			String scriptName = p.getProperty("appearanceScript");
			MazeScript appearanceScript;
			if (scriptName == null || scriptName.length() == 0)
			{
				appearanceScript = null;
			}
			else
			{
				appearanceScript = Database.getInstance().getMazeScript(scriptName);
			}

			scriptName = p.getProperty("deathScript");
			MazeScript deathScript;
			if (scriptName == null || scriptName.length() == 0)
			{
				deathScript = null;
			}
			else
			{
				deathScript = Database.getInstance().getMazeScript(scriptName);
			}

			List<String> naturalWeapons = V1Utils.stringList.fromString(p.getProperty("naturalWeapons"));
			SpellBook spellbook = V1SpellBook.fromString(p.getProperty("spellBook"));
			List<SpellLikeAbility> spellLikeAbilityList = spellLikeAbilities.fromString(
				p.getProperty("spellLikeAbilities"));

			CharacterClass.Focus focus = CharacterClass.Focus.valueOf(p.getProperty("focus"));
			NpcFaction.Attitude attitude = NpcFaction.Attitude.valueOf(p.getProperty("attitude"));

			return new FoeTemplate(
				name,
				pluralName,
				unidentifiedName,
				unidentifiedPluralName,
				types,
				race,
				characterClass,
				hitPointsRange,
				action,
				magicPointsRange,
				levelRange,
				experience,
				stats,
				bodyParts,
				playerBodyParts,
				baseTexture,
				meleeAttackTexture,
				rangedAttackTexture,
				castSpellTexture,
				specialAbilityTexture,
				loot,
				evasionBehaviour,
				cannotBeEvaded,
				identificationDifficulty,
				foeGroupBannerModifiers,
				allFoesBannerModifiers,
				fleeChance,
				stealthBehaviour,
				faction,
				isNpc,
				appearanceScript,
				deathScript,
				naturalWeapons,
				spellbook,
				spellLikeAbilityList,
				focus,
				attitude);
		}
	}
}
