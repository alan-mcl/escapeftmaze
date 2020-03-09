package mclachlan.maze.data.v1;

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
		this.campaign = campaign;
	}
}
