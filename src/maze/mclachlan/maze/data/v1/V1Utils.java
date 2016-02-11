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

import java.util.Properties;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.ByteArrayInputStream;

/**
 *
 */
public class V1Utils
{
	public static String NEWLINE = "\r\n";
	public static final String GENDERS = "genders.txt";
	public static final String RACES = "races.txt";
	public static final String BODY_PARTS = "bodyparts.txt";
	public static final String EXPERIENCE_TABLES = "experiencetables.txt";
	public static final String CHARACTER_CLASSES = "characterclasses.txt";
	public static final String ATTACK_TYPES = "attacktypes.txt";
	public static final String CONDITION_EFFECTS = "conditioneffects.txt";
	public static final String CONDITION_TEMPLATES = "conditiontemplates.txt";
	public static final String SPELL_EFFECTS = "spelleffects.txt";
	public static final String LOOT_ENTRIES = "lootentries.txt";
	public static final String LOOT_TABLES = "loottables.txt";
	public static final String SPELLS = "spells.txt";
	public static final String PLAYER_SPELL_BOOKS = "playerspellbooks.txt";
	public static final String MAZE_TEXTURES = "textures.txt";
	public static final String MAZE_SCRIPTS = "scripts.txt";
	public static final String FOE_ATTACKS = "foeattacks.txt";
	public static final String FOE_TEMPLATES = "foetemplates.txt";
	public static final String TRAPS = "traps.txt";
	public static final String FOE_ENTRIES = "foeentries.txt";
	public static final String ENCOUNTER_TABLES = "encountertables.txt";
	public static final String NPC_FACTION_TEMPLATES = "npcfactiontemplates.txt";
	public static final String NPC_TEMPLATES = "npctemplates.txt";
	public static final String WIELDING_COMBOS = "wieldingcombos.txt";
	public static final String ITEM_TEMPLATES = "itemtemplates.txt";
	public static final String CRAFT_RECIPES = "craftrecipes.txt";
	public static final String ITEM_ENCHANTMENTS = "itemenchantments.txt";
	public static final String NATURAL_WEAPONS = "naturalweapons.txt";
	public static final String FOE_TYPES = "foetypes.txt";
	public static final String PERSONALITIES = "personalities.txt";
	public static final String STARTING_KITS = "startingkits.txt";
	public static final String ZONES = "zones/";
	public static final String DIFFICULTY_LEVELS = "difficultylevels.txt";
	public static final String CHARACTER_GUILD = "guild.txt";

	public static final String GAME_STATE = "gamestate.txt";
	public static final String PLAYER_CHARACTERS = "playercharacters.txt";
	public static final String NPCS = "npcs.txt";
	public static final String NPC_FACTIONS = "npcfactions.txt";
	public static final String MAZE_VARIABLES = "mazevariables.txt";
	public static final String ITEM_CACHES = "itemcaches.txt";
	public static final String TILES_VISITED = "tilesvisited.txt";
	public static final String CONDITIONS = "conditions.txt";
	public static final String JOURNALS = "journals/";

	public static final String USER_CONFIG = "user.cfg";

	/*-------------------------------------------------------------------------*/
	public static V1List<String> stringList = new V1List<String>()
	{
		public String typeToString(String s)
		{
			return s;
		}

		public String typeFromString(String s)
		{
			return s;
		}
	};

	/*-------------------------------------------------------------------------*/
	public static Properties getProperties(BufferedReader reader)
		throws IOException
	{
		String line;
		StringBuilder s = new StringBuilder();
		line = reader.readLine();
		while (line != null && !line.equals("@"))
		{
			s.append(line).append("\n");
			line = reader.readLine();
		}

		Properties p = new Properties();
		ByteArrayInputStream inStream = new ByteArrayInputStream(s.toString().getBytes());
		p.load(inStream);
		inStream.close();

		return p;
	}

	/*-------------------------------------------------------------------------*/
	public static String escapeNewlines(String str)
	{
		return str.replaceAll("\n", "%n");
	}

	/*-------------------------------------------------------------------------*/
	public static String replaceNewlines(String str)
	{
		return str.replaceAll("%n", "\n");
	}

	/*-------------------------------------------------------------------------*/
	public static String escapeCommas(String str)
	{
		return str.replaceAll(",", "%c");
	}

	/*-------------------------------------------------------------------------*/
	public static String replaceCommas(String str)
	{
		return str.replaceAll("%c", ",");
	}

	/*-------------------------------------------------------------------------*/
	public static String escapeNewlineaAndCommas(String str)
	{
		return escapeCommas(escapeNewlines(str));
	}

	/*-------------------------------------------------------------------------*/
	public static String replaceNewlineaAndCommas(String str)
	{
		return replaceCommas(replaceNewlines(str));
	}

	/*-------------------------------------------------------------------------*/
	public static String toStringInts(int[] arr, String separator)
	{
		if (arr == null)
		{
			return "";
		}

		StringBuilder s = new StringBuilder();
		for (int i = 0; i < arr.length; i++)
		{
			s.append(arr[i]);
			if (i < arr.length-1)
			{
				s.append(separator);
			}
		}

		return s.toString();
	}

	/*-------------------------------------------------------------------------*/
	public static int[] fromStringInts(String s, String separator)
	{
		if (s.equals(""))
		{
			return null;
		}

		String[] strs = s.split(separator);
		int[] result = new int[strs.length];

		for (int i = 0; i < strs.length; i++)
		{
			result[i] = Integer.parseInt(strs[i]);
		}

		return result;
	}

	/*-------------------------------------------------------------------------*/
	public static String toStringStrings(String[] arr, String separator)
	{
		if (arr == null)
		{
			return "";
		}

		StringBuilder s = new StringBuilder();
		for (int i = 0; i < arr.length; i++)
		{
			s.append(arr[i]);
			if (i < arr.length-1)
			{
				s.append(separator);
			}
		}

		return s.toString();
	}

	/*-------------------------------------------------------------------------*/
	public static String[] fromStringStrings(String s, String separator)
	{
		if (s.equals(""))
		{
			return null;
		}
		
		return s.split(separator);
	}
}
