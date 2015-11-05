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

package mclachlan.maze.balance;

import java.awt.Point;
import java.util.*;
import java.util.concurrent.*;
import mclachlan.maze.data.Database;
import mclachlan.maze.data.Loader;
import mclachlan.maze.data.Saver;
import mclachlan.maze.data.v1.V1Loader;
import mclachlan.maze.data.v1.V1Saver;
import mclachlan.maze.game.*;
import mclachlan.maze.map.script.FlavourTextEvent;
import mclachlan.maze.stat.*;
import mclachlan.maze.stat.combat.*;
import mclachlan.maze.stat.combat.event.ConditionEvent;
import mclachlan.maze.stat.condition.Condition;
import mclachlan.maze.stat.condition.ConditionTemplate;
import mclachlan.maze.stat.magic.MagicSys;
import mclachlan.maze.stat.magic.SpellEffect;
import mclachlan.maze.stat.npc.NpcFaction;
import mclachlan.maze.ui.diygui.Constants;
import mclachlan.maze.util.MazeException;

/**
 *
 */
public class MockCombat
{
	public static void main(String[] args) throws Exception
	{
		Loader loader = new V1Loader();
		Saver saver = new V1Saver();
		Database db = new Database(loader, saver);

		Maze maze = getMockMaze(loader);

		MockCombat mc = new MockCombat();

//		mc.singleTest(db, maze, "Hero", "Human", "Male", 5);

/*
		mc.referenceTestCombat(db, maze, "Hero", "Human", "Male", 10, 30);
		mc.referenceTestCombat(db, maze, "Warrior", "Human", "Male", 10, 30);
		mc.referenceTestCombat(db, maze, "Samurai", "Human", "Male", 10, 30);
		mc.referenceTestCombat(db, maze, "Paladin", "Human", "Male", 10, 30);
		mc.referenceTestCombat(db, maze, "Skald", "Human", "Male", 10, 30);
		mc.referenceTestCombat(db, maze, "Amazon", "Human", "Female", 10, 30);
		mc.referenceTestCombat(db, maze, "Sohei", "Human", "Male", 10, 30);
		mc.referenceTestCombat(db, maze, "Blackguard", "Human", "Male", 10, 30);
		mc.referenceTestCombat(db, maze, "Monk", "Human", "Male", 10, 30);
*/
		refTestCombatClasses(db, maze);
	}

	/*-------------------------------------------------------------------------*/
	public static Maze getMockMaze(Loader loader) throws Exception
	{
		Campaign campaign = Maze.getStubCampaign();
		loader.init(campaign);

		Maze maze = new Maze(Launcher.getConfig(), campaign);

		maze.initAudio(new MockAudioPlayer());
		maze.initLog(new SoutLog());
		maze.initState();
//		maze.initDb();
		maze.initSystems();
		maze.initUi(new HeadlessUi());
//		maze.startThreads();
		return maze;
	}

	/*-------------------------------------------------------------------------*/
	private static void refTestCombatClasses(final Database db, final Maze maze) throws Exception
	{
		List<String> classes = new ArrayList<String>();

		for (CharacterClass cc : db.getCharacterClasses().values())
		{
			if (cc.getFocus() == CharacterClass.Focus.COMBAT)
			{
				classes.add(cc.getName());
			}
		}

		System.out.println("classes = [" + classes + "]");

		ArrayList<Callable<List<CombatStatistics>>> tasks = new ArrayList<Callable<List<CombatStatistics>>>();
		for (final String className : classes)
		{
			tasks.add(new Callable<List<CombatStatistics>>()
			{
				public List<CombatStatistics> call() throws Exception
				{
					MockCombat mc = new MockCombat();
					List<CombatStatistics> result = mc.referenceTestCombat(db, maze, className, "Human", "Male", 100, 30);
					System.out.print(".");
					return result;
				}
			});
		}

		ExecutorService executorService = Executors.newSingleThreadExecutor();
		List<Future<List<CombatStatistics>>> futures = executorService.invokeAll(tasks);

		// wait for all to be done
		for (Future f : futures)
		{
			while (!f.isDone())
			{
				Thread.sleep(100);
			}
		}

		System.out.println("done!");

		// report results
		for (Future<List<CombatStatistics>> f : futures)
		{
			List<CombatStatistics> cs = f.get();
//			System.out.println(cs.get(0).getName());
//			for (int i=0; i<cs.size(); i++)
//			{
//				reportCombatStatistics(i+1, cs.get(i));
//			}

			reportCsv(cs);
		}

		executorService.shutdown();
	}

