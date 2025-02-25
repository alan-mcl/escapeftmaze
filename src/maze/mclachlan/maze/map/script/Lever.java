package mclachlan.maze.map.script;

import java.awt.Point;
import java.util.*;
import mclachlan.crusader.EngineObject;
import mclachlan.crusader.Map;
import mclachlan.crusader.MouseClickScript;
import mclachlan.crusader.Texture;
import mclachlan.maze.data.Database;
import mclachlan.maze.game.Maze;
import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.game.MazeScript;
import mclachlan.maze.game.MazeVariables;
import mclachlan.maze.game.event.MazeScriptEvent;
import mclachlan.maze.map.TileScript;
import mclachlan.maze.util.MazeException;

/**
 *
 */
public class Lever extends TileScript
{
	enum State {POSITION_1, POSITION_2};

	/** Variable to store the state of this lever. */
	private String mazeVariable;

	/** textures representing an POSITION_1 state */
	private String northTexture, southTexture, eastTexture, westTexture;

	/** script on transition */
	private MazeScript preTransitionScript, postTransitionScript;

	/** raycaster object */
	private EngineObject engineObject;

	public Lever()
	{
	}

	/*-------------------------------------------------------------------------*/
	public Lever(
		String northTexture,
		String southTexture,
		String eastTexture,
		String westTexture,
		String mazeVariable,
		MazeScript preTransitionScript,
		MazeScript postTransitionScript)
	{
		this.mazeVariable = mazeVariable;
		this.northTexture = northTexture;
		this.southTexture = southTexture;
		this.eastTexture = eastTexture;
		this.westTexture = westTexture;
		this.preTransitionScript = preTransitionScript;
		this.postTransitionScript = postTransitionScript;

		initEngineObject();
	}

	/*-------------------------------------------------------------------------*/
	public void initEngineObject()
	{
		this.engineObject = new EngineObject(
			null,
			getTexture(northTexture),
			getTexture(southTexture),
			getTexture(eastTexture),
			getTexture(westTexture),
			0,
			false,
			null,
			null,
			EngineObject.Alignment.BOTTOM);
	}

