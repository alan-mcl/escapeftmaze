/*
 * This file is part of Brewday.
 *
 * Brewday is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Brewday is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Brewday.  If not, see https://www.gnu.org/licenses.
 */

package mclachlan.maze.data.v2;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.*;
import mclachlan.maze.data.Database;

/**
 *
 */
public class SingletonSilo<T> implements V2SiloSingleton<T>
{
	private final V2SerialiserMap<T> serialiser;

	public SingletonSilo(V2SerialiserMap<T> serialiser)
	{
		this.serialiser = serialiser;
	}

	@Override
	public T load(BufferedReader reader, Database db) throws IOException
	{
		Map map = V2Utils.getMap(reader);
		return serialiser.fromObject(map, db);
	}

	@Override
	public void save(BufferedWriter writer, T t, Database db) throws IOException
	{
		Map map = serialiser.toObject(t, db);
		V2Utils.writeJson(map, writer);
	}
}
