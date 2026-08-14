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

package mclachlan.maze.data.v2;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.*;
import mclachlan.maze.util.MazeException;

/**
 * Reads only the {@code metadata} object from a zone JSON file without
 * deserialising tiles or the Crusader map.
 */
public final class ZoneMetadataPeek
{
	private static final Type METADATA_TYPE = new TypeToken<Map<String, String>>()
	{
	}.getType();

	private ZoneMetadataPeek()
	{
	}

	/*-------------------------------------------------------------------------*/
	public static Map<String, String> readMetadata(File zoneFile)
	{
		if (!zoneFile.exists())
		{
			throw new MazeException("invalid: [" + zoneFile + "]");
		}

		try (JsonReader reader = new JsonReader(new InputStreamReader(
			new FileInputStream(zoneFile), StandardCharsets.UTF_8)))
		{
			if (reader.peek() != JsonToken.BEGIN_OBJECT)
			{
				return Collections.emptyMap();
			}

			reader.beginObject();
			while (reader.hasNext())
			{
				String name = reader.nextName();
				if ("metadata".equals(name))
				{
					if (reader.peek() == JsonToken.NULL)
					{
						reader.nextNull();
						return Collections.emptyMap();
					}
					Gson gson = new GsonBuilder().create();
					Map<String, String> metadata = gson.fromJson(reader, METADATA_TYPE);
					return metadata != null ? metadata : Collections.emptyMap();
				}
				reader.skipValue();
			}
			reader.endObject();
			return Collections.emptyMap();
		}
		catch (IOException e)
		{
			throw new MazeException(e);
		}
	}
}
