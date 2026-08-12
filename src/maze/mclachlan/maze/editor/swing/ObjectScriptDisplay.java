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

import mclachlan.crusader.ObjectScript;
import mclachlan.crusader.script.JagObjectVertically;
import mclachlan.crusader.script.JagObjectWithinRadius;
import mclachlan.crusader.script.SinusoidalStretch;

/**
 * Editor-only display helper for Crusader object scripts.
 */
public final class ObjectScriptDisplay
{
	private ObjectScriptDisplay()
	{
	}

	public static String toString(ObjectScript script)
	{
		if (script == null)
		{
			return "";
		}

		if (script instanceof JagObjectVertically)
		{
			JagObjectVertically jv = (JagObjectVertically)script;
			return "jagY offset=" + jv.getMinOffset() + "-" + jv.getMaxOffset()
				+ " speed=" + jv.getMinSpeed() + "-" + jv.getMaxSpeed();
		}
		if (script instanceof JagObjectWithinRadius)
		{
			JagObjectWithinRadius jwr = (JagObjectWithinRadius)script;
			return "jagR radius=" + jwr.getMaxRadius()
				+ " speed=" + jwr.getMinSpeed() + "-" + jwr.getMaxSpeed();
		}
		if (script instanceof SinusoidalStretch)
		{
			SinusoidalStretch ss = (SinusoidalStretch)script;
			return "stretch " + ss.getMinStretch() + "-" + ss.getMaxStretch()
				+ " speed=" + ss.getSpeed()
				+ (ss.isVertical() ? " V" : "")
				+ (ss.isHorizontal() ? " H" : "");
		}
		return script.getClass().getName();
	}
}
