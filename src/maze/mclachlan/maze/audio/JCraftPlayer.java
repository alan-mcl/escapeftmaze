package mclachlan.maze.audio;

/*
 * Copyright &copy; Jon Kristensen, 2008.
 * All rights reserved.
 *
 * This is version 1.0 of this source code, made to work with JOrbis 1.x. The
 * last time this file was updated was the 15th of March, 2008.
 *
 * Version history:
 *
 * 1.0: Initial release.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *   * Redistributions of source code must retain the above copyright notice,
 *     this list of conditions and the following disclaimer.
 *
 *   * Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *
 *   * Neither the name of jonkri.com nor the names of its contributors may be
 *     used to endorse or promote products derived from this software without
 *     specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

import com.jcraft.jogg.Packet;
import com.jcraft.jogg.Page;
import com.jcraft.jogg.StreamState;
import com.jcraft.jogg.SyncState;
import com.jcraft.jorbis.Block;
import com.jcraft.jorbis.Comment;
import com.jcraft.jorbis.DspState;
import com.jcraft.jorbis.Info;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import javax.sound.sampled.*;
import mclachlan.maze.util.MazeException;

/**
 * This class is based on http://www.jcraft.com/jorbis/tutorial/ExamplePlayer.java
 * <b>
 * See also http://www.jcraft.com/jorbis/tutorial/Tutorial.html
 *
 * @author Jon Kristensen
 * @version 1.0
 */
public class JCraftPlayer extends Thread
{
	// If you wish to debug this source, please set the variable below to true.
	private static final boolean debugMode = false;

	/*
	 * InputStream object so that we can open a connection to the media file.
	 */
	private InputStream inputStream = null;

	/*
	 * We need a buffer, it's size, a count to know how many bytes we have read
	 * and an index to keep track of where we are. This is standard networking
	 * stuff used with read().
	 */
	private byte[] buffer = null;
	private final int bufferSize = 2048;
	private int count = 0;
	private int index = 0;

	/*
	 * JOgg and JOrbis require fields for the converted buffer. This is a buffer
	 * that is modified in regards to the number of audio channels. Naturally,
	 * it will also need a size.
	 */
	private byte[] convertedBuffer;
	private int convertedBufferSize;

	// The source data line onto which data can be written.
	private SourceDataLine outputLine = null;

	// A three-dimensional an array with PCM information.
	private float[][][] pcmInfo;

	// The index for the PCM information.
	private int[] pcmIndex;

	// Here are the four required JOgg objects...
	private final Packet joggPacket = new Packet();
	private final Page joggPage = new Page();
	private final StreamState joggStreamState = new StreamState();
	private final SyncState joggSyncState = new SyncState();

	// ... followed by the four required JOrbis objects.
	private final DspState jorbisDspState = new DspState();
	private final Block jorbisBlock = new Block(jorbisDspState);
	private final Comment jorbisComment = new Comment();
	private final Info jorbisInfo = new Info();

	/** Used to control the player state */
	private boolean playing;
	private final Object playbackMutex;
	private final int startingVolume;

	/**
	 * The programs <code>main()</code> method. Will read the first
	 * command-line argument and use it as URL, after which it will start the
	 * thread.
	 *
	 * @param args command-line arguments
	 */
	public static void main(String[] args) throws Exception
	{
		// Set the URL as the first argument, if any.
		String url = args.length > 0 ? url = args[0] : null;

		/*
		 * If the url variable is set, start the thread. If not, give an error
		 * and die.
		 */
		if(url != null)
		{
			JCraftPlayer JCraftPlayer = new JCraftPlayer(Files.newInputStream(Paths.get(url)), new Object(), 100);
			JCraftPlayer.start();
		}
		else
		{
			throw new MazeException("Please provide an argument with the file to play.");
		}
	}

	/**
	 */
	JCraftPlayer(InputStream is, Object playbackMutex, int startingVolume) throws IOException
	{
		inputStream = is;
		this.playbackMutex = playbackMutex;
		this.startingVolume = startingVolume;
	}

	/**
	 * This method is probably easiest understood by looking at the body.
	 * However, it will - if no problems occur - call methods to initialize the
	 * JOgg JOrbis libraries, read the header, initialize the sound system, read
	 * the body of the stream and clean up.
	 */
	public void run()
	{
		playing = true;

		// Check that we got an InputStream.
		if(inputStream == null)
		{
			throw new MazeException("We don't have an input stream and therefor cannot continue.");
		}

		// Initialize JOrbis.
		initializeJOrbis();

		/*
		 * If we can read the header, we try to initialize the sound system. If we
		 * could initialize the sound system, we try to read the body.
		 */
		if(playing && readHeader())
		{
			if(playing && initializeSound())
			{
				if (playing)
				{
					readBody();
				}
			}
		}

		// Afterwards, we clean up.
		cleanUp();

		synchronized (playbackMutex)
		{
			playbackMutex.notifyAll();
		}
	}

