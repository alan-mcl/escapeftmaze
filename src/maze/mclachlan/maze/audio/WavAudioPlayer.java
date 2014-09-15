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

package mclachlan.maze.audio;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.Map;
import java.util.HashMap;
import javax.sound.sampled.*;
import mclachlan.maze.data.Database;
import mclachlan.maze.util.MazeException;

/**
 *
 */
public class WavAudioPlayer implements AudioPlayer
{
	private Map<String, Clip> clips = new HashMap<String, Clip>();

	/*-------------------------------------------------------------------------*/
	public void playSound(String clipName)
	{
		Clip clip = Database.getInstance().getClip(clipName);
		clip.setMicrosecondPosition(0);
		clip.start();
	}
	
	/*-------------------------------------------------------------------------*/
	public synchronized Clip getClip(String clipName, InputStream stream)
	{
		Clip result = clips.get(clipName);
		
		if (result == null)
		{
			try
			{
				AudioInputStream ais = AudioSystem.getAudioInputStream(
					new BufferedInputStream(stream));
				
				AudioFormat af = ais.getFormat();
				
				Mixer mixer = AudioSystem.getMixer(null);
				
				DataLine.Info info = new DataLine.Info(javax.sound.sampled.Clip.class,
					ais.getFormat(), (int)ais.getFrameLength() * af.getFrameSize());
	
				result = (Clip)mixer.getLine(info);
				result.open(ais);
				
				clips.put(clipName, result);
			}
			catch (Exception x)
			{
				throw new MazeException(x);
			}
		}
		
		return result;
	}
}
