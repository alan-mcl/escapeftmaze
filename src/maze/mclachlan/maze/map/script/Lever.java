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
	private final String mazeVariable;

	/** textures representing an POSITION_1 state */
	private final String northTexture, southTexture, eastTexture, westTexture;

	/** script on transition */
	private final MazeScript preTransitionScript, postTransitionScript;

	/** raycaster object */
	private final EngineObject engineObject;

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

	private Texture getTexture(String texture)
	{
		return Database.getInstance().getMazeTexture(texture).getTexture();
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public void initialise(Maze maze, Point tile, int tileIndex)
	{
		engineObject.setTileIndex(tileIndex);

		if (MazeVariables.get(this.mazeVariable) == null)
		{
			// lever has not been encountered yet, default to POSITION_1
			MazeVariables.set(this.mazeVariable, State.POSITION_1.name());
		}

		setTextureBasedOnState();

		// add the mouse click script
		engineObject.setMouseClickScript(new LeverMouseClickScript(this));

		maze.addObject(engineObject);
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * Set the textures based on the lever state
	 */
	private void setTextureBasedOnState()
	{
		switch (State.valueOf(MazeVariables.get(this.mazeVariable)))
		{
			case POSITION_1:
				engineObject.setNorthTexture(getTexture(northTexture));
				engineObject.setSouthTexture(getTexture(southTexture));
				engineObject.setEastTexture(getTexture(eastTexture));
				engineObject.setWestTexture(getTexture(westTexture));
				break;
			case POSITION_2:
				// swap textures
				engineObject.setNorthTexture(getTexture(southTexture));
				engineObject.setSouthTexture(getTexture(northTexture));
				engineObject.setEastTexture(getTexture(westTexture));
				engineObject.setWestTexture(getTexture(eastTexture));
				break;
			default:
				throw new MazeException("invalid state "+MazeVariables.get(this.mazeVariable));
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
				case POSITION_1:
					MazeVariables.set(lever.mazeVariable, State.POSITION_2.name());
					break;
				case POSITION_2:
					MazeVariables.set(lever.mazeVariable, State.POSITION_1.name());
					break;
				default:
					throw new MazeException("invalid state "+MazeVariables.get(lever.mazeVariable));
			}

			lever.setTextureBasedOnState();

			return result;
		}
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
