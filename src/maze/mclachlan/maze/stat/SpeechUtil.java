/*
 * Copyright (c) 2012 Alan McLachlan
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

import java.awt.Rectangle;
import java.util.*;
import mclachlan.maze.data.StringUtil;
import mclachlan.maze.game.Maze;
import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.game.event.UiMessageEvent;
import mclachlan.maze.stat.combat.event.PersonalitySpeechBubbleEvent;
import mclachlan.maze.stat.condition.Condition;
import mclachlan.maze.stat.condition.ConditionEffect;
import mclachlan.maze.ui.diygui.animation.SpeechBubble;
import mclachlan.maze.util.MazeException;

/**
 *
 */
public class SpeechUtil
{
	private static final SpeechUtil instance = new SpeechUtil();

	public static final int OFF = 0;
	public static final int LOW = 1;
	public static final int MEDIUM = 2;
	public static final int HIGH = 3;

	/*-------------------------------------------------------------------------*/
	public static SpeechUtil getInstance()
	{
		return instance;
	}

	/*-------------------------------------------------------------------------*/

	/**
	 * @param speechKey
	 * 	Lookup key for the speech
	 * @param pc
	 * 	The PC to originate from, null if none
	 * @param p
	 * 	The personality speaking
	 * @param origin
	 * 	Bounds to originate from
	 * @param orientation
	 * 	Where to locate the speech bubble relative to the origin. Null if
	 * 	the animation is to just guess.
	 */
	public void genericSpeech(
		String speechKey,
		PlayerCharacter pc,
		Personality p,
		Rectangle origin,
		SpeechBubble.Orientation orientation)
	{
		Maze.getInstance().speechBubble(
			speechKey,
			pc,
			p,
			origin,
			orientation);
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> attackEventSpeech(PlayerCharacter pc)
	{
		// chance of character having something to say
		if (backgroundCharacterSpeechOccurs())
		{
			String speechKey = switch (Dice.d3.roll("SpeechUtil.attackEventSpeech"))
				{
					case 1 -> Personality.BasicSpeech.MELEE_ATTACK_1.getKey();
					case 2 -> Personality.BasicSpeech.MELEE_ATTACK_2.getKey();
					case 3 -> Personality.BasicSpeech.MELEE_ATTACK_3.getKey();
					default -> throw new MazeException("wtf?");
				};

			return getSpeechBubbleEvent(pc, speechKey);
		}
		else
		{
			return null;
		}
	}

	/*-------------------------------------------------------------------------*/
	private List<MazeEvent> getSpeechBubbleEvent(PlayerCharacter pc,
		String speechKey)
	{
		String words = pc.getPersonality().getWords(speechKey);
		if (words != null && words.length() > 0)
		{
			return List.of((MazeEvent)new PersonalitySpeechBubbleEvent(pc, speechKey));
		}
		else
		{
			return new ArrayList<>();
		}
	}

	/*-------------------------------------------------------------------------*/
	private boolean backgroundCharacterSpeechOccurs()
	{
		return switch (Maze.getInstance().getUserConfig().getPersonalityChattiness())
			{
				case OFF -> false;
				case LOW ->
					Dice.d10.roll("SpeechUtil.backgroundCharacterSpeechOccurs.1") == 1;
				case MEDIUM ->
					Dice.d2.roll("SpeechUtil.backgroundCharacterSpeechOccurs.2") == 1;
				case HIGH -> true;
				default -> throw new MazeException(
					"Invalid mclachlan.maze.ui.personality_chattiness: " + Maze.getInstance().getUserConfig().getPersonalityChattiness());
			};
	}

	/*-------------------------------------------------------------------------*/
	public void startCombatSpeech(int avgFoeLevel, int partyLevel)
	{
		if (!backgroundCharacterSpeechOccurs())
		{
			return;
		}

		String speechKey;
		if (avgFoeLevel < partyLevel -3)
		{
			speechKey = Personality.BasicSpeech.ENCOUNTER_START_EASY.getKey();
		}
		else if (avgFoeLevel < partyLevel +3)
		{
			speechKey = Personality.BasicSpeech.ENCOUNTER_START_NORMAL.getKey();
		}
		else
		{
			speechKey = Personality.BasicSpeech.ENCOUNTER_START_HARD.getKey();
		}

		PlayerCharacter speaker = getRandomPlayerCharacterForSpeech(speechKey);
		if (speaker != null)
		{
			Maze.getInstance().speechBubble(speechKey, speaker);
		}
	}

	/*-------------------------------------------------------------------------*/
	public PlayerCharacter getRandomPlayerCharacterForSpeech(String speechKey)
	{
		return Maze.getInstance().getParty().getRandomPlayerCharacterForSpeech(speechKey);
	}

	/*-------------------------------------------------------------------------*/
	public void winCombatSpeech(int avgFoeLevel, int partyLevel)
	{
		if (!backgroundCharacterSpeechOccurs())
		{
			return;
		}
		
		String speechKey;
		if (avgFoeLevel < partyLevel -3)
		{
			speechKey = Personality.BasicSpeech.ENCOUNTER_WIN_EASY.getKey();
		}
		else if (avgFoeLevel < partyLevel +3)
		{
			speechKey = Personality.BasicSpeech.ENCOUNTER_WIN_NORMAL.getKey();
		}
		else
		{
			speechKey = Personality.BasicSpeech.ENCOUNTER_WIN_HARD.getKey();
		}

		PlayerCharacter speaker = getRandomPlayerCharacterForSpeech(speechKey);
		if (speaker != null)
		{
			Maze.getInstance().speechBubble(speechKey, speaker);
		}
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> allyDiesSpeech(PlayerCharacter victim)
	{
		if (!backgroundCharacterSpeechOccurs())
		{
			return new ArrayList<>();
		}

		PlayerParty party = Maze.getInstance().getParty();

		if (party == null)
		{
			// party's over
			return null;
		}

		String speechKey;

		if (party.numAlive() == 1)
		{
			// last man standing
			speechKey = Personality.BasicSpeech.LAST_MAN_STANDING.getKey();
		}
		else if (victim.getGender().getName().equals("Male"))
		{
			speechKey = Personality.BasicSpeech.ALLY_DIES_MALE.getKey();
		}
		else if (victim.getGender().getName().equals("Female"))
		{
			speechKey = Personality.BasicSpeech.ALLY_DIES_FEMALE.getKey();
		}
		else
		{
			speechKey = switch (Dice.d2.roll("SpeechUtil.allyDiesSpeech"))
				{
					case 1 -> Personality.BasicSpeech.ALLY_DIES_MALE.getKey();
					case 2 -> Personality.BasicSpeech.ALLY_DIES_FEMALE.getKey();
					default -> throw new MazeException("wtf");
				};
		}

		PlayerCharacter speaker = null;

		for (int i=0; i<6; i++)
		{
			speaker = party.getRandomPlayerCharacterForSpeech(speechKey);
			if (speaker != null && speaker != victim)
			{
				break;
			}
		}

		if (speaker != null)
		{
			return getSpeechBubbleEvent(speaker, speechKey);
		}
		else
		{
			return new ArrayList<>();
		}
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> slayFoeSpeech(PlayerCharacter pc)
	{
		if (backgroundCharacterSpeechOccurs())
		{
			return getSpeechBubbleEvent(pc, Personality.BasicSpeech.SLAY_FOE.getKey());
		}
		else
		{
			return new ArrayList<>();
		}
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> badlyWoundedSpeech(PlayerCharacter pc)
	{
		if (!backgroundCharacterSpeechOccurs())
		{
			return getSpeechBubbleEvent(pc, Personality.BasicSpeech.BADLY_WOUNDED.getKey());
		}
		else
		{
			return new ArrayList<>();
		}
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> resurrectionSpeech(PlayerCharacter pc)
	{
		if (!backgroundCharacterSpeechOccurs())
		{
			return getSpeechBubbleEvent(pc, Personality.BasicSpeech.RESURRECTED.getKey());
		}
		else
		{
			return new ArrayList<>();
		}
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> conditionSpeech(Condition condition, PlayerCharacter pc)
	{
		if (!backgroundCharacterSpeechOccurs())
		{
			return new ArrayList<>();
		}

		ConditionEffect effect = condition.getEffect();
		if (effect != null)
		{
			String speechKey = effect.getSpeechKey();

			if (speechKey != null)
			{
				return getSpeechBubbleEvent(pc, speechKey);
			}
		}

		return new ArrayList<>();
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> spotStashSpeech(PlayerCharacter pc)
	{
		List<MazeEvent> result = new ArrayList<>();

		result.add(new UiMessageEvent(
			StringUtil.getGamesysString("scouting.spot.stash", false, pc.getDisplayName())));

		// always say something, unless character speech is off
		if (Maze.getInstance().getUserConfig().getPersonalityChattiness() != OFF)
		{
			String speechKey = Personality.BasicSpeech.SCOUTING_SPOT_STASH.getKey();
			result.addAll(getSpeechBubbleEvent(pc, speechKey));
		}

		return result;
	}

	/*-------------------------------------------------------------------------*/
	public void dropItemInInventorySpeech(PlayerCharacter pc, Item item)
	{
		if (!backgroundCharacterSpeechOccurs())
		{
			return;
		}
		
		int cur = pc.getCarrying();
		int max = GameSys.getInstance().getCarryingCapacity(pc);

		if (cur > max * .75)
		{
			Maze.getInstance().speechBubble(
				Personality.BasicSpeech.INVENTORY_HEAVY_LOAD.getKey(),
				pc,
				pc.getPersonality(),
				Maze.getInstance().getUi().getPortraitWidgetBounds(pc),
				null);
		}
		else if (item.getBaseCost() >= 10000)
		{
			Maze.getInstance().speechBubble(
				Personality.BasicSpeech.INVENTORY_PREMIUM_ITEM.getKey(),
				pc,
				pc.getPersonality(),
				Maze.getInstance().getUi().getPortraitWidgetBounds(pc),
				null);
		}
	}

	/*-------------------------------------------------------------------------*/
	public int getSpeechBubbleDuration(String text)
	{
		// todo: configurable, and/or worked out by text length
		return 5000;
	}

	/*-------------------------------------------------------------------------*/
	public int getChattiness()
	{
		return Maze.getInstance().getUserConfig().getPersonalityChattiness();
	}
}
