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
import mclachlan.maze.map.script.*;
import mclachlan.maze.stat.Stats;
import mclachlan.maze.stat.combat.Combat;
import mclachlan.maze.stat.combat.event.*;
import mclachlan.maze.stat.magic.ValueList;
import mclachlan.maze.stat.npc.*;
import mclachlan.maze.ui.diygui.Animation;
import mclachlan.maze.util.MazeException;

/**
 *
 */
public class V1MazeEvent
{
	public static final String SEP = ",";
	public static Map<Class, Integer> types;

	public static final int CUSTOM = 0;
	public static final int _ZoneChangeEvent = 1;
	public static final int _CastSpellEvent = 2;
	public static final int _EncounterActorsEvent = 3;
	public static final int _FlavourTextEvent = 4;
	public static final int _GrantExperienceEvent = 5;
	public static final int _GrantGoldEvent = 6;
	public static final int _GrantItemsEvent = 7;
	public static final int _SignBoardEvent = 8;
	public static final int _LootTableEvent = 9;
	public static final int _DelayEvent = 10;
	public static final int _MovePartyEvent = 11;
	public static final int _CharacterClassKnowledgeEvent = 12;
	public static final int _MazeScript = 13;
	public static final int _RemoveWall = 14;
	public static final int _BlockingScreen = 15;
	public static final int _EndGame = 16;
	public static final int _SetMazeVariableEvent = 17;
	public static final int _PersonalitySpeechEvent = 18;
	public static final int _StoryboardEvent = 19;
	public static final int _SetUserConfigEvent = 20;
	public static final int _TogglePortalStateEvent = 21;
	public static final int _RemoveObjectEvent = 22;
	public static final int _SkillTestEvent = 23;
	public static final int _AnimationEvent = 104;
	public static final int _MusicEvent = 149;
	public static final int _SoundEffectEvent = 138;

	public static final int _ActorDiesEvent = 100;
	public static final int _ActorUnaffectedEvent = 101;
	public static final int _AmbushHitEvent = 102;
	public static final int _AmbushMissEvent = 103;
	public static final int _AttackDodgeEvent = 105;
	public static final int _AttackEvent = 106;
	public static final int _AttackHitEvent = 107;
	public static final int _AttackMissEvent = 108;
	public static final int _BreaksFreeEvent = 109;
	public static final int _ConditionEvent = 110;
	public static final int _CowerInFearEvent = 111;
	public static final int _DamageEvent = 112;
	public static final int _DancesWildlyEvent = 113;
	public static final int _DefendEvent = 114;
	public static final int _EquipEvent = 116;
	public static final int _FailureEvent = 117;
	public static final int _FatigueEvent = 118;
	public static final int _FreezeInTerrorEvent = 119;
	public static final int _GagsHelplesslyEvent = 120;
	public static final int _HealingEvent = 121;
	public static final int _HideAttemptEvent = 122;
	public static final int _HideFailsEvent = 123;
	public static final int _HideSucceedsEvent = 124;
	public static final int _ItchesUncontrollablyEvent = 125;
	public static final int _ItemUseEvent = 126;
	public static final int _LaughsMadlyEvent = 127;
	public static final int _NoEffectEvent = 128;
	public static final int _NpcCharmedEvent = 129;
	public static final int _NpcMindreadEvent = 130;
	public static final int _NpcMindreadFailedEvent = 131;
	public static final int _NpcNotCharmedEvent = 132;
	public static final int _RemoveCurseEvent = 133;
	public static final int _RetchesNoisilyEvent = 134;
	public static final int _RunAwayAttemptEvent = 135;
	public static final int _RunAwayFailedEvent = 136;
	public static final int _RunAwaySuccessEvent = 137;
	public static final int _SpecialAbilityUseEvent = 139;
	public static final int _SpellCastEvent = 140;
	public static final int _SpellFizzlesEvent = 141;
	public static final int _StrugglesMightilyEvent = 142;
	public static final int _StumbleBlindlyEvent = 143;
	public static final int _SuccessEvent = 144;
	public static final int _SummoningFailsEvent = 145;
	public static final int _SummoningSucceedsEvent = 146;
	public static final int _TheftSpellFailed = 147;
	public static final int _TheftSpellSucceeded = 148;