	/*-------------------------------------------------------------------------*/
	public CombatStatistics singleTest(
		Database db,
		Maze maze,
		String characterClass,
		String characterRace,
		String characterGender,
		int level)
	{
		CharacterBuilder cb = new CharacterBuilder(db);

		UnifiedActor pc = cb.buildCharacter("Tester", characterClass, characterRace, characterGender, level,
			new PriorityModifierApproach(
				Stats.Modifiers.BRAWN,
				Stats.Modifiers.SKILL,
				Stats.Modifiers.CUT,
				Stats.Modifiers.THRUST,
				Stats.Modifiers.SHOOT));

		PlayerParty party = new PlayerParty(Arrays.asList(pc));

		maze.setParty(party);

		GameState gs = new GameState(db.getZone("arena"), new DifficultyLevel(), new Point(), 0, 0,
			0, Arrays.asList("Tester"), 1, 0);
		maze.setGameState(gs);

		Foe foe = getReferenceFoeCombat(level);

		FoeGroup foeGroup = new FoeGroup();
		foeGroup.add(foe);

		ArrayList<FoeGroup> foeGroups = new ArrayList<FoeGroup>(Arrays.asList(foeGroup));

		Combat combat = runMockCombat(db, maze, party, foeGroups);

		for (UnifiedActor a : party.getActors())
		{
			System.out.println(a + ": " + a.getHitPoints() + "hp");
		}
		for (FoeGroup fg : foeGroups)
		{
			for (UnifiedActor a : fg.getActors())
			{
				System.out.println(a + ": " + a.getHitPoints() + "hp");
			}
		}

		System.out.println("Combat Stats");

		CombatStatistics s = combat.getCombatStatistics();
		System.out.println(s.getNrPlayerCharacters() + " player characters");
		System.out.println(" ave lvl " + s.getAveragePlayerCharacterLevel());
		System.out.println(s.getNrFoes() + " foes in " + s.getNrFoeGroups() + " groups");
		System.out.println(" ave lvl " + s.getAverageFoeLevel());
		System.out.println(" ave hp " + s.getAverageFoeHp());
		System.out.println(" ave sp " + s.getAverageFoeSp());
		System.out.println(" ave mp " + s.getAverageFoeMp());
		System.out.println("Ambush status: " + s.getAmbushStatus());
		System.out.println("Combat rounds: " + s.getCombatRounds());
		System.out.println("PC attacks: " + s.getNrPcAttacks());
		System.out.println(" " + s.getNrPcAttackHits() + " hit, " + s.getNrPcAttackMiss() + " miss");
		System.out.println("Foe attacks: " + s.getNrFoeAttacks());
		System.out.println(" " + s.getNrFoeAttackHits() + " hit, " + s.getNrFoeAttackMiss() + " miss");

		return s;
	}

	/*-------------------------------------------------------------------------*/

	/**
	 * For each character level, runs a number of 1v1 combats against the matching
	 * reference foe and reports the results
	 *
	 * @param nrTests
	 * 	The number of combats to run per level
	 * @param maxLevel
	 * 	the max level to test up to (starts at 1)
	 * @return
	 * 	a list of combat statistics (aggregated over combats in that level)
	 * 	indexed by level
	 */
	public List<CombatStatistics> referenceTestCombat(
		Database db,
		Maze maze,
		String characterClass,
		String characterRace,
		String characterGender,
		int nrTests,
		int maxLevel) throws Exception
	{
		CharacterBuilder cb = new CharacterBuilder(db);

		List<CombatStatistics> result = new ArrayList<CombatStatistics>();

		for (int level = 1; level <= maxLevel; level++)
		{
			CombatStatistics agg = new CombatStatistics(
				"Ref Test Combat: "+characterGender+" "+characterRace+" "+characterClass);

			for (int i = 0; i < nrTests; i++)
			{
				PlayerCharacter pc = cb.buildCharacter("Tester", characterClass, characterRace, characterGender, level,
					new PriorityModifierApproach(
						Stats.Modifiers.BRAWN,
						Stats.Modifiers.SKILL,
						Stats.Modifiers.CUT,
						Stats.Modifiers.THRUST,
						Stats.Modifiers.SHOOT));

				pc.setPrimaryWeapon(getReferenceWeapon(
					level,
					Stats.Modifiers.CUT,
					Stats.Modifiers.THRUST));

				pc.setHelm(getReferenceArmourCombat(level));
				pc.setTorsoArmour(getReferenceArmourCombat(level));
				pc.setLegArmour(getReferenceArmourCombat(level));
				pc.setGloves(getReferenceArmourCombat(level));
				pc.setBoots(getReferenceArmourCombat(level));

				PlayerParty party = new PlayerParty(Arrays.asList((UnifiedActor)pc));

				maze.setParty(party);

				GameState gs = new GameState(db.getZone("arena"), new DifficultyLevel(), new Point(), 0, 0,
					0, Arrays.asList("Tester"), 1, 0);
				maze.setGameState(gs);

				Foe foe = getReferenceFoeCombat(level);

				FoeGroup foeGroup = new FoeGroup();
				foeGroup.add(foe);

				ArrayList<FoeGroup> foeGroups = new ArrayList<FoeGroup>(Arrays.asList(foeGroup));

				Combat combat = runMockCombat(db, maze, party, foeGroups);
				CombatStatistics s = combat.getCombatStatistics();

				agg.aggregate(s);
			}

			result.add(agg);
		}

		return result;
	}

