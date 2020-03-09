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

import java.util.*;
import mclachlan.maze.data.Database;
import mclachlan.maze.data.StringUtil;
import mclachlan.maze.stat.condition.Condition;
import mclachlan.maze.stat.magic.*;
import mclachlan.maze.util.MazeException;

/**
 *
 */
public class Leveler
{
	public static final String BONUS_HIT_POINTS = "+3 Hit points";
	public static final String BONUS_ACTION_POINTS = "+3 Action points";
	public static final String BONUS_MAGIC_POINTS = "+3 Magic points";
	public static final String BONUS_ATTRIBUTE = "+1 to an attribute";
	public static final String BONUS_MODIFIERS = "+2 assignable modifiers";
	public static final String BONUS_SPELL_PICK = "+1 spell pick";
	public static final String UNLOCK_MODIFIER = "unlock a modifier";
	public static final String UNLOCK_SPELL_LEVEL = "+1 maximum spell level";
	public static final String REVITALISE = "Revitalise";
	public static final String CHANGE_CLASS = "Change class";
	public static final String UPGRADE_SIGNATURE_WEAPON = "Upgrade signature weapon";
	public static final String MODIFIER_UPGRADE = "Modifier Upgrade";

	/*-------------------------------------------------------------------------*/
	public static String getRandomRace()
	{
		List<String> raceList = new ArrayList<>(Database.getInstance().getRaces().keySet());
		Dice raceD = new Dice(1, raceList.size(), -1);

		String result;
		Race r;

		do
		{
			result = raceList.get(raceD.roll("leveler: race"));
			r = Database.getInstance().getRace(result);
		}
		while (r.isLocked());

		return result;
	}

	/*-------------------------------------------------------------------------*/
	public static Gender getRandomGender(String raceName)
	{
		Race raceInst = Database.getInstance().getRace(raceName);
		List<Gender> genderList = raceInst.getAllowedGenders();
		Dice genderD = new Dice(1, genderList.size(), -1);

		return genderList.get(genderD.roll("leveler: gender"));
	}

	/*-------------------------------------------------------------------------*/
	public static CharacterClass getRandomCharacterClass(String race,
		String gender)
	{
		List<CharacterClass> classList = new ArrayList<CharacterClass>(
			Database.getInstance().getCharacterClasses().values());
		Dice classD = new Dice(1, classList.size(), -1);

		CharacterClass result = null;

		while (result == null)
		{
			result = classList.get(classD.roll("leveler: class"));

			if (!result.getAllowedGenders().contains(gender) ||
				!result.getAllowedRaces().contains(race))
			{
				result = null;
			}
		}

		return result;
	}

	/*-------------------------------------------------------------------------*/
	public static StartingKit getRandomStartingKit(String characterClass,
		String characterRace)
	{
		List<StartingKit> kitList = getKitsForRace(characterRace);

		if (kitList == null || kitList.isEmpty())
		{
			kitList = getKitsForClass(characterClass);
		}

		Dice kitD = new Dice(1, kitList.size(), -1);
		return kitList.get(kitD.roll("leveler: kit"));
	}

	/*-------------------------------------------------------------------------*/
	public static List<StartingKit> getKitsForClass(String characterClass)
	{
		Map<String, StartingKit> startingKits = Database.getInstance().getStartingKits();

		List<StartingKit> result = new ArrayList<StartingKit>();

		for (StartingKit sk : startingKits.values())
		{
			if (sk.getUsableByCharacterClass().contains(characterClass))
			{
				result.add(sk);
			}
		}

		return result;
	}

	/*-------------------------------------------------------------------------*/
	public static List<StartingKit> getKitsForRace(String race)
	{
		Race r = Database.getInstance().getRace(race);
		return r.getStartingItems();
	}

	/*-------------------------------------------------------------------------*/
	public static String getRandomName(Race race, Gender gender)
	{
		Map<String, List<String>> namesByGender = race.getSuggestedNames();

		if (namesByGender != null &&
			!namesByGender.isEmpty())
		{
			List<String> names = namesByGender.get(gender.getName());

			if (names != null && !names.isEmpty())
			{
				Dice d = new Dice(1, names.size(), -1);
				return names.get(d.roll("leveler: name"));
			}
		}
		return null;
	}

