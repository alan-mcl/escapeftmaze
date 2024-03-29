package mclachlan.maze.map.script;

import java.awt.Point;
import java.util.*;
import mclachlan.crusader.Map;
import mclachlan.crusader.Texture;
import mclachlan.crusader.Wall;
import mclachlan.maze.data.Database;
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
	private final boolean state1Secret;
	private final int state1Height;

	// state 2 wall attributes
	private final Texture state2Texture;
	private final Texture state2MaskTexture;
	private final boolean state2Visible;
	private final boolean state2Solid;
	private final boolean state2Secret;
	private final int state2Height;

	// something to do before and afterwards
	private final String preToggleScript;
	private final String postToggleScript;

	public ToggleWall(
		String mazeVariable,
		int wallIndex,
		boolean horizontalWall,
		Texture state1Texture,
		Texture state1MaskTexture,
		boolean state1Visible,
		boolean state1Solid,
		boolean state1Secret,
		int state1Height,
		Texture state2Texture,
		Texture state2MaskTexture,
		boolean state2Visible,
		boolean state2Solid,
		boolean state2Secret,
		int state2Height,
		String preToggleScript,
		String postToggleScript)
	{
		this.horizontalWall = horizontalWall;
		this.wallIndex = wallIndex;

		this.mazeVariable = mazeVariable;

		this.state1Texture = state1Texture;
		this.state1MaskTexture = state1MaskTexture;
		this.state1Visible = state1Visible;
		this.state1Solid = state1Solid;
		this.state1Secret = state1Secret;
		this.state1Height = state1Height;

		this.state2Texture = state2Texture;
		this.state2MaskTexture = state2MaskTexture;
		this.state2Visible = state2Visible;
		this.state2Solid = state2Solid;
		this.state2Secret = state2Secret;
		this.state2Height = state2Height;

		this.preToggleScript = preToggleScript;
		this.postToggleScript = postToggleScript;
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

		setWallAttributes(
			maze,
			mazeVariable,
			wallIndex,
			horizontalWall,
			state1Texture,
			state1MaskTexture,
			state1Visible,
			state1Solid,
			state1Height,
			state2Texture,
			state2MaskTexture,
			state2Visible,
			state2Solid,
			state2Height);
	}

	@Override
	public List<MazeEvent> execute(Maze maze, Point tile, Point previousTile,
		int facing)
	{
		List<MazeEvent> result = new ArrayList<>();

		if (preToggleScript != null)
		{
			result.addAll(Database.getInstance().getMazeScript(preToggleScript).getEvents());
		}

		result.add(
			new ToggleWallEvent(mazeVariable,
				wallIndex,
				horizontalWall,
				state1Texture,
				state1MaskTexture,
				state1Visible,
				state1Solid,
				state1Height,
				state2Texture,
				state2MaskTexture,
				state2Visible,
				state2Solid,
				state2Height));

		if (postToggleScript != null)
		{
			result.addAll(Database.getInstance().getMazeScript(postToggleScript).getEvents());
		}

		return result;
	}

	static void setWallAttributes(
		Maze maze,
		String mazeVariable,
		int wallIndex,
		boolean horizontalWall,
		Texture state1Texture,
		Texture state1MaskTexture,
		boolean state1Visible,
		boolean state1Solid,
		int state1Height,
		Texture state2Texture,
		Texture state2MaskTexture,
		boolean state2Visible,
		boolean state2Solid,
		int state2Height)
	{
		Wall origWall = maze.getCurrentZone().getMap().getWall(wallIndex, horizontalWall);
		Wall newWall = new Wall();

		switch (State.valueOf(MazeVariables.get(mazeVariable)))
		{
			case STATE_1:
				newWall.setTexture(state1Texture==null?Map.NO_WALL:state1Texture);
				newWall.setMaskTexture(state1MaskTexture);
				newWall.setSolid(state1Solid);
				newWall.setVisible(state1Visible);
				newWall.setHeight(state1Height);
				break;
			case STATE_2:
				newWall.setTexture(state2Texture==null?Map.NO_WALL:state2Texture);
				newWall.setMaskTexture(state2MaskTexture);
				newWall.setSolid(state2Solid);
				newWall.setVisible(state2Visible);
				newWall.setHeight(state2Height);
				break;
			default:
				throw new MazeException("invalid state "+MazeVariables.get(mazeVariable));
		}

		// copy any mouse click scripts
		newWall.setMouseClickScript(origWall.getMouseClickScript());
		newWall.setMaskTextureMouseClickScript(origWall.getMaskTextureMouseClickScript());

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

	public boolean isState1Secret()
	{
		return state1Secret;
	}

	public boolean isState2Secret()
	{
		return state2Secret;
	}

	public int getState2Height()
	{
		return state2Height;
	}

	public String getPreToggleScript()
	{
		return preToggleScript;
	}

	public String getPostToggleScript()
	{
		return postToggleScript;
	}

	@Override
	public boolean isHiddenSecret()
	{
		if (MazeVariables.get(mazeVariable) != null)
		{
			switch (State.valueOf(MazeVariables.get(mazeVariable)))
			{
				case STATE_1:
					return state1Secret && super.isHiddenSecret();
				case STATE_2:
					return state2Secret && super.isHiddenSecret();
				default:
					throw new MazeException("invalid state " + MazeVariables.get(mazeVariable));
			}
		}
		else
		{
			return super.isHiddenSecret();
		}
	}
}
