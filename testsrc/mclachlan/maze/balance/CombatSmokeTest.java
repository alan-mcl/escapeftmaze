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

package mclachlan.maze.balance;

import java.awt.Point;
import java.util.*;
import mclachlan.maze.data.Database;
import mclachlan.maze.game.*;
import mclachlan.maze.stat.*;
import mclachlan.maze.stat.combat.*;
import mclachlan.maze.test.support.HeadlessMaze;
import mclachlan.maze.test.support.InMemoryLoader;
import mclachlan.maze.test.support.MazeTestSupport;
import mclachlan.maze.test.support.TestData;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tier 4: a bounded, hermetic combat between a synthetic party and a synthetic
 * foe group, driven through the live combat engine via {@link HeadlessMaze}.
 * Asserts the combat terminates, emits events and applies damage, without
 * throwing.
 */
public class CombatSmokeTest extends MazeTestSupport
{
	/*-------------------------------------------------------------------------*/
	@Test
	void boundedCombatTerminatesAndAppliesDamage() throws Exception
	{
		InMemoryLoader loader = new InMemoryLoader();
		loader.naturalWeapons.put("claw", TestData.clawNaturalWeapon());
		// the combat statistics capture computes nr-attacks, which builds the
		// "unarmed" weapon and references this spell effect by name.
		mclachlan.maze.stat.magic.SpellEffect martialArtsKo =
			new mclachlan.maze.stat.magic.SpellEffect();
		martialArtsKo.setName("MARTIAL_ARTS_KO");
		loader.spellEffects.put("MARTIAL_ARTS_KO", martialArtsKo);
		// cosmetic scripts referenced by name during attack/death resolution
		for (String script : new String[]{
			"generic weapon swish", "_WEAPON_HIT_", "_DIE_"})
		{
			loader.mazeScripts.put(script, new MazeScript(script, new ArrayList<>()));
		}
		Database db = TestData.buildDatabase(loader);
		Maze maze = HeadlessMaze.boot(db);

		seed(42L);

		PlayerCharacter pc = TestData.newCombatPc("Hero");
		PlayerParty party = new PlayerParty(new ArrayList<>(List.of((UnifiedActor)pc)));
		maze.setParty(party);

		Foe foe = TestData.referenceFoe(1, List.of("claw"));
		FoeGroup foeGroup = new FoeGroup();
		foeGroup.add(foe);
		List<FoeGroup> foeGroups = new ArrayList<>(List.of(foeGroup));

		maze.setGameStateNoZone(new GameState("arena", new DifficultyLevel(), new Point(),
			0, 0, 0, List.of("Hero"), 1, 0));

		int foeHpBefore = foe.getHitPoints().getCurrent();

		Combat combat = new Combat(party, foeGroups, null);
		assertNotNull(combat.getAmbushStatus());

		int eventsResolved = runBoundedCombat(combat, party, foeGroups);

		assertTrue(eventsResolved > 0, "combat should resolve at least one event");
		assertTrue(party.numAlive() == 0 || liveFoes(foeGroups) == 0,
			"combat should terminate with one side defeated");

		// some damage should have been dealt somewhere
		int foeHpAfter = foe.getHitPoints().getCurrent();
		assertTrue(foeHpAfter < foeHpBefore || pc.getHitPoints().getCurrent() < pc.getHitPoints().getMaximum(),
			"combat should apply damage to someone");
	}

	/*-------------------------------------------------------------------------*/
	private int runBoundedCombat(Combat combat, PlayerParty party,
		List<FoeGroup> foeGroups)
	{
		int resolved = 0;
		int round = 0;

		while (party.numAlive() > 0 && liveFoes(foeGroups) > 0 && round < 100)
		{
			round++;

			List<ActorActionIntention[]> foeIntentions = new ArrayList<>();
			for (FoeGroup fg : foeGroups)
			{
				List<UnifiedActor> foes = fg.getActors();
				ActorActionIntention[] arr = new ActorActionIntention[foes.size()];
				for (int i = 0; i < foes.size(); i++)
				{
					arr[i] = ((Foe)foes.get(i)).getCombatIntention();
				}
				foeIntentions.add(arr);
			}

			ActorActionIntention[] partyIntentions =
				new ActorActionIntention[party.getActors().size()];
			for (int i = 0; i < partyIntentions.length; i++)
			{
				PlayerCharacter pc = (PlayerCharacter)party.getActors().get(i);
				List<AttackWith> options = pc.getAttackWithOptions();
				partyIntentions[i] = new AttackIntention(
					foeGroups.get(0), combat, options.get(0));
			}

			Iterator actions = combat.combatRound(partyIntentions, foeIntentions);
			while (actions.hasNext())
			{
				CombatAction action = (CombatAction)actions.next();
				List<MazeEvent> events = combat.resolveAction(action);
				resolved += resolveAll(events);
				party.reorderPartyToCompensateForDeadCharacters();

				if (party.numAlive() == 0 || liveFoes(foeGroups) == 0)
				{
					break;
				}
			}

			resolved += resolveAll(combat.endRound());
		}

		if (liveFoes(foeGroups) == 0)
		{
			combat.endCombat();
		}

		return resolved;
	}

	/*-------------------------------------------------------------------------*/
	private int resolveAll(List<MazeEvent> events)
	{
		if (events == null)
		{
			return 0;
		}

		int count = 0;
		for (MazeEvent event : events)
		{
			count++;
			count += resolveAll(event.resolve());
		}
		return count;
	}

	/*-------------------------------------------------------------------------*/
	private int liveFoes(List<FoeGroup> foeGroups)
	{
		int sum = 0;
		for (ActorGroup g : foeGroups)
		{
			sum += g.numAlive();
		}
		return sum;
	}
}
