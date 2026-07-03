/*
 * Copyright (c) 2012 Alan McLachlan
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

package mclachlan.maze.audio;

import javax.sound.sampled.BooleanControl;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.Line;

/**
 * Shared volume control for Ogg playback lines.
 */
public final class AudioVolume
{
	private AudioVolume()
	{
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * Set the volume on an audio line by trying various controls.
	 *
	 * @param line the data line
	 * @param volume the volume between 0 and 100. 0 means mute
	 */
	public static void setVolume(Line line, int volume)
	{
		boolean muteFailed = false;
		try
		{
			if (line != null && line.isControlSupported(BooleanControl.Type.MUTE))
			{
				BooleanControl bc = (BooleanControl)line.getControl(BooleanControl.Type.MUTE);
				bc.setValue(volume == 0);
			}
			else
			{
				muteFailed = volume == 0;
			}
		}
		catch (Exception ex)
		{
			// some linux implementation throws exception
			muteFailed = volume == 0;
		}

		try
		{
			// try master gain
			if (line != null && line.isControlSupported(FloatControl.Type.MASTER_GAIN))
			{
				FloatControl fc = (FloatControl)line.getControl(FloatControl.Type.MASTER_GAIN);
				if (muteFailed)
				{
					// set to the lowest gain possible
					fc.setValue(fc.getMinimum());
				}
				else
				{
					fc.setValue(computeGain(fc, volume));
				}
				return;
			}
		}
		catch (Exception ex)
		{
			// some linux implementation throws exception, give up
		}

		try
		{
			// try volume
			if (line != null && line.isControlSupported(FloatControl.Type.VOLUME))
			{
				FloatControl fc = (FloatControl)line.getControl(FloatControl.Type.VOLUME);
				if (muteFailed)
				{
					fc.setValue(fc.getMinimum());
				}
				else
				{
					float low = fc.getMinimum();
					float high = fc.getMaximum();
					fc.setValue(low + (high - low) * volume / 100);
				}
			}
		}
		catch (Exception ex)
		{
			// some linux implementation throws exception, give up
		}
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * Compute the gain of the given gain control based on a linear mapping of the
	 * volume.
	 *
	 * @param fc The target float control
	 * @param volume the linear volume of 0..100
	 * @return the gain value
	 */
	public static float computeGain(FloatControl fc, int volume)
	{
		float min = Math.max(fc.getMinimum(), -43);
		float max = Math.min(fc.getMaximum(), 0);
		return min + (max - min) * volume / 100;
	}
}