	/*-------------------------------------------------------------------------*/
	public static String getRandomPortraitName(String raceName,
		String genderName)
	{
		List<String> names = getAvailablePortraitName(raceName, genderName);
		Dice d = new Dice(1, names.size(), -1);

		return names.get(d.roll("leveler: portrait"));
	}

	/*-------------------------------------------------------------------------*/
	public static List<String> getAvailablePortraitName(String raceName,
		String genderName)
	{
		List<String> raceAndGenderMatches = new ArrayList<String>();
		List<String> raceOnlyMatches = new ArrayList<String>();
		List<String> portraits = Database.getInstance().getPortraitNames();

		// stores the best match if a gender is not matched
		int bestMatch = -1;
		for (int i = 0; i < portraits.size(); i++)
		{
			String s = portraits.get(i);
			if (s.contains(raceName.toLowerCase()))
			{
				// bit of a hack, to avoid "male" matching on a string containing "female"
				if (s.contains("_" + genderName.toLowerCase()))
				{
					raceAndGenderMatches.add(s);
				}
				else if (bestMatch == -1)
				{
					raceOnlyMatches.add(s);
				}
			}
		}

		if (!raceAndGenderMatches.isEmpty())
		{
			return raceAndGenderMatches;
		}
		else
		{
			return raceOnlyMatches;
		}
	}

	/*-------------------------------------------------------------------------*/
	public static Personality getRandomPersonality()
	{
		List<Personality> personalities = new ArrayList<Personality>(
			Database.getInstance().getPersonalities().values());

		Dice d = new Dice(1, personalities.size(), -1);
		return personalities.get(d.roll("leveler: personality"));
	}

	/*-------------------------------------------------------------------------*/
	public static List<Spell> getRandomSpells(
		PlayerCharacter pc)
	{
		List<Spell> result = new ArrayList<Spell>();

		List<Spell> canBeLearned = pc.getSpellsThatCanBeLearned();

		for (int i = 0; i < pc.getSpellPicks(); i++)
		{
			Dice d = new Dice(1, canBeLearned.size(), -1);
			Spell spell = canBeLearned.get(d.roll("leveler: spells"));
			result.add(spell);
			canBeLearned.remove(spell);
		}

		return result;
	}

	/*-------------------------------------------------------------------------*/

	/**
	 * The creation of a new level 1 player character.
	 */
	public PlayerCharacter createNewPlayerCharacter(
		String name,
		CharacterClass characterClass,
		Race race,
		Gender gender,
		String portrait,
		Personality personality,
		List<Spell> startingSpells)
	{
		// calculate starting stats
		int maxHp = calcStartingHitPoints(characterClass, race);
		int maxAp = calcStartingActionPoints(characterClass, race);
		int maxMp = calcStartingMagicPoints(characterClass, race);

		Map<String, Integer> levels = new HashMap<String, Integer>();
		levels.put(characterClass.getName(), 1);

		PlayerCharacter result = new PlayerCharacter(name,
			race,
			gender,
			characterClass,
			portrait,
			levels,
			maxHp,
			maxAp,
			maxMp,
			characterClass.getStartingActiveModifiers());

		// apply class and race modifiers
		result.applyPermanentStatModifier(characterClass.getStartingModifiers());
		result.applyPermanentStatModifier(race.getStartingModifiers());

		result.setPersonality(personality);

		result.setSpellBook(new SpellBook());

		result.setSpellPicks(calcSpellPicks(result, 1));

		if (startingSpells != null)
		{
			for (Spell s : startingSpells)
			{
				if (result.getSpellPicks() > 0)
				{
					result.getSpellBook().addSpell(s);
					result.incSpellPicks(-1);
				}
			}
		}

		return result;
	}

	/*-------------------------------------------------------------------------*/
	public static int calcStartingMagicPoints(CharacterClass characterClass,
		Race race)
	{
		return characterClass.getStartingMagicPoints() *
			race.getStartingMagicPointPercent() / 100;
	}