	/**
	 * @return
	 * 	The audio output line, useful for operations like setting volume
	 */
	public SourceDataLine getOutputLine()
	{
		return outputLine;
	}

	public boolean isPlaying()
	{
		return playing;
	}

	public void setPlaying(boolean playing)
	{
		debugOutput("ExamplePlayer.setPlaying "+playing);
		this.playing = playing;
	}

	/**
	 * Initializes JOrbis. First, we initialize the <code>SyncState</code>
	 * object. After that, we prepare the <code>SyncState</code> buffer. Then
	 * we "initialize" our buffer, taking the data in <code>SyncState</code>.
	 */
	private void initializeJOrbis()
	{
		debugOutput("Initializing JOrbis.");

		// Initialize SyncState
		joggSyncState.init();

		// Prepare the to SyncState internal buffer
		joggSyncState.buffer(bufferSize);

		/*
		 * Fill the buffer with the data from SyncState's internal buffer. Note
		 * how the size of this new buffer is different from bufferSize.
		 */
		buffer = joggSyncState.data;

		debugOutput("Done initializing JOrbis.");
	}

	/**
	 * This method reads the header of the stream, which consists of three
	 * packets.
	 *
	 * @return true if the header was successfully read, false otherwise
	 */
	private boolean readHeader()
	{
		debugOutput("Starting to read the header.");

		/*
		 * Variable used in loops below. While we need more data, we will
		 * continue to read from the InputStream.
		 */
		boolean needMoreData = true;

		/*
		 * We will read the first three packets of the header. We start off by
		 * defining packet = 1 and increment that value whenever we have
		 * successfully read another packet.
		 */
		int packet = 1;

		/*
		 * While we need more data (which we do until we have read the three
		 * header packets), this loop reads from the stream and has a big
		 * <code>switch</code> statement which does what it's supposed to do in
		 * regards to the current packet.
		 */
		while(needMoreData)
		{
			// Read from the InputStream.
			try
			{
				count = inputStream.read(buffer, index, bufferSize);
			}
			catch(IOException exception)
			{
				debugOutput("Could not read from the input stream.");
				throw new MazeException(exception);
			}

			// We let SyncState know how many bytes we read.
			joggSyncState.wrote(count);

			/*
			 * We want to read the first three packets. For the first packet, we
			 * need to initialize the StreamState object and a couple of other
			 * things. For packet two and three, the procedure is the same: we
			 * take out a page, and then we take out the packet.
			 */
			switch(packet)
			{
				// The first packet.
				case 1:
				{
					// We take out a page.
					switch(joggSyncState.pageout(joggPage))
					{
						// If there is a hole in the data, we must exit.
						case -1:
						{
							throw new MazeException("There is a hole in the first packet data.");
//							return false;
						}

						// If we need more data, we break to get it.
						case 0:
						{
							break;
						}

						/*
						 * We got where we wanted. We have successfully read the
						 * first packet, and we will now initialize and reset
						 * StreamState, and initialize the Info and Comment
						 * objects. Afterwards we will check that the page
						 * doesn't contain any errors, that the packet doesn't
						 * contain any errors and that it's Vorbis data.
						 */
						case 1:
						{
							// Initializes and resets StreamState.
							joggStreamState.init(joggPage.serialno());
							joggStreamState.reset();

							// Initializes the Info and Comment objects.
							jorbisInfo.init();
							jorbisComment.init();

							// Check the page (serial number and stuff).
							if(joggStreamState.pagein(joggPage) == -1)
							{
								throw new MazeException("We got an error while reading the first header page.");
//								return false;
							}

							/*
							 * Try to extract a packet. All other return values
							 * than "1" indicates there's something wrong.
							 */
							if(joggStreamState.packetout(joggPacket) != 1)
							{
								throw new MazeException("We got an error while reading the first header packet.");
//								return false;
							}

							/*
							 * We give the packet to the Info object, so that it
							 * can extract the Comment-related information,
							 * among other things. If this fails, it's not
							 * Vorbis data.
							 */
							if(jorbisInfo.synthesis_headerin(jorbisComment,
								joggPacket) < 0)
							{
								throw new MazeException("We got an error while "
									+ "interpreting the first packet. "
									+ "Apparantly, it's not Vorbis data.");
//								return false;
							}

							// We're done here, let's increment "packet".
							packet++;
							break;
						}
					}

					/*
					 * Note how we are NOT breaking here if we have proceeded to
					 * the second packet. We don't want to read from the input
					 * stream again if it's not necessary.
					 */
					if(packet == 1) break;
				}

				// The code for the second and third packets follow.
				case 2:	case 3:
				{
					// Try to get a new page again.
					switch(joggSyncState.pageout(joggPage))
					{
						// If there is a hole in the data, we must exit.
						case -1:
						{
							throw new MazeException("There is a hole in the second or third packet data.");
//							return false;
						}

						// If we need more data, we break to get it.
						case 0:
						{
							break;
						}

						/*
						 * Here is where we take the page, extract a packet and
						 * and (if everything goes well) give the information to
						 * the Info and Comment objects like we did above.
						 */
						case 1:
						{
							// Share the page with the StreamState object.
							joggStreamState.pagein(joggPage);

							/*
							 * Just like the switch(...packetout...) lines
							 * above.
							 */
							switch(joggStreamState.packetout(joggPacket))
							{
								// If there is a hole in the data, we must exit.
								case -1:
								{
									throw new MazeException("There is a hole in the first packet data.");
//									return false;
								}

								// If we need more data, we break to get it.
								case 0:
								{
									break;
								}

								// We got a packet, let's process it.
								case 1:
								{
									/*
									 * Like above, we give the packet to the
									 * Info and Comment objects.
									 */
									jorbisInfo.synthesis_headerin(
										jorbisComment, joggPacket);

									// Increment packet.
									packet++;

									if(packet == 4)
									{
										/*
										 * There is no fourth packet, so we will
										 * just end the loop here.
										 */
										needMoreData = false;
									}

									break;
								}
							}

							break;
						}
					}

					break;
				}
			}

			// We get the new index and an updated buffer.
			index = joggSyncState.buffer(bufferSize);
			buffer = joggSyncState.data;

			/*
			 * If we need more data but can't get it, the stream doesn't contain
			 * enough information.
			 */
			if(count == 0 && needMoreData)
			{
				throw new MazeException("Not enough header data was supplied.");
//				return false;
			}
		}

		debugOutput("Finished reading the header.");

		return true;
	}

