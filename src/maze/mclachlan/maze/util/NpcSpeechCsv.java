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

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.*;
import mclachlan.maze.data.Database;
import mclachlan.maze.data.v1.V1Utils;
import mclachlan.maze.stat.npc.*;

/**
 * RFC 4180 CSV import/export for keyword dialogue rows on NPC templates and
 * FoeSpeech packs.
 */
public class NpcSpeechCsv
{
	public static final String OWNER_TYPE_NPC = "npc";
	public static final String OWNER_TYPE_FOE_SPEECH = "foespeech";

	private static final String[] HEADER =
		{"owner_type", "owner_name", "priority", "keywords", "speech"};

	/*-------------------------------------------------------------------------*/
	public record SpeechCsvRow(
		String ownerType,
		String ownerName,
		int priority,
		Set<String> keywords,
		String speech)
	{
	}

	/*-------------------------------------------------------------------------*/
	public record ApplyResult(
		boolean npcTemplatesDirty,
		boolean foeSpeechDirty,
		int ownersUpdated,
		List<String> unknownOwners)
	{
	}

	/*-------------------------------------------------------------------------*/
	private NpcSpeechCsv()
	{
	}

	/*-------------------------------------------------------------------------*/
	public static List<SpeechCsvRow> exportAll(Database db)
	{
		List<SpeechCsvRow> rows = new ArrayList<>();

		for (NpcTemplate npc : db.getNpcTemplates().values())
		{
			if (npc.getDialogue() != null)
			{
				rows.addAll(exportOne(OWNER_TYPE_NPC, npc.getName(), npc.getDialogue()));
			}
		}

		for (FoeSpeech foeSpeech : db.getFoeSpeeches().values())
		{
			if (foeSpeech.getDialog() != null)
			{
				rows.addAll(exportOne(
					OWNER_TYPE_FOE_SPEECH,
					foeSpeech.getName(),
					foeSpeech.getDialog()));
			}
		}

		sortRows(rows);
		return rows;
	}

	/*-------------------------------------------------------------------------*/
	public static List<SpeechCsvRow> exportOne(
		String ownerType,
		String ownerName,
		NpcSpeech npcSpeech)
	{
		List<SpeechCsvRow> rows = new ArrayList<>();

		if (npcSpeech == null)
		{
			return rows;
		}

		for (NpcSpeechRow row : npcSpeech.getDialogue())
		{
			rows.add(toCsvRow(ownerType, ownerName, row));
		}

		sortRows(rows);
		return rows;
	}

	/*-------------------------------------------------------------------------*/
	public static List<SpeechCsvRow> exportRows(
		String ownerType,
		String ownerName,
		List<NpcSpeechRow> dialogue)
	{
		List<SpeechCsvRow> rows = new ArrayList<>();

		for (NpcSpeechRow row : dialogue)
		{
			rows.add(toCsvRow(ownerType, ownerName, row));
		}

		sortRows(rows);
		return rows;
	}

	/*-------------------------------------------------------------------------*/
	public static ApplyResult applyToDatabase(Database db, List<SpeechCsvRow> rows)
	{
		Map<String, List<SpeechCsvRow>> grouped = new LinkedHashMap<>();

		for (SpeechCsvRow row : rows)
		{
			String key = row.ownerType() + "\0" + row.ownerName();
			grouped.computeIfAbsent(key, k -> new ArrayList<>()).add(row);
		}

		boolean npcDirty = false;
		boolean foeSpeechDirty = false;
		int ownersUpdated = 0;
		List<String> unknownOwners = new ArrayList<>();

		for (List<SpeechCsvRow> ownerRows : grouped.values())
		{
			SpeechCsvRow first = ownerRows.get(0);
			List<NpcSpeechRow> dialogue = ownerRows.stream()
				.map(NpcSpeechCsv::toNpcSpeechRow)
				.collect(Collectors.toList());

			if (OWNER_TYPE_NPC.equals(first.ownerType()))
			{
				NpcTemplate npc = db.getNpcTemplates().get(first.ownerName());
				if (npc == null)
				{
					unknownOwners.add(first.ownerType() + ":" + first.ownerName());
					continue;
				}

				NpcSpeech speech = npc.getDialogue();
				if (speech == null)
				{
					speech = new NpcSpeech();
					npc.setDialogue(speech);
				}
				speech.setDialogue(dialogue);
				npcDirty = true;
				ownersUpdated++;
			}
			else if (OWNER_TYPE_FOE_SPEECH.equals(first.ownerType()))
			{
				FoeSpeech foeSpeech = db.getFoeSpeeches().get(first.ownerName());
				if (foeSpeech == null)
				{
					unknownOwners.add(first.ownerType() + ":" + first.ownerName());
					continue;
				}

				NpcSpeech dialog = foeSpeech.getDialog();
				if (dialog == null)
				{
					dialog = new NpcSpeech();
					foeSpeech.setDialog(dialog);
				}
				dialog.setDialogue(dialogue);
				foeSpeechDirty = true;
				ownersUpdated++;
			}
			else
			{
				throw new MazeException("Unknown owner_type: " + first.ownerType());
			}
		}

		return new ApplyResult(npcDirty, foeSpeechDirty, ownersUpdated, unknownOwners);
	}