	/*-------------------------------------------------------------------------*/
	public static int calcStartingActionPoints(CharacterClass characterClass,
		Race race)
	{
		return characterClass.getStartingActionPoints() *
			race.getStartingActionPointPercent() / 100;
	}

	/*-------------------------------------------------------------------------*/
	public static int calcStartingHitPoints(CharacterClass characterClass,
		Race race)
	{
		return characterClass.getStartingHitPoints() *
			race.getStartingHitPointPercent() / 100;
	}

	/*-------------------------------------------------------------------------*/
	public int getAssignableModifiers(PlayerCharacter pc, LevelUpState state)
	{
		return pc.getCharacterClass().getLevelUpAssignableModifiers()
			+ state.getExtraAssignableModifiers();
	}

	/*-------------------------------------------------------------------------*/
	public void applyBonus(
		PlayerCharacter pc,
		LevelUpState state,
		String bonus,
		Object bonusDetails)
	{
		if (bonus.equals(BONUS_HIT_POINTS))
		{
			incCurMax(pc.getHitPoints(), 3);
		}
		else if (bonus.equalsIgnoreCase(BONUS_ACTION_POINTS))
		{
			incCurMax(pc.getActionPoints(), 3);
		}
		else if (bonus.equalsIgnoreCase(BONUS_MAGIC_POINTS))
		{
			incCurMax(pc.getMagicPoints(), 3);
		}
		else if (bonus.equalsIgnoreCase(BONUS_ATTRIBUTE))
		{
			pc.incModifier((Stats.Modifier)bonusDetails, 1);
		}
		else if (bonus.equalsIgnoreCase(BONUS_MODIFIERS))
		{
			state.extraAssignableModifiers += 2;
		}
		else if (bonus.equalsIgnoreCase(BONUS_SPELL_PICK))
		{
			pc.incSpellPicks(1);
		}
		else if (bonus.equalsIgnoreCase(UNLOCK_MODIFIER))
		{
			pc.unlockModifier((Stats.Modifier)bonusDetails);
		}
		else if (bonus.equalsIgnoreCase(UNLOCK_SPELL_LEVEL))
		{
			pc.unlockSpellLevel((MagicSys.SpellBook)bonusDetails);
		}
		else if (bonus.equalsIgnoreCase(REVITALISE))
		{
			pc.getHitPoints().setCurrentToMax();
			pc.getHitPoints().setSub(0);
			pc.getActionPoints().setCurrentToMax();
			pc.getMagicPoints().setCurrentToMax();
			for (Condition c : state.conditions)
			{
				if (revitaliseRemovesCondition(pc, c))
				{
					pc.removeCondition(c);
				}
			}
		}
		else if (bonus.equalsIgnoreCase(CHANGE_CLASS))
		{
			CharacterClass c = Database.getInstance().getCharacterClass((String)bonusDetails);
			pc.applyClassChange(c);
			pc.setActiveModifiers(c.getStartingActiveModifiers());
		}
		else if (bonus.equalsIgnoreCase(UPGRADE_SIGNATURE_WEAPON))
		{
			pc.upgradeSignatureWeapon((String)bonusDetails);
		}
		else if (bonus.equals(MODIFIER_UPGRADE))
		{
			for (Stats.Modifier m : Stats.allModifiers)
			{
				if (bonusDetails.equals(StringUtil.getModifierName(m)))
				{
					pc.incModifier(m, 1);
					return;
				}
			}

			throw new MazeException("Invalid bonusDetails: [" + bonusDetails + "]");
		}
		else
		{
			throw new MazeException("Invalid bonus: [" + bonus + "]");
		}
	}