	/*-------------------------------------------------------------------------*/
	private static void reportCombatStatistics(int level, CombatStatistics s)
	{
		System.out.println(level + ": " + s.getNrPcVictories() + "/" + s.getNrCombats() + ", " +
			"php" + (int)s.getAveragePcHp() + "," +
			"fhp" + (int)s.getAverageFoeHp() + "," +
			"hpr" + (int)(100 * s.getAveragePcHp() / s.getAverageFoeHp()) + "%," +
			"#pa" + s.getNrPcAttacks() + ",par" + s.getAveragePcAttackRate() + "," +
			"ph" + (int)(100.0 * s.getNrPcAttackHits() / s.getNrPcAttacks()) + "%," +
			"pd" + (int)s.getAveragePcAttackDamage() + "," +
			"#fa" + s.getNrFoeAttacks() + "," +
			"fh" + (int)(100.0 * s.getNrFoeAttackHits() / s.getNrFoeAttacks()) + "%" +
			"fd" + (int)s.getAverageFoeAttackDamage() + ",");
	}

	/*-------------------------------------------------------------------------*/
	private static void reportCsv(List<CombatStatistics> cs)
	{
		System.out.print(cs.get(0).getName()+",");
		for (CombatStatistics s : cs)
		{
			System.out.print(1.0F * s.getNrPcVictories() / s.getNrCombats() + ",");
		}
		System.out.println();
	}

	/*-------------------------------------------------------------------------*/
	private static Item getReferenceWeapon(int level, String mod1, String mod2)
	{
		Dice damage = null;
		int toHit = 0;
		int toPenetrate = 0;
		int toCritical = 0;
		int toInitiative = 0;
		String[] attackTypes = null;
		GroupOfPossibilities<SpellEffect> spellEffects = null;
		int bonusAttacks = 0;
		int bonusStrikes = 0;

		int spellEffectLevel = 0;

		damage = new Dice(1, Math.max(level, 3), level/3);
		toPenetrate = 0;
		toInitiative = 0;
		attackTypes = new String[]{mod1, mod2};
		toCritical = 0;

		spellEffects = new GroupOfPossibilities<SpellEffect>();

		ItemTemplate result = new ItemTemplate(
			"Reference Weapon",
			"Reference Weapon",
			"Reference Weapon",
			ItemTemplate.Type.SHORT_WEAPON,
			ItemTemplate.WeaponSubType.SWORD,
			"Reference Weapon",
			StatModifier.NULL_STAT_MODIFIER,
			"item/defaultitem",
			new BitSet(),
			0,
			1,
			0,
			null,
			spellEffectLevel,
			Dice.d1,
			ItemTemplate.ChargesType.CHARGES_INFINITE,
			null,
			null,
			null,
			false,
			0,
			0,
			0,
			StatModifier.NULL_STAT_MODIFIER,
			StatModifier.NULL_STAT_MODIFIER,
			Database.getInstance().getScript("generic weapon swish"),
			damage,
			MagicSys.SpellEffectType.SLASHING,
			attackTypes,
			false,
			false,
			false,
			true,
			true,
			toHit,
			toPenetrate,
			toCritical,
			toInitiative,
			ItemTemplate.WeaponRange.MELEE,
			ItemTemplate.WeaponRange.MELEE,
			null,
			spellEffects,
			bonusAttacks,
			bonusStrikes,
			null,
			null,
			null,
			0,
			0,
			0,
			ItemTemplate.EnchantmentCalculation.STRAIGHT,
			null,
			null,
			0F);

		return new Item(result)
		{
			public String getDisplayName()
			{
				return "";
			}
		};
	}

