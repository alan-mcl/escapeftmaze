package mclachlan.maze.ui.diygui.animation;

import java.awt.Graphics2D;
import java.util.*;
import mclachlan.crusader.Map;
import mclachlan.crusader.Tile;
import mclachlan.maze.game.Maze;
import mclachlan.maze.ui.diygui.Animation;

/**
 *
 */
public class TestShadowPassingAnimation extends Animation
{
	private int duration;

	// tile X and Y where the shadow starts and ends
	private int startX, startY;
	private int endX, endY;

	// matrix of light levels representing the shadow
	private int[][] shadowShape;

	// volatile
	long startTime;
	int distX, distY;
	int tileX, tileY;

	public TestShadowPassingAnimation()
	{
	}

	public TestShadowPassingAnimation(int duration, int startX, int startY, int endX, int endY,
		int[][] shadowShape)
	{
		this.duration = duration;
		this.startX = startX;
		this.startY = startY;
		this.endX = endX;
		this.endY = endY;
		this.shadowShape = shadowShape;

		startTime = System.currentTimeMillis();
		distX = Math.abs(startX - endX);
		distY = Math.abs(startY - endY);
	}

	@Override
	public void update(Graphics2D g)
	{
		Map map = Maze.getInstance().getCurrentZone().getMap();
		Tile[] tiles = map.getTiles();

		double elapsedTime = System.currentTimeMillis() - startTime;
		double elapsed = elapsedTime/duration;

		// center the shadow
		if (startX > endX)
		{
			tileX = (int)(startX - Math.round(distX*elapsed));
		}
		else if (startX < endX)
		{
			tileX = (int)(startX + Math.round(distX*elapsed));
		}
		else
		{
			tileX = startX;
		}

		if (startY > endY)
		{
			tileY = (int)(startY - Math.round(distY*elapsed));
		}
		else if (startY < endY)
		{
			tileY = (int)(startY + Math.round(distY*elapsed));
		}
		else
		{
			tileY = startY;
		}

		// update tile light levels around the center
		updateLightLevels(map, tiles, tileX, tileY, shadowShape);
	}

	/*-------------------------------------------------------------------------*/
	private void updateLightLevels(Map map, Tile[] tiles, int tileX, int tileY,
		int[][] lightlevels)
	{
		int shadowWidth = lightlevels.length;
		int shadowHeight = lightlevels[0].length;
		for (int x=0; x< shadowWidth; x++)
		{
			for (int y=0; y< shadowHeight; y++)
			{
				int xx = tileX -shadowWidth/2 +x;
				int yy = tileY -shadowHeight/2 +y;

				int tileIndex = yy * map.getWidth() + xx;

				if (tileIndex >= 0 && tileIndex < tiles.length)
				{
					Tile tile = tiles[tileIndex];
					if (lightlevels[x][y] == -1)
					{
						tile.setCurrentLightLevel(tile.getLightLevel());
					}
					else
					{
						tile.setCurrentLightLevel(lightlevels[x][y]);
					}
				}
			}
		}
	}

	@Override
	public Animation spawn(AnimationContext context)
	{
		return new TestShadowPassingAnimation(500, 7,3,14,5,
			new int[][]
				{
					{-1, -1, -1, -1, -1},
					{-1, -1, 10, -1, -1},
					{-1, 10, 10, 10, -1},
					{-1, -1, 10, -1, -1},
					{-1, -1, -1, -1, -1},
				});
	}

	@Override
	public void destroy()
	{
		Map map = Maze.getInstance().getCurrentZone().getMap();

		int[][] originalLightLevels = new int[shadowShape.length][shadowShape[0].length];
		for (int i = 0; i < originalLightLevels.length; i++)
		{
			Arrays.fill(originalLightLevels[i], -1);
		}

		updateLightLevels(
			map,
			map.getTiles(),
			tileX,
			tileY,
			originalLightLevels);
	}

	@Override
	public boolean isFinished()
	{
		return System.currentTimeMillis() - startTime >= duration;
	}

	public int getDuration()
	{
		return duration;
	}

	public void setDuration(int duration)
	{
		this.duration = duration;
	}
}
