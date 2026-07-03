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

package mclachlan.crusader.client;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.*;
import javax.imageio.ImageIO;
import mclachlan.crusader.CrusaderException;
import mclachlan.maze.data.Database;
import mclachlan.maze.data.MazeTexture;
import mclachlan.maze.util.MazeException;

/**
 * Minimal {@link Database} for loading the self-contained {@code test/crusader/}
 * raycaster fixture without initialising the full game.
 */
public class CrusaderTestDatabase extends Database
{
	public static final String TEST_IMG_DIR = "test/crusader/img/";

	private final Map<String, MazeTexture> testTextures = new HashMap<>();

	public CrusaderTestDatabase() throws Exception
	{
		super(null, null, null);
	}

	public void setTestTextures(Map<String, MazeTexture> textures)
	{
		testTextures.clear();
		testTextures.putAll(textures);
		for (MazeTexture mt : testTextures.values())
		{
			initTestTexture(mt);
		}
	}

	private void initTestTexture(MazeTexture mt)
	{
		List<String> resources = mt.getImageResources();
		BufferedImage[] images = new BufferedImage[resources.size()];
		for (int i = 0; i < resources.size(); i++)
		{
			images[i] = loadTestImage(resources.get(i));
		}
		mt.setTexture(new mclachlan.crusader.Texture(
			mt.getName(),
			images,
			mt.getAnimationDelay(),
			mt.getScrollBehaviour(),
			mt.getScrollSpeed(),
			null));
		if (images.length > 0)
		{
			mt.setImageWidth(images[0].getWidth());
			mt.setImageHeight(images[0].getHeight());
		}
	}

	public static BufferedImage loadTestImage(String resourceName)
	{
		try
		{
			File file = new File(TEST_IMG_DIR + resourceName + ".png");
			if (!file.exists())
			{
				file = new File(TEST_IMG_DIR + resourceName + ".jpg");
			}
			if (!file.exists())
			{
				throw new CrusaderException("missing test image [" + resourceName + "]");
			}
			return ImageIO.read(file);
		}
		catch (Exception e)
		{
			throw new CrusaderException("[" + resourceName + "]", e);
		}
	}

	@Override
	public MazeTexture getMazeTexture(String textureName)
	{
		MazeTexture result = testTextures.get(textureName);
		if (result == null)
		{
			throw new MazeException("invalid maze texture [" + textureName + "]");
		}
		return result;
	}

	@Override
	public Map<String, MazeTexture> getMazeTextures()
	{
		return testTextures;
	}

	@Override
	public BufferedImage getImage(String resourceName)
	{
		return loadTestImage(resourceName);
	}
}
