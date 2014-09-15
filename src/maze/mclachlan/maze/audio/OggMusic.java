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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

import com.jcraft.jogg.Packet;
import com.jcraft.jogg.Page;
import com.jcraft.jogg.StreamState;
import com.jcraft.jogg.SyncState;
import com.jcraft.jorbis.Block;
import com.jcraft.jorbis.Comment;
import com.jcraft.jorbis.DspState;
import com.jcraft.jorbis.Info;
import mclachlan.maze.util.MazeException;

/**
 * Plays back ogg music. Based on the com.craft.jorbis JOrbisPlayer.
 */
public class OggMusic
{
	/**
	 * The current playback thread.
	 */
	private volatile Thread playbackThread;
	/**
	 * Ogg Player object.
	 */
	private SyncState oy;
	/**
	 * Ogg Player object.
	 */
	private StreamState os;
	/**
	 * Ogg Player object.
	 */
	private Page og;
	/**
	 * Ogg Player object.
	 */
	private Packet op;
	/**
	 * Ogg Player object.
	 */
	private Info vi;
	/**
	 * Ogg Player object.
	 */
	private Comment vc;
	/**
	 * Ogg Player object.
	 */
	private DspState vd;
	/**
	 * Ogg Player object.
	 */
	private Block vb;
	/**
	 * Ogg Player object.
	 */
	private static final int BUFSIZE = 4096 * 2;
	/**
	 * Ogg Player object.
	 */
	private int convsize = BUFSIZE * 2;
	/**
	 * Ogg Player object.
	 */
	private byte[] convbuffer = new byte[convsize];

	/**
	 * Ogg Player object.
	 */
	private int rate = 0;
	/**
	 * Ogg Player object.
	 */
	private int channels = 0;
	/**
	 * The current playback source data line.
	 */
	public volatile SourceDataLine outputLine = null;
	/**
	 * Current/initial sound gain.
	 */
	private int initialVolume;

