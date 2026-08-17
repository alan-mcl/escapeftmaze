/*
 * Copyright (c) 2026 Alan McLachlan
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
import mclachlan.maze.map.script.GrantItemsEvent;
import mclachlan.maze.stat.combat.Combat;
import mclachlan.maze.stat.*;
import mclachlan.maze.stat.npc.NpcFaction;
import mclachlan.maze.test.support.HeadlessMaze;
import mclachlan.maze.test.support.InMemoryLoader;
import mclachlan.maze.test.support.MazeTestSupport;
import mclachlan.maze.test.support.TestData;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Hermetic test that {@link HarnessUi} supplies attack intentions and auto-takes loot.
 */
public class HarnessCombatTest extends MazeTestSupport
{
	/*-------------------------------------------------------------------------*/
	@Test
	void harnessUiAttacksAndGrantsLoot() throws Exception
	{
		InMemoryLoader loader = new InMemoryLoader();
		loader.naturalWeapons.put("claw", TestData.clawNaturalWeapon());
		mclachlan.maze.stat.magic.SpellEffect martialArtsKo =
			new mclachlan.maze.stat.magic.SpellEffect();
		martialArtsKo.setName("MARTIAL_ARTS_KO");
		loader.spellEffects.put("MARTIAL_ARTS_KO", martialArtsKo);
		for (String script : new String[]{
			"generic weapon swish", "_WEAPON_HIT_", "_DIE_", "_ENCOUNTER_"})
		{
			loader.mazeScripts.put(script, new MazeScript(script, new ArrayList<>()));
		}

		Database db = TestData.buildDatabase(loader);
		HarnessUi ui = new HarnessUi();
		Maze maze = HeadlessMaze.boot(db, ui);

		seed(42L);

		PlayerCharacter pc = TestData.newCombatPc("Hero");
		PlayerParty party = new PlayerParty(new ArrayList<>(List.of((UnifiedActor)pc)));
		maze.setParty(party);

		Foe foe = TestData.referenceFoe(1, List.of("claw"));
		FoeGroup foeGroup = new FoeGroup();
		foeGroup.add(foe);
		List<FoeGroup> actors = new ArrayList<>();
		actors.add(foeGroup);

		maze.setCurrentActorEncounter(new ActorEncounter(
			actors,
			null,
			NpcFaction.Attitude.ATTACKING,
			Combat.AmbushStatus.NONE,
			false,
			null,
			null,
			null,
			null));
		maze.setGameStateNoZone(new GameState("arena", new DifficultyLevel(), new Point(),
			0, 0, 0, List.of("Hero"), 1, 0));
		maze.setZoneAndTileForTesting("arena", new Point(0, 0));

		int foeHpBefore = foe.getHitPoints().getCurrent();

		Combat combat = new Combat(party, actors, Combat.AmbushStatus.NONE);
		maze.setState(Maze.State.COMBAT);

		assertNotNull(maze.getCurrentCombat(), "combat should start");
		assertNotNull(foe.getCombatantData(), "foe should be registered with combat");

		maze.executeCombatRound(combat);
		CombatDriver.drainEvents(maze);

		assertTrue(foe.getHitPoints().getCurrent() < foeHpBefore
				|| pc.getHitPoints().getCurrent() < pc.getHitPoints().getMaximum(),
			"harness should fight, not defend forever");

		ItemTemplate template = new ItemTemplate();
		template.setName("Harness Token");
		template.setMaxItemsPerStack(10);
		template.setBaseCost(25);
		Item loot = new Item(template);

		int invBefore = pc.getInventory().getItems().size();
		maze.appendEvents(new GrantItemsEvent(loot));
		CombatDriver.drainEvents(maze);

		assertTrue(pc.getInventory().getItems().size() > invBefore,
			"grantItems should place loot in party inventory");
		assertEquals(25, ui.getLootBaseCostTotal());
	}
}
