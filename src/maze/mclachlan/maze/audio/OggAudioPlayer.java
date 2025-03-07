package mclachlan.maze.audio;

import java.io.*;
import java.util.*;
import javax.sound.sampled.*;
import mclachlan.maze.data.Database;
import mclachlan.maze.data.v2.V2Loader;
import mclachlan.maze.data.v2.V2Saver;
import mclachlan.maze.game.Maze;
import mclachlan.maze.util.MazeException;

public class OggAudioPlayer implements AudioPlayer
{
	private final Map<String, byte[]> soundCache = new HashMap<>();

	/*-------------------------------------------------------------------------*/
	@Override
	public void playSound(String soundName, int volume)
	{
		Database.getInstance().cacheSound(soundName);
		byte[] soundData = soundCache.get(soundName);
		if (soundData == null)
		{
			throw new MazeException("Sound not found in cache: [" + soundName + "]");
		}

		new Thread(() -> {
			try (AudioInputStream audioStream = AudioSystem.getAudioInputStream(new ByteArrayInputStream(soundData)))
			{
				playOggStream2(audioStream, volume);
			}
			catch (Exception e)
			{
				throw new MazeException(e);
			}
		}).start();
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public void cacheSound(String soundName, InputStream stream)
	{
		try (ByteArrayOutputStream buffer = new ByteArrayOutputStream())
		{
			byte[] data = new byte[4096];
			int bytesRead;
			while ((bytesRead = stream.read(data, 0, data.length)) != -1)
			{
				buffer.write(data, 0, bytesRead);
			}
			soundCache.put(soundName, buffer.toByteArray());
		}
		catch (IOException e)
		{
			throw new MazeException(e);
		}
	}

	/*-------------------------------------------------------------------------*/
	private void playOggStream2(AudioInputStream in,
		int volume) throws Exception
	{
		AudioFormat baseFormat = in.getFormat();

		AudioFormat targetFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, baseFormat.getSampleRate(),
			16, baseFormat.getChannels(), baseFormat.getChannels() * 2, baseFormat.getSampleRate(), false);

		AudioInputStream dataIn = AudioSystem.getAudioInputStream(targetFormat, in);

		byte[] buffer = new byte[4096];

		// get a line from a mixer in the system with the wanted format
		DataLine.Info info = new DataLine.Info(SourceDataLine.class, targetFormat);
		SourceDataLine line = (SourceDataLine)AudioSystem.getLine(info);
		setVolume(line, volume);

		if (line != null)
		{
			line.open();

			line.start();
			int nBytesRead = 0, nBytesWritten = 0;
			while (nBytesRead != -1)
			{
				nBytesRead = dataIn.read(buffer, 0, buffer.length);
				if (nBytesRead != -1)
				{
					nBytesWritten = line.write(buffer, 0, nBytesRead);
				}
			}

			line.drain();
			line.stop();
			line.close();

			dataIn.close();
		}

		in.close();
	}

	/*-------------------------------------------------------------------------*/
	private void setVolume(SourceDataLine line, int volume)
	{
		if (line.isControlSupported(FloatControl.Type.MASTER_GAIN))
		{
			FloatControl gainControl = (FloatControl)line.getControl(FloatControl.Type.MASTER_GAIN);
			float dB = (float)(Math.log(volume / 100.0) * 20.0);
			gainControl.setValue(dB);
		}
	}

	/*-------------------------------------------------------------------------*/
	public static void main(String[] args) throws Exception
	{
		V2Saver saver = new V2Saver();
		V2Loader loader = new V2Loader();
		Database dbv2 = new Database(loader, saver, Maze.getStubCampaign());

		OggAudioPlayer player = new OggAudioPlayer();
		Maze maze = new Maze(new HashMap<>(), Maze.getStubCampaign());
		maze.initAudio(player);;

		dbv2.initImpls();
//      dbv2.initCaches(null);

		String clipName = "424690__9931__dissonance-example";

		player.cacheSound(clipName, new FileInputStream("data/default/sound/" + clipName + ".ogg"));
		player.playSound(clipName, 100);
	}
}