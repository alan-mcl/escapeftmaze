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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.*;
import mclachlan.maze.game.journal.Journal;
import mclachlan.maze.game.journal.JournalEntry;

/**
 *
 */
public class V1Journal
{
	private static String SECTION_HEADER = "-section-";
	private static String SECTION_FOOTER = "-end-section-";

	/*-------------------------------------------------------------------------*/
	public static Journal load(BufferedReader reader) throws Exception
	{
		// first row is the name
		String name = reader.readLine();

		Map<String, List<JournalEntry>> contents = new HashMap<String, List<JournalEntry>>();

		String line = reader.readLine();
		while (line != null)
		{
			if (SECTION_HEADER.equals(line))
			{
				String sectionName = reader.readLine();

				ArrayList<JournalEntry> entries = new ArrayList<JournalEntry>();
				contents.put(sectionName, entries);

				line = reader.readLine();
				while (!SECTION_FOOTER.equals(line))
				{
					entries.add(V1JournalEntry.fromString(line));
					line = reader.readLine();
				}
			}

			line = reader.readLine();
		}

		return new Journal(name, contents);
	}

	/*-------------------------------------------------------------------------*/
	public static void save(BufferedWriter bw, Journal journal) throws Exception
	{
		MyWriter writer = new MyWriter(bw);

		writer.writeln(journal.getName());

		Map<String, List<JournalEntry>> contents = journal.getContents();

		for (String sectionName : contents.keySet())
		{
			writer.writeln(SECTION_HEADER);
			writer.writeln(sectionName);

			List<JournalEntry> journalEntries = contents.get(sectionName);
			for (JournalEntry je : journalEntries)
			{
				writer.writeln(V1JournalEntry.toString(je));
			}

			writer.writeln(SECTION_FOOTER);
		}
	}

	/*-------------------------------------------------------------------------*/
	private static class MyWriter extends Writer
	{
		BufferedWriter writer;

		public MyWriter(BufferedWriter writer)
		{
			this.writer = writer;
		}

		private void writeln(String s) throws IOException
		{
			writer.write(s);
			writer.newLine();
		}

		public void close() throws IOException
		{
			writer.close();
		}

		public void flush() throws IOException
		{
			writer.flush();
		}

		public void write(char cbuf[], int off, int len) throws IOException
		{
			writer.write(cbuf, off, len);
		}

		public void writeln() throws IOException
		{
			writer.newLine();
		}
	}
}
