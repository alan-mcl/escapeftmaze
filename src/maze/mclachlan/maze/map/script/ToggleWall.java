package mclachlan.maze.map.script;

import java.awt.Point;
import java.util.*;
import mclachlan.crusader.Map;
import mclachlan.crusader.Texture;
import mclachlan.crusader.Wall;
import mclachlan.maze.data.Database;
import mclachlan.maze.data.MazeTexture;
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
	private boolean horizontalWall;
	private int wallIndex;

	/** Variable to store the state of this lever. */
	private String mazeVariable;

	// todo: multiple textures for wall heights

	// state 1 wall attributes
	private MazeTexture state1Texture;
	private MazeTexture state1MaskTexture;
	private boolean state1Visible;
	private boolean state1Solid;
	private boolean state1Secret;
	private int state1Height;

	// state 2 wall attributes
	private MazeTexture state2Texture;
	private MazeTexture state2MaskTexture;
	private boolean state2Visible;
	private boolean state2Solid;
	private boolean state2Secret;
	private int state2Height;

	// something to do before and afterwards
	private String preToggleScript;
	private String postToggleScript;

	public ToggleWall()
	{
	}

	/*-------------------------------------------------------------------------*/
	public ToggleWall(
		String mazeVariable,
		int wallIndex,
		boolean horizontalWall,
		MazeTexture state1Texture,
		MazeTexture state1MaskTexture,
		boolean state1Visible,
		boolean state1Solid,
		boolean state1Secret,
		int state1Height,
		MazeTexture state2Texture,
		MazeTexture state2MaskTexture,
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
			maze.getUi().addTexture(state1Texture.getTexture());
		}
		if (state1MaskTexture != null)
		{
			maze.getUi().addTexture(state1MaskTexture.getTexture());
		}
		if (state2Texture != null)
		{
			maze.getUi().addTexture(state2Texture.getTexture());
		}
		if (state2MaskTexture != null)
		{
			maze.getUi().addTexture(state2MaskTexture.getTexture());
		}

		setWallAttributes(
			maze,
			mazeVariable,
			wallIndex,
			horizontalWall,
			state1Texture.getTexture(),
			state1MaskTexture == null ? null : state1MaskTexture.getTexture(),
			state1Visible,
			state1Solid,
			state1Height,
			state2Texture.getTexture(),
			state2MaskTexture == null ? null : state2MaskTexture.getTexture(),
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
				state1Texture.getTexture(),
				state1MaskTexture.getTexture(),
				state1Visible,
				state1Solid,
				state1Height,
				state2Texture.getTexture(),
				state2MaskTexture.getTexture(),
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
		Wall newWall = new Wall(
			origWall.getTextures(),
			origWall.getMaskTextures(),
			origWall.isVisible(),
			origWall.isSolid(),
			origWall.getHeight(),
			origWall.getMouseClickScript(),
			origWall.getMaskTextureMouseClickScript(),
			origWall.getInternalScript());

		switch (State.valueOf(MazeVariables.get(mazeVariable)))
		{
			case STATE_1 ->
			{
				newWall.setTexture(0, state1Texture == null ? Map.NO_WALL : state1Texture);
				newWall.setMaskTextures(state1MaskTexture==null ? null : new Texture[]{state1MaskTexture});
				newWall.setSolid(state1Solid);
				newWall.setVisible(state1Visible);
				newWall.setHeight(state1Height);
			}
			case STATE_2 ->
			{
				newWall.setTexture(0, state2Texture == null ? Map.NO_WALL : state2Texture);
				newWall.setMaskTextures(state2MaskTexture==null ? null : new Texture[]{state2MaskTexture});
				newWall.setSolid(state2Solid);
				newWall.setVisible(state2Visible);
				newWall.setHeight(state2Height);
			}
			default ->
				throw new MazeException("invalid state " + MazeVariables.get(mazeVariable));
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

	public MazeTexture getState1Texture()
	{
		return state1Texture;
	}

	public MazeTexture getState1MaskTexture()
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

	public MazeTexture getState2Texture()
	{
		return state2Texture;
	}

	public MazeTexture getState2MaskTexture()
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

	public void setHorizontalWall(boolean horizontalWall)
	{
		this.horizontalWall = horizontalWall;
	}

	public void setWallIndex(int wallIndex)
	{
		this.wallIndex = wallIndex;
	}

	public void setMazeVariable(String mazeVariable)
	{
		this.mazeVariable = mazeVariable;
	}

	public void setState1Texture(MazeTexture state1Texture)
	{
		this.state1Texture = state1Texture;
	}

	public void setState1MaskTexture(MazeTexture state1MaskTexture)
	{
		this.state1MaskTexture = state1MaskTexture;
	}

	public void setState1Visible(boolean state1Visible)
	{
		this.state1Visible = state1Visible;
	}

	public void setState1Solid(boolean state1Solid)
	{
		this.state1Solid = state1Solid;
	}

	public void setState1Secret(boolean state1Secret)
	{
		this.state1Secret = state1Secret;
	}

	public void setState1Height(int state1Height)
	{
		this.state1Height = state1Height;
	}

	public void setState2Texture(MazeTexture state2Texture)
	{
		this.state2Texture = state2Texture;
	}

	public void setState2MaskTexture(MazeTexture state2MaskTexture)
	{
		this.state2MaskTexture = state2MaskTexture;
	}

	public void setState2Visible(boolean state2Visible)
	{
		this.state2Visible = state2Visible;
	}

	public void setState2Solid(boolean state2Solid)
	{
		this.state2Solid = state2Solid;
	}

	public void setState2Secret(boolean state2Secret)
	{
		this.state2Secret = state2Secret;
	}

	public void setState2Height(int state2Height)
	{
		this.state2Height = state2Height;
	}

	public void setPreToggleScript(String preToggleScript)
	{
		this.preToggleScript = preToggleScript;
	}

	public void setPostToggleScript(String postToggleScript)
	{
		this.postToggleScript = postToggleScript;
	}

	@Override
	public boolean isHiddenSecret()
	{
		if (MazeVariables.get(mazeVariable) != null)
		{
			return switch (State.valueOf(MazeVariables.get(mazeVariable)))
				{
					case STATE_1 -> state1Secret && super.isHiddenSecret();
					case STATE_2 -> state2Secret && super.isHiddenSecret();
					default ->
						throw new MazeException("invalid state " + MazeVariables.get(mazeVariable));
				};
		}
		else
		{
			return super.isHiddenSecret();
		}
	}

	/*-------------------------------------------------------------------------*/

	@Override
	public boolean equals(Object o)
	{
		if (this == o)
		{
			return true;
		}
		if (o == null || getClass() != o.getClass())
		{
			return false;
		}
		if (!super.equals(o))
		{
			return false;
		}

		ToggleWall that = (ToggleWall)o;

		if (isHorizontalWall() != that.isHorizontalWall())
		{
			return false;
		}
		if (getWallIndex() != that.getWallIndex())
		{
			return false;
		}
		if (isState1Visible() != that.isState1Visible())
		{
			return false;
		}
		if (isState1Solid() != that.isState1Solid())
		{
			return false;
		}
		if (isState1Secret() != that.isState1Secret())
		{
			return false;
		}
		if (getState1Height() != that.getState1Height())
		{
			return false;
		}
		if (isState2Visible() != that.isState2Visible())
		{
			return false;
		}
		if (isState2Solid() != that.isState2Solid())
		{
			return false;
		}
		if (isState2Secret() != that.isState2Secret())
		{
			return false;
		}
		if (getState2Height() != that.getState2Height())
		{
			return false;
		}
		if (getMazeVariable() != null ? !getMazeVariable().equals(that.getMazeVariable()) : that.getMazeVariable() != null)
		{
			return false;
		}
		if (getState1Texture() != null ? !getState1Texture().equals(that.getState1Texture()) : that.getState1Texture() != null)
		{
			return false;
		}
		if (getState1MaskTexture() != null ? !getState1MaskTexture().equals(that.getState1MaskTexture()) : that.getState1MaskTexture() != null)
		{
			return false;
		}
		if (getState2Texture() != null ? !getState2Texture().equals(that.getState2Texture()) : that.getState2Texture() != null)
		{
			return false;
		}
		if (getState2MaskTexture() != null ? !getState2MaskTexture().equals(that.getState2MaskTexture()) : that.getState2MaskTexture() != null)
		{
			return false;
		}
		if (getPreToggleScript() != null ? !getPreToggleScript().equals(that.getPreToggleScript()) : that.getPreToggleScript() != null)
		{
			return false;
		}
		return getPostToggleScript() != null ? getPostToggleScript().equals(that.getPostToggleScript()) : that.getPostToggleScript() == null;
	}

	@Override
	public int hashCode()
	{
		int result = super.hashCode();
		result = 31 * result + (isHorizontalWall() ? 1 : 0);
		result = 31 * result + getWallIndex();
		result = 31 * result + (getMazeVariable() != null ? getMazeVariable().hashCode() : 0);
		result = 31 * result + (getState1Texture() != null ? getState1Texture().hashCode() : 0);
		result = 31 * result + (getState1MaskTexture() != null ? getState1MaskTexture().hashCode() : 0);
		result = 31 * result + (isState1Visible() ? 1 : 0);
		result = 31 * result + (isState1Solid() ? 1 : 0);
		result = 31 * result + (isState1Secret() ? 1 : 0);
		result = 31 * result + getState1Height();
		result = 31 * result + (getState2Texture() != null ? getState2Texture().hashCode() : 0);
		result = 31 * result + (getState2MaskTexture() != null ? getState2MaskTexture().hashCode() : 0);
		result = 31 * result + (isState2Visible() ? 1 : 0);
		result = 31 * result + (isState2Solid() ? 1 : 0);
		result = 31 * result + (isState2Secret() ? 1 : 0);
		result = 31 * result + getState2Height();
		result = 31 * result + (getPreToggleScript() != null ? getPreToggleScript().hashCode() : 0);
		result = 31 * result + (getPostToggleScript() != null ? getPostToggleScript().hashCode() : 0);
		return result;
	}
}
