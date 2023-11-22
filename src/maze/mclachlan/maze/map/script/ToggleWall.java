package mclachlan.maze.map.script;

import java.awt.Point;
import java.util.*;
import mclachlan.crusader.MouseClickScript;
import mclachlan.crusader.Texture;
import mclachlan.crusader.Wall;
import mclachlan.maze.game.Maze;
import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.game.MazeVariables;
import mclachlan.maze.map.TileScript;
import mclachlan.maze.util.MazeException;

/**
 *
 */
public class ToggleWall extends TileScript
{
	enum State {STATE_1, STATE_2};

	// identity of the wall to toggle
	private final boolean horizontalWall;
	private final int wallIndex;

	/** Variable to store the state of this lever. */
	private final String mazeVariable;

	// state 1 wall attributes
	private final Texture state1Texture;
	private final Texture state1MaskTexture;
	private final boolean state1Visible;
	private final boolean state1Solid;
	private final int state1Height;
	private final MouseClickScript state1MouseClickScript;
	private final MouseClickScript state1MaskTextureMouseClickScript;

	// state 2 wall attributes
	private final Texture state2Texture;
	private final Texture state2MaskTexture;
	private final boolean state2Visible;
	private final boolean state2Solid;
	private final int state2Height;
	private final MouseClickScript state2MouseClickScript;
	private final MouseClickScript state2MaskTextureMouseClickScript;

	public ToggleWall(boolean horizontalWall, int wallIndex, String mazeVariable,
		Texture state1Texture, Texture state1MaskTexture, boolean state1Visible,
		boolean state1Solid, int state1Height,
		MouseClickScript state1MouseClickScript,
		MouseClickScript state1MaskTextureMouseClickScript, Texture state2Texture,
		Texture state2MaskTexture, boolean state2Visible, boolean state2Solid,
		int state2Height, MouseClickScript state2MouseClickScript,
		MouseClickScript state2MaskTextureMouseClickScript)
	{
		this.horizontalWall = horizontalWall;
		this.wallIndex = wallIndex;

		this.mazeVariable = mazeVariable;

		this.state1Texture = state1Texture;
		this.state1MaskTexture = state1MaskTexture;
		this.state1Visible = state1Visible;
		this.state1Solid = state1Solid;
		this.state1Height = state1Height;
		this.state1MouseClickScript = state1MouseClickScript;
		this.state1MaskTextureMouseClickScript = state1MaskTextureMouseClickScript;

		this.state2Texture = state2Texture;
		this.state2MaskTexture = state2MaskTexture;
		this.state2Visible = state2Visible;
		this.state2Solid = state2Solid;
		this.state2Height = state2Height;
		this.state2MouseClickScript = state2MouseClickScript;
		this.state2MaskTextureMouseClickScript = state2MaskTextureMouseClickScript;
	}

	@Override
	public void initialise(Maze maze, Point tile, int tileIndex)
	{
		setWallAttributes(maze);
	}

	@Override
	public List<MazeEvent> execute(Maze maze, Point tile, Point previousTile,
		int facing)
	{
		setWallAttributes(maze);
		return null;
	}

	private void setWallAttributes(Maze maze)
	{
		Wall wall = maze.getCurrentZone().getMap().getWall(horizontalWall, wallIndex);

		switch (State.valueOf(MazeVariables.get(this.mazeVariable)))
		{
			case STATE_1:
				wall.setTexture(state1Texture);
				wall.setMaskTexture(state1MaskTexture);
				wall.setMaskTextureMouseClickScript(state1MaskTextureMouseClickScript);
				wall.setSolid(state1Solid);
				wall.setVisible(state1Visible);
				wall.setHeight(state1Height);
				wall.setMouseClickScript(state1MouseClickScript);
				wall.setMaskTextureMouseClickScript(state1MaskTextureMouseClickScript);
				break;
			case STATE_2:
				wall.setTexture(state2Texture);
				wall.setMaskTexture(state2MaskTexture);
				wall.setMaskTextureMouseClickScript(state2MaskTextureMouseClickScript);
				wall.setSolid(state2Solid);
				wall.setVisible(state2Visible);
				wall.setHeight(state2Height);
				wall.setMouseClickScript(state2MouseClickScript);
				wall.setMaskTextureMouseClickScript(state2MaskTextureMouseClickScript);
				break;
			default:
				throw new MazeException("invalid state "+MazeVariables.get(this.mazeVariable));
		}
	}
}
