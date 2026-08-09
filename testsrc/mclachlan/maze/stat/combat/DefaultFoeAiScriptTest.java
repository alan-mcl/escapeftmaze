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

package mclachlan.maze.stat.combat;

import java.util.*;
import mclachlan.maze.data.Database;
import mclachlan.maze.game.ActorEncounter;
import mclachlan.maze.game.Maze;
import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.game.MazeScript;
import mclachlan.maze.stat.*;
import mclachlan.maze.stat.npc.*;
import mclachlan.maze.test.support.HeadlessMaze;
import mclachlan.maze.test.support.InMemoryLoader;
import mclachlan.maze.test.support.MazeTestSupport;
import mclachlan.maze.test.support.TestData;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link DefaultFoeAiScript} interpreting {@link FoeInteraction} data.
 */
public class DefaultFoeAiScriptTest extends MazeTestSupport
{
	/*-------------------------------------------------------------------------*/
	@Test
	void greetingUsesInteractionScript() throws Exception
	{
		FoeInteraction interaction = new FoeInteraction();
		interaction.setNeutralGreeting(new MazeScript(
			"test.neutralGreeting",
			List.of(new NpcSpeechEvent("Halt.", null))));

		ScriptHarness harness = bootHarness(interaction, NpcFaction.Attitude.NEUTRAL);

		List<MazeEvent> events = harness.script().firstGreeting();

		assertNotNull(events);
		assertEquals(1, events.size());
		assertInstanceOf(NpcSpeechEvent.class, events.get(0));
		assertEquals("Halt.", ((NpcSpeechEvent)events.get(0)).getSpeechText());
	}

	/*-------------------------------------------------------------------------*/
	@Test
	void farewellAppendsActorsLeaveEvent() throws Exception
	{
		FoeInteraction interaction = new FoeInteraction();
		interaction.setNeutralFarewell(new MazeScript(
			"test.neutralFarewell",
			List.of(new NpcSpeechEvent("Goodbye.", null))));

		ScriptHarness harness = bootHarness(interaction, NpcFaction.Attitude.NEUTRAL);

		List<MazeEvent> events = harness.script().partyLeavesNeutral();

		assertEquals(2, events.size());
		assertInstanceOf(NpcSpeechEvent.class, events.get(0));
		assertInstanceOf(ActorsLeaveEvent.class, events.get(1));
	}

	/*-------------------------------------------------------------------------*/
	@Test
	void givenItemRunsInteractionScriptWhenNameMatches() throws Exception
	{
		FoeInteraction interaction = new FoeInteraction();
		interaction.setGivenItemName("Test Token");
		interaction.setGivenItemScript(new MazeScript(
			"test.givenItemScript",
			List.of(new NpcSpeechEvent("Accepted.", null))));

		ScriptHarness harness = bootHarness(interaction, NpcFaction.Attitude.NEUTRAL);
		Item item = testItem("Test Token");
		PlayerCharacter pc = TestData.newCombatPc("Hero");

		List<MazeEvent> events = harness.script().givenItemByParty(pc, item);

		assertEquals(1, events.size());
		assertInstanceOf(NpcSpeechEvent.class, events.get(0));
		assertEquals("Accepted.", ((NpcSpeechEvent)events.get(0)).getSpeechText());
	}

	/*-------------------------------------------------------------------------*/
	@Test
	void givenItemFallsBackWhenNameDoesNotMatch() throws Exception
	{
		FoeInteraction interaction = new FoeInteraction();
		interaction.setGivenItemName("Expected Token");
		interaction.setGivenItemScript(new MazeScript(
			"test.givenItemScript",
			List.of(new NpcSpeechEvent("Wrong branch.", null))));

		ScriptHarness harness = bootHarness(interaction, NpcFaction.Attitude.NEUTRAL);
		Item item = testItem("Other Token");
		PlayerCharacter pc = TestData.newCombatPc("Hero");
		pc.getInventory().add(item);

		List<MazeEvent> events = harness.script().givenItemByParty(pc, item);

		assertNotNull(events);
		assertFalse(events.isEmpty());
		assertFalse(events.stream().anyMatch(e ->
			e instanceof NpcSpeechEvent nse && "Wrong branch.".equals(nse.getSpeechText())));
	}

	/*-------------------------------------------------------------------------*/
	private static ScriptHarness bootHarness(
		FoeInteraction interaction,
		NpcFaction.Attitude attitude) throws Exception
	{
		InMemoryLoader loader = new InMemoryLoader();
		Database db = TestData.buildDatabase(loader);
		Maze maze = HeadlessMaze.boot(db);

		FoeTemplate template = TestData.referenceFoe(1).getFoeTemplate();
		template.setFoeInteraction(interaction);
		Foe foe = new Foe(template);

		ActorEncounter encounter = new ActorEncounter(
			List.of(new FoeGroup(List.of(foe))),
			null,
			attitude,
			Combat.AmbushStatus.NONE,
			false,
			null,
			null,
			null,
			null);
		maze.setCurrentActorEncounter(encounter);

		return new ScriptHarness(new DefaultFoeAiScript(encounter));
	}

	private static Item testItem(String name)
	{
		ItemTemplate template = new ItemTemplate();
		template.setName(name);
		template.setMaxItemsPerStack(10);
		return new Item(template);
	}

	private record ScriptHarness(DefaultFoeAiScript script) { }
}