	/**
	 * This method starts the sound system. It starts with initializing the
	 * <code>DspState</code> object, after which it sets up the
	 * <code>Block</code> object. Last but not least, it opens a line to the
	 * source data line.
	 *
	 * @return true if the sound system was successfully started, false
	 *         otherwise
	 */
	private boolean initializeSound()
	{
		debugOutput("Initializing the sound system.");

		// This buffer is used by the decoding method.
		convertedBufferSize = bufferSize * 2;
		convertedBuffer = new byte[convertedBufferSize];

		// Initializes the DSP synthesis.
		jorbisDspState.synthesis_init(jorbisInfo);

		// Make the Block object aware of the DSP.
		jorbisBlock.init(jorbisDspState);

		// Wee need to know the channels and rate.
		int channels = jorbisInfo.channels;
		int rate = jorbisInfo.rate;

		// Creates an AudioFormat object and a DataLine.Info object.
		AudioFormat audioFormat = new AudioFormat((float) rate, 16, channels,
			true, false);
		DataLine.Info datalineInfo = new DataLine.Info(SourceDataLine.class,
			audioFormat, AudioSystem.NOT_SPECIFIED);

		// Check if the line is supported.
		if(!AudioSystem.isLineSupported(datalineInfo))
		{
			throw new MazeException("Audio output line is not supported.");
//			return false;
		}

		/*
		 * Everything seems to be alright. Let's try to open a line with the
		 * specified format and start the source data line.
		 */
		try
		{
			outputLine = (SourceDataLine) AudioSystem.getLine(datalineInfo);
			outputLine.open(audioFormat);
		}
		catch(LineUnavailableException exception)
		{
			debugOutput("The audio output line could not be opened due "
				+ "to resource restrictions.");
			throw new MazeException(exception);
//			return false;
		}
		catch(IllegalStateException exception)
		{
			debugOutput("The audio output line is already open.");
			throw new MazeException(exception);
//			return false;
		}
		catch(SecurityException exception)
		{
			debugOutput("The audio output line could not be opened due "
				+ "to security restrictions.");
			throw new MazeException(exception);
//			return false;
		}

		// Start it.
		outputLine.start();

		// set starting volume
		Music.setVolume(outputLine, startingVolume);

		/*
		 * We create the PCM variables. The index is an array with the same
		 * length as the number of audio channels.
		 */
		pcmInfo = new float[1][][];
		pcmIndex = new int[jorbisInfo.channels];

		debugOutput("Done initializing the sound system.");

		return true;
	}