	/*-------------------------------------------------------------------------*/
	private static Item getReferenceArmourCombat(int level)
	{
		Dice damage = null;
		int toHit = 0;
		int toPenetrate = 0;
		int toCritical = 0;
		int toInitiative = 0;
		String[] attackTypes = null;
		GroupOfPossibilities<SpellEffect> spellEffects = null;
		int bonusAttacks = 0;
		int bonusStrikes = 0;

		int spellEffectLevel = 0;

		damage = new Dice(1, Math.max(level, 3), level/3);
		toPenetrate = 0;
		toInitiative = 0;
		toCritical = 0;

		spellEffects = new GroupOfPossibilities<SpellEffect>();

		ItemTemplate result = new ItemTemplate(
			"Reference Armour",
			"Reference Armour",
			"Reference Armour",
			ItemTemplate.Type.SHORT_WEAPON,
			ItemTemplate.WeaponSubType.SWORD,
			"Reference Armour",
			StatModifier.NULL_STAT_MODIFIER,
			"item/defaultitem",
			new BitSet(),
			0,
			1,
			0,
			null,
			spellEffectLevel,
			Dice.d1,
			ItemTemplate.ChargesType.CHARGES_INFINITE,
			null,
			null,
			null,
			false,
			0,
			0,
			0,
			StatModifier.NULL_STAT_MODIFIER,
			StatModifier.NULL_STAT_MODIFIER,
			null,
			damage,
			MagicSys.SpellEffectType.SLASHING,
			attackTypes,
			false,
			false,
			false,
			true,
			true,
			toHit,
			toPenetrate,
			toCritical,
			toInitiative,
			ItemTemplate.WeaponRange.MELEE,
			ItemTemplate.WeaponRange.MELEE,
			null,
			spellEffects,
			bonusAttacks,
			bonusStrikes,
			null,
			null,
			null,
			Math.max(2, level/2),
			100,
			0,
			ItemTemplate.EnchantmentCalculation.STRAIGHT,
			null,
			null,
			0F);

		return new Item(result)
		{
			public String getDisplayName()
			{
				return "";
			}
		};
	}

	/*-------------------------------------------------------------------------*/
	private Combat runMockCombat(Database db, Maze maze,
		PlayerParty party, ArrayList<FoeGroup> foeGroups)
	{
		Combat combat = new Combat(party, foeGroups, null);

		runCombat(combat, party, foeGroups);
		return combat;
	}

	/*-------------------------------------------------------------------------*/