	/*-------------------------------------------------------------------------*/
	/**
	 * Constructor.
	 *
	 * @param me the playback thread running the run() method
	 * @param volume the initial volume
	 */
	public OggMusic(Thread me, int volume)
	{
		this.playbackThread = me;
		this.initialVolume = volume;
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * Close playback thread.
	 */
	public void close()
	{
		Thread th = playbackThread;
		if (th != null)
		{
			if (outputLine != null)
			{
				outputLine.close();
			}
			th.interrupt();
			playbackThread = null;
		}
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * Initialize JOrbis.
	 */
	void initJOrbis()
	{
		oy = new SyncState();
		os = new StreamState();
		og = new Page();
		op = new Packet();

		vi = new Info();
		vc = new Comment();
		vd = new DspState();
		vb = new Block(vd);

		oy.init();
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * Initialize the audio subsystem.
	 *
	 * @param channels the number of channels
	 * @param rate the frequence
	 */
	void initAudio(int channels, int rate)
	{
		try
		{
			AudioFormat audioFormat = new AudioFormat(rate, 16,
				channels, true, // PCM_Signed
				false // littleEndian
			);
			DataLine.Info info = new DataLine.Info(SourceDataLine.class,
				audioFormat, AudioSystem.NOT_SPECIFIED);
			if (!AudioSystem.isLineSupported(info))
			{
				return;
			}

			try
			{
				outputLine = (SourceDataLine)AudioSystem.getLine(info);
				outputLine.open(audioFormat);
			}
			catch (LineUnavailableException ex)
			{
				System.out.println("Unable to open the sourceDataLine");
				ex.printStackTrace();
				return;
			}
			catch (IllegalArgumentException ex)
			{
				System.out.println("Illegal Argument");
				ex.printStackTrace();
				return;
			}

			this.rate = rate;
			this.channels = channels;
		}
		catch (Exception ee)
		{
			System.out.println(ee);
		}
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * Create or return the output line.
	 *
	 * @param channels the number of channels
	 * @param rate the frequency
	 * @return the current output line
	 */
	SourceDataLine getOutputLine(int channels, int rate)
	{
		if (outputLine == null || this.rate != rate
			|| this.channels != channels)
		{
			if (outputLine != null)
			{
				outputLine.close();
			}
			initAudio(channels, rate);
			outputLine.start();
		}
		return outputLine;
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * Play the OGG file.
	 *
	 * @param bitStream the opened file to play
	 */
	public void playOgg(InputStream bitStream)
	{
		boolean chained = false;
		initJOrbis();
		int bytes = 0;
		loop:
		while (true)
		{
			int eos = 0;

			int index = oy.buffer(BUFSIZE);
			byte[] buffer = oy.data;
			try
			{
				bytes = bitStream.read(buffer, index, BUFSIZE);
			}
			catch (Exception e)
			{
				throw new MazeException(e);
			}
			oy.wrote(bytes);

			if (chained)
			{ //
				chained = false; //
			}
			else
			{ //
				if (oy.pageout(og) != 1)
				{
					if (bytes < BUFSIZE)
					{
						break;
					}
					throw new MazeException("Input does not appear to be an Ogg bitstream.");
				}
			} //
			os.init(og.serialno());
			os.reset();

			vi.init();
			vc.init();

			if (os.pagein(og) < 0)
			{
				// error; stream version mismatch perhaps
				throw new MazeException("Error reading first page of Ogg bitstream data.");
			}

			if (os.packetout(op) != 1)
			{
				// no page? must not be vorbis
				throw new MazeException("Error reading initial header packet.");
			}

			if (vi.synthesis_headerin(vc, op) < 0)
			{
				// error case; not a vorbis header
				throw new MazeException("This Ogg bitstream does not contain Vorbis audio data.");
			}

			int i = 0;

			while (i < 2)
			{
				while (i < 2)
				{
					int result = oy.pageout(og);
					if (result == 0)
					{
						break; // Need more data
					}
					if (result == 1)
					{
						os.pagein(og);
						while (i < 2)
						{
							result = os.packetout(op);
							if (result == 0)
							{
								break;
							}
							if (result == -1)
							{
								throw new MazeException("Corrupt secondary header.  Exiting.");
							}
							vi.synthesis_headerin(vc, op);
							i++;
						}
					}
				}

				index = oy.buffer(BUFSIZE);
				buffer = oy.data;
				try
				{
					bytes = bitStream.read(buffer, index, BUFSIZE);
				}
				catch (Exception e)
				{
					throw new MazeException(e);
				}
				if (bytes == 0 && i < 2)
				{
					throw new MazeException("End of file before finding all Vorbis headers!");
				}
				oy.wrote(bytes);
			}

			byte[][] ptr = vc.user_comments;

			for (int j = 0; j < ptr.length; j++)
			{
				if (ptr[j] == null)
				{
					break;
				}
			}

			convsize = BUFSIZE / vi.channels;

			vd.synthesis_init(vi);
			vb.init(vd);

			float[][][] lPcmf = new float[1][][];
			int[] lIndex = new int[vi.channels];

			// Preset initial gain and mute
			SourceDataLine sdl = getOutputLine(vi.channels, vi.rate);
			AudioThread.setVolume(sdl, initialVolume);

			while (eos == 0)
			{
				while (eos == 0)
				{

					if (playbackThread != Thread.currentThread()
						|| Thread.currentThread().isInterrupted())
					{
						try
						{
							bitStream.close();
							outputLine.close();
							outputLine = null;
						}
						catch (IOException ee)
						{
							throw new MazeException(ee);
						}
						return;
					}

					int result = oy.pageout(og);
					if (result == 0)
					{
						break; // need more data
					}
					if (result != -1)
					{
						os.pagein(og);

						if (og.granulepos() == 0)
						{ //
							chained = true; //
							eos = 1; //
							break; //
						} //

						while (!Thread.currentThread().isInterrupted())
						{
							result = os.packetout(op);
							if (result == 0)
							{
								break; // need more data
							}
							if (result != -1)
							{
								// we have a packet. Decode it
								int samples;
								if (vb.synthesis(op) == 0)
								{ // test for
									// success!
									vd.synthesis_blockin(vb);
								}
								samples = vd.synthesis_pcmout(lPcmf,
									lIndex);
								while (samples > 0 && !Thread.currentThread().isInterrupted())
								{
									float[][] pcmf = lPcmf[0];
									int bout = (samples < convsize ? samples
										: convsize);

									// convert doubles to 16 bit signed ints
									// (host order) and
									// interleave
									for (i = 0; i < vi.channels; i++)
									{
										int iptr = i * 2;
										// int ptr=i;
										int mono = lIndex[i];
										for (int j = 0; j < bout; j++)
										{
											int val = (int)(pcmf[i][mono + j] * 32767.);
											if (val > 32767)
											{
												val = 32767;
											}
											if (val < -32768)
											{
												val = -32768;
											}
											if (val < 0)
											{
												val = val | 0x8000;
											}
											convbuffer[iptr] = (byte)(val);
											convbuffer[iptr + 1] = (byte)(val >>> 8);
											iptr += 2 * (vi.channels);
										}
									}
									outputLine.write(convbuffer, 0, 2
										* vi.channels * bout);
									vd.synthesis_read(bout);
									samples = vd.synthesis_pcmout(lPcmf,
										lIndex);
								}
							}
						}
						if (og.eos() != 0)
						{
							eos = 1;
						}
					}
				}

				if (eos == 0)
				{
					index = oy.buffer(BUFSIZE);
					buffer = oy.data;
					try
					{
						bytes = bitStream.read(buffer, index, BUFSIZE);
					}
					catch (Exception e)
					{
						System.err.println(e);
						return;
					}
					if (bytes == -1)
					{
						break;
					}
					oy.wrote(bytes);
					if (bytes == 0)
					{
						eos = 1;
					}
				}
			}

			os.clear();
			vb.clear();
			vd.clear();
			vi.clear();
		}

		oy.clear();

		try
		{
			bitStream.close();
		}
		catch (IOException e)
		{
			throw new MazeException(e);
		}
	}

	/*-------------------------------------------------------------------------*/
	public static void main(String[] args) throws Exception
	{
		FileInputStream fis = new FileInputStream(
			new File("data/default/sound/track/startup.ogg"));

		OggMusic om = new OggMusic(Thread.currentThread(), 100);
		om.playOgg(fis);
	}
}