	/*-------------------------------------------------------------------------*/
	public void rollbackBonus(
		PlayerCharacter pc,
		LevelUpState state,
		String bonus,
		Object bonusDetails)
	{
		if (bonus.equals(Leveler.BONUS_HIT_POINTS))
		{
			incCurMax(pc.getHitPoints(), -3);
		}
		else if (bonus.equalsIgnoreCase(Leveler.BONUS_ACTION_POINTS))
		{
			incCurMax(pc.getActionPoints(), -3);
		}
		else if (bonus.equalsIgnoreCase(Leveler.BONUS_MAGIC_POINTS))
		{
			incCurMax(pc.getMagicPoints(), -3);
		}
		else if (bonus.equalsIgnoreCase(Leveler.BONUS_ATTRIBUTE))
		{
			pc.incModifier((Stats.Modifier)bonusDetails, -1);
		}
		else if (bonus.equalsIgnoreCase(Leveler.BONUS_MODIFIERS))
		{
			state.extraAssignableModifiers -= 2;
		}
		else if (bonus.equalsIgnoreCase(Leveler.BONUS_SPELL_PICK))
		{
			pc.incSpellPicks(-1);
		}
		else if (bonus.equalsIgnoreCase(Leveler.UNLOCK_MODIFIER))
		{
			pc.lockModifier((Stats.Modifier)bonusDetails);
		}
		else if (bonus.equalsIgnoreCase(Leveler.UNLOCK_SPELL_LEVEL))
		{
			pc.lockSpellLevel((MagicSys.SpellBook)bonusDetails);
		}
		else if (bonus.equalsIgnoreCase(Leveler.REVITALISE))
		{
			pc.getHitPoints().setCurrent(state.hpCur);
			pc.getHitPoints().setSub(state.fatigueCur);
			pc.getActionPoints().setCurrent(state.spCur);
			pc.getMagicPoints().setCurrent(state.mpCur);
			for (Condition c : state.conditions)
			{
				if (revitaliseRemovesCondition(pc, c))
				{
					pc.removeCondition(c);
				}
			}
		}
		else if (bonus.equalsIgnoreCase(Leveler.CHANGE_CLASS))
		{
			CharacterClass c = Database.getInstance().getCharacterClass((String)bonusDetails);
			pc.rollbackClassChange(state.curClass, c);
			pc.setActiveModifiers(state.activeModifiers);
		}
		else if (bonus.equalsIgnoreCase(Leveler.UPGRADE_SIGNATURE_WEAPON))
		{
			pc.downgradeSignatureWeapon((String)bonusDetails);
		}
		else if (bonus.equals(MODIFIER_UPGRADE))
		{
			for (Stats.Modifier m : Stats.allModifiers)
			{
				if (bonusDetails.equals(StringUtil.getModifierName(m)))
				{
					pc.incModifier(m, -1);
					return;
				}
			}

			throw new MazeException("Invalid bonusDetails: [" + bonusDetails + "]");
		}
		else
		{
			throw new MazeException("Invalid bonus: [" + bonus + "]");
		}
	}

	/*-------------------------------------------------------------------------*/
	public void applyInitialChanges(PlayerCharacter pc, LevelUpState state)
	{
		pc.incLevel(1);
		pc.incSpellPicks(state.spellPicksInc);
		incCurMax(pc.getHitPoints(), state.hpInc);
		incCurMax(pc.getActionPoints(), state.spInc);
		incCurMax(pc.getMagicPoints(), state.mpInc);
		pc.applyPermanentStatModifier(pc.getCharacterClass().getLevelUpModifiers());
	}

	/*-------------------------------------------------------------------------*/
	public void rollbackInitialChanges(PlayerCharacter pc, LevelUpState state)
	{
		pc.incLevel(-1);
		pc.incSpellPicks(-state.spellPicksInc);
		incCurMax(pc.getHitPoints(), -state.hpInc);
		incCurMax(pc.getActionPoints(), -state.spInc);
		incCurMax(pc.getMagicPoints(), -state.mpInc);
		pc.rollbackPermanentStatModifier(pc.getCharacterClass().getLevelUpModifiers());
	}

	/*-------------------------------------------------------------------------*/

	/**
	 * @return The number of new spell picks available to this character
	 */
	protected static int calcSpellPicks(PlayerCharacter pc, int level)
	{
		LevelAbilityProgression progression = pc.getCharacterClass().getProgression();
		List<LevelAbility> forLevel = progression.getForLevel(level);

		int result = 0;
		for (LevelAbility la : forLevel)
		{
			if (la instanceof AddSpellPicks)
			{
				result += ((AddSpellPicks)la).getSpellPicks();
			}
		}

		return result;
	}

