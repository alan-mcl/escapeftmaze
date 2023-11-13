package mclachlan.maze.data.v1;

import mclachlan.maze.util.MazeException;

/**
 *
 */
public abstract class DataObject
{
	private String campaign;

	public abstract String getName();

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
