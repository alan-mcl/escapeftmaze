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

import java.util.HashMap;
import java.util.Map;
import java.util.BitSet;
import mclachlan.maze.data.Database;
import mclachlan.maze.map.TileScript;
import mclachlan.maze.map.Trap;
import mclachlan.maze.map.HiddenStuff;
import mclachlan.maze.map.script.RemoveWall;
import mclachlan.maze.map.script.*;
import mclachlan.maze.stat.npc.NpcFaction;
import mclachlan.maze.util.MazeException;
import mclachlan.maze.stat.PercentageTable;
import mclachlan.maze.game.MazeScript;

/**
 *
 */
public class V1TileScript
{
	static final String SEP = ",";
	static final String SUB_SEP = "/";
	static Map<Class, Integer> types;

	private static final int CUSTOM = 0;
	private static final int CAST_SPELL = 1;
	private static final int CHEST = 2;
	private static final int ENCOUNTER = 3;
	private static final int FLAVOUR_TEXT = 4;
	private static final int LOOT = 5;
	private static final int REMOVE_WALL = 6;
	private static final int EXECUTE_MAZE_SCRIPT = 7;
	private static final int SIGNBOARD = 8;
	private static final int SET_MAZE_VARIABLE = 9;
	private static final int HIDDEN_STUFF = 10;
	private static final int WATER = 11;

	static V1PercentageTable<Trap> traps = new V1PercentageTable<Trap>()
	{
		public Trap typeFromString(String s)
		{
			return Database.getInstance().getTrap(s);
		}

		public String typeToString(Trap trap)
		{
			return trap.getName();
		}
	};

	static
	{
		types = new HashMap<Class, Integer>();

		types.put(CastSpell.class, CAST_SPELL);
		types.put(Chest.class, CHEST);
		types.put(Encounter.class, ENCOUNTER);
		types.put(FlavourText.class, FLAVOUR_TEXT);
		types.put(Loot.class, LOOT);
		types.put(RemoveWall.class, REMOVE_WALL);
		types.put(ExecuteMazeScript.class, EXECUTE_MAZE_SCRIPT);
		types.put(SignBoard.class, SIGNBOARD);
		types.put(SetMazeVariable.class, SET_MAZE_VARIABLE);
		types.put(HiddenStuff.class, HIDDEN_STUFF);
		types.put(Water.class, WATER);
	}

	/*-------------------------------------------------------------------------*/
	public static String toString(TileScript t)
	{
		if (t == null)
		{
			return "";
		}

		StringBuilder s = new StringBuilder();

		int type;
		if (types.containsKey(t.getClass()))
		{
			type = types.get(t.getClass());
		}
		else
		{
			type = CUSTOM;
		}

		s.append(type);
		s.append(SEP);
		s.append(t.getExecuteOnceMazeVariable()==null?"":t.getExecuteOnceMazeVariable());
		s.append(SEP);
		s.append(V1BitSet.toString(t.getFacings()));
		s.append(SEP);
		s.append(t.isReexecuteOnSameTile());
		s.append(SEP);

		switch (type)
		{
			case CUSTOM:
				s.append(t.getClass().getName());
				break;
			case CAST_SPELL:
				CastSpell cs = (CastSpell)t;
				s.append(cs.getSpellName());
				s.append(SEP);
				s.append(cs.getCastingLevel());
				s.append(SEP);
				s.append(cs.getCasterLevel());
				break;
			case CHEST:
				Chest c = (Chest)t;
				String chestContents = toString(c.getChestContents());
				chestContents = chestContents.replaceAll(SEP, SUB_SEP);
				s.append(chestContents);
				s.append(SEP);
				s.append(traps.toString(c.getTraps(), "`", "~"));
				s.append(SEP);
				s.append(c.getMazeVariable());
				s.append(SEP);
				s.append(c.getNorthTexture());
				s.append(SEP);
				s.append(c.getSouthTexture());
				s.append(SEP);
				s.append(c.getEastTexture());
				s.append(SEP);
				s.append(c.getWestTexture());
				s.append(SEP);
				s.append(c.getPreScript()==null?"":c.getPreScript().getName());
				break;
			case ENCOUNTER:
				Encounter e = (Encounter)t;
				s.append(e.getEncounterTable().getName());
				s.append(SEP);
				s.append(e.getMazeVariable()==null?"":e.getMazeVariable());
				s.append(SEP);
				s.append(e.getAttitude()==null?"":e.getAttitude().toString());
				break;
			case FLAVOUR_TEXT:
				FlavourText ft = (FlavourText)t;
				s.append(V1Utils.escapeNewlines(ft.getText()));
				break;
			case LOOT:
				Loot l = (Loot)t;
				s.append(l.getLootTable());
				break;
			case REMOVE_WALL:
				RemoveWall r = (RemoveWall)t;
				s.append(r.getMazeVariable());
				s.append(SEP);
				s.append(r.getWallIndex());
				s.append(SEP);
				s.append(r.isHorizontalWall());
				break;
			case EXECUTE_MAZE_SCRIPT:
				ExecuteMazeScript eme = (ExecuteMazeScript)t;
				s.append(eme.getScript());
				break;
			case SIGNBOARD:
				SignBoard sb = (SignBoard)t;
				s.append(V1Utils.escapeNewlines(sb.getText()));
				break;
			case SET_MAZE_VARIABLE:
				SetMazeVariable smv = (SetMazeVariable)t;
				s.append(smv.getMazeVariable());
				s.append(SEP);
				s.append(smv.getValue());
				break;
			case HIDDEN_STUFF:
				HiddenStuff hs = (HiddenStuff)t;
				s.append(hs.getMazeVariable()==null?"":hs.getMazeVariable());
				s.append(SEP);
				s.append(hs.getContent().getName());
				s.append(SEP);
				s.append(hs.getPreScript()== null?"":hs.getPreScript().getName());
				break;
			case WATER:
				break;

			default: throw new MazeException("Invalid type "+type);
		}

		return s.toString();
	}

