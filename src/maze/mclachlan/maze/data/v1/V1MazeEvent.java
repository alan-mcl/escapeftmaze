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

import java.awt.Point;
import java.util.*;
import mclachlan.maze.data.Database;
import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.game.event.*;
import mclachlan.maze.game.journal.JournalEntryEvent;
import mclachlan.maze.map.script.*;
import mclachlan.maze.stat.Stats;
import mclachlan.maze.stat.combat.Combat;
import mclachlan.maze.stat.combat.event.*;
import mclachlan.maze.stat.magic.ValueList;
import mclachlan.maze.stat.npc.*;
import mclachlan.maze.ui.diygui.Animation;
import mclachlan.maze.util.MazeException;
import static mclachlan.maze.editor.swing.MazeEventEditor.*;

/**
 *
 */
public class V1MazeEvent
{
	public static final String SEP = ",";

	private static final V1List<String> slashSeparatedStringList = new V1List<String>("/")
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
	public static String toString(MazeEvent e)
	{
		if (e == null)
		{
			return "";
		}

		StringBuilder s = new StringBuilder();

		int type;
		if (types.containsKey(e.getClass()))
		{
			type = types.get(e.getClass());
		}
		else
		{
			type = CUSTOM;
		}

		s.append(type);
		s.append(SEP);

		switch (type)
		{
			case CUSTOM:
				s.append(e.getClass().getName());
				break;
			case _ZoneChangeEvent:
				ZoneChangeEvent zce = (ZoneChangeEvent)e;
				s.append(zce.getZone());
				s.append(SEP);
				s.append(zce.getPos().x);
				s.append(SEP);
				s.append(zce.getPos().y);
				s.append(SEP);
				s.append(zce.getFacing());
				break;
			case _CastSpellEvent:
				CastSpellEvent cse = (CastSpellEvent)e;
				s.append(cse.getSpellName());
				s.append(SEP);
				s.append(cse.getCasterLevel());
				s.append(SEP);
				s.append(cse.getCastingLevel());
				break;
			case _EncounterActorsEvent:
				EncounterActorsEvent ee = (EncounterActorsEvent)e;
				s.append(ee.getEncounterTable());
				s.append(SEP);
				s.append(ee.getMazeVariable()==null?"":ee.getMazeVariable());
				s.append(SEP);
				s.append(ee.getAttitude()==null?"":ee.getAttitude().name());
				s.append(SEP);
				s.append(ee.getAmbushStatus()==null?"":ee.getAmbushStatus().name());
				s.append(SEP);
				s.append(ee.getPreScript()==null?"":ee.getPreScript());
				s.append(SEP);
				s.append(ee.getPostAppearanceScript()==null?"":ee.getPostAppearanceScript());
				break;
			case _FlavourTextEvent:
				FlavourTextEvent fte = (FlavourTextEvent)e;
				s.append(fte.getDelay());
				s.append(SEP);
				s.append(fte.shouldClearText());
				s.append(SEP);
				s.append(V1Utils.escapeNewlines(fte.getFlavourText()));
				break;
			case _GrantExperienceEvent:
				GrantExperienceEvent gee = (GrantExperienceEvent)e;
				s.append(gee.getAmount());
				// not supporting saving a specific PC
				break;
			case _GrantGoldEvent:
				GrantGoldEvent gge = (GrantGoldEvent)e;
				s.append(gge.getAmount());
				break;
			case _GrantItemsEvent:
				throw new MazeException("not supported "+e);
			case _SignBoardEvent:
				SignBoardEvent sbe = (SignBoardEvent)e;
				s.append(V1Utils.escapeNewlines(sbe.getSignBoardText()));
				break;
			case _LootTableEvent:
				LootTableEvent lte = (LootTableEvent)e;
				s.append(lte.getLootTable().getName());
				break;
			case _DelayEvent:
				DelayEvent de = (DelayEvent)e;
				s.append(de.getDelay());
				break;
			case _MovePartyEvent:
				MovePartyEvent mpe = (MovePartyEvent)e;
				s.append(mpe.getPos().x);
				s.append(SEP);
				s.append(mpe.getPos().y);
				s.append(SEP);
				s.append(mpe.getFacing());
				break;
			case _CharacterClassKnowledgeEvent:
				CharacterClassKnowledgeEvent ccke = (CharacterClassKnowledgeEvent)e;

				Map<String, String> map = ccke.getKnowledgeText();

				for (String className : map.keySet())
				{
					s.append(className);
					s.append(SEP);
					String str = map.get(className);
					s.append(V1Utils.escapeNewlines(V1Utils.escapeCommas(str)));
					s.append(SEP);
				}

				break;
			case _PersonalitySpeechEvent:
				PersonalitySpeechBubbleEvent spbe = (PersonalitySpeechBubbleEvent)e;
				s.append(spbe.getSpeechKey());
				s.append(SEP);
				s.append(spbe.isModal());
				break;
			case _StoryboardEvent:
				StoryboardEvent se = (StoryboardEvent)e;
				s.append(se.getImageResource());
				s.append(SEP);
				s.append(se.getTextResource());
				s.append(SEP);
				s.append(se.getTextPlacement());
				break;
			case _SetUserConfigEvent:
				SetUserConfigEvent suce = (SetUserConfigEvent)e;
				s.append(suce.getVar());
				s.append(SEP);
				s.append(suce.getValue());
				break;
			case _TogglePortalStateEvent:
				TogglePortalStateEvent tpse = (TogglePortalStateEvent)e;
				s.append(V1Point.toString(tpse.getTile()));
				s.append(SEP);
				s.append(tpse.getFacing());
				break;
			case _MazeScript:
				MazeScriptEvent mse = (MazeScriptEvent)e;
				s.append(mse.getScript());
				break;
			case _RemoveWall:
				RemoveWallEvent rwe = (RemoveWallEvent)e;
				s.append(rwe.isHorizontalWall());
				s.append(SEP);
				s.append(rwe.getMazeVariable());
				s.append(SEP);
				s.append(rwe.getWallIndex());
				break;
			case _RemoveObjectEvent:
				RemoveObjectEvent roe = (RemoveObjectEvent)e;
				s.append(roe.getObjectName());
				break;
			case _BlockingScreen:
				BlockingScreenEvent bse = (BlockingScreenEvent)e;
				s.append(bse.getImageResource());
				s.append(SEP);
				s.append(bse.getMode());
				break;
			case _EndGame:
				break;
			case _SetMazeVariableEvent:
				SetMazeVariableEvent sme = (SetMazeVariableEvent)e;
				s.append(sme.getMazeVariable());
				s.append(SEP);
				s.append(sme.getValue());
				break;
			case _SkillTestEvent:
				SkillTestEvent ste = (SkillTestEvent)e;
				s.append(ste.getKeyModifier()==null?"":ste.getKeyModifier());
				s.append(SEP);
				s.append(V1Value.toString(ste.getSkill(), "~", "`"));
				s.append(SEP);
				s.append(V1Value.toString(ste.getSuccessValue(), "~", "`"));
				s.append(SEP);
				s.append(ste.getSuccessScript()==null?"":ste.getSuccessScript());
				s.append(SEP);
				s.append(ste.getFailureScript()==null?"":ste.getFailureScript());
				break;
			case _JournalEntryEvent:
				JournalEntryEvent jee = (JournalEntryEvent)e;
				s.append(jee.getType());
				s.append(SEP);
				s.append(jee.getKey());
				s.append(SEP);
				s.append(jee.getJournalText());
				break;

			case _SoundEffectEvent:
				SoundEffectEvent see = (SoundEffectEvent)e;
				s.append(slashSeparatedStringList.toString(see.getClipNames()));
				break;
			case _MusicEvent:
				MusicEvent me = (MusicEvent)e;
				s.append(slashSeparatedStringList.toString(me.getTrackNames()));
				s.append(SEP);
				s.append(me.getMusicState()==null?"":me.getMusicState());
				break;
			case _AnimationEvent:
				AnimationEvent ae = (AnimationEvent)e;
				s.append(V1Animation.toString(ae.getAnimation()));
				break;

			case _ActorDiesEvent:
			case _ActorUnaffectedEvent:
			case _AmbushHitEvent:
			case _AmbushMissEvent:
			case _AttackDodgeEvent:
			case _AttackEvent:
			case _AttackHitEvent:
			case _AttackMissEvent:
			case _BreaksFreeEvent:
			case _ConditionEvent:
			case _CowerInFearEvent:
			case _DamageEvent:
			case _DancesWildlyEvent:
			case _DefendEvent:
			case _EquipEvent:
			case _FailureEvent:
			case _FatigueEvent:
			case _FreezeInTerrorEvent:
			case _GagsHelplesslyEvent:
			case _HealingEvent:
			case _HideAttemptEvent:
			case _HideFailsEvent:
			case _HideSucceedsEvent:
			case _ItchesUncontrollablyEvent:
			case _ItemUseEvent:
			case _LaughsMadlyEvent:
			case _NoEffectEvent	:
			case _NpcCharmedEvent:
			case _NpcMindreadEvent:
			case _NpcMindreadFailedEvent:
			case _NpcNotCharmedEvent:
			case _RemoveCurseEvent:
			case _RetchesNoisilyEvent:
			case _RunAwayAttemptEvent:
			case _RunAwayFailedEvent:
			case _RunAwaySuccessEvent:
			case _SpecialAbilityUseEvent:
			case _SpellCastEvent:
			case _SpellFizzlesEvent:
			case _StrugglesMightilyEvent:
			case _StumbleBlindlyEvent:
			case _SuccessEvent:
			case _SummoningFailsEvent:
			case _SummoningSucceedsEvent:
			case _TheftSpellFailed:
			case _TheftSpellSucceeded:
			case _ChangeNpcFactionAttitudeEvent:
			case _ChangeNpcAttitudeEvent:
			case _ChangeNpcLocationEvent:
			case _ChangeNpcTheftCounter:
			case _GiveItemToParty:
			case _NpcAttacksEvent:
			case _NpcLeavesEvent:
			case _NpcSpeechEvent:
			case _NpcTakesItemEvent:
			case _WaitForPlayerSpeech:

			default: throw new MazeException("invalid type: "+type+" ["+e+"]");
		}

		return s.toString();
	}

