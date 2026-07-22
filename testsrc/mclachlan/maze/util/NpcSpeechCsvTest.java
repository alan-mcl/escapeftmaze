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

package mclachlan.maze.util;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.*;
import mclachlan.maze.data.Database;
import mclachlan.maze.stat.npc.*;
import mclachlan.maze.test.support.InMemoryLoader;
import mclachlan.maze.test.support.MazeTestSupport;
import mclachlan.maze.test.support.TestData;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link NpcSpeechCsv}.
 */
public class NpcSpeechCsvTest extends MazeTestSupport
{
	/*-------------------------------------------------------------------------*/
	@Test
	void roundTripSpecialCharacters() throws Exception
	{
		List<NpcSpeechCsv.SpeechCsvRow> rows = List.of(
			new NpcSpeechCsv.SpeechCsvRow(
				NpcSpeechCsv.OWNER_TYPE_NPC,
				"Gurney",
				9,
				new LinkedHashSet<>(List.of("deal", "trade", "buy")),
				"Line one.\nHe said \"hello\", friend."),
			new NpcSpeechCsv.SpeechCsvRow(
				NpcSpeechCsv.OWNER_TYPE_FOE_SPEECH,
				"gnomes",
				0,
				new LinkedHashSet<>(List.of("aenen")),
				"Simple reply"));

		StringWriter writer = new StringWriter();
		NpcSpeechCsv.write(writer, rows);
		List<NpcSpeechCsv.SpeechCsvRow> parsed =
			NpcSpeechCsv.read(new StringReader(writer.toString()));

		assertEquals(2, parsed.size());
		assertEquals(rows.get(0), parsed.get(0));
		assertEquals(rows.get(1), parsed.get(1));
	}

	/*-------------------------------------------------------------------------*/
	@Test
	void setDialoguePreservesStockPhrases()
	{
		NpcSpeech speech = new NpcSpeech();
		speech.setFriendlyGreeting("Hello friend.");
		speech.setDoesntKnowAbout("Never heard of '%s'.");

		speech.setDialogue(List.of(
			new NpcSpeechRow(5, Set.of("quest"), "Ask the mayor.")));

		assertEquals("Hello friend.", speech.getFriendlyGreeting());
		assertEquals("Never heard of '%s'.", speech.getDoesntKnowAbout());
		assertEquals(1, speech.getDialogue().size());
		assertEquals("Ask the mayor.", speech.getDialogue().iterator().next().getSpeech());
	}

	/*-------------------------------------------------------------------------*/
	@Test
	void applyGroupsMultiOwnerRows() throws Exception
	{
		InMemoryLoader loader = new InMemoryLoader();

		NpcTemplate npc = new NpcTemplate();
		npc.setName("test-npc");
		NpcSpeech npcDialogue = new NpcSpeech();
		npcDialogue.setFriendlyGreeting("Hi there.");
		npcDialogue.addNpcSpeechRow(new NpcSpeechRow(9, Set.of("old"), "Old line."));
		npc.setDialogue(npcDialogue);
		loader.npcTemplates.put(npc.getName(), npc);

		FoeSpeech foeSpeech = new FoeSpeech();
		foeSpeech.setName("test-foespeech");
		foeSpeech.setNeutralGreeting("Halt.");
		foeSpeech.setDialog(new NpcSpeech());
		loader.foeSpeech.put(foeSpeech.getName(), foeSpeech);

		Database db = TestData.buildDatabase(loader);

		List<NpcSpeechCsv.SpeechCsvRow> rows = List.of(
			new NpcSpeechCsv.SpeechCsvRow(
				NpcSpeechCsv.OWNER_TYPE_NPC,
				"test-npc",
				1,
				Set.of("quest"),
				"New quest line."),
			new NpcSpeechCsv.SpeechCsvRow(
				NpcSpeechCsv.OWNER_TYPE_FOE_SPEECH,
				"test-foespeech",
				2,
				Set.of("parley"),
				"We can talk."));

		NpcSpeechCsv.ApplyResult result = NpcSpeechCsv.applyToDatabase(db, rows);

		assertTrue(result.npcTemplatesDirty());
		assertTrue(result.foeSpeechDirty());
		assertEquals(2, result.ownersUpdated());
		assertTrue(result.unknownOwners().isEmpty());

		NpcSpeech updatedNpcDialogue = db.getNpcTemplates().get("test-npc").getDialogue();
		assertEquals("Hi there.", updatedNpcDialogue.getFriendlyGreeting());
		assertEquals(1, updatedNpcDialogue.getDialogue().size());
		assertEquals("New quest line.",
			updatedNpcDialogue.getDialogue().iterator().next().getSpeech());

		NpcSpeech updatedFoeDialogue =
			db.getFoeSpeeches().get("test-foespeech").getDialog();
		assertEquals("Halt.", db.getFoeSpeeches().get("test-foespeech").getNeutralGreeting());
		assertEquals(1, updatedFoeDialogue.getDialogue().size());
		assertEquals("We can talk.",
			updatedFoeDialogue.getDialogue().iterator().next().getSpeech());
	}

	/*-------------------------------------------------------------------------*/
	@Test
	void applySkipsUnknownOwners() throws Exception
	{
		Database db = TestData.buildEmptyDatabase();

		List<NpcSpeechCsv.SpeechCsvRow> rows = List.of(
			new NpcSpeechCsv.SpeechCsvRow(
				NpcSpeechCsv.OWNER_TYPE_NPC,
				"missing-npc",
				9,
				Set.of("hello"),
				"Nobody home."));

		NpcSpeechCsv.ApplyResult result = NpcSpeechCsv.applyToDatabase(db, rows);

		assertFalse(result.npcTemplatesDirty());
		assertFalse(result.foeSpeechDirty());
		assertEquals(0, result.ownersUpdated());
		assertEquals(List.of("npc:missing-npc"), result.unknownOwners());
	}

	/*-------------------------------------------------------------------------*/
	@Test
	void filterForOwnerKeepsMatchingRowsOnly()
	{
		List<NpcSpeechCsv.SpeechCsvRow> rows = List.of(
			new NpcSpeechCsv.SpeechCsvRow(
				NpcSpeechCsv.OWNER_TYPE_NPC,
				"Gurney",
				9,
				Set.of("trade"),
				"A"),
			new NpcSpeechCsv.SpeechCsvRow(
				NpcSpeechCsv.OWNER_TYPE_NPC,
				"Elsibet",
				9,
				Set.of("trade"),
				"B"));

		List<NpcSpeechCsv.SpeechCsvRow> filtered =
			NpcSpeechCsv.filterForOwner(rows, NpcSpeechCsv.OWNER_TYPE_NPC, "Gurney");

		assertEquals(1, filtered.size());
		assertEquals("A", filtered.get(0).speech());
	}
}
