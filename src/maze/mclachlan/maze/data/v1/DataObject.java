package mclachlan.maze.data.v1;

import mclachlan.maze.data.v2.V2DataObject;
import mclachlan.maze.util.MazeException;

/**
 *
 */
public abstract class DataObject implements V2DataObject
{
	private String campaign;

	public String getCampaign()
	{
		return campaign;
	}

	public void setCampaign(String campaign)
	{
		if (campaign == null)
		{
			throw new MazeException("invalid NULL campaign");
		}
		this.campaign = campaign;
	}
}
