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

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Streaming zone metadata peek must not require full Zone deserialisation.
 */
public class ZoneMetadataPeekTest
{
	@Test
	void readsMetadataWithoutLoadingTiles(@TempDir File tempDir) throws Exception
	{
		File zoneFile = new File(tempDir, "huge.zone.json");
		StringBuilder sb = new StringBuilder();
		sb.append("{\n  \"name\": \"huge\",\n  \"metadata\": {\n");
		sb.append("    \"fragment\": \"true\",\n");
		sb.append("    \"fragment.role\": \"flavour\"\n  },\n  \"tiles\": [\n");
		for (int i = 0; i < 500; i++)
		{
			sb.append("    [],\n");
		}
		sb.append("    []\n  ],\n  \"map\": {\"length\": \"1\", \"width\": \"1\"}\n}\n");
		Files.writeString(zoneFile.toPath(), sb.toString(), StandardCharsets.UTF_8);

		var metadata = ZoneMetadataPeek.readMetadata(zoneFile);

		assertEquals("true", metadata.get("fragment"));
		assertEquals("flavour", metadata.get("fragment.role"));
	}

	@Test
	void missingMetadataReturnsEmpty(@TempDir File tempDir) throws Exception
	{
		File zoneFile = new File(tempDir, "plain.json");
		Files.writeString(zoneFile.toPath(), "{\"name\":\"plain\",\"tiles\":[]}",
			StandardCharsets.UTF_8);

		assertTrue(ZoneMetadataPeek.readMetadata(zoneFile).isEmpty());
	}
}
