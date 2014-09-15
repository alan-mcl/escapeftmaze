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

package mclachlan.maze.editor.swing.map;

import java.awt.Frame;
import java.awt.HeadlessException;
import java.awt.event.ActionListener;
import mclachlan.crusader.MapScript;
import mclachlan.crusader.script.RandomLightingScript;
import mclachlan.crusader.script.SinusoidalLightingScript;
import mclachlan.maze.editor.swing.MapScriptEditor;
import mclachlan.maze.util.MazeException;

/**
 *
 */
public class ToolMapScriptEditor extends MapScriptEditor implements ActionListener
{
	/*-------------------------------------------------------------------------*/
	public ToolMapScriptEditor(Frame owner, MapScript mapScript, int dirtyFlag)
		throws HeadlessException
	{
		super(owner, mapScript, dirtyFlag);

		randomLightScriptTiles.setEditable(false);
		sineLightScriptTiles.setEditable(false);
	}

	/*-------------------------------------------------------------------------*/
	public MapScript getNewResult(int[] tiles)
	{
		int srType = type.getSelectedIndex();
		switch (srType)
		{
			case CUSTOM:
				try
				{
					Class clazz = Class.forName(impl.getText());
					this.result = (MapScript)clazz.newInstance();
				}
				catch (Exception x)
				{
					throw new MazeException(x);
				}
				break;
			case RANDOM_LIGHTING:
				result = new RandomLightingScript(
					tiles,
					(Integer)randomLightScriptFreq.getValue(),
					(Integer)randomLightScriptMinLightLevel.getValue(),
					(Integer)randomLightScriptMaxLightLevel.getValue());
				break;
			case SINE_LIGHTING:
				result = new SinusoidalLightingScript(
					tiles,
					(Integer)sineLightScriptFreq.getValue(),
					(Integer)sineLightScriptMinLightLevel.getValue(),
					(Integer)sineLightScriptMaxLightLevel.getValue());
				break;
			default: throw new MazeException("Invalid type "+srType);
		}

		return result;
	}
}