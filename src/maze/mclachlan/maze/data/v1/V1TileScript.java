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
import mclachlan.maze.game.MazeScript;
import mclachlan.maze.map.HiddenStuff;
import mclachlan.maze.map.TileScript;
import mclachlan.maze.map.Trap;
import mclachlan.maze.map.script.*;
import mclachlan.maze.stat.PercentageTable;
import mclachlan.maze.stat.combat.Combat;
import mclachlan.maze.stat.npc.NpcFaction;
import mclachlan.maze.util.MazeException;

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
	private static final int LEVER = 12;
	private static final int TOGGLE_WALL = 13;
	private static final int PERSONALITY_SPEECH = 14;
	private static final int DISPLAY_OPTIONS = 15;

	static V1PercentageTable<Trap> traps = new V1PercentageTable<>()
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

	static V1List<String> optionsList = new V1List<>("#")
	{
		@Override
		public String typeToString(String s)
		{
			return s;
		}

		@Override
		public String typeFromString(String s)
		{
			return s;
		}
	};

	static
	{
		types = new HashMap<>();

		types.put(CastSpell.class, CAST_SPELL);
		types.put(Chest.class, CHEST);
		types.put(Encounter.class, ENCOUNTER);
		types.put(FlavourText.class, FLAVOUR_TEXT);
		types.put(PersonalitySpeech.class, PERSONALITY_SPEECH);
		types.put(DisplayOptions.class, DISPLAY_OPTIONS);
		types.put(Loot.class, LOOT);
		types.put(RemoveWall.class, REMOVE_WALL);
		types.put(ExecuteMazeScript.class, EXECUTE_MAZE_SCRIPT);
		types.put(SignBoard.class, SIGNBOARD);
		types.put(SetMazeVariable.class, SET_MAZE_VARIABLE);
		types.put(HiddenStuff.class, HIDDEN_STUFF);
		types.put(Water.class, WATER);
		types.put(Lever.class, LEVER);
		types.put(ToggleWall.class, TOGGLE_WALL);
	}

	/*-------------------------------------------------------------------------*/
	public static String toString(TileScript t)
	{
		return toString(t, SEP);
	}

	/*-------------------------------------------------------------------------*/
	public static String toString(TileScript t, String sep)
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
		s.append(sep);
		s.append(t.getExecuteOnceMazeVariable()==null?"":t.getExecuteOnceMazeVariable());
		s.append(sep);
		s.append(V1BitSet.toString(t.getFacings()));
		s.append(sep);
		s.append(t.isReexecuteOnSameTile());
		s.append(sep);
		s.append(t.getScoutSecretDifficulty());
		s.append(sep);

		switch (type)
		{
			case CUSTOM:
				s.append(t.getClass().getName());
				break;
			case CAST_SPELL:
				CastSpell cs = (CastSpell)t;
				s.append(cs.getSpellName());
				s.append(sep);
				s.append(cs.getCastingLevel());
				s.append(sep);
				s.append(cs.getCasterLevel());
				break;
			case CHEST:
				Chest c = (Chest)t;
				String chestContents = toString(c.getChestContents());
				chestContents = chestContents.replaceAll(sep, SUB_SEP);
				s.append(chestContents);
				s.append(sep);
				s.append(traps.toString(c.getTraps(), "`", "~"));
				s.append(sep);
				s.append(c.getMazeVariable());
				s.append(sep);
				s.append(c.getNorthTexture());
				s.append(sep);
				s.append(c.getSouthTexture());
				s.append(sep);
				s.append(c.getEastTexture());
				s.append(sep);
				s.append(c.getWestTexture());
				s.append(sep);
				s.append(c.getPreScript()==null?"":c.getPreScript().getName());
				break;
			case LEVER:
				Lever lever = (Lever)t;
				s.append(lever.getMazeVariable());
				s.append(sep);
				s.append(lever.getNorthTexture());
				s.append(sep);
				s.append(lever.getSouthTexture());
				s.append(sep);
				s.append(lever.getEastTexture());
				s.append(sep);
				s.append(lever.getWestTexture());
				s.append(sep);
				s.append(lever.getPreTransitionScript()==null?"":lever.getPreTransitionScript().getName());
				s.append(sep);
				s.append(lever.getPostTransitionScript()==null?"":lever.getPostTransitionScript().getName());
				break;
			case ENCOUNTER:
				Encounter e = (Encounter)t;
				s.append(e.getEncounterTable().getName());
				s.append(sep);
				s.append(e.getMazeVariable()==null?"":e.getMazeVariable());
				s.append(sep);
				s.append(e.getAttitude()==null?"":e.getAttitude().toString());
				s.append(sep);
				s.append(e.getAmbushStatus()==null?"":e.getAmbushStatus().toString());
				s.append(sep);
				s.append(e.getPreScript()==null?"":e.getPreScript());
				s.append(sep);
				s.append(e.getPostAppearanceScript()==null?"":e.getPostAppearanceScript());
				break;
			case FLAVOUR_TEXT:
				FlavourText ft = (FlavourText)t;
				s.append(V1Utils.escapeNewlines(ft.getText()));
				break;
			case PERSONALITY_SPEECH:
				PersonalitySpeech ps = (PersonalitySpeech)t;
				s.append(ps.getSpeechKey());
				s.append(sep);
				s.append(ps.isModal());
				break;
			case DISPLAY_OPTIONS:
				DisplayOptions dop = (DisplayOptions)t;
				s.append(dop.isForceSelection());
				s.append(sep);
				s.append(dop.getTitle());
				s.append(sep);
				s.append(optionsList.toString(dop.getOptions()));
				s.append(sep);
				s.append(optionsList.toString(dop.getScripts()));
				break;
			case LOOT:
				Loot l = (Loot)t;
				s.append(l.getLootTable());
				break;
			case REMOVE_WALL:
				RemoveWall r = (RemoveWall)t;
				s.append(r.getMazeVariable());
				s.append(sep);
				s.append(r.getWallIndex());
				s.append(sep);
				s.append(r.isHorizontalWall());
				break;
			case TOGGLE_WALL:
				ToggleWall tw = (ToggleWall)t;
				s.append(tw.getMazeVariable());
				s.append(sep);
				s.append(tw.getWallIndex());
				s.append(sep);
				s.append(tw.isHorizontalWall());
				s.append(sep);
				
				s.append(tw.getState1Texture()==null?"":tw.getState1Texture().getName());
				s.append(sep);
				s.append(tw.getState1MaskTexture()==null?"":tw.getState1MaskTexture().getName());
				s.append(sep);
				s.append(tw.isState1Visible());
				s.append(sep);
				s.append(tw.isState1Solid());
				s.append(sep);
				s.append(tw.isState1Secret());
				s.append(sep);
				s.append(tw.getState1Height());
				s.append(sep);

				s.append(tw.getState2Texture()==null?"":tw.getState2Texture().getName());
				s.append(sep);
				s.append(tw.getState2MaskTexture()==null?"":tw.getState2MaskTexture().getName());
				s.append(sep);
				s.append(tw.isState2Visible());
				s.append(sep);
				s.append(tw.isState2Solid());
				s.append(sep);
				s.append(tw.isState2Secret());
				s.append(sep);
				s.append(tw.getState2Height());
				s.append(sep);

				s.append(tw.getPreToggleScript()==null?"":tw.getPreToggleScript());
				s.append(sep);
				s.append(tw.getPostToggleScript()==null?"":tw.getPostToggleScript());

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
				s.append(sep);
				s.append(smv.getValue());
				break;
			case HIDDEN_STUFF:
				HiddenStuff hs = (HiddenStuff)t;
				s.append(hs.getMazeVariable()==null?"":hs.getMazeVariable());
				s.append(sep);
				s.append(hs.getContent().getName());
				s.append(sep);
				s.append(hs.getPreScript()== null?"":hs.getPreScript().getName());
				s.append(sep);
				s.append(hs.getFindDifficulty());
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
		return fromString(s, SEP);
	}

	/*-------------------------------------------------------------------------*/
	public static TileScript fromString(String s, String sep)
	{
		if (s.equals(""))
		{
			return null;
		}

		// since hierarchy doesn't matter, treat it as flat
		String[] strs = s.split(sep,-1);

		int i = 0;
		int type = Integer.parseInt(strs[i++]);
		String ss = strs[i++];
		String executeOnceMazeVariable = "".equals(ss)?null:ss;
		BitSet facings = V1BitSet.fromString(strs[i++]);
		boolean reexecuteOnSameTile = Boolean.valueOf(strs[i++]);
		int scoutSecretDifficulty = Integer.parseInt(strs[i++]);

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
				String contents = strs[i++].replaceAll(SUB_SEP, sep);
				TileScript chestContents = fromString(contents);
				PercentageTable<Trap> t = traps.fromString(strs[i++], "`", "~");
				String mazeVariable = strs[i++];
				String northTexture = strs[i++];
				String southTexture = strs[i++];
				String eastTexture = strs[i++];
				String westTexture = strs[i++];
				MazeScript script = ("".equals(strs[i])) ? null : Database.getInstance().getMazeScript(strs[i]);
				i++;
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
			case LEVER:
				String mazeVariableL = strs[i++];
				String northTextureL = strs[i++];
				String southTextureL = strs[i++];
				String eastTextureL = strs[i++];
				String westTextureL = strs[i++];
				MazeScript preTransScript = ("".equals(strs[i])) ? null : Database.getInstance().getMazeScript(strs[i]);
				i++;
				MazeScript postTransScript = ("".equals(strs[i])) ? null : Database.getInstance().getMazeScript(strs[i]);
				i++;
				result = new Lever(
					northTextureL,
					southTextureL,
					eastTextureL,
					westTextureL,
					mazeVariableL,
					preTransScript,
					postTransScript);
				break;
			case ENCOUNTER:
				String encTable = strs[i++];
				String encMazVar = strs[i++];
				String str = strs[i++];
				NpcFaction.Attitude attitude = "".equals(str) ? null : NpcFaction.Attitude.valueOf(str);
				str = strs[i++];
				Combat.AmbushStatus ambushStatus = "".equals(str) ? null : Combat.AmbushStatus.valueOf(str);
				str = strs[i++];
				String preEncounterScript = "".equals(str) ? null : str;
				str = strs[i++];
				String postAppearanceScript = "".equals(str) ? null : str;

				result = new Encounter(
					Database.getInstance().getEncounterTable(encTable),
					encMazVar,
					attitude,
					ambushStatus,
					preEncounterScript,
					postAppearanceScript);
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
			case PERSONALITY_SPEECH:
				result = new PersonalitySpeech(strs[i++], Boolean.valueOf(strs[i++]));
				break;
			case DISPLAY_OPTIONS:
				boolean forceSelection = Boolean.valueOf(strs[i++]);
				String title = strs[i++];
				List<String> options = optionsList.fromString(strs[i++]);
				List<String> scripts = optionsList.fromString(strs[i++]);
				result = new DisplayOptions(forceSelection, title, options, scripts);
				break;
			case LOOT:
				result = new Loot(strs[i++]);
				break;
			case REMOVE_WALL:
				result = new RemoveWall(strs[i++], Integer.parseInt(strs[i++]), Boolean.valueOf(strs[i++]));
				break;
			case TOGGLE_WALL:
				String toggleWallMazeVar = strs[i++];
				int wallIndex = Integer.parseInt(strs[i++]);
				boolean horizontalWall = Boolean.valueOf(strs[i++]);
				
				String state1Texture = strs[i++]; 
				String state1MaskTexture = strs[i++];
				boolean state1Visible = Boolean.valueOf(strs[i++]);
				boolean state1Solid = Boolean.valueOf(strs[i++]);
				boolean state1Secret = Boolean.valueOf(strs[i++]);
				int state1Height = Integer.parseInt(strs[i++]);

				String state2Texture = strs[i++]; 
				String state2MaskTexture = strs[i++];
				boolean state2Visible = Boolean.valueOf(strs[i++]);
				boolean state2Solid = Boolean.valueOf(strs[i++]);
				boolean state2Secret = Boolean.valueOf(strs[i++]);
				int state2Height = Integer.parseInt(strs[i++]);

				String preToggleScript = strs[i++];
				String postToggleScript = strs[i++];

				result = new ToggleWall(
					toggleWallMazeVar,
					wallIndex,
					horizontalWall,
					"".equals(state1Texture)?null:Database.getInstance().getMazeTexture(state1Texture).getTexture(),
					"".equals(state1MaskTexture)?null:Database.getInstance().getMazeTexture(state1MaskTexture).getTexture(),
					state1Visible,
					state1Solid,
					state1Secret,
					state1Height,
					"".equals(state2Texture)?null:Database.getInstance().getMazeTexture(state2Texture).getTexture(),
					"".equals(state2MaskTexture)?null:Database.getInstance().getMazeTexture(state2MaskTexture).getTexture(),
					state2Visible,
					state2Solid,
					state2Secret,
					state2Height,
					"".equals(preToggleScript)?null:preToggleScript,
					"".equals(postToggleScript)?null:postToggleScript);
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
				int findDifficulty = Integer.parseInt(strs[i++]);
				MazeScript content = ("".equals(contentStr)) ? null : Database.getInstance().getMazeScript(contentStr);
				MazeScript preScript = ("".equals(preStr)) ? null : Database.getInstance().getMazeScript(preStr);
				result = new HiddenStuff(content, preScript, mazeVar, findDifficulty);
				break;
			case WATER:
				result = new Water();
				break;
			default: throw new MazeException("Invalid type "+type+" ["+s+"]");
		}
		
		result.setExecuteOnceMazeVariable(executeOnceMazeVariable);
		result.setFacings(facings);
		result.setReexecuteOnSameTile(reexecuteOnSameTile);
		result.setScoutSecretDifficulty(scoutSecretDifficulty);
		
		return result;
	}
}
