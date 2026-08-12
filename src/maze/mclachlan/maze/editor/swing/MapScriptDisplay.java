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

import mclachlan.crusader.MapScript;
import mclachlan.crusader.script.RandomLightingScript;
import mclachlan.crusader.script.SinusoidalLightingScript;
import mclachlan.maze.data.codec.CodecUtils;

/**
 * Editor-only display helper for Crusader map scripts.
 */
public final class MapScriptDisplay
{
	private MapScriptDisplay()
	{
	}

	public static String toString(MapScript script)
	{
		if (script == null)
		{
			return "";
		}

		if (script instanceof SinusoidalLightingScript)
		{
			SinusoidalLightingScript sls = (SinusoidalLightingScript)script;
			return "sine freq=" + sls.getFrequency()
				+ " light=" + sls.getMinLightLevel() + "-" + sls.getMaxLightLevel()
				+ " tiles=" + CodecUtils.toStringInts(sls.getAffectedTiles(), "/");
		}
		if (script instanceof RandomLightingScript)
		{
			RandomLightingScript rls = (RandomLightingScript)script;
			return "random freq=" + rls.getFrequency()
				+ " light=" + rls.getMinLightLevel() + "-" + rls.getMaxLightLevel()
				+ " tiles=" + CodecUtils.toStringInts(rls.getAffectedTiles(), "/");
		}
		return script.getClass().getName();
	}
}
