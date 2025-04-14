package mclachlan.maze.editor.swing.map;

import mclachlan.crusader.EngineObject;
import mclachlan.crusader.MouseClickScript;
import mclachlan.crusader.ObjectScript;
import mclachlan.crusader.Texture;

/**
 *
 */
public class SingleObjectProxy extends ObjectProxy
{
	EngineObject obj;

	public SingleObjectProxy(EngineObject obj)
	{
		this.obj = obj;
	}

	@Override
	public String getName()
	{
		return obj.getName();
	}

	@Override
	public void setName(String name)
	{
		obj.setName(name);
	}

	@Override
	public Texture getNorthTexture()
	{
		return obj.getNorthTexture();
	}

	@Override
	public void setNorthTexture(Texture northTexture)
	{
		obj.setNorthTexture(northTexture);
	}

	@Override
	public Texture getSouthTexture()
	{
		return obj.getSouthTexture();
	}

	@Override
	public void setSouthTexture(Texture southTexture)
	{
		obj.setSouthTexture(southTexture);
	}

	@Override
	public Texture getEastTexture()
	{
		return obj.getEastTexture();
	}

	@Override
	public void setEastTexture(Texture eastTexture)
	{
		obj.setEastTexture(eastTexture);
	}

	@Override
	public Texture getWestTexture()
	{
		return obj.getWestTexture();
	}

	@Override
	public void setWestTexture(Texture westTexture)
	{
		obj.setWestTexture(westTexture);
	}

	@Override
	public boolean isLightSource()
	{
		return obj.isLightSource();
	}

	@Override
	public void setLightSource(boolean lightSource)
	{
		obj.setLightSource(lightSource);
	}

	@Override
	public MouseClickScript getMouseClickScript()
	{
		return obj.getMouseClickScript();
	}

	@Override
	public void setMouseClickScript(MouseClickScript mouseClickScript)
	{
		obj.setMouseClickScript(mouseClickScript);
	}

	@Override
	public EngineObject.Alignment getVerticalAlignment()
	{
		return obj.getVerticalAlignment();
	}

	@Override
	public void setVerticalAlignment(EngineObject.Alignment verticalAlignment)
	{
		obj.setVerticalAlignment(verticalAlignment);
	}

	@Override
	public ObjectScript[] getScripts()
	{
		return obj.getScripts();
	}

	@Override
	public void setScripts(ObjectScript[] scripts)
	{
		obj.setScripts(scripts);
	}

	@Override
	public int getXPos()
	{
		return obj.getXPos();
	}

	@Override
	public void setXPos(int x)
	{
		obj.setXPos(x);
	}

	@Override
	public int getYPos()
	{
		return obj.getYPos();
	}

	@Override
	public void setYPos(int y)
	{
		obj.setYPos(y);
	}
}
