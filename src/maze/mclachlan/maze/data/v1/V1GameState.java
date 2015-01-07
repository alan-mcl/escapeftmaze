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

import java.awt.Point;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.util.*;
import mclachlan.maze.data.Database;
import mclachlan.maze.game.DifficultyLevel;
import mclachlan.maze.game.GameState;
import mclachlan.maze.map.Zone;

/**
 *
 */
public class V1GameState
{
	/*-------------------------------------------------------------------------*/
	public static GameState load(BufferedReader reader) throws Exception
	{
		Properties p = V1Utils.getProperties(reader);
		return fromProperties(p);
	}

	/*-------------------------------------------------------------------------*/
	public static void save(BufferedWriter writer, GameState gameState) throws Exception
	{
		writer.write(toProperties(gameState));
		writer.write("@");
		writer.newLine();
	}

	/*-------------------------------------------------------------------------*/
	private static String toProperties(GameState gameState)
	{
		StringBuilder b = new StringBuilder();

		b.append("zone=");
		b.append(gameState.getCurrentZone().getName());
		b.append(V1Utils.NEWLINE);
		b.append("difficultyLevel=");
		b.append(gameState.getDifficultyLevel().getName());
		b.append(V1Utils.NEWLINE);
		b.append("playerPos=");
		b.append(V1Point.toString(gameState.getPlayerPos()));
		b.append(V1Utils.NEWLINE);
		b.append("facing=");
		b.append(gameState.getFacing());
		b.append(V1Utils.NEWLINE);
		b.append("partyGold=");
		b.append(gameState.getPartyGold());
		b.append(V1Utils.NEWLINE);
		b.append("partySupplies=");
		b.append(gameState.getPartySupplies());
		b.append(V1Utils.NEWLINE);
		b.append("partyNames=");
		b.append(V1Utils.stringList.toString(gameState.getPartyNames()));
		b.append(V1Utils.NEWLINE);
		b.append("formation=");
		b.append(gameState.getFormation());
		b.append(V1Utils.NEWLINE);
		b.append("turnNr=");
		b.append(gameState.getTurnNr());
		b.append(V1Utils.NEWLINE);

		return b.toString();
	}

	/*-------------------------------------------------------------------------*/
	static GameState fromProperties(Properties p) throws Exception
	{
		Zone zone = Database.getInstance().getZone(p.getProperty("zone"));
		DifficultyLevel dl = Database.getInstance().getDifficultyLevels().get(p.getProperty("difficultyLevel"));
		Point playerPos = V1Point.fromString(p.getProperty("playerPos"));
		int facing = Integer.parseInt(p.getProperty("facing"));
		int partyGold = Integer.parseInt(p.getProperty("partyGold"));
		int partySupplies = Integer.parseInt(p.getProperty("partySupplies"));
		List<String> partyNames = V1Utils.stringList.fromString(p.getProperty("partyNames"));
		int formation = Integer.parseInt(p.getProperty("formation"));
		long turnNr = Long.parseLong(p.getProperty("turnNr"));

		return new GameState(zone, dl, playerPos, facing, partyGold, partySupplies, partyNames, formation, turnNr);
	}
}