	/*-------------------------------------------------------------------------*/
	public static void write(Writer writer, List<SpeechCsvRow> rows) throws IOException
	{
		writeRecord(writer, HEADER);
		writer.write('\n');

		for (SpeechCsvRow row : rows)
		{
			writeRecord(writer, new String[] {
				row.ownerType(),
				row.ownerName(),
				Integer.toString(row.priority()),
				keywordsToString(row.keywords()),
				row.speech() == null ? "" : row.speech()
			});
			writer.write('\n');
		}
	}

	/*-------------------------------------------------------------------------*/
	public static List<SpeechCsvRow> read(Reader reader) throws IOException
	{
		List<List<String>> records = parseCsv(reader);
		if (records.isEmpty())
		{
			return List.of();
		}

		List<String> header = records.get(0);
		validateHeader(header);

		List<SpeechCsvRow> rows = new ArrayList<>();
		for (int i = 1; i < records.size(); i++)
		{
			List<String> record = records.get(i);
			if (record.isEmpty() || record.stream().allMatch(String::isEmpty))
			{
				continue;
			}
			rows.add(parseRow(record, i + 1));
		}

		return rows;
	}

	/*-------------------------------------------------------------------------*/
	public static List<SpeechCsvRow> filterForOwner(
		List<SpeechCsvRow> rows,
		String ownerType,
		String ownerName)
	{
		return rows.stream()
			.filter(r -> ownerType.equals(r.ownerType())
				&& ownerName.equals(r.ownerName()))
			.collect(Collectors.toList());
	}

	/*-------------------------------------------------------------------------*/
	public static List<NpcSpeechRow> toNpcSpeechRows(List<SpeechCsvRow> rows)
	{
		return rows.stream()
			.map(NpcSpeechCsv::toNpcSpeechRow)
			.collect(Collectors.toList());
	}

	/*-------------------------------------------------------------------------*/
	private static SpeechCsvRow toCsvRow(
		String ownerType,
		String ownerName,
		NpcSpeechRow row)
	{
		return new SpeechCsvRow(
			ownerType,
			ownerName,
			row.getPriority(),
			new LinkedHashSet<>(row.getKeywords()),
			row.getSpeech());
	}

	/*-------------------------------------------------------------------------*/
	private static NpcSpeechRow toNpcSpeechRow(SpeechCsvRow row)
	{
		return new NpcSpeechRow(row.priority(), new LinkedHashSet<>(row.keywords()), row.speech());
	}

	/*-------------------------------------------------------------------------*/
	private static void sortRows(List<SpeechCsvRow> rows)
	{
		rows.sort(Comparator
			.comparing(SpeechCsvRow::ownerType)
			.thenComparing(SpeechCsvRow::ownerName)
			.thenComparing(r -> keywordsToString(r.keywords())));
	}

	/*-------------------------------------------------------------------------*/
	static String keywordsToString(Set<String> keywords)
	{
		if (keywords == null || keywords.isEmpty())
		{
			return "";
		}

		String[] arr = keywords.toArray(new String[keywords.size()]);
		Arrays.sort(arr);
		return V1Utils.toStringStrings(arr, ",");
	}

	/*-------------------------------------------------------------------------*/
	static Set<String> parseKeywords(String keywords, int lineNumber)
	{
		if (keywords == null || keywords.isEmpty())
		{
			throw new MazeException("Line " + lineNumber + ": keywords must not be empty");
		}

		Set<String> result = new LinkedHashSet<>();
		for (String part : keywords.split(","))
		{
			String trimmed = part.trim();
			if (!trimmed.isEmpty())
			{
				result.add(trimmed);
			}
		}

		if (result.isEmpty())
		{
			throw new MazeException("Line " + lineNumber + ": keywords must not be empty");
		}

		return result;
	}

