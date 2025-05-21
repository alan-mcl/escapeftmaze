package mclachlan.maze.stat.combat;

import java.util.*;
import mclachlan.crusader.EngineObject;
import mclachlan.crusader.Texture;
import mclachlan.maze.game.Maze;
import mclachlan.maze.game.MazeEvent;

/**
 *
 */
public class TempChangeTextureEvent extends MazeEvent
{
	private final EngineObject obj;
	final private Texture texture;

	public TempChangeTextureEvent(EngineObject obj, Texture texture)
	{
		this.obj = obj;
		this.texture = texture;
	}

	@Override
	public List<MazeEvent> resolve()
	{
		Maze.getInstance().getUi().tempChangeTexture(obj, texture);
		return null;
	}
}
