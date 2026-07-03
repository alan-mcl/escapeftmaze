package mclachlan.maze.audio;

import java.io.*;
import java.util.*;
import mclachlan.maze.data.Database;
import mclachlan.maze.data.v2.V2Loader;
import mclachlan.maze.data.v2.V2Saver;
import mclachlan.maze.game.Maze;
import mclachlan.maze.util.MazeException;

/**
 * Unified Ogg playback engine for sound effects and background music.
 */
public class OggAudioPlayer implements AudioPlayer
{
	private final Map<String, byte[]> soundCache = new HashMap<>();

	private boolean musicEnabled = true;
	private MusicPlaybackThread musicPlaybackThread;
	private final Object musicPlaybackMutex = new Object();
	private JCraftPlayer musicPlayer;

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
			try
			{
				JCraftPlayer player = new JCraftPlayer(
					new ByteArrayInputStream(soundData),
					new Object(),
					volume);
				player.start();
			}
			catch (IOException e)
			{
				throw new MazeException(e);
			}
		}).start();
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public synchronized void cacheSound(String soundName, InputStream stream)
	{
		if (soundCache.containsKey(soundName))
		{
			return;
		}

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
	/**
	 * Play the given track list in sequence, looping indefinitely.
	 */
	public void playMusicLooped(final int volume, final String... trackNames)
	{
		if (!musicEnabled)
		{
			return;
		}

		stopMusic();
		musicPlaybackThread = new MusicPlaybackThread(volume, trackNames);
		musicPlaybackThread.start();
	}

	/*-------------------------------------------------------------------------*/
	public void stopMusic()
	{
		if (musicPlayer != null)
		{
			musicPlayer.setPlaying(false);
		}
		if (musicPlaybackThread != null)
		{
			musicPlaybackThread.stopPlaying();
		}
		musicPlayer = null;
		musicPlaybackThread = null;
	}

	/*-------------------------------------------------------------------------*/
	public void setMusicVolume(int volume)
	{
		if (musicPlayer != null)
		{
			AudioVolume.setVolume(musicPlayer.getOutputLine(), volume);
		}
	}

	/*-------------------------------------------------------------------------*/
	public boolean isMusicPlaying()
	{
		return musicPlayer != null && musicPlayer.isPlaying();
	}

	/*-------------------------------------------------------------------------*/
	public void setMusicEnabled(boolean enabled)
	{
		if (!enabled)
		{
			stopMusic();
		}
		this.musicEnabled = enabled;
	}

	/*-------------------------------------------------------------------------*/
	private class MusicPlaybackThread extends Thread
	{
		private final int startingVolume;
		private boolean playing;
		private final Queue<String> tracks;

		public MusicPlaybackThread(int startingVolume, String... trackNames)
		{
			this.startingVolume = startingVolume;
			this.tracks = new LinkedList<>(Arrays.asList(trackNames));
		}

		@Override
		public void run()
		{
			this.playing = true;

			while (playing)
			{
				try
				{
					String track = tracks.poll();

					if (track != null)
					{
						try (InputStream is = Database.getInstance().getMusic(track))
						{
							musicPlayer = new JCraftPlayer(is, musicPlaybackMutex, startingVolume);
							musicPlayer.start();

							synchronized (musicPlaybackMutex)
							{
								musicPlaybackMutex.wait();
							}
						}
					}

					if (playing)
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
			if (musicPlayer != null)
			{
				musicPlayer.setPlaying(false);
			}
			synchronized (musicPlaybackMutex)
			{
				musicPlaybackMutex.notifyAll();
			}
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
		maze.initAudio(player);
		;

		dbv2.initImpls();
//      dbv2.initCaches(null);

		String clipName = "424690__9931__dissonance-example";
		String clipName2 = "27826_Erdie_sword01";

		player.cacheSound(clipName, new FileInputStream("data/default/sound/" + clipName + ".ogg"));
		player.cacheSound(clipName, new FileInputStream("data/default/sound/" + clipName2 + ".ogg"));

		new Thread(() ->
		{
			for (int i = 0; i < 20; i++)
			{
				player.playSound(clipName, 100);
				try
				{
					Thread.sleep(1000);
				}
				catch (InterruptedException e)
				{
					throw new RuntimeException(e);
				}
			}
		}).start();

		new Thread(() ->
		{
			for (int i = 0; i < 20; i++)
			{
				player.playSound(clipName2, 100);
				try
				{
					Thread.sleep(200);
				}
				catch (InterruptedException e)
				{
					throw new RuntimeException(e);
				}
			}
		}).start();


		Thread.sleep(5000);
	}
}
