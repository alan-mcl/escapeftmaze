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

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import javax.sound.sampled.SourceDataLine;
import mclachlan.maze.data.Database;
import mclachlan.maze.util.MazeException;

/**
 * Background music player.
 */
public class Music
{
	private boolean enabled;

	/**
	 * The audio output line.
	 */
	private SourceDataLine sdl;
	/**
	 * The background playback thread.
	 */
	private volatile Thread playbackThread;
	/**
	 * The database.
	 */
	private final Database db;
	/**
	 * Use soundClip for playback.
	 */
	private final boolean useClip = false;
	/**
	 * OGG music player.
	 */
	private volatile OggMusic oggMusic;

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
	 * Start/continue the music playback.
	 */
	public void play()
	{
		log("Music.play");

		if (!enabled)
		{
			return;
		}

		if (sdl != null)
		{
			sdl.start();
		}
		else if (oggMusic != null)
		{
			oggMusic.outputLine.start();
		}
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

		Thread th = playbackThread;
		if (th != null)
		{
			th.interrupt();
		}
		if (sdl != null)
		{
			sdl.close();
		}
		else if (oggMusic != null && oggMusic.outputLine != null)
		{
			oggMusic.outputLine.close();
		}
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

		Thread th = playbackThread;
		if (th != null)
		{
			th.interrupt();
			playbackThread = null;
		}
		if (sdl != null)
		{
			sdl.close();
			sdl = null;
		}
		if (oggMusic != null)
		{
			oggMusic.close();
			oggMusic = null;
		}
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
	 * @param fileName the array of file names to play
	 * @param volume the initial playback volume
	 */
	public void playLooped(final int volume, final String... fileName)
	{
		log("Music.playLooped "+Arrays.asList(fileName));

		if (!enabled)
		{
			return;
		}

		stop();
		Thread th = playbackThread;
		if (th != null)
		{
			th.interrupt();
			playbackThread = null;
		}
		th = new Thread(null, new Runnable()
		{
			public void run()
			{
				playbackLoop(volume, fileName);
			}
		}, "MusicPlaybackL-" + Arrays.toString(fileName));
		playbackThread = th;
		th.start();
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * Play the given file list in the given sequence once.
	 *
	 * @param fileName the array of file names to play
	 * @param volume the initial playback volume
	 */
	public void playSequence(final int volume, final String... fileName)
	{
		log("Music.playSequence "+Arrays.asList(fileName));

		if (!enabled)
		{
			return;
		}

		stop();
		Thread th = playbackThread;
		if (th != null)
		{
			th.interrupt();
			playbackThread = null;
		}
		th = new Thread(null, new Runnable()
		{
			public void run()
			{
				playbackSequence(volume, fileName);
			}
		}, "MusicPlaybackS-" + Arrays.toString(fileName));
		playbackThread = th;
		th.start();
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * The audio playback loop.
	 *
	 * @param fileNames the audio files to play back
	 * @param volume the initial playback volume
	 */
	private void playbackLoop(final int volume, String... fileNames)
	{
		int fails = 0;
		while (checkStop() && fails < fileNames.length)
		{
			fails += playbackSequence(volume, fileNames);
		}
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * Play the sequence of audio data and return when all completed.
	 *
	 * @param volume the audio volume function
	 * @param fileNames the list of resource names to get
	 * @return the failure count
	 */
	int playbackSequence(final int volume, String... fileNames)
	{
		int fails = 0;
		for (final String name : fileNames)
		{
			InputStream rp = db.getMusic(name);
			if (!checkStop())
			{
				break;
			}
			else
			{
				try
				{
					if (!playbackOgg(rp, volume))
					{
						fails++;
					}
				}
				catch (IOException ex)
				{
					throw new MazeException(ex);
				}
			}
		}
		return fails;
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * @return true if the playback can continue
	 */
	private boolean checkStop()
	{
		return playbackThread == Thread.currentThread()
			&& !Thread.currentThread().isInterrupted();
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * Plays back the given filename as an OGG audio file.
	 *
	 * @param is the resource place representing the music
	 * @param volume the initial playback volume
	 * @return true if the file was accessible
	 * @throws IOException on IO error
	 */
	private boolean playbackOgg(InputStream is, int volume) throws IOException
	{
		try
		{
			oggMusic = new OggMusic(Thread.currentThread(), volume);
			oggMusic.playOgg(is);
			return true;
		}
		finally
		{
			if (is != null)
			{
				is.close();
			}
		}
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * Play back music using the Clip object.
	 *
	 * @param rp the resource to play back
	 * @param volume the initial playback volume
	 */
	private void playBackClip(InputStream rp, int volume)
	{
		/*try
		{
			AudioInputStream ain = AudioSystem.getAudioInputStream(rp);
			try
			{
				AudioFormat af = ain.getFormat();
				byte[] snd = IOUtils.load(ain);
				// upscale an 8 bit sample to 16 bit
				if (af.getSampleSizeInBits() == 8)
				{
					// signify if unsigned, because the upscaling works on signed data
					if (af.getEncoding() == Encoding.PCM_UNSIGNED)
					{
						for (int i = 0; i < snd.length; i++)
						{
							snd[i] = (byte)((snd[i] & 0xFF) - 128);
						}
					}
					snd = AudioThread.convert8To16(snd);
					af = new AudioFormat(af.getSampleRate(), 16, af.getChannels(), true, af.isBigEndian());
				}
				sdl = AudioSystem.getSourceDataLine(af);
				sdl.open(af);
				try
				{
					setVolume(volume);
					sdl.start();
					sdl.write(snd, 0, snd.length);
					sdl.drain();
				}
				finally
				{
					sdl.close();
					sdl = null;
				}
			}
			finally
			{
				ain.close();
			}
		}
		catch (UnsupportedAudioFileException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		catch (LineUnavailableException e)
		{
			e.printStackTrace();
		}*/
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * Finds the 'data' chunk in a RIFF wav file.
	 *
	 * @param raf the random access file
	 * @return the offset of the actual data chunk
	 * @throws IOException if an IO error occurs
	 */
	/*private static long findData(InputStream raf) throws IOException
	{
		IOUtils.skipFully(raf, 12);
		long offset = 12;
		while (true)
		{
			int type = IOUtils.readIntLE(raf);
			offset += 4;
			if (type == 0x61746164)
			{
				IOUtils.skipFully(raf, 4);
				return offset + 4;
			}
			else
			{
				int count = IOUtils.readIntLE(raf);
				IOUtils.skipFully(raf, count);
				offset += count;
			}
		}
	}*/

	/**
	 * Finds the 'data' chunk in a RIFF wav file.
	 *
	 * @param raf the random access file
	 * @return the offset of the actual data chunk
	 * @throws IOException if an IO error occurs
	 */
	/*static long findData(RandomAccessFile raf) throws IOException
	{
		raf.seek(12);
		long offset = 12;
		while (true)
		{
			int type = IOUtils.readIntLE(raf);
			offset += 4;
			if (type == 0x61746164)
			{
				IOUtils.skipFullyD(raf, 4);
				return offset + 4;
			}
			else
			{
				int count = IOUtils.readIntLE(raf);
				IOUtils.skipFullyD(raf, count);
				offset += count;
			}
		}
	}*/

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

		if (sdl != null)
		{
			AudioThread.setVolume(sdl, volume);
		}
		else if (oggMusic != null && oggMusic.outputLine != null)
		{
			AudioThread.setVolume(oggMusic.outputLine, volume);
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

		return (sdl != null && sdl.isActive())
			|| (oggMusic != null && oggMusic.outputLine != null && oggMusic.outputLine.isActive());
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
//		System.out.println(s+":"+Thread.currentThread().getName());
	}
}