	/*-------------------------------------------------------------------------*/
	public void applyModifiers(PlayerCharacter pc, StatModifier sm)
	{
		pc.applyPermanentStatModifier(sm);
	}

	/*-------------------------------------------------------------------------*/
	public void rollbackModifiers(PlayerCharacter pc, StatModifier sm)
	{
		pc.rollbackPermanentStatModifier(sm);
	}

	/*-------------------------------------------------------------------------*/
	public void applySpells(PlayerCharacter pc, List<Spell> spells)
	{
		pc.incSpellPicks(-spells.size());
		pc.getSpellBook().addSpells(spells);
	}

	/*-------------------------------------------------------------------------*/
	public void rollbackSpells(PlayerCharacter pc, List<Spell> spells)
	{
		pc.incSpellPicks(spells.size());
		pc.getSpellBook().removeSpells(spells);
	}


	/*-------------------------------------------------------------------------*/
	private boolean revitaliseRemovesCondition(UnifiedActor playerCharacter,
		Condition condition)
	{
		return condition.getEffect().isRemovedByRevitalise(playerCharacter, condition);
	}

	/*-------------------------------------------------------------------------*/
	private void incCurMax(CurMax curMax, int value)
	{
		curMax.incMaximum(value);
		curMax.incCurrent(value);
	}

	/*-------------------------------------------------------------------------*/
	public void plus(PlayerCharacter playerCharacter, StatModifier statModifier,
		Stats.Modifier modifier, CurMax bonuses)
	{
		int current = playerCharacter.getModifier(modifier) + statModifier.getModifier(modifier);
		int cost = GameSys.getInstance().getModifierIncreaseCost(
			modifier,
			playerCharacter,
			current);

		statModifier.setModifier(modifier, statModifier.getModifier(modifier) + 1);
		bonuses.decCurrent(cost);
	}

	/*-------------------------------------------------------------------------*/
	public void minus(PlayerCharacter playerCharacter, StatModifier statModifier,
		Stats.Modifier modifier, CurMax bonuses)
	{
		int current = playerCharacter.getModifier(modifier) + statModifier.getModifier(modifier);
		int cost = GameSys.getInstance().getModifierIncreaseCost(
			modifier,
			playerCharacter,
			current - 1);

		statModifier.setModifier(modifier, statModifier.getModifier(modifier) - 1);
		bonuses.incCurrent(cost);
	}

	/*-------------------------------------------------------------------------*/
	public PlayerCharacter createRandomPlayerCharacter()
	{
		Race race = Database.getInstance().getRace(getRandomRace());
		Gender gender = getRandomGender(race.getName());
		CharacterClass characterClass = getRandomCharacterClass(race.getName(), gender.getName());
		StartingKit startingKit = getRandomStartingKit(characterClass.getName(), race.getName());
		String name = getRandomName(race, gender);
		String portrait = getRandomPortraitName(race.getName(), gender.getName());
		Personality personality = getRandomPersonality();
		List<Spell> noSpells = new ArrayList<Spell>();

		PlayerCharacter result = createNewPlayerCharacter(
			name,
			characterClass,
			race,
			gender,
			portrait,
			personality,
			noSpells);

		result.applyStartingKit(startingKit);

		if (!race.isMagicDead())
		{
			List<Spell> randomSpells = getRandomSpells(result);
			result.setSpellBook(new SpellBook(randomSpells));
			result.incSpellPicks(-randomSpells.size());
		}

		return result;
	}

