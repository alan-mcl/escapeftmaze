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

import java.io.InputStream;
import java.util.*;
import javax.sound.sampled.BooleanControl;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.Line;
import mclachlan.maze.data.Database;
import mclachlan.maze.game.Log;
import mclachlan.maze.game.Maze;
import mclachlan.maze.util.MazeException;

/**
 * Background music player.
 */
public class Music
{
	private boolean enabled;

	/**
	 * The background playback thread.
	 */
	private PlaybackThread playbackThread;
	private final Object playbackMutex = new Object();

	/**
	 * The database.
	 */
	private final Database db;

	/**
	 * music player.
	 */
	private JCraftPlayer player;

	/**
	 * Flag for the current state of music.
	 */
	private String state;

	/*-------------------------------------------------------------------------*/
	public Music(Database db, boolean enabled)
	{
		this.db = db;

		this.enabled = enabled;
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * Stop the music playback.
	 */
	public void stop()
	{
		log("Music.stop");

		if (!enabled)
		{
			return;
		}

		if (player != null)
		{
			player.setPlaying(false);
			playbackThread.stopPlaying();
		}

		this.state = null;
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * Stop the music playback and close the playback thread.
	 */
	public void close()
	{
		log("Music.close");

		if (!enabled)
		{
			return;
		}

		stop();
		player = null;
		playbackThread = null;
	}

	/*-------------------------------------------------------------------------*/
	public String getState()
	{
		return state;
	}

	/*-------------------------------------------------------------------------*/
	public void setState(String state)
	{
		log("Music.setState ["+state+"]");
		this.state = state;
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * Play the given file list in the given sequence repeatedly.
	 *
	 * @param fileNames the array of file names to play
	 * @param volume the initial playback volume
	 */
	public void playLooped(final int volume, final String... fileNames)
	{
		log("Music.playLooped "+Arrays.asList(fileNames));

		if (!enabled)
		{
			return;
		}

		stop();
		playbackThread = new PlaybackThread(volume, true, fileNames);
		playbackThread.start();
	}

	/*-------------------------------------------------------------------------*/

	/**
	 * Set the linear volume.
	 *
	 * @param volume the volume 0..100, volume 0 mutes the sound
	 */
	public void setVolume(int volume)
	{
		if (!enabled)
		{
			return;
		}

		if (player != null)
		{
			setVolume(player.getOutputLine(), volume);
		}
	}

	/**
	 * @return is a music playing?
	 */
	public boolean isRunning()
	{
		if (!enabled)
		{
			return false;
		}

		return player.isPlaying();
	}

	public void setEnabled(boolean enabled)
	{
		if (!enabled)
		{
			stop();
		}
		
		this.enabled = enabled;
	}

	private void log(String s)
	{
		Maze.log(Log.DEBUG, s + ":" + Thread.currentThread().getName());
	}

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
					fc.setValue(AudioThread.computeGain(fc, volume));
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
	private class PlaybackThread extends Thread
	{
		private final int startingVolume;
		private boolean playing;
		private final boolean looping;
		private final Queue<String> tracks;

		public PlaybackThread(int startingVolume, boolean looped, String... tracks)
		{
			this.startingVolume = startingVolume;
			this.looping = looped;
			this.tracks = new LinkedList<>(Arrays.asList(tracks));
		}

		@Override
		public void run()
		{
			this.playing = true;

			while (playing)
			{
				try
				{
					// play the next track
					String track = tracks.poll();

					if (track != null)
					{
						try (InputStream is = db.getMusic(track))
						{
							player = new JCraftPlayer(is, playbackMutex, startingVolume);
							player.start();

							synchronized (playbackMutex)
							{
								playbackMutex.wait();
							}
						}
					}

					// playing has finished
					if (looping)
					{
						tracks.offer(track);
					}
				}
				catch (Exception e)
				{
					throw new MazeException(e);
				}
			}
		}

		public void stopPlaying()
		{
			playing = false;
			if (player != null)
			{
				player.setPlaying(false);
			}
			synchronized (playbackMutex)
			{
				playbackMutex.notifyAll();
			}
		}
	}
}