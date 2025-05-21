package mclachlan.maze.stat;

import java.util.*;
import mclachlan.crusader.ObjectScript;
import mclachlan.maze.data.v1.DataObject;

/**
 *
 */
public class ObjectAnimations extends DataObject
{
	private String name;
	private List<ObjectScript> animationScripts;

	public ObjectAnimations()
	{
	}

	public ObjectAnimations(String name, List<ObjectScript> animationScripts)
	{
		this.name = name;
		this.animationScripts = animationScripts;
	}

	@Override
	public String getName()
	{
		return name;
	}

	@Override
	public void setName(String name)
	{
		this.name = name;
	}

	public List<ObjectScript> getAnimationScripts()
	{
		return animationScripts;
	}

	public void setAnimationScripts(
		List<ObjectScript> animationScripts)
	{
		this.animationScripts = animationScripts;
	}
}
