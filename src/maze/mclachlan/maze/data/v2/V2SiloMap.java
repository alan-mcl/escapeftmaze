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
import java.util.*;
import mclachlan.maze.data.Database;

/**
 * A Silo that stores a map of Strings to complex types.
 *
 * @param <V>
 *    The type of the values in this silo
 */
public interface V2SiloMap<V extends V2DataObject>
{
	/**
	 * Load up this silo from the given input stream.
	 */
	Map<String,V> load(BufferedReader reader, Database db) throws Exception;

	/**
	 * Save this silo to the given output stream.
	 */
	void save(BufferedWriter writer, Map<String,V> map, Database db) throws Exception;
}