	/**
	 * This method reads the entire stream body. Whenever it extracts a packet,
	 * it will decode it by calling <code>decodeCurrentPacket()</code>.
	 */
	private void readBody()
	{
		debugOutput("Reading the body.");

		/*
		 * Variable used in loops below, like in readHeader(). While we need
		 * more data, we will continue to read from the InputStream.
		 */
		while(playing)
		{
			switch(joggSyncState.pageout(joggPage))
			{
				// If there is a hole in the data, we just proceed.
				case -1:
				{
					debugOutput("There is a hole in the data. We proceed.");
				}

				// If we need more data, we break to get it.
				case 0:
				{
					break;
				}

				// If we have successfully checked out a page, we continue.
				case 1:
				{
					// Give the page to the StreamState object.
					joggStreamState.pagein(joggPage);

					// If granulepos() returns "0", we don't need more data.
					if(joggPage.granulepos() == 0)
					{
						playing = false;
						break;
					}

					// Here is where we process the packets.
					processPackets: while(true)
					{
						switch(joggStreamState.packetout(joggPacket))
						{
							// Is it a hole in the data?
							case -1:
							{
								debugOutput("There is a hole in the data, we "
									+ "continue though.");
							}

							// If we need more data, we break to get it.
							case 0:
							{
								break processPackets;
							}

							/*
							 * If we have the data we need, we decode the
							 * packet.
							 */
							case 1:
							{
								decodeCurrentPacket();
							}
						}
					}

					/*
					 * If the page is the end-of-stream, we don't need more
					 * data.
					 */
					if(joggPage.eos() != 0) playing = false;
				}
			}

			// If we need more data...
			if(playing)
			{
				// We get the new index and an updated buffer.
				index = joggSyncState.buffer(bufferSize);
				buffer = joggSyncState.data;

				// Read from the InputStream.
				try
				{
					count = inputStream.read(buffer, index, bufferSize);
				}
				catch(Exception e)
				{
//						throw new MazeException(e);
					// we stop in the face of an error
				}

				// We let SyncState know how many bytes we read.
				joggSyncState.wrote(count);

				// There's no more data in the stream.
				if(count == 0) playing = false;
			}
		}
		debugOutput("Done reading the body.");
	}

	/**
	 * A clean-up method, called when everything is finished. Clears the
	 * JOgg/JOrbis objects and closes the <code>InputStream</code>.
	 */
	private void cleanUp()
	{
		debugOutput("Cleaning up.");

		// Clear the necessary JOgg/JOrbis objects.
		joggStreamState.clear();
		jorbisBlock.clear();
		jorbisDspState.clear();
		jorbisInfo.clear();
		joggSyncState.clear();

		// Closes the stream.
		try
		{
			if(inputStream != null) inputStream.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}

		debugOutput("Done cleaning up.");
	}

	/**
	 *  Decodes the current packet and sends it to the audio output line.
	 */
	private void decodeCurrentPacket()
	{
		int samples;

		// Check that the packet is a audio data packet etc.
		if(jorbisBlock.synthesis(joggPacket) == 0)
		{
			// Give the block to the DspState object.
			jorbisDspState.synthesis_blockin(jorbisBlock);
		}

		// We need to know how many samples to process.
		int range;

		/*
		 * Get the PCM information and count the samples. And while these
		 * samples are more than zero...
		 */
		while((samples = jorbisDspState.synthesis_pcmout(pcmInfo, pcmIndex))
			> 0)
		{
			// We need to know for how many samples we are going to process.
			if(samples < convertedBufferSize)
			{
				range = samples;
			}
			else
			{
				range = convertedBufferSize;
			}

			// For each channel...
			for(int i = 0; i < jorbisInfo.channels; i++)
			{
				int sampleIndex = i * 2;

				// For every sample in our range...
				for(int j = 0; j < range; j++)
				{
					/*
					 * Get the PCM value for the channel at the correct
					 * position.
					 */
					int value = (int) (pcmInfo[0][i][pcmIndex[i] + j] * 32767);

					/*
					 * We make sure our value doesn't exceed or falls below
					 * +-32767.
					 */
					if(value > 32767)
					{
						value = 32767;
					}
					if(value < -32768)
					{
						value = -32768;
					}

					/*
					 * It the value is less than zero, we bitwise-or it with
					 * 32768 (which is 1000000000000000 = 10^15).
					 */
					if(value < 0) value = value | 32768;

					/*
					 * Take our value and split it into two, one with the last
					 * byte and one with the first byte.
					 */
					convertedBuffer[sampleIndex] = (byte) (value);
					convertedBuffer[sampleIndex + 1] = (byte) (value >>> 8);

					/*
					 * Move the sample index forward by two (since that's how
					 * many values we get at once) times the number of channels.
					 */
					sampleIndex += 2 * (jorbisInfo.channels);
				}
			}

			// Write the buffer to the audio output line.
			outputLine.write(convertedBuffer, 0, 2 * jorbisInfo.channels
				* range);

			// Update the DspState object.
			jorbisDspState.synthesis_read(range);
		}
	}

	/**
	 * This method is being called internally to output debug information
	 * whenever that is wanted.
	 *
	 * @param output the debug output information
	 */
	private void debugOutput(String output)
	{
		if(debugMode)
		{
			System.out.println("Debug: " + output);
		}
	}
}