	/*-------------------------------------------------------------------------*/
	public boolean validateParty(List<PlayerCharacter> pcs)
	{
		int combat, stealth, magic, healing, lockAndTrap;
		combat = stealth = magic = healing = lockAndTrap = 0;
		Set<String> classes = new HashSet<String>();

		for (PlayerCharacter pc : pcs)
		{
			if (classes.contains(pc.getCharacterClass().getName()))
			{
				return false;
			}
			classes.add(pc.getCharacterClass().getName());

			// tally up class foci's
			switch (pc.getCharacterClass().getFocus())
			{
				case COMBAT:
					combat++;
					break;
				case STEALTH:
					stealth++;
					break;
				case MAGIC:
					// check no magic dead characters have a magic focus
					if (pc.getRace().isMagicDead())
					{
						return false;
					}
					magic++;
					break;
			}
			// check there is at least one healing spell
			for (Spell spell : pc.getSpellBook().getSpells())
			{
				for (SpellEffect se : spell.getEffects().getPossibilities())
				{
					if (se.getUnsavedResult() instanceof HealingSpellResult)
					{
						healing++;
					}
				}
			}

			// check at least one lock&trap pc
			StatModifier activeModifiers = pc.getActiveModifiers();
			if (activeModifiers.getModifiers().containsKey(Stats.Modifier.LOCK_AND_TRAP))
			{
				lockAndTrap++;
			}
		}

		return
			combat>0 &&
			stealth>0 &&
			magic>0 &&
			healing>0 &&
			lockAndTrap>0;
	}

	/*-------------------------------------------------------------------------*/
	public static class LevelUpState
	{
		// player current state
		private int hpCur;
		private int fatigueCur;
		private int spCur;
		private int mpCur;
		private List<Condition> conditions;
		private StatModifier activeModifiers;
		private CharacterClass curClass;

		private int extraAssignableModifiers;
		private int hpInc;
		private int spInc;
		private int mpInc;
		private int spellPicksInc;

		/*-------------------------------------------------------------------------*/
		public LevelUpState(PlayerCharacter pc, int extraAssignableModifiers)
		{
			hpCur = pc.getHitPoints().getCurrent();
			fatigueCur = pc.getHitPoints().getSub();
			spCur = pc.getActionPoints().getCurrent();
			mpCur = pc.getMagicPoints().getCurrent();
			conditions = new ArrayList<Condition>(pc.getConditions());
			activeModifiers = pc.getActiveModifiers();
			curClass = pc.getCharacterClass();

			hpInc = pc.getCharacterClass().getLevelUpHitPoints().roll("leveler: hp");
			spInc = pc.getCharacterClass().getLevelUpActionPoints().roll("leveler: ap");
			mpInc = pc.getCharacterClass().getLevelUpMagicPoints().roll("leveler: mp") +
				pc.getModifier(Stats.Modifier.BRAINS);

			spellPicksInc = calcSpellPicks(pc, pc.getCurrentClassLevel()+1);

			this.extraAssignableModifiers = extraAssignableModifiers;
		}

		public int getExtraAssignableModifiers()
		{
			return extraAssignableModifiers;
		}

		public void setExtraAssignableModifiers(int extraAssignableModifiers)
		{
			this.extraAssignableModifiers = extraAssignableModifiers;
		}

		public StatModifier getActiveModifiers()
		{
			return activeModifiers;
		}

		public void setActiveModifiers(StatModifier activeModifiers)
		{
			this.activeModifiers = activeModifiers;
		}

		public List<Condition> getConditions()
		{
			return conditions;
		}

		public void setConditions(List<Condition> conditions)
		{
			this.conditions = conditions;
		}

		public CharacterClass getCurClass()
		{
			return curClass;
		}

		public void setCurClass(CharacterClass curClass)
		{
			this.curClass = curClass;
		}

		public int getFatigueCur()
		{
			return fatigueCur;
		}

		public void setFatigueCur(int fatigueCur)
		{
			this.fatigueCur = fatigueCur;
		}

		public int getHpCur()
		{
			return hpCur;
		}

		public void setHpCur(int hpCur)
		{
			this.hpCur = hpCur;
		}

		public int getMpCur()
		{
			return mpCur;
		}

		public void setMpCur(int mpCur)
		{
			this.mpCur = mpCur;
		}

		public int getSpCur()
		{
			return spCur;
		}

		public void setSpCur(int spCur)
		{
			this.spCur = spCur;
		}
	}
}
