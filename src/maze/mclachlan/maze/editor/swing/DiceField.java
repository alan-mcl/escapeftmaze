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

package mclachlan.maze.editor.swing;

import javax.swing.*;
import mclachlan.maze.stat.Dice;
import mclachlan.maze.data.v1.V1Dice;

/**
 *
 */
public class DiceField extends JFormattedTextField
{
	/*-------------------------------------------------------------------------*/
	public DiceField()
	{
		setColumns(15);
		//todo: regex formatter
	}

	/*-------------------------------------------------------------------------*/
	public void setDice(Dice d)
	{
		setText(V1Dice.toString(d));
	}

	/*-------------------------------------------------------------------------*/
	public Dice getDice()
	{
		return V1Dice.fromString(getText());
	}
}
