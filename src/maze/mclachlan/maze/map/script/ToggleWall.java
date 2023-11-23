package mclachlan.maze.map.script;

import java.awt.Point;
import java.util.*;
import mclachlan.crusader.Map;
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

	public ToggleWall(
		String mazeVariable, int wallIndex, boolean horizontalWall,
		Texture state1Texture,
		Texture state1MaskTexture,
		boolean state1Visible,
		boolean state1Solid,
		int state1Height,
		MouseClickScript state1MouseClickScript,
		MouseClickScript state1MaskTextureMouseClickScript,
		Texture state2Texture,
		Texture state2MaskTexture,
		boolean state2Visible,
		boolean state2Solid,
		int state2Height,
		MouseClickScript state2MouseClickScript,
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
		if (MazeVariables.get(this.mazeVariable) == null)
		{
			// init to state 1
			MazeVariables.set(this.mazeVariable, State.STATE_1.name());
		}

		// make sure any textures are added to the raycaster
		if (state1Texture != null)
		{
			maze.getUi().addTexture(state1Texture);
		}
		if (state1MaskTexture != null)
		{
			maze.getUi().addTexture(state1MaskTexture);
		}
		if (state2Texture != null)
		{
			maze.getUi().addTexture(state2Texture);
		}
		if (state2MaskTexture != null)
		{
			maze.getUi().addTexture(state2MaskTexture);
		}


		setWallAttributes(maze);
	}

	@Override
	public List<MazeEvent> execute(Maze maze, Point tile, Point previousTile,
		int facing)
	{
		switch (State.valueOf(MazeVariables.get(this.mazeVariable)))
		{
			case STATE_1:
				MazeVariables.set(this.mazeVariable, State.STATE_2.name());
				break;
			case STATE_2:
				MazeVariables.set(this.mazeVariable, State.STATE_1.name());
				break;
			default:
				throw new MazeException("invalid state: "+MazeVariables.get(this.mazeVariable));
		}

		setWallAttributes(maze);
		return null;
	}

	private void setWallAttributes(Maze maze)
	{
		Wall newWall = new Wall();//maze.getCurrentZone().getMap().getWall(horizontalWall, wallIndex);

		switch (State.valueOf(MazeVariables.get(this.mazeVariable)))
		{
			case STATE_1:
				newWall.setTexture(state1Texture==null?Map.NO_WALL:state1Texture);
				newWall.setMaskTexture(state1MaskTexture);
				newWall.setMaskTextureMouseClickScript(state1MaskTextureMouseClickScript);
				newWall.setSolid(state1Solid);
				newWall.setVisible(state1Visible);
				newWall.setHeight(state1Height);
				newWall.setMouseClickScript(state1MouseClickScript);
				newWall.setMaskTextureMouseClickScript(state1MaskTextureMouseClickScript);
				break;
			case STATE_2:
				newWall.setTexture(state2Texture==null?Map.NO_WALL:state2Texture);
				newWall.setMaskTexture(state2MaskTexture);
				newWall.setMaskTextureMouseClickScript(state2MaskTextureMouseClickScript);
				newWall.setSolid(state2Solid);
				newWall.setVisible(state2Visible);
				newWall.setHeight(state2Height);
				newWall.setMouseClickScript(state2MouseClickScript);
				newWall.setMaskTextureMouseClickScript(state2MaskTextureMouseClickScript);
				break;
			default:
				throw new MazeException("invalid state "+MazeVariables.get(this.mazeVariable));
		}

		maze.getCurrentZone().getMap().setWall(wallIndex, horizontalWall, newWall);
	}

	/*-------------------------------------------------------------------------*/

	public boolean isHorizontalWall()
	{
		return horizontalWall;
	}

	public int getWallIndex()
	{
		return wallIndex;
	}

	public String getMazeVariable()
	{
		return mazeVariable;
	}

	public Texture getState1Texture()
	{
		return state1Texture;
	}

	public Texture getState1MaskTexture()
	{
		return state1MaskTexture;
	}

	public boolean isState1Visible()
	{
		return state1Visible;
	}

	public boolean isState1Solid()
	{
		return state1Solid;
	}

	public int getState1Height()
	{
		return state1Height;
	}

	public MouseClickScript getState1MouseClickScript()
	{
		return state1MouseClickScript;
	}

	public MouseClickScript getState1MaskTextureMouseClickScript()
	{
		return state1MaskTextureMouseClickScript;
	}

	public Texture getState2Texture()
	{
		return state2Texture;
	}

	public Texture getState2MaskTexture()
	{
		return state2MaskTexture;
	}

	public boolean isState2Visible()
	{
		return state2Visible;
	}

	public boolean isState2Solid()
	{
		return state2Solid;
	}

	public int getState2Height()
	{
		return state2Height;
	}

	public MouseClickScript getState2MouseClickScript()
	{
		return state2MouseClickScript;
	}

	public MouseClickScript getState2MaskTextureMouseClickScript()
	{
		return state2MaskTextureMouseClickScript;
	}
}