	/**
	 * @return true if the combats ends with a PC victory, false otherwise
	 */
	private boolean runCombat(
		Combat currentCombat,
		PlayerParty party,
		List<FoeGroup> foeGroups)
	{
		ArrayList<MazeEvent> evts = new ArrayList<MazeEvent>();
		evts.add(new FlavourTextEvent("ENCOUNTER!",
			Maze.getInstance().getUserConfig().getCombatDelay(), true));

		int avgFoeLevel = 0;
		for (FoeGroup fg : foeGroups)
		{
			avgFoeLevel += fg.getAverageLevel();
		}
		avgFoeLevel /= foeGroups.size();

		GameSys.getInstance().attemptManualIdentification(foeGroups, party, 0);

		switch (currentCombat.getAmbushStatus())
		{
			case PARTY_MAY_AMBUSH_OR_EVADE_FOES:
				evts.add(new FlavourTextEvent("Party may evade foes!", Maze.getInstance().getUserConfig().getCombatDelay(), true));
				break;
			case PARTY_MAY_AMBUSH_FOES:
				evts.add(new FlavourTextEvent("Party surprises foes!", Maze.getInstance().getUserConfig().getCombatDelay(), true));
				break;
			case FOES_MAY_AMBUSH_OR_EVADE_PARTY:
				Foe leader = GameSys.getInstance().getLeader(foeGroups);
				if (leader.shouldEvade(foeGroups, party))
				{
					// cancel the encounter
					return false;
				}
				else
				{
					evts.add(new FlavourTextEvent("Foes surprise party!", Maze.getInstance().getUserConfig().getCombatDelay(), true));
				}
				break;
			case FOES_MAY_AMBUSH_PARTY:
				evts.add(new FlavourTextEvent("Foes surprise party!", Maze.getInstance().getUserConfig().getCombatDelay(), true));
				break;
		}

		resolveEvents(evts);

		int combatRound = 1;
		while (party.numAlive() > 0 && getLiveFoes(foeGroups) > 0)
		{
			Maze.log("--- combat round " + combatRound + " starts ---");

			//--- foe intentions
			java.util.List<ActorActionIntention[]> foeIntentionList = new ArrayList<ActorActionIntention[]>();

			for (FoeGroup other : foeGroups)
			{
				foeIntentionList.add(getFoeCombatIntentions(other));
			}

			//--- player character intentions
			ActorActionIntention[] partyIntentions = new ActorActionIntention[party.getActors().size()];

			if (currentCombat.getAmbushStatus() == Combat.AmbushStatus.FOES_MAY_AMBUSH_PARTY ||
				currentCombat.getAmbushStatus() == Combat.AmbushStatus.FOES_MAY_AMBUSH_OR_EVADE_PARTY)
			{
				// party is surprised, cannot take action
				for (int i = 0; i < partyIntentions.length; i++)
				{
					partyIntentions[i] = ActorActionIntention.INTEND_NOTHING;
				}
			}
			else
			{
				int max = party.numAlive();
				int i = 0;
				while (i < max)
				{
					try
					{
						ActorActionIntention actorActionIntention = ActorActionIntention.INTEND_NOTHING;
						// dead characters should always be lined up at the end, so
						// a numAlive check works here
						if (i < party.numAlive())
						{
							// display character options
							PlayerCharacter pc = (PlayerCharacter)party.getActors().get(i);
							if (GameSys.getInstance().askActorForCombatIntentions(pc))
							{
								actorActionIntention = getCombatIntention(pc, foeGroups, currentCombat);
							}
							else
							{
								actorActionIntention = ActorActionIntention.INTEND_NOTHING;
							}
						}
						{
							partyIntentions[i++] = actorActionIntention;
						}
					}
					catch (Exception e)
					{
						StringBuilder sb = new StringBuilder();
						for (ActorActionIntention ci : partyIntentions)
						{
							sb.append("ci = [").append(ci).append("]");
						}
						throw new MazeException(sb.toString(), e);
					}
				}

			}

			Iterator combatActions = currentCombat.combatRound(partyIntentions, foeIntentionList);
			while (combatActions.hasNext())
			{
				CombatAction action = (CombatAction)combatActions.next();
				List<MazeEvent> events = currentCombat.resolveAction(action);

				resolveEvents(events);

				if (!checkPartyStatus(party))
				{
					return false;
				}

				party.reorderPartyToCompensateForDeadCharacters();

				if (currentCombat == null)
				{
					// this means that something has suddenly terminated the combat
					return false;
				}
				else
				{
					UnifiedActor actor = action.getActor();
					CurMaxSub hp = actor.getHitPoints();
					if (hp.getSub() >= hp.getCurrent() && hp.getCurrent() > 0)
					{
						ConditionTemplate kot = Database.getInstance().getConditionTemplate(
							Constants.Conditions.FATIGUE_KO);
						Condition ko = kot.create(
							actor, actor, 1, MagicSys.SpellEffectType.NONE, MagicSys.SpellEffectSubType.NONE);
						resolveEvent(new ConditionEvent(actor, ko), true);
					}
				}
			}

			resolveEvents(currentCombat.endRound());
//			this.ui.setFoes(foeGroups);

			// todo: reorder
/*			if (pendingPartyOrder != null)
			{
				this.reorderParty(pendingPartyOrder, pendingFormation);

				pendingPartyOrder = null;
				pendingFormation = -1;
			}*/

			GameSys.getInstance().attemptManualIdentification(foeGroups, party, combatRound);
			combatRound++;
//			incTurn(false);

			if (combatRound > 1000)
			{
//				System.out.println("combatRound "+combatRound);
				// endless combat, player loses
				return false;
			}
		}

		if (!checkPartyStatus(party))
		{
			return false;
		}
		party.reorderPartyToCompensateForDeadCharacters();
		currentCombat.endCombat();
		
		return true;
	}

	/*-------------------------------------------------------------------------*/

	/**
	 * Checks to determine if the party is still alive, or if it's GAME OVER.
	 */
	public static boolean checkPartyStatus(PlayerParty party)
	{
		if (party != null && party.numAlive() == 0)
		{
			// party is all dead; todo
			return false;
		}

		return true;
	}

