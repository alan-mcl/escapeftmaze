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

import java.util.Arrays;
import mclachlan.maze.data.Database;
import mclachlan.maze.game.Log;
import mclachlan.maze.game.Maze;

/**
 * Background music facade. Playback is delegated to {@link OggAudioPlayer}.
 */
public class Music
{
	private boolean enabled;
	private int volume = 100;

	/**
	 * The database (retained for construction compatibility).
	 */
	private final Database db;

	/**
	 * Flag for the current state of music.
	 */
	private String state;

	/** Deferred playback when {@link Maze#initAudio} has not run yet. */
	private String[] pendingTracks;
	private int pendingVolume = -1;

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

		OggAudioPlayer player = engineOrNull();
		if (player != null)
		{
			player.stopMusic();
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
	 * @return true if playback started (or was deferred until audio init)
	 */
	public boolean playLooped(final int volume, final String... fileNames)
	{
		log("Music.playLooped "+Arrays.asList(fileNames));

		if (!enabled)
		{
			return false;
		}

		OggAudioPlayer player = engineOrNull();
		if (player == null)
		{
			this.pendingVolume = volume;
			this.pendingTracks = fileNames;
			log("Music.playLooped deferred: audio not initialised");
			return true;
		}

		clearPending();
		player.playMusicLooped(volume, fileNames);
		return true;
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * Set the linear volume.
	 *
	 * @param volume the volume 0..100, volume 0 mutes the sound
	 */
	public void setVolume(int volume)
	{
		this.volume = volume;
		if (!enabled)
		{
			return;
		}

		OggAudioPlayer player = engineOrNull();
		if (player != null)
		{
			player.setMusicVolume(volume);
		}
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * @return is a music playing?
	 */
	public boolean isRunning()
	{
		if (!enabled)
		{
			return false;
		}

		OggAudioPlayer player = engineOrNull();
		return player != null && player.isMusicPlaying();
	}

	/*-------------------------------------------------------------------------*/
	public void setEnabled(boolean enabled)
	{
		if (!enabled)
		{
			stop();
		}

		this.enabled = enabled;
		OggAudioPlayer player = engineOrNull();
		if (player != null)
		{
			player.setMusicEnabled(enabled);
		}
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * Push buffered music settings to the audio engine after {@link Maze#initAudio}.
	 */
	public void syncToEngine()
	{
		OggAudioPlayer player = engineOrNull();
		if (player == null)
		{
			return;
		}
		player.setMusicEnabled(enabled);
		if (enabled)
		{
			player.setMusicVolume(volume);
			if (pendingTracks != null)
			{
				playLooped(pendingVolume, pendingTracks);
			}
		}
	}

	/*-------------------------------------------------------------------------*/
	private void clearPending()
	{
		pendingTracks = null;
		pendingVolume = -1;
	}

	/*-------------------------------------------------------------------------*/
	private OggAudioPlayer engineOrNull()
	{
		AudioPlayer player = Maze.getInstance().getAudioPlayer();
		if (player instanceof OggAudioPlayer)
		{
			return (OggAudioPlayer)player;
		}
		return null;
	}

	/*-------------------------------------------------------------------------*/
	private void log(String s)
	{
		Maze.log(Log.DEBUG, s + ":" + Thread.currentThread().getName());
	}
}