	/*-------------------------------------------------------------------------*/
	public static TileScript fromString(String s)
	{
		if (s.equals(""))
		{
			return null;
		}

		// since hierarchy doesn't matter, treat it as flat
		String[] strs = s.split(SEP,-1);

		int i = 0;
		int type = Integer.parseInt(strs[i++]);
		String ss = strs[i++];
		String executeOnceMazeVariable = "".equals(ss)?null:ss;
		BitSet facings = V1BitSet.fromString(strs[i++]);
		boolean reexecuteOnSameTile = Boolean.valueOf(strs[i++]);
		
		TileScript result;

		switch (type)
		{
			case CUSTOM:
				try
				{
					Class clazz = Class.forName(strs[i++]);
					result = (TileScript)clazz.newInstance();
					break;
				}
				catch (Exception e)
				{
					throw new MazeException(e);
				}
			case CAST_SPELL:
				String spellName = strs[i++];
				int castingLevel = Integer.parseInt(strs[i++]);
				int casterLevel = Integer.parseInt(strs[i++]);
				result = new CastSpell(spellName, castingLevel, casterLevel);
				break;
			case CHEST:
				String contents = strs[i++].replaceAll(SUB_SEP, SEP);
				TileScript chestContents = fromString(contents);
				PercentageTable<Trap> t = traps.fromString(strs[i++], "`", "~");
				String mazeVariable = strs[i++];
				String northTexture = strs[i++];
				String southTexture = strs[i++];
				String eastTexture = strs[i++];
				String westTexture = strs[i++];
				MazeScript script = ("".equals(strs[i])) ? null : Database.getInstance().getScript(strs[i++]);
				result = new Chest(
					chestContents, 
					t, 
					mazeVariable, 
					northTexture,
					southTexture,
					eastTexture, 
					westTexture, 
					script);
				break;
			case ENCOUNTER:
				String encTable = strs[i++];
				String encMazVar = strs[i++];
				String str = strs[i++];
				NpcFaction.Attitude attitude = null;
				if ("".equals(str))
				{
					attitude = null;
				}
				else
				{
					attitude = NpcFaction.Attitude.valueOf(str);
				}
				result = new Encounter(
					Database.getInstance().getEncounterTable(encTable),
					encMazVar,
					attitude);
				break;
			case FLAVOUR_TEXT:
				// hack alert.  Any commas will have been split above.  Replace them
				StringBuilder sb = new StringBuilder();
				for (int ii=i; ii<strs.length; ii++)
				{
					sb.append(strs[ii]).append(',');
				}
				String text = V1Utils.replaceNewlines(sb.substring(0, sb.length()-1));
				result = new FlavourText(text);
				break;
			case LOOT:
				result = new Loot(strs[i++]);
				break;
			case REMOVE_WALL:
				result = new RemoveWall(strs[i++], Integer.parseInt(strs[i++]), Boolean.valueOf(strs[i++]));
				break;
			case EXECUTE_MAZE_SCRIPT:
				result = new ExecuteMazeScript(strs[i++]);
				break;
			case SIGNBOARD:
				result = new SignBoard(V1Utils.replaceNewlines(strs[i++]));
				break;
			case SET_MAZE_VARIABLE:
				result = new SetMazeVariable(strs[i++], strs[i++]);
				break;
			case HIDDEN_STUFF:
				String mazeVar = strs[i++];
				String contentStr = strs[i++];
				String preStr = strs[i++];
				MazeScript content = ("".equals(contentStr)) ? null : Database.getInstance().getScript(contentStr);
				MazeScript preScript = ("".equals(preStr)) ? null : Database.getInstance().getScript(preStr);
				result = new HiddenStuff(content, preScript, mazeVar);
				break;
			case WATER:
				result = new Water();
				break;
			default: throw new MazeException("Invalid type "+type+" ["+s+"]");
		}
		
		result.setExecuteOnceMazeVariable(executeOnceMazeVariable);
		result.setFacings(facings);
		result.setReexecuteOnSameTile(reexecuteOnSameTile);
		
		return result;
	}
}