	/*-------------------------------------------------------------------------*/
	public static MazeEvent fromString(String s)
	{
		if (s.equals(""))
		{
			return null;
		}

		String[] strs = s.split(SEP, -1);
		int type = Integer.parseInt(strs[0]);

		switch (type)
		{
			case CUSTOM:
				try
				{
					Class clazz = Class.forName(strs[1]);
					return (MazeEvent)clazz.newInstance();
				}
				catch (Exception e)
				{
					throw new RuntimeException(e);
				}
			case _ZoneChangeEvent:
				String zone = strs[1];
				int x = Integer.parseInt(strs[2]);
				int y = Integer.parseInt(strs[3]);
				int facing = Integer.parseInt(strs[4]);
				return new ZoneChangeEvent(zone, new Point(x, y), facing);
			case _CastSpellEvent:
				String spellName = strs[1];
				int casterLevel = Integer.parseInt(strs[2]);
				int castingLevel = Integer.parseInt(strs[3]);
				return new CastSpellEvent(spellName, casterLevel, castingLevel);
			case _EncounterActorsEvent:

				// 1=3,gatehouse.level.1.bats,gatehouse.first.room.bats.1,ATTACKING,preScript

				String encounterTable = strs[1];
				String mazeVariable = strs.length > 2 ? strs[2] : null;
				NpcFaction.Attitude attitude = null;
				Combat.AmbushStatus ambushStatus = null;
				String preScript = null, postAppearanceScript = null;
				if (strs.length > 3)
				{
					attitude = "".equals(strs[3]) ? null : NpcFaction.Attitude.valueOf(strs[3]);
					ambushStatus = "".equals(strs[4]) ? null : Combat.AmbushStatus.valueOf(strs[4]);
					preScript = "".equals(strs[5])?null:strs[5];
					postAppearanceScript = "".equals(strs[6])?null:strs[6];
				}
				return new EncounterActorsEvent(mazeVariable, (String)null, attitude, ambushStatus, null, null, null, null); // todo
			case _FlavourTextEvent:
				int delay = Integer.parseInt(strs[1]);
				boolean shouldClearTest = Boolean.valueOf(strs[2]);
				// hack alert.  Any commas will have been split above.  Replace them
				StringBuilder sb = new StringBuilder();
				for (int i=3; i<strs.length; i++)
				{
					sb.append(strs[i]).append(',');
				}
				String text = V1Utils.replaceNewlines(sb.substring(0, sb.length()-1));
				return new FlavourTextEvent(text, delay, shouldClearTest, FlavourTextEvent.Alignment.CENTER); // todo
			case _GrantExperienceEvent:
				int amount = Integer.parseInt(strs[1]);
				return new GrantExperienceEvent(amount, null);
			case _GrantGoldEvent:
				amount = Integer.parseInt(strs[1]);
				return new GrantGoldEvent(amount);
			case _GrantItemsEvent:
				throw new MazeException("not supported ["+s+"]");
			case _SignBoardEvent:
				String sbText = V1Utils.replaceNewlines(strs[1]);
				return new SignBoardEvent(sbText);
			case _LootTableEvent:
				String lootTable = strs[1];
				return new LootTableEvent(Database.getInstance().getLootTable(lootTable));
			case _DelayEvent:
				int d = Integer.parseInt(strs[1]);
				return new DelayEvent(d);
			case _MovePartyEvent:
				int mpe_x = Integer.parseInt(strs[1]);
				int mpe_y = Integer.parseInt(strs[2]);
				int mpe_facing = Integer.parseInt(strs[3]);
				return new MovePartyEvent(new Point(mpe_x, mpe_y), mpe_facing);
			case _CharacterClassKnowledgeEvent:
				Map<String, String> knowledgeText = new HashMap<String, String>();

				for (int i = 1; i < strs.length; i+=2)
				{
					List<String> classes = slashSeparatedStringList.fromString(strs[i]);
					String str = V1Utils.replaceNewlines(V1Utils.replaceCommas(strs[i+1]));
					for (String c : classes)
					{
						knowledgeText.put(c, str);
					}
				}
				return new CharacterClassKnowledgeEvent(knowledgeText);
			case _PersonalitySpeechEvent:
				return new PersonalitySpeechBubbleEvent(strs[1], Boolean.valueOf(strs[2]));
			case _StoryboardEvent:
				return new StoryboardEvent(strs[1], strs[2], StoryboardEvent.TextPlacement.valueOf(strs[3]));
			case _SetUserConfigEvent:
				return new SetUserConfigEvent(strs[1], strs[2]);
			case _TogglePortalStateEvent:
				return new TogglePortalStateEvent(V1Point.fromString(strs[1]), Integer.parseInt(strs[2]));
			case _MazeScript:
				return new MazeScriptEvent(strs[1]);
			case _RemoveWall:
				boolean isHoriz = Boolean.valueOf(strs[1]);
				String mazeVar = strs[2];
				int wallIndex = Integer.parseInt(strs[3]);
				return new RemoveWallEvent(mazeVar, isHoriz, wallIndex);
			case _RemoveObjectEvent:
				return new RemoveObjectEvent(strs[1]);
			case _BlockingScreen:
				String imageResource = strs[1];
				int mode = Integer.parseInt(strs[2]);
				return new BlockingScreenEvent(imageResource, mode);
			case _EndGame:
				return new EndGameEvent();
			case _SetMazeVariableEvent:
				return new SetMazeVariableEvent(strs[1], strs[2]);
			case _SkillTestEvent:
				Stats.Modifier keyMod = "".equals(strs[1]) ? null : Stats.Modifier.valueOf(strs[1]);
				ValueList skill = V1Value.fromString(strs[2], "~", "`");
				ValueList successValue = V1Value.fromString(strs[3], "~", "`");
				return new SkillTestEvent(keyMod, skill, successValue, "".equals(strs[4])?null:strs[4], "".equals(strs[5])?null:strs[5]);

			case _ActorDiesEvent:
			case _ActorUnaffectedEvent:
			case _AmbushHitEvent:
			case _AmbushMissEvent:
			case _AttackDodgeEvent:
			case _AttackEvent:
			case _AttackHitEvent:
			case _AttackMissEvent:
			case _BreaksFreeEvent:
			case _ConditionEvent:
			case _CowerInFearEvent:
			case _DamageEvent:
			case _DancesWildlyEvent:
			case _DefendEvent:
			case _EquipEvent:
			case _FailureEvent:
			case _FatigueEvent:
			case _FreezeInTerrorEvent:
			case _GagsHelplesslyEvent:
			case _HealingEvent:
			case _HideAttemptEvent:
			case _HideFailsEvent:
			case _HideSucceedsEvent:
			case _ItchesUncontrollablyEvent:
			case _ItemUseEvent:
			case _LaughsMadlyEvent:
			case _NoEffectEvent	:
			case _NpcCharmedEvent:
			case _NpcMindreadEvent:
			case _NpcMindreadFailedEvent:
			case _NpcNotCharmedEvent:
			case _RemoveCurseEvent:
			case _RetchesNoisilyEvent:
			case _RunAwayAttemptEvent:
			case _RunAwayFailedEvent:
			case _RunAwaySuccessEvent:
			case _SpecialAbilityUseEvent:
			case _SpellCastEvent:
			case _SpellFizzlesEvent:
			case _StrugglesMightilyEvent:
			case _StumbleBlindlyEvent:
			case _SuccessEvent:
			case _SummoningFailsEvent:
			case _SummoningSucceedsEvent:
			case _TheftSpellFailed:
			case _TheftSpellSucceeded:
				throw new MazeException("not supported yet ["+s+"]");
			case _SoundEffectEvent:
				return new SoundEffectEvent(slashSeparatedStringList.fromString(strs[1]));
			case _MusicEvent:
				List<String> trackNames = null;
				if (strs.length > 1)
				{
					trackNames = slashSeparatedStringList.fromString(strs[1]);
				}
				String musicState = null;
				if (strs.length > 2)
				{
					musicState = "".equals(strs[2]) ? null : strs[2];
				}
				return new MusicEvent(trackNames, musicState);
			case _AnimationEvent:
				Animation a = V1Animation.fromString(strs[1]);
				return new AnimationEvent(a);

			case _ChangeNpcAttitudeEvent:
			case _ChangeNpcLocationEvent:
			case _ChangeNpcTheftCounter:
			case _GiveItemToParty:
			case _NpcAttacksEvent:
			case _NpcLeavesEvent:
			case _NpcSpeechEvent:
			case _NpcTakesItemEvent:
			case _WaitForPlayerSpeech:

			default: throw new MazeException("invalid type: "+type+" ["+s+"]");
		}
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * for testing only
	 */
	public static void main(String[] args)
	{

	}
}