	/*-------------------------------------------------------------------------*/
	private Texture getTexture(String texture)
	{
		return Database.getInstance().getMazeTexture(texture).getTexture();
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public void initialise(Maze maze, Point tile, int tileIndex)
	{
		getEngineObject().setTileIndex(tileIndex);

		if (MazeVariables.get(this.mazeVariable) == null)
		{
			// lever has not been encountered yet, default to POSITION_1
			MazeVariables.set(this.mazeVariable, State.POSITION_1.name());
		}

		setTextureBasedOnState();

		// add the mouse click script
		getEngineObject().setMouseClickScript(new LeverMouseClickScript(this));

		maze.addObject(getEngineObject());
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * Set the textures based on the lever state
	 */
	private void setTextureBasedOnState()
	{
		switch (State.valueOf(MazeVariables.get(this.mazeVariable)))
		{
			case POSITION_1 ->
			{
				getEngineObject().setNorthTexture(getTexture(northTexture));
				getEngineObject().setSouthTexture(getTexture(southTexture));
				getEngineObject().setEastTexture(getTexture(eastTexture));
				getEngineObject().setWestTexture(getTexture(westTexture));
			}
			case POSITION_2 ->
			{
				// swap textures
				getEngineObject().setNorthTexture(getTexture(southTexture));
				getEngineObject().setSouthTexture(getTexture(northTexture));
				getEngineObject().setEastTexture(getTexture(westTexture));
				getEngineObject().setWestTexture(getTexture(eastTexture));
			}
			default ->
				throw new MazeException("invalid state " + MazeVariables.get(this.mazeVariable));
		}
	}

	/*-------------------------------------------------------------------------*/

	public String getMazeVariable()
	{
		return mazeVariable;
	}

	public String getNorthTexture()
	{
		return northTexture;
	}

	public String getSouthTexture()
	{
		return southTexture;
	}

	public String getEastTexture()
	{
		return eastTexture;
	}

	public String getWestTexture()
	{
		return westTexture;
	}

	public MazeScript getPreTransitionScript()
	{
		return preTransitionScript;
	}

	public MazeScript getPostTransitionScript()
	{
		return postTransitionScript;
	}

	public void setMazeVariable(String mazeVariable)
	{
		this.mazeVariable = mazeVariable;
	}

	public void setNorthTexture(String northTexture)
	{
		this.northTexture = northTexture;
	}

	public void setSouthTexture(String southTexture)
	{
		this.southTexture = southTexture;
	}

	public void setEastTexture(String eastTexture)
	{
		this.eastTexture = eastTexture;
	}

	public void setWestTexture(String westTexture)
	{
		this.westTexture = westTexture;
	}

	public void setPreTransitionScript(MazeScript preTransitionScript)
	{
		this.preTransitionScript = preTransitionScript;
	}

	public void setPostTransitionScript(
		MazeScript postTransitionScript)
	{
		this.postTransitionScript = postTransitionScript;
	}

	public EngineObject getEngineObject()
	{
		if (engineObject == null)
		{
			initEngineObject();
		}

		return engineObject;
	}

	public void setEngineObject(EngineObject engineObject)
	{
		this.engineObject = engineObject;
	}

	/*-------------------------------------------------------------------------*/
	private static class ChangeLeverState extends MazeEvent
	{
		private final Lever lever;

		public ChangeLeverState(Lever lever)
		{
			this.lever = lever;
		}

		@Override
		public List<MazeEvent> resolve()
		{
			List<MazeEvent> result = new ArrayList<>();

			result.add(new MazeScriptEvent("_PULL_LEVER_"));

			switch (State.valueOf(MazeVariables.get(lever.mazeVariable)))
			{
				case POSITION_1 ->
					MazeVariables.set(lever.mazeVariable, State.POSITION_2.name());
				case POSITION_2 ->
					MazeVariables.set(lever.mazeVariable, State.POSITION_1.name());
				default ->
					throw new MazeException("invalid state " + MazeVariables.get(lever.mazeVariable));
			}

			lever.setTextureBasedOnState();

			return result;
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
		if (!(o instanceof Lever))
		{
			return false;
		}
		if (!super.equals(o))
		{
			return false;
		}

		Lever lever = (Lever)o;

		if (getMazeVariable() != null ? !getMazeVariable().equals(lever.getMazeVariable()) : lever.getMazeVariable() != null)
		{
			return false;
		}
		if (getNorthTexture() != null ? !getNorthTexture().equals(lever.getNorthTexture()) : lever.getNorthTexture() != null)
		{
			return false;
		}
		if (getSouthTexture() != null ? !getSouthTexture().equals(lever.getSouthTexture()) : lever.getSouthTexture() != null)
		{
			return false;
		}
		if (getEastTexture() != null ? !getEastTexture().equals(lever.getEastTexture()) : lever.getEastTexture() != null)
		{
			return false;
		}
		if (getWestTexture() != null ? !getWestTexture().equals(lever.getWestTexture()) : lever.getWestTexture() != null)
		{
			return false;
		}
		if (getPreTransitionScript() != null ? !getPreTransitionScript().equals(lever.getPreTransitionScript()) : lever.getPreTransitionScript() != null)
		{
			return false;
		}
		if (getPostTransitionScript() != null ? !getPostTransitionScript().equals(lever.getPostTransitionScript()) : lever.getPostTransitionScript() != null)
		{
			return false;
		}
		if (!(getEngineObject() != null ? getEngineObject().equals(lever.getEngineObject()) : lever.getEngineObject() == null))
		{
			return false;
		}

		return true;
	}

	@Override
	public int hashCode()
	{
		int result = super.hashCode();
		result = 31 * result + (getMazeVariable() != null ? getMazeVariable().hashCode() : 0);
		result = 31 * result + (getNorthTexture() != null ? getNorthTexture().hashCode() : 0);
		result = 31 * result + (getSouthTexture() != null ? getSouthTexture().hashCode() : 0);
		result = 31 * result + (getEastTexture() != null ? getEastTexture().hashCode() : 0);
		result = 31 * result + (getWestTexture() != null ? getWestTexture().hashCode() : 0);
		result = 31 * result + (getPreTransitionScript() != null ? getPreTransitionScript().hashCode() : 0);
		result = 31 * result + (getPostTransitionScript() != null ? getPostTransitionScript().hashCode() : 0);
		result = 31 * result + (getEngineObject() != null ? getEngineObject().hashCode() : 0);
		return result;
	}

	/*-------------------------------------------------------------------------*/
	private static class LeverMouseClickScript implements MouseClickScript
	{
		private final Lever lever;

		public LeverMouseClickScript(Lever lever)
		{
			this.lever = lever;
		}

		@Override
		public void initialise(Map map)
		{

		}

		@Override
		public void execute(Map map)
		{
			List<MazeEvent> events = new ArrayList<>();

			if (lever.preTransitionScript != null)
			{
				events.addAll(lever.preTransitionScript.getEvents());
			}

			events.add(new ChangeLeverState(lever));

			if (lever.postTransitionScript != null)
			{
				events.addAll(lever.postTransitionScript.getEvents());
			}

			Maze.getInstance().appendEvents(events);
		}

		@Override
		public int getMaxDist()
		{
			return 1;
		}
	}
}
