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
import mclachlan.maze.data.Database;

/**
 * A Silo that stores a single object.
 *
 * @param <V>
 *    The type of the values in this silo
 */
public interface V2SiloSingleton<V>
{
	/**
	 * Load up this silo from the given input stream.
	 */
	V load(BufferedReader reader, Database database) throws Exception;

	/**
	 * Save this silo to the given output stream.
	 */
	void save(BufferedWriter writer, V obj) throws Exception;
}
