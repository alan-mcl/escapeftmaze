package mclachlan.maze.data;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.*;
import javax.imageio.ImageIO;
import mclachlan.maze.util.MazeException;

public class BufferedImageCache
{
	private final ConcurrentHashMap<String, BufferedImage> cache = new ConcurrentHashMap<>();

	/**
	 * Retrieves a BufferedImage from the cache or loads it from disk if not
	 * already cached.
	 *
	 * @param imageId The file path to use as the key and to load the image.
	 * @return The BufferedImage corresponding to the imageId.
	 * @throws IOException If the image cannot be read from disk.
	 */
	public BufferedImage getImage(String imageId)
	{
		BufferedImage image = cache.get(imageId);
		if (image == null)
		{
          try
          {
              image = ImageIO.read(new File(imageId));
              if (image == null)
              {
                  throw new MazeException("Failed to load image: " + imageId);
              }
          }
          catch (IOException e)
          {
              throw new MazeException(e);
          }
          cache.put(imageId, image);
		}
		return image;
	}

	public boolean containsImage(String imageId)
	{
		return cache.containsKey(imageId);
	}

	/**
	 * Manually adds or replaces a cached image.
	 *
	 * @param imageId The path identifier.
	 * @param image  The BufferedImage to cache.
	 */
	public void putImage(String imageId, BufferedImage image)
	{
		cache.put(imageId, image);
	}
}