	/*-------------------------------------------------------------------------*/
	private static SpeechCsvRow parseRow(List<String> record, int lineNumber)
	{
		if (record.size() != HEADER.length)
		{
			throw new MazeException("Line " + lineNumber + ": expected "
				+ HEADER.length + " columns, got " + record.size());
		}

		String ownerType = record.get(0).trim();
		if (!OWNER_TYPE_NPC.equals(ownerType) && !OWNER_TYPE_FOE_SPEECH.equals(ownerType))
		{
			throw new MazeException("Line " + lineNumber + ": unknown owner_type: "
				+ ownerType);
		}

		String ownerName = record.get(1).trim();
		if (ownerName.isEmpty())
		{
			throw new MazeException("Line " + lineNumber + ": owner_name must not be empty");
		}

		int priority;
		try
		{
			priority = Integer.parseInt(record.get(2).trim());
		}
		catch (NumberFormatException e)
		{
			throw new MazeException("Line " + lineNumber + ": invalid priority: "
				+ record.get(2));
		}

		Set<String> keywords = parseKeywords(record.get(3), lineNumber);
		String speech = record.get(4);

		return new SpeechCsvRow(ownerType, ownerName, priority, keywords, speech);
	}

	/*-------------------------------------------------------------------------*/
	private static void validateHeader(List<String> header)
	{
		if (header.size() != HEADER.length)
		{
			throw new MazeException("Invalid CSV header: expected "
				+ Arrays.toString(HEADER));
		}

		for (int i = 0; i < HEADER.length; i++)
		{
			if (!HEADER[i].equals(header.get(i)))
			{
				throw new MazeException("Invalid CSV header: expected "
					+ Arrays.toString(HEADER));
			}
		}
	}

	/*-------------------------------------------------------------------------*/
	private static void writeRecord(Writer writer, String[] fields) throws IOException
	{
		for (int i = 0; i < fields.length; i++)
		{
			if (i > 0)
			{
				writer.write(',');
			}
			writeField(writer, fields[i]);
		}
	}

	/*-------------------------------------------------------------------------*/
	private static void writeField(Writer writer, String value) throws IOException
	{
		if (value == null)
		{
			value = "";
		}

		boolean needsQuotes = value.indexOf(',') >= 0
			|| value.indexOf('"') >= 0
			|| value.indexOf('\n') >= 0
			|| value.indexOf('\r') >= 0;

		if (needsQuotes)
		{
			writer.write('"');
			writer.write(value.replace("\"", "\"\""));
			writer.write('"');
		}
		else
		{
			writer.write(value);
		}
	}

	/*-------------------------------------------------------------------------*/
	static List<List<String>> parseCsv(Reader reader) throws IOException
	{
		List<List<String>> records = new ArrayList<>();
		List<String> currentRecord = new ArrayList<>();
		StringBuilder field = new StringBuilder();
		boolean inQuotes = false;
		PushbackReader in = new PushbackReader(reader, 1);

		int c;
		while ((c = in.read()) != -1)
		{
			char ch = (char)c;

			if (inQuotes)
			{
				if (ch == '"')
				{
					int next = in.read();
					if (next == '"')
					{
						field.append('"');
					}
					else
					{
						inQuotes = false;
						if (next != -1)
						{
							in.unread(next);
						}
					}
				}
				else
				{
					field.append(ch);
				}
			}
			else if (ch == '"')
			{
				inQuotes = true;
			}
			else if (ch == ',')
			{
				currentRecord.add(field.toString());
				field.setLength(0);
			}
			else if (ch == '\r')
			{
				int next = in.read();
				if (next != '\n' && next != -1)
				{
					in.unread(next);
				}
				currentRecord.add(field.toString());
				field.setLength(0);
				records.add(currentRecord);
				currentRecord = new ArrayList<>();
			}
			else if (ch == '\n')
			{
				currentRecord.add(field.toString());
				field.setLength(0);
				records.add(currentRecord);
				currentRecord = new ArrayList<>();
			}
			else
			{
				field.append(ch);
			}
		}

		if (inQuotes)
		{
			throw new MazeException("Unterminated quoted field in CSV");
		}

		if (field.length() > 0 || !currentRecord.isEmpty())
		{
			currentRecord.add(field.toString());
			records.add(currentRecord);
		}

		return records;
	}

	/*-------------------------------------------------------------------------*/
	public static void writeFile(File file, List<SpeechCsvRow> rows) throws IOException
	{
		try (Writer writer = new OutputStreamWriter(
			new FileOutputStream(file), StandardCharsets.UTF_8))
		{
			write(writer, rows);
		}
	}

	/*-------------------------------------------------------------------------*/
	public static List<SpeechCsvRow> readFile(File file) throws IOException
	{
		try (Reader reader = new InputStreamReader(
			new FileInputStream(file), StandardCharsets.UTF_8))
		{
			return read(reader);
		}
	}
}