	/*-------------------------------------------------------------------------*/
	private ActorActionIntention getCombatIntention(PlayerCharacter pc,
		List<FoeGroup> foeGroups, Combat currentCombat)
	{
		// todo:
		return new AttackIntention(foeGroups.get(0), currentCombat, pc.getAttackWithOptions().get(0));
	}

	/*-------------------------------------------------------------------------*/
	private ActorActionIntention[] getFoeCombatIntentions(FoeGroup foeGroup)
	{
		List<UnifiedActor> foes = foeGroup.getActors();
		ActorActionIntention[] result = new ActorActionIntention[foes.size()];

		for (int i = 0; i < foes.size(); i++)
		{
			result[i] = ((Foe)(foes.get(i))).getCombatIntention();
		}

		return result;
	}

	/*-------------------------------------------------------------------------*/
	private int getLiveFoes(List<FoeGroup> foes)
	{
		int sum = 0;
		for (ActorGroup g : foes)
		{
			sum += g.numAlive();
		}
		return sum;
	}

	/*-------------------------------------------------------------------------*/
	public void resolveEvents(List<MazeEvent> events)
	{
		resolveEvents(events, true);
	}

	/*-------------------------------------------------------------------------*/
	public static void resolveEvents(List<MazeEvent> events,
		boolean displayEventText)
	{
		if (events == null)
		{
			return;
		}

		for (MazeEvent event : events)
		{
			resolveEvent(event, displayEventText);
		}
	}

	/*-------------------------------------------------------------------------*/
	public static void resolveEvent(MazeEvent event, boolean displayEventText)
	{
		List<MazeEvent> subEvents = event.resolve();

		if (subEvents != null && !subEvents.isEmpty())
		{
			resolveEvents(subEvents, displayEventText);
		}
	}

	/*-------------------------------------------------------------------------*/
	private static Foe getReferenceFoeCombat(int level)
	{
		PercentageTable<BodyPart> bodyParts = new PercentageTable<BodyPart>();
		bodyParts.add(new BodyPart("bp", "bp", new StatModifier(), 0, 0, 0, EquipableSlot.Type.MISC_ITEM), 100);

		/*GameSys.getInstance().getNrAttacks(level/4+10,0,0),*/

		PercentageTable<String> playerBodyParts = new PercentageTable<String>();
		playerBodyParts.add("head", 18);
		playerBodyParts.add("torso", 33);
		playerBodyParts.add("leg", 31);
		playerBodyParts.add("hand", 8);
		playerBodyParts.add("foot", 10);

		// todo assuming COMBAT foe here
		StatModifier stats = new StatModifier();
		stats.setModifier(Stats.Modifiers.BRAWN, level);
		stats.setModifier(Stats.Modifiers.SKILL, level);

		stats.setModifier(Stats.Modifiers.RESIST_AIR, level*2);
		stats.setModifier(Stats.Modifiers.RESIST_EARTH, level*2);
		stats.setModifier(Stats.Modifiers.RESIST_ENERGY, level*2);
		stats.setModifier(Stats.Modifiers.RESIST_FIRE, level*2);
		stats.setModifier(Stats.Modifiers.RESIST_MENTAL, level*2);
		stats.setModifier(Stats.Modifiers.RESIST_WATER, level*2);
		stats.setModifier(Stats.Modifiers.RESIST_BLUDGEONING, level);
		stats.setModifier(Stats.Modifiers.RESIST_SLASHING, level);
		stats.setModifier(Stats.Modifiers.RESIST_PIERCING, level);

		FoeTemplate ft = new FoeTemplate(
			"TestingFoe",
			"TestingFoes",
			"???",
			"???s",
			"Test",
			new Dice(20, 1, level * 9),
			new Dice(20, 1, level * 9),
			new Dice(20, 1, level * 9),
			new Dice(level, 1, 0),
			level * 100,
			stats,
			bodyParts,
			playerBodyParts,
			null,
			null,
			null,
			null,
			null,
			null,
			Foe.EvasionBehaviour.NEVER_EVADE,
			true,
			0,
			new StatModifier(),
			new StatModifier(),
			false,
			0,
			Foe.StealthBehaviour.NOT_STEALTHY,
			null,
			false,
			null,
			null,
			null,
			null,
			null,
			CharacterClass.Focus.COMBAT,
			NpcFaction.Attitude.ATTACKING);

		return new Foe(ft);
	}

}