	public static final int _ChangeNpcAttitudeEvent = 200;
	public static final int _ChangeNpcLocationEvent = 201;
	public static final int _ChangeNpcTheftCounter = 202;
	public static final int _GiveItemToParty = 203;
	public static final int _NpcAttacksEvent = 204;
	public static final int _NpcLeavesEvent = 205;
	public static final int _NpcSpeechEvent = 206;
	public static final int _NpcTakesItemEvent = 207;
	public static final int _WaitForPlayerSpeech = 208;
	public static final int _ChangeNpcFactionAttitudeEvent = 209;
	
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
	static
	{
		types = new HashMap<Class, Integer>();

		types.put(ZoneChangeEvent.class, _ZoneChangeEvent);
		types.put(CastSpellEvent.class, _CastSpellEvent);
		types.put(EncounterActorsEvent.class, _EncounterActorsEvent);
		types.put(FlavourTextEvent.class, _FlavourTextEvent);
		types.put(GrantExperienceEvent.class, _GrantExperienceEvent);
		types.put(GrantGoldEvent.class, _GrantGoldEvent);
		types.put(GrantItemsEvent.class, _GrantItemsEvent);
		types.put(SignBoardEvent.class, _SignBoardEvent);
		types.put(LootTableEvent.class, _LootTableEvent);
		types.put(ActorDiesEvent.class, _ActorDiesEvent);
		types.put(ActorUnaffectedEvent.class, _ActorUnaffectedEvent);
		types.put(AnimationEvent.class, _AnimationEvent);
		types.put(AttackDodgeEvent.class, _AttackDodgeEvent);
		types.put(AttackEvent.class, _AttackEvent);
		types.put(AttackHitEvent.class, _AttackHitEvent);
		types.put(AttackMissEvent.class, _AttackMissEvent);
		types.put(BreaksFreeEvent.class, _BreaksFreeEvent);
		types.put(ConditionEvent.class, _ConditionEvent);
		types.put(CowerInFearEvent.class, _CowerInFearEvent);
		types.put(DamageEvent.class, _DamageEvent);
		types.put(DancesWildlyEvent.class, _DancesWildlyEvent);
		types.put(DefendEvent.class, _DefendEvent);
		types.put(DelayEvent.class, _DelayEvent);
		types.put(MovePartyEvent.class, _MovePartyEvent);
		types.put(CharacterClassKnowledgeEvent.class, _CharacterClassKnowledgeEvent);
		types.put(PersonalitySpeechBubbleEvent.class, _PersonalitySpeechEvent);
		types.put(StoryboardEvent.class, _StoryboardEvent);
		types.put(SetUserConfigEvent.class, _SetUserConfigEvent);
		types.put(TogglePortalStateEvent.class, _TogglePortalStateEvent);
		types.put(RemoveObjectEvent.class, _RemoveObjectEvent);
		types.put(SkillTestEvent.class, _SkillTestEvent);

		types.put(MazeScriptEvent.class, _MazeScript);
		types.put(RemoveWallEvent.class, _RemoveWall);
		types.put(BlockingScreenEvent.class, _BlockingScreen);
		types.put(EndGameEvent.class, _EndGame);
		types.put(EquipEvent.class, _EquipEvent);
		types.put(FailureEvent.class, _FailureEvent);
		types.put(FatigueEvent.class, _FatigueEvent);
		types.put(FreezeInTerrorEvent.class, _FreezeInTerrorEvent);
		types.put(GagsHelplesslyEvent.class, _GagsHelplesslyEvent);
		types.put(HealingEvent.class, _HealingEvent);
		types.put(HideAttemptEvent.class, _HideAttemptEvent);
		types.put(HideFailsEvent.class, _HideFailsEvent);
		types.put(HideSucceedsEvent.class, _HideSucceedsEvent);
		types.put(ItchesUncontrollablyEvent.class, _ItchesUncontrollablyEvent);
		types.put(ItemUseEvent.class, _ItemUseEvent);
		types.put(LaughsMadlyEvent.class, _LaughsMadlyEvent);
		types.put(NoEffectEvent	.class, _NoEffectEvent	);
		types.put(NpcCharmedEvent.class, _NpcCharmedEvent);
		types.put(NpcMindreadEvent.class, _NpcMindreadEvent);
		types.put(NpcMindreadFailedEvent.class, _NpcMindreadFailedEvent);
		types.put(NpcNotCharmedEvent.class, _NpcNotCharmedEvent);
		types.put(RemoveCurseEvent.class, _RemoveCurseEvent);
		types.put(RetchesNoisilyEvent.class, _RetchesNoisilyEvent);
		types.put(RunAwayAttemptEvent.class, _RunAwayAttemptEvent);
		types.put(RunAwayFailedEvent.class, _RunAwayFailedEvent);
		types.put(RunAwaySuccessEvent.class, _RunAwaySuccessEvent);
		types.put(SoundEffectEvent.class, _SoundEffectEvent);
		types.put(MusicEvent.class, _MusicEvent);
		types.put(SpecialAbilityUseEvent.class, _SpecialAbilityUseEvent);
		types.put(SpellCastEvent.class, _SpellCastEvent);
		types.put(SpellFizzlesEvent.class, _SpellFizzlesEvent);
		types.put(StrugglesMightilyEvent.class, _StrugglesMightilyEvent);
		types.put(StumbleBlindlyEvent.class, _StumbleBlindlyEvent);
		types.put(SuccessEvent.class, _SuccessEvent);
		types.put(SummoningFailsEvent.class, _SummoningFailsEvent);
		types.put(SummoningSucceedsEvent.class, _SummoningSucceedsEvent);
		types.put(TheftSpellFailed.class, _TheftSpellFailed);
		types.put(TheftSpellSucceeded.class, _TheftSpellSucceeded);
		types.put(ChangeNpcAttitudeEvent.class, _ChangeNpcAttitudeEvent);
		types.put(ChangeNpcLocationEvent.class, _ChangeNpcLocationEvent);
		types.put(ChangeNpcTheftCounter.class, _ChangeNpcTheftCounter);
		types.put(GiveItemToParty.class, _GiveItemToParty);
		types.put(NpcAttacksEvent.class, _NpcAttacksEvent);
		types.put(ActorsLeaveEvent.class, _NpcLeavesEvent);
		types.put(NpcSpeechEvent.class, _NpcSpeechEvent);
		types.put(NpcTakesItemEvent.class, _NpcTakesItemEvent);
		types.put(WaitForPlayerSpeech.class, _WaitForPlayerSpeech);
		types.put(SetMazeVariableEvent.class, _SetMazeVariableEvent);
		types.put(ChangeNpcFactionAttitudeEvent.class, _ChangeNpcFactionAttitudeEvent);
	}

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
				return new EncounterActorsEvent(mazeVariable, encounterTable, attitude, ambushStatus, preScript, postAppearanceScript);
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
				return new FlavourTextEvent(text, delay, shouldClearTest);
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